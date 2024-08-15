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
        jvmToolchain((project.properties["java.version"] as String).toInt())
        testRuns["test"].executionTask.configure {
            useJUnitPlatform()
        }
    }

    val ktor = libs.versions.ktor.get()
    sourceSets {
        val commonMain by getting {
            dependencies {
                implementation(kotlin("reflect"))
                implementation(libs.coroutines)
                implementation(project(":modules:game-model"))
                implementation("com.squareup.okio:okio:3.7.0")
                implementation("io.ktor:ktor-client-core:$ktor")
                implementation("io.ktor:ktor-client-logging:$ktor")
                implementation("io.ktor:ktor-client-websockets:$ktor")
                implementation("io.ktor:ktor-serialization-kotlinx-json:$ktor")
                implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.5.1")
                implementation("org.reflections:reflections:0.10.2")
            }
        }
        val commonTest by getting {
            dependencies {
                implementation(kotlin("test"))
            }
        }
        val jvmMain by getting {
            dependencies {
                implementation("io.ktor:ktor-client-okhttp:$ktor")
            }
        }
        val jvmTest by getting
    }
}
