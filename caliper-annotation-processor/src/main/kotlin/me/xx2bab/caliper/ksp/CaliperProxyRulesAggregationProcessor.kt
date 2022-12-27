package me.xx2bab.caliper.ksp

import com.google.devtools.ksp.processing.*
import com.google.devtools.ksp.symbol.KSAnnotated
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.KSFunctionDeclaration
import com.google.devtools.ksp.symbol.KSVisitorVoid
import com.google.devtools.ksp.validate
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

    override fun process(resolver: Resolver): List<KSAnnotated> {
        logger.lifecycle("process")

        val symbols = resolver.getSymbolsWithAnnotation("me.xx2bab.caliper.anno.CaliperMethodProxy", true)//CaliperMethodProxy::class.java.canonicalName)
        logger.lifecycle("symbols: ${symbols.toList().size}")

        val ret = symbols.filter { !it.validate() }.toList()
        symbols.filter { it is KSFunctionDeclaration && it.validate() }
            .forEach { it.accept(GenerationVisitor(), Unit) }
        return ret
    }

    override fun onError() {
        super.onError()
    }

    override fun finish() {
        super.finish()

    }

    inner class GenerationVisitor : KSVisitorVoid() {
        override fun visitClassDeclaration(classDeclaration: KSClassDeclaration, data: Unit) {
            classDeclaration.primaryConstructor!!.accept(this, data)
        }

        override fun visitFunctionDeclaration(function: KSFunctionDeclaration, data: Unit) {
//            if (function.parentDeclaration is KSClassDeclaration) {
//                val currClass = function.parentDeclaration as KSClassDeclaration
//                val className = currClass.qualifiedName!!
                val functionName = function.qualifiedName!!
                val functionParameters = function.parameters
                val functionReturnType = function.returnType

//                logger.lifecycle("className = $className")
                logger.lifecycle("functionName = $functionName")
                logger.lifecycle("functionReturnType = $functionReturnType")
//            }
        }

    }


}