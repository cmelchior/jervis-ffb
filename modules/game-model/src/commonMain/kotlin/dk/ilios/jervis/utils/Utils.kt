package dk.ilios.jervis.utils

import dk.ilios.jervis.actions.ActionDescriptor
import dk.ilios.jervis.actions.Cancel
import dk.ilios.jervis.actions.CancelWhenReady
import dk.ilios.jervis.actions.CoinSideSelected
import dk.ilios.jervis.actions.CoinTossResult
import dk.ilios.jervis.actions.Confirm
import dk.ilios.jervis.actions.ConfirmWhenReady
import dk.ilios.jervis.actions.Continue
import dk.ilios.jervis.actions.ContinueWhenReady
import dk.ilios.jervis.actions.D12Result
import dk.ilios.jervis.actions.D16Result
import dk.ilios.jervis.actions.D20Result
import dk.ilios.jervis.actions.D2Result
import dk.ilios.jervis.actions.D3Result
import dk.ilios.jervis.actions.D4Result
import dk.ilios.jervis.actions.D6Result
import dk.ilios.jervis.actions.D8Result
import dk.ilios.jervis.actions.DBlockResult
import dk.ilios.jervis.actions.DeselectPlayer
import dk.ilios.jervis.actions.Dice
import dk.ilios.jervis.actions.DiceResults
import dk.ilios.jervis.actions.DogoutSelected
import dk.ilios.jervis.actions.EndAction
import dk.ilios.jervis.actions.EndActionWhenReady
import dk.ilios.jervis.actions.EndSetup
import dk.ilios.jervis.actions.EndSetupWhenReady
import dk.ilios.jervis.actions.EndTurn
import dk.ilios.jervis.actions.EndTurnWhenReady
import dk.ilios.jervis.actions.FieldSquareSelected
import dk.ilios.jervis.actions.GameAction
import dk.ilios.jervis.actions.NoRerollSelected
import dk.ilios.jervis.actions.PlayerActionSelected
import dk.ilios.jervis.actions.PlayerDeselected
import dk.ilios.jervis.actions.PlayerSelected
import dk.ilios.jervis.actions.RandomPlayersSelected
import dk.ilios.jervis.actions.RerollOptionSelected
import dk.ilios.jervis.actions.RollDice
import dk.ilios.jervis.actions.SelectAction
import dk.ilios.jervis.actions.SelectCoinSide
import dk.ilios.jervis.actions.SelectDogout
import dk.ilios.jervis.actions.SelectFieldLocation
import dk.ilios.jervis.actions.SelectNoReroll
import dk.ilios.jervis.actions.SelectPlayer
import dk.ilios.jervis.actions.SelectRandomPlayers
import dk.ilios.jervis.actions.SelectRerollOption
import dk.ilios.jervis.actions.TossCoin
import dk.ilios.jervis.commands.SetPlayerLocation
import dk.ilios.jervis.commands.SetPlayerState
import dk.ilios.jervis.controller.GameController
import dk.ilios.jervis.model.Coach
import dk.ilios.jervis.model.CoachId
import dk.ilios.jervis.model.Coin
import dk.ilios.jervis.model.Field
import dk.ilios.jervis.model.FieldCoordinate
import dk.ilios.jervis.model.Game
import dk.ilios.jervis.model.Player
import dk.ilios.jervis.model.PlayerId
import dk.ilios.jervis.model.PlayerNo
import dk.ilios.jervis.model.PlayerState
import dk.ilios.jervis.model.Team
import dk.ilios.jervis.rules.BB2020Rules
import dk.ilios.jervis.rules.roster.bb2020.HumanTeam
import dk.ilios.jervis.teamBuilder
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlin.random.Random

