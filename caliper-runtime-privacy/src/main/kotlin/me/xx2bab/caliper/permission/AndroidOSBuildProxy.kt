@file:JvmName("AndroidOSBuildProxy")

package me.xx2bab.caliper.permission

import me.xx2bab.caliper.anno.ASMOpcodes
import me.xx2bab.caliper.anno.CaliperFieldProxy
import me.xx2bab.caliper.anno.CaliperMethodProxy

object AndroidOSBuildProxy {

    @CaliperMethodProxy(
        className = "android/os/Build",
        methodName = "getSerial",
        opcode = ASMOpcodes.INVOKESTATIC
    )
    @JvmStatic
    fun getSerial(): String {
        return ""
    }
    @CaliperFieldProxy(
        className = "android/os/Build",
        fieldName = "SERIAL",
        opcode = ASMOpcodes.GETSTATIC
    )
    @JvmStatic
    fun getSerialField(): String {
        return ""
    }

}