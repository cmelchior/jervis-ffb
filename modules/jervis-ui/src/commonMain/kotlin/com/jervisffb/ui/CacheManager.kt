package com.jervisffb.ui

import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asSkiaBitmap
import androidx.compose.ui.graphics.toComposeImageBitmap
import com.jervisffb.engine.serialize.FILE_EXTENSION_TEAM_FILE
import com.jervisffb.engine.serialize.JervisSerialization.jervisEngineModule
import com.jervisffb.engine.serialize.JervisTeamFile
import com.jervisffb.utils.FileManager
import com.jervisffb.utils.platformFileSystem
import io.ktor.http.Url
import kotlinx.serialization.json.Json
import okio.buffer
import okio.use
import org.jetbrains.skia.EncodedImageFormat
import org.jetbrains.skia.Image

object CacheManager {

    val imageCacheRoot = "images"
    val teamsCacheRoot = "teams"
    val rosterCacheRoot = "rosters"

    val fileManager = FileManager()
    val json = Json {
        useArrayPolymorphism = true
        serializersModule = jervisEngineModule
        prettyPrint = true
    }

    suspend fun loadTeams(): List<JervisTeamFile> {
        return fileManager.getFilesWithExtension(teamsCacheRoot, FILE_EXTENSION_TEAM_FILE).map { file ->
            platformFileSystem.source(file).use { source ->
                val fileContent = source.buffer().readUtf8()
                json.decodeFromString<JervisTeamFile>(fileContent)
            }
        }
    }

    /**
     * @param cachePath relative path under ~/.jervis/cache/images
     */
    suspend fun getCachedImage(url: Url): ImageBitmap? {
        val host = url.host
        val path = url.encodedPath.replace("/", "_")
        return fileManager.getFile("$imageCacheRoot/$host/$path")?.let { file ->
            platformFileSystem.source(file).use { source ->
                val fileContent = source.buffer().readByteArray()
                Image.makeFromEncoded(fileContent).toComposeImageBitmap()
            }
        }
    }

    suspend fun saveImage(url: Url, bitmap: ImageBitmap) {
        val format = when (url.encodedPath.endsWith(".gif")) {
            true -> EncodedImageFormat.GIF
            else -> EncodedImageFormat.PNG
        }
        val host = url.host
        val fileName = url.encodedPath.replace("/", "_")
        val imageData = Image.makeFromBitmap(bitmap.asSkiaBitmap()).encodeToData(
            format = format,
        )?.bytes ?: error("This bitmap cannot be encoded")
        fileManager.writeFile("$imageCacheRoot/$host", fileName, imageData)
    }
}
