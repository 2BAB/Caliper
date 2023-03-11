plugins {
    id("com.android.library")
    `maven-publish`
}

android {
    publishing {
        multipleVariants("allVariants") {
            allVariants()
            withSourcesJar()
            withJavadocJar()
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