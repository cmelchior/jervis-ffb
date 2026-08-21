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
        outputModuleName.set("tourplay-net")
        browser()
    }

    sourceSets {
        val commonMain = getByName("commonMain") {
            dependencies {
                implementation(project(":modules:platform-utils"))
                implementation(project(":modules:jervis-engine:package"))
                // Only here to be able to swap FUMBBL rosters with default ones. Should be removed when
                // we create rosters from the FUMBBL API instead.
                implementation(project(":modules:jervis-resources"))
                implementation(libs.coroutines)
                implementation(libs.okio)
                implementation(libs.kotlinx.datetime)
            }
        }
        val commonTest = getByName("commonTest") {
            dependencies {
                implementation(kotlin("test"))
            }
        }
        val jvmMain = getByName("jvmMain")
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
