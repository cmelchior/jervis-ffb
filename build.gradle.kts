import org.jlleitschuh.gradle.ktlint.reporter.ReporterType

plugins {
    alias(libs.plugins.jvm) apply false
    alias(libs.plugins.multiplatform) apply false
    alias(libs.plugins.serialization) apply false
    id("org.jlleitschuh.gradle.ktlint") version "12.1.1" apply false
}

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
    "modules/game-model",
    "modules/game-ui",
    "modules/replay-analyzer",
)

fun taskName(subdir: String): String {
    return subdir.split("/", "-").map { it.capitalize() }.joinToString(separator = "")
}

//tasks {
//
//    register("ktlintCheck") {
//        description = "Runs ktlintCheck on all projects."
//        group = "Verification"
//        dependsOn(subprojects.map { "ktlintCheck${taskName(it)}" })
//    }
//
//    register("ktlintFormat") {
//        description = "Runs ktlintFormat on all projects."
//        group = "Formatting"
//        dependsOn(subprojects.map { "ktlintFormat${taskName(it)}" })
//    }
//
//    subprojects.forEach { subdir ->
//        register<Exec>("ktlintCheck${taskName(subdir)}") {
//            description = "Run ktlintCheck on /$subdir project"
//            workingDir = file("${rootDir}/$subdir")
//            commandLine = listOf("./gradlew", "ktlintCheck")
//        }
//
//        register<Exec>("ktlintFormat${taskName(subdir)}") {
//            description = "Run ktlintFormat on /$subdir project"
//            workingDir = file("${rootDir}/$subdir")
//            commandLine = listOf("./gradlew", "ktlintFormat")
//        }
//    }
//}
