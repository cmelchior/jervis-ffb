import org.jetbrains.compose.desktop.application.dsl.TargetFormat
import org.jetbrains.kotlin.gradle.targets.js.webpack.KotlinWebpackConfig

plugins {
    alias(libs.plugins.serialization)
    alias(libs.plugins.multiplatform)
    alias(libs.plugins.jetbrainsCompose)
    alias(libs.plugins.composeCompiler)
}

group = "dk.ilios.bloodbowl.ui"
version = "1.0-SNAPSHOT"

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
                @OptIn(org.jetbrains.compose.ExperimentalComposeLibrary::class)
                implementation(compose.desktop.uiTestJUnit4)
            }
        }
        val jvmTest by getting
        val wasmJsMain by getting
    }
}

// android {
//    compileSdkVersion(33)
//    sourceSets["main"].manifest.srcFile("src/androidMain/AndroidManifest.xml")
//    defaultConfig {
//        minSdkVersion(24)
//        targetSdkVersion(33)
//    }
//    compileOptions {
//        sourceCompatibility = JavaVersion.VERSION_1_8
//        targetCompatibility = JavaVersion.VERSION_1_8
//    }
// }


compose.desktop {
    application {
        mainClass = "MainKt"
        nativeDistributions {
            targetFormats(TargetFormat.Dmg)
//            targetFormats(TargetFormat.Dmg, TargetFormat.Msi, TargetFormat.Deb)
            packageName = "jervis-ui"
            packageVersion = "1.0.0"
        }
    }
}
