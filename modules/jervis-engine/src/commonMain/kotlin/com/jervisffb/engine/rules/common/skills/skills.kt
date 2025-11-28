package com.jervisffb.engine.rules.common.skills

import com.jervisffb.engine.fsm.Procedure
import com.jervisffb.engine.model.Game
import com.jervisffb.engine.model.RerollSourceId
import com.jervisffb.engine.model.TeamId
import com.jervisffb.engine.rules.DiceRollType
import com.jervisffb.engine.rules.common.procedures.DieRoll
import com.jervisffb.engine.rules.common.procedures.UseStandardSkillReroll
import com.jervisffb.engine.rules.common.procedures.UseTeamReroll
import kotlinx.serialization.Serializable

sealed interface TeamReroll : RerollSource {
    val teamId: TeamId
    val carryOverIntoOvertime: Boolean
    // When is this reroll removed from the Team, regardless of it being used or not
    val duration: Duration
    override val rerollProcedure: Procedure
        get() = UseTeamReroll

    override fun canReroll(
        state: Game,
        type: DiceRollType,
        value: List<DieRoll<*>>,
        wasSuccess: Boolean?,
    ): Boolean {
        // TODO Some types cannot be rerolled
        if (state.activeTeam?.id != teamId) return false
        if (state.activeTeam?.usedRerollThisTurn == true && !state.rules.allowMultipleTeamRerollsPrTurn) return false
        return value.all { it.rerollSource == null }
    }

    override fun calculateRerollOptions(
        type: DiceRollType,
        value: List<DieRoll<*>>,
        wasSuccess: Boolean?,
    ): List<DiceRerollOption> {
        return listOf(DiceRerollOption(this.id, value))
    }
}

class RegularTeamReroll(override val teamId: TeamId, val index: Int) : TeamReroll {
    override val id: RerollSourceId = RerollSourceId("${teamId.value}-reroll-$index")
    override val carryOverIntoOvertime: Boolean = true
    override val duration = Duration.PERMANENT
    override val rerollResetAt: Duration = Duration.END_OF_HALF
    override val rerollDescription: String = "Team reroll"
    override var rerollUsed: Boolean = false
}

class LeaderTeamReroll(override val teamId: TeamId) : TeamReroll {
    override val id: RerollSourceId = RerollSourceId("${teamId.value}-leader")
    override val carryOverIntoOvertime: Boolean = true
    override val duration = Duration.SPECIAL
    override val rerollResetAt: Duration = Duration.END_OF_HALF
    override val rerollDescription: String = "Team reroll (Leader)"
    override var rerollUsed: Boolean = false
}

class BrilliantCoachingReroll(override val teamId: TeamId) : TeamReroll {
    override val id: RerollSourceId = RerollSourceId("${teamId.value}-brilliant-coaching")
    override val carryOverIntoOvertime: Boolean = false
    override val duration = Duration.END_OF_DRIVE
    override val rerollResetAt: Duration = Duration.END_OF_DRIVE
    override val rerollDescription: String = "Team Reroll (Brilliant Coaching)"
    override var rerollUsed: Boolean = false
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
        return listOf(DiceRerollOption(this.id, value))
    }
}

@Serializable
data class DiceRerollOption(
    val rerollId: RerollSourceId,
    val dice: List<DieRoll<*>>,
) {
    constructor(rerollId: RerollSourceId, dieRoll: DieRoll<*>): this(rerollId, listOf(dieRoll))
    constructor(source: RerollSource, dieRoll: DieRoll<*>): this(source.id, listOf(dieRoll))

    fun getRerollSource(game: Game): RerollSource {
        return game.getRerollSourceById(rerollId)
    }
}
