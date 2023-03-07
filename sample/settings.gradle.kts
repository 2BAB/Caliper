rootProject.name = "caliper-sample-root"

pluginManagement {
    extra["externalDependencyBaseDir"] = "../"
    val versions =
        file(extra["externalDependencyBaseDir"].toString() + "deps.versions.toml").readText()
    val regexPlaceHolder = "%s\\s\\=\\s\\\"([A-Za-z0-9\\.\\-]+)\\\""
    val getVersion =
        { s: String -> regexPlaceHolder.format(s).toRegex().find(versions)!!.groupValues[1] }

    plugins {
        kotlin("android") version getVersion("kotlinVer") apply false
        id("com.android.application") version getVersion("agpVer") apply false
        id("com.android.library") version getVersion("agpVer") apply false
        id("com.google.devtools.ksp") version getVersion("kspVer") apply false
    }
    resolutionStrategy {
        eachPlugin {
            if (requested.id.id == "me.2bab.caliper") {
                // It will be replaced by a local module using `includeBuild` below,
                // thus we just put a generic version (+) here.
                useModule("me.2bab:caliper-gradle-plugin:+")
            }
        }
    }
    repositories {
        mavenCentral()
        google()
        gradlePluginPortal()
        maven("https://oss.sonatype.org/content/repositories/snapshots/") // only for detekt snapshot so far
    }
}

val externalDependencyBaseDir = extra["externalDependencyBaseDir"].toString()
val enabledCompositionBuild = true

dependencyResolutionManagement {
    repositories {
        mavenLocal()
        google()
        mavenCentral()
    }
    versionCatalogs {
        create("deps") {
            from(files(externalDependencyBaseDir + "deps.versions.toml"))
        }
    }
}

include(":app", ":library", ":custom-proxy")
if (enabledCompositionBuild) {
    includeBuild(externalDependencyBaseDir) {
        dependencySubstitution {
            substitute(module("me.2bab:caliper-gradle-plugin"))
                .using(project(":caliper-gradle-plugin"))
            substitute(module("me.2bab:caliper-annotation-processor"))
                .using(project(":caliper-annotation-processor"))
            substitute(module("me.2bab:caliper-runtime"))
                .using(project(":caliper-runtime"))
            substitute(module("me.2bab:caliper-runtime-battery-optim"))
                .using(project(":caliper-runtime-battery-optim"))
            substitute(module("me.2bab:caliper-runtime-privacy"))
                .using(project(":caliper-runtime-privacy"))
        }
    }
}