package me.xx2bab.caliper.common

import kotlinx.serialization.Serializable

@Serializable
data class ProxiedMethod(
    val className: String,
    val methodName: String,
    val opcode: Int,
    val replacedClassName: String,
    val replacedMethodName: String
)

@Serializable
data class ProxiedField(
    val className: String,
    val fieldName: String,
    val opcode: Int,
    val replacedClassName: String,
    val replacedMethodName: String
)

@Serializable
data class ProxiedClass(
    val className: String,
    val replacedClassName: String
)