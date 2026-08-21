@file:OptIn(ExperimentalWasmDsl::class)

import org.jetbrains.kotlin.gradle.ExperimentalWasmDsl
import org.jetbrains.kotlin.gradle.targets.js.webpack.KotlinWebpackConfig

plugins {
    alias(libs.plugins.multiplatform)
    alias(libs.plugins.jetbrainsCompose)
    alias(libs.plugins.composeCompiler)
}

// Generate an `index.html` file with a reference to the current version (defined by the git commit)
// This way; we ensure the browser always loads the correct .wasm resource files. Before this, it
// was possible for the browser to use a cached version of `composeApp.js` that referred to older
// .wasm files that were no longer present. Which resulted in the page failing to load.

// Create Abstract Task to enable support with Gradle Configuration Cache
abstract class GenerateIndexHtmlTask : DefaultTask() {
    @get:InputFile
    abstract val inputFile: RegularFileProperty

    @get:Input
    abstract val gitHash: Property<String>

    @get:OutputDirectory
    abstract val outputDir: DirectoryProperty

    @TaskAction
    fun generate() {
        val content = inputFile.get().asFile.readText()
        val modified = content.replace("%composeAppRef%", "composeApp-${gitHash.get()}.js")
        val outFile = outputDir.file("index.html").get().asFile
        outFile.parentFile.mkdirs()
        outFile.writeText(modified)
    }
}
@Suppress("UNCHECKED_CAST")
val gitHashLong = rootProject.ext["gitHashLong"] as Provider<String>
val generateIndexHtml = tasks.register<GenerateIndexHtmlTask>("generateIndexHtml") {
    inputFile.set(layout.projectDirectory.file("src/wasmJsMain/template/index.html"))
    outputDir.set(layout.buildDirectory.dir("generated/wasmJs"))
    gitHash.set(gitHashLong)
}

kotlin {
    jvmToolchain((project.findProperty("java.version") as String).toInt())
    wasmJs {
        outputModuleName.set("jervis-ui")
        browser {
            val projectDirPath = project.projectDir.path
            val fileName = gitHashLong.map {"composeApp-$it.js" }.get()
            commonWebpackConfig {
                outputFileName = fileName
                devServer = (devServer ?: KotlinWebpackConfig.DevServer()).apply {
                    // Serve sources to debug inside the browser
                    static(projectDirPath)
                }
            }
        }
        binaries.executable()
    }

    sourceSets {
        val wasmJsMain = getByName("wasmJsMain") {
            resources.srcDir(generateIndexHtml.map { it.outputs.files.singleFile })
            dependencies {
                implementation(project(":modules:jervis-ui:shared"))
            }
        }
    }
}
