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

/**
 * Low-level api for storing and retrieving key/value properties.
 *
 * - JVM: Stored in a jervis.properties file in the application folder
 * - Wasm: Stored in LocalStorage in the browser.
 * - iOS: Figure out where
 */
expect class PropertiesManager() {
    // If the System supports system environment variables, this will attempt to fetch it.
    // If the platform doesn't support this or the key wasn't found, "" is returned
    fun getSystemEnv(key: String): String
    suspend fun getString(key: String): String?
    suspend fun getBoolean(key: String): Boolean?
    suspend fun getInt(key: String): Int?
    suspend fun setProperty(key: String, value: Any?)
}
