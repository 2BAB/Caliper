@file:JvmName("SettingsSecureProxy")

package me.xx2bab.caliper.permission

import android.os.Build
import me.xx2bab.caliper.anno.ASMOpcodes
import me.xx2bab.caliper.anno.CaliperMethodProxy

object SettingsSecureProxy {

    const val abc = "getString"

    @CaliperMethodProxy(
        className = "android/provider/Settings\$Secure",
        methodName = abc,// "getString",
        opcode = ASMOpcodes.INVOKESTATIC
    )
    @JvmStatic
    fun getString(): String {
        return Build.BOARD.toString()
    }

}