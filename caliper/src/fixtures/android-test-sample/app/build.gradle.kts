plugins {
    id("com.android.application")
    id("me.2bab.caliper")
}
android {
    compileSdk = 31
    defaultConfig {
        applicationId = "me.xx2bab.sample"
        minSdk = 23
        targetSdk = 31
        versionCode = 1
        versionName = "1.0"
    }
    buildTypes {
        getByName("debug") {
            isMinifyEnabled = false
        }
    }
}

dependencies {
    implementation("androidx.appcompat:appcompat:1.4.1")
}

