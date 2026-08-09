package com.jervisffb.engine.statistics.probability.normalizer

import com.jervisffb.engine.actions.D6Result
import com.jervisffb.engine.rules.DiceRollType
import com.jervisffb.engine.statistics.probability.event.ActionPathEvent
import com.jervisffb.engine.statistics.probability.event.ActualRerollUse
import com.jervisffb.engine.statistics.probability.event.PhysicalRollRole
import com.jervisffb.engine.statistics.probability.observation.ChanceObservation

/**
 * Normalizes chance observations while treating a reroll selected during a
 * game as an actual dice event, i.e., one that was supposed to happen.
 *
 * While more accurate, it also always results in a worse score if this was
 * collapsed into a single event.
 */
object ActualRerollUsageNormalizerPolicy : AbstractChanceNormalizerPolicy() {

    override fun normalize(observations: List<ChanceObservation>): List<ActionPathEvent> {
        val scoreableObservations = observations.withoutTerminalUnfinalizedRollFamily()
        val diceRolls = scoreableObservations.filterIsInstance<ChanceObservation.DiceRoll>()
        val byIndex = diceRolls.associateBy { it.index }
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
                        observation.rollType == DiceRollType.BLOCK && observation.rerolledRollIndex == null -> add(
                            normalizePhysicalBlock(observation, replacements),
                        )
                        observation.rollType == DiceRollType.BLOCK -> Unit
                        observation.rollType in ignoredD6Rolls -> Unit
                        observation.outcome != null -> add(
                            normalizePhysicalOutcome(observation, byIndex),
                        )
                        observation.rollType in supportedPrimaryD6Rolls ||
                            observation.rollType in activationD6Rolls -> add(
                            normalizePhysicalD6(observation, byIndex),
                        )
                        observation.rerolledRollIndex != null -> Unit
                        else -> add(unsupported(observation, "Roll type is not supported by hybrid scoring."))
                    }
                }
            }
        }
        return normalized.reindex()
    }

    private fun normalizePhysicalD6(
        roll: ChanceObservation.DiceRoll,
        observations: Map<Int, ChanceObservation.DiceRoll>,
    ): ActionPathEvent {
        val result = roll.dice.singleOrNull()?.result as? D6Result
            ?: return unsupported(roll, "Expected one physical D6 result.")
        if (!roll.finalized) return unsupported(roll, "Physical D6 roll was not finalized.")
        val isSuccess = roll.success
            ?: return unsupported(roll, "Physical D6 roll does not expose a factual success result.")

        val selectedSource = roll.selectedReroll?.takeUnless { it.aborted }?.let { selection ->
            roll.rerollOptions.firstOrNull { it.source.id == selection.sourceId }?.source
                ?: return unsupported(roll, "Selected reroll source ${selection.sourceId.id} was not snapshotted.")
        }
        val sourceResult = selectedSource?.toRecoveryResource()
        sourceResult?.reason?.let { return unsupported(roll, it) }
        val actualRecovery = selectedSource?.let { source ->
            ActualRerollUse(sourceResult!!.resource!!, source.description)
        }
        val recoveryResult = when (actualRecovery) {
            null -> recoveries(roll.rerollOptions, isSuccess)
            else -> RecoveryConversion(emptyList())
        }
        recoveryResult.reason?.let { return unsupported(roll, it) }

        return ActionPathEvent.Physical.die(
            index = roll.index,
            traceRootIndex = traceRoot(roll, observations),
            rollType = roll.rollType,
            owner = roll.teamId,
            result = result,
            role = when {
                roll.rerolledRollIndex != null -> PhysicalRollRole.REROLL
                roll.rollType in activationD6Rolls -> PhysicalRollRole.ACTIVATION
                else -> PhysicalRollRole.PRIMARY
            },
            scope = roll.scope.toFixedLineScope(),
            isSuccess = isSuccess,
            actualRecovery = actualRecovery,
            recoveries = recoveryResult.options,
            finalized = true,
        )
    }
}
