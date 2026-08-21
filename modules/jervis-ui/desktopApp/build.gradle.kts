import org.jetbrains.compose.desktop.application.dsl.TargetFormat
import org.jetbrains.compose.reload.gradle.ComposeHotRun

plugins {
    alias(libs.plugins.jvm)
    alias(libs.plugins.jetbrainsCompose)
    alias(libs.plugins.composeCompiler)
    alias(libs.plugins.conveyor)

}

group = "com.jervisffb"
@Suppress("UNCHECKED_CAST")
version = (rootProject.ext["mavenVersion"] as Provider<String>).get()

kotlin {
    jvmToolchain((project.findProperty("java.version") as String).toInt())
    dependencies {
        implementation(project(":modules:jervis-ui:shared"))
        val composeVersion = libs.versions.compose.get()
        implementation("org.jetbrains.compose.ui:ui-tooling-preview:$composeVersion")
        implementation("org.jetbrains.compose.ui:ui-test-junit4:$composeVersion")
        implementation(compose.desktop.currentOs)
        testImplementation(project(":modules:jervis-test-utils"))
        testImplementation(kotlin("test"))
    }
}

tasks.withType<ComposeHotRun>().configureEach {
    mainClass.set("com.jervisffb.MainKt")
}

compose.desktop {
    application {
        mainClass = "com.jervisffb.MainKt"
        // See https://youtrack.jetbrains.com/issue/CMP-7048/Missing-customization-options-for-About-dialog-on-MacOS
        // for request to customize the UI
        nativeDistributions {
            targetFormats(TargetFormat.Dmg, TargetFormat.Exe, TargetFormat.Deb)
            packageName = "Jervis Fantasy Football"
            @Suppress("UNCHECKED_CAST")
            packageVersion = (rootProject.ext["distributionVersion"] as Provider<String>).get()

            // androidx.datastore requires sun.misc.Unsafe
            // See https://github.com/JetBrains/compose-multiplatform/issues/2686#issuecomment-1413429842
            modules("jdk.unsupported")
            macOS {
                bundleID = "com.jervisffb"
                iconFile.set(rootProject.file("logo/logo.icns"))
                signing {
                    sign.set(true)
                    identity.set(providers.environmentVariable("JERVIS_MACOS_SIGNING_ID").getOrNull())
                    keychain.set(providers.environmentVariable("JERVIS_MACOS_KEYCHAIN_PATH").getOrNull())
                }
                notarization {
                    appleID.set(providers.environmentVariable("JERVIS_MACOS_NOTARIZATION_APPLE_ID").getOrNull())
                    password.set(providers.environmentVariable("JERVIS_MACOS_NOTARIZATION_PASSWORD").getOrNull())
                    teamID.set(providers.environmentVariable("JERVIS_MACOS_NOTARIZATION_TEAM_ID").getOrNull())
                }
            }
            windows {
                iconFile.set(rootProject.file("logo/logo.ico"))
                upgradeUuid = providers.environmentVariable("JERVIS_WINDOWS_PACKAGE_GUID").getOrNull()
                menuGroup = "Jervis Fantasy Football"
                dirChooser = true
                perUserInstall = true
                shortcut = true
            }
            linux {
                iconFile.set(rootProject.file("logo/logo.svg"))
                packageName = "jervis-ffb"
                debMaintainer = "christianmelchior@gmail.com"
                // appRelease = "1" - Not currently used. Need to figure out how to increment this if used
                appCategory = "Game"
                rpmLicenseType = "Apache-2.0"
                menuGroup = "Jervis Fantasy Football"
                shortcut = true
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

// BEGIN -- Dependencies and work-around required for Conveyor Support
// These top-level dependecies are being added to all targets, including iOS
// and WASM, which is problematic as it will break the build. For that reason,
// we need to filter them out.
dependencies {
    val composeVersion = libs.versions.compose.get()
    linuxAmd64("org.jetbrains.compose.desktop:desktop-jvm-linux-x64:$composeVersion")
    macAmd64("org.jetbrains.compose.desktop:desktop-jvm-macos-x64:$composeVersion")
    macAarch64("org.jetbrains.compose.desktop:desktop-jvm-macos-arm64:$composeVersion")
    windowsAmd64("org.jetbrains.compose.desktop:desktop-jvm-windows-x64:$composeVersion")
}

val nonDesktopTargets = listOf("ios", "wasm")
configurations.matching { conf ->
    nonDesktopTargets.any {
        conf.name.contains(it, ignoreCase = true)
    }
}.configureEach {
    exclude(group = "org.jetbrains.compose.desktop")
}
// -- END
