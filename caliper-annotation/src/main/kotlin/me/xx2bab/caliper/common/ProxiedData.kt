package me.xx2bab.caliper.common

import kotlinx.serialization.Serializable

@Serializable
data class ProxiedMethod(
    val targetClassName: String,
    val targetMethodName: String,
    val targetOpcode: Int,
    val newClassName: String,
    val newMethodName: String,
    val wrapperClassName: String,
    val wrapperMethodName: String
)

@Serializable
data class ProxiedField(
    val targetClassName: String,
    val targetFieldName: String,
    val targetOpcode: Int,
    val newClassName: String,
    val newMethodName: String,
    val wrapperClassName: String,
    val wrapperMethodName: String
)

@Serializable
data class ProxiedClass(
    val targetClassName: String,
    val newClassName: String
)