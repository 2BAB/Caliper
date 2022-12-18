package me.xx2bab.caliper.runtime

import me.xx2bab.caliper.anno.ProxyMethod
import org.objectweb.asm.Opcodes

interface SettingsSecureProxy {

    @me.xx2bab.caliper.anno.ProxyMethod(
        className = "android/provider/Settings\$Secure",
        methodName = "getString",
        opcode = Opcodes.INVOKESTATIC
    )
    fun getString(): String

}