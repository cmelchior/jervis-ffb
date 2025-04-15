import org.jetbrains.kotlin.gradle.ExperimentalWasmDsl
import org.jetbrains.kotlin.gradle.dsl.KotlinCompile

plugins {
    alias(libs.plugins.multiplatform)
}

group = "com.jervisffb"
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
        val ktor = libs.versions.ktor.get()
        val commonMain by getting {
            dependencies {
                implementation(kotlin("reflect"))
                implementation(libs.coroutines)
                implementation(libs.okio)
                implementation(libs.okio.fake)
                api(libs.jsonserialization)
                api(libs.kermit)
                api(libs.ktor.client.core)
                api(libs.ktor.client.logging)
                api(libs.ktor.client.websockets)
                api(libs.ktor.client.contentNegotiation)
                api(libs.ktor.serialization.json)
            }
        }

        val jvmMain by getting {
            dependencies {
                implementation(libs.coroutines.swing)
                implementation(libs.ktor.client.okhttp)
                implementation(libs.reflections)
                implementation(libs.datastore)
                implementation(libs.datastore.preferences)
            }
        }

        val wasmJsMain by getting {
            dependencies {
                // Stored in mavenRepo for now
                implementation("com.juul.indexeddb:core:main-SNAPSHOT")
                implementation(libs.kotlinx.browser)
            }
        }

        val iosX64Main by getting
        val iosArm64Main by getting
        val iosSimulatorArm64Main by getting
        val iosMain by creating {
            dependsOn(commonMain)
            iosX64Main.dependsOn(this)
            iosArm64Main.dependsOn(this)
            iosSimulatorArm64Main.dependsOn(this)
            dependencies {
                implementation(libs.ktor.client.darwin)
            }
        }
    }
}

tasks.withType<KotlinCompile<*>>().configureEach {
    kotlinOptions {
        freeCompilerArgs = listOf("-Xwasm-use-new-exception-proposal")
    }
}
