rootProject.name = "FUMBBL AI"

dependencyResolutionManagement {
    versionCatalogs {
        create("libs") {
            // Versions
            val kotlin = version("kotlinVersion", "1.9.20")
            val ktor = version("ktor", "2.3.0")
            val compose = version("compose", "1.5.10")
            val coroutines = version("coroutines", "1.7.3")

            // Plugins
            plugin("jvm", "org.jetbrains.kotlin.jvm").versionRef(kotlin)
            plugin("multiplatform", "org.jetbrains.kotlin.multiplatform").versionRef(kotlin)
            plugin("serialization", "org.jetbrains.kotlin.plugin.serialization").versionRef(kotlin)
            plugin("ktor", "io.ktor.plugin").versionRef(ktor)
            plugin("compose", "org.jetbrains.compose").versionRef(compose)

            // Libraries
            library("coroutines", "org.jetbrains.kotlinx:kotlinx-coroutines-core:1.7.3")
//            plugin

            //        kotlin("multiplatform").version(extra["kotlin.version"] as String)
//        kotlin("android").version(extra["kotlin.version"] as String)
//        id("com.android.application").version(extra["agp.version"] as String)
//        id("com.android.library").version(extra["agp.version"] as String)
//        id("org.jetbrains.compose").version(extra["compose.version"] as String)

            // Dependencies
        }
    }
    repositories {
        mavenCentral()
    }
}

include(":game-model")
include(":game-downloader")
include(":FantasyFootballClient")
include("replay-analyzer")
include("fumbbl-client-downloader")
//include(":game-ui:android")
include(":game-ui:common")
include(":game-ui:desktop")
include(":game-ui:web")
