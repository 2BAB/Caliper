package me.xx2bab.caliper.ksp

import com.tschuchort.compiletesting.*
import me.xx2bab.caliper.anno.ASMOpcodes
import me.xx2bab.caliper.common.Constants
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers
import org.hamcrest.text.IsEqualCompressingWhiteSpace.equalToCompressingWhiteSpace
import org.junit.jupiter.api.Test
import java.io.File

class MethodProxyWrapperGenTest {

    @Test
    fun `Test Java file generation`() {
        val proxyWrittenInJava = SourceFile.java(
            "ProxyWrittenInJavaForMethodReplacement.java", """
                package me.xx2bab.caliper.test;
                import me.xx2bab.caliper.anno.CaliperMethodProxy;
                public class ProxyWrittenInJavaForMethodReplacement {            
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
            kspArgs = mutableMapOf<String, String>(
                Constants.KSP_OPTION_ANDROID_APP to "true",
                Constants.KSP_OPTION_MODULE_NAME to "app"
            )
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
            }/ProxyWrittenInJavaForMethodReplacement_CaliperWrapper.java"
        ).readText()
        val expectClassContent =
            File("src/test/resources/ProxyWrittenInJavaForMethodReplacement_CaliperWrapper.java").readText()
        assertThat(
            expectClassContent,
            equalToCompressingWhiteSpace(generatedClass)
        )

        val generatedJson = File(
            kspGenDir,
            "resources/app.caliper.json"
        ).readText()
        val expectJson = File("src/test/resources/method.caliper.json").readText()
        assertThat(expectJson, equalToCompressingWhiteSpace(generatedJson))
    }

    @Test
    fun `Test Kotlin file generation`() {
        val proxyWrittenInKt = SourceFile.kotlin(
            "ProxyWrittenInKtForMethodReplacement.kt", """
        package me.xx2bab.caliper.test;
        import me.xx2bab.caliper.anno.CaliperMethodProxy 
        object ProxyWrittenInKtForMethodReplacement {
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
            kspArgs = mutableMapOf<String, String>(
                Constants.KSP_OPTION_ANDROID_APP to "true",
                Constants.KSP_OPTION_MODULE_NAME to "app"
            )
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
            }/ProxyWrittenInKtForMethodReplacement_CaliperWrapper.java"
        ).readText()
        val expectContent =
            File("src/test/resources/ProxyWrittenInKtForMethodReplacement_CaliperWrapper.java").readText()
        assertThat(
            expectContent,
            equalToCompressingWhiteSpace(generatedClass)
        )

        val generatedJson = File(
            kspGenDir,
            "resources/app.caliper.json"
        ).readText()
        val expectJson = File("src/test/resources/method-kt.caliper.json").readText()
        assertThat(expectJson, equalToCompressingWhiteSpace(generatedJson))
    }

}