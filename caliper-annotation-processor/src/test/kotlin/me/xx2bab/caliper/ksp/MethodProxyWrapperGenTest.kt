package me.xx2bab.caliper.ksp

import com.tschuchort.compiletesting.*
import me.xx2bab.caliper.anno.ASMOpcodes
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.equalToIgnoringWhiteSpace
import org.hamcrest.text.IsEqualCompressingWhiteSpace.equalToCompressingWhiteSpace
import org.junit.jupiter.api.Test
import java.io.File

class MethodProxyWrapperGenTest {

    @Test
    fun `Test Java file generation`() {
        val proxyWrittenInJava = SourceFile.java(
            "ProxyWrittenInJava.java", """
                package me.xx2bab.caliper.test;
                import me.xx2bab.caliper.anno.CaliperMethodProxy;
                public class ProxyWrittenInJava {            
                    @CaliperMethodProxy(
                        className = "android/provider/Settings${'$'}Secure",
                        methodName = "getString",
                        opcode = ${ASMOpcodes.INVOKESTATIC}
                    )
                    public static String getString(String name) {
                        return "123";
                    }           
                }       
            """
        )
        val compilationTool = KotlinCompilation()
        val result = compilationTool.apply {
            sources = listOf(proxyWrittenInJava)
            inheritClassPath = true
            messageOutputStream = System.out // see diagnostics in real time
            this.symbolProcessorProviders = listOf(CaliperProxyRulesAggregationProcessorProvider())
            kspWithCompilation = true
        }.compile()
        val kspGenDir = compilationTool.kspSourcesDir

        val targetFile = File(
            kspGenDir,
            "java/${Constants.CALIPER_PACKAGE_FOR_WRAPPER.replace(".", "/")}/ProxyWrittenInJava_CaliperWrapper.java"
        )

        val targetContent = "package me.xx2bab.caliper.runtime.wrapper;\n" +
                "\n" +
                "import java.lang.String;\n" +
                "import me.xx2bab.caliper.ksp.CaliperMeta;\n" +
                "\n" +
                "@CaliperMeta(\n" +
                "        metadataInJSON = {\"proxiedMethods\":[{\"className\":\"android/provider/Settings\$Secure\",\"methodName\":\"getString\",\"opcode\":184,\"replacedClassName\":\"me.xx2bab.caliper.test.ProxyWrittenInJava_CaliperWrapper\",\"replacedMethodName\":\"getString\"}],\"proxiedFields\":[]}\n" +
                ")\n" +
                "public final class ProxyWrittenInJava_CaliperWrapper {\n" +
                "    public static String getString(String name) {\n" +
                "        // Caliper.visitMethod(\"android/provider/Settings\$Secure\",\"getString\",name);\n" +
                "        return me.xx2bab.caliper.test.ProxyWrittenInJava.getString(name);\n" +
                "    }\n" +
                "}\n"

        assertThat(
            targetFile.readText(),
            equalToCompressingWhiteSpace(targetContent)
        )
    }

    @Test
    fun `Test Kotlin file generation`() {
        val proxyWrittenInKt = SourceFile.kotlin(
            "ProxyWrittenInKt.kt", """
        package me.xx2bab.caliper.test;
        import me.xx2bab.caliper.anno.CaliperMethodProxy 
        object ProxyWrittenInKt {
            @CaliperMethodProxy(
                className = "android/provider/Settings\${'$'}Secure",
                methodName = "getString",
                opcode = ${ASMOpcodes.INVOKESTATIC}
            )
            @JvmStatic
            fun getString(name: String): String {
                return "123"
            }
        }
        """
        )

        val compilationTool = KotlinCompilation()
        val result = compilationTool.apply {
            sources = listOf(proxyWrittenInKt)
            inheritClassPath = true
            messageOutputStream = System.out // see diagnostics in real time
            this.symbolProcessorProviders = listOf(CaliperProxyRulesAggregationProcessorProvider())
            kspWithCompilation = true
        }.compile()
        val kspGenDir = compilationTool.kspSourcesDir

        val targetFile = File(
            kspGenDir,
            "java/${Constants.CALIPER_PACKAGE_FOR_WRAPPER.replace(".", "/")}/ProxyWrittenInKt_CaliperWrapper.java"
        )

        val targetContent = "package me.xx2bab.caliper.runtime.wrapper;\n" +
                "\n" +
                "import java.lang.String;\n" +
                "import me.xx2bab.caliper.ksp.CaliperMeta;\n" +
                "\n" +
                "@CaliperMeta(\n" +
                "        metadataInJSON = {\"proxiedMethods\":[{\"className\":\"android/provider/Settings\$Secure\",\"methodName\":\"getString\",\"opcode\":184,\"replacedClassName\":\"me.xx2bab.caliper.test.ProxyWrittenInKt_CaliperWrapper\",\"replacedMethodName\":\"getString\"}],\"proxiedFields\":[]}\n" +
                ")\n" +
                "public final class ProxyWrittenInKt_CaliperWrapper {\n" +
                "    public static String getString(String name) {\n" +
                "        // Caliper.visitMethod(\"android/provider/Settings\$Secure\",\"getString\",name);\n" +
                "        return me.xx2bab.caliper.test.ProxyWrittenInKt.getString(name);\n" +
                "    }\n" +
                "}"

        assertThat(
            targetContent,
            equalToCompressingWhiteSpace(targetFile.readText())
        )
    }

}