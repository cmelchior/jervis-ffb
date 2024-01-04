plugins {
    alias(libs.plugins.multiplatform)
    alias(libs.plugins.serialization)
}

group = "dk.ilios.jervis"
version = "fumbbl-net"

repositories {
    mavenCentral()
    google()
}

kotlin {

    jvm {
        jvmToolchain(17)
        testRuns["test"].executionTask.configure {
            useJUnitPlatform()
        }
    }

    sourceSets {
        val commonMain by getting {
            dependencies {
                implementation(libs.coroutines)
                implementation("io.ktor:ktor-client-core:${libs.versions.ktor.get()}")
                implementation("io.ktor:ktor-client-logging:2.3.7")
                implementation("io.ktor:ktor-client-websockets:2.3.7")
                implementation("io.ktor:ktor-serialization-kotlinx-json:2.3.7")
                implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.5.1")
            }
        }
        val commonTest by getting {
            dependencies {
                implementation(kotlin("test"))
            }
        }
        val jvmMain by getting {
            dependencies {
                implementation("io.ktor:ktor-client-okhttp:2.3.7")
            }
        }
        val jvmTest by getting
    }
}
