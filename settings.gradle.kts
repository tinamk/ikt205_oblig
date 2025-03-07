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
dependencyResolutionManagement {
    repositories {
        google()
        mavenCentral()
        // Add JitPack repository
        maven { url = uri("https://jitpack.io") }
    }
}

rootProject.name = "Applikasjons_Avokadoene"
include(":app")
