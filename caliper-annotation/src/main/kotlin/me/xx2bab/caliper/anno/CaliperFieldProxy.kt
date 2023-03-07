package me.xx2bab.caliper.anno

/**
 * An annotation that indicates a target field is going to be proxied by the annotated function.
 * More instruction can be found in [CaliperMethodProxy] comments.
 */
@Target(AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.SOURCE)
annotation class CaliperFieldProxy(
    val className: String,
    val fieldName: String,
    val opcode: Int
)