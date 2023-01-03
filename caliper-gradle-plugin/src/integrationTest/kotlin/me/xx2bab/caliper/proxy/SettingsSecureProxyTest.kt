package me.xx2bab.caliper.proxy

import com.tschuchort.compiletesting.KotlinCompilation
import com.tschuchort.compiletesting.KotlinCompilation.Result
import com.tschuchort.compiletesting.KotlinCompilation.ExitCode
import com.tschuchort.compiletesting.SourceFile
import me.xx2bab.caliper.anno.ASMOpcodes
import me.xx2bab.caliper.common.ProxiedMethod
import me.xx2bab.caliper.core.CaliperASMManipulator
import me.xx2bab.caliper.core.ProxyConfig
import me.xx2bab.caliper.tool.checkByteCodeIntegrity
import me.xx2bab.caliper.tool.getFieldValueInString
import me.xx2bab.caliper.tool.invokeMethod
import me.xx2bab.caliper.tool.printAllMethods
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.`is`
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test

class SettingsSecureProxyTest {

    companion object {
        const val mockAndroidId = "123"
        lateinit var result: Result

        @BeforeAll
        @JvmStatic
        fun setup() {
            val settings = SourceFile.java(
                "Settings.java", """
                package android.provider;
                import android.content.ContentResolver;
                public final class Settings {
                    public static final class Secure {
                        public static String getString(ContentResolver resolver, String name) {
                            return "12345";
                        }
                    }
                }        
            """
            )

            val contentResolver = SourceFile.java(
                "ContentResolver.java", """
                package android.content;
                public class ContentResolver {}
            """.trimIndent()
            )

            val testCaseFile = SourceFile.kotlin(
                "TestCaseForSecure.kt", """                
                import android.provider.Settings
                import android.content.ContentResolver           
                
                class TestCaseForSecure() {
                    companion object {                                
                        val crAsStaticProp = ContentResolver()
                        val androidIdAsStaticProp = Settings.Secure.getString(crAsStaticProp, "android_id")                       
                    }
    
                    val crAsClassProp = ContentResolver()
                    val androidIdAsClassProp: String = Settings.Secure.getString(crAsClassProp, "android_id")
    
                    fun getAndroidId(): String {
                        println("androidIdAsClassProp:${'$'}androidIdAsClassProp")
                        val cr = ContentResolver()
                        return Settings.Secure.getString(cr, "android_id")
                    }
                }
            """.trimIndent()
            )

            val replacedCaller = SourceFile.java(
                "Caliper.java", """
                package me.xx2bab.caliper.runtime;
                import android.content.ContentResolver;
                import android.provider.Settings;
    
                public class Caliper {            
                    public static String getString(ContentResolver resolver, String name) {
                        return "$mockAndroidId";
                    }           
                }
            """.trimIndent()
            )

            result = KotlinCompilation().apply {
                sources = listOf(settings, contentResolver, testCaseFile, replacedCaller)
                messageOutputStream = System.out // see diagnostics in real time
            }.compile()
            result.printAll()
        }
    }

    @Test
    fun `Test files compilation goes well`() {
        assertThat(result.exitCode, `is`(ExitCode.OK))
    }

    @Test
    fun `Hook the AndroidId retrieval successfully`() {
        val compiledTestClassFile = result.getCompiledFileByName("TestCaseForSecure.class")
        val asmManipulator = CaliperASMManipulator(
            inputClassFile = compiledTestClassFile,
            config = ProxyConfig(
                proxiedMethods = mutableListOf(
                    ProxiedMethod(
                        className = "android/provider/Settings\$Secure",
                        methodName = "getString",
                        opcode = ASMOpcodes.INVOKESTATIC,
                        replacedClassName = "me/xx2bab/caliper/runtime/Caliper",
                        replacedMethodName = "getString"
                    )
                )
            ),
        )
        asmManipulator.processInPlace()

        val errorLog = compiledTestClassFile.checkByteCodeIntegrity()
        assertThat(
            "The revised class file does not pass the integrity check, the reason is:\n $errorLog",
            errorLog == null
        )

        val testCaseClass = result.classLoader.loadClass("TestCaseForSecure")
        val testCase = testCaseClass.getDeclaredConstructor().newInstance()
        // testCaseClass.printAllMethods()
        val androidIdByMethod = testCase.invokeMethod(testCaseClass, "getAndroidId")
        assertThat(
            androidIdByMethod,
            `is`(mockAndroidId)
        )
        val androidIdByClassProp = testCase.getFieldValueInString(testCaseClass, "androidIdAsClassProp")
        assertThat(
            androidIdByClassProp,
            `is`(mockAndroidId)
        )
        val androidIdByStaticProp = testCase.getFieldValueInString(testCaseClass, "androidIdAsStaticProp")
        assertThat(
            androidIdByStaticProp,
            `is`(mockAndroidId)
        )
    }


}