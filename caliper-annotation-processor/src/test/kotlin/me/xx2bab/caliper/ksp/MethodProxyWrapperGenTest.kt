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
            "ProxyWrittenInJavaForMethodProxy.java", """
                package me.xx2bab.caliper.test;
                import me.xx2bab.caliper.anno.CaliperMethodProxy;
                public class ProxyWrittenInJavaForMethodProxy {            
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
            // KSP
            kspWithCompilation = true
            symbolProcessorProviders = listOf(CaliperProxyRulesAggregationProcessorProvider())
            kspArgs = mutableMapOf<String, String>(Constants.KSP_OPTION_ANDROID_APP to "true")
        }.compile()
        val kspGenDir = compilationTool.kspSourcesDir

        val generatedClass = File(
            kspGenDir,
            "java/${Constants.CALIPER_PACKAGE_FOR_WRAPPER.replace(".", "/")}/ProxyWrittenInJavaForMethodProxy_CaliperWrapper.java"
        ).readText()

        val expectContent = File("src/test/resources/ProxyWrittenInJavaForMethodProxy_CaliperWrapper.java").readText()

        assertThat(
            expectContent,
            equalToCompressingWhiteSpace(generatedClass)
        )
    }

    @Test
    fun `Test Kotlin file generation`() {
        val proxyWrittenInKt = SourceFile.kotlin(
            "ProxyWrittenInKtForMethodProxy.kt", """
        package me.xx2bab.caliper.test;
        import me.xx2bab.caliper.anno.CaliperMethodProxy 
        object ProxyWrittenInKtForMethodProxy {
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
            // KSP
            kspWithCompilation = true
            symbolProcessorProviders = listOf(CaliperProxyRulesAggregationProcessorProvider())
            kspArgs = mutableMapOf<String, String>(Constants.KSP_OPTION_ANDROID_APP to "true")
        }.compile()
        val kspGenDir = compilationTool.kspSourcesDir

        val generatedClass = File(
            kspGenDir,
            "java/${Constants.CALIPER_PACKAGE_FOR_WRAPPER.replace(".", "/")}/ProxyWrittenInKtForMethodProxy_CaliperWrapper.java"
        ).readText()

        val expectContent = File("src/test/resources/ProxyWrittenInKtForMethodProxy_CaliperWrapper.java").readText()

        assertThat(
            expectContent,
            equalToCompressingWhiteSpace(generatedClass)
        )
    }

}