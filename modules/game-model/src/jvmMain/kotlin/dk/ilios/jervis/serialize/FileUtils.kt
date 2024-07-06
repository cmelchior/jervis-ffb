package dk.ilios.jervis.serialize

import okio.FileSystem
import okio.Path

actual val platformFileSystem: FileSystem = FileSystem.SYSTEM

fun Path.readText(): String {
    return platformFileSystem.read(this) { readUtf8() }
}

fun Path.writeText(text: String) {
    platformFileSystem.write(this) { writeUtf8(text) }
}
