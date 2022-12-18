package me.xx2bab.caliper.runtime

import me.xx2bab.caliper.anno.ProxyField
import me.xx2bab.caliper.anno.ProxyMethod
import org.objectweb.asm.Opcodes

interface AndroidOSBuildProxy {

    @me.xx2bab.caliper.anno.ProxyField(
        className = "android/os/Build",
        fieldName = "getSerial",
        opcode = Opcodes.GETSTATIC
    )
    @me.xx2bab.caliper.anno.ProxyMethod(
        className = "android/os/Build",
        methodName = "getSerial",
        opcode = Opcodes.INVOKESTATIC
    )
    fun getSerial(): String

}