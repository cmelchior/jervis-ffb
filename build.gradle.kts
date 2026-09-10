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
    alias(libs.plugins.conveyor) apply false
    alias(libs.plugins.ktlint)
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

val gitCommitCount: Provider<String> = System.getenv("JERVIS_COMMIT_COUNT")
    ?.let { providers.provider { it } }
    ?: providers.exec {
        // We only use for release versions, and since we only do releases from
        // `main` we just use that branch directly.
        commandLine("git", "rev-list", "--count", "origin/main")
    }.standardOutput.asText.map { it.trim() }

val gitHashLong: Provider<String> = providers.exec {
    commandLine("git", "rev-parse",  "HEAD")
}.standardOutput.asText.map { it.trim() }

val gitHistory: Provider<String> = providers.exec {
    commandLine("git", "--no-pager", "log", "-5", "--pretty=format:%at:%s")
}.standardOutput.asText.map { it.trim() }


private fun createVersionStr(): Provider<String> {
    val majorVersion = providers.gradleProperty("jervis.version.major")
    val minorVersion = providers.gradleProperty("jervis.version.minor")
    val patchVersion = gitCommitCount
    return majorVersion
        .zip(minorVersion) { major, minor -> "$major.$minor" }
        .zip(patchVersion) { majorMinor, patch -> "$majorMinor.$patch" }
}

