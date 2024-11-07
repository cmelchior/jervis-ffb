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
                implementation("dev.whyoleg.cryptography:cryptography-core:0.3.1")
                implementation(libs.kotlinx.datetime)
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
        val jvmMain by getting {
            dependencies {
                implementation("dev.whyoleg.cryptography:cryptography-provider-jdk:0.3.1")
            }
        }
        val jvmTest by getting
        val wasmJsMain by getting {
            dependencies {
                implementation("dev.whyoleg.cryptography:cryptography-provider-webcrypto:0.3.1")
            }
        }
        val wasmJsTest by getting
    }
}
