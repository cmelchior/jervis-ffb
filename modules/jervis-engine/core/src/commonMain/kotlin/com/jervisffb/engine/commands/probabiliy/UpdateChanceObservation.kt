package com.jervisffb.engine.commands.probabiliy

import com.jervisffb.engine.GameDelta
import com.jervisffb.engine.GameEngineController
import com.jervisffb.engine.commands.Command
import com.jervisffb.engine.model.Game
import com.jervisffb.engine.statistics.probability.observation.ChanceObservation

/**
 * Updates a previously recorded [ChanceObservation] in
 * [GameDelta.chanceObservations].
 *
 * As the collection of chance events is done by [GameEngineController],
 * this command doesn't actually modify the game state. Instead, it is left
 * up to [GameEngineController] to extract [updated] during the creation of
 * a [GameDelta].
 */
class UpdateChanceObservation(
    private val sequence: Int,
    internal val previous: ChanceObservation,
    internal val updated: ChanceObservation,
) : Command {
    init {
        require(previous.index == sequence) {
            "Previous observation sequence ${previous.index} does not match slot $sequence."
        }
        require(updated.index == sequence) {
            "Updated observation sequence ${updated.index} does not match slot $sequence."
        }
    }

    /** Observation updates are metadata only and do not mutate [Game]. */
    override fun execute(state: Game) = Unit
    override fun undo(state: Game) = Unit
}
