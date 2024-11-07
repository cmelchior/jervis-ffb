@file:OptIn(
    ExperimentalWasmDsl::class,
    org.jetbrains.compose.ExperimentalComposeLibrary::class
)

import org.jetbrains.compose.desktop.application.dsl.TargetFormat
import org.jetbrains.kotlin.gradle.ExperimentalWasmDsl
import org.jetbrains.kotlin.gradle.targets.js.webpack.KotlinWebpackConfig

plugins {
    alias(libs.plugins.serialization)
    alias(libs.plugins.multiplatform)
    alias(libs.plugins.jetbrainsCompose)
    alias(libs.plugins.composeCompiler)
    id("com.github.gmazzo.buildconfig") version "5.5.0"
}

group = "com.jervisffb"
version = rootProject.ext["mavenVersion"] as String

buildConfig {
    this.packageName("com.jervisffb.ui")
    buildConfigField("releaseVersion", rootProject.ext["publicVersion"] as String)
    buildConfigField("gitHash", rootProject.ext["gitHash"] as String)
}

kotlin {
    jvmToolchain((project.properties["java.version"] as String).toInt())
    jvm()
    wasmJs {
        moduleName = "common"
        browser {
            val projectDirPath = project.projectDir.path
            commonWebpackConfig {
                outputFileName = "composeApp.js"
                devServer = (devServer ?: KotlinWebpackConfig.DevServer()).apply {
                    static = (static ?: mutableListOf()).apply {
                        // Serve sources to debug inside browser
                        add(projectDirPath)
                    }
                }
            }
        }
        binaries.executable()
    }

    sourceSets {
        val commonMain by getting {
            dependencies {
                implementation(project(":modules:utils"))
                implementation(project(":modules:fumbbl-net"))
                implementation(project(":modules:jervis-engine"))
                implementation(libs.okio)
                implementation(libs.kotlinx.datetime)
                implementation(libs.coroutines)
                implementation(libs.bundles.voyager)
                implementation(libs.jsonserialization)
                implementation(compose.components.resources)
                api(compose.runtime)
                api(compose.foundation)
                api(compose.material)
            }
        }
        val commonTest by getting {
            dependencies {
                implementation(kotlin("test"))
            }
        }
        val jvmMain by getting {
            dependencies {
                api(compose.preview)
                implementation(compose.desktop.currentOs)
//                implementation(compose.desktop.macos_arm64)
//                implementation("org.jetbrains.skiko:skiko-awt-runtime-macos-arm64:+")
                implementation(compose.desktop.uiTestJUnit4)
            }
        }
        val jvmTest by getting
        val wasmJsMain by getting
    }
}

compose.desktop {
    application {
        mainClass = "com.jervisffb.MainKt"
        // See https://youtrack.jetbrains.com/issue/CMP-7048/Missing-customization-options-for-About-dialog-on-MacOS
        // for request to customize the UI
        nativeDistributions {
            targetFormats(TargetFormat.Dmg, TargetFormat.Msi, TargetFormat.Deb)
            packageName = "Test app"
            packageVersion = rootProject.ext["distributionVersion"] as String
        }

        // See https://youtrack.jetbrains.com/issue/CMP-4216
        buildTypes.release.proguard {
            // Enabling Proguard prevents the app from launching.
            // Needs more investigation
            isEnabled = false
            version.set("7.6.0")
            configurationFiles.from(project.file("jervisffb.pro"))
            optimize = true
            obfuscate = false
        }
    }
}
