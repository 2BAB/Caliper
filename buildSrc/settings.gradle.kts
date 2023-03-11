dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        mavenCentral()
        google()
        gradlePluginPortal()
        maven("https://oss.sonatype.org/content/repositories/snapshots/") // only for detekt snapshot so far
    }
    versionCatalogs {
        create("deps") {
            from(files("../deps.versions.toml"))
        }
    }
}