package me.xx2bab.caliper.sample.customproxy

import me.xx2bab.caliper.anno.ASMOpcodes
import me.xx2bab.caliper.anno.CaliperMethodProxy
import me.xx2bab.caliper.sample.library.LibrarySampleClass

object CustomProxy {

    @CaliperMethodProxy(
        className = "me/xx2bab/caliper/sample/library/LibrarySampleClass",
        methodName = "commonMethodReturnsString",
        opcode = ASMOpcodes.INVOKEVIRTUAL
    )
    @JvmStatic
    fun commonMethodReturnsString(lib: LibrarySampleClass) = "CustomProxy"

}