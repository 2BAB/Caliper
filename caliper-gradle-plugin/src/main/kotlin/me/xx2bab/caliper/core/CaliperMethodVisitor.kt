package me.xx2bab.caliper.core

import me.xx2bab.caliper.anno.ASMOpcodes
import me.xx2bab.caliper.common.Constants.CALIPER_PACKAGE_FOR_WRAPPER_SPLIT_BY_SLASH
import org.objectweb.asm.MethodVisitor
import org.objectweb.asm.commons.AdviceAdapter

class CaliperMethodVisitor(
    access: Int,
    descriptor: String?,
    signature: String?,
    private val superVisitor: MethodVisitor,
    private val className: String,
    private val methodName: String?,
    private val proxyConfig: ProxyConfig,
    private val logger: KLogger = DefaultKotlinLogger()
) : AdviceAdapter(ASM9, superVisitor, access, methodName, descriptor) {

//    override fun visitInsn(opcode: Int) {
//        super.visitInsn(opcode)
//        logger.info(
//            "[CaliperMethodVisitor] visitInsn class = $className , opcode = $opcode"
//        )
//    }

    private var isNewOperationCodeFound = false

    override fun visitTypeInsn(opcode: Int, type: String?) {
        logger.info(
            "[CaliperMethodVisitor] visitTypeInsn class = $className , opcode = $opcode,"
        )
        val classProxies = proxyConfig.proxiedClasses
        for (cp in classProxies) {
            if (opcode == NEW && type == cp.className) {
                isNewOperationCodeFound = true
                superVisitor.visitTypeInsn(NEW, cp.replacedClassName)
                return
            }
        }
        super.visitTypeInsn(opcode, type)
    }


    override fun visitFieldInsn(opcode: Int, owner: String?, name: String?, descriptor: String?) {
        logger.info(
            "[CaliperMethodVisitor] visitFieldInsn class = $className , opcode = $opcode, owner = $owner," +
                    " name = $name, descriptor = $descriptor"
        )
        val fieldProxies = proxyConfig.proxiedFields
        for (fp in fieldProxies) {
            if (opcode == fp.opcode
                && owner == fp.className
                && name == fp.fieldName
                && descriptor != null // All sensitive data should have at least an output of the API call.
                && className.contains(CALIPER_PACKAGE_FOR_WRAPPER_SPLIT_BY_SLASH).not()
            ) {
                logger.info("[CaliperMethodVisitor] visitFieldInsn matched.")
                if (opcode == ASMOpcodes.GETSTATIC) {
                    visitMethodInsn(
                        opcode = INVOKESTATIC,
                        owner = fp.replacedClassName,
                        methodName = fp.replacedMethodName,
                        descriptor = "()$descriptor",
                        isInterface = false
                    )
                } else if (opcode == ASMOpcodes.GETFIELD) {
                    val newMethodDescWithOriginCallerClass = "(L${fp.className};)" + descriptor
                    visitMethodInsn(
                        opcode = INVOKESTATIC,
                        owner = fp.replacedClassName,
                        methodName = fp.replacedMethodName,
                        descriptor = newMethodDescWithOriginCallerClass,
                        isInterface = false
                    )
                }
                return
            }
        }
        super.visitFieldInsn(opcode, owner, name, descriptor)
    }


    override fun visitMethodInsn(
        opcode: Int,
        owner: String?,
        methodName: String?,
        descriptor: String?,
        isInterface: Boolean
    ) {
        logger.info(
            "[CaliperMethodVisitor] visitMethodInsn class = $className, opcode = $opcode, owner = $owner," +
                    " methodName = $methodName, descriptor = $descriptor"
        )

        // To match a pair of the `new` keyword and the `init()` method.
        if (isNewOperationCodeFound) {
            val classProxies = proxyConfig.proxiedClasses
            for (cp in classProxies) {
                if (opcode == INVOKESPECIAL
                    && owner == cp.className
                    && className != cp.replacedClassName
                ) {
                    isNewOperationCodeFound = false
                    superVisitor.visitMethodInsn(
                        INVOKESPECIAL,
                        cp.replacedClassName,
                        methodName,
                        descriptor,
                        isInterface
                    )
                    return
                }
            }
        }

        val methodProxies = proxyConfig.proxiedMethods
        for (mp in methodProxies) {
            if (opcode == mp.opcode
                && owner == mp.className
                && methodName == mp.methodName
                && descriptor != null // All sensitive data should have at least the output of an API call.
                && className.contains(CALIPER_PACKAGE_FOR_WRAPPER_SPLIT_BY_SLASH).not()
            ) {
                logger.info("[CaliperMethodVisitor] visitMethodInsn matched.")
                if (opcode == ASMOpcodes.INVOKESTATIC) {
                    visitMethodInsn(
                        opcode = INVOKESTATIC,
                        owner = mp.replacedClassName,
                        methodName = mp.replacedMethodName,
                        descriptor = descriptor,
                        isInterface = false
                    )
                } else if (opcode == ASMOpcodes.INVOKEVIRTUAL) { // TODO: may have more opcodes
                    val newMethodDescWithOriginCallerClass = StringBuilder().append(descriptor.substring(0, 1))
                        .append("L${mp.className};")
                        .append(descriptor.substring(1, descriptor.length))
                        .toString()
                    visitMethodInsn(
                        opcode = INVOKESTATIC,
                        owner = mp.replacedClassName,
                        methodName = mp.replacedMethodName,
                        descriptor = newMethodDescWithOriginCallerClass,
                        isInterface = false
                    )
                }
                return
            }
        }

        super.visitMethodInsn(opcode, owner, methodName, descriptor, isInterface)
    }

    override fun visitEnd() {
        visitMaxs(0, 0)
        super.visitEnd()
    }

}