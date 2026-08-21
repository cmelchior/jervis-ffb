import org.jetbrains.kotlin.gradle.ExperimentalWasmDsl

plugins {
    alias(libs.plugins.multiplatform)
}

group = "com.jervisffb"
@Suppress("UNCHECKED_CAST")
version = (rootProject.ext["mavenVersion"] as Provider<String>).get()

kotlin {
    jvmToolchain((project.findProperty("java.version") as String).toInt())
    jvm()

    iosArm64()
    iosSimulatorArm64()

    @OptIn(ExperimentalWasmDsl::class)
    wasmJs {
        outputModuleName.set("jervis-resources")
        browser()
    }

    sourceSets {
        val commonMain = getByName("commonMain") {
            dependencies {
                implementation(project(":modules:platform-utils"))
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
            }
        }

        val wasmJsMain = getByName("wasmJsMain") {
            dependencies {
            }
        }

        val iosArm64Main = getByName("iosArm64Main")
        val iosSimulatorArm64Main = getByName("iosSimulatorArm64Main")
        val iosMain = create("iosMain") {
            dependsOn(commonMain)
            iosArm64Main.dependsOn(this)
            iosSimulatorArm64Main.dependsOn(this)
        }
    }
}
