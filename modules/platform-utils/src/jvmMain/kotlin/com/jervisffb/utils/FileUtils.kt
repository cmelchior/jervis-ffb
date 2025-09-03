@file:Suppress("EXPECT_ACTUAL_CLASSIFIERS_ARE_IN_BETA_WARNING")

package com.jervisffb.utils

import okio.FileSystem
import okio.Path
import okio.Path.Companion.toPath
import okio.buffer
import okio.use
import java.io.File

actual val platformFileSystem: FileSystem = FileSystem.SYSTEM
actual val APPLICATION_DIRECTORY: String = "${System.getProperty("user.home")}${File.separator}.jervis"

actual class FileManager {

    actual suspend fun getFilesWithExtension(directory: String, extension: String): List<Path> {
        val dirPath = "$APPLICATION_DIRECTORY/$directory".toPath()
        return platformFileSystem.listOrNull(dirPath)
            ?.filter { it.name.endsWith(extension) }
            ?.map { it.toString().substringAfter("$APPLICATION_DIRECTORY${File.separator}") }
            ?.map { it.toPath() }
            ?: emptyList()
    }

    actual suspend fun getFile(path: String): ByteArray? {
        val filePath = "$APPLICATION_DIRECTORY/$path".toPath()
        return if (platformFileSystem.exists(filePath)) {
            platformFileSystem.source(filePath).use { source ->
                source.buffer().readByteArray()
            }
        } else {
            null
        }
    }

    actual suspend fun writeFile(dir: String, fileName: String, fileContent: ByteArray) {
        platformFileSystem.createDirectories("$APPLICATION_DIRECTORY/$dir".toPath())
        platformFileSystem.write("$APPLICATION_DIRECTORY/$dir/$fileName".toPath()) {
            write(fileContent)
        }
    }
}
