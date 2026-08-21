@file:OptIn(ExperimentalKotlinGradlePluginApi::class)

import org.jetbrains.kotlin.gradle.ExperimentalKotlinGradlePluginApi

plugins {
    alias(libs.plugins.multiplatform)
    alias(libs.plugins.serialization)
}

group = "com.jervisffb"
@Suppress("UNCHECKED_CAST")
version = (rootProject.ext["mavenVersion"] as Provider<String>).get()

repositories {
    mavenCentral()
}


kotlin {
    jvmToolchain((project.findProperty("java.version") as String).toInt())
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
        val commonMain = getByName("commonMain") {
            dependencies {
                implementation(project(":modules:platform-utils"))
                implementation(libs.coroutines)
                implementation(project(":modules:fumbbl-net"))
                implementation(project(":modules:jervis-engine:package"))
            }
        }
        val commonTest = getByName("commonTest") {
            dependencies {
                implementation(kotlin("test"))
            }
        }
        val jvmMain = getByName("jvmMain") {
            dependencies {
                implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.5.1")
            }
        }
        val jvmTest = getByName("jvmTest")
    }
}
