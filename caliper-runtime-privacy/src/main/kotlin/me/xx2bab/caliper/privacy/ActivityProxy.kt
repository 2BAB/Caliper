@file:JvmName("ActivityProxy")

package me.xx2bab.caliper.privacy

import android.app.Activity
import me.xx2bab.caliper.anno.ASMOpcodes
import me.xx2bab.caliper.anno.CaliperFieldProxy
import me.xx2bab.caliper.anno.CaliperMethodProxy

object ActivityProxy {

    @CaliperMethodProxy(
        className = "android/app/Activity",
        methodName = "requestPermissions",
        opcode = ASMOpcodes.INVOKEVIRTUAL
    )
    @JvmStatic
    fun requestPermissions(activity: Activity, permissions: Array<String>, requestCode: Int) {
        activity.requestPermissions(permissions, requestCode)
    }
}