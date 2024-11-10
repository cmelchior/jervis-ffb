plugins {
    alias(libs.plugins.multiplatform)
    alias(libs.plugins.serialization)
    id("application")
}

group = "com.jervisffb"
version = rootProject.ext["mavenVersion"] as String

repositories {
    mavenCentral()
}

application {
    mainClass.set("com.jervisffb.analyzer.MainKt")
}

kotlin {
    jvmToolchain((project.properties["java.version"] as String).toInt())
    jvm {
        withJava()
        testRuns["test"].executionTask.configure {
            useJUnitPlatform()
        }
    }

    sourceSets {
        val commonMain by getting {
            dependencies {
                implementation(project(":modules:platform-utils"))
                implementation(libs.coroutines)
                implementation(project(":modules:fumbbl-net"))
                implementation(project(":modules:jervis-engine"))
            }
        }
        val commonTest by getting {
            dependencies {
                implementation(kotlin("test"))
            }
        }
        val jvmMain by getting {
            dependencies {
                implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.5.1")
//    implementation(project(mapOf("path" to ":game-model")))
//                testImplementation("org.junit.jupiter:junit-jupiter-api:5.8.1")
//                testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:5.8.1")
            }
        }
        val jvmTest by getting
    }
}
