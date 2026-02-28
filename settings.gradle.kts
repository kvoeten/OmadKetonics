pluginManagement {
    repositories {
        google {
            content {
                includeGroupByRegex("com\\.android.*")
                includeGroupByRegex("com\\.google.*")
                includeGroupByRegex("androidx.*")
            }
        }
        mavenCentral()
        gradlePluginPortal()
    }
}
plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "1.0.0"
}
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
    }
}

rootProject.name = "OmadKetonics"
include(":app")
include(":core:model")
include(":core:common")
include(":core:ui")
include(":domain")
include(":data:local")
include(":data:remote")
include(":data:repository")
include(":feature:plan")
include(":feature:groceries")
include(":feature:recipes")
include(":feature:rankings")
include(":feature:progress")
