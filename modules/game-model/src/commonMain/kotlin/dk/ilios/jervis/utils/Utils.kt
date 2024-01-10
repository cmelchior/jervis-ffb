package dk.ilios.jervis.utils

import dk.ilios.jervis.actions.GameAction
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
import dk.ilios.jervis.actions.DeselectPlayer
import dk.ilios.jervis.actions.Dice
import dk.ilios.jervis.actions.DiceResults
import dk.ilios.jervis.actions.DieResult
import dk.ilios.jervis.actions.DogoutSelected
import dk.ilios.jervis.actions.EndAction
import dk.ilios.jervis.actions.EndActionWhenReady
import dk.ilios.jervis.actions.EndSetup
import dk.ilios.jervis.actions.EndSetupWhenReady
import dk.ilios.jervis.actions.EndTurn
import dk.ilios.jervis.actions.EndTurnWhenReady
import dk.ilios.jervis.actions.FieldSquareSelected
import dk.ilios.jervis.actions.PlayerActionSelected
import dk.ilios.jervis.actions.PlayerDeselected
import dk.ilios.jervis.actions.PlayerSelected
import dk.ilios.jervis.actions.RollDice
import dk.ilios.jervis.actions.SelectAction
import dk.ilios.jervis.actions.SelectCoinSide
import dk.ilios.jervis.actions.SelectDogout
import dk.ilios.jervis.actions.SelectFieldLocation
import dk.ilios.jervis.actions.SelectPlayer
import dk.ilios.jervis.actions.TossCoin
import dk.ilios.jervis.model.Coin
import dk.ilios.jervis.model.Game
import kotlin.random.Random

fun createRandomAction(state: Game, availableActions: List<ActionDescriptor>): GameAction {
    return when(val action = availableActions.random()) {
        ContinueWhenReady -> Continue
        EndTurnWhenReady -> EndTurn
        is RollDice -> {
            val results: List<DieResult> = action.dice.map {
                when(it) {
                    Dice.D2 -> D2Result()
                    Dice.D3 -> D3Result()
                    Dice.D4 -> D4Result()
                    Dice.D6 -> D6Result()
                    Dice.D8 -> D8Result()
                    Dice.D12 -> D12Result()
                    Dice.D16 -> D16Result()
                    Dice.D20 -> D20Result()
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
            when(Random.nextInt(2)) {
                0 -> CoinSideSelected(Coin.HEAD)
                1 -> CoinSideSelected(Coin.TAIL)
                else -> throw IllegalStateException()
            }
        }
        TossCoin -> {
            when(Random.nextInt(2)) {
                0 -> CoinTossResult(Coin.HEAD)
                1 -> CoinTossResult(Coin.TAIL)
                else -> throw IllegalStateException()
            }
        }
    }
}

const val enableAsserts = true
inline fun assert(condition: Boolean) {
    if (enableAsserts && !condition) {
        throw IllegalStateException("A invariant failed")
    }
}

class InvalidAction(message: String): RuntimeException(message)
class InvalidGameState(message: String): IllegalStateException(message)

inline fun INVALID_GAME_STATE(message: String = "Unexpected game state"): Nothing {
    throw InvalidGameState(message)
}

inline fun INVALID_ACTION(action: GameAction): Nothing {
    throw InvalidAction("Invalid action selected: $action")
}
