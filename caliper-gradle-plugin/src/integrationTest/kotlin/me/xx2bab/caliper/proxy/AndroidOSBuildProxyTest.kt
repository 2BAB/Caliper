package me.xx2bab.caliper.proxy

import com.tschuchort.compiletesting.KotlinCompilation
import com.tschuchort.compiletesting.KotlinCompilation.Result
import com.tschuchort.compiletesting.SourceFile
import me.xx2bab.caliper.anno.ASMOpcodes
import me.xx2bab.caliper.common.ProxiedField
import me.xx2bab.caliper.core.CaliperASMManipulator
import me.xx2bab.caliper.core.ProxyConfig
import me.xx2bab.caliper.tool.checkByteCodeIntegrity
import me.xx2bab.caliper.tool.invokeMethod
import org.hamcrest.MatcherAssert
import org.hamcrest.Matchers.`is`
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test

class AndroidOSBuildProxyTest {

    companion object {

        private const val MOCK_SERIAL_ID = "mock_id"
        lateinit var result: Result

        @BeforeAll
        @JvmStatic
        fun setup() {
            val androidOSBuild = SourceFile.java(
                "Build.java", """
                package android.os; 
                
                public class Build {
                    // The original SERIAL is delegated by a get method,
                    // to avoid compiler optimization we add `toLowerCase()` here.
                    public static final String SERIAL = "no.such.thing".toLowerCase(); 
                }        
            """
            )

            val replacedCaller = SourceFile.java(
                "Caliper.java", """
                package me.xx2bab.caliper.runtime;
    
                public class Caliper {                                                    
                    public static String getSerial() {
                        return "$MOCK_SERIAL_ID";
                    }
                }
            """.trimIndent()
            )

            val testCaseFile = SourceFile.kotlin(
                "TestCaseForBuildSerial.kt", """
                import android.os.Build                      
                
                class TestCaseForBuildSerial() {                    
                    fun getAndroidSerial(): String { 
                        return Build.SERIAL
                    }
                }
            """.trimIndent()
            )


            result = KotlinCompilation().apply {
                sources = listOf(androidOSBuild, replacedCaller, testCaseFile)
                messageOutputStream = System.out // see diagnostics in real time
            }.compile()
            result.printAll()
        }
    }

    @Test
    fun `Test files compilation goes well`() {
        MatcherAssert.assertThat(
            result.exitCode,
            `is`(KotlinCompilation.ExitCode.OK)
        )
    }

    @Test
    fun `Hook the android-os-Build-SERIAL retrieval successfully`() {
        val compiledTestClassFile = result.getCompiledFileByName("TestCaseForBuildSerial.class")
        val asmManipulator = CaliperASMManipulator(
            inputClassFile = compiledTestClassFile,
            config = ProxyConfig(
                proxiedMethods = mutableListOf(),
                proxiedFields = mutableListOf(
                    ProxiedField(
                        className = "android/os/Build",
                        fieldName = "SERIAL",
                        opcode = ASMOpcodes.GETSTATIC,
                        replacedClassName = "me/xx2bab/caliper/runtime/Caliper",
                        replacedMethodName = "getSerial"
                    )
                )
            ),
        )
        asmManipulator.processInPlace()

        val errorLog = compiledTestClassFile.checkByteCodeIntegrity()
        MatcherAssert.assertThat(
            "The revised class file does not pass the integrity check, the reason is:\n $errorLog",
            errorLog == null
        )

        val testCaseClass = result.classLoader.loadClass("TestCaseForBuildSerial")
        val testCase = testCaseClass.getDeclaredConstructor().newInstance()
        val serial = testCase.invokeMethod(testCaseClass, "getAndroidSerial")
        MatcherAssert.assertThat(
            serial,
            `is`(MOCK_SERIAL_ID)
        )
    }

}