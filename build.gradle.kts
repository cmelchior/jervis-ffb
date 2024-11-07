import org.jlleitschuh.gradle.ktlint.reporter.ReporterType

plugins {
    alias(libs.plugins.jvm) apply false
    alias(libs.plugins.multiplatform) apply false
    alias(libs.plugins.serialization) apply false
    id("org.jlleitschuh.gradle.ktlint") version "12.1.1" apply false
}

enum class ReleaseType {
    SNAPSHOT, DEV, PROD
}

val releaseType = when(properties["jervis.releaseType"]) {
    "snapshot" -> ReleaseType.SNAPSHOT
    "dev" -> ReleaseType.DEV
    "prod" -> ReleaseType.PROD
    else -> ReleaseType.SNAPSHOT
}

val gitHash: String by lazy {
    Runtime.getRuntime().exec(arrayOf("git", "rev-parse", "--short",  "HEAD"))
        .inputStream
        .bufferedReader()
        .use { it.readText().trim() }
}

// Create Maven version
private fun createMavenVersion(): String {
    val versionStr = properties["jervis.version"] as String
    return when (releaseType) {
        ReleaseType.SNAPSHOT -> "$versionStr-SNAPSHOT"
        ReleaseType.DEV -> "$versionStr-dev-$gitHash"
        ReleaseType.PROD -> versionStr
    }
}

// Create Public version (as visible inside the app)
private fun createProjectVersion(): String {
    val versionStr = properties["jervis.version"] as String
    return when (releaseType) {
        ReleaseType.SNAPSHOT -> "$versionStr.dev.local"
        ReleaseType.DEV -> "$versionStr.dev.$gitHash"
        ReleaseType.PROD -> versionStr
    }
}

// Create version used when creating distribution packages.
// See https://github.com/JetBrains/compose-multiplatform/blob/master/tutorials/Native_distributions_and_local_execution/README.md
// for restrictions on these.
private fun createDistributionVersion(): String {
    val versionStr = properties["jervis.version"] as String
    return if (versionStr.startsWith("0.")) {
        "1.0.0"
    } else {
        versionStr
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

subprojects {
    apply(plugin = "org.jlleitschuh.gradle.ktlint")
    configure<org.jlleitschuh.gradle.ktlint.KtlintExtension> {
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

val subprojects = listOf(
    "modules/fumbbl-cli",
    "modules/fumbbl-net",
    "modules/jervis-engine",
    "modules/jervis-ui",
    "modules/replay-analyzer",
)

fun taskName(subdir: String): String {
    return subdir.split("/", "-").map { it.capitalize() }.joinToString(separator = "")
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

// Internal task that will flatten the fumbble resource directory and move the
// resulting files to a new temporary location
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
            //   This is player icons/images and are loaded dynamically
            when {
                this.relativePath.startsWith("sounds/") -> {
                    this.path = "files/${this.path}"
                }
                this.relativePath.startsWith("icons/cached") -> {
                    this.path = "files/${this.path}"
                }
                else -> {
                    // Names are required to be flattened in order to generate accessors for them
                    val newFileName = this.relativePath.segments.joinToString("_")
                    this.path = "drawable/$newFileName"
                }
            }
        }
    }

    // Make the task fail if the source directory does not exist
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
    dependsOn("flattenFFBResources") // Make sure this runs after flattenFolder

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
