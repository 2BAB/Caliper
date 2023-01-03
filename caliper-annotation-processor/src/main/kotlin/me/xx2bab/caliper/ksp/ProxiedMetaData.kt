package me.xx2bab.caliper.ksp

import com.google.devtools.ksp.symbol.KSFile
import kotlinx.serialization.Required
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient

@Serializable
data class ProxiedMetaData(
    @Required val proxiedMethods: MutableList<ProxiedMethod> = mutableListOf(),
    @Required val proxiedFields: MutableList<ProxiedField> = mutableListOf(),
    @Transient val mapKSFiles: MutableList<KSFile> = mutableListOf()
)

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
