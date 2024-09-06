plugins {
    alias(libs.plugins.multiplatform)
    alias(libs.plugins.serialization)
}

group = "dk.ilios"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

kotlin {
    jvmToolchain((project.properties["java.version"] as String).toInt())
    jvm {
        withJava()
        testRuns["test"].executionTask.configure {
            useJUnitPlatform()
        }
    }

    wasmJs {
        moduleName = "game-model"
        browser()
    }

    sourceSets {
        val commonMain by getting {
            dependencies {
                implementation(project(":modules:utils"))
                implementation(kotlin("reflect"))
                implementation(libs.coroutines)
                implementation(libs.jsonserialization)
                implementation(libs.okio)
            }
        }
        val commonTest by getting {
            dependencies {
                implementation(kotlin("test"))
            }
        }
        val jvmMain by getting
        val jvmTest by getting
    }
}
