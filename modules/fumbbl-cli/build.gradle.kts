plugins {
    alias(libs.plugins.jvm)
    alias(libs.plugins.serialization)
    alias(libs.plugins.ktor)
    application
}

group = "com.jervisffb"
version = rootProject.ext["mavenVersion"] as String

repositories {
    mavenCentral()
}

dependencies {
    implementation(project(":modules:platform-utils"))
    implementation(project(":modules:fumbbl-net"))
    implementation(libs.clikt)
    implementation(libs.minimalJson)
    implementation(libs.javaAssist)
    implementation(libs.ktor.client.okhttp)
    implementation(libs.okhttp3)
    implementation(libs.moshi.kotlin)
    implementation(libs.kotlinx.serialization.json)
    implementation(libs.kotlin.test.junit)
}

application {
    mainClass.set("com.jervisffb.fumbbl.netcli.MainCliKt")
}

kotlin {
    jvmToolchain((project.properties["java.version"] as String).toInt())
}
