@file:JvmName("WakeLockProxy")
package me.xx2bab.caliper.battery

import android.os.PowerManager.WakeLock
import me.xx2bab.caliper.anno.ASMOpcodes
import me.xx2bab.caliper.anno.CaliperMethodProxy

object PowerManagerWakeLockProxy {

    @CaliperMethodProxy(
        className = "android/os/PowerManager\$WakeLock",
        methodName = "acquire",
        opcode = ASMOpcodes.INVOKEVIRTUAL
    )
    @JvmStatic
    fun acquire(wakeLock: WakeLock) {
        wakeLock.acquire()
    }

    fun acquire(wakeLock: WakeLock, timeout: Long) {
        wakeLock.acquire(timeout)
    }

    @CaliperMethodProxy(
        className = "android/os/PowerManager\$WakeLock",
        methodName = "release",
        opcode = ASMOpcodes.INVOKEVIRTUAL
    )
    @JvmStatic
    fun release(wakeLock: WakeLock) {
        wakeLock.release()
    }

    fun release(wakeLock: WakeLock, flags: Int) {
        wakeLock.release(flags)
    }

}