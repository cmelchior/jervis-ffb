import org.jetbrains.kotlin.gradle.ExperimentalWasmDsl

plugins {
    alias(libs.plugins.multiplatform)
}

group = "com.jervisffb.test"
version = "1.0-SNAPSHOT"

kotlin {
    jvmToolchain((project.properties["java.version"] as String).toInt())
    jvm()

    iosX64()
    iosArm64()
    iosSimulatorArm64()

    @OptIn(ExperimentalWasmDsl::class)
    wasmJs {
        moduleName = "utils"
        browser()
    }

    sourceSets {
        val commonMain by getting {
            dependencies {
                implementation(project(":modules:jervis-engine"))
            }
        }
    }
}
