package me.xx2bab.caliper.ksp

import com.tschuchort.compiletesting.*
import me.xx2bab.caliper.anno.ASMOpcodes
import me.xx2bab.caliper.ksp.Constants.KSP_OPTION_ANDROID_APP
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.text.IsEqualCompressingWhiteSpace.equalToCompressingWhiteSpace
import org.junit.jupiter.api.Test
import java.io.File

class FieldProxyWrapperGenTest {

    @Test
    fun `Test Java file generation`() {
        val proxyWrittenInJava = SourceFile.java(
            "ProxyWrittenInJavaForFieldProxy.java", """
                package me.xx2bab.caliper.test;
                import me.xx2bab.caliper.anno.CaliperFieldProxy;
                public class ProxyWrittenInJavaForFieldProxy {            
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
            // KSP
            kspWithCompilation = true
            symbolProcessorProviders = listOf(CaliperProxyRulesAggregationProcessorProvider())
            kspArgs = mutableMapOf<String, String>(KSP_OPTION_ANDROID_APP to "true")
        }.compile()
        val kspGenDir = compilationTool.kspSourcesDir

        val generatedClass = File(
            kspGenDir,
            "java/${Constants.CALIPER_PACKAGE_FOR_WRAPPER.replace(".", "/")}/ProxyWrittenInJavaForFieldProxy_CaliperWrapper.java"
        ).readText()

        val expectContent = File("src/test/resources/ProxyWrittenInJavaForFieldProxy_CaliperWrapper.java").readText()

        assertThat(
            expectContent,
            equalToCompressingWhiteSpace(generatedClass)
        )
    }

    @Test
    fun `Test Kotlin file generation`() {
        val proxyWrittenInKt = SourceFile.kotlin(
            "ProxyWrittenInKtForFieldProxy.kt", """
        package me.xx2bab.caliper.test;
        import me.xx2bab.caliper.anno.CaliperFieldProxy 
        object ProxyWrittenInKtForFieldProxy {
            @CaliperFieldProxy(
                className = "android/provider/Settings\${'$'}Secure",
                fieldName = "SERIAL",
                opcode = ${ASMOpcodes.GETSTATIC}
            )
            @JvmStatic
            fun getString(): String {
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
            // KSP
            kspWithCompilation = true
            symbolProcessorProviders = listOf(CaliperProxyRulesAggregationProcessorProvider())
            kspArgs = mutableMapOf<String, String>(KSP_OPTION_ANDROID_APP to "true")
        }.compile()
        val kspGenDir = compilationTool.kspSourcesDir

        val generatedClass = File(
            kspGenDir,
            "java/${Constants.CALIPER_PACKAGE_FOR_WRAPPER.replace(".", "/")}/ProxyWrittenInKtForFieldProxy_CaliperWrapper.java"
        ).readText()

        val expectContent = File("src/test/resources/ProxyWrittenInKtForFieldProxy_CaliperWrapper.java").readText()

        assertThat(
            expectContent,
            equalToCompressingWhiteSpace(generatedClass)
        )
    }

}