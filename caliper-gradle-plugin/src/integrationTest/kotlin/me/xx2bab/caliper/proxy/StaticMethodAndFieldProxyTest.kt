package me.xx2bab.caliper.proxy

import com.tschuchort.compiletesting.KotlinCompilation
import com.tschuchort.compiletesting.KotlinCompilation.Result
import com.tschuchort.compiletesting.SourceFile
import me.xx2bab.caliper.anno.ASMOpcodes
import me.xx2bab.caliper.common.ProxiedField
import me.xx2bab.caliper.common.ProxiedMethod
import me.xx2bab.caliper.core.CaliperASMManipulator
import me.xx2bab.caliper.core.ProxyConfig
import me.xx2bab.caliper.tool.checkByteCodeIntegrity
import me.xx2bab.caliper.tool.getFieldValueInString
import me.xx2bab.caliper.tool.invokeMethod
import org.hamcrest.MatcherAssert
import org.hamcrest.Matchers.`is`
import org.junit.jupiter.api.Test

class StaticMethodAndFieldProxyTest {

    @Test
    fun `Hook the android-os-Build-SERIAL retrieval (static field) successfully`() {
        val mockSerialId = "mock_id"
        lateinit var result: Result
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
                        return "$mockSerialId";
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


        // Base Check
        MatcherAssert.assertThat(result.exitCode, `is`(KotlinCompilation.ExitCode.OK))

        // Manipulate bytecodes
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

        // ByteCode integrity check
        val errorLog = compiledTestClassFile.checkByteCodeIntegrity()
        MatcherAssert.assertThat(
            "The revised class file does not pass the integrity check, the reason is:\n $errorLog",
            errorLog == null
        )

        // Actual running test (based on reflection)
        val testCaseClass = result.classLoader.loadClass("TestCaseForBuildSerial")
        val testCase = testCaseClass.getDeclaredConstructor().newInstance()
        val serial = testCase.invokeMethod(testCaseClass, "getAndroidSerial")
        MatcherAssert.assertThat(
            serial,
            `is`(mockSerialId)
        )
    }


    @Test
    fun `Hook the SettingsSecure getString(xxx) (static method) successfully`() {
        val mockAndroidId = "123"

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

        val result = KotlinCompilation().apply {
            sources = listOf(settings, contentResolver, testCaseFile, replacedCaller)
            messageOutputStream = System.out // see diagnostics in real time
        }.compile()
        result.printAll()

        MatcherAssert.assertThat(result.exitCode, `is`(KotlinCompilation.ExitCode.OK))


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
        MatcherAssert.assertThat(
            "The revised class file does not pass the integrity check, the reason is:\n $errorLog",
            errorLog == null
        )

        val testCaseClass = result.classLoader.loadClass("TestCaseForSecure")
        val testCase = testCaseClass.getDeclaredConstructor().newInstance()
        // testCaseClass.printAllMethods()
        val androidIdByMethod = testCase.invokeMethod(testCaseClass, "getAndroidId")
        MatcherAssert.assertThat(
            androidIdByMethod,
            `is`(mockAndroidId)
        )
        val androidIdByClassProp = testCase.getFieldValueInString(testCaseClass, "androidIdAsClassProp")
        MatcherAssert.assertThat(
            androidIdByClassProp,
            `is`(mockAndroidId)
        )
        val androidIdByStaticProp = testCase.getFieldValueInString(testCaseClass, "androidIdAsStaticProp")
        MatcherAssert.assertThat(
            androidIdByStaticProp,
            `is`(mockAndroidId)
        )
    }


}