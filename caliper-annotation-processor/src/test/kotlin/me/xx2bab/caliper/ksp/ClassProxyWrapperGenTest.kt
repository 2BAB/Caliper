package me.xx2bab.caliper.ksp

import com.tschuchort.compiletesting.*
import me.xx2bab.caliper.anno.ASMOpcodes
import me.xx2bab.caliper.common.Constants
import me.xx2bab.caliper.common.Constants.KSP_OPTION_ANDROID_APP
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers
import org.hamcrest.text.IsEqualCompressingWhiteSpace.equalToCompressingWhiteSpace
import org.junit.jupiter.api.Test
import java.io.File

class ClassProxyWrapperGenTest {

    @Test
    fun `Test Java file generation`() {
        val mockedNameSuffix = "-ThreadClassProxyWrittenInJava"
        val proxyWrittenInJava = SourceFile.java(
            "ThreadClassProxyWrittenInJava.java", """
                package me.xx2bab.caliper.test;
                import me.xx2bab.caliper.anno.CaliperClassProxy;
                @CaliperClassProxy(className = "java.lang.Thread")
                public class ThreadClassProxyWrittenInJava extends Thread {
                    public ThreadClassProxyWrittenInJava(String name) {
                        super(name + "$mockedNameSuffix");
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
            kspArgs = mutableMapOf(KSP_OPTION_ANDROID_APP to "true")
        }.compile()
        assertThat(result.exitCode, Matchers.`is`(KotlinCompilation.ExitCode.OK))

        val kspGenDir = compilationTool.kspSourcesDir
        val generatedClass = File(
            kspGenDir,
            "java/${
                Constants.CALIPER_PACKAGE_FOR_WRAPPER_SPLIT_BY_SLASH.replace(
                    ".",
                    "/"
                )
            }/ThreadClassProxyWrittenInJava_CaliperWrapper.java"
        ).readText()

        val expectContent = File("src/test/resources/ThreadClassProxyWrittenInJava_CaliperWrapper.java").readText()
        assertThat(
            expectContent,
            equalToCompressingWhiteSpace(generatedClass)
        )
    }

//    @Test
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
        assertThat(result.exitCode, Matchers.`is`(KotlinCompilation.ExitCode.OK))

        val kspGenDir = compilationTool.kspSourcesDir

        val generatedClass = File(
            kspGenDir,
            "java/${
                Constants.CALIPER_PACKAGE_FOR_WRAPPER_SPLIT_BY_SLASH.replace(
                    ".",
                    "/"
                )
            }/ProxyWrittenInKtForFieldProxy_CaliperWrapper.java"
        ).readText()

        val expectContent = File("src/test/resources/ProxyWrittenInKtForFieldProxy_CaliperWrapper.java").readText()

        assertThat(
            expectContent,
            equalToCompressingWhiteSpace(generatedClass)
        )
    }

}