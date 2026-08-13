package com.jervisffb.engine.challenge

import com.jervisffb.engine.GameDelta
import com.jervisffb.engine.GameEngineController
import com.jervisffb.engine.actions.GameAction
import com.jervisffb.engine.model.Game
import com.jervisffb.engine.rules.policy.GameRulePolicy

/**
 * A challenge restriction layered on top of the normal rules of the game.
 */
interface ChallengeRule {

    /** One line, shown in the "Rules" list on the challenge details page. */
    val description: String

    /** Can different instances of this rule be applied to a game */
    val isMultipleAllowed: Boolean

    /** Any extra rules policies this challenge puts on top of the normal game rules */
    val policies: List<GameRulePolicy>
        get() = emptyList()

    /**
     * Called after the [GameEngineController] is started and initial actions
     * have been applied, but before any other actions can be applied to the
     * game state.
     *
     * This allows a [ChallengeRule] to modify the game state in a way that is
     * normally not allowed before a user sees it.
     */
    fun applyToGame(state: Game)

    /**
     * Initializes the starting context for the rule. Called just before control
     * is handed to the coach, but after all initial actions have been applied.
     * Can be used to track state, not otherwise found in [Game].
     */
    fun initialize(state: Game): ChallengeContext?

    /**
     * Evaluates the rule against the current game state after a [GameAction]
     * has been applied.
     */
    fun evaluate(
        state: Game,
        delta: GameDelta,
        contexts: ChallengeContextHolder,
    ): RuleProgress
}
