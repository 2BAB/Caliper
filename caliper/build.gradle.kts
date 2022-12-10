plugins {
    `kotlin-dsl`
    id("java-gradle-plugin")
    `github-release`
//    `maven-central-publish`
}

version = BuildConfig.Versions.caliperVersion

dependencies {
    // Basis
    implementation(gradleApi())
    implementation(deps.kotlin.std)
    implementation(deps.android.gradle.pluginapi)
    compileOnly(deps.android.gradle.plugin)

    // Tests
    testImplementation(deps.hamcrest)
    testImplementation(deps.mockk)
    testImplementation(gradleTestKit())
    testImplementation(deps.android.gradle.plugin)
}

// To include the AGP and other testImplementation deps into classpath.
tasks.withType<PluginUnderTestMetadata>().configureEach {
    pluginClasspath.from(provider { sourceSets.test.get().runtimeClasspath.files })
}

val deleteOldInstrumentedTests by tasks.registering(Delete::class) {
    delete(layout.buildDirectory.dir("test-samples"))
}

testing {
    suites {
        val test by getting(JvmTestSuite::class) {
            useJUnitJupiter()
        }

        val integrationTest by registering(JvmTestSuite::class) {
            dependencies {
                implementation(project())
                implementation(project(":gradle-instrumented-kit"))
            }
            targets {
                all {
                    testTask.configure {
                        shouldRunAfter(test)
                        dependsOn(deleteOldInstrumentedTests)
                    }
                }
            }
        }

        val functionalTest by registering(JvmTestSuite::class) {
            dependencies {
                implementation(project())
                implementation(project(":gradle-instrumented-kit"))
            }
            targets {
                all {
                    testTask.configure {
                        shouldRunAfter(test)
                        dependsOn(deleteOldInstrumentedTests)
                    }
                }
            }
        }
    }
}

configurations["integrationTestImplementation"]
    .extendsFrom(configurations["testImplementation"])
configurations["functionalTestImplementation"]
    .extendsFrom(configurations["testImplementation"])

tasks.check.configure {
    dependsOn(tasks.named("test"))
    dependsOn(tasks.named("integrationTest"))
    dependsOn(tasks.named("functionalTest"))
}

tasks.withType<Test> {
    testLogging {
        this.showStandardStreams = true
    }
}

java {
    withSourcesJar()
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(11))
    }
}

gradlePlugin {
    plugins {
        register("caliper") {
            id = "me.2bab.caliper"
            implementationClass ="me.xx2bab.caliper.CaliperPlugin"
            displayName = "me.2bab.caliper"
        }
    }
    testSourceSets.add(sourceSets["integrationTest"])
    testSourceSets.add(sourceSets["functionalTest"])
}