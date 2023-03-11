plugins {
    `kotlin-dsl`
}

dependencies {
    // GitHub Release
    implementation("com.github.breadmoirai:github-release:2.4.1")
    implementation(deps.android.gradle.plugin)
    implementation(deps.kotlin.plugin)
}
