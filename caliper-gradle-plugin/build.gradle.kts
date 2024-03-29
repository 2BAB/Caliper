plugins {
//    `kotlin-dsl` // For 1.8.10
    id("org.gradle.kotlin.kotlin-dsl") version "3.2.7" // For 1.7.22

    alias(deps.plugins.kotlin.serialization)
    id("java-gradle-plugin")
    alias(deps.plugins.build.config)
    `github-release`
    `maven-central-publish`
    `jar-publish`
}

version = BuildConfig.Versions.caliperVersion

dependencies {
    // Basis
    implementation(projects.caliperAnnotation)
    implementation(gradleApi())
    implementation(deps.kotlin.std)
    implementation(deps.android.gradle.pluginapi)
    compileOnly(deps.android.gradle.plugin)
    implementation(deps.asm.core)
    implementation(deps.asm.commons)
    implementation(deps.asm.util)
    implementation(deps.kotlin.serialization)
    implementation(deps.ksp.gradle.plugin)

    // kotlin compiler & static analytics
    implementation(projects.codeAnalyzer)
    implementation(deps.apache.common.text)

    // Tests
    testImplementation(deps.hamcrest)
    testImplementation(deps.mockk)
    testImplementation(deps.kotlin.compile.testing)
    testImplementation(gradleTestKit())
    testImplementation(deps.android.gradle.plugin)
}

// To include the AGP and other testImplementation deps into classpath.
val testCompileTask = project.tasks.getByName("compileTestKotlin")
tasks.withType<PluginUnderTestMetadata>().configureEach {
    pluginClasspath.from(provider { sourceSets.test.get().runtimeClasspath.files })
    dependsOn("processTestResources")
    testCompileTask.dependsOn(this)
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
                        dependsOn("compileTestKotlin")
                        dependsOn("compileTestJava")
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
                        dependsOn("compileTestKotlin")
                        dependsOn("compileTestJava")
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
    // dependsOn(tasks.named("functionalTest"))
}

tasks.withType<Test> {
    testLogging {
        this.showStandardStreams = true
    }
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(17))
    }
}

gradlePlugin {
    plugins {
        register("me.2bab.caliper") {
            id = "me.2bab.caliper"
            implementationClass = "me.xx2bab.caliper.gradle.CaliperPlugin"
            displayName = "Caliper Gradle Plugin"
        }
    }
    testSourceSets.add(sourceSets["integrationTest"])
    testSourceSets.add(sourceSets["functionalTest"])
}

buildConfig {
    packageName("me.xx2bab.caliper.gradle.build")
    useKotlinOutput()
    buildConfigField("String", "CALIPER_VERSION", "\"${version}\"")
}

// FIXME: Workaround for Gradle 8.0+ publishing issue:
//  "implicit dependency on the `signPluginMavenPublication`
//  and `signMe.2bab.caliperPluginMarkerMavenPublication` tasks"
afterEvaluate {
    tasks.getByName("publishMe.2bab.caliperPluginMarkerMavenPublicationToMyMavenlocalRepository")
        .dependsOn(tasks.getByName("signPluginMavenPublication"))
    tasks.getByName("publishMe.2bab.caliperPluginMarkerMavenPublicationToSonatypeRepository")
        .dependsOn(tasks.getByName("signPluginMavenPublication"))
    tasks.getByName("publishPluginMavenPublicationToMyMavenlocalRepository")
        .dependsOn(tasks.getByName("signMe.2bab.caliperPluginMarkerMavenPublication"))
    tasks.getByName("publishPluginMavenPublicationToSonatypeRepository")
        .dependsOn(tasks.getByName("signMe.2bab.caliperPluginMarkerMavenPublication"))
}