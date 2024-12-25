import org.jetbrains.kotlin.gradle.ExperimentalWasmDsl
import org.jetbrains.kotlin.gradle.dsl.KotlinCompile

plugins {
    alias(libs.plugins.multiplatform)
}

group = "com.jervisffb.utils"
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
                api("io.ktor:ktor-client-core:$ktor")
                api("io.ktor:ktor-client-logging:$ktor")
                api("io.ktor:ktor-client-websockets:$ktor")
                api("io.ktor:ktor-serialization-kotlinx-json:$ktor")
            }
        }

        val jvmMain by getting {
            dependencies {
                implementation("io.ktor:ktor-client-okhttp:$ktor")
                implementation("org.reflections:reflections:0.10.2")
                implementation("androidx.datastore:datastore-jvm:1.1.0")
                implementation("androidx.datastore:datastore-preferences-jvm:1.1.0")
            }
        }

        val wasmJsMain by getting {
            dependencies {
                implementation("com.juul.indexeddb:core:main-SNAPSHOT")
                implementation("org.jetbrains.kotlinx:kotlinx-browser:0.3")
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
        }
    }
}

tasks.withType<KotlinCompile<*>>().configureEach {
    kotlinOptions {
        freeCompilerArgs = listOf("-Xwasm-use-new-exception-proposal")
    }
}
