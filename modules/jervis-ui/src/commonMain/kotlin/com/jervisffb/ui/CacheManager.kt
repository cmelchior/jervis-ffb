package com.jervisffb.ui

import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asSkiaBitmap
import androidx.compose.ui.graphics.toComposeImageBitmap
import com.jervisffb.engine.serialize.FILE_EXTENSION_TEAM_FILE
import com.jervisffb.engine.serialize.JervisSerialization.jervisEngineModule
import com.jervisffb.engine.serialize.JervisTeamFile
import com.jervisffb.resources.StandaloneTeams
import com.jervisffb.utils.FileManager
import io.ktor.http.Url
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.jetbrains.skia.EncodedImageFormat
import org.jetbrains.skia.Image

object CacheManager {

    val imageCacheRoot = "images"
    val teamsCacheRoot = "teams"
    val rosterCacheRoot = "rosters"

    val fileManager = FileManager()
    val jsonSerializer = Json {
        useArrayPolymorphism = true
        serializersModule = jervisEngineModule
        prettyPrint = true
    }

    suspend fun createInitialTeamFiles() {
        StandaloneTeams.defaultTeams.forEach { (fileName, roster) ->
            val json = jsonSerializer.encodeToString(roster).encodeToByteArray()
            FILE_MANAGER.writeFile(teamsCacheRoot, fileName, json)
        }
    }

    suspend fun loadTeams(): List<JervisTeamFile> {
        return fileManager.getFilesWithExtension(teamsCacheRoot, FILE_EXTENSION_TEAM_FILE).map { file ->
            val fileContent = fileManager.getFile(file.toString())
            if (fileContent == null) {
                throw IllegalStateException("Could not find: $file")

            }
            val json = fileContent.map { Char(it.toInt()) }.toCharArray().concatToString()
            jsonSerializer.decodeFromString<JervisTeamFile>(json)
        }
    }

    /**
     * @param cachePath relative path under ~/.jervis/cache/images
     */
    suspend fun getCachedImage(url: Url): ImageBitmap? {
        val host = url.host
        val path = url.encodedPath.replace("/", "_")
        return fileManager.getFile("$imageCacheRoot/$host/$path")?.let { fileContent ->
            Image.makeFromEncoded(fileContent).toComposeImageBitmap()
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

    suspend fun saveTeam(file: JervisTeamFile) {
        val fileContent = jsonSerializer.encodeToString(file)
        val fileName = "team_${file.team.id.value}.$FILE_EXTENSION_TEAM_FILE"
        fileManager.writeFile(teamsCacheRoot, fileName, fileContent.encodeToByteArray())
    }
}
