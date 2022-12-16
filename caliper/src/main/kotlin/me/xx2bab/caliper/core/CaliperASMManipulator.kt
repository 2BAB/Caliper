package me.xx2bab.caliper.core

import org.objectweb.asm.*
import java.io.File

class CaliperASMManipulator(
    private val inputClassFile: File,
    private val config: ProxyConfig,
    private val logger: KLogger = DefaultKotlinLogger()
) {

    private val reader: ClassReader = ClassReader(inputClassFile.inputStream())
    private val writer: ClassWriter = ClassWriter(reader, ClassWriter.COMPUTE_FRAMES or ClassWriter.COMPUTE_MAXS)
    private val classVisitor: ClassVisitor

    init {
        logger.info("[CaliperASMManipulator] init")
        classVisitor = object : ClassVisitor(Opcodes.ASM9, writer) {
            private var tempClassName = ""

            override fun visit(
                version: Int,
                access: Int,
                name: String?,
                signature: String?,
                superName: String?,
                interfaces: Array<out String>?
            ) {
                super.visit(version, access, name, signature, superName, interfaces)
                println("visitClass $name")
                tempClassName = name ?: ""
            }

            override fun visitMethod(
                access: Int,
                methodName: String?,
                descriptor: String?,
                signature: String?,
                exceptions: Array<out String>?
            ): MethodVisitor {
                val sv = super.visitMethod(access, methodName, descriptor, signature, exceptions)
                return CaliperMethodVisitor(access, descriptor, signature, sv, tempClassName, methodName, config)
            }

            override fun visitEnd() {
                super.visitEnd()
            }
        }
    }


    fun process(): ByteArray {
        reader.accept(classVisitor, ClassReader.EXPAND_FRAMES)
        return writer.toByteArray()
    }

    fun process(outputClassFile: File) {
        reader.accept(classVisitor, ClassReader.EXPAND_FRAMES)
        outputClassFile.writeBytes(writer.toByteArray())
    }

    fun processInPlace() {
        reader.accept(classVisitor, ClassReader.EXPAND_FRAMES)
        inputClassFile.writeBytes(writer.toByteArray())
    }

}