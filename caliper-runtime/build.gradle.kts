plugins {
    id("com.android.library")
    kotlin("android")
}

android {
    compileSdk = 31
    defaultConfig {
        minSdk = 21
        targetSdk = 31
    }

    sourceSets["main"].java.srcDir("src/main/kotlin")

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }

    kotlinOptions {
        jvmTarget = "1.8"
    }

    testOptions {
        unitTests {
            isIncludeAndroidResources = true
        }
    }
}

dependencies {
    implementation(projects.caliperAnnotation)
    implementation(deps.androidx.annotation)
    compileOnly(deps.asm.core)
    testImplementation(deps.junit4)
    testImplementation(deps.mockk)
    testImplementation(deps.robolectric)
}
