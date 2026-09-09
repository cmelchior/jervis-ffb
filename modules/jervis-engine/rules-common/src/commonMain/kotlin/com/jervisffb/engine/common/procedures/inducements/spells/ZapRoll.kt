package com.jervisffb.engine.common.procedures.inducements.spells

import com.jervisffb.engine.actions.D6Result
import com.jervisffb.engine.commands.Command
import com.jervisffb.engine.common.context.ResolveInducementEffectsContext
import com.jervisffb.engine.common.procedures.dicerolls.D6WithTeamRerollProcedure
import com.jervisffb.engine.common.procedures.dicerolls.TeamRerollData
import com.jervisffb.engine.fsm.Node
import com.jervisffb.engine.model.Game
import com.jervisffb.engine.model.Player
import com.jervisffb.engine.model.Team
import com.jervisffb.engine.model.context.ProcedureContext
import com.jervisffb.engine.model.context.assertContext
import com.jervisffb.engine.model.context.getContext
import com.jervisffb.engine.rules.DiceRollType
import com.jervisffb.engine.rules.Rules
import com.jervisffb.engine.rules.common.procedures.D6DieRoll

/**
 * Control rolling the die for the ZAP! spell.
 *
 * See page 149 in the BB2025 rulebook.
 */
object ZapRoll: D6WithTeamRerollProcedure() {
    override val rollType: DiceRollType = DiceRollType.ZAP
    override val initialNode: Node get() = RollDie
    override fun onEnterRollProcedure(state: Game, rules: Rules): Command? = null
    override fun onExitRollProcedure(state: Game, rules: Rules): Command? = null
    override fun isValid(state: Game, rules: Rules) = state.assertContext<ZapContext>()
    override fun getActionOwner(state: Game): Team = state.getContext<ResolveInducementEffectsContext>().team

    override val RollDie = object : AbstractRollDie() {
        override fun updateContext(state: Game, rules: Rules, d6: D6Result): ProcedureContext {
            val context = state.getContext<ZapContext>()
            val player = context.target
            val isTransformed = isSuccess(player, d6)
            return context.copy(
                roll = D6DieRoll.create(d6),
                isSuccess = isTransformed,
            )
        }
    }

    override val ChooseReRollSource = object : AbstractChooseRerollSource() {
        override fun getRerollData(state: Game, rules: Rules): TeamRerollData {
            val inducementsContext = state.getContext<ResolveInducementEffectsContext>()
            val context = state.getContext<ZapContext>()
            return TeamRerollData(inducementsContext.team, context.roll!!, context.isSuccess)
        }
    }

    override val ReRollDie = object : AbstractReRollDie() {
        override fun updateContext(state: Game, rules: Rules, d6: D6Result): ProcedureContext {
            val context = state.getContext<ZapContext>()
            return context.copy(
                roll = context.roll!!.copyReroll(
                    rerollSource = state.getRerollContext().source,
                    rerolledResult = d6,
                ),
                isSuccess = isSuccess(context.target, d6)
            )
        }
    }

    private fun isSuccess(target: Player, roll: D6Result): Boolean {
        val target = target.strength
        return roll.value >= target
    }
}
