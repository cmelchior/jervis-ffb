package dk.ilios.jervis.serialize

import dk.ilios.jervis.actions.Cancel
import dk.ilios.jervis.actions.CoinSideSelected
import dk.ilios.jervis.actions.CoinTossResult
import dk.ilios.jervis.actions.Confirm
import dk.ilios.jervis.actions.Continue
import dk.ilios.jervis.actions.D12Result
import dk.ilios.jervis.actions.D16Result
import dk.ilios.jervis.actions.D20Result
import dk.ilios.jervis.actions.D2Result
import dk.ilios.jervis.actions.D3Result
import dk.ilios.jervis.actions.D4Result
import dk.ilios.jervis.actions.D6Result
import dk.ilios.jervis.actions.D8Result
import dk.ilios.jervis.actions.DBlockResult
import dk.ilios.jervis.actions.DiceRollResults
import dk.ilios.jervis.actions.DogoutSelected
import dk.ilios.jervis.actions.EndAction
import dk.ilios.jervis.actions.EndSetup
import dk.ilios.jervis.actions.EndTurn
import dk.ilios.jervis.actions.FieldSquareSelected
import dk.ilios.jervis.actions.GameAction
import dk.ilios.jervis.actions.MoveTypeSelected
import dk.ilios.jervis.actions.NoRerollSelected
import dk.ilios.jervis.actions.PlayerActionSelected
import dk.ilios.jervis.actions.PlayerDeselected
import dk.ilios.jervis.actions.PlayerSelected
import dk.ilios.jervis.actions.RandomPlayersSelected
import dk.ilios.jervis.actions.RerollOptionSelected
import dk.ilios.jervis.controller.GameController
import dk.ilios.jervis.fsm.Procedure
import dk.ilios.jervis.model.Field
import dk.ilios.jervis.model.Game
import dk.ilios.jervis.model.Team
import dk.ilios.jervis.model.locations.FieldCoordinate
import dk.ilios.jervis.procedures.actions.blitz.BlitzAction
import dk.ilios.jervis.procedures.actions.block.BlockAction
import dk.ilios.jervis.procedures.actions.foul.FoulAction
import dk.ilios.jervis.procedures.actions.move.MoveAction
import dk.ilios.jervis.rules.Rules
import dk.ilios.jervis.rules.StandardBB2020Rules
import dk.ilios.jervis.rules.roster.Position
import dk.ilios.jervis.rules.roster.Roster
import dk.ilios.jervis.rules.roster.bb2020.BB2020Position
import dk.ilios.jervis.rules.roster.bb2020.BB2020Roster
import dk.ilios.jervis.rules.roster.bb2020.ChaosDwarfTeam
import dk.ilios.jervis.rules.roster.bb2020.ElvenUnionTeam
import dk.ilios.jervis.rules.roster.bb2020.HumanTeam
import dk.ilios.jervis.rules.roster.bb2020.KhorneTeam
import dk.ilios.jervis.rules.roster.bb2020.LizardmenTeam
import dk.ilios.jervis.rules.roster.bb2020.SkavenTeam
import dk.ilios.jervis.utils.platformFileSystem
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
                subclass(dk.ilios.jervis.model.locations.FieldCoordinateImpl::class)
            }
            polymorphic(GameAction::class) {
                // polymorphic(DieResult::class) {
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
                subclass(DiceRollResults::class)
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
                JervisGameData(controller.initialHomeTeamState!!, controller.initialAwayTeamState!!, controller.actionHistory),
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
        val state = Game(homeTeam, awayTeam, Field.createForRuleset(rules))
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
