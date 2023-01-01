package me.xx2bab.caliper.anno

/**
 * An annotation that indicates a target function is going to be proxied by the annotated function.
 * All annotated methods will be aggregated by a KSP processor (the [caliper-aggregate-processor] module)
 * during build phrase, the processor should generate:
 *
 * - a proxy entry object class [Caliper],
 * - an interceptor [CaliperListener],
 * - a json file that will be consumed by the [caliper] module.
 */
@Target(AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.SOURCE)
annotation class CaliperMethodProxy(
    val className: String,
    val methodName: String,
    val opcode: Int
)