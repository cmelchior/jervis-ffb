import org.jetbrains.kotlin.gradle.targets.js.dsl.ExperimentalWasmDsl

plugins {
    alias(libs.plugins.multiplatform)
    alias(libs.plugins.serialization)
}

group = "dk.ilios.jervis"
version = "fumbbl-net"

repositories {
    mavenCentral()
    google()
}

kotlin {
    jvmToolchain((project.properties["java.version"] as String).toInt())
    jvm {
        testRuns["test"].executionTask.configure {
            useJUnitPlatform()
        }
    }
    @OptIn(ExperimentalWasmDsl::class)
    wasmJs {
        moduleName = "fumbbl-net"
        browser()
    }


    val ktor = libs.versions.ktor.get()
    sourceSets {
        val commonMain by getting {
            dependencies {
                implementation(project(":modules:utils"))
                implementation(project(":modules:jervis-model"))
                implementation(kotlin("reflect"))
                implementation(libs.coroutines)
                implementation(libs.okio)
                implementation(libs.kotlinx.datetime)
                implementation("io.ktor:ktor-client-core:$ktor")
                implementation("io.ktor:ktor-client-logging:$ktor")
                implementation("io.ktor:ktor-client-websockets:$ktor")
                implementation("io.ktor:ktor-serialization-kotlinx-json:$ktor")
                implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:${libs.versions.serialization}")
            }
        }
        val commonTest by getting {
            dependencies {
                implementation(kotlin("test"))
            }
        }
        val jvmMain by getting {
            dependencies {
                implementation("io.ktor:ktor-client-okhttp:$ktor")
                implementation("org.reflections:reflections:0.10.2")
            }
        }
        val jvmTest by getting
        val wasmJsMain by getting {
            dependencies {
//                implementation("io.ktor:ktor-client-core-wasm-js:$ktor")
            }
        }
    }
}
