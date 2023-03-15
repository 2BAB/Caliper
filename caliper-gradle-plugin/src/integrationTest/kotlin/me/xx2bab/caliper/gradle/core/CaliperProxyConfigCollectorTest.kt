package me.xx2bab.caliper.gradle.core

import io.mockk.spyk
import me.xx2bab.caliper.anno.ASMOpcodes
import me.xx2bab.caliper.common.ProxiedMethod
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
        assertThat(result.proxiedMethods.first().targetMethodName, `is`("commonMethodReturnsString"))
        assertThat(
            result.proxiedMethods.first().targetClassName,
            `is`("me/xx2bab/caliper/sample/library/LibrarySampleClass")
        )
        assertThat(result.proxiedMethods.first().targetOpcode, `is`(ASMOpcodes.INVOKEVIRTUAL))
        assertThat(
            result.proxiedMethods.first().wrapperMethodName,
            `is`("commonMethodReturnsString")
        )
        assertThat(
            result.proxiedMethods.first().wrapperClassName,
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

        assertThat(result.proxiedMethods.size, `is`(3))
        assertThat(result.proxiedMethods, containsInAnyOrder(
            ProxiedMethod(
                "android/os/Build",
                "getSerial",
                ASMOpcodes.INVOKESTATIC,
                "me/xx2bab/caliper/privacy/AndroidOSBuildProxy",
                "getSerial",
                "me/xx2bab/caliper/runtime/wrapper/AndroidOSBuildProxy_CaliperWrapper",
                "getSerial"
            ),
            ProxiedMethod(
                "android/provider/Settings\$Secure",
                "getString",
                ASMOpcodes.INVOKESTATIC,
                "me/xx2bab/caliper/privacy/SettingsSecureProxy",
                "getString",
                "me/xx2bab/caliper/runtime/wrapper/SettingsSecureProxy_CaliperWrapper",
                "getString"
            ),
            ProxiedMethod(
                "android/app/Activity",
                "requestPermissions",
                ASMOpcodes.INVOKEVIRTUAL,
                "me/xx2bab/caliper/privacy/ActivityProxy",
                "requestPermissions",
                "me/xx2bab/caliper/runtime/wrapper/ActivityProxy_CaliperWrapper",
                "requestPermissions"
            )
        ))

        assertThat(result.proxiedFields.size, `is`(1))
        assertThat(result.proxiedFields.first().targetFieldName, `is`("SERIAL"))
        assertThat(result.proxiedFields.first().targetClassName, `is`("android/os/Build"))
        assertThat(result.proxiedFields.first().targetOpcode, `is`(ASMOpcodes.GETSTATIC))
        assertThat(result.proxiedFields.first().wrapperMethodName, `is`("getSerialField"))
        assertThat(
            result.proxiedFields.first().wrapperClassName,
            `is`("me/xx2bab/caliper/runtime/wrapper/AndroidOSBuildProxy_CaliperWrapper")
        )
    }

}