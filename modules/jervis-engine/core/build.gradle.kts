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
        browser()
    }

    sourceSets {
        val commonMain = getByName("commonMain") {
            dependencies {
                api(project(":modules:platform-utils"))
                api(libs.kotlinx.datetime)
                api(libs.kotlinx.collections.immutable)
                api(libs.coroutines)
                api(libs.jsonserialization)
                api(libs.okio)
                api(libs.cryptography.core)
            }
        }
        val jvmMain = getByName("jvmMain") {
            dependencies {
                implementation(libs.cryptography.provider.jdk)
            }
        }
        val wasmJsMain = getByName("wasmJsMain") {
            dependencies {
                implementation(libs.cryptography.provider.webcrypto)
            }
        }
        val commonTest = getByName("commonTest") {
            dependencies {
                implementation(kotlin("test"))
                implementation(project(":modules:jervis-test-utils"))
            }
        }
    }
}
