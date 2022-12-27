plugins {
    kotlin("jvm")
}

dependencies {
    implementation(projects.caliperAnnotation)
    implementation(deps.kotlin.std)
    implementation(deps.ksp.api)

    testImplementation(projects.caliperAnnotation)
    testImplementation(deps.hamcrest)
    testImplementation(deps.mockk)
    testImplementation(deps.kotlin.compile.testing)
    testImplementation(deps.kotlin.compile.testing.ksp)
    testImplementation(gradleTestKit())
}
testing {
    suites {
        val test by getting(JvmTestSuite::class) {
            useJUnitJupiter()
            dependencies {
                project()
            }
        }
    }
}
tasks.withType<Test> {
    testLogging {
        this.showStandardStreams = true
    }
}