package com.jervisffb.engine.statistics.probability

import com.jervisffb.engine.model.RerollSourceId
import kotlinx.serialization.Serializable

/**
 * A re-roll that was available to the coach, modelled as a resource that can be
 * spent once across the rolls that list it in [RollChance.eligibleRerolls].
 *
 * Every re-roll is modelled this way, including skill re-rolls. Most of them
 * really are shared: Dodge and Sure Feet reset at the end of the turn and Pro
 * at the end of an activation, so a player making three dodges still only has
 * one Dodge re-roll. A genuinely dedicated re-roll like Sure Hands or Catch is
 * just the case where a single roll lists it.
 *
 * [activationProbability] covers re-rolls that are not certain to happen:
 * `(7 - X) / 6` for a Loner(X+) player using a Team Re-roll, and
 * `(7 - Rules.proSuccessTarget) / 6` for Pro. Callers work the value out, so
 * that no ruleset specifics leak into the maths.
 */
@Serializable
data class RerollResource(
    val id: RerollSourceId,
    val activationProbability: Probability = Probability.ALWAYS,
) {
    init {
        require(activationProbability > Probability.NEVER && activationProbability <= Probability.NEVER) {
            "Activation probability must be in (0, 1]: $activationProbability"
        }
    }
}
