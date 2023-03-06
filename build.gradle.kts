import BuildConfig.Path
task("clean") {
    delete(rootProject.buildDir)
}

val aggregateJars by tasks.registering {
    doLast {
        val output = Path.getAggregatedJarDirectory(project)
        output.mkdir()
        subprojects {
            if (this.name.contains("caliper-runtime")) {
                File(buildDir.absolutePath + "/outputs/aar").walk()
                    .filter { it.name.startsWith(this.name) && it.extension == "aar" }
                    .forEach { it.copyTo(File(output, it.name)) }
            } else {
                File(buildDir.absolutePath + File.separator + "libs").walk()
                    .filter { it.name.startsWith(this.name) && it.extension == "jar" }
                    .forEach { it.copyTo(File(output, it.name)) }
            }
        }
    }
}
