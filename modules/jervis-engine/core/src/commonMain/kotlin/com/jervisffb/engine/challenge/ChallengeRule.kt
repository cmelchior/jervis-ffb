package com.jervisffb.engine.challenge

import com.jervisffb.engine.GameDelta
import com.jervisffb.engine.GameEngineController
import com.jervisffb.engine.model.Game

/**
 * A challenge restriction layered on top of the normal rules of the game.
 */
interface ChallengeRule {

    /** One line, shown in the "Rules" list on the challenge details page. */
    val description: String

    /**
     * Called after the [GameEngineController] is started and initial actions
     * have been applied, but before any other actions can be applied to the
     * game state.
     *
     * This allows a [ChallengeRule] to modify the game state in a way that is
     * normally not allowed before a user sees it.
     */
    fun applyToGame(state: Game)

    /** Initializes the starting context for the rule */
    fun initialize(state: Game): ChallengeContext?

    /**
     * Evaluates the rule against the current game state.
     */
    fun evaluate(
        state: Game,
        delta: GameDelta,
        contexts: ChallengeContextHolder,
    ): RuleProgress
}
