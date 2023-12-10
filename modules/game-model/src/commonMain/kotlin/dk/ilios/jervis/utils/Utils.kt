package dk.ilios.jervis.utils

import dk.ilios.jervis.actions.Action
import dk.ilios.jervis.actions.ActionDescriptor
import dk.ilios.jervis.actions.Continue
import dk.ilios.jervis.actions.ContinueWhenReady
import dk.ilios.jervis.actions.D2Result
import dk.ilios.jervis.actions.EndTurn
import dk.ilios.jervis.actions.EndTurnWhenReady
import dk.ilios.jervis.actions.RollD2
import dk.ilios.jervis.actions.SelectAvailableSpace
import dk.ilios.jervis.model.Game
import kotlin.random.Random

fun createRandomAction(state: Game, availableActions: List<ActionDescriptor>): Action {
    return when(availableActions.random()) {
        ContinueWhenReady -> Continue
        EndTurnWhenReady -> EndTurn
        RollD2 -> D2Result(Random.nextInt(1, 2))
        is SelectAvailableSpace -> INVALID_GAME_STATE()
    }
}

const val enableAsserts = true
inline fun assert(condition: Boolean) {
    if (enableAsserts && !condition) {
        throw IllegalStateException("A invariant failed")
    }
}

class InvalidGameState(message: String): RuntimeException(message)

inline fun INVALID_GAME_STATE(message: String = "Unexpected game state"): Nothing {
    throw InvalidGameState(message)
}