// Create Maven version
private fun createMavenVersion(): Provider<String> {
    val versionStr = createVersionStr()
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
    val versionStr = createVersionStr()
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

// Create version used when creating distribution packages. These versions
// can only be numbers and dots.
//
// See https://conveyor.hydraulic.dev/22.0/configs/names/#appversion
// See https://www.jetbrains.com/help/kotlin-multiplatform-dev/compose-native-distribution.html#specifying-distribution-properties
private fun createDistributionVersion(): Provider<String> {
    val versionStr = createVersionStr()
    return versionStr
        .map {
            // Dmg installers requires major > 0. For now, we hack all versions below 0.x
            // to be 1.0.
            if (it.startsWith("0.1.")) {
                it.replaceFirst("0.1.", "1.0.")
            } else {
                it
            }
        }
}

// Current short git hash
rootProject.ext["gitHash"] = gitHash
rootProject.ext["gitHashLong"] = gitHashLong
// Number of commits (we use this as the patch version)
rootProject.ext["gitCommitCount"] = gitCommitCount
// History of last 5 commits
rootProject.ext["gitHistory"] = gitHistory

// Version number used for Maven Artifacts
rootProject.ext["mavenVersion"] = createMavenVersion()
// Version number used in the App
rootProject.ext["publicVersion"] = createProjectVersion()
// Used in Distribution packages (must be SemVer >= 1.0.0)
rootProject.ext["distributionVersion"] = createDistributionVersion()

subprojects {
    apply(plugin = "org.jlleitschuh.gradle.ktlint")
    configure<KtlintExtension> {
        version.set("1.5.0") // See https://github.com/pinterest/ktlint
        debug.set(true)
        verbose.set(true)
        filter {
            exclude("**/LZString.kt")
            exclude("**/package-info.kt")
            exclude { it.file.absolutePath.contains("/build/generated/") }
        }
        reporters {
            reporter(ReporterType.PLAIN)
            reporter(ReporterType.CHECKSTYLE)
        }
    }
}

tasks.register<Exec>("createConveyorDevInstallers") {
    @Suppress("UNCHECKED_CAST")
    val version = (rootProject.ext["distributionVersion"] as Provider<String>).get()
    environment("JERVIS_APP_VERSION", version)
    commandLine("conveyor", "-f", "conveyor.dev.conf", "make", "site")
}

tasks.register("printDistributionVersion") {
    @Suppress("UNCHECKED_CAST")
    val version = rootProject.ext["distributionVersion"] as Provider<String>
    doLast {
        println(version.get())
    }
}

tasks.register<Copy>("copyClientDownloaderJar") {
    dependsOn(":modules:fumbbl-cli:shadowJar")
    from("${projectDir.absolutePath}/modules/fumbbl-cli/build/libs/fumbbl-cli-all.jar")
    into("${projectDir.absolutePath}/tools")
    rename { "fumbblcli.jar" }
}

tasks.register<Copy>("copyFuzzerCliJar") {
    dependsOn(":modules:fuzzer-cli:shadowJar")
    from("${projectDir.absolutePath}/modules/fuzzer-cli/build/libs/fuzzer-cli-all.jar")
    into("${projectDir.absolutePath}/tools")
    rename { "fuzzercli.jar" }
}

tasks.register("buildTools") {
    group = "Publishing"
    description = "Build and copy all tools (Jars) into the tools/ folder."
    dependsOn("copyClientDownloaderJar")
    dependsOn("copyFuzzerCliJar")
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
    val targetDir = file("${layout.projectDirectory.asFile.absolutePath}/modules/jervis-ui/shared/src/commonMain/composeResources/files/fumbbl")
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
    val targetDir = file("${layout.projectDirectory.asFile.absolutePath}/modules/jervis-ui/shared/src/commonMain/composeResources")

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

// The TourPlay icon mapping is keyed on the same FUMBBL URLs as
// `files/fumbbl/icons.ini`, so the files silently drift apart whenever FUMBBL
// adds, replaces or removes an icon. This task guards against that by requiring
// every FUMBBL icon to be listed in the TourPlay file for its kind - either
// mapped to a TourPlay position or left with an empty value to mark it as
// xdeliberately unmapped.
tasks.register("checkTourPlayIcons") {
    description = "Verify that the TourPlay icon mapping covers every FUMBBL player icon"
    group = "verification"

    val resourceDir = layout.projectDirectory.dir("modules/jervis-ui/shared/src/commonMain/composeResources/files")
    val fumbblIcons = resourceDir.file("fumbbl/icons.ini").asFile
    val fumbblExtraIcons = resourceDir.file("fumbbl/icons-extra.ini").asFile
    val tourPlayIcons = mapOf(
        "iconsets" to resourceDir.file("tourplay/icons-iconsets.ini").asFile,
        "portraits" to resourceDir.file("tourplay/icons-portraits.ini").asFile,
    )
    inputs.files(fumbblIcons, fumbblExtraIcons, *tourPlayIcons.values.toTypedArray())

    doLast {
        fun readEntries(file: File): List<Pair<String, String>> {
            if (!file.exists()) {
                throw GradleException("Icon mapping file does not exist: ${file.absolutePath}")
            }
            return file.readLines()
                .map { it.trim() }
                .filter { it.isNotEmpty() && !it.startsWith("#") }
                .map { line ->
                    val separator = line.indexOf('=')
                    if (separator == -1) {
                        throw GradleException("Malformed entry in ${file.name}, expected `url=value`: $line")
                    }
                    line.substring(0, separator) to line.substring(separator + 1)
                }
        }

        // Must match how `TourPlayIconMapping` builds its lookup keys.
        fun normalize(name: String): String = name.filter { it.isLetterOrDigit() }.lowercase()
        fun keyOf(entry: String): String =
            entry.split("/", limit = 2).joinToString("/") { normalize(it) }

        val fumbblPaths: Map<String, String> = (readEntries(fumbblIcons) + readEntries(fumbblExtraIcons)).toMap()
        val errors = mutableListOf<String>()
        var total = 0
        var mapped = 0

        tourPlayIcons.forEach { (kind, file) ->
            val prefix = "players/$kind/"
            val entries = readEntries(file)
            total += entries.size
            mapped += entries.count { it.second.isNotBlank() }

            val duplicates = entries.groupBy { it.first }.filterValues { it.size > 1 }.keys
            if (duplicates.isNotEmpty()) {
                errors += "${duplicates.size} URL(s) listed more than once in ${file.name}:\n" +
                    duplicates.sorted().joinToString("\n") { "  $it" }
            }

            val listed = entries.mapTo(mutableSetOf()) { it.first }
            val expected = fumbblPaths.filterValues { it.startsWith(prefix) }
            val missing = expected.keys.filterNot { it in listed }
            if (missing.isNotEmpty()) {
                errors += "${missing.size} FUMBBL $kind have no entry in ${file.name}. Add a line for " +
                    "each, mapping it to a TourPlay position or leaving the value empty if it has none:\n" +
                    missing.sortedBy { expected.getValue(it) }
                        .joinToString("\n") { "  $it=  # ${expected.getValue(it)}" }
            }

            // Entries for the other kind belong in the sibling file, not this one.
            val wrongKind = entries.map { it.first }.distinct().filterNot { it in expected }
            if (wrongKind.isNotEmpty()) {
                errors += "${wrongKind.size} entry/entries in ${file.name} are not a FUMBBL " +
                    "`$prefix` image and should be removed or moved to the sibling file:\n" +
                    wrongKind.sorted().joinToString("\n") { "  $it  # ${fumbblPaths[it] ?: "unknown to FUMBBL"}" }
            }

            // A TourPlay position can only render one image per kind, so two FUMBBL icons
            // claiming it means one of them is silently ignored at runtime.
            val claims = mutableMapOf<String, MutableSet<String>>()
            entries.forEach { (url, positions) ->
                val path = fumbblPaths[url] ?: return@forEach
                positions.split(",")
                    .map { it.trim() }
                    .filter { it.isNotEmpty() }
                    .forEach { position -> claims.getOrPut(keyOf(position)) { mutableSetOf() }.add(path) }
            }
            val conflicts = claims.filterValues { it.size > 1 }
            if (conflicts.isNotEmpty()) {
                errors += "${conflicts.size} TourPlay position(s) are claimed by more than one FUMBBL $kind:\n" +
                    conflicts.entries.sortedBy { it.key }
                        .joinToString("\n") { (key, paths) -> "  $key: ${paths.sorted().joinToString(", ")}" }
            }
        }

        if (errors.isNotEmpty()) {
            throw GradleException("The TourPlay icon mapping is out of sync with the FUMBBL icon files.\n\n" +
                errors.joinToString("\n\n"))
        }

        logger.lifecycle(
            "TourPlay icon mapping is in sync with the FUMBBL icon files: " +
                "$total images, $mapped mapped to a TourPlay position."
        )
    }
}
