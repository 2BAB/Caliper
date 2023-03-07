plugins {
    id("com.android.library")
    kotlin("android")
    id("com.google.devtools.ksp")
    `maven-central-publish`
}

android {
    namespace = "me.xx2bab.caliper.runtime.batteryoptim"
    compileSdk = 31
    defaultConfig {
        minSdk = 21
    }

    sourceSets["main"].java.srcDir("src/main/kotlin")

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlinOptions {
        jvmTarget = "17"
    }

    publishing {
        multipleVariants("allVariants") {
            allVariants()
            withJavadocJar()
            withSourcesJar()
        }
    }
}

dependencies {
    implementation(projects.caliperAnnotation)
    implementation(projects.caliperRuntime)
    ksp(projects.caliperAnnotationProcessor)
}

ksp {
    arg("ANDROID_APPLICATION_MODULE", "false")
    arg("MODULE_NAME", "caliper-runtime-battery-optim")
}