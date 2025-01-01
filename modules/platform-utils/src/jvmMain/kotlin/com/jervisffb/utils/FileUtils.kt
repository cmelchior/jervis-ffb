package com.jervisffb.utils

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import kotlinx.coroutines.flow.first
import okio.FileSystem
import okio.Path
import okio.Path.Companion.toPath
import okio.buffer
import okio.use
import java.io.File

actual val platformFileSystem: FileSystem = FileSystem.SYSTEM
actual val APPLICATION_DIRECTORY: String = "${System.getProperty("user.home")}/.jervis"

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

actual class PropertiesManager actual constructor() {

    private val store: DataStore<Preferences> = PreferenceDataStoreFactory.create {
        File("${System.getProperty("user.home")}/.jervis/settings.preferences_pb")
    }

    actual suspend fun getString(key: String): String? {
        return store.data.first()[stringPreferencesKey(key)]
    }

    actual suspend fun getBoolean(key: String): Boolean? {
        return store.data.first()[booleanPreferencesKey(key)]
    }

    actual suspend fun getInt(key: String): Int? {
        return store.data.first()[intPreferencesKey(key)]
    }

    actual suspend fun setProperty(key: String, value: Any?) {
        store.edit { props ->
            when (value) {
                is String -> props[stringPreferencesKey(key)] = value
                is Boolean -> props[booleanPreferencesKey(key)] = value
                is Int -> props[intPreferencesKey(key)] = value
                else -> throw IllegalArgumentException("Unsupported value type: ${value?.let { it::class.simpleName }}")
            }
        }
    }
 }
