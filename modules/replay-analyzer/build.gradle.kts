@file:OptIn(ExperimentalKotlinGradlePluginApi::class)

import org.jetbrains.kotlin.gradle.ExperimentalKotlinGradlePluginApi

plugins {
    alias(libs.plugins.multiplatform)
    alias(libs.plugins.serialization)
}

group = "com.jervisffb"
version = rootProject.ext["mavenVersion"] as String

repositories {
    mavenCentral()
}


kotlin {
    jvmToolchain((project.properties["java.version"] as String).toInt())
    jvm {
        testRuns["test"].executionTask.configure {
            useJUnitPlatform()
        }
        binaries {
            executable {
                mainClass.set("com.jervisffb.replay.analyzer.ReplayAnalyzerKt")
            }
        }
    }

    sourceSets {
        val commonMain by getting {
            dependencies {
                implementation(project(":modules:platform-utils"))
                implementation(libs.coroutines)
                implementation(project(":modules:fumbbl-net"))
                implementation(project(":modules:jervis-engine"))
            }
        }
        val commonTest by getting {
            dependencies {
                implementation(kotlin("test"))
            }
        }
        val jvmMain by getting {
            dependencies {
                implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.5.1")
//    implementation(project(mapOf("path" to ":game-model")))
//                testImplementation("org.junit.jupiter:junit-jupiter-api:5.8.1")
//                testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:5.8.1")
            }
        }
        val jvmTest by getting
    }
}
