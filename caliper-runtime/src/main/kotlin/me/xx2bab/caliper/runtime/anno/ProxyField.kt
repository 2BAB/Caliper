package me.xx2bab.caliper.runtime.anno

/**
 * An annotation that indicates a target field is going to be proxied by the annotated function/field.
 * More instruction can be found in [ProxyMethod] comments.
 */
@Target(
    AnnotationTarget.FIELD,
    AnnotationTarget.FUNCTION
)
@Retention(AnnotationRetention.SOURCE)
annotation class ProxyField(
    val className: String,
    val fieldName: String,
    val opcode: Int
)