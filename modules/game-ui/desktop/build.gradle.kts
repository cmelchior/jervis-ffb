import org.jetbrains.compose.desktop.application.dsl.TargetFormat
import org.jetbrains.kotlin.builtins.StandardNames.FqNames.target

plugins {
    alias(libs.plugins.multiplatform)
    alias(libs.plugins.compose)
}

group = "dk.ilios.bloodbowl.ui"
version = "1.0-SNAPSHOT"
kotlin {
    jvm {
        jvmToolchain((project.properties["java.version"] as String).toInt())
    }
    sourceSets {
        val commonMain by getting {
            dependencies {
                implementation(project(":modules:game-ui:common"))
                implementation(project(":modules:game-model"))
                implementation(project(":modules:fumbbl-net"))
                implementation("com.squareup.okio:okio:3.7.0")
                implementation(libs.coroutines)
                implementation(compose.desktop.common)
            }
        }
        val jvmMain by getting {
            dependencies {
                implementation(compose.desktop.currentOs)
//                implementation(compose.desktop.macos_arm64)
//                implementation("org.jetbrains.skiko:skiko-awt-runtime-macos-arm64:+")
                api(compose.preview)
                @OptIn(org.jetbrains.compose.ExperimentalComposeLibrary::class)
                implementation(compose.desktop.uiTestJUnit4)
            }
        }
        val jvmTest by getting
    }
}


//kotlin {
//    jvm {
//        jvmToolchain(17)
//        withJava()
//    }
//    sourceSets {
//        val commonMain by getting {
//            dependencies {
//                implementation(project(":game-ui:common"))
//                implementation(compose.desktop.common)
//            }
//        }
//        val jvmMain by getting {
//            dependencies {
////                implementation(compose.desktop.currentOs)
//                implementation(compose.desktop.macos_arm64)
//                api(compose.preview)
//                @OptIn(org.jetbrains.compose.ExperimentalComposeLibrary::class)
//                implementation(compose.uiTestJUnit4)
//            }
//        }
//        val jvmTest by getting {
//            dependencies {
//            }
//        }
//    }
//}

compose.desktop {
    application {
        mainClass = "MainKt"
        nativeDistributions {
            targetFormats(TargetFormat.Dmg)
//            targetFormats(TargetFormat.Dmg, TargetFormat.Msi, TargetFormat.Deb)
            packageName = "game-ui"
            packageVersion = "1.0.0"
        }
    }
}
