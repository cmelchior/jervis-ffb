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
                api(project(":modules:jervis-engine:rules-common"))
                // BB2025 rules are built as an incremental layer on top of BB2020's
                // implementation and share several procedures (Multiple Block, push
                // chain, etc.). Depend on bb2020 explicitly so the reuse is honest.
                api(project(":modules:jervis-engine:rules-bb2020"))
            }
        }
        val commonTest by getting {
            dependencies {
                implementation(kotlin("test"))
                implementation(project(":modules:jervis-test-utils"))
            }
        }
    }
}
