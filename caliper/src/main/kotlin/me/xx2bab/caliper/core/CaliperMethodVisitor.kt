package me.xx2bab.caliper.core

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

    override fun visitMethodInsn(
        opcode: Int,
        owner: String?,
        methodName: String?,
        descriptor: String?,
        isInterface: Boolean
    ) {
        logger.info("[CaliperMethodVisitor] visitMethodInsn $methodName")
        val methodProxys = proxyConfig.methodProxyList
        for (mp in methodProxys) {
            if (opcode == INVOKESTATIC
                && owner == "android/provider/Settings\$Secure"
                && (methodName == "getString")
                && descriptor != null // All sensitive data should have at least the output of an API call.
                && className != "me/xx2bab/caliper/runtime/Caliper"
            ) {
                logger.info("[CaliperMethodVisitor] class == $className, opcode = $opcode, owner = $owner, methodName = $methodName, descriptor = $descriptor")
                // val desc = StringBuilder().append(descriptor.substring(0, 1))
                //     .append("Landroid/provider/Settings\$Secure;")
                //     .append(descriptor.substring(1, descriptor.length))
                //     .toString()
                visitMethodInsn(
                    INVOKESTATIC, "me/xx2bab/caliper/runtime/Caliper",
                    methodName, descriptor, false
                )
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