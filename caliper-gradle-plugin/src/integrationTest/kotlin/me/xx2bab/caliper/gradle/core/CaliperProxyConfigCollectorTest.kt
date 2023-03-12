package me.xx2bab.caliper.gradle.core

import io.mockk.spyk
import me.xx2bab.caliper.anno.ASMOpcodes
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.containsInAnyOrder
import org.hamcrest.Matchers.`is`
import org.junit.jupiter.api.Test
import java.io.File

class CaliperProxyConfigCollectorTest {

    private val kLogger = spyk(
        object : KLogger {
            override fun lifecycle(message: String) {}
            override fun info(message: String) {}
            override fun warn(message: String) {}
            override fun error(message: String) {}
            override fun debug(message: String) {}
        })

    @Test
    fun `doCollect() aggregates metadata from local project successfully`() {
        val config = CaliperProxyConfigCollector(kLogger)
        val localResJar = File(
            "src/fixtures/gen-metadata/local-res-jar" +
                    "/build/intermediates/library_java_res/debug/res.jar"
        )
        val result = config.doCollect(setOf(localResJar))
        assertThat(result.proxiedMethods.size, `is`(1))
        assertThat(result.proxiedMethods.first().methodName, `is`("commonMethodReturnsString"))
        assertThat(
            result.proxiedMethods.first().className,
            `is`("me/xx2bab/caliper/sample/library/LibrarySampleClass")
        )
        assertThat(result.proxiedMethods.first().opcode, `is`(ASMOpcodes.INVOKEVIRTUAL))
        assertThat(
            result.proxiedMethods.first().replacedMethodName,
            `is`("commonMethodReturnsString")
        )
        assertThat(
            result.proxiedMethods.first().replacedClassName,
            `is`("me/xx2bab/caliper/runtime/wrapper/CustomProxy_CaliperWrapper")
        )
    }

    @Test
    fun `doCollect() aggregates metadata from remote artifact successfully`() {
        val config = CaliperProxyConfigCollector(kLogger)
        val remoteClassJar = File(
            "src/fixtures/gen-metadata/remote-class-jar" +
                    "/classes.jar"
        )
        val result = config.doCollect(setOf(remoteClassJar))

        assertThat(result.proxiedMethods.size, `is`(2))
        assertThat(
            result.proxiedMethods.map { it.methodName },
            containsInAnyOrder("getSerial", "getString")
        )
        assertThat(
            result.proxiedMethods.map { it.className },
            containsInAnyOrder("android/os/Build", "android/provider/Settings\$Secure")
        )
        assertThat(
            result.proxiedMethods.map { it.opcode },
            containsInAnyOrder(ASMOpcodes.INVOKESTATIC, ASMOpcodes.INVOKESTATIC)
        )
        assertThat(
            result.proxiedMethods.map { it.replacedMethodName },
            containsInAnyOrder("getSerial", "getString")
        )
        assertThat(
            result.proxiedMethods.map { it.replacedClassName }, containsInAnyOrder(
                "me/xx2bab/caliper/runtime/wrapper/AndroidOSBuildProxy_CaliperWrapper",
                "me/xx2bab/caliper/runtime/wrapper/SettingsSecureProxy_CaliperWrapper"
            )
        )

        assertThat(result.proxiedFields.size, `is`(1))
        assertThat(result.proxiedFields.first().fieldName, `is`("SERIAL"))
        assertThat(result.proxiedFields.first().className, `is`("android/os/Build"))
        assertThat(result.proxiedFields.first().opcode, `is`(ASMOpcodes.GETSTATIC))
        assertThat(result.proxiedFields.first().replacedMethodName, `is`("getSerialField"))
        assertThat(
            result.proxiedFields.first().replacedClassName,
            `is`("me/xx2bab/caliper/runtime/wrapper/AndroidOSBuildProxy_CaliperWrapper")
        )
    }

}