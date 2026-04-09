package com.jervisffb.engine.rules.common.procedures

import com.jervisffb.engine.actions.D6Result
import com.jervisffb.engine.commands.Command
import com.jervisffb.engine.fsm.Node
import com.jervisffb.engine.model.Game
import com.jervisffb.engine.model.Team
import com.jervisffb.engine.model.context.ProcedureContext
import com.jervisffb.engine.model.context.SteadyFootingRollContext
import com.jervisffb.engine.model.context.assertContext
import com.jervisffb.engine.model.context.getContext
import com.jervisffb.engine.rules.DiceRollType
import com.jervisffb.engine.rules.Rules
import com.jervisffb.engine.rules.common.procedures.actions.dicerolls.D6WithRerollProcedure
import com.jervisffb.engine.rules.common.procedures.actions.dicerolls.RerollData

/**
 * Procedure for handling a Steady Footing Roll as described on page 136 in the
 * BB2025 rulebook. It is only responsible for handling the actual dice roll.
 * The result is stored in [SteadyFootingRoll]] and it is up to the caller of
 * the procedure to choose the appropriate action depending on the outcome.
 */
object SteadyFootingRoll : D6WithRerollProcedure() {
    override val rollType: DiceRollType = DiceRollType.STEADY_FOOTING
    override val initialNode: Node get() = RollDie
    override fun onEnterRollProcedure(state: Game, rules: Rules): Command? = null
    override fun onExitRollProcedure(state: Game, rules: Rules): Command? = null
    override fun isValid(state: Game, rules: Rules) = state.assertContext<SteadyFootingRollContext>()
    override fun getActionOwner(state: Game): Team = state.getContext<SteadyFootingRollContext>().player.team

    override val RollDie = object : AbstractRollDie() {
        override fun updateContext(state: Game, rules: Rules, d6: D6Result): ProcedureContext {
            val rollContext = state.getContext<SteadyFootingRollContext>()
            return rollContext.copy(
                roll = D6DieRoll.create(state, d6),
                isSuccess = steadyFootingTest(d6)
            )
        }
    }

    // Team Reroll, Pro, Catch (only if failed), other skills
    override val ChooseReRollSource = object : AbstractChooseRerollSource() {
        override fun getRerollData(state: Game, rules: Rules): RerollData {
            val context = state.getContext<SteadyFootingRollContext>()
            return RerollData(context.player, context.roll!!, context.isSuccess)
        }
    }

    override val ReRollDie = object : AbstractReRollDie() {
        override fun updateContext(state: Game, rules: Rules, d6: D6Result): ProcedureContext {
            val context = state.getContext<SteadyFootingRollContext>()
            return context.copy(
                roll = context.roll!!.copyReroll(
                    rerollSource = state.rerollContext!!.source,
                    rerolledResult = d6,
                ),
                isSuccess = steadyFootingTest(d6)
            )
        }
    }

    // -- HELPER METHODS --

    private fun steadyFootingTest(d6: D6Result): Boolean {
        val target = 6
        return d6.value == target
    }
}
