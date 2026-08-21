import org.jetbrains.kotlin.gradle.ExperimentalWasmDsl

plugins {
    alias(libs.plugins.multiplatform)
    alias(libs.plugins.serialization)
    alias(libs.plugins.atomicfu)
}

group = "com.jervisffb"
@Suppress("UNCHECKED_CAST")
version = (rootProject.ext["mavenVersion"] as Provider<String>).get()

repositories {
    mavenCentral()
    google()
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
        outputModuleName.set("jervis-net")
        browser()
    }

    sourceSets {
        val commonMain = getByName("commonMain") {
            dependencies {
                implementation(project(":modules:platform-utils"))
                implementation(project(":modules:jervis-engine:package"))
                implementation(libs.ktor.client.core)
                implementation(libs.ktor.client.logging)
                implementation(libs.ktor.client.websockets)
                implementation(libs.coroutines)
                implementation(libs.jsonserialization)
                implementation(libs.okio)
                implementation(libs.kotlinx.datetime)
            }
        }
        val commonTest = getByName("commonTest") {
            dependencies {
                implementation(project(":modules:jervis-test-utils"))
                implementation(project(":modules:jervis-engine:package"))
                implementation(kotlin("test"))
            }
        }
        val jvmMain = getByName("jvmMain") {
            dependencies {
                implementation(libs.ktor.server.core.jvm)
                implementation(libs.ktor.server.websockets)
                implementation(libs.ktor.server.contentNegotation)
                implementation(libs.ktor.serialization.json)
                implementation(libs.ktor.server.netty)
            }
        }
        val jvmTest = getByName("jvmTest")
        val wasmJsMain = getByName("wasmJsMain")
        val iosArm64Main = getByName("iosArm64Main")
        val iosSimulatorArm64Main = getByName("iosSimulatorArm64Main")
        val iosMain = create("iosMain") {
            dependsOn(commonMain)
            iosArm64Main.dependsOn(this)
            iosSimulatorArm64Main.dependsOn(this)
        }
    }
}
