@file:OptIn(ExperimentalWasmDsl::class)

import org.jetbrains.kotlin.gradle.ExperimentalWasmDsl
import org.jetbrains.kotlin.gradle.targets.js.webpack.KotlinWebpackConfig

plugins {
    alias(libs.plugins.multiplatform)
    alias(libs.plugins.jetbrainsCompose)
    alias(libs.plugins.composeCompiler)
}

kotlin {
    compilerOptions {
        freeCompilerArgs.add("-Xexplicit-backing-fields")
    }
    jvmToolchain((project.properties["java.version"] as String).toInt())
    wasmJs {
        outputModuleName.set("protoApp")
        browser {
            val projectDirPath = project.projectDir.path
            commonWebpackConfig {
                outputFileName = "protoApp.js"
                devServer = (devServer ?: KotlinWebpackConfig.DevServer()).apply {
                    static(projectDirPath)
                }
            }
        }
        binaries.executable()
    }

    sourceSets {
        val wasmJsMain by getting {
            dependencies {
                implementation(project(":modules:jervis-ui:shared"))
                implementation(project(":modules:jervis-test-utils"))
            }
        }
    }
}
