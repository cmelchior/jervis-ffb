package com.jervisffb.engine.bb2025.challenge.rule

import com.jervisffb.engine.ActionRequest
import com.jervisffb.engine.GameDelta
import com.jervisffb.engine.actions.D6Result
import com.jervisffb.engine.actions.Dice
import com.jervisffb.engine.actions.DiceFaces
import com.jervisffb.engine.actions.RollDice
import com.jervisffb.engine.challenge.ChallengeContext
import com.jervisffb.engine.challenge.ChallengeContextHolder
import com.jervisffb.engine.challenge.ChallengeRule
import com.jervisffb.engine.challenge.RuleProgress
import com.jervisffb.engine.model.Game
import com.jervisffb.engine.rules.DiceRollType
import com.jervisffb.engine.rules.policy.ActionFilterContext
import com.jervisffb.engine.rules.policy.ActionFilterPolicy
import com.jervisffb.engine.rules.policy.GameRulePolicy

/**
 * Challenge rule that forces single D6 rolls to show [forcedResult].
 *
 * If [rollTypes] is `null`, all roll types are restricted. Otherwise, only
 * the listed roll types are restricted.
 */
class RestrictedSingleD6DiceRollRule(
    val forcedResult: D6Result,
    val rollTypes: List<DiceRollType>? = null,
) : ChallengeRule, ActionFilterPolicy {

    constructor(forcedResult: D6Result) : this(forcedResult, null)
    constructor(forcedResult: D6Result, rollType: DiceRollType) : this(forcedResult, listOf(rollType))

    override val description: String = when (rollTypes != null) {
        true -> "All ${rollTypes.joinToString(", ")} rolls will roll a ${forcedResult.value}"
        false -> "All D6 rolls will roll a ${forcedResult.value}."
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
                if (descriptor is RollDice && shouldRestrict(descriptor)) {
                    RollDice(
                        *descriptor.dice.toTypedArray(),
                        type = descriptor.type,
                        allowedFaces = listOf(DiceFaces.of(forcedResult.value)),
                    )
                } else {
                    descriptor
                }
            },
        )
    }

    private fun shouldRestrict(descriptor: RollDice): Boolean {
        return descriptor.dice.size == 1
            && descriptor.dice.single() == Dice.D6
            && (rollTypes == null || descriptor.type in rollTypes)
    }
}
