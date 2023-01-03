package me.xx2bab.caliper.ksp

import com.google.devtools.ksp.processing.*
import com.google.devtools.ksp.symbol.*
import com.google.devtools.ksp.validate
import com.squareup.kotlinpoet.javapoet.KotlinPoetJavaPoetPreview
import com.squareup.kotlinpoet.javapoet.toJTypeName
import com.squareup.kotlinpoet.ksp.toTypeName
import me.xx2bab.caliper.anno.*
import kotlin.collections.getOrPut
import me.xx2bab.caliper.ksp.Constants.KSP_OPTION_ANDROID_APP
import java.util.concurrent.atomic.AtomicBoolean

class CaliperProxyRulesAggregationProcessorProvider : SymbolProcessorProvider {
    override fun create(
        env: SymbolProcessorEnvironment
    ): SymbolProcessor {
        val logger = KSPLoggerWrapper(env.logger)
        val isAndroidAppModule = env.options[KSP_OPTION_ANDROID_APP].toBoolean()
        logger.lifecycle("isAndroidAppModule: $isAndroidAppModule")
        return CaliperProxyRulesAggregationProcessor(
            isAndroidAppModule, env.codeGenerator, logger
        )
    }
}

/**
 * The processor to aggregate all [@ProxyMethod] [@ProxyField] from [caliper-runtime] module.
 */
class CaliperProxyRulesAggregationProcessor(
    private val isAndroidAppModule: Boolean,
    private val codeGenerator: CodeGenerator,
    private val logger: KSPLoggerWrapper,
) : SymbolProcessor {

    // For per module
    private val metadataMap: MutableMap<String, ProxyMetaData> = mutableMapOf()

    // For app module only
    private val subMetaDataCollected = AtomicBoolean(false)
    private val aggregatedMetadata = ProxiedMetaData()
    private lateinit var aggregator: CaliperAggregator

    companion object {
        const val ERROR_ILLEGAL_CLASS_STRUCTURE = "The annotated element is not wrapped by a class."
        const val ERROR_MULTI_CALIPER_ANNOTATIONS =
            "More than one Caliper annotation is found on the current element %s."
        const val ERROR_NO_CALIPER_ANNOTATION = "No Caliper annotation is found on the current element %s."
    }

    override fun process(resolver: Resolver): List<KSAnnotated> {
        logger.lifecycle("process")

        // Load metadata from subprojects, put all of them into the `exportMetadata` of main project,
        val symbols = resolver.getSymbolsWithAnnotation(CaliperMethodProxy::class.qualifiedName!!)
            .plus(resolver.getSymbolsWithAnnotation(CaliperFieldProxy::class.qualifiedName!!))
        logger.lifecycle("Method + Field symbols: ${symbols.toList().size}")

        // val ret = symbols.filter { !it.validate() }.toList()
        symbols.filter { it is KSFunctionDeclaration && it.validate() }.forEach { it.accept(MetaCollector(), Unit) }

        if (!subMetaDataCollected.get()) {
            subMetaDataCollected.set(true)
            aggregator = CaliperAggregator(logger)
            aggregator.collect(aggregatedMetadata, resolver)
        }

        // To simplify the workflow, we only support one round processing,
        // since all proxy class are designated to be fixed (resolved elements).
        return emptyList()
    }

    override fun finish() {
        super.finish()
        logger.lifecycle("finish")
        logger.lifecycle("metadataMap size: ${metadataMap.size}")
        val generator = CaliperWrapperGenerator(metadataMap, codeGenerator, logger)
        val currProxiedMetadata = generator.generate()
        if (isAndroidAppModule) {
            aggregatedMetadata.proxiedMethods.addAll(currProxiedMetadata.proxiedMethods)
            aggregatedMetadata.proxiedFields.addAll(currProxiedMetadata.proxiedFields)
            aggregatedMetadata.mapKSFiles.addAll(currProxiedMetadata.mapKSFiles)
            aggregator.generate(aggregatedMetadata, codeGenerator)
        }
    }

    @OptIn(KotlinPoetJavaPoetPreview::class)
    inner class MetaCollector : KSVisitorVoid() {

        override fun visitFunctionDeclaration(function: KSFunctionDeclaration, data: Unit) {
            super.visitFunctionDeclaration(function, data)
            logger.info("visitFunctionDeclaration")

            val functionName = function.simpleName.asString()
            logger.lifecycle("functionName = $functionName")

            val methodProxyAnnotation =
                function.annotations.firstOrNull { it.shortName.asString() == CaliperMethodProxy::class.simpleName }
            val fieldProxyAnnotation =
                function.annotations.firstOrNull { it.shortName.asString() == CaliperFieldProxy::class.simpleName }
            if (methodProxyAnnotation != null && fieldProxyAnnotation != null) {
                logger.error(ERROR_MULTI_CALIPER_ANNOTATIONS.format(functionName))
                return
            }
            if (methodProxyAnnotation == null && fieldProxyAnnotation == null) {
                logger.error(ERROR_NO_CALIPER_ANNOTATION.format(functionName))
                return
            }

            val validAnno = if (methodProxyAnnotation != null) {
                methodProxyAnnotation!!
            } else {
                fieldProxyAnnotation!!
            }
            val targetClassName = validAnno.getParamValueByKey("className").toString()
            val targetElementName = if (methodProxyAnnotation != null) {
                methodProxyAnnotation!!.getParamValueByKey("methodName").toString()
            } else {
                fieldProxyAnnotation!!.getParamValueByKey("fieldName").toString()
            }
            val targetOpcode = validAnno.getParamValueByKey("opcode") as Int

            if ((function.parentDeclaration is KSClassDeclaration).not()) {
                logger.error(ERROR_ILLEGAL_CLASS_STRUCTURE)
                return
            }

            val currClass = function.parentDeclaration as KSClassDeclaration
            if (currClass.qualifiedName == null) {
                logger.error(ERROR_ILLEGAL_CLASS_STRUCTURE)
                return
            }
            if (currClass.containingFile == null) {
                logger.error(ERROR_ILLEGAL_CLASS_STRUCTURE)
                return
            }

            val className = currClass.qualifiedName!!.asString()
            logger.lifecycle("className = $className")

            val functionReturnType = function.returnType?.toTypeName()?.toJTypeName()
            logger.lifecycle("functionReturnType = $functionReturnType")

            val functionParameters = function.parameters
            val paramList = mutableListOf<MethodParam>()
            functionParameters.forEach {
                if (it.name != null) {
                    paramList.add(
                        MethodParam(
                            paramName = it.name!!.getShortName(), type = it.type.toTypeName().toJTypeName()
                        )
                    )
                } else {
                    logger.error(
                        "Can not parse the element's name of the type " + "${it.type.toTypeName().toString()}"
                    )
                }
            }

            val proxyMethod = ProxyMethod(
                methodName = functionName,
                params = paramList,
                returnType = functionReturnType,
                targetClassName = targetClassName,
                targetElementName = targetElementName,
                targetOpcode = targetOpcode,
                targetType = validAnno.shortName.asString()
            )
            val metaData = metadataMap.getOrPut(className) {
                ProxyMetaData(
                    className = currClass.asStarProjectedType().toTypeName().toJTypeName(),
                    sourceRef = currClass.containingFile!!,
                    methods = mutableListOf()
                )
            }

            metaData.methods.add(proxyMethod)
        }

    }

    private fun KSAnnotation.getParamValueByKey(key: String) = arguments
        .first { it.name != null && it.name!!.asString() == key }
        .value!!

}