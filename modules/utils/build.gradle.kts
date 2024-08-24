plugins {
    alias(libs.plugins.multiplatform)
}

group = "dk.ilios.jervis.utils"
version = "1.0-SNAPSHOT"

kotlin {
    jvmToolchain((project.properties["java.version"] as String).toInt())
    jvm()
    wasmJs {
        moduleName = "utils"
        browser()
    }

    sourceSets {
        val commonMain by getting {
            dependencies {
                implementation(libs.coroutines)
                implementation(libs.okio)
                implementation(libs.okio.fake)
            }
        }
    }
}

