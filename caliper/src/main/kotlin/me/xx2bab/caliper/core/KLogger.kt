package me.xx2bab.caliper.core

import org.gradle.api.logging.Logger

interface KLogger {
    fun lifecycle(message: String)
    fun info(message: String)
    fun warn(message: String)
    fun error(message: String)
}

class GradleKLogger(private val logger: Logger) : KLogger {
    override fun lifecycle(message: String) {
        logger.lifecycle("[Caliper][Log]: $message")
    }

    override fun info(message: String) {
        logger.info("[Caliper][Info]: $message")
    }

    override fun warn(message: String) {
        logger.warn("[Caliper][Warn]: $message")
    }

    override fun error(message: String) {
        logger.error("[Caliper][Error]: $message")
    }
}

class DefaultKotlinLogger : KLogger {
    override fun lifecycle(message: String) {
        println("[Caliper][Lifecycle]: $message")
    }

    override fun info(message: String) {
        println("[Caliper][Info]: $message")
    }

    override fun warn(message: String) {
        println("[Caliper][Warn]: $message")
    }

    override fun error(message: String) {
        println("[Caliper][Error]: $message")
    }
}