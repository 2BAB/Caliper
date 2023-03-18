@file:JvmName("SettingsSecureProxy")

package me.xx2bab.caliper.privacy

import android.content.ContentResolver
import android.os.Build
import android.provider.Settings
import me.xx2bab.caliper.anno.ASMOpcodes
import me.xx2bab.caliper.anno.CaliperMethodProxy
import me.xx2bab.caliper.runtime.Caliper

object SettingsSecureProxy {

    @CaliperMethodProxy(
        className = "android/provider/Settings\$Secure",
        methodName = "getString",
        opcode = ASMOpcodes.INVOKESTATIC
    )
    @JvmStatic
    fun getString(resolver: ContentResolver, name: String): String {
        return Settings.Secure.getString(resolver, name)
    }

}