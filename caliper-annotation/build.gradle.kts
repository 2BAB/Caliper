plugins {
    kotlin("jvm")
    kotlin("plugin.serialization")
    `maven-central-publish`
}

dependencies {
    implementation(deps.kotlin.std)
    implementation(deps.kotlin.serialization)
}