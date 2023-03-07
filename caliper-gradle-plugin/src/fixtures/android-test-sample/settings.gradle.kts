include(":app")

dependencyResolutionManagement {
    repositories {
        google()
        mavenCentral()
    }
}

includeBuild("../../../../") {
    dependencySubstitution {
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