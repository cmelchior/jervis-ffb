package com.jervisffb.ui

import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asSkiaBitmap
import androidx.compose.ui.graphics.toComposeImageBitmap
import com.jervisffb.engine.serialize.FILE_EXTENSION_SETUP_FILE
import com.jervisffb.engine.serialize.FILE_EXTENSION_TEAM_FILE
import com.jervisffb.engine.serialize.JervisSerialization.jervisEngineModule
import com.jervisffb.engine.serialize.JervisSetupFile
import com.jervisffb.engine.serialize.JervisTeamFile
import com.jervisffb.resources.DefaultSetups
import com.jervisffb.resources.StandaloneBB7Teams
import com.jervisffb.resources.StandaloneStandardTeams
import com.jervisffb.utils.FileManager
import io.ktor.http.Url
import kotlinx.serialization.json.Json
import okio.Path
import org.jetbrains.skia.EncodedImageFormat
import org.jetbrains.skia.Image

object CacheManager {

    val imageCacheRoot = "images"
    val teamsCacheRoot = "teams"
    val rosterCacheRoot = "rosters"
    val fumbbleReplays = "fumbbl_replays"
    val setupsCacheRoot = "setups"

    val fileManager = FileManager()
    val jsonSerializer = Json {
        useArrayPolymorphism = true
        serializersModule = jervisEngineModule
        prettyPrint = true
    }

    suspend fun createInitialTeamFiles() {
        (StandaloneStandardTeams.defaultTeams + StandaloneBB7Teams.defaultTeams).forEach { (fileName, roster) ->
            val json = jsonSerializer.encodeToString(roster).encodeToByteArray()
            FILE_MANAGER.writeFile(teamsCacheRoot, fileName, json)
        }
    }

    suspend fun createInitialSetupFiles() {
        (DefaultSetups.standardSetups + DefaultSetups.sevensSetups).forEach { (fileName, setupFile) ->
            val json = jsonSerializer.encodeToString(setupFile).encodeToByteArray()
            FILE_MANAGER.writeFile(setupsCacheRoot, fileName, json)
        }
    }

    suspend fun loadSetups(): List<JervisSetupFile> {
        return fileManager.getFilesWithExtension(setupsCacheRoot, FILE_EXTENSION_SETUP_FILE)
            .map { file ->
                val fileContent = fileManager.getFile(file.toString()) ?: throw IllegalStateException("Could not find: $file")
                val json = fileContent.decodeToString()
                jsonSerializer.decodeFromString<JervisSetupFile>(json)
            }
    }

    suspend fun loadTeams(): List<JervisTeamFile> {
        return fileManager.getFilesWithExtension(teamsCacheRoot, FILE_EXTENSION_TEAM_FILE).map { file ->
            val fileContent = fileManager.getFile(file.toString()) ?: throw IllegalStateException("Could not find: $file")
            val json = fileContent.decodeToString()
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

    /**
     * Save a FUMBBL replay file to disk so we do not need to fetch it every time.
     * @param gameId the game id on FUMBBL. Used to generate the filename.
     */
    suspend fun saveFumbblReplay(gameId: Int, fileContent: ByteArray) {
        // TODO Figure out how to store replays, so we can also show the overview stats
        //  quickly without being forced to read all the files.
        //  Without a database, we could maybe store a summary in the settings file
        //  and try not to get them out of sync.
        val fileName = "replay_${gameId}.json"
        fileManager.writeFile(fumbbleReplays, fileName, fileContent)
    }

    suspend fun getFumbleReplayFiles(): List<Path> {
        return fileManager.getFilesWithExtension(fumbbleReplays, "json")
    }
}
