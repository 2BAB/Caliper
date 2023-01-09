package me.xx2bab.caliper.proxy

import com.tschuchort.compiletesting.KotlinCompilation
import com.tschuchort.compiletesting.SourceFile
import me.xx2bab.caliper.anno.ASMOpcodes
import me.xx2bab.caliper.common.ProxiedClass
import me.xx2bab.caliper.common.ProxiedField
import me.xx2bab.caliper.common.ProxiedMethod
import me.xx2bab.caliper.core.CaliperASMManipulator
import me.xx2bab.caliper.core.ProxyConfig
import me.xx2bab.caliper.tool.checkByteCodeIntegrity
import me.xx2bab.caliper.tool.invokeMethod
import org.hamcrest.MatcherAssert
import org.hamcrest.Matchers
import org.junit.jupiter.api.Test

class ClassProxyTest {

    @Test
    fun `Class will be proxied successfully`() {
        val originName = "Origin Name"
        val appendedName = "+ Caliper"
        val testCase = SourceFile.kotlin(
            "TestCaseForThreadProxy.kt", """       
            import java.lang.Thread
            class TestCaseForThreadProxy {                            
                fun test(): String {     
                    val t = Thread({ println("") }, "$originName")
                    t.start()                    
                    return t.name
                }                
            }
        """.trimIndent()
        )

        val proxy = SourceFile.java(
            "NamedThread.java", """            
            import java.lang.Thread;
            public final class NamedThread extends Thread {                               
                public NamedThread(String name) {
                    super(name + "$appendedName");
                }
                public NamedThread(ThreadGroup group, String name) {
                    super(group, name + "$appendedName");
                }
                public NamedThread(Runnable target, String name) {
                    super(target, name + "$appendedName");
                }
                public NamedThread(ThreadGroup group, Runnable target,String name) {
                    super(group, target, name + "$appendedName");
                }
                public NamedThread(ThreadGroup group, Runnable target,String name, long stackSize) {
                    super(group, target, name + "$appendedName", stackSize);
                }
                public NamedThread(ThreadGroup group, Runnable target, String name, long stackSize, boolean inheritThreadLocals) {
                    super(group, target, name + "$appendedName", stackSize, inheritThreadLocals);
                }}
        """.trimIndent()
        )

        val result = KotlinCompilation().apply {
            sources = listOf(testCase, proxy)
            messageOutputStream = System.out // To see diagnostics in real time
        }.compile()
        result.printAll()

        MatcherAssert.assertThat(result.exitCode, Matchers.`is`(KotlinCompilation.ExitCode.OK))

        val compiledTestClassFile = result.getCompiledFileByName("TestCaseForThreadProxy.class")
        val asmManipulator = CaliperASMManipulator(
            inputClassFile = compiledTestClassFile,
            config = ProxyConfig(
                proxiedClasses = mutableListOf(
                    ProxiedClass(
                        className = "java/lang/Thread",
                        replacedClassName = "NamedThread"
                    )
                )
            ),
        )
        asmManipulator.processInPlace()

//        val errorLog = compiledTestClassFile.checkByteCodeIntegrity()
//        MatcherAssert.assertThat(
//            "The revised class file does not pass the integrity check, the reason is:\n $errorLog",
//            errorLog == null
//        )

        val testCaseClass = result.classLoader.loadClass("TestCaseForThreadProxy")
        val testCaseInstance = testCaseClass.getDeclaredConstructor().newInstance()
        // testCaseClass.printAllMethods()
        val testResult = testCaseInstance.invokeMethod(testCaseClass, "test")
        MatcherAssert.assertThat(
            testResult,
            Matchers.`is`(originName + appendedName)
        )
    }


}