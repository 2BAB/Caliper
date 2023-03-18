package me.xx2bab.caliper.ksp

import com.tschuchort.compiletesting.*
import me.xx2bab.caliper.anno.ASMOpcodes
import me.xx2bab.caliper.common.Constants
import me.xx2bab.caliper.common.Constants.KSP_OPTION_ANDROID_APP
import me.xx2bab.caliper.common.Constants.KSP_OPTION_MODULE_NAME
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
            kspArgs = mutableMapOf(
                KSP_OPTION_ANDROID_APP to "true",
                KSP_OPTION_MODULE_NAME to "app"
            )
        }.compile()
        assertThat(result.exitCode, Matchers.`is`(KotlinCompilation.ExitCode.OK))

        val kspGenDir = compilationTool.kspSourcesDir
        /*val generatedClass = File(
            kspGenDir,
            "java/${
                Constants.CALIPER_PACKAGE_FOR_WRAPPER_SPLIT_BY_SLASH.replace(
                    ".",
                    "/"
                )
            }/ProxyWrittenInJavaToReplaceThreadClass_CaliperWrapper.java"
        ).readText()

        val expectContent =
            File("src/test/resources/ProxyWrittenInJavaToReplaceThreadClass_CaliperWrapper.java").readText()
        assertThat(
            expectContent,
            equalToCompressingWhiteSpace(generatedClass)
        )*/

        val generatedJson = File(
            kspGenDir,
            "resources/app.caliper.json"
        ).readText()
        val expectJson = File("src/test/resources/thread.caliper.json").readText()
        assertThat(expectJson, equalToCompressingWhiteSpace(generatedJson))
    }

    @Test
    fun `Test Kotlin file generation`() {
        val proxyWrittenInKt = SourceFile.kotlin(
            "ThreadClassProxyWrittenInKotlin.kt", """
                package me.xx2bab.caliper.test;
                import me.xx2bab.caliper.anno.CaliperClassProxy
                @CaliperClassProxy(className = "java.lang.Thread")
                open class ThreadClassProxyWrittenInKotlin(name: String) : Thread(name + "mockedNameSuffix") {} 
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
            kspArgs = mutableMapOf<String, String>(
                KSP_OPTION_ANDROID_APP to "true",
                KSP_OPTION_MODULE_NAME to "app"
            )
        }.compile()
        assertThat(result.exitCode, Matchers.`is`(KotlinCompilation.ExitCode.OK))

        val kspGenDir = compilationTool.kspSourcesDir

        val generatedJson = File(
            kspGenDir,
            "resources/app.caliper.json"
        ).readText()
        val expectJson = File("src/test/resources/thread-kt.caliper.json").readText()
        assertThat(expectJson, equalToCompressingWhiteSpace(generatedJson))
    }

}