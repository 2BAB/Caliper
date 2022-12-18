package me.xx2bab.caliper.core

import org.objectweb.asm.Opcodes

data class ProxyConfig(
    val methodProxyList: List<MethodProxy>,
    val fieldProxyList: List<FieldProxy>
)

data class MethodProxy(
    val opcode: Int,
    val className: String,
    val methodName: String
)

data class FieldProxy(
    val opcode: Int,
    val className: String,
    val fieldName: String
)
