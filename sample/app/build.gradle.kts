import org.jetbrains.kotlin.gradle.internal.Kapt3GradleSubplugin.Companion.isIncludeCompileClasspath

plugins {
    id("com.android.application")
    kotlin("android")

    id("com.google.devtools.ksp") // Apply the KSP plugin ahead of Caliper
    id("me.2bab.caliper")
}

android {
    namespace = "me.xx2bab.caliper.sample"
    compileSdk = 31
    defaultConfig {
        applicationId = "me.xx2bab.caliper.sample"
        minSdk = 23
        targetSdk = 31
        versionCode = 1
        versionName = "1.0.0"
    }

    buildTypes {
        getByName("debug") {
            isMinifyEnabled = false
            applicationIdSuffix = ".debug"
            versionNameSuffix = "-debug"
        }
        getByName("release") {
            isMinifyEnabled = false
        }
    }

    flavorDimensions += "featureScope"
    productFlavors {
        create("demo") {
            dimension = "featureScope"
            applicationIdSuffix = ".demo"
            versionNameSuffix = "-demo"
        }
        create("full") {
            dimension = "featureScope"
            applicationIdSuffix = ".full"
            versionNameSuffix = "-full"
        }
    }

    splits {
        density {
            isEnable = true
            reset()
            include("mdpi")
            compatibleScreens("xlarge")
        }
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
            // For AGP to trigger the resources package and other tasks for test variants
            // especially when using Robolectric to do Unit Test.
            isIncludeAndroidResources = true
        }
    }
}

dependencies {
    implementation(project(":library"))
    implementation(deps.kotlin.std)
    implementation("androidx.appcompat:appcompat:1.4.1")
    implementation("com.google.android.material:material:1.4.+")
    implementation("androidx.constraintlayout:constraintlayout:2.1.3")

    caliper("me.2bab:caliper-runtime-privacy:+")
    caliper("me.2bab:caliper-runtime-battery-optim:+")
    caliper(project(":custom-proxy"))

    testImplementation(deps.junit4)
    testImplementation(deps.robolectric)
    testImplementation(deps.hamcrest)
}

// Run `./gradlew clean assembleFullDebug` for testing
caliper {
    // Main feature flags (Mandatory).
    // Can not be lazily set, it's valid only if you call it before "afterEvaluate{}".
    enableByVariant { variant ->
        // With below snippet, only "FullDebug" variant will be interacted with Caliper.
        // variant.buildType == "debug" && variant.flavorName == "full"
        true
    }
}