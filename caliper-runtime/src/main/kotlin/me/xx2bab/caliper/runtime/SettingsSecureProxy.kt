@file:JvmName("SettingsSecureProxy")

package me.xx2bab.caliper.runtime

import me.xx2bab.caliper.anno.CaliperMethodProxy
import org.objectweb.asm.Opcodes

object SettingsSecureProxy {

    @CaliperMethodProxy(
        className = "android/provider/Settings\$Secure",
        methodName = "getString",
        opcode = Opcodes.INVOKESTATIC
    )
    fun getString(): String {
        return ""
    }

}