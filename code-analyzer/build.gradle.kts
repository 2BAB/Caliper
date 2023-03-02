// The project forked from https://github.com/bennyhuo/kotlin-code-analyzer .
// Thus I would like to keep it independent from the Caliper main project,
// will not use version catelog or any other resources from the main project.

plugins {
    kotlin("jvm")
}

dependencies {
    val detektVer = "main-SNAPSHOT"
    implementation("io.gitlab.arturbosch.detekt:detekt-core:$detektVer")
    implementation("io.gitlab.arturbosch.detekt:detekt-tooling:$detektVer")
    implementation("io.gitlab.arturbosch.detekt:detekt-parser:$detektVer")
    implementation("io.gitlab.arturbosch.detekt:detekt-utils:$detektVer")
    api("org.jetbrains.kotlin:kotlin-compiler-embeddable:1.8.10")

    testImplementation("junit:junit:4.12")
}