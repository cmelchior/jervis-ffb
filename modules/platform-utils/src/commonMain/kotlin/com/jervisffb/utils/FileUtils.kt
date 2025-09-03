@file:Suppress("EXPECT_ACTUAL_CLASSIFIERS_ARE_IN_BETA_WARNING")

package com.jervisffb.utils

import okio.FileSystem
import okio.Path

expect val platformFileSystem: FileSystem
expect val APPLICATION_DIRECTORY: String

/**
 * Low-level API for application specific files. This will differ based on the system:
 * Accessing or saving files outside this directory is done through the [file]
 *
 * - JVM: Files are stored under a ~/.jervis folder.
 * - Wasm: Files are stored in IndexedDB.
 * - iOS: TBD
 */
expect class FileManager() {
    // Will always return a relative path from the root of the hidden Jervis folder
    suspend fun getFilesWithExtension(directory: String, extension: String): List<Path>
    suspend fun getFile(path: String): ByteArray?
    suspend fun writeFile(dir: String, fileName: String, fileContent: ByteArray)
}

