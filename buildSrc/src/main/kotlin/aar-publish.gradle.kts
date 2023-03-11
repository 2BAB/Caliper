plugins {
    id("com.android.library")
}

android {
    publishing {
        multipleVariants("allVariants") {
            allVariants()
            withSourcesJar()
        }
    }
}

publishing {
    publications {
        register<MavenPublication>("allVariants") {
            afterEvaluate {
                from(components["allVariants"])
            }
        }
    }
}