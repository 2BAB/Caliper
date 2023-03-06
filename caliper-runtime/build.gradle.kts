plugins {
    id("com.android.library")
    kotlin("android")
    `maven-central-publish`
}

android {
    namespace = "me.xx2bab.caliper.runtime.core"
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

    testOptions {
        unitTests {
            isIncludeAndroidResources = true
        }
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
    api(projects.caliperAnnotation)
    implementation(deps.androidx.annotation)
    compileOnly(deps.asm.core)
    testImplementation(deps.junit4)
    testImplementation(deps.mockk)
    testImplementation(deps.robolectric)
}
