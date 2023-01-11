package me.xx2bab.caliper.ksp

import com.tschuchort.compiletesting.*
import me.xx2bab.caliper.common.Constants
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.text.IsEqualCompressingWhiteSpace.equalToCompressingWhiteSpace
import org.junit.jupiter.api.Test
import java.io.File

class MetadataAggregationGenTest {

    @Test
    fun `Metadata files aggregation generated successfully`() {
        val metaFileWithProxyMethod = SourceFile.fromPath(
            File("src/test/resources/ProxyWrittenInJavaForMethodProxy_CaliperWrapper.java"))
        val metaFileWithProxyField = SourceFile.fromPath(
            File("src/test/resources/ProxyWrittenInJavaForFieldProxy_CaliperWrapper.java"))
        val metaFileWithProxyClass = SourceFile.fromPath(
            File("src/test/resources/ThreadClassProxyWrittenInJava_CaliperWrapper.java")
        )

        val compilationTool = KotlinCompilation()
        val result = compilationTool.apply {
            sources = listOf(metaFileWithProxyMethod, metaFileWithProxyField, metaFileWithProxyClass)
            inheritClassPath = true
            messageOutputStream = System.out // see diagnostics in real time
            // KSP
            kspWithCompilation = true
            symbolProcessorProviders = listOf(CaliperProxyRulesAggregationProcessorProvider())
            kspArgs = mutableMapOf(Constants.KSP_OPTION_ANDROID_APP to "true")
        }.compile()
        val kspGenDir = compilationTool.kspSourcesDir
        println("[Caliper][KSP] test kspGenDir: $kspGenDir")
        val targetFile = File(
            kspGenDir,
            "resources/caliper-aggregation.json"
        )

        val targetContent = File("src/test/resources/caliper-aggregation.json").readText()

        assertThat(
            targetFile.readText(),
            equalToCompressingWhiteSpace(targetContent)
        )
    }

}