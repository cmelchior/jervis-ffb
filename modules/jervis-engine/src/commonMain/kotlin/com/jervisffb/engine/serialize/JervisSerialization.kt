package com.jervisffb.engine.serialize

import com.jervisffb.engine.GameEngineController
import com.jervisffb.engine.actions.GameAction
import com.jervisffb.engine.model.Coach
import com.jervisffb.engine.model.Field
import com.jervisffb.engine.model.Game
import com.jervisffb.engine.model.Team
import com.jervisffb.utils.platformFileSystem
import kotlinx.serialization.encodeToString
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
    val game: GameEngineController,
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

    fun saveToFile(
        controller: GameEngineController,
        file: Path,
    ) {
        val fileData =
            JervisGameFile(
                JervisMetaData(FILE_FORMAT_VERSION),
                JervisConfiguration(controller.rules),
                JervisGameData(controller.initialHomeTeamState!!, controller.initialAwayTeamState!!, controller.history.flatMap { it.steps.map { it.action }}),
            )
        val fileContent = jsonFormat.encodeToString(fileData)
        platformFileSystem.sink(file).use { fileSink ->
            fileSink.buffer().use {
                it.writeUtf8(fileContent)
            }
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
            val fileData = jsonFormat.decodeFromString<JervisGameFile>(fileContent)
            val rules = fileData.configuration.rules
            val unknownCoach = Coach.UNKNOWN
            val serializedHomeTeam = jsonFormat.decodeFromJsonElement<SerializedTeam>(fileData.game.homeTeam)
            val homeTeam = SerializedTeam.deserialize(rules, serializedHomeTeam, unknownCoach)
            val serializedAwayTeam = jsonFormat.decodeFromJsonElement<SerializedTeam>(fileData.game.awayTeam)
            val awayTeam = SerializedTeam.deserialize(rules, serializedAwayTeam, unknownCoach)
            val state = Game(rules, homeTeam, awayTeam, Field.createForRuleset(rules))
            val controller = GameEngineController(state)
            val gameData = GameFileData(homeTeam, awayTeam, controller, fileData.game.actions)
            return Result.success(gameData)
        } catch (ex: Exception) {
            return Result.failure(ex)
        }
    }
}
