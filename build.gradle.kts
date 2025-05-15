import org.jlleitschuh.gradle.ktlint.KtlintExtension
import org.jlleitschuh.gradle.ktlint.reporter.ReporterType

plugins {
    // this is necessary to avoid the plugins to be loaded multiple times
    // in each subproject's classloader
    alias(libs.plugins.composeCompiler) apply false
    alias(libs.plugins.jetbrainsCompose) apply false
    alias(libs.plugins.jvm) apply false
    alias(libs.plugins.ktor) apply false
    alias(libs.plugins.multiplatform) apply false
    alias(libs.plugins.serialization) apply false
    id("org.jlleitschuh.gradle.ktlint") version "12.2.0"
}

allprojects {
    repositories {
        mavenCentral()
        google()
        maven {
            url = uri("${rootProject.projectDir}/mavenRepo")
        }
        mavenLocal()    }
}

enum class ReleaseType {
    SNAPSHOT, DEV, PROD
}

val releaseType = when (properties["jervis.releaseType"]) {
    "snapshot" -> ReleaseType.SNAPSHOT
    "dev" -> ReleaseType.DEV
    "prod" -> ReleaseType.PROD
    else -> ReleaseType.SNAPSHOT
}

val gitHash: Provider<String> = providers.exec {
    commandLine("git", "rev-parse", "--short",  "HEAD")
}.standardOutput.asText.map { it.trim() }

val gitHashLong: Provider<String> = providers.exec {
    commandLine("git", "rev-parse",  "HEAD")
}.standardOutput.asText.map { it.trim() }

val gitHistory: Provider<String> = providers.exec {
    commandLine("git", "--no-pager", "log", "-5", "--pretty=format:%at:%s")
}.standardOutput.asText.map { it.trim() }

// Create Maven version
private fun createMavenVersion(): Provider<String> {
    val versionStr = providers.gradleProperty("jervis.version")
    return when (releaseType) {
        ReleaseType.SNAPSHOT -> versionStr.map { "$it-SNAPSHOT" }
        ReleaseType.DEV -> {
            gitHash.zip(versionStr) { gitHash, version ->
                "$version-dev-$gitHash"
            }
        }
        ReleaseType.PROD -> versionStr
    }
}

// Create Public version (as visible inside the app)
private fun createProjectVersion(): Provider<String> {
    val versionStr = providers.gradleProperty("jervis.version")
    return when (releaseType) {
        ReleaseType.SNAPSHOT -> versionStr.map { "$it.dev.local" }
        ReleaseType.DEV -> {
            gitHash.zip(versionStr) { gitHash, version ->
                "$version.dev.$gitHash"
            }
        }
        ReleaseType.PROD -> versionStr
    }
}

// Create version used when creating distribution packages.
// See https://www.jetbrains.com/help/kotlin-multiplatform-dev/compose-native-distribution.html#specifying-distribution-properties
// for restrictions on these.
private fun createDistributionVersion(): Provider<String> {
    return providers.gradleProperty("jervis.version")
        .map {
            if (it.startsWith("0.")) {
                "1.0.0"
            } else {
                it
            }
        }
}

// Version number used for Maven Artifacts
rootProject.ext["mavenVersion"] = createMavenVersion()
// Version number used in the App
rootProject.ext["publicVersion"] = createProjectVersion()
// Used in Distribution packages (must be SemVer >= 1.0.0)
rootProject.ext["distributionVersion"] = createDistributionVersion()
// Current short git hash
rootProject.ext["gitHash"] = gitHash
rootProject.ext["gitHashLong"] = gitHashLong
// History of last 5 commits
rootProject.ext["gitHistory"] = gitHistory

subprojects {
    apply(plugin = "org.jlleitschuh.gradle.ktlint")
    configure<KtlintExtension> {
        version.set("1.5.0") // See https://github.com/pinterest/ktlint
        debug.set(true)
        verbose.set(true)
        filter {
            exclude("**/LZString.kt")
            exclude("**/package-info.kt")
        }
        reporters {
            reporter(ReporterType.PLAIN)
            reporter(ReporterType.CHECKSTYLE)
        }
    }
}

tasks.register<Copy>("copyClientDownloaderJar") {
    dependsOn(":modules:fumbbl-cli:shadowJar")
    from("${projectDir.absolutePath}/modules/fumbbl-cli/build/libs/fumbbl-cli-all.jar")
    into("${projectDir.absolutePath}/tools")
    rename { "fumbblcli.jar" }
}

