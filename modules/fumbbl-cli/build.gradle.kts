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
    implementation(project(":modules:fumbbl-net"))
    compileOnly("com.eclipsesource.minimal-json:minimal-json:0.9.5")
    implementation("com.github.ajalt.clikt:clikt:4.2.1")
    implementation("org.javassist:javassist:3.29.2-GA")
    implementation("io.ktor:ktor-client-okhttp:2.3.0")
    implementation("com.squareup.okhttp3:okhttp:4.10.0")
    implementation("com.squareup.moshi:moshi-kotlin:1.14.0")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.5.0")
    testImplementation("org.jetbrains.kotlin:kotlin-test-junit:1.8.21")
}

application {
    mainClass.set("dk.ilios.jervis.fumbblcli.MainCliKt")
}

kotlin {
    jvmToolchain(11)
}