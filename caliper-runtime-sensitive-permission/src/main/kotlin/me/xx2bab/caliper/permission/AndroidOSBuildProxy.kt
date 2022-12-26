@file:JvmName("AndroidOSBuildProxy")

package me.xx2bab.caliper.permission

import me.xx2bab.caliper.anno.ASMOpcodes
import me.xx2bab.caliper.anno.CaliperFieldProxy
import me.xx2bab.caliper.anno.CaliperMethodProxy

object AndroidOSBuildProxy {

    @CaliperFieldProxy(
        className = "android/os/Build",
        fieldName = "SERIAL",
        opcode = ASMOpcodes.GETSTATIC
    )
    @CaliperMethodProxy(
        className = "android/os/Build",
        methodName = "getSerial",
        opcode = ASMOpcodes.INVOKESTATIC
    )
    @JvmStatic
    private fun getSerial(): String {
        return ""
    }

}