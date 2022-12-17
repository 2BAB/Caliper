package me.xx2bab.caliper.runtime

import me.xx2bab.caliper.runtime.anno.ProxyMethod
import org.objectweb.asm.Opcodes

interface SettingsSecureProxy {

    @ProxyMethod(
        className = "android/provider/Settings\$Secure",
        methodName = "getString",
        opcode = Opcodes.INVOKESTATIC
    )
    fun getString(): String

}