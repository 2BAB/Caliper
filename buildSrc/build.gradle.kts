plugins {
    `kotlin-dsl`
}

repositories {
    mavenCentral()
    gradlePluginPortal()
}

dependencies {
    // GitHub Release
    implementation("com.github.breadmoirai:github-release:2.4.1")
}
