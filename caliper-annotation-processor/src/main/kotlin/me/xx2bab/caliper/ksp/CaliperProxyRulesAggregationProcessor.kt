package me.xx2bab.caliper.ksp

import com.google.devtools.ksp.processing.*
import com.google.devtools.ksp.symbol.KSAnnotated


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




        // We guarantee the processor will be required for one round only,
        // since it's used in our own project, and we know there are not postpone elements to be resolved in the next round.
        return emptyList()
    }

}