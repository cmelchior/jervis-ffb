package com.jervisffb.engine.rules.bb2025.procedures.skills

import com.jervisffb.engine.actions.D6Result
import com.jervisffb.engine.commands.Command
import com.jervisffb.engine.fsm.Node
import com.jervisffb.engine.model.Game
import com.jervisffb.engine.model.Player
import com.jervisffb.engine.model.context.HypnoticGazeContext
import com.jervisffb.engine.model.context.ProcedureContext
import com.jervisffb.engine.model.context.assertContext
import com.jervisffb.engine.model.context.getContext
import com.jervisffb.engine.rules.DiceRollType
import com.jervisffb.engine.rules.Rules
import com.jervisffb.engine.rules.common.procedures.D6DieRoll
import com.jervisffb.engine.rules.common.procedures.actions.dicerolls.D6WithRerollProcedure
import com.jervisffb.engine.rules.common.procedures.actions.dicerolls.RerollData

/**
 * Implement the Hypnotic Gaze Roll as described on page 129 in the BB2025
 * rulebook.
 *
 * Success on a 3+. The result is stored in [HypnoticGazeContext] and it is up
 * to the caller to determine what to do with the result.
 */
object HypnoticGazeRoll : D6WithRerollProcedure() {
    override val rollType: DiceRollType = DiceRollType.HYPNOTIC_GAZE
    override val initialNode: Node get() = RollDie
    override fun onEnterRollProcedure(state: Game, rules: Rules): Command? = null
    override fun onExitRollProcedure(state: Game, rules: Rules): Command? = null
    override fun isValid(state: Game, rules: Rules) = state.assertContext<HypnoticGazeContext>()
    override fun getActionOwner(state: Game): Player = state.getContext<HypnoticGazeContext>().gazer

    override val RollDie = object : AbstractRollDie() {
        override fun updateContext(state: Game, rules: Rules, d6: D6Result): ProcedureContext {
            val context = state.getContext<HypnoticGazeContext>()
            return context.copy(
                roll = D6DieRoll.create(state, d6),
                isSuccess = isSuccess(d6),
            )
        }
    }

    override val ChooseReRollSource = object : AbstractChooseRerollSource() {
        override fun getRerollData(state: Game, rules: Rules): RerollData {
            val context = state.getContext<HypnoticGazeContext>()
            return RerollData(context.gazer, context.roll!!, context.isSuccess)
        }
    }

    override val ReRollDie = object : AbstractReRollDie() {
        override fun updateContext(state: Game, rules: Rules, d6: D6Result): ProcedureContext {
            val context = state.getContext<HypnoticGazeContext>()
            return context.copy(
                roll = context.roll!!.copyReroll(
                    rerollSource = state.getRerollContext().source,
                    rerolledResult = d6,
                ),
                isSuccess = isSuccess(d6),
            )
        }
    }

    private fun isSuccess(d6: D6Result): Boolean {
        return d6.value >= 3
    }
}
