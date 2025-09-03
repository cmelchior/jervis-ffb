@file:OptIn(
    ExperimentalWasmDsl::class,
    org.jetbrains.compose.ExperimentalComposeLibrary::class
)

import org.jetbrains.compose.desktop.application.dsl.TargetFormat
import org.jetbrains.compose.reload.gradle.ComposeHotRun
import org.jetbrains.kotlin.gradle.ExperimentalWasmDsl
import org.jetbrains.kotlin.gradle.targets.js.webpack.KotlinWebpackConfig
import java.nio.file.Files
import java.util.Properties

plugins {
    alias(libs.plugins.serialization)
    alias(libs.plugins.multiplatform)
    alias(libs.plugins.jetbrainsCompose)
    alias(libs.plugins.composeCompiler)
    alias(libs.plugins.composeHotReload)
}

group = "com.jervisffb"
@Suppress("UNCHECKED_CAST")
version = (rootProject.ext["mavenVersion"] as Provider<String>).get()

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
    jvmToolchain((project.properties["java.version"] as String).toInt())
    jvm()

    listOf(
        iosX64(),
        iosArm64(),
        iosSimulatorArm64()
    ).forEach { iosTarget ->
        iosTarget.binaries.framework {
            baseName = "JervisUI"
            freeCompilerArgs += "-Xbinary=bundleId=com.jervisffb"
            isStatic = true
        }
    }

    wasmJs {
        outputModuleName.set("jervis-ui")
        browser {
            val projectDirPath = project.projectDir.path
            val fileName = gitHashLong.map {"composeApp-$it.js" }.get()
            commonWebpackConfig {
                outputFileName = fileName
                devServer = (devServer ?: KotlinWebpackConfig.DevServer()).apply {
                    static = (static ?: mutableListOf()).apply {
                        // Serve sources to debug inside the browser
                        add(projectDirPath)
                    }
                }
            }
        }
        binaries.executable()
    }

    sourceSets {
        val commonMain by getting {
            dependencies {
                implementation(project(":modules:platform-utils"))
                implementation(project(":modules:fumbbl-net"))
                implementation(project(":modules:tourplay-net"))
                implementation(project(":modules:jervis-engine"))
                implementation(project(":modules:jervis-net"))
                implementation(project(":modules:jervis-resources"))
                implementation(libs.okio)
                implementation(libs.kotlinx.datetime)
                implementation(libs.coroutines)
                implementation(libs.bundles.voyager)
                implementation(libs.kotlinx.collections.immutable)
                implementation(libs.jsonserialization)
                implementation(compose.components.resources)
                implementation(compose.runtime)
                implementation(compose.foundation)
                implementation(compose.material3)
                implementation("com.adamglin:compose-shadow:2.0.4")
            }
        }
        val commonTest by getting {
            dependencies {
                implementation(project(":modules:jervis-test-utils"))
                implementation(kotlin("test"))
            }
        }
        val jvmMain by getting {
            dependencies {
                // Additional dependencies required for .ogg support
                // See https://github.com/finnkuusisto/TinySound/tree/master/lib
                implementation("kuusisto.tinysound:tinysound:1.1.1")
                implementation("javazoom.vorbisspi:vorbisspi:1.0.3")
                implementation("com.jcraft:jorbis:0.0.17")
                implementation("org.tritonus:tritonus_share:0.0.1")
                implementation(compose.components.uiToolingPreview)
                implementation(compose.desktop.currentOs)
                implementation(compose.desktop.uiTestJUnit4)
            }
        }
        val jvmTest by getting
        val wasmJsMain by getting {
            resources.srcDir(generateIndexHtml.map { it.outputs.files.singleFile })
        }
        val iosX64Main by getting
        val iosArm64Main by getting
        val iosSimulatorArm64Main by getting
        val iosMain by creating {
            dependsOn(commonMain)
            iosX64Main.dependsOn(this)
            iosArm64Main.dependsOn(this)
            iosSimulatorArm64Main.dependsOn(this)
            dependencies {
            }
        }
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

// ----
// Setup conversion of default-client-settings.ini to generated code
// ----

abstract class GenerateClientConfig : DefaultTask() {
    @get:InputFile
    @get:PathSensitive(PathSensitivity.RELATIVE)
    abstract val iniFile: RegularFileProperty

    @get:OutputDirectory
    abstract val outputDir: DirectoryProperty

    @get:Input
    abstract val packageName: Property<String>

    @TaskAction
    fun generate() {
        data class ConfigEntry(
            val codeKey: String,
            val stringKey: String,
            val value: String
        )

        val outRoot = outputDir.get().asFile
        val pkg = packageName.get()
        val outDir = outRoot.resolve(pkg.replace('.', '/')).apply { mkdirs() }
        val outFile = outDir.resolve("ClientConfig.kt")

        val props = Properties()
        props.load(iniFile.asFile.get().inputStream())

        // Parse INI - we only expect [a-zA-Z_.] characters since we control it. Ignore comments and empty lines.
        val entries = mutableListOf<ConfigEntry>()
        val regex = Regex("(?<=[a-z])(?=[A-Z])|(?<=[A-Z])(?=[A-Z][a-z])")
        for ((key, value) in props.entries) {
            var stringKey = key.toString()
            val codeKey = stringKey
                .split(".")
                .map {
                    it.replace(regex, "_")
                }
                .joinToString("_")
                .uppercase()
            val value = value.toString().trim()
            entries.add(ConfigEntry(codeKey, stringKey, value))
        }

        // Clean output dir to avoid stale constants
        outRoot.deleteRecursively()
        outDir.mkdirs()

        val content = buildString {
            appendLine("// AUTOGENERATED FILE. DO NOT EDIT.")
            appendLine("package $pkg")
            appendLine("import com.jervisffb.utils.SettingsManager")
            appendLine()

            // Create entries for all keys
            appendLine("object SettingsKeys {")
            for (entry in entries) {
                append("\tconst val ")
                    .append(entry.codeKey)
                    .append(" = ")
                    .append('"').append(entry.stringKey).append('"')
                    .appendLine()
            }
            appendLine("}")
            appendLine()

            // Create reset function
            appendLine("fun resetSettings(settings: SettingsManager) {")
            appendLine("\tsettings.clear()")
            for (entry in entries) {
                if (entry.value.isEmpty()) {
                    appendLine("\tsettings[SettingsKeys.${entry.codeKey}] = null")
                } else if (entry.value == "true" || entry.value == "false") {
                    appendLine("\tsettings[SettingsKeys.${entry.codeKey}] = ${entry.value}")
                } else if (entry.value.toIntOrNull() != null) {
                    appendLine("\tsettings[SettingsKeys.${entry.codeKey}] = ${entry.value}")
                } else {
                    appendLine("\tsettings[SettingsKeys.${entry.codeKey}] = \"${entry.value}\"")
                }
            }
            appendLine("}")
            appendLine()
            append(GameMenuParser.parse(props))
        }

        Files.writeString(outFile.toPath(), content)
    }
}

val generateClientConfig =
    tasks.register<GenerateClientConfig>("generateJervisClientConfig") {
        iniFile.set(layout.projectDirectory.file("src/commonMain/resources/default-client-settings.ini"))
        outputDir.set(layout.buildDirectory.dir("generated/jervis/commonMain/kotlin"))
        packageName.set("com.jervis.generated")
    }

kotlin.sourceSets.named("commonMain") {
    kotlin.srcDir(generateClientConfig.flatMap { it.outputDir })
}

// ----
// Code for parsing the menu structure from the ini file
// ----
object GameMenuParser {

    fun parse(props: Properties): String {

        // 1) Collect section labels from keys like: jervis.autoAction.label=Automatic Actions
        val sectionLabels = mutableMapOf<String, String>()
        props.forEach { (pKey, pValue) ->
            val key = pKey.toString()
            val value = pValue.toString()
            val parts = key.split('.')
            if (parts.last() == "sectionLabel") {
                // e.g., ["jervis","autoAction","label"] -> "jervis.autoAction"
                sectionLabels[parts.subList(0, parts.lastIndex).joinToString(".")] = value
            }
        }

        // 2) Collect items per section. Item keys look like:
        //    jervis.autoAction.doNotRerollSuccessfulActions.label
        //    jervis.autoAction.doNotRerollSuccessfulActions.value
        data class Tmp(var label: String? = null, var value: Boolean? = null)
        val sectionData = mutableMapOf<String, MutableMap<String, Tmp>>() // sectionId -> (fullItemKey -> Tmp)
        props.forEach { (pKey, pValue) ->
            val key = pKey.toString()
            val itemId = key.substringBeforeLast('.')
            val value = pValue.toString()

            // Check if line is a sub-item of already found section headers
            val sectionId = sectionLabels.keys.firstOrNull { sectionId ->
                itemId.startsWith("$sectionId.")
            }
            if (sectionId != null) {
                val sectionItems = sectionData.getOrPut(sectionId) { mutableMapOf() }
                val entryData = sectionItems.getOrPut(itemId) { Tmp() }
                when {
                    key.endsWith(".label") -> {
                        entryData.label = value
                    }
                    key.endsWith(".value") -> {
                        entryData.value = value.toBooleanStrictOrNull()
                    }
                }
            }
        }

        // 3) Build generated code
        return buildString {
            appendLine("""
                data class GameMenu(val sections: List<MenuSection>)
                data class MenuSection(
                    val id: String,
                    val label: String,
                    val items: List<ToggleItem>
                )
                data class ToggleItem(
                    val key: String,
                    val label: String,
                    val value: Boolean
                )
            """.trimIndent())
            appendLine("fun getGameSettingsMenu(): GameMenu {")
            appendLine("\tval sections = listOf(")
            sectionData.forEach { (sectionId, sectionItems) ->
                appendLine("\t\tMenuSection(")
                appendLine("\t\t\tid = \"${sectionId}\",")
                appendLine("\t\t\tlabel = \"${sectionLabels[sectionId]}\",")
                appendLine("\t\t\titems = listOf(")
                sectionItems.forEach { (fullItemKey, tmp) ->
                    appendLine("\t\t\t\tToggleItem(")
                    appendLine("\t\t\t\t\tkey = \"${fullItemKey}.value\",")
                    appendLine("\t\t\t\t\tlabel = \"${tmp.label}\",")
                    appendLine("\t\t\t\t\tvalue = ${tmp.value},")
                    appendLine("\t\t\t\t),")
                }
                appendLine("\t\t\t),")
                appendLine("\t\t),")
            }
            appendLine("\t)")
            appendLine("\treturn GameMenu(sections)")
            appendLine("}")
        }
    }
}
