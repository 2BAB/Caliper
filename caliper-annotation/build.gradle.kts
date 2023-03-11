plugins {
    kotlin("jvm")
    kotlin("plugin.serialization")
    `maven-central-publish`
    `jar-publish`
}

dependencies {
    implementation(deps.kotlin.std)
    implementation(deps.kotlin.serialization)
}
