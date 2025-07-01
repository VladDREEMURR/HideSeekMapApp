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

        maven("https://maven.tryformation.com/releases") {
            // optional but it speeds up the gradle dependency resolution
            content {
                includeGroup("com.jillesvangurp")
                includeGroup("com.github.jillesvangurp")
                includeGroup("com.tryformation")
            }
        }
//        maven ("http://maven.google.com/")
    }
}
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
    }
}

rootProject.name = "HideSeekMapApp"
include(":app")
