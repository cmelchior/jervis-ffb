package com.jervisffb.engine.statistics

import com.jervisffb.engine.GameDelta
import com.jervisffb.engine.GameEngineController
import com.jervisffb.engine.actions.GameAction
import com.jervisffb.engine.actions.Revert
import com.jervisffb.engine.actions.Undo
import com.jervisffb.engine.model.Game
import com.jervisffb.engine.statistics.probability.ProbabilityTracker

/**
 * Responsible for processing game events to extract statists about a running
 * game, e.g., "Number of 2D blocks done in the game", "What is the distribution
 * of dice being rolled".
 *
 * This is done through the [addAction] function, which takes each [GameDelta]
 * and evaluates the changes to the game state. This method also supports
 * [Undo] and [Revert].
 *
 * Statistics are enabled by adding an instance of this class to
 * [GameEngineController.statistics]
 *
 * Note: For now, this class is only collecting dice statistics needed to score
 * challenges. We need to figure out exactly what kind of statistics we want to
 * collect before adding more functionality.
 */
class GameStatistics(
    // If `set` any actions loaded in `GameEngineController.initialActions` will
    // not be used when calculating statistics.
    val ignoreInitialActions: Boolean = true
) {

    // Track dice rolls to calculate the chance of success for the given sequence.
    // TODO: Perhaps this concept can be extended to also include Luck Calculations,
    //  rather than just for scoring challenges.
    private val diceProbabilityTracker = ProbabilityTracker()
    val diceProbabilities: ProbabilityTracker
        get() = diceProbabilityTracker

    // Stats reported by FUMBBL. Can be used as a starting point for Jervis.

    // Armour Rolls
    // Injury Rolls
    // Total 2D6's
    // Total Block Dice
    // Total D6's
    // Single D6's
    // Total Blocks
    // Successful Blocks
    // Failed Blocks
    // Total Dodges
    // Successful Dodges
    // Failed Dodges
    // Total Rushes
    // Successful Rushes
    // Failed Rushes

    /**
     * Extracts statistics from the last action applied to [Game]. This method
     * should not be called if [GameEngineController] rejected the action.
     *
     * [action] was the action that was applied.
     * [delta] is the result of applying [action]. For [Undo] and [Revert], the
     * reversed delta should be used.
     */
    fun handleAction(action: GameAction, delta: GameDelta) {
        when (action) {
            Revert,
            Undo -> {
                require(delta.reversed) { "delta was not reversed for action: $action" }
                diceProbabilityTracker.handleAction(delta)
            }
            else -> diceProbabilityTracker.handleAction(delta)
        }
    }
}
