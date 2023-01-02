package me.xx2bab.caliper.anno

/**
 * An annotation that indicates a target field is going to be proxied by the annotated class.
 * More instruction can be found in [CaliperMethodProxy] comments.
 */
@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.SOURCE)
annotation class CaliperClassProxy(
    val className: String,
    val opcode: Int
)