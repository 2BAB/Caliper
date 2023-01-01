package me.xx2bab.caliper.ksp

import com.tschuchort.compiletesting.*
import me.xx2bab.caliper.anno.ASMOpcodes
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.text.IsEqualCompressingWhiteSpace.equalToCompressingWhiteSpace
import org.junit.jupiter.api.Test
import java.io.File

class FieldProxyWrapperGenTest {

    @Test
    fun `Test Java file generation`() {
        val proxyWrittenInJava = SourceFile.java(
            "ProxyWrittenInJava.java", """
                package me.xx2bab.caliper.test;
                import me.xx2bab.caliper.anno.CaliperFieldProxy;
                public class ProxyWrittenInJava {            
                    @CaliperFieldProxy(
                        className = "android/provider/Settings${'$'}Secure",
                        fieldName = "SERIAL",
                        opcode = ${ASMOpcodes.GETSTATIC}
                    )
                    public static String getString() {
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
                "\n" +
                "public final class ProxyWrittenInJava_CaliperWrapper {\n" +
                "    public static String getString() {\n" +
                "        // Caliper.visitMethod(\"android/provider/Settings\$Secure\",\"getString\");\n" +
                "        return me.xx2bab.caliper.test.ProxyWrittenInJava.getString();\n" +
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
        import me.xx2bab.caliper.anno.CaliperFieldProxy 
        object ProxyWrittenInKt {
            @CaliperFieldProxy(
                className = "android/provider/Settings\${'$'}Secure",
                fieldName = "SERIAL",
                opcode = ${ASMOpcodes.GETSTATIC}
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
                "\n" +
                "public final class ProxyWrittenInKt_CaliperWrapper {\n" +
                "    public static String getString(String name) {\n" +
                "        // Caliper.visitMethod(\"android/provider/Settings\$Secure\",\"getString\",name);\n" +
                "        return me.xx2bab.caliper.test.ProxyWrittenInKt.getString(name);\n" +
                "    }\n" +
                "}\n"

        assertThat(
            targetContent,
            equalToCompressingWhiteSpace(targetFile.readText())
        )
    }

}