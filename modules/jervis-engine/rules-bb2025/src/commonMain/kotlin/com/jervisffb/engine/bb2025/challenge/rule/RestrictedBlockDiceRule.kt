package com.jervisffb.engine.bb2025.challenge.rule

import com.jervisffb.engine.ActionRequest
import com.jervisffb.engine.GameDelta
import com.jervisffb.engine.actions.DBlockResult
import com.jervisffb.engine.actions.DiceFaces
import com.jervisffb.engine.actions.RollDice
import com.jervisffb.engine.challenge.ChallengeContext
import com.jervisffb.engine.challenge.ChallengeContextHolder
import com.jervisffb.engine.challenge.ChallengeRule
import com.jervisffb.engine.challenge.RuleProgress
import com.jervisffb.engine.common.context.BlockContext
import com.jervisffb.engine.model.Game
import com.jervisffb.engine.model.context.getContext
import com.jervisffb.engine.rules.DiceRollType
import com.jervisffb.engine.rules.policy.ActionFilterContext
import com.jervisffb.engine.rules.policy.ActionFilterPolicy
import com.jervisffb.engine.rules.policy.GameRulePolicy

/**
 * Challenge rule that forces all block dice to roll [forcedResult], but only
 * if [diceCount] number of dice was rolled.
 *
 * [diceCount] can either be [1, 3] (for rolling 1 to 3 dice with the attacker
 * choosing) or [-2, -3] (for rolling 2 or 3 dice with the defender choosing).
 */
class RestrictedBlockDiceRule(
    val forcedResult: DBlockResult,
    val diceCount: Int
) : ChallengeRule, ActionFilterPolicy {

    init {
        require(diceCount in listOf(1, 2, 3, -2, -3)) { "diceCount must be in [1, 3] or [-2, -3]: $diceCount" }
    }

    override val description: String = buildString {
        when (diceCount > 0) {
            true -> append("Always roll ${forcedResult.blockResult.description} when rolling $diceCount or more Block dice in your favour.")
            false -> append("Always roll ${forcedResult.blockResult.description} when rolling ${-diceCount} or more Block dice in opponents favour.")
        }
    }

    override val policies: List<GameRulePolicy> = listOf(this)

    override fun applyToGame(state: Game) {
        // Do nothing
    }

    override fun initialize(state: Game): ChallengeContext? = null

    override fun evaluate(
        state: Game,
        delta: GameDelta,
        contexts: ChallengeContextHolder,
    ): RuleProgress = RuleProgress(ruleBroken = false, updatedContext = null)

    override fun filterRequest(context: ActionFilterContext, request: ActionRequest): ActionRequest {
        return request.copy(
            actions = request.actions.map { descriptor ->
                if (
                    descriptor is RollDice
                    && descriptor.type == DiceRollType.BLOCK
                    && context.state.getContext<BlockContext>().calculateNoOfBlockDice() == diceCount
                ) {
                    RollDice(
                        *descriptor.dice.toTypedArray(),
                        type = descriptor.type,
                        allowedFaces = descriptor.dice.map { DiceFaces.of(forcedResult.value) },
                    )
                } else {
                    descriptor
                }
            },
        )
    }
}
