package com.jervisffb.engine.rules.bb2025.procedures.actions.pass

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
import com.jervisffb.engine.rules.bb2020.testAgainstAgility
import com.jervisffb.engine.rules.common.procedures.D6DieRoll
import com.jervisffb.engine.rules.common.procedures.actions.dicerolls.D6WithRerollProcedure
import com.jervisffb.engine.rules.common.procedures.actions.dicerolls.RerollData

/**
 * Procedure for handling rolling for Interception as described on page 71
 * in the BB20235 rulebook.
 *
 * It is only responsible for handling the actual dice roll. The result is stored
 * in [InterceptionRollContext] and it is up to the caller of the procedure
 * to choose the appropriate action depending on the outcome.
 */
object InterceptionRoll : D6WithRerollProcedure() {
    override val rollType: DiceRollType = DiceRollType.INTERCEPTION
    override val initialNode: Node get() = RollDie
    override fun onEnterRollProcedure(state: Game, rules: Rules): Command? = null
    override fun onExitRollProcedure(state: Game, rules: Rules): Command? = null
    override fun isValid(state: Game, rules: Rules) = state.assertContext<InterceptionRollContext>()
    override fun getActionOwner(state: Game): Team = state.getContext<InterceptionRollContext>().player.team

    override val RollDie = object : AbstractRollDie() {
        override fun updateContext(state: Game, rules: Rules, d6: D6Result): ProcedureContext {
            val rollContext = state.getContext<InterceptionRollContext>()
            return rollContext.copy(
                roll = D6DieRoll.create(state, d6),
                isSuccess = testAgainstAgility(rollContext.player, d6, rollContext.modifiers)
            )
        }
    }

    override val ChooseReRollSource = object : AbstractChooseRerollSource() {
        override fun getRerollData(state: Game, rules: Rules): RerollData {
            val context = state.getContext<InterceptionRollContext>()
            return RerollData(context.player, context.roll!!, context.isSuccess)
        }
    }

    override val ReRollDie = object : AbstractReRollDie() {
        override fun updateContext(state: Game, rules: Rules, d6: D6Result): ProcedureContext {
            val context = state.getContext<InterceptionRollContext>()
            return context.copy(
                roll = context.roll!!.copyReroll(
                    rerollSource = state.getRerollContext().source,
                    rerolledResult = d6,
                ),
                isSuccess = testAgainstAgility(context.player, d6, context.modifiers)
            )
        }
    }
}
