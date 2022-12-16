package me.xx2bab.caliper.core

import org.objectweb.asm.Opcodes

data class ProxyConfig(
    val methodProxyList: List<MethodProxy>
)

data class MethodProxy(
    private val opcode: Int,
    private val className: String,
    private val methodName: String
)
