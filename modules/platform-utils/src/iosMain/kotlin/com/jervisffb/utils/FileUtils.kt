@file:Suppress("EXPECT_ACTUAL_CLASSIFIERS_ARE_IN_BETA_WARNING")

package com.jervisffb.utils

import kotlinx.coroutines.flow.first
import okio.FileSystem
import okio.Path
import okio.Path.Companion.toPath
import okio.buffer
import okio.use
import platform.Foundation.NSDocumentDirectory
import platform.Foundation.NSSearchPathForDirectoriesInDomains
import platform.Foundation.NSUserDomainMask

// TODO We are mostly using this for replay files. Probably this can be hidden behind some kind of better
//  interface
actual val platformFileSystem: FileSystem = FileSystem.SYSTEM
actual val APPLICATION_DIRECTORY: String by lazy {
    NSSearchPathForDirectoriesInDomains(
        NSDocumentDirectory,
        NSUserDomainMask,
        true
    ).first() as String
}

actual class FileManager {

    actual suspend fun getFilesWithExtension(directory: String, extension: String): List<Path> {
        val dirPath = "$APPLICATION_DIRECTORY/$directory".toPath()
        return platformFileSystem.listOrNull(dirPath)
            ?.filter { it.name.endsWith(extension) }
            ?.map { it.toString().substringAfter("$APPLICATION_DIRECTORY/") }
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
