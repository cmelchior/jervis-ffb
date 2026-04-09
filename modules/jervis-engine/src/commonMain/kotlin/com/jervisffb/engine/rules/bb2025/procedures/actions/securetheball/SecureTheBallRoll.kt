package com.jervisffb.engine.rules.bb2025.procedures.actions.securetheball

import com.jervisffb.engine.actions.D6Result
import com.jervisffb.engine.commands.Command
import com.jervisffb.engine.fsm.Node
import com.jervisffb.engine.model.Game
import com.jervisffb.engine.model.Team
import com.jervisffb.engine.model.context.ProcedureContext
import com.jervisffb.engine.model.context.SecureTheBallRollContext
import com.jervisffb.engine.model.context.assertContext
import com.jervisffb.engine.model.context.getContext
import com.jervisffb.engine.model.modifiers.DiceModifier
import com.jervisffb.engine.rules.DiceRollType
import com.jervisffb.engine.rules.Rules
import com.jervisffb.engine.rules.common.procedures.D6DieRoll
import com.jervisffb.engine.rules.common.procedures.actions.dicerolls.D6WithRerollProcedure
import com.jervisffb.engine.rules.common.procedures.actions.dicerolls.RerollData
import com.jervisffb.engine.utils.sum

/**
 * Procedure for handling a Secure the Ball roll.
 *
 * The result is stored in [SecureTheBallRollContext]] and it is up to the caller of the procedure to
 * choose the appropriate action depending on the outcome.
 */
object SecureTheBallRoll : D6WithRerollProcedure() {
    override val rollType: DiceRollType = DiceRollType.SECURE_THE_BALL
    override val initialNode: Node get() = RollDie
    override fun onEnterRollProcedure(state: Game, rules: Rules): Command? = null
    override fun onExitRollProcedure(state: Game, rules: Rules): Command? = null
    override fun isValid(state: Game, rules: Rules) = state.assertContext<SecureTheBallRollContext>()
    override fun getActionOwner(state: Game): Team = state.getContext<SecureTheBallRollContext>().player.team

    override val RollDie = object : AbstractRollDie() {
        override fun updateContext(state: Game, rules: Rules, d6: D6Result): ProcedureContext {
            val context = state.getContext<SecureTheBallRollContext>()
            return context.copy(
                roll = D6DieRoll.create(state, d6),
                isSuccess = isSecuringSuccessful(d6, context.modifiers),
            )
        }
    }

    // We expect all team reroll types, but not Sure Hands
    override val ChooseReRollSource = object : AbstractChooseRerollSource() {
        override fun getRerollData(state: Game, rules: Rules): RerollData {
            val context = state.getContext<SecureTheBallRollContext>()
            return RerollData(context.player, context.roll!!, context.isSuccess)
        }
    }

    override val ReRollDie = object : AbstractReRollDie() {
        override fun updateContext(state: Game, rules: Rules, d6: D6Result): ProcedureContext {
            val context = state.getContext<SecureTheBallRollContext>()
            return context.copy(
                roll = context.roll!!.copyReroll(
                    rerollSource = state.rerollContext!!.source,
                    rerolledResult = d6,
                ),
                isSuccess = isSecuringSuccessful(d6, context.modifiers)
            )
        }
    }

    private fun isSecuringSuccessful(roll: D6Result, modifiers: List<DiceModifier>): Boolean {
        val target = 2
        return when (roll.value) {
            1 -> false
            in 2..5 -> (target <= roll.value + modifiers.sum())
            6 -> true
            else -> error("Invalid value: ${roll.value}")
        }
    }
}
