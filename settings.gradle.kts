enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")
pluginManagement {
    val versions = file("deps.versions.toml").readText()
    val regexPlaceHolder = "%s\\s\\=\\s\\\"([A-Za-z0-9\\.\\-]+)\\\""
    val getVersion = { s: String -> regexPlaceHolder.format(s).toRegex().find(versions)!!.groupValues[1] }

    plugins {
        kotlin("jvm") version getVersion("kotlinVer") apply false
        kotlin("android") version getVersion("kotlinVer") apply false
        kotlin("plugin.serialization") version getVersion("kotlinVer") apply false
        id("com.android.library") version getVersion("agpVer") apply false
    }
    repositories {
        mavenCentral()
        google()
        gradlePluginPortal()
    }
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        mavenCentral()
        google()
    }
    versionCatalogs {
        create("deps") {
            from(files("./deps.versions.toml"))
        }
    }
}
rootProject.name = "caliper-root"

include(
    ":caliper-annotation",
    ":caliper-annotation-processor",

    ":caliper-gradle-plugin",
    ":gradle-instrumented-kit",

    ":caliper-runtime",
    ":caliper-runtime-battery-metric",
    ":caliper-runtime-sensitive-permission",
)
