import org.jetbrains.compose.compose

plugins {
    alias(libs.plugins.multiplatform)
    alias(libs.plugins.compose)
//    id("com.android.library")
}

group = "dk.ilios.bloodbowl.ui"
version = "1.0-SNAPSHOT"

kotlin {
    jvm("desktop") {
        jvmToolchain((project.properties["java.version"] as String).toInt())
    }
    sourceSets {
        val commonMain by getting {
            dependencies {
                implementation(project(":modules:fumbbl-net"))
                implementation(project(":modules:game-model"))
                implementation("com.squareup.okio:okio:3.7.0")
                api(compose.runtime)
                api(compose.foundation)
                api(compose.material)
                implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.8.0")
                val voyagerVersion = "1.0.0"
                implementation("cafe.adriel.voyager:voyager-navigator:$voyagerVersion")
                implementation("cafe.adriel.voyager:voyager-screenmodel:$voyagerVersion")
                implementation("cafe.adriel.voyager:voyager-bottom-sheet-navigator:$voyagerVersion")
                implementation("cafe.adriel.voyager:voyager-tab-navigator:$voyagerVersion")
                implementation("cafe.adriel.voyager:voyager-transitions:$voyagerVersion")
            }
        }
        val commonTest by getting {
            dependencies {
                implementation(kotlin("test"))
            }
        }
        val desktopMain by getting {
            dependencies {
                api(compose.preview)
            }
        }
        val desktopTest by getting
    }
}

//android {
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
//}