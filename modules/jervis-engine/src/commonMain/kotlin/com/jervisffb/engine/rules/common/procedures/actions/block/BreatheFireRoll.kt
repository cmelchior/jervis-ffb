package com.jervisffb.engine.rules.common.procedures.actions.block

import com.jervisffb.engine.actions.D6Result
import com.jervisffb.engine.commands.Command
import com.jervisffb.engine.fsm.Node
import com.jervisffb.engine.model.Game
import com.jervisffb.engine.model.Team
import com.jervisffb.engine.model.context.ProcedureContext
import com.jervisffb.engine.model.context.assertContext
import com.jervisffb.engine.model.context.getContext
import com.jervisffb.engine.rules.DiceRollType
import com.jervisffb.engine.rules.Rules
import com.jervisffb.engine.rules.common.procedures.D6DieRoll
import com.jervisffb.engine.rules.common.procedures.actions.dicerolls.D6WithRerollProcedure
import com.jervisffb.engine.rules.common.procedures.actions.dicerolls.RerollData

/**
 * Implement the Breathe Fire Roll as described on page 126 in the BB2025
 * rulebook.
 *
 * The result is stored in [BreatheFireContext] and it is up to the caller to
 * determine what to do with the result.
 */
object BreatheFireRoll: D6WithRerollProcedure() {
    override val rollType: DiceRollType = DiceRollType.BREATHE_FIRE
    override val initialNode: Node get() = RollDie
    override fun onEnterProcedure(state: Game, rules: Rules): Command? = null
    override fun onExitProcedure(state: Game, rules: Rules): Command? = null
    override fun isValid(state: Game, rules: Rules) = state.assertContext<BreatheFireContext>()
    override fun getActionOwner(state: Game): Team = state.getContext<BreatheFireContext>().attacker.team

    override val RollDie = object : AbstractRollDie() {
        override fun updateContext(state: Game, rules: Rules, d6: D6Result): ProcedureContext {
            val context = state.getContext<BreatheFireContext>()
            return context.copy(
                breatheRoll = D6DieRoll.create(state, d6),
            )
        }
    }

    override val ChooseReRollSource = object : AbstractChooseRerollSource() {
        override fun getRerollData(state: Game, rules: Rules): RerollData {
            val context = state.getContext<BreatheFireContext>()
            return RerollData(context.attacker, context.breatheRoll!!, null)
        }
    }

    override val ReRollDie = object : AbstractReRollDie() {
        override fun updateContext(state: Game, rules: Rules, d6: D6Result): ProcedureContext {
            val context = state.getContext<BreatheFireContext>()
            return context.copy(
                breatheRoll = context.breatheRoll!!.copyReroll(
                    rerollSource = state.rerollContext!!.source,
                    rerolledResult = d6,
                ),
            )
        }
    }
}
