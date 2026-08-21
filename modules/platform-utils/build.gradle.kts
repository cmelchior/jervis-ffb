import org.jetbrains.kotlin.gradle.ExperimentalWasmDsl

plugins {
    alias(libs.plugins.serialization)
    alias(libs.plugins.multiplatform)
    alias(libs.plugins.buildconfig)

}

group = "com.jervisffb"
@Suppress("UNCHECKED_CAST")
version = (rootProject.ext["mavenVersion"] as Provider<String>).get()

@Suppress("UNCHECKED_CAST")
buildConfig {
    packageName("com.jervisffb")
    buildConfigField("releaseVersion", (rootProject.ext["publicVersion"] as Provider<String>).get())
    buildConfigField("gitHash", (rootProject.ext["gitHash"] as Provider<String>).get())
    buildConfigField("gitHashLong", (rootProject.ext["gitHashLong"] as Provider<String>).get())
    buildConfigField("gitHistory", (rootProject.ext["gitHistory"] as Provider<String>).get())
    useKotlinOutput { internalVisibility = false }
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
        outputModuleName.set("utils")
        browser()
    }

    sourceSets {
        val commonMain = getByName("commonMain") {
            dependencies {
                implementation(kotlin("reflect"))
                implementation(libs.coroutines)
                implementation(libs.okio)
                implementation(libs.okio.fake)
                implementation(libs.settings)
                implementation(libs.settings.noarg)
                implementation(libs.settings.coroutines)
                implementation(libs.settings.observable)
                api(libs.jsonserialization)
                api(libs.kermit)
                api(libs.ktor.client.core)
                api(libs.ktor.client.logging)
                api(libs.ktor.client.websockets)
                api(libs.ktor.client.contentNegotiation)
                api(libs.ktor.serialization.json)
            }
        }

        val jvmMain = getByName("jvmMain") {
            dependencies {
                implementation(libs.coroutines.swing)
                implementation(libs.ktor.client.okhttp)
                implementation(libs.reflections)
                implementation(libs.conveyor.control)
            }
        }

        val jvmTest = getByName("jvmTest") {
            dependencies {
                implementation(kotlin("test"))
                implementation(libs.coroutines)
            }
        }

        val wasmJsMain = getByName("wasmJsMain") {
            dependencies {
                // Stored in mavenRepo for now
                implementation("com.juul.indexeddb:core:main-SNAPSHOT")
                implementation(libs.kotlinx.browser)
            }
        }

        val iosArm64Main = getByName("iosArm64Main")
        val iosSimulatorArm64Main = getByName("iosSimulatorArm64Main")
        val iosMain = create("iosMain") {
            dependsOn(commonMain)
            iosArm64Main.dependsOn(this)
            iosSimulatorArm64Main.dependsOn(this)
            dependencies {
                implementation(libs.ktor.client.darwin)
            }
        }
    }
}
