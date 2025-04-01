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
    @Suppress("UNCHECKED_CAST")
    buildConfigField("gitHistory", rootProject.ext["gitHistory"] as String)
}

kotlin {
    jvmToolchain((project.properties["java.version"] as String).toInt())
    jvm()

    listOf(
        iosX64(),
        iosArm64(),
        iosSimulatorArm64()
    ).forEach { iosTarget ->
        iosTarget.binaries.framework {
            baseName = "JervisUI"
            isStatic = true
        }
    }

    wasmJs {
        moduleName = "jervis-ui"
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
                implementation(project(":modules:platform-utils"))
                implementation(project(":modules:fumbbl-net"))
                implementation(project(":modules:jervis-engine"))
                implementation(project(":modules:jervis-net"))
                implementation(project(":modules:jervis-resources"))
                implementation(libs.okio)
                implementation(libs.kotlinx.datetime)
                implementation(libs.coroutines)
                implementation(libs.bundles.voyager)
                implementation(libs.jsonserialization)
                implementation(compose.components.resources)
                implementation(compose.runtime)
                implementation(compose.foundation)
                implementation(compose.material)
            }
        }
        val commonTest by getting {
            dependencies {
                implementation(project(":modules:jervis-test-utils"))
                implementation(kotlin("test"))
            }
        }
        val jvmMain by getting {
            dependencies {
                // Additional dependencies required fog .ogg support
                // See https://github.com/finnkuusisto/TinySound/tree/master/lib
                implementation("kuusisto.tinysound:tinysound:1.1.1")
                implementation("javazoom.vorbisspi:vorbisspi:1.0.3")
                implementation("com.jcraft:jorbis:0.0.17")
                implementation("org.tritonus:tritonus_share:0.0.1")
                implementation(compose.preview)
                implementation(compose.desktop.currentOs)
                implementation(compose.desktop.uiTestJUnit4)
            }
        }
        val jvmTest by getting
        val wasmJsMain by getting
        val iosX64Main by getting
        val iosArm64Main by getting
        val iosSimulatorArm64Main by getting
        val iosMain by creating {
            dependsOn(commonMain)
            iosX64Main.dependsOn(this)
            iosArm64Main.dependsOn(this)
            iosSimulatorArm64Main.dependsOn(this)
            dependencies {
            }
        }
    }
}

compose.desktop {
    application {
        mainClass = "com.jervisffb.MainKt"
        // See https://youtrack.jetbrains.com/issue/CMP-7048/Missing-customization-options-for-About-dialog-on-MacOS
        // for request to customize the UI
        nativeDistributions {
            targetFormats(TargetFormat.Dmg, TargetFormat.Exe, TargetFormat.Deb)
            packageName = "Jervis Fantasy Football"
            packageVersion = rootProject.ext["distributionVersion"] as String

            // androidx.datastore requires sun.misc.Unsafe
            // See https://github.com/JetBrains/compose-multiplatform/issues/2686#issuecomment-1413429842
            modules("jdk.unsupported")
            val providers = project.providers
            macOS {
                bundleID = "com.jervisffb"
                iconFile.set(rootProject.file("icons/jervis.icns"))
                signing {
                    sign.set(true)
                    identity.set(providers.environmentVariable("JERVIS_MACOS_SIGNING_IDENTITY").getOrElse(""))
                    // keychain.set(providers.environmentVariable("JERVIS_MACOS_KEYCHAIN").getOrElse("")
                }
                notarization {
                    appleID.set(providers.environmentVariable("JERVIS_MACOS_NOTARIZATION_APPLE_ID").getOrElse(""))
                    password.set(providers.environmentVariable("JERVIS_MACOS_NOTARIZATION_PASSWORD").getOrElse(""))
                    teamID.set(providers.environmentVariable("JERVIS_MACOS_NOTARIZATION_TEAM_ID").getOrElse(""))
                }
            }

            windows {
                iconFile.set(rootProject.file("icons/jervis.ico"))
                upgradeUuid = providers.environmentVariable("JERVIS_WINDOWS_PACKAGE_GUID").getOrElse("")
                menuGroup = "Jervis Fantasy Football"
                dirChooser = true
                perUserInstall = true
                shortcut = true
            }

            linux {
                iconFile.set(rootProject.file("icons/logo.svg"))
                packageName = "jervis-ffb"
                debMaintainer = "christianmelchior@gmail.com"
                // appRelease = "1" - Not currently used. Need to figure out how to increment this if used
                appCategory = "Game"
                rpmLicenseType = "Apache-2.0"
                menuGroup = "Jervis Fantasy Football"
            }
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