tasks.register("buildTools") {
    group = "Publishing"
    description = "Build and copy all tools (Jars) into the tools/ folder."
    dependsOn("copyClientDownloaderJar")
}

// Internal task for cloning or updating the FFB repo
tasks.register<Exec>("cloneFFBRepo") {
    // Either clone the FFB codebase or update our clone if it was already cloned.
    val targetDir = File(layout.buildDirectory.get().asFile, "ffb-repo")
    if (!targetDir.exists()) {
        val repoUrl = "https://github.com/christerk/ffb"
        commandLine("git", "clone", repoUrl, targetDir.absolutePath)
    } else {
        workingDir = targetDir
        commandLine("git", "pull")
    }
    outputs.upToDateWhen { false }
}

// Internal tasks that will copy the FUMBBL `icons.ini` which contains the mapping between
// local paths and their download location.
tasks.register<Copy>("copyFFBIconsIni") {
    description = "Update FUMBBL icon mapping"
    group = "Jervis Tasks"
    dependsOn("cloneFFBRepo")
    val sourceFile = file("${layout.buildDirectory.get().asFile.absolutePath}/ffb-repo/ffb-client/src/main/resources-live/icons.ini")
    val targetDir = file("${layout.projectDirectory.asFile.absolutePath}/modules/jervis-ui/src/commonMain/composeResources/files/fumbbl")
    onlyIf {
        if (!sourceFile.exists()) {
            throw GradleException("Source file does not exist: ${sourceFile.absolutePath}")
        }
        true
    }
    from(sourceFile)
    into(targetDir)
}

// Internal task that will flatten the FUMBBL resource directory and move the
// relevant resulting files to a new temporary location (from where it can be
// processed further). We do not want to include player icons and portraits as
// their licensing status is unclear, so they can only be loaded at runtime.
tasks.register<Copy>("flattenFFBResources") {
    dependsOn("cloneFFBRepo")

    val sourceDir = File(layout.buildDirectory.get().asFile, "ffb-repo/ffb-resources/src/main/resources")
    val targetDir = File(layout.buildDirectory.get().asFile, "ffb-resources")

    from(sourceDir)
    into(targetDir)

    eachFile {
        // Ignore directories
        if (this.isDirectory) {
            this.exclude()
        }

        if (this.relativePath.segments.size > 1) {
            // TODO How to handle pitches? They are currently included as zip-files
            //  For now I have manually unzipped the default pitch and uses that.
            // We have 3 locations:
            // - drawable/: Images that need a static reference
            // - files/sounds: Sound files
            // - files/cached: All files under the "cached" folder.
            //   This also include player icons/portraits, but these must only be
            //   loaded at runtime so are excluded.
            when {
                this.relativePath.startsWith("sounds/") -> {
                    this.path = "files/fumbbl/${this.path}"
                }
                this.relativePath.startsWith("icons/cached") -> {
                    if (relativePath.startsWith("icons/cached/players") || relativePath.startsWith("icons/cached/pitches")) {
                        this.exclude()
                    } else {
                        this.path = "files/fumbbl/${this.path}"
                    }
                }
                else -> {
                    // Names are required to be flattened to generate accessors for them
                    val newFileName = this.relativePath.segments.joinToString("_")
                    this.path = "drawable/$newFileName"
                }
            }
        }
    }

    // Make sure the task fails if the source directory does not exist
    onlyIf {
        if (!sourceDir.exists()) {
            throw GradleException("Source directory does not exist: ${sourceDir.absolutePath}")
        }
        true
    }

    includeEmptyDirs = false
}

tasks.register<Copy>("updateFFBResources") {
    description = "Update Jervis UI with latest version of FFB resources"
    group = "Jervis Tasks"
    dependsOn("flattenFFBResources", "copyFFBIconsIni")

    val tempDir = file("${layout.buildDirectory.get().asFile.absolutePath}/ffb-resources")
    val targetDir = file("${layout.projectDirectory.asFile.absolutePath}/modules/jervis-ui/src/commonMain/composeResources")

    onlyIf {
        if (!tempDir.exists()) {
            throw GradleException("Source directory does not exist: ${tempDir.absolutePath}")
        }
        true
    }

    from(tempDir) {
        include("**/*") // Include all files
    }
    into(targetDir) // Move files into the final destination
}
