plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "0.5.0"
}
rootProject.name = "Jervis-A-Blood-Bowl-AI"

dependencyResolutionManagement {
    versionCatalogs {
        create("libs") {
            // Versions
            val kotlin = version("kotlinVersion", "1.9.23")
            val ktor = version("ktor", "2.3.7")
            val compose = version("compose", "1.6.1")
            val coroutines = version("coroutines", "1.7.3")
            val minimalJson = version("minimalJson", "0.9.5")
            val javaAssist = version("javaAssist", "3.29.2-GA")
//            val serialization = version("serialization", "1.5.1")

            // Plugins
            plugin("jvm", "org.jetbrains.kotlin.jvm").versionRef(kotlin)
            plugin("multiplatform", "org.jetbrains.kotlin.multiplatform").versionRef(kotlin)
            plugin("serialization", "org.jetbrains.kotlin.plugin.serialization").versionRef(kotlin)
            plugin("ktor", "io.ktor.plugin").versionRef(ktor)
            plugin("compose", "org.jetbrains.compose").versionRef(compose)

            // Libraries
            library("coroutines", "org.jetbrains.kotlinx:kotlinx-coroutines-core:1.8.0")
            library("jsonserialization", "org.jetbrains.kotlinx:kotlinx-serialization-json:1.6.3")
            library("okio", "com.squareup.okio:okio:3.7.0")
        }
    }
    repositories {
        mavenCentral()
        google()
    }
}

include(":modules:fumbbl-cli")
include(":modules:fumbbl-net")
include(":modules:game-model")
include(":Debug-FantasyFootballClient")
include(":modules:replay-analyzer")
include(":modules:game-ui:common")
include(":modules:game-ui:desktop")
include(":modules:game-ui:web")
