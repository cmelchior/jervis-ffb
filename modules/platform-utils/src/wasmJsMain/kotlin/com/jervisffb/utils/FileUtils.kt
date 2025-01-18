package com.jervisffb.utils

import Database
import KeyPath
import com.jervisffb.utils.DatabaseManager.filesStore
import com.juul.indexeddb.external.IDBKey
import com.juul.indexeddb.external.IDBKeyRange
import kotlinx.browser.window
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.toList
import okio.FileHandle
import okio.FileMetadata
import okio.FileSystem
import okio.Path
import okio.Path.Companion.toPath
import okio.Sink
import okio.Source
import openDatabase
import org.khronos.webgl.Int8Array
import org.khronos.webgl.toByteArray
import org.khronos.webgl.toInt8Array

internal fun <T : JsAny> jso(): T = js("({})")
internal fun <T : JsAny> jso(block: T.() -> Unit): T = jso<T>().apply(block)

// Wrapper for interacting with IndexedDB
object DatabaseManager {

    val dbName = "JervisDatabase"
    val dbVersion = 1
    var database: Database? = null
    val filesStore = "files"

    suspend fun getDatabase(): Database {
        if (database == null) {
            initializeDatabase()
        }
        return database!!
    }

    private suspend fun initializeDatabase() {
        database = openDatabase(dbName, dbVersion) { db, oldVersion, newVersion ->
            var startVersion = oldVersion
            if (startVersion == 0) {
                val primaryKey = KeyPath("path")
                val store = db.createObjectStore(filesStore, primaryKey)
                store.createIndex("path", primaryKey, unique = true)
                startVersion++
            }
        }
    }

}


// TODO We are mostly using this for replay files. Probably this can be hidden behind some kind of better
//  interface
actual val platformFileSystem: FileSystem = object: FileSystem() {

    override fun appendingSink(file: Path, mustExist: Boolean): Sink {
        TODO("Filesystem not supported on JS")
    }
    override fun atomicMove(source: Path, target: Path) { /* Do nothing */ }
    override fun canonicalize(path: Path): Path = path
    override fun createDirectory(dir: Path, mustCreate: Boolean) { /* Do nothing */ }
    override fun createSymlink(source: Path, target: Path) { /* Do nothing */ }
    override fun delete(path: Path, mustExist: Boolean) { /* Do nothing */ }
    override fun list(dir: Path): List<Path> = emptyList()
    override fun listOrNull(dir: Path): List<Path>? = null
    override fun metadataOrNull(path: Path): FileMetadata? = null

    override fun openReadOnly(file: Path): FileHandle {
        TODO("Filesystem not supported on JS")
    }

    override fun openReadWrite(file: Path, mustCreate: Boolean, mustExist: Boolean): FileHandle {
        TODO("Filesystem not supported on JS")
    }

    override fun sink(file: Path, mustCreate: Boolean): Sink {
        TODO("Filesystem not supported on JS")
    }

    override fun source(file: Path): Source {
        TODO("Filesystem not supported on JS")
    }
}
// Not used on Wasm as things are either stored in LocalStorage or IndexedDB
actual val APPLICATION_DIRECTORY: String = ""

external interface WebFile: JsAny {
    var path: String
    var content: Int8Array
}

actual class FileManager {

    actual suspend fun getFilesWithExtension(directory: String, extension: String): List<Path> {
        val db = DatabaseManager.getDatabase()
        return db.transaction<List<Path>>(filesStore) {
            val store = objectStore(filesStore)
            val pathIndex = store.index("path")
            val range = IDBKeyRange.bound(
                IDBKey(directory),
                IDBKey("\uffff"),
                false,
                false
            )
            val files = pathIndex.openCursor(IDBKey(range), autoContinue = true)
                .filter {
                    it.key.toString().endsWith(extension)
                }
                .map { it.key.toString().toPath() }
                .toList()
            files
        }
    }
    actual suspend fun getFile(path: String): ByteArray? {
        val db = DatabaseManager.getDatabase()
        return db.transaction(filesStore) {
            val store = objectStore(filesStore)
            (store.get(IDBKey(path)) as WebFile?)?.content?.toByteArray()
        }
    }
    actual suspend fun writeFile(dir: String, fileName: String, fileContent: ByteArray) {
        val db = DatabaseManager.getDatabase()
        db.writeTransaction(filesStore) {
            val store = objectStore(filesStore)
            store.put(jso<WebFile> { path = "$dir/$fileName" ; content = fileContent.toInt8Array() })
        }
    }
}

actual class PropertiesManager actual constructor() {

    actual fun getSystemEnv(key: String): String {
        TODO()
    }

    actual suspend fun getString(key: String): String? {
        return window.localStorage.getItem(key)
    }
    actual suspend fun getBoolean(key: String): Boolean? {
        return window.localStorage.getItem(key)?.toBoolean()
    }
    actual suspend fun getInt(key: String): Int? {
        return window.localStorage.getItem(key)?.toInt()
    }
    actual suspend fun setProperty(key: String, value: Any?) {
        window.localStorage.setItem(key, value.toString())
    }
}
