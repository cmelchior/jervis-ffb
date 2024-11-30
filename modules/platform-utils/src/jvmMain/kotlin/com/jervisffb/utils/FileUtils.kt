package com.jervisffb.utils

import okio.FileSystem
import okio.Path
import okio.Path.Companion.toPath

actual val platformFileSystem: FileSystem = FileSystem.SYSTEM

actual class FileManager {

    val fileRoot = "/Users/christian.melchior/.jervis"

    actual suspend fun getFilesWithExtension(directory: String, extension: String): List<Path> {
        val dirPath = "$fileRoot/$directory".toPath()
        return platformFileSystem.listOrNull(dirPath)
            ?.filter { it.name.endsWith(extension) }
            ?: emptyList()
    }

    actual suspend fun getFile(path: String): Path? {
        val filePath = "$fileRoot/$path".toPath()
        return if (platformFileSystem.exists(filePath)) {
            filePath
        } else {
            null
        }
    }

    actual suspend fun writeFile(dir: String, fileName: String, fileContent: ByteArray) {
        platformFileSystem.createDirectories("$fileRoot/$dir".toPath())
        platformFileSystem.write("$fileRoot/$dir/$fileName".toPath()) {
            write(fileContent)
        }
    }
}

