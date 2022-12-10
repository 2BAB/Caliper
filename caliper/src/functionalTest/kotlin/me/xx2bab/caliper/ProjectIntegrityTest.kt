package me.xx2bab.caliper

import me.xx2bab.gradle.CaliperConfigs
import me.xx2bab.gradle.GradleRunnerExecutor.execute
import org.gradle.testkit.runner.TaskOutcome
import org.hamcrest.MatcherAssert.assertThat
import org.junit.jupiter.api.Test

class ProjectIntegrityTest {

    @Test
    fun `Check Gradle scripts integrity`() {
        val taskPath = ":app:clean"
        val result = execute(
            CaliperConfigs.regular(),
            taskPath
        )
        assertThat(
            "Project gradle scripts may have some compilation errors" +
                    " that causes the clean task failed.",
            result.buildResult.task(taskPath)?.outcome != TaskOutcome.FAILED
        )
    }

    @Test
    fun `Check project integrity by assembling`() {
        val taskPath = ":app:assembleDebug"
        val result = execute(
            CaliperConfigs.regular(),
            taskPath
        )
        assertThat(
            "Project gradle scripts may have some compilation errors" +
                    " that causes the clean task failed.",
            result.buildResult.task(taskPath)?.outcome != TaskOutcome.FAILED
        )
    }

}