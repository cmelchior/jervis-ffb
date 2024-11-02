package com.jervisffb.engine.serialize

import com.jervisffb.engine.GameController
import com.jervisffb.engine.actions.BlockTypeSelected
import com.jervisffb.engine.actions.Cancel
import com.jervisffb.engine.actions.CoinSideSelected
import com.jervisffb.engine.actions.CoinTossResult
import com.jervisffb.engine.actions.Confirm
import com.jervisffb.engine.actions.Continue
import com.jervisffb.engine.actions.D12Result
import com.jervisffb.engine.actions.D16Result
import com.jervisffb.engine.actions.D20Result
import com.jervisffb.engine.actions.D2Result
import com.jervisffb.engine.actions.D3Result
import com.jervisffb.engine.actions.D4Result
import com.jervisffb.engine.actions.D6Result
import com.jervisffb.engine.actions.D8Result
import com.jervisffb.engine.actions.DBlockResult
import com.jervisffb.engine.actions.DicePoolResultsSelected
import com.jervisffb.engine.actions.DiceRollResults
import com.jervisffb.engine.actions.DirectionSelected
import com.jervisffb.engine.actions.DogoutSelected
import com.jervisffb.engine.actions.EndAction
import com.jervisffb.engine.actions.EndSetup
import com.jervisffb.engine.actions.EndTurn
import com.jervisffb.engine.actions.FieldSquareSelected
import com.jervisffb.engine.actions.GameAction
import com.jervisffb.engine.actions.MoveTypeSelected
import com.jervisffb.engine.actions.NoRerollSelected
import com.jervisffb.engine.actions.PlayerActionSelected
import com.jervisffb.engine.actions.PlayerDeselected
import com.jervisffb.engine.actions.PlayerSelected
import com.jervisffb.engine.actions.RandomPlayersSelected
import com.jervisffb.engine.actions.RerollOptionSelected
import com.jervisffb.engine.fsm.Procedure
import com.jervisffb.engine.model.Field
import com.jervisffb.engine.model.Game
import com.jervisffb.engine.model.Team
import com.jervisffb.engine.model.locations.FieldCoordinate
import com.jervisffb.engine.model.locations.FieldCoordinateImpl
import com.jervisffb.engine.rules.Rules
import com.jervisffb.engine.rules.StandardBB2020Rules
import com.jervisffb.engine.rules.bb2020.procedures.actions.blitz.BlitzAction
import com.jervisffb.engine.rules.bb2020.procedures.actions.block.BlockAction
import com.jervisffb.engine.rules.bb2020.procedures.actions.foul.FoulAction
import com.jervisffb.engine.rules.bb2020.procedures.actions.move.MoveAction
import com.jervisffb.engine.rules.bb2020.roster.BB2020Position
import com.jervisffb.engine.rules.bb2020.roster.BB2020Roster
import com.jervisffb.engine.rules.bb2020.roster.ChaosDwarfTeam
import com.jervisffb.engine.rules.bb2020.roster.ElvenUnionTeam
import com.jervisffb.engine.rules.bb2020.roster.HumanTeam
import com.jervisffb.engine.rules.bb2020.roster.KhorneTeam
import com.jervisffb.engine.rules.bb2020.roster.LizardmenTeam
import com.jervisffb.engine.rules.bb2020.roster.SkavenTeam
import com.jervisffb.engine.rules.common.roster.Position
import com.jervisffb.engine.rules.common.roster.Roster
import com.jervisffb.utils.platformFileSystem
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.decodeFromJsonElement
import kotlinx.serialization.json.encodeToJsonElement
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.modules.polymorphic
import kotlinx.serialization.modules.subclass
import okio.Path
import okio.buffer
import okio.use

/**
 * Class encapsulating the the logic for serializing and deserializing a Jervis game file.
 */
object JervisSerialization {
    private val jervisModule =
        SerializersModule {
            polymorphic(Procedure::class) {
                subclass(MoveAction::class)
                subclass(BlitzAction::class)
                subclass(BlockAction::class)
                subclass(FoulAction::class)
            }
            polymorphic(Rules::class) {
                subclass(StandardBB2020Rules::class)
            }
            polymorphic(Roster::class) {
                subclass(ChaosDwarfTeam::class)
                subclass(ElvenUnionTeam::class)
                subclass(HumanTeam::class)
                subclass(KhorneTeam::class)
                subclass(LizardmenTeam::class)
                subclass(SkavenTeam::class)
            }
            polymorphic(BB2020Roster::class) {
                subclass(ChaosDwarfTeam::class)
                subclass(ElvenUnionTeam::class)
                subclass(HumanTeam::class)
                subclass(KhorneTeam::class)
                subclass(LizardmenTeam::class)
                subclass(SkavenTeam::class)
            }
            polymorphic(Position::class) {
                subclass(BB2020Position::class)
            }
            polymorphic(FieldCoordinate::class) {
                subclass(FieldCoordinateImpl::class)
            }
            polymorphic(GameAction::class) {
                // polymorphic(DieResult::class) {
                subclass(BlockTypeSelected::class)
                subclass(Cancel::class)
                subclass(CoinSideSelected::class)
                subclass(CoinTossResult::class)
                subclass(Confirm::class)
                subclass(Continue::class)
                subclass(D12Result::class)
                subclass(D16Result::class)
                subclass(D20Result::class)
                subclass(D2Result::class)
                subclass(D3Result::class)
                subclass(D4Result::class)
                subclass(D6Result::class)
                subclass(D8Result::class)
                subclass(DBlockResult::class)
                subclass(DicePoolResultsSelected::class)
                subclass(DiceRollResults::class)
                subclass(DirectionSelected::class)
                subclass(DogoutSelected::class)
                subclass(EndAction::class)
                subclass(EndSetup::class)
                subclass(EndTurn::class)
                subclass(FieldSquareSelected::class)
                subclass(MoveTypeSelected::class)
                subclass(NoRerollSelected::class)
                subclass(PlayerActionSelected::class)
                subclass(PlayerDeselected::class)
                subclass(PlayerSelected::class)
                subclass(RandomPlayersSelected::class)
                subclass(RerollOptionSelected::class)
            }
        }

    private val jsonFormat =
        Json {
            useArrayPolymorphism = true
            serializersModule = jervisModule
            prettyPrint = true
        }

    fun createTeamSnapshot(team: Team): JsonElement {
        return jsonFormat.encodeToJsonElement(team)
    }

    fun saveToFile(
        controller: GameController,
        file: Path,
    ) {
        val fileData =
            JervisFile(
                JervisMetaData(),
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

    suspend fun loadFromFile(file: Path): Pair<GameController, List<GameAction>> {
        val fileContent =
            platformFileSystem.source(file).use { fileSource ->
                fileSource.buffer().readUtf8()
            }
        val gameData = jsonFormat.decodeFromString<JervisFile>(fileContent)
        val rules = gameData.configuration.rules
        val homeTeam = jsonFormat.decodeFromJsonElement<Team>(gameData.game.homeTeam)
        homeTeam.noToPlayer.values.forEach { it.team = homeTeam }
        homeTeam.notifyDogoutChange()
        val awayTeam = jsonFormat.decodeFromJsonElement<Team>(gameData.game.awayTeam)
        awayTeam.noToPlayer.values.forEach { it.team = awayTeam }
        awayTeam.notifyDogoutChange()
        val state = Game(rules, homeTeam, awayTeam, Field.createForRuleset(rules))
        val controller = GameController(rules, state)
        return Pair(controller, remapActionRefs(gameData.game.actions, state))
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
