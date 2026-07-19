/**
 * Aggregator build script for the Jervis Engine.
 *
 * It exposes a number of test tasks, making it easier to test the entire engine
 * using tasks like `./gradlew :modules:jervis-engine:jvmTest`.
 */
listOf(
    "jvmTest",
    "iosSimulatorArm64Test",
    "wasmJsTest",
    "allTests",
).forEach { testTask ->
    tasks.register(testTask) {
        group = "verification"
        description = "Runs '$testTask' for every Jervis engine submodule."
        dependsOn(subprojects.map { "${it.path}:$testTask" })
    }
}
