package com.jervisffb.engine.statistics.probability.normalizer

import com.jervisffb.engine.actions.D6Result
import com.jervisffb.engine.rules.DiceRollType
import com.jervisffb.engine.statistics.probability.event.ActionPathEvent
import com.jervisffb.engine.statistics.probability.observation.ChanceObservation

/**
 * Normalizes chance observations while always collapsing rolls and rerolls into
 * a single dice event, even if this was the actual choice made during the game
 *
 * This allows a scorer to distribute rerolls more optimally than what
 * was actually done during the game.
 */
object FixedRerollUsageNormalizerPolicy : AbstractChanceNormalizerPolicy() {

    override fun normalize(observations: List<ChanceObservation>): List<ActionPathEvent> {
        val scoreableObservations = observations.withoutTerminalUnfinalizedRollFamily()
        val diceRolls = scoreableObservations.filterIsInstance<ChanceObservation.DiceRoll>()
        val replacements = diceRolls.groupBy { it.rerolledRollIndex }
        val normalized = buildList {
            scoreableObservations.forEach { observation ->
                when (observation) {
                    is ChanceObservation.UnstructuredAction -> add(
                        ActionPathEvent.Unsupported(
                            observation.index,
                            reason = "${observation.nodeDescription} emitted ${observation.actionName} without structured dice data.",
                        ),
                    )
                    is ChanceObservation.DiceRoll -> when {
                        observation.rerolledRollIndex != null -> Unit
                        observation.enclosingRollIndex != null -> Unit
                        observation.rollType in ignoredRollTypes -> Unit
                        observation.rollType == DiceRollType.BLOCK -> add(normalizeLogicalBlock(observation, replacements))
                        observation.rollType in activationRollTypes -> Unit
                        observation.outcome != null -> add(
                            normalizeLogicalOutcome(
                                root = observation,
                                finalRoll = finalReplacement(observation, replacements),
                            ),
                        )
                        observation.rollType in primaryRollTypes -> add(
                            normalizeLogicalD6(observation, replacements),
                        )
                        else -> add(unsupported(observation, "Roll type is not supported by fixed-line scoring."))
                    }
                }
            }
        }
        return normalized.reindex()
    }

    private fun normalizeLogicalD6(
        root: ChanceObservation.DiceRoll,
        replacements: Map<Int?, List<ChanceObservation.DiceRoll>>,
    ): ActionPathEvent {
        val finalRoll = finalReplacement(root, replacements)
        if (!root.finalized || !finalRoll.finalized) {
            return unsupported(root, "D6 roll was not finalized.")
        }
        val result = finalRoll.dice.singleOrNull()?.result as? D6Result
            ?: return unsupported(root, "Expected one D6 result.")
        val success = finalRoll.success
            ?: return unsupported(root, "The roll does not expose a factual success result.")
        val legalValues = if (success) 2..6 else 1..5
        if (result.value !in legalValues) {
            return unsupported(root, "D6 value ${result.value} is inconsistent with success=$success.")
        }
        val recoveryResult = recoveries(root.rerollOptions, success)
        recoveryResult.reason?.let { return unsupported(root, it) }
        return ActionPathEvent.Logical.die(
            index = root.index,
            rollType = root.rollType,
            owner = root.team,
            result = result,
            isSuccess = success,
            scope = root.scope.toFixedLineScope(),
            recoveries = recoveryResult.options,
        )
    }

    private fun finalReplacement(
        root: ChanceObservation.DiceRoll,
        replacements: Map<Int?, List<ChanceObservation.DiceRoll>>,
    ): ChanceObservation.DiceRoll {
        var current = root
        val seen = mutableSetOf(root.index)
        while (true) {
            val replacement = replacements[current.index].orEmpty().maxByOrNull { it.index } ?: return current
            if (!seen.add(replacement.index)) return current
            current = replacement
        }
    }
}
