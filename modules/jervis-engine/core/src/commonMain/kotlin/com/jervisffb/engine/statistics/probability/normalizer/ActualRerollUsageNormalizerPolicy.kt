package com.jervisffb.engine.statistics.probability.normalizer

import com.jervisffb.engine.actions.D6Result
import com.jervisffb.engine.rules.DiceRollType
import com.jervisffb.engine.statistics.probability.event.ActionPathEvent
import com.jervisffb.engine.statistics.probability.event.ActualRerollUse
import com.jervisffb.engine.statistics.probability.event.PhysicalD6Role
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
        val byId = diceRolls.associateBy { it.index }
        val replacements = diceRolls.groupBy { it.rerolledRollId }
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
                        observation.rollType == DiceRollType.BLOCK && observation.rerolledRollId == null -> add(
                            normalizeBlock(observation, diceRolls, replacements),
                        )
                        observation.rollType == DiceRollType.BLOCK -> Unit
                        observation.rollType in ignoredD6Rolls -> Unit
                        observation.rollType in supportedPrimaryD6Rolls ||
                            observation.rollType in activationD6Rolls -> add(
                            normalizePhysicalD6(observation, byId),
                        )
                        observation.rerolledRollId != null -> Unit
                        else -> add(unsupported(observation, "Roll type is not supported by hybrid scoring."))
                    }
                }
            }
        }
        return normalized.resequence()
    }

    private fun normalizePhysicalD6(
        roll: ChanceObservation.DiceRoll,
        observations: Map<Int, ChanceObservation.DiceRoll>,
    ): ActionPathEvent {
        val result = roll.dice.singleOrNull()?.result as? D6Result
            ?: return unsupported(roll, "Expected one physical D6 result.")
        if (!roll.finalized) return unsupported(roll, "Physical D6 roll was not finalized.")

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
            null -> recoveries(roll.rerollOptions, roll.success)
            else -> RecoveryConversion(emptyList())
        }
        recoveryResult.reason?.let { return unsupported(roll, it) }

        return ActionPathEvent.PhysicalD6(
            index = roll.index,
            traceRootSequence = traceRoot(roll, observations),
            rollType = roll.rollType,
            owner = roll.teamId,
            selectedValue = result,
            role = when {
                roll.rerolledRollId != null -> PhysicalD6Role.REROLL
                roll.rollType in activationD6Rolls -> PhysicalD6Role.ACTIVATION
                else -> PhysicalD6Role.PRIMARY
            },
            scope = roll.scope.toFixedLineScope(),
            observedSuccess = if (actualRecovery == null) roll.success else null,
            actualRecovery = actualRecovery,
            recoveries = recoveryResult.options,
            finalized = true,
        )
    }

    private fun traceRoot(
        roll: ChanceObservation.DiceRoll,
        observations: Map<Int, ChanceObservation.DiceRoll>,
    ): Int {
        var current = roll
        while (current.rerolledRollId != null) {
            current = observations[current.rerolledRollId] ?: break
        }
        return current.index
    }
}
