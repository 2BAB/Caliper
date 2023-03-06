import org.gradle.api.Project
import java.io.File

object BuildConfig {

    object Path {
        fun getAggregatedJarDirectory(project: Project) = File(
            project.rootProject.buildDir.absolutePath + File.separator + "libs")
    }

    object Versions {
        const val caliperVersion = "0.1.0"
    }

}