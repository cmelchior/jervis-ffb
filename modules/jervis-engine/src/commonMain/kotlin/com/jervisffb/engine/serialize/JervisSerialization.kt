package com.jervisffb.engine.serialize

import com.jervisffb.BuildConfig
import com.jervisffb.engine.GameEngineController
import com.jervisffb.engine.actions.GameAction
import com.jervisffb.engine.model.Coach
import com.jervisffb.engine.model.Field
import com.jervisffb.engine.model.Game
import com.jervisffb.engine.model.Team
import com.jervisffb.utils.getBuildType
import com.jervisffb.utils.getPlatformDescription
import com.jervisffb.utils.platformFileSystem
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.decodeFromJsonElement
import kotlinx.serialization.json.encodeToJsonElement
import okio.Path
import okio.buffer
import okio.use

data class GameFileData(
    val homeTeam: Team,
    val awayTeam: Team,
    // Controller in its initial state (what does this mean exactly?)
    // actions have already been applied to it. Why was it we wanted this here?
    // It is only being used by the DevScreen currently?
    val game: GameEngineController,
    // All game actions already known by the game. Can be used to get a GameEngineController
    // into the same state as when this game was saved.
    val actions: List<GameAction>,
)

/**
 * Class encapsulating the the logic for serializing and deserializing a Jervis game file.
 *
 * TODO Pretty annoying to keep this up to date. Figure out if there is a way to automate this.
 *  Perhaps a Gradle task that autogenerate it?
 */
object JervisSerialization {
    val jervisEngineModule = generatedJervisSerializerModule

    private val jsonFormat =
        Json {
            useArrayPolymorphism = true
            serializersModule = jervisEngineModule
            prettyPrint = true
        }

    fun createTeamSnapshot(team: Team): JsonElement {
        val serializedTeam = SerializedTeam.serialize(team)
        return jsonFormat.encodeToJsonElement(serializedTeam)
    }

    fun getGameFileName(controller: GameEngineController, includeDebugState: Boolean): String {
        val homeName = toValidFilename(controller.state.homeTeam.name)
        val awayName = toValidFilename(controller.state.awayTeam.name)
        val prefix = when (includeDebugState) {
            true -> "dump"
            false -> "game"
        }
        return "$prefix-$homeName-vs-$awayName.$FILE_EXTENSION_GAME_FILE"
    }

    fun serializeGameStateToJson(
        controller: GameEngineController,
        includeDebugInformation: Boolean
    ): String {
        val debugInfo = when (includeDebugInformation) {
            true -> createDebugInfo(controller)
            false -> null
        }
        val fileData =
            JervisGameFile(
                JervisMetaData(FILE_FORMAT_VERSION),
                JervisConfiguration(controller.rules),
                JervisGameData(controller.initialHomeTeamState!!, controller.initialAwayTeamState!!, controller.history.flatMap { it.steps.map { it.action }}),
                debugInfo
            )
        return jsonFormat.encodeToString(fileData)
    }

    fun saveToFile(
        controller: GameEngineController,
        file: Path,
        includeDebugInformation: Boolean
    ) {
        val fileContent = serializeGameStateToJson(controller, includeDebugInformation)
        platformFileSystem.sink(file).use { fileSink ->
            fileSink.buffer().use {
                it.writeUtf8(fileContent)
            }
        }
    }

    private fun createDebugInfo(controller: GameEngineController): JervisDebugInfo {
        val platformInfo = getBuildType() + "\n" + getPlatformDescription()
        val clientInfo =  BuildConfig.releaseVersion
        val gitCommit = BuildConfig.gitHashLong
        val errors = listOfNotNull(controller.lastHandleActionError?.stackTraceToString())
        return JervisDebugInfo(platformInfo, clientInfo, gitCommit, errors)
    }

    fun loadFromFileContent(json: String): Result<GameFileData> {
        try {
            val fileData = jsonFormat.decodeFromString<JervisGameFile>(json)
            val rules = fileData.configuration.rules
            val unknownCoach = Coach.UNKNOWN
            val serializedHomeTeam = jsonFormat.decodeFromJsonElement<SerializedTeam>(fileData.game.homeTeam)
            val homeTeam = SerializedTeam.deserialize(rules, serializedHomeTeam, unknownCoach)
            val serializedAwayTeam = jsonFormat.decodeFromJsonElement<SerializedTeam>(fileData.game.awayTeam)
            val awayTeam = SerializedTeam.deserialize(rules, serializedAwayTeam, unknownCoach)
            val state = Game(rules, homeTeam, awayTeam, Field.createForRuleset(rules))
            val controller = GameEngineController(state, fileData.game.actions)
            val gameData = GameFileData(homeTeam, awayTeam, controller, fileData.game.actions)
            return Result.success(gameData)
        } catch (ex: Exception) {
            return Result.failure(ex)
        }
    }

    /**
     * Load a Jervis Game File and prepare the game state from it.
     */
    fun loadFromFile(file: Path): Result<GameFileData> {
        try {
            val fileContent =
                platformFileSystem.source(file).use { fileSource ->
                    fileSource.buffer().readUtf8()
                }
            return loadFromFileContent(fileContent)
        } catch (ex: Exception) {
            return Result.failure(ex)
        }
    }

    private fun toValidFilename(input: String): String {
        return input
            .lowercase()
            .replace("\\s+".toRegex(), "_")
            .replace("[^a-z0-9._-]".toRegex(), "")
    }

}
