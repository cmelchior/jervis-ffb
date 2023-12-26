package dk.ilios.jervis.utils

import dk.ilios.jervis.actions.Action
import dk.ilios.jervis.actions.ActionDescriptor
import dk.ilios.jervis.actions.Confirm
import dk.ilios.jervis.actions.ConfirmWhenReady
import dk.ilios.jervis.actions.Continue
import dk.ilios.jervis.actions.ContinueWhenReady
import dk.ilios.jervis.actions.D2Result
import dk.ilios.jervis.actions.DogoutSelected
import dk.ilios.jervis.actions.EndSetup
import dk.ilios.jervis.actions.EndSetupWhenReady
import dk.ilios.jervis.actions.EndTurn
import dk.ilios.jervis.actions.EndTurnWhenReady
import dk.ilios.jervis.actions.FieldSquareSelected
import dk.ilios.jervis.actions.PlayerSelected
import dk.ilios.jervis.actions.RollD2
import dk.ilios.jervis.actions.SelectDogout
import dk.ilios.jervis.actions.SelectFieldLocation
import dk.ilios.jervis.actions.SelectPlayer
import dk.ilios.jervis.model.Game
import kotlin.random.Random

fun createRandomAction(state: Game, availableActions: List<ActionDescriptor>): Action {
    return when(val action = availableActions.random()) {
        ContinueWhenReady -> Continue
        EndTurnWhenReady -> EndTurn
        RollD2 -> D2Result(Random.nextInt(1, 2))
        ConfirmWhenReady -> Confirm
        EndSetupWhenReady -> EndSetup
        SelectDogout -> DogoutSelected
        is SelectFieldLocation -> FieldSquareSelected(action.x, action.y)
        is SelectPlayer -> PlayerSelected(action.player)
    }
}

const val enableAsserts = true
inline fun assert(condition: Boolean) {
    if (enableAsserts && !condition) {
        throw IllegalStateException("A invariant failed")
    }
}

class InvalidAction(message: String): RuntimeException(message)
class InvalidGameState(message: String): RuntimeException(message)

inline fun INVALID_GAME_STATE(message: String = "Unexpected game state"): Nothing {
    throw InvalidGameState(message)
}

inline fun INVALID_ACTION(action: Action): Nothing {
    throw InvalidAction("Invalid action selected: $action")
}