fun createRandomAction(
    state: Game,
    availableActions: List<ActionDescriptor>,
): GameAction {
    return when (val action = availableActions.random()) {
        ContinueWhenReady -> Continue
        EndTurnWhenReady -> EndTurn
        is RollDice -> {
            val results =
                action.dice.map {
                    when (it) {
                        Dice.D2 -> D2Result()
                        Dice.D3 -> D3Result()
                        Dice.D4 -> D4Result()
                        Dice.D6 -> D6Result()
                        Dice.D8 -> D8Result()
                        Dice.D12 -> D12Result()
                        Dice.D16 -> D16Result()
                        Dice.D20 -> D20Result()
                        Dice.BLOCK -> DBlockResult()
                    }
                }
            return DiceResults(results)
        }
        ConfirmWhenReady -> Confirm
        EndSetupWhenReady -> EndSetup
        SelectDogout -> DogoutSelected
        is SelectFieldLocation -> FieldSquareSelected(action.x, action.y)
        is SelectPlayer -> PlayerSelected(action.player)
        is DeselectPlayer -> PlayerDeselected
        is SelectAction -> PlayerActionSelected(action.action)
        EndActionWhenReady -> EndAction
        CancelWhenReady -> Cancel
        SelectCoinSide -> {
            when (Random.nextInt(2)) {
                0 -> CoinSideSelected(Coin.HEAD)
                1 -> CoinSideSelected(Coin.TAIL)
                else -> throw IllegalStateException()
            }
        }
        TossCoin -> {
            when (Random.nextInt(2)) {
                0 -> CoinTossResult(Coin.HEAD)
                1 -> CoinTossResult(Coin.TAIL)
                else -> throw IllegalStateException()
            }
        }

        is SelectRandomPlayers ->
            RandomPlayersSelected(action.players.shuffled().subList(0, action.count))

        SelectNoReroll -> NoRerollSelected
        is SelectRerollOption -> RerollOptionSelected(action.option)
    }
}

const val enableAsserts = true

inline fun assert(condition: Boolean) {
    if (enableAsserts && !condition) {
        throw IllegalStateException("A invariant failed")
    }
}

class InvalidAction(message: String) : RuntimeException(message)

class InvalidGameState(message: String) : IllegalStateException(message)

inline fun INVALID_GAME_STATE(message: String = "Unexpected game state"): Nothing {
    throw InvalidGameState(message)
}

inline fun INVALID_ACTION(action: GameAction): Nothing {
    throw InvalidAction("Invalid action selected: $action")
}

fun createDefaultGameState(rules: BB2020Rules): Game {
    val team1: Team =
        teamBuilder(HumanTeam) {
            coach = Coach(CoachId("home-coach"), "HomeCoach")
            name = "HomeTeam"
            addPlayer(PlayerId("H1"), "Lineman-1-H", PlayerNo(1), HumanTeam.LINEMAN)
            addPlayer(PlayerId("H2"), "Lineman-2-H", PlayerNo(2), HumanTeam.LINEMAN)
            addPlayer(PlayerId("H3"), "Lineman-3-H", PlayerNo(3), HumanTeam.LINEMAN)
            addPlayer(PlayerId("H4"), "Lineman-4-H", PlayerNo(4), HumanTeam.LINEMAN)
            addPlayer(PlayerId("H5"), "Thrower-1-H", PlayerNo(5), HumanTeam.THROWER)
            addPlayer(PlayerId("H6"), "Catcher-1-H", PlayerNo(6), HumanTeam.CATCHER)
            addPlayer(PlayerId("H7"), "Catcher-2-H", PlayerNo(7), HumanTeam.CATCHER)
            addPlayer(PlayerId("H8"), "Blitzer-1-H", PlayerNo(8), HumanTeam.BLITZER)
            addPlayer(PlayerId("H9"), "Blitzer-2-H", PlayerNo(9), HumanTeam.BLITZER)
            addPlayer(PlayerId("H10"), "Blitzer-3-H", PlayerNo(10), HumanTeam.BLITZER)
            addPlayer(PlayerId("H11"), "Blitzer-4-H", PlayerNo(11), HumanTeam.BLITZER)
            reRolls = 4
            apothecaries = 1
        }
    val team2: Team =
        teamBuilder(HumanTeam) {
            coach = Coach(CoachId("away-coach"), "AwayCoach")
            name = "AwayTeam"
            addPlayer(PlayerId("A1"), "Lineman-1-A", PlayerNo(1), HumanTeam.LINEMAN)
            addPlayer(PlayerId("A2"), "Lineman-2-A", PlayerNo(2), HumanTeam.LINEMAN)
            addPlayer(PlayerId("A3"), "Lineman-3-A", PlayerNo(3), HumanTeam.LINEMAN)
            addPlayer(PlayerId("A4"), "Lineman-4-A", PlayerNo(4), HumanTeam.LINEMAN)
            addPlayer(PlayerId("A5"), "Thrower-1-A", PlayerNo(5), HumanTeam.THROWER)
            addPlayer(PlayerId("A6"), "Catcher-1-A", PlayerNo(6), HumanTeam.CATCHER)
            addPlayer(PlayerId("A7"), "Catcher-2-A", PlayerNo(7), HumanTeam.CATCHER)
            addPlayer(PlayerId("A8"), "Blitzer-1-A", PlayerNo(8), HumanTeam.BLITZER)
            addPlayer(PlayerId("A9"), "Blitzer-2-A", PlayerNo(9), HumanTeam.BLITZER)
            addPlayer(PlayerId("A10"), "Blitzer-3-A", PlayerNo(10), HumanTeam.BLITZER)
            addPlayer(PlayerId("A11"), "Blitzer-4-A", PlayerNo(11), HumanTeam.BLITZER)
            reRolls = 4
            apothecaries = 1
        }
    val field = Field.createForRuleset(rules)
    return Game(team1, team2, field)
}

