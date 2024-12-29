package com.jervisffb.engine.rules.bb2020.skills

import com.jervisffb.engine.fsm.Procedure
import com.jervisffb.engine.model.Player
import com.jervisffb.engine.model.Team
import com.jervisffb.engine.rules.PlayerSpecialActionType
import com.jervisffb.engine.rules.bb2020.procedures.DieRoll
import com.jervisffb.engine.rules.bb2020.procedures.UseStandardSkillReroll
import com.jervisffb.engine.rules.bb2020.procedures.UseTeamReroll
import kotlinx.serialization.Serializable
import kotlin.jvm.JvmInline

@Serializable
sealed interface TeamReroll : RerollSource {
    val carryOverIntoOvertime: Boolean
    // When is this reroll removed from the Team, regardless of it being used or not
    val duration: Duration
    override val rerollProcedure: Procedure
        get() = UseTeamReroll

    override fun canReroll(
        type: DiceRollType,
        value: List<DieRoll<*>>,
        wasSuccess: Boolean?,
    ): Boolean {
        // TODO Some types cannot be rerolled
        return value.all { it.rerollSource == null }
    }

    override fun calculateRerollOptions(
        type: DiceRollType,
        value: List<DieRoll<*>>,
        wasSuccess: Boolean?,
    ): List<DiceRerollOption> {
        return listOf(DiceRerollOption(this, value))
    }
}

@Serializable
class RegularTeamReroll(val index: Int) : TeamReroll {
    override val id: RerollSourceId =
        RerollSourceId("team-reroll-$index")
    override val carryOverIntoOvertime: Boolean = true
    override val duration = Duration.PERMANENT
    override val rerollResetAt: Duration =
        Duration.END_OF_HALF
    override val rerollDescription: String = "Team reroll"
    override var rerollUsed: Boolean = false
}

@Serializable
class LeaderTeamReroll(val player: Player) : TeamReroll {
    override val id: RerollSourceId =
        RerollSourceId("leader-${player.id.value}")
    override val carryOverIntoOvertime: Boolean = true
    override val duration = Duration.SPECIAL
    override val rerollResetAt: Duration =
        Duration.END_OF_HALF
    override val rerollDescription: String = "Team reroll (Leader)"
    override var rerollUsed: Boolean = false
}

@Serializable
class BrilliantCoachingReroll(val team: Team) : TeamReroll {
    override val id: RerollSourceId =
        RerollSourceId("brilliant-coaching-${team.id.value}")
    override val carryOverIntoOvertime: Boolean = false
    override val duration = Duration.END_OF_DRIVE
    override val rerollResetAt: Duration =
        Duration.END_OF_HALF
    override val rerollDescription: String = "Team Reroll (Brilliant Coaching)"
    override var rerollUsed: Boolean = false
}

// Should we split this into a "normal dice" and "block dice" interface?
interface RerollSource {
    val id: RerollSourceId // Unique identifier for this reroll. Should only be unique within a single team.
    val rerollResetAt: Duration
    val rerollDescription: String
    var rerollUsed: Boolean
    val rerollProcedure: Procedure

    // Returns `true` if `calculateRerollOptions` will return a non-empty list
    fun canReroll(type: DiceRollType, value: List<DieRoll<*>>, wasSuccess: Boolean? = null): Boolean

    fun calculateRerollOptions(
        // What kind of dice roll
        type: DiceRollType,
        // All dice part of the roll
        value: List<DieRoll<*>>,
        // If the roll was "successful" (as some skills only allow rerolls if unsuccessful). For some roll types
        // this concept doesn't make sense, like Block rolls or rolling for a table result.
        wasSuccess: Boolean? = null,
    ): List<DiceRerollOption>

    // Helper method, for just rolling a single dice. Which is by far, the most common scenario.
    fun calculateRerollOptions(type: DiceRollType, value: DieRoll<*>, wasSuccess: Boolean?): List<DiceRerollOption> =
        calculateRerollOptions(
            type,
            listOf(value),
            wasSuccess,
        )
}

interface D6StandardSkillReroll : RerollSource {
    override val rerollProcedure: Procedure
        get() = UseStandardSkillReroll

    override fun calculateRerollOptions(
        type: DiceRollType,
        value: List<DieRoll<*>>,
        wasSuccess: Boolean?,
    ): List<DiceRerollOption> {
        // For standard skills
        if (value.size != 1) error("Unsupported number of dice: ${value.joinToString()}")
        return listOf(DiceRerollOption(this, value))
    }
}

/**
 * Interface for skills that provide a special action.
 * Note, for skills that replace blocks, they procedure being referenced is the Standalone
 * variant. Special skills that can be used as part of a Multiple Block
 * are defined in [com.jervisffb.rules.bb2020.procedures.actions.block.MultipleBlockContext] and will
 * be handled separately there.
 */
interface SpecialActionProvider {
    val specialAction: PlayerSpecialActionType
    var isSpecialActionUsed: Boolean
}

@Serializable
data class DiceRerollOption(
    val source: RerollSource,
    val dice: List<DieRoll<*>>,
) {
    constructor(source: RerollSource, dieRoll: DieRoll<*>): this(source, listOf(dieRoll))
}

// When does the "used" state reset?
enum class Duration {
    IMMEDIATE, // The effect expires immediately.
    START_OF_ACTIVATION, // The effect expires when the player is activated
    END_OF_ACTIVATION, // The effect expires at the end of the current players activation
    END_OF_ACTION, // The effect expires at the end of the action, this also includes subactions, like the Block part of a Blitz.
    END_OF_TURN, // The effect expires at the end of the current teams turn.
    END_OF_DRIVE, // The effect expires at the end of the current drive
    END_OF_HALF, // The effect expires at the end of the current half
    END_OF_GAME, // The effect lasts for the entire game, but doesn't carry over to the next game
    SPECIAL, // The duration of this effect is too hard to put into a bucket and must be handled manually.
    STANDING_UP, // The effect expires when the player is going from prone to standing up.
    PERMANENT, // The effect is a permanent change to the team.
}

@Serializable
sealed interface Skill {
    // Unique identifier for this skill
    val skillId: String
    // Human readable name of this skill
    val name: String
    // Whether this skill is compulsory to use
    val compulsory: Boolean
    // Whether this skill count as being "used". The meaning of this is interpreted in the context it is used.
    // If the skill is always available, this should always be false.
    // Note, this specifically does not apply to a "reroll" part of a skill.
    var used: Boolean
    // Represents any value in brackes, like Might Blow(1+) or Loner(4+). It is up to the context to correctly interpret this value
    val value: Int?
    // When the `used` state reset back to `false`?
    val resetAt: Duration
    // Which category does this skill belong to?
    val category: SkillCategory
    // Whether this skill works when the player has lost its tackle zones
    val workWithoutTackleZones: Boolean
    // Whether this skill works when the player is prone or stunned
    val workWhenProne: Boolean
    // Whether this skill is temporary
    val isTemporary: Boolean
    val expiresAt: Duration
}

// TODO Not really liking this API. Is there a good way to serialize them?
//  Also it doesn't look nice when naming the skill in the Position list
//  Ideally this `listOf(SureHands)`, not `listOf(SureHands.Factory)`
@Serializable
sealed interface SkillFactory {
    val value: Int?
    fun createSkill(isTemporary: Boolean = false, expiresAt: Duration = Duration.PERMANENT): Skill
}

interface SkillCategory {
    val id: Long
    val description: String
}

@Serializable
sealed interface BB2020Skill : Skill
