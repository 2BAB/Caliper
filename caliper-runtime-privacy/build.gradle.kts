plugins {
    id("com.android.library")
    kotlin("android")
    id("com.google.devtools.ksp")
    `maven-central-publish`
    `aar-publish`
}

android {
    namespace = "me.xx2bab.caliper.runtime.privacy"
    compileSdk = 31
    defaultConfig {
        minSdk = 23
        aarMetadata {
            minAgpVersion = "7.4.0"
            minCompileSdk = 23
        }
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
}

tasks.withType<Test> {
    testLogging {
        this.showStandardStreams = true
    }
}

ksp {
    arg("ANDROID_APPLICATION_MODULE", "false")
    arg("MODULE_NAME", "caliper-runtime-privacy")
}

