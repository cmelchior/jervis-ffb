import org.gradle.declarative.dsl.schema.FqName.Empty.packageName
import org.jetbrains.kotlin.gradle.ExperimentalWasmDsl

plugins {
    alias(libs.plugins.multiplatform)
    alias(libs.plugins.serialization)
    alias(libs.plugins.buildconfig)
}

group = "com.jervisffb"
@Suppress("UNCHECKED_CAST")
version = (rootProject.ext["mavenVersion"] as Provider<String>).get()

// `core` holds the entire shared Jervis engine: model, commands, reports,
// actions, fsm, the abstract `Rules` type, and all shared rule
// implementations in `engine.rules.common.*`. Both ruleset submodules
// (`rules-bb2020`, `rules-bb2025`) depend on this. The higher-level
// `rules-common` module ties everything together with cross-ruleset glue
// (`RulesParametersHolder`, `JervisSerialization`).
@Suppress("UNCHECKED_CAST")
buildConfig {
    packageName("com.jervisffb")
    buildConfigField("releaseVersion", (rootProject.ext["publicVersion"] as Provider<String>).get())
    buildConfigField("gitHash", (rootProject.ext["gitHash"] as Provider<String>).get())
    buildConfigField("gitHashLong", (rootProject.ext["gitHashLong"] as Provider<String>).get())
    buildConfigField("gitHistory", (rootProject.ext["gitHistory"] as Provider<String>).get())
    useKotlinOutput { internalVisibility = false }
}

repositories {
    mavenCentral()
}

kotlin {
    jvmToolchain((project.properties["java.version"] as String).toInt())

    jvm {
        testRuns["test"].executionTask.configure {
            useJUnitPlatform()
        }
    }

    iosArm64()
    iosSimulatorArm64()

    @OptIn(ExperimentalWasmDsl::class)
    wasmJs {
        browser()
    }

    sourceSets {
        val commonMain by getting {
            dependencies {
                api(project(":modules:platform-utils"))
                api(libs.kotlinx.datetime)
                api(libs.kotlinx.collections.immutable)
                api(libs.coroutines)
                api(libs.jsonserialization)
                api(libs.okio)
                api(libs.cryptography.core)
            }
        }
        val jvmMain by getting {
            dependencies {
                implementation(libs.cryptography.provider.jdk)
            }
        }
        val wasmJsMain by getting {
            dependencies {
                implementation(libs.cryptography.provider.webcrypto)
            }
        }
        val commonTest by getting {
            dependencies {
                implementation(kotlin("test"))
                implementation(project(":modules:jervis-test-utils"))
            }
        }
    }
}
