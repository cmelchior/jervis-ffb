package com.jervisffb.utils

import okio.FileSystem
import okio.Path

expect val platformFileSystem: FileSystem

/**
 * Low-level API for accessing files under the ~/.jervis` cache folder.
 * (or however it is represented on the system)
 */
expect class FileManager() {
    suspend fun getFilesWithExtension(directory: String, extension: String): List<Path>
    suspend fun getFile(path: String): Path?
    suspend fun writeFile(dir: String, fileName: String, fileContent: ByteArray)
}
