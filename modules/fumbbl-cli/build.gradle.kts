plugins {
    alias(libs.plugins.jvm)
    alias(libs.plugins.serialization)
    alias(libs.plugins.ktor)
    application
}

group = "dk.ilios.jervis"
version = "1.0.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    implementation(project(":modules:utils"))
    implementation(project(":modules:fumbbl-net"))
    implementation("com.eclipsesource.minimal-json:minimal-json:${libs.versions.minimalJson.get()}")
    implementation("com.github.ajalt.clikt:clikt:4.2.1")
    implementation("org.javassist:javassist:${libs.versions.javaAssist.get()}")
    implementation("io.ktor:ktor-client-okhttp:${libs.versions.ktor.get()}")
    implementation("com.squareup.okhttp3:okhttp:4.10.0")
    implementation("com.squareup.moshi:moshi-kotlin:1.14.0")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:${libs.versions.serialization.get()}")
    testImplementation("org.jetbrains.kotlin:kotlin-test-junit:${libs.versions.kotlin.get()}")
}

application {
    mainClass.set("com.jervisffb.fumbbl.netcli.MainCliKt")
}

kotlin {
    jvmToolchain((project.properties["java.version"] as String).toInt())
}
