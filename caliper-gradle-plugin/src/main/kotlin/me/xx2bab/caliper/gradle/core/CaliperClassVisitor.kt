package me.xx2bab.caliper.gradle.core

import kotlinx.serialization.json.Json
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.decodeFromStream
import me.xx2bab.caliper.gradle.CaliperPlugin
import org.gradle.api.file.ConfigurableFileCollection
import org.objectweb.asm.*
import java.io.File
import java.util.jar.JarFile

class CaliperClassVisitor(
    private val api: Int,
    private val classVisitor: ClassVisitor,
    private val config:ProxyConfig,
    private val logger: KLogger = DefaultKotlinLogger()
) : ClassVisitor(api, classVisitor) {
    private var tempClassName = ""

    init {
        logger.debug("[CaliperClassVisitor] init")
    }

    override fun visit(
        version: Int,
        access: Int,
        name: String?,
        signature: String?,
        superName: String?,
        interfaces: Array<out String>?
    ) {
        super.visit(version, access, name, signature, superName, interfaces)
        logger.debug("visit class $name")
        tempClassName = name ?: ""
    }

    override fun visitMethod(
        access: Int,
        methodName: String?,
        descriptor: String?,
        signature: String?,
        exceptions: Array<out String>?
    ): MethodVisitor {
        logger.debug("visitMethod $methodName")
        val sv = super.visitMethod(access, methodName, descriptor, signature, exceptions)
        return CaliperMethodVisitor(
            access, descriptor, signature, sv, tempClassName, methodName, config, logger
        )
    }

}