plugins {
    alias(libs.plugins.jvm)
    id("com.gradleup.shadow")
    application
}

group = "com.jervisffb"
@Suppress("UNCHECKED_CAST")
version = (rootProject.ext["mavenVersion"] as Provider<String>).get()

repositories {
    mavenCentral()
}

dependencies {
    implementation(project(":modules:platform-utils"))
    implementation(project(":modules:jervis-engine:package"))
    implementation(project(":modules:jervis-test-utils"))
    implementation(libs.clikt)
    implementation(libs.coroutines)
    implementation(libs.kermit)
}

application {
    mainClass.set("com.jervisffb.fuzzer.cli.MainCliKt")
}

// Match `fumbbl-cli`'s output layout so the root `copyFuzzerCliJar` task can find the fat jar
// at a stable path. The Ktor plugin already does this for `fumbbl-cli`; we do it explicitly here.
tasks.shadowJar {
    archiveVersion.set("")
}

kotlin {
    jvmToolchain((project.properties["java.version"] as String).toInt())
}
