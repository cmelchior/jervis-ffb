package com.jervisffb.engine.commands.probabiliy

import com.jervisffb.engine.GameDelta
import com.jervisffb.engine.GameEngineController
import com.jervisffb.engine.commands.Command
import com.jervisffb.engine.model.Game
import com.jervisffb.engine.statistics.probability.ChanceObservation

/**
 * Records structured data for one chance event in
 * [GameDelta.chanceObservations].
 *
 * As gathering all chance observations is done by [GameEngineController], this
 * command isn't responsible for actually storing it. Instead, it is left
 * up to [GameEngineController] to extract [observation] during the creation of
 * a [GameDelta].
 */
class AddChanceObservation(
    internal val observation: ChanceObservation,
) : Command {
    val observationIndex = observation.index

    override fun execute(state: Game) {
        require(observationIndex == state.chanceObservationSequence) {
            "Chance observation sequence diverged: expected ${state.chanceObservationSequence}, found $observationIndex"
        }
        state.chanceObservationSequence++
    }

    override fun undo(state: Game) {
        require(state.chanceObservationSequence == observationIndex + 1) {
            "Chance observation sequence could not be undone: $observationIndex"
        }
        state.chanceObservationSequence--
    }
}
