plugins {
    alias(libs.plugins.jvm) apply false
    alias(libs.plugins.multiplatform) apply false
    alias(libs.plugins.serialization) apply false
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

