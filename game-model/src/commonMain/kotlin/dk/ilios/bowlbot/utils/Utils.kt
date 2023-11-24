package dk.ilios.bowlbot.utils

import dk.ilios.bowlbot.actions.Action
import dk.ilios.bowlbot.actions.ActionDescriptor
import dk.ilios.bowlbot.actions.Continue
import dk.ilios.bowlbot.actions.ContinueWhenReady
import dk.ilios.bowlbot.actions.D2Result
import dk.ilios.bowlbot.actions.EndTurn
import dk.ilios.bowlbot.actions.EndTurnWhenReady
import dk.ilios.bowlbot.actions.RollD2
import dk.ilios.bowlbot.actions.SelectAvailableSpace
import dk.ilios.bowlbot.model.Game
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