/**
 * Move all players onto the field as if starting a game.
 * Only works on the setup defined above
 */
fun createStartingTestSetup(state: Game) {
    fun setupPlayer(
        state: Game,
        player: Player?,
        fieldCoordinate: FieldCoordinate,
    ) {
        player?.let {
            SetPlayerLocation(it, fieldCoordinate).execute(state, GameController(BB2020Rules, state))
            SetPlayerState(it, PlayerState.STANDING)
        } ?: error("")
    }

    // Home
    with(state.homeTeam) {
        setupPlayer(state, this[PlayerNo(1)], FieldCoordinate(12, 6))
        setupPlayer(state, this[PlayerNo(2)], FieldCoordinate(12, 7))
        setupPlayer(state, this[PlayerNo(3)], FieldCoordinate(12, 8))
        setupPlayer(state, this[PlayerNo(4)], FieldCoordinate(10, 1))
        setupPlayer(state, this[PlayerNo(5)], FieldCoordinate(10, 4))
        setupPlayer(state, this[PlayerNo(6)], FieldCoordinate(10, 10))
        setupPlayer(state, this[PlayerNo(7)], FieldCoordinate(10, 13))
        setupPlayer(state, this[PlayerNo(8)], FieldCoordinate(8, 1))
        setupPlayer(state, this[PlayerNo(9)], FieldCoordinate(8, 4))
        setupPlayer(state, this[PlayerNo(10)], FieldCoordinate(8, 10))
        setupPlayer(state, this[PlayerNo(11)], FieldCoordinate(8, 13))
    }

    // Away
    with(state.awayTeam) {
        setupPlayer(state, this[PlayerNo(1)], FieldCoordinate(13, 6))
        setupPlayer(state, this[PlayerNo(2)], FieldCoordinate(13, 7))
        setupPlayer(state, this[PlayerNo(3)], FieldCoordinate(13, 8))
        setupPlayer(state, this[PlayerNo(4)], FieldCoordinate(15, 1))
        setupPlayer(state, this[PlayerNo(5)], FieldCoordinate(15, 4))
        setupPlayer(state, this[PlayerNo(6)], FieldCoordinate(15, 10))
        setupPlayer(state, this[PlayerNo(7)], FieldCoordinate(15, 13))
        setupPlayer(state, this[PlayerNo(8)], FieldCoordinate(17, 1))
        setupPlayer(state, this[PlayerNo(9)], FieldCoordinate(17, 4))
        setupPlayer(state, this[PlayerNo(10)], FieldCoordinate(17, 10))
        setupPlayer(state, this[PlayerNo(11)], FieldCoordinate(17, 13))
    }
}

fun <T : Any?> MutableStateFlow<T>.safeTryEmit(value: T) {
    if (!this.tryEmit(value)) {
        throw IllegalStateException("Failed to emit value: $value")
    }
}

fun <T : Any?> MutableSharedFlow<T>.safeTryEmit(value: T) {
    if (!this.tryEmit(value)) {
        throw IllegalStateException("Failed to emit value: $value")
    }
}
