rootProject.name = "FUMBBL AI"

dependencyResolutionManagement {
    versionCatalogs {
        create("libs") {
            // Versions
            val kotlin = version("kotlinVersion", "1.8.21")
            val ktor = version("ktor", "2.3.0")

            // Plugins
            plugin("jvm", "org.jetbrains.kotlin.jvm").versionRef(kotlin)
            plugin("multiplatform", "org.jetbrains.kotlin.multiplatform").versionRef(kotlin)
            plugin("serialization", "org.jetbrains.kotlin.plugin.serialization").versionRef(kotlin)
            plugin("ktor", "io.ktor.plugin").versionRef(ktor)

            // Dependencies
        }
    }
    repositories {
        mavenCentral()
    }
}

include(":game-model")
include(":game-downloader")


