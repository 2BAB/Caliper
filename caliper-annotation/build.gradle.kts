plugins {
    kotlin("jvm")
    kotlin("plugin.serialization")
}

dependencies {
    implementation(deps.kotlin.std)
    implementation(deps.kotlin.serialization)
}