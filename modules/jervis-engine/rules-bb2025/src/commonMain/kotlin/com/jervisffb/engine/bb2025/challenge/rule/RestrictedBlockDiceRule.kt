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
 * if [diceType] number of dice was rolled.
 *
 * If [diceType] is null, then the rule will be applied to all dice rolls.
 */
class RestrictedBlockDiceRule(
    val forcedResult: DBlockResult,
    val diceType: RollType? = null,
) : ChallengeRule, ActionFilterPolicy {

    enum class RollType {
        OPPONENT_CHOOSES, // [-2, -3]
        SINGLE_DIE, // [1]
        ATTACKER_CHOOSES, // [2, 3]
    }

    override val description: String = buildString {
        val label = when (diceType) {
            RollType.ATTACKER_CHOOSES -> "Always rolls ${forcedResult.blockResult.description} when rolling 2 or more Block dice in your favour."
            RollType.SINGLE_DIE -> "Always rolls ${forcedResult.blockResult.description} when rolling 1 Block dice."
            RollType.OPPONENT_CHOOSES -> "Always rolls ${forcedResult.blockResult.description} when rolling 2 or more Block dice in your opponents favour."
            null -> "All Block dice will roll ${forcedResult.blockResult.description}."
        }
        append(label)
    }

    override val isMultipleAllowed: Boolean = true
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
                    && getDiceType(context) == diceType
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

    private fun getDiceType(context: ActionFilterContext): RollType {
        val dice = context.state.getContext<BlockContext>().calculateNoOfBlockDice()
        return when {
            dice < 0 -> RollType.OPPONENT_CHOOSES
            dice == 1 -> RollType.SINGLE_DIE
            dice > 1 -> RollType.ATTACKER_CHOOSES
            else -> error("Unsupported value: $dice")
        }
    }
}
