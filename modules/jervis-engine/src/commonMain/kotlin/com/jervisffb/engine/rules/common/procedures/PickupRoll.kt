package com.jervisffb.engine.rules.common.procedures

import com.jervisffb.engine.actions.D6Result
import com.jervisffb.engine.commands.Command
import com.jervisffb.engine.fsm.Node
import com.jervisffb.engine.model.Game
import com.jervisffb.engine.model.Team
import com.jervisffb.engine.model.context.PickupRollContext
import com.jervisffb.engine.model.context.ProcedureContext
import com.jervisffb.engine.model.context.assertContext
import com.jervisffb.engine.model.context.getContext
import com.jervisffb.engine.model.modifiers.DiceModifier
import com.jervisffb.engine.rules.DiceRollType
import com.jervisffb.engine.rules.Rules
import com.jervisffb.engine.rules.common.procedures.actions.dicerolls.D6WithRerollProcedure
import com.jervisffb.engine.rules.common.procedures.actions.dicerolls.RerollData
import com.jervisffb.engine.utils.sum

/**
 * Procedure for handling a Pickup Roll as described on page 46 in the rulebook.
 * It is only responsible for handling the actual dice roll. The result is stored
 * in [PickupRollContext]] and it is up to the caller of the procedure to
 * choose the appropriate action depending on the outcome.
 */
object PickupRoll : D6WithRerollProcedure() {
    override val rollType: DiceRollType = DiceRollType.PICKUP
    override val initialNode: Node get() = RollDie
    override fun onEnterRollProcedure(state: Game, rules: Rules): Command? = null
    override fun onExitRollProcedure(state: Game, rules: Rules): Command? = null
    override fun isValid(state: Game, rules: Rules) = state.assertContext<PickupRollContext>()
    override fun getActionOwner(state: Game): Team = state.getContext<PickupRollContext>().player.team

    override val RollDie = object : AbstractRollDie() {
        override fun updateContext(state: Game, rules: Rules, d6: D6Result): ProcedureContext {
            val context = state.getContext<PickupRollContext>()
            return context.copy(
                roll = D6DieRoll.create(state, d6),
                isSuccess = isPickupSuccess(d6, context.player.agility, context.modifiers),
            )
        }
    }

    override val ChooseReRollSource = object : AbstractChooseRerollSource() {
        override fun getRerollData(state: Game, rules: Rules): RerollData {
            val context = state.getContext<PickupRollContext>()
            return RerollData(context.player, context.roll!!, context.isSuccess)
        }
    }

    override val ReRollDie = object : AbstractReRollDie() {
        override fun updateContext(state: Game, rules: Rules, d6: D6Result): ProcedureContext {
            val context = state.getContext<PickupRollContext>()
            return context.copy(
                roll = context.roll!!.copyReroll(
                    rerollSource = state.getRerollContext().source,
                    rerolledResult = d6,
                ),
                isSuccess = isPickupSuccess(d6, context.player.agility, context.modifiers)
            )
        }
    }

    private fun isPickupSuccess(roll: D6Result, target: Int, modifiers: List<DiceModifier>): Boolean {
        return when (roll.value) {
            1 -> false
            in 2..5 -> roll.value != 1 && (target <= roll.value + modifiers.sum())
            6 -> true
            else -> error("Invalid value: ${roll.value}")
        }
    }
}
