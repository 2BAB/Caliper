// Once Android Studio or IDEA got issues fixed, we can remove corresponding script below,
// Gradle can run without these fixes.
task("clean") {
    delete(rootProject.buildDir)
}
