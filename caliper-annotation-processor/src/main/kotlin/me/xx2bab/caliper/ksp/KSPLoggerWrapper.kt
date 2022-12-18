package me.xx2bab.caliper.ksp

import com.google.devtools.ksp.processing.KSPLogger

class KSPLoggerWrapper(val kspLogger: KSPLogger) {

    private val tag = "[Caliper][KSP]: "

    fun lifecycle(message: String) {
        kspLogger.logging(tag + message)
    }

    fun info(message: String) {
        kspLogger.info(tag + message)
    }

    fun warn(message: String) {
        kspLogger.warn(tag + message)
    }

    fun error(message: String) {
        kspLogger.error(tag + message)
    }
}