package com.jervisffb.utils

import okio.FileHandle
import okio.FileMetadata
import okio.FileSystem
import okio.Path
import okio.Sink
import okio.Source

// TODO We are mostly using this for replay files. Probably this can be hidden behind some kind of better
//  interface
actual val platformFileSystem: FileSystem = object: FileSystem() {
    override fun appendingSink(file: Path, mustExist: Boolean): Sink {
        TODO("Not yet implemented")
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
        TODO("Not yet implemented")
    }

    override fun openReadWrite(file: Path, mustCreate: Boolean, mustExist: Boolean): FileHandle {
        TODO("Not yet implemented")
    }

    override fun sink(file: Path, mustCreate: Boolean): Sink {
        TODO("Not yet implemented")
    }

    override fun source(file: Path): Source {
        TODO("Not yet implemented")
    }
}

actual class FileManager {
    actual suspend fun getFilesWithExtension(directory: String, extension: String): List<Path> {
        TODO()
    }
    actual suspend fun getFile(path: String): Path? {
        TODO()
    }
    actual suspend fun writeFile(dir: String, fileName: String, fileContent: ByteArray) {
        TODO()
    }
}
