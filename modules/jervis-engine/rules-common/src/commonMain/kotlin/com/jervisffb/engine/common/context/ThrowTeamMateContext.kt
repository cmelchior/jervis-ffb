package com.jervisffb.engine.common.context

import com.jervisffb.engine.common.procedures.actions.throwteammate.ThrowPlayerResult
import com.jervisffb.engine.model.Player
import com.jervisffb.engine.model.context.ProcedureContext
import com.jervisffb.engine.model.locations.PitchCoordinate
import com.jervisffb.engine.model.modifiers.DiceModifier
import com.jervisffb.engine.rules.common.procedures.D6DieRoll
import com.jervisffb.engine.rules.common.tables.Range
import com.jervisffb.engine.utils.sum
import kotlinx.collections.immutable.PersistentList
import kotlinx.collections.immutable.persistentListOf

data class ThrowTeamMateContext(
    val thrower: Player,
    val thrownPlayer: Player? = null,
    val hasMoved: Boolean = false,
    // Target of the throw in the current step. This means it will be updated when the ball scatters, deviates, etc.
    val target: PitchCoordinate? = null,
    val range: Range? = null,
    val qualityRoll: D6DieRoll? = null,
    val qualityRollModifiers: PersistentList<DiceModifier> = persistentListOf(),
    val qualityRollResult: ThrowPlayerResult? = null,

    // BB2020: If a player without TZ or prone/stunned are thrown they will bounce one
    // extra time before landing. They will automatically fail the landing roll. This is called
    // Crash Landing.
    // BB2025: If a player without TZ or prone/stunned is thrown, they will just automatically
    // fail the landing roll. This is not a named concept, instea "Crash Landing" is only used
    // as a side-remark for landing on another player.
    // In Jervis, we use "Crash Landing" to mean being thrown while being Distracted, Prone or
    // Stunned across all rulesets.
    val willCrashLand: Boolean = false,
    // If a player bounces on another player, the result when they land differs between rulesets.
    // - in BB2020, the rulebook says they will Fall Down, but it was errata'ed to a Knock Down
    // - in BB2025, the rulebook says they will Fall Over, and no errata currently exist.
    val fallOverWhenLanding: Boolean = false,
    val knockedDownWhenLanding: Boolean = false,
    // If the player scattered, deviated or bounced into the crowd while holding the ball.
    // The ball should be thrown in from this square.
    val outOfBoundsAt: PitchCoordinate? = null
) : ProcedureContext {
    val isQualityRollSuccess: Boolean
        get() {
            val pa = thrower.passing
            val roll = qualityRoll
            if (pa == null || pa == 0) return false
            if (roll == null) return false
            return pa >= roll.result.value + qualityRollModifiers.sum()
        }
}
