// This file is a thin aggregator that `api`-exports all four submodules so
// downstream consumers can use `implementation(project(":modules:jervis-engine"))`
//
// The aggregator is also responsible for hosting `JervisSerialization`, which
// composes the three per-module `SerializersModule`s (`coreSerializerModule`,
// `bb2020SerializerModule`, `bb2025SerializerModule`) into the single one
// used at runtime.
import org.jetbrains.kotlin.gradle.ExperimentalWasmDsl

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
    }

    iosArm64()
    iosSimulatorArm64()

    @OptIn(ExperimentalWasmDsl::class)
    wasmJs {
        outputModuleName.set("jervis-engine")
        browser()
    }

    sourceSets {
        val commonMain by getting {
            dependencies {
                api(project(":modules:jervis-engine:core"))
                api(project(":modules:jervis-engine:rules-common"))
                api(project(":modules:jervis-engine:rules-bb2020"))
                api(project(":modules:jervis-engine:rules-bb2025"))
            }
        }
        val commonTest by getting {
            dependencies {
                implementation(project(":modules:jervis-test-utils"))
                implementation(kotlin("test"))
            }
        }
    }
}
