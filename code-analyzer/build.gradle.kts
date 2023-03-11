// The project forked from https://github.com/bennyhuo/kotlin-code-analyzer .
// Thus I would like to keep it independent from the Caliper main project,
// will not use version catelog or any other resources from the main project.

plugins {
    kotlin("jvm")
    `maven-central-publish`
    `jar-publish`
}

dependencies {
    val detektVer = "1.22.0"
    implementation("io.gitlab.arturbosch.detekt:detekt-core:$detektVer")
    implementation("io.gitlab.arturbosch.detekt:detekt-tooling:$detektVer")
    implementation("io.gitlab.arturbosch.detekt:detekt-utils:$detektVer") // main-SNAPSHOT
    api("org.jetbrains.kotlin:kotlin-compiler-embeddable:1.7.22") // 1.8.10

    testImplementation("junit:junit:4.12")
}

java {
    withSourcesJar()
}