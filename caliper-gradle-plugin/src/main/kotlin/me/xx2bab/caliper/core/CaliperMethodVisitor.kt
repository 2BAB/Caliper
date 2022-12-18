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

    override fun visitInsn(opcode: Int) {
        super.visitInsn(opcode)
        logger.info(
            "[CaliperMethodVisitor] visitInsn class = $className , opcode = $opcode"
        )
    }

    override fun visitFieldInsn(opcode: Int, owner: String?, name: String?, descriptor: String?) {
        logger.info(
            "[CaliperMethodVisitor] visitFieldInsn class = $className , opcode = $opcode, owner = $owner," +
                    " name = $name, descriptor = $descriptor"
        )
        val fieldProxies = proxyConfig.fieldProxyList
        for (fp in fieldProxies) {
            if (opcode == fp.opcode
                && owner == fp.className
                && name == fp.fieldName
                && descriptor != null // All sensitive data should have at least an output of the API call.
                && className != "me/xx2bab/caliper/runtime/Caliper"
            ) {
                logger.info("[CaliperMethodVisitor] visitFieldInsn matched")
                // val desc = StringBuilder().append(descriptor.substring(0, 1))
                //     .append("Landroid/provider/Settings\$Secure;")
                //     .append(descriptor.substring(1, descriptor.length))
                //     .toString()
                visitFieldInsn(
                    opcode,
                    "me/xx2bab/caliper/runtime/Caliper",
                    name,
                    descriptor
                )
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

        val methodProxies = proxyConfig.methodProxyList
        for (mp in methodProxies) {
            if (opcode == mp.opcode
                && owner == mp.className
                && methodName == mp.methodName
                && descriptor != null // All sensitive data should have at least the output of an API call.
                && className != "me/xx2bab/caliper/runtime/Caliper"
            ) {
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