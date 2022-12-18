package me.xx2bab.caliper

import org.gradle.testfixtures.ProjectBuilder
import org.hamcrest.MatcherAssert.assertThat
import org.junit.jupiter.api.Test

class CaliperExtensionTest {

    @Test
    fun `extension integrity test`() {
        val project = ProjectBuilder.builder()
            .withName("extension-test")
            .build()

        val slackExt = project.extensions.create(
            "caliper",
            CaliperExtension::class.java
        ).apply {
            enableByVariant { variant ->
                variant.name.contains("debug", true)
                        && variant.name.contains("full", true)
            }
        }

        assertThat("The `kotlinEnableByVariant` should not be null.",
            slackExt.kotlinEnableByVariant != null)
    }
}