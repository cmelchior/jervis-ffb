import org.jetbrains.kotlin.gradle.ExperimentalWasmDsl

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
    }

    iosX64()
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
                implementation(project(":modules:platform-utils"))
                implementation(libs.cryptography.core)
                implementation(libs.kotlinx.datetime)
                implementation(libs.coroutines)
                implementation(libs.jsonserialization)
                implementation(libs.okio)
            }
        }
        val commonTest by getting {
            dependencies {
                implementation(project(":modules:jervis-test-utils"))
                implementation(kotlin("test"))
            }
        }
        val jvmMain by getting {
            dependencies {
                implementation(libs.cryptography.provider.jdk)
            }
        }
        val jvmTest by getting
        val wasmJsMain by getting {
            dependencies {
                implementation(libs.cryptography.provider.webcrypto)
            }
        }
        val wasmJsTest by getting
    }
}
