package me.xx2bab.caliper.gradle.core.proxy

import com.tschuchort.compiletesting.KotlinCompilation
import com.tschuchort.compiletesting.SourceFile
import me.xx2bab.caliper.anno.ASMOpcodes
import me.xx2bab.caliper.common.ProxiedField
import me.xx2bab.caliper.common.ProxiedMethod
import me.xx2bab.caliper.gradle.ASMManipulator
import me.xx2bab.caliper.gradle.core.CaliperClassVisitor
import me.xx2bab.caliper.gradle.core.ProxyConfig
import me.xx2bab.caliper.proxy.getCompiledFileByName
import me.xx2bab.caliper.proxy.printAll
import me.xx2bab.caliper.gradle.tool.checkByteCodeIntegrity
import me.xx2bab.caliper.gradle.tool.invokeMethod
import org.hamcrest.MatcherAssert
import org.hamcrest.Matchers
import org.junit.jupiter.api.Test
import org.objectweb.asm.Opcodes

class NonStaticMethodAndFieldProxyTest {

    // Activity ->
    // public final void requestPermissions(@NonNull String[] permissions, int requestCode) {}
    @Test
    fun `Non static method will be proxied successfully`() {
        val mockedPermission = "android.internet.replaced"
        val activity = SourceFile.java(
            "Activity.java", """
            package android.app;
            
            public class Activity {
                public final void requestPermissions(String[] permissions, int requestCode) {}
            }
        """.trimIndent()
        )

        val testCase = SourceFile.kotlin(
            "TestCaseForActivityPermissionReq.kt", """
            import android.app.Activity
            class TestCaseForActivityPermissionReq {                            
                fun doRequest(): String {
                    val arr = arrayOf("android.permission.INTERNET")
                    Activity().requestPermissions(arr, 0x888)
                    return arr[0]
                }                
            }
        """.trimIndent()
        )

        val proxy = SourceFile.java(
            "ActivityProxy.java", """
            import android.app.Activity;
            public final class ActivityProxy {
               
                public static void requestPermissions(Activity activity, String[] permissions, int requestCode) {
                    System.out.println("ActivityProxy#requestPermissions");
                    permissions[0] = "$mockedPermission";
                }
            }
        """.trimIndent()
        )

        val result = KotlinCompilation().apply {
            sources = listOf(activity, testCase, proxy)
            messageOutputStream = System.out // To see diagnostics in real time
        }.compile()
        result.printAll()

        MatcherAssert.assertThat(result.exitCode, Matchers.`is`(KotlinCompilation.ExitCode.OK))

        val compiledTestClassFile = result.getCompiledFileByName("TestCaseForActivityPermissionReq.class")
        val asmManipulator = ASMManipulator(
            inputClassFile = compiledTestClassFile,
        )
        val classVisitor = CaliperClassVisitor(
            api = Opcodes.ASM9,
            classVisitor = asmManipulator.writer,
            config = ProxyConfig(
                proxiedMethods = mutableListOf(
                    ProxiedMethod(
                        targetClassName = "android/app/Activity",
                        targetMethodName = "requestPermissions",
                        targetOpcode = ASMOpcodes.INVOKEVIRTUAL,
                        newClassName = "", // unnecessary for this test
                        newMethodName = "", // unnecessary for this test
                        wrapperClassName = "ActivityProxy",
                        wrapperMethodName = "requestPermissions"
                    )
                )
            ),
        )
        asmManipulator.processInPlace(classVisitor)

        val errorLog = compiledTestClassFile.checkByteCodeIntegrity()
        MatcherAssert.assertThat(
            "The revised class file does not pass the integrity check, the reason is:\n $errorLog",
            errorLog == null
        )


        val testCaseClass = result.classLoader.loadClass("TestCaseForActivityPermissionReq")
        val testCaseInstance = testCaseClass.getDeclaredConstructor().newInstance()
        // testCaseClass.printAllMethods()
        val replacedPermission = testCaseInstance.invokeMethod(testCaseClass, "doRequest")
        MatcherAssert.assertThat(
            replacedPermission,
            Matchers.`is`(mockedPermission)
        )
    }

    @Test
    fun `Non static field will be proxied successfully`() {
        val mockedBar = "123"
        val foo = SourceFile.java(
            "NonStaticFieldFoo.java", """            
            public class NonStaticFieldFoo {
                public final String bar = "non-static-field";            
            }
        """.trimIndent()
        )

        val testCase = SourceFile.kotlin(
            "TestCaseForNonStaticFieldFoo.kt", """            
            class TestCaseForNonStaticFieldFoo {                            
                fun test(): String {                                        
                    return NonStaticFieldFoo().bar
                }                
            }
        """.trimIndent()
        )

        val proxy = SourceFile.java(
            "NonStaticFieldFooProxy.java", """            
            public final class NonStaticFieldFooProxy {               
                public static String getBar(NonStaticFieldFoo foo) {
                    return "$mockedBar";
                }
            }
        """.trimIndent()
        )

        val result = KotlinCompilation().apply {
            sources = listOf(foo, testCase, proxy)
            messageOutputStream = System.out // To see diagnostics in real time
        }.compile()
        result.printAll()

        MatcherAssert.assertThat(result.exitCode, Matchers.`is`(KotlinCompilation.ExitCode.OK))

        val compiledTestClassFile = result.getCompiledFileByName("TestCaseForNonStaticFieldFoo.class")
        val asmManipulator = ASMManipulator(
            inputClassFile = compiledTestClassFile,
        )
        val classVisitor = CaliperClassVisitor(
            api = Opcodes.ASM9,
            classVisitor = asmManipulator.writer,
            config = ProxyConfig(
                proxiedFields = mutableListOf(
                    ProxiedField(
                        targetClassName = "NonStaticFieldFoo",
                        targetFieldName = "bar",
                        targetOpcode = ASMOpcodes.GETFIELD,
                        newClassName = "", // unnecessary for this test
                        newMethodName = "", // unnecessary for this test
                        wrapperClassName = "NonStaticFieldFooProxy",
                        wrapperMethodName = "getBar"
                    )
                )
            ),
        )
        asmManipulator.processInPlace(classVisitor)

        val errorLog = compiledTestClassFile.checkByteCodeIntegrity()
        MatcherAssert.assertThat(
            "The revised class file does not pass the integrity check, the reason is:\n $errorLog",
            errorLog == null
        )

        val testCaseClass = result.classLoader.loadClass("TestCaseForNonStaticFieldFoo")
        val testCaseInstance = testCaseClass.getDeclaredConstructor().newInstance()
        // testCaseClass.printAllMethods()
        val testResult = testCaseInstance.invokeMethod(testCaseClass, "test")
        MatcherAssert.assertThat(
            testResult,
            Matchers.`is`(mockedBar)
        )
    }


}