package me.xx2bab.caliper.ksp

import com.google.devtools.ksp.processing.*
import com.google.devtools.ksp.symbol.*
import com.google.devtools.ksp.validate
import com.squareup.javapoet.TypeName
import com.squareup.kotlinpoet.javapoet.KotlinPoetJavaPoetPreview
import com.squareup.kotlinpoet.javapoet.toJTypeName
import com.squareup.kotlinpoet.ksp.toTypeName
import kotlin.collections.getOrPut
import me.xx2bab.caliper.anno.CaliperMethodProxy

class CaliperProxyRulesAggregationProcessorProvider : SymbolProcessorProvider {
    override fun create(
        env: SymbolProcessorEnvironment
    ): SymbolProcessor {
        return CaliperProxyRulesAggregationProcessor(
            env.codeGenerator, KSPLoggerWrapper(env.logger)
        )
    }
}

/**
 * The processor to aggregate all [@ProxyMethod] [@ProxyField] from [caliper-runtime] module.
 */
class CaliperProxyRulesAggregationProcessor(
    val codeGenerator: CodeGenerator,
    val logger: KSPLoggerWrapper,
) : SymbolProcessor {

    private val metadataMap: MutableMap<String, ProxyMetaData> = mutableMapOf()

    companion object {
        const val ERROR_ILLEGAL_CLASS_STRUCTURE = "The annotated element is not wrapped by a class."
    }

    override fun process(resolver: Resolver): List<KSAnnotated> {
        logger.lifecycle("process")

        val symbols = resolver.getSymbolsWithAnnotation(
            "me.xx2bab.caliper.anno.CaliperMethodProxy", true
        )//CaliperMethodProxy::class.java.canonicalName)
        logger.lifecycle("symbols: ${symbols.toList().size}")

        val ret = symbols.filter { !it.validate() }.toList()
        symbols.filter { it is KSFunctionDeclaration && it.validate() }.forEach { it.accept(MetaCollector(), Unit) }
        return ret
    }

    override fun finish() {
        super.finish()
        logger.lifecycle("finish")
        logger.lifecycle("metadataMap size: ${metadataMap.size}")
        val generator = CaliperWrapperGenerator(metadataMap, codeGenerator, logger)
        generator.generate()
    }

    @OptIn(KotlinPoetJavaPoetPreview::class)
    inner class MetaCollector : KSVisitorVoid() {

        override fun visitPropertyDeclaration(property: KSPropertyDeclaration, data: Unit) {
            super.visitPropertyDeclaration(property, data)
            logger.info("visitPropertyDeclaration")

            // We don't allow top level function
            if (property.parentDeclaration is KSClassDeclaration) {

            } else {
                logger.error(ERROR_ILLEGAL_CLASS_STRUCTURE)
            }
        }

        override fun visitFunctionDeclaration(function: KSFunctionDeclaration, data: Unit) {
            super.visitFunctionDeclaration(function, data)
            logger.info("visitFunctionDeclaration")
            val targetClassName = function.annotations
                .first { it.shortName.asString() == CaliperMethodProxy::class.simpleName }
                .arguments
                .first { it.name != null && it.name!!.asString() == "className" }
                .value!!.toString()

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

            val functionName = function.simpleName.asString()
            logger.lifecycle("functionName = $functionName")

            val functionReturnType = function.returnType?.toTypeName()?.toJTypeName()
            logger.lifecycle("functionReturnType = $functionReturnType")

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
                        "Can not parse the element's name of the type " +
                                "${it.type.toTypeName().toString()}"
                    )
                }
            }

            val proxyMethod = ProxyMethod(functionName, paramList, functionReturnType)
            val metaData = metadataMap.getOrPut(className) {
                ProxyMetaData(
                    currClass.asStarProjectedType().toTypeName().toJTypeName(),
                    targetClassName = targetClassName,
                    currClass.containingFile!!,
                    mutableListOf(), mutableListOf()
                )
            }

            metaData.methods.add(proxyMethod)
        }

    }


}