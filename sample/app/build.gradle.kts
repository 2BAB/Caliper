plugins {
    id("com.android.application")
    kotlin("android")
    id("me.2bab.caliper")
}

android {
    compileSdk = 31
    defaultConfig {
        applicationId = "me.xx2bab.caliper.sample"
        minSdk = 23
        targetSdk = 31
        versionCode = 1
        versionName = "3.2.0"
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
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }

    kotlinOptions {
        jvmTarget = "1.8"
    }
}


dependencies {
    implementation(deps.kotlin.std)
    implementation("androidx.appcompat:appcompat:1.4.1")
}

// Run `./gradlew clean assembleFullDebug` for testing
caliper {
    // Main feature flags. !!! Mandatory field.!!!
    // Can not be lazily set, it's a valid only if you call it before "afterEvaluate{}".
    // With below snippet, only "FullDebug" variant will be interacted with Caliper.
    enableByVariant { variant ->
        variant.name.contains("debug", true)
                && variant.name.contains("full", true)
    }
}