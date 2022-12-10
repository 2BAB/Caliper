plugins {
    `kotlin-dsl`
    id("java-gradle-plugin")
//    `github-release`
//    `maven-central-publish`
}

java {
    withSourcesJar()
    sourceCompatibility = JavaVersion.VERSION_1_8
    targetCompatibility = JavaVersion.VERSION_1_8
}

gradlePlugin {
    plugins {
        create("caliper") {
            id = "me.2bab.caliper"
            implementationClass ="me.xx2bab.caliper.CaliperPlugin"
            displayName = "me.2bab.caliper"
        }
    }
}

dependencies {
    implementation(deps.polyfill.main)

    implementation(gradleApi())
    implementation(deps.kotlin.std)
    implementation(deps.kotlin.serialization)

    compileOnly(deps.android.gradle.plugin)

    testImplementation(gradleTestKit())
}
