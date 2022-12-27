package me.xx2bab.caliper.ksp

import com.tschuchort.compiletesting.*
import me.xx2bab.caliper.anno.ASMOpcodes
import org.hamcrest.MatcherAssert.assertThat
import org.junit.jupiter.api.Test
import java.io.File

class MethodProxyWrapperGenTest {
    val proxyWrittenInKt = SourceFile.kotlin("ProxyWrittenInKt.kt", """
        import me.xx2bab.caliper.anno.CaliperMethodProxy
        import me.xx2bab.caliper.anno.CaliperClassProxy
        @CaliperClassProxy(
            className = "ABC",
            opcode = 1
        )
        object ProxyWrittenInKt {
            @CaliperMethodProxy(
                className = "android/provider/Settings\${'$'}Secure",
                methodName = "getString",
                opcode = ${ASMOpcodes.INVOKESTATIC}
            )
            @JvmStatic
            fun getString(): String {
                return ""
            }

        }
        """
    )

    @Test
    fun `Test Java file generation`() {
        val proxyWrittenInJava = SourceFile.java(
            "ProxyWrittenInJava.java", """
                import me.xx2bab.caliper.anno.CaliperMethodProxy;
                public class ProxyWrittenInJava {            
                    @CaliperMethodProxy(
                        className = "android/provider/Settings\${'$'}Secure",
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
            this.
            symbolProcessorProviders = listOf(CaliperProxyRulesAggregationProcessorProvider())
            kspWithCompilation = true
        }.compile()
        val kspGenDir = compilationTool.kspSourcesDir

        val targetFile = File(kspGenDir, "")

        val targetContent = """
            package me.xx2bab.caliper.wrapper;                   
            public class ProxyWrittenInJava_CaliperWrapper {                                
                 public static String getString(String name) {
                     Caliper.visitMethod("android/provider/Settings\${'$'}Secure", "getString", name)
                     return ProxyWrittenInJava.getString(name);
                 }           
            }                                                              
            """.trimIndent()

        assertThat("", targetFile.readText() == targetContent)
    }

}