package me.xx2bab.caliper.gradle.core

import org.objectweb.asm.*

class CaliperClassVisitor(
    private val api: Int,
    private val classVisitor: ClassVisitor,
    private val config: ProxyConfig,
    private val logger: KLogger = DefaultKotlinLogger()
) : ClassVisitor(api, classVisitor) {
    private var tempClassName = ""

    init {
        logger.info("[CaliperClassVisitor] init")
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
        logger.info("visit $name")
        tempClassName = name ?: ""
    }

    override fun visitMethod(
        access: Int,
        methodName: String?,
        descriptor: String?,
        signature: String?,
        exceptions: Array<out String>?
    ): MethodVisitor {
        logger.info("visit $methodName")
        val sv = super.visitMethod(access, methodName, descriptor, signature, exceptions)
        return CaliperMethodVisitor(access, descriptor, signature, sv, tempClassName, methodName, config)
    }

}