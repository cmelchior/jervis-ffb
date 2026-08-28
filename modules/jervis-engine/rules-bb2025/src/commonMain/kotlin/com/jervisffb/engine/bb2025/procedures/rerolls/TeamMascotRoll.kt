package com.jervisffb.engine.bb2025.procedures.rerolls

import com.jervisffb.engine.actions.D6Result
import com.jervisffb.engine.bb2025.context.MascotRollContext
import com.jervisffb.engine.commands.Command
import com.jervisffb.engine.common.procedures.dicerolls.D6WithTeamRerollProcedure
import com.jervisffb.engine.common.procedures.dicerolls.TeamRerollData
import com.jervisffb.engine.fsm.Node
import com.jervisffb.engine.model.Game
import com.jervisffb.engine.model.Team
import com.jervisffb.engine.model.context.ProcedureContext
import com.jervisffb.engine.model.context.assertContext
import com.jervisffb.engine.model.context.getContext
import com.jervisffb.engine.rules.DiceRollType
import com.jervisffb.engine.rules.Rules
import com.jervisffb.engine.rules.common.procedures.D6DieRoll
import com.jervisffb.engine.rules.common.rerolls.DiceRerollOption

/**
 * Procedure controlling the Mascot roll, i.e., when a team checks to see if
 * its Team Mascot reroll works.
 */
object TeamMascotRoll : D6WithTeamRerollProcedure() {
    override val rollType: DiceRollType = DiceRollType.TEAM_MASCOT
    override val initialNode: Node get() = RollDie

    override fun onEnterRollProcedure(state: Game, rules: Rules): Command? = null
    override fun onExitRollProcedure(state: Game, rules: Rules): Command? = null
    override fun isValid(state: Game, rules: Rules) = state.assertContext<MascotRollContext>()
    override fun getActionOwner(state: Game): Team = state.getContext<MascotRollContext>().team

    override val RollDie = object : AbstractRollDie() {
        override fun updateContext(state: Game, rules: Rules, d6: D6Result): ProcedureContext {
            val context = state.getContext<MascotRollContext>()
            return context.copy(
                roll = D6DieRoll.create(d6),
                isSuccess = isSuccess(d6),
            )
        }
    }

    override val ChooseReRollSource = object : AbstractChooseRerollSource() {
        override fun getRerollData(state: Game, rules: Rules): TeamRerollData {
            val context = state.getContext<MascotRollContext>()
            return TeamRerollData(context.team, context.roll!!, context.isSuccess)
        }

        override fun getAvailableRerolls(
            state: Game,
            rules: Rules,
            rerollData: TeamRerollData,
        ): List<DiceRerollOption> {
            // A Team Mascot roll cannot itself be rerolled. A failed Mascot may
            // instead be replaced by another team reroll in TeamMascotStep.
            return emptyList()
        }
    }

    override val ReRollDie = object : AbstractReRollDie() {
        override fun updateContext(state: Game, rules: Rules, d6: D6Result): ProcedureContext {
            val context = state.getContext<MascotRollContext>()
            return context.copy(
                roll = context.roll!!.copyReroll(
                    rerollSource = state.getRerollContext().source,
                    rerolledResult = d6,
                ),
                isSuccess = isSuccess(d6),
            )
        }
    }

    private fun isSuccess(roll: D6Result): Boolean {
        return roll.value >= TeamMascotReroll.TARGET
    }
}
