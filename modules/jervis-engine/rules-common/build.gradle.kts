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

// `rules-common` is the shared rules layer that sits between `core` (engine
// infrastructure) and the two ruleset modules (`rules-bb2020`, `rules-bb2025`).
// It hosts `AbstractRules` — the concrete abstract-class body backing the
// [Rules] interface in `core` — plus all the shared rules code that used to
// live under `engine.rules.common.*`.
// Depends only on `core`.
kotlin {
    jvmToolchain((project.properties["java.version"] as String).toInt())

    jvm {
        testRuns["test"].executionTask.configure {
            useJUnitPlatform()
        }
    }

    iosArm64()
    iosSimulatorArm64()

    @OptIn(ExperimentalWasmDsl::class)
    wasmJs {
        browser()
    }

    sourceSets {
        val commonMain by getting {
            dependencies {
                api(project(":modules:jervis-engine:core"))
            }
        }
        val commonTest by getting {
            dependencies {
                implementation(project(":modules:jervis-test-utils"))
            }
        }
    }
}
