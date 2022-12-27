@file:JvmName("SettingsSecureProxy")

package me.xx2bab.caliper.permission

import me.xx2bab.caliper.anno.ASMOpcodes
import me.xx2bab.caliper.anno.CaliperMethodProxy

object SettingsSecureProxy {

    @CaliperMethodProxy(
        className = "android/provider/Settings\$Secure",
        methodName = "getString",
        opcode = ASMOpcodes.INVOKESTATIC
    )
    @JvmStatic
    fun getString(): String {
        return ""
    }

}