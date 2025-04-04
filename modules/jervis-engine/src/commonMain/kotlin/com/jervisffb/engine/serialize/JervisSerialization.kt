package com.jervisffb.engine.serialize

import com.jervisffb.engine.GameEngineController
import com.jervisffb.engine.actions.GameAction
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
        return jsonFormat.encodeToJsonElement(team)
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
            val homeTeam = jsonFormat.decodeFromJsonElement<Team>(fileData.game.homeTeam)
            homeTeam.noToPlayer.values.forEach { it.team = homeTeam }
            homeTeam.notifyDogoutChange()
            val awayTeam = jsonFormat.decodeFromJsonElement<Team>(fileData.game.awayTeam)
            awayTeam.noToPlayer.values.forEach { it.team = awayTeam }
            awayTeam.notifyDogoutChange()
            val state = Game(rules, homeTeam, awayTeam, Field.createForRuleset(rules))
            val controller = GameEngineController(state)
            val gameData = GameFileData(homeTeam, awayTeam, controller, fileData.game.actions)
            return Result.success(gameData)
        } catch (ex: Exception) {
            return Result.failure(ex)
        }
    }

    /**
     * Make sure that all [Team] and [Game] references are set after deserializing a Team.
     * Hopefully, this can be removed eventually, but it requires changes to the deserializer.
     */
    fun fixStateRefs(state: Game): Game {
        state.homeTeam.forEach { it.team = state.homeTeam }
        state.awayTeam.forEach { it.team = state.awayTeam }
        state.homeTeam.teamIsHomeTeam = true
        state.homeTeam.teamIsAwayTeam = false
        state.homeTeam.setGameReference(state)
        state.awayTeam.teamIsHomeTeam = false
        state.awayTeam.teamIsAwayTeam = true
        state.awayTeam.setGameReference(state)
        state.homeTeam.notifyDogoutChange()
        state.awayTeam.notifyDogoutChange()
        return state
    }

    fun fixTeamRefs(team: Team): Team {
        team.forEach { it.team = team }
        team.notifyDogoutChange()
        return team
    }

    // Remap object references like to Player in GameActions so they all point to the same instance
    private fun remapActionRefs(
        actions: List<GameAction>,
        state: Game,
    ): List<GameAction> {
        return actions.map { action ->
            when (action) {
//                is PlayerSelected -> {
//                    val isHomeTeam = state.homeTeam.firstOrNull { it.id == action.player.id } != null
//                    val playerNo = action.player.number
//                    val team = if (isHomeTeam) state.homeTeam else state.awayTeam
//                    val player = team[playerNo]!!
//                    PlayerSelected(player)
//                }
                else -> action
            }
        }
    }
}
