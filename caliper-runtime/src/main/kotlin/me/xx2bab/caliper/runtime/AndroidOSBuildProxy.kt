package me.xx2bab.caliper.runtime

import me.xx2bab.caliper.runtime.anno.ProxyField
import me.xx2bab.caliper.runtime.anno.ProxyMethod
import org.objectweb.asm.Opcodes

interface AndroidOSBuildProxy {

    @ProxyField(
        className = "android/os/Build",
        fieldName = "getSerial",
        opcode = Opcodes.GETSTATIC
    )
    @ProxyMethod(
        className = "android/os/Build",
        methodName = "getSerial",
        opcode = Opcodes.INVOKESTATIC
    )
    fun getSerial(): String

}