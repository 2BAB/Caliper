package me.xx2bab.caliper.ksp

import com.google.devtools.ksp.processing.*
import com.google.devtools.ksp.symbol.*
import com.google.devtools.ksp.validate
import com.squareup.kotlinpoet.javapoet.KotlinPoetJavaPoetPreview
import com.squareup.kotlinpoet.javapoet.toJTypeName
import com.squareup.kotlinpoet.ksp.toTypeName
import me.xx2bab.caliper.anno.*
import kotlin.collections.getOrPut
import me.xx2bab.caliper.common.Constants.KSP_OPTION_ANDROID_APP
import me.xx2bab.caliper.common.Constants.KSP_OPTION_MODULE_NAME
import java.util.concurrent.atomic.AtomicBoolean

class CaliperProxyRulesAggregationProcessorProvider : SymbolProcessorProvider {
    override fun create(
        env: SymbolProcessorEnvironment
    ): SymbolProcessor {
        val logger = KSPLoggerWrapper(env.logger)
        val isAndroidAppModule = env.options[KSP_OPTION_ANDROID_APP].toBoolean()
        val moduleName = env.options[KSP_OPTION_MODULE_NAME].toString()
        logger.info("isAndroidAppModule: $isAndroidAppModule")
        logger.info("moduleName: $moduleName")
        return CaliperProxyRulesAggregationProcessor(
            isAndroidAppModule, moduleName, env.codeGenerator, logger
        )
    }
}

/**
 * The processor to aggregate all [@ProxyMethod] [@ProxyField] from [caliper-runtime] module.
 */
class CaliperProxyRulesAggregationProcessor(
    private val isAndroidAppModule: Boolean,
    private val moduleName: String,
    private val codeGenerator: CodeGenerator,
    private val logger: KSPLoggerWrapper,
) : SymbolProcessor {

    // For per module
    private val metadataMap: MutableMap<String, ProxyMetaData> = mutableMapOf()

    companion object {
        const val ERROR_ILLEGAL_CLASS_NAME = "The annotated class is not valid with qualified name."
        const val ERROR_ILLEGAL_CLASS_STRUCTURE = "The annotated element is not wrapped by a class."
        const val ERROR_MULTI_CALIPER_ANNOTATIONS =
            "More than one Caliper annotation is found on the current element %s."
        const val ERROR_NO_CALIPER_ANNOTATION =
            "No Caliper annotation is found on the current element %s."
    }

    override fun process(resolver: Resolver): List<KSAnnotated> {
        logger.info("process")

        val methodAndFieldSymbols =
            resolver.getSymbolsWithAnnotation(CaliperMethodProxy::class.qualifiedName!!)
                .plus(resolver.getSymbolsWithAnnotation(CaliperFieldProxy::class.qualifiedName!!))
        logger.info("Method + Field symbols: ${methodAndFieldSymbols.toList().size}")
        methodAndFieldSymbols.filter { it is KSFunctionDeclaration && it.validate() }
            .forEach { it.accept(MetaCollectorForMethodAndFieldProxy(), Unit) }

        val classSymbols =
            resolver.getSymbolsWithAnnotation(CaliperClassProxy::class.qualifiedName!!)
        classSymbols.filter { it is KSClassDeclaration && it.validate() }
            .forEach { it.accept(MetaCollectorForClass(), Unit) }

        // To simplify the workflow, we only support one round processing,
        // since all proxy class are designated to be fixed (resolved elements).
        return emptyList()
    }

    override fun finish() {
        super.finish()
        logger.info("finish")
        logger.info("current metadataMap size: ${metadataMap.size}")
        val generator = CaliperWrapperGenerator(moduleName, metadataMap, codeGenerator, logger)
        generator.generate()
    }

    @OptIn(KotlinPoetJavaPoetPreview::class)
    inner class MetaCollectorForClass : KSVisitorVoid() {

        override fun visitClassDeclaration(classDeclaration: KSClassDeclaration, data: Unit) {
            super.visitClassDeclaration(classDeclaration, data)
            logger.info("visitClassDeclaration")

            if (classDeclaration.qualifiedName == null) {
                logger.error(ERROR_ILLEGAL_CLASS_NAME)
                return
            }
            val validClassName = classDeclaration.qualifiedName!!.asString()
            val classProxyAnnotation =
                classDeclaration.annotations.first { it.shortName.asString() == CaliperClassProxy::class.simpleName }
            val targetClassName = classProxyAnnotation.getParamValueByKey("className").toString()
            metadataMap.getOrPut(validClassName) {
                ProxyMetaData(
                    classTypeName = classDeclaration.asStarProjectedType().toTypeName()
                        .toJTypeName(),
                    sourceRef = classDeclaration.containingFile!!,
                    methods = mutableListOf(),
                    targetClass = targetClassName
                )
            }
        }

    }

    @OptIn(KotlinPoetJavaPoetPreview::class)
    inner class MetaCollectorForMethodAndFieldProxy : KSVisitorVoid() {

        override fun visitFunctionDeclaration(function: KSFunctionDeclaration, data: Unit) {
            super.visitFunctionDeclaration(function, data)
            logger.info("visitFunctionDeclaration")

            val functionName = function.simpleName.asString()
            logger.info("functionName = $functionName")

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

            val validAnno = methodProxyAnnotation ?: fieldProxyAnnotation!!
            val targetClassName = validAnno.getParamValueByKey("className").toString()
            val targetElementName =
                methodProxyAnnotation?.getParamValueByKey("methodName")?.toString()
                    ?: fieldProxyAnnotation!!.getParamValueByKey("fieldName").toString()
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
            logger.info("className = $className")

            val functionReturnType = function.returnType?.toTypeName()?.toJTypeName()
            logger.info("functionReturnType = $functionReturnType")

            val functionParameters = function.parameters
            val paramList = mutableListOf<MethodParam>()
            functionParameters.forEach {
                if (it.name != null) {
                    paramList.add(
                        MethodParam(
                            paramName = it.name!!.getShortName(),
                            type = it.type.toTypeName().toJTypeName()
                        )
                    )
                } else {
                    logger.error(
                        "Can not parse the element's name of the type " + it.type.toTypeName()
                            .toString()
                    )
                }
            }

            val proxyMethod = ProxyMethod(
                methodName = functionName,
                params = paramList,
                modifiers = listOf(),
                returnType = functionReturnType,
                targetClassName = targetClassName,
                targetElementName = targetElementName,
                targetOpcode = targetOpcode,
                targetType = validAnno.shortName.asString()
            )
            val metaData = metadataMap.getOrPut(className) {
                ProxyMetaData(
                    classTypeName = currClass.asStarProjectedType().toTypeName().toJTypeName(),
                    sourceRef = currClass.containingFile!!,
                    methods = mutableListOf(),
                    targetClass = null
                )
            }

            metaData.methods.add(proxyMethod)
        }

    }

    private fun KSAnnotation.getParamValueByKey(key: String) = arguments
        .first { it.name != null && it.name!!.asString() == key }
        .value!!

}