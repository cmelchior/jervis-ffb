import org.jetbrains.compose.desktop.application.dsl.TargetFormat

plugins {
    alias(libs.plugins.multiplatform)
    alias(libs.plugins.compose)
}

group = "dk.ilios.bloodbowl.ui"
version = "1.0-SNAPSHOT"

kotlin {
    jvm {
        jvmToolchain(17)
        withJava()
    }
    sourceSets {
        val commonMain by getting {
            dependencies {
                implementation(project(":game-ui:common"))
                implementation(compose.desktop.common)
            }
        }
        val jvmMain by getting {
            dependencies {
                println(compose.desktop.currentOs)
//                implementation(compose.desktop.currentOs)
                implementation(compose.desktop.macos_arm64)
                api(compose.preview)
                @OptIn(org.jetbrains.compose.ExperimentalComposeLibrary::class)
                implementation(compose.uiTestJUnit4)
            }
        }
        val jvmTest by getting {
            dependencies {
            }
        }
    }
}

compose.desktop {
    application {
        mainClass = "MainKt"
        nativeDistributions {
            targetFormats(TargetFormat.Dmg, TargetFormat.Msi, TargetFormat.Deb)
            packageName = "game-ui"
            packageVersion = "1.0.0"
        }
    }
}
