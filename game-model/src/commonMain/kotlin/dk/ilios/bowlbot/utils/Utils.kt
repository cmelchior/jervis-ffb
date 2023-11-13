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
        is SelectAvailableSpace -> TODO()
    }
}