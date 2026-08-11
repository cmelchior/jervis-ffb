plugins {
    alias(libs.plugins.jvm)
    application
}

group = "com.jervisffb"
@Suppress("UNCHECKED_CAST")
version = (rootProject.ext["mavenVersion"] as Provider<String>).get()

repositories {
    mavenCentral()
}

dependencies {
    implementation(project(":modules:jervis-engine:package"))
    implementation(kotlin("reflect"))
    implementation("io.github.classgraph:classgraph:4.8.157")
}

application {
    mainClass.set("com.jervisffb.tools.serializers.GenerateSerializersKt")
}

kotlin {
    jvmToolchain((project.properties["java.version"] as String).toInt())
}

val serializerOutputDirectory = layout.buildDirectory.dir("generated/serializers")
val generateSerializers = tasks.register<JavaExec>("generateSerializers") {
    group = "generation"
    description = "Scans the packaged engine and generates serializer module sources."
    dependsOn(":modules:jervis-engine:package:jvmJar")
    classpath = sourceSets.main.get().runtimeClasspath
    mainClass.set(application.mainClass)
    args(serializerOutputDirectory.get().asFile.absolutePath)
}

val copyCoreSerializers = tasks.register<Copy>("copyCoreSerializers") {
    from(serializerOutputDirectory.map { it.file("coreSerializers.kt") })
    into(rootProject.layout.projectDirectory.dir(
        "modules/jervis-engine/core/src/commonMain/kotlin/com/jervisffb/engine/serialization"
    ))
}

val copyCommonSerializers = tasks.register<Copy>("copyCommonSerializers") {
    from(serializerOutputDirectory.map { it.file("commonSerializers.kt") })
    into(rootProject.layout.projectDirectory.dir(
        "modules/jervis-engine/rules-common/src/commonMain/kotlin/com/jervisffb/engine/common/serialization"
    ))
}

val copyBb2020Serializers = tasks.register<Copy>("copyBb2020Serializers") {
    from(serializerOutputDirectory.map { it.file("bb2020Serializers.kt") })
    into(rootProject.layout.projectDirectory.dir(
        "modules/jervis-engine/rules-bb2020/src/commonMain/kotlin/com/jervisffb/engine/bb2020/serialization"
    ))
}

val copyBb2025Serializers = tasks.register<Copy>("copyBb2025Serializers") {
    from(serializerOutputDirectory.map { it.file("bb2025Serializers.kt") })
    into(rootProject.layout.projectDirectory.dir(
        "modules/jervis-engine/rules-bb2025/src/commonMain/kotlin/com/jervisffb/engine/bb2025/serialization"
    ))
}

generateSerializers.configure {
    finalizedBy(copyCoreSerializers, copyCommonSerializers, copyBb2020Serializers, copyBb2025Serializers)
}

tasks.named("help") {
    group = "help"
    description = "Describes the available tools tasks."
    doLast {
        println(
            """
            Jervis tools
            ============

            generateSerializers
                Scan :modules:jervis-engine:package and update serializer sources.
                A combined report is written to tools/build/generated/serializers.

            """.trimIndent()
        )
    }
}
