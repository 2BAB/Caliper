plugins {
    id("com.android.library")
    kotlin("android")

    id("com.google.devtools.ksp") // Apply the KSP plugin ahead of Caliper
    id("me.2bab.caliper")
}

android {
    namespace = "me.xx2bab.caliper.sample.customproxy"
    compileSdk = 31
    defaultConfig {
        minSdk = 23
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    sourceSets["main"].java.srcDir("src/main/kotlin")

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlinOptions {
        jvmTarget = "17"
    }

}

dependencies {
    implementation(project(":library"))
}