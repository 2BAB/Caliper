plugins {
    id("com.android.library")
    kotlin("android")

    id("com.google.devtools.ksp") // Apply the KSP plugin ahead of Caliper
}

android {
    namespace = "me.xx2bab.caliper.runtime.privacy"
    compileSdk = 31
    defaultConfig {
        minSdk = 21
    }

    sourceSets["main"].java.srcDir("src/main/kotlin")
    sourceSets["test"].java.srcDir("test/kotlin")

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlinOptions {
        jvmTarget = "17"
    }

}

dependencies {
    implementation(projects.caliperAnnotation)
    implementation(projects.caliperRuntime)
    ksp(projects.caliperAnnotationProcessor)

    testImplementation(deps.junit4)
    testApi("com.bennyhuo.kotlin:code-analyzer:1.1")
    testApi("io.gitlab.arturbosch.detekt:detekt-core:1.20.0")
    testApi("io.gitlab.arturbosch.detekt:detekt-tooling:1.20.0")
    testApi("io.gitlab.arturbosch.detekt:detekt-parser:1.20.0")
    testApi("io.gitlab.arturbosch.detekt:detekt-utils:1.20.0")
}

tasks.withType<Test> {
    testLogging {
        this.showStandardStreams = true
    }
}

ksp {    arg("ANDROID_APPLICATION_MODULE", "false")    arg("MODULE_NAME", "caliper-runtime-privacy")}