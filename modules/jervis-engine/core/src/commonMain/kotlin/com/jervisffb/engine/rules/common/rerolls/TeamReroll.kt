package com.jervisffb.engine.rules.common.rerolls

import com.jervisffb.engine.fsm.Procedure
import com.jervisffb.engine.model.Game
import com.jervisffb.engine.model.TeamId
import com.jervisffb.engine.rules.DiceRollType
import com.jervisffb.engine.rules.common.procedures.DieRoll
import com.jervisffb.engine.rules.common.skills.Duration
import com.jervisffb.engine.rules.common.skills.RerollSource

/**
 * Interface describing all types of Team Rerolls.
 */
interface TeamReroll : RerollSource {
    val teamId: TeamId
    // Whether this reroll can be carried over into overtime if it isn't used in the half
    val carryOverIntoOvertime: Boolean
    // When is this reroll removed from the Team, regardless of it being used or not
    val duration: Duration
    // Separate from "used". If a Team re-roll is disabled, it means it cannot be used
    // "right now", but might become available later.
    var enabled: Boolean

    // Which procedure controls using this reroll
    override val rerollProcedure: Procedure

    override fun canReroll(
        state: Game,
        type: DiceRollType,
        dicePool: List<DieRoll<*>>,
        wasSuccess: Boolean?,
    ): Boolean {
        if (!isApplicableTo(state, type, dicePool, wasSuccess)) return false
        if (rerollUsed) return false
        if (state.activeTeam?.usedRerollThisTurn == true && !state.rules.allowMultipleTeamRerollsPrTurn) return false
        return dicePool.all { it.rerollSource == null }
    }

    override fun isApplicableTo(
        state: Game,
        type: DiceRollType,
        dicePool: List<DieRoll<*>>,
        wasSuccess: Boolean?,
    ): Boolean {
        if (!enabled) return false
        if (state.activeTeam?.id != teamId) return false
        if (!state.canUseTeamRerolls) return false
        return state.rules.canBeRerolledByTeamReroll(type)
    }

    override fun calculateRerollOptions(
        type: DiceRollType,
        value: List<DieRoll<*>>,
        wasSuccess: Boolean?,
    ): List<DiceRerollOption> {
        return listOf(DiceRerollOption(this.id, value))
    }
}
