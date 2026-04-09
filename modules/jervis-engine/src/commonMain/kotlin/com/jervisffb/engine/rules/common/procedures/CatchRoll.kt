package com.jervisffb.engine.rules.common.procedures

import com.jervisffb.engine.actions.D6Result
import com.jervisffb.engine.commands.Command
import com.jervisffb.engine.fsm.Node
import com.jervisffb.engine.model.Game
import com.jervisffb.engine.model.Team
import com.jervisffb.engine.model.context.CatchContext
import com.jervisffb.engine.model.context.ProcedureContext
import com.jervisffb.engine.model.context.assertContext
import com.jervisffb.engine.model.context.getContext
import com.jervisffb.engine.rules.DiceRollType
import com.jervisffb.engine.rules.Rules
import com.jervisffb.engine.rules.bb2020.testAgainstAgility
import com.jervisffb.engine.rules.common.procedures.actions.dicerolls.D6WithRerollProcedure
import com.jervisffb.engine.rules.common.procedures.actions.dicerolls.RerollData

/**
 * Procedure for handling a Catch Roll as described on page 51 in the rulebook.
 * It is only responsible for handling the actual dice roll. The result is stored
 * in [CatchContext] and it is up to the caller of the procedure to choose
 * the appropriate action depending on the outcome.
 */
object CatchRoll : D6WithRerollProcedure() {
    override val rollType: DiceRollType = DiceRollType.CATCH
    override val initialNode: Node get() = RollDie
    override fun onEnterProcedure(state: Game, rules: Rules): Command? = null
    override fun onExitProcedure(state: Game, rules: Rules): Command? = null
    override fun isValid(state: Game, rules: Rules) = state.assertContext<CatchContext>()
    override fun getActionOwner(state: Game): Team = state.getContext<CatchContext>().catchingPlayer.team

    override val RollDie = object : AbstractRollDie() {
        override fun updateContext(state: Game, rules: Rules, d6: D6Result): ProcedureContext {
            val rollContext = state.getContext<CatchContext>()
            return rollContext.copy(
                roll = D6DieRoll.create(state, d6),
                isSuccess = testAgainstAgility(rollContext.catchingPlayer, d6, rollContext.modifiers)
            )
        }
    }

    override val ChooseReRollSource = object : AbstractChooseRerollSource() {
        override fun getRerollData(state: Game, rules: Rules): RerollData {
            val context = state.getContext<CatchContext>()
            return RerollData(context.catchingPlayer, context.roll!!, context.isSuccess)
        }
    }

    override val ReRollDie = object : AbstractReRollDie() {
        override fun updateContext(state: Game, rules: Rules, d6: D6Result): ProcedureContext {
            val catchRollContext = state.getContext<CatchContext>()
            return catchRollContext.copy(
                roll = catchRollContext.roll!!.copyReroll(
                    rerollSource = state.rerollContext!!.source,
                    rerolledResult = d6,
                ),
                isSuccess = testAgainstAgility(catchRollContext.catchingPlayer, d6, catchRollContext.modifiers)
            )
        }
    }
}
