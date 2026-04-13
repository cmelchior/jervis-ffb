package com.jervisffb.engine.rules.bb2025.procedures.actions.block

import com.jervisffb.engine.actions.D6Result
import com.jervisffb.engine.commands.Command
import com.jervisffb.engine.fsm.Node
import com.jervisffb.engine.model.Game
import com.jervisffb.engine.model.Player
import com.jervisffb.engine.model.context.ProcedureContext
import com.jervisffb.engine.model.context.assertContext
import com.jervisffb.engine.model.context.getContext
import com.jervisffb.engine.model.modifiers.JumpUpModifier
import com.jervisffb.engine.rules.DiceRollType
import com.jervisffb.engine.rules.Rules
import com.jervisffb.engine.rules.bb2020.testAgainstAgility
import com.jervisffb.engine.rules.common.procedures.D6DieRoll
import com.jervisffb.engine.rules.common.procedures.actions.dicerolls.D6WithRerollProcedure
import com.jervisffb.engine.rules.common.procedures.actions.dicerolls.RerollData

data class JumpUpRollContext(
    val player: Player,
    val roll: D6DieRoll? = null,
    val isSuccess: Boolean = false,
): ProcedureContext

/**
 * Implement the Jump Up Roll as described on page XXX in the BB2025
 * rulebook.
 *
 * The result is stored in [JumpUpRollContext] and it is up to the caller to
 * determine what to do with the result.
 */
object JumpUpRoll: D6WithRerollProcedure() {
    override val rollType: DiceRollType = DiceRollType.JUMP_UP
    override val initialNode: Node get() = RollDie
    override fun onEnterRollProcedure(state: Game, rules: Rules): Command? = null
    override fun onExitRollProcedure(state: Game, rules: Rules): Command? = null
    override fun isValid(state: Game, rules: Rules) = state.assertContext<JumpUpRollContext>()
    override fun getActionOwner(state: Game): Player = state.getContext<JumpUpRollContext>().player

    override val RollDie = object : AbstractRollDie() {
        override fun updateContext(state: Game, rules: Rules, d6: D6Result): ProcedureContext {
            val context = state.getContext<JumpUpRollContext>()
            return context.copy(
                roll = D6DieRoll.create(state, d6),
                isSuccess = testAgainstAgility(context.player, d6, listOf(JumpUpModifier.JUMP_UP)),
            )
        }
    }

    override val ChooseReRollSource = object : AbstractChooseRerollSource() {
        override fun getRerollData(state: Game, rules: Rules): RerollData {
            val context = state.getContext<JumpUpRollContext>()
            return RerollData(context.player, context.roll!!, context.isSuccess)
        }
    }

    override val ReRollDie = object : AbstractReRollDie() {
        override fun updateContext(state: Game, rules: Rules, d6: D6Result): ProcedureContext {
            val context = state.getContext<JumpUpRollContext>()
            return context.copy(
                roll = context.roll!!.copyReroll(
                    rerollSource = state.getRerollContext().source,
                    rerolledResult = d6,
                ),
                isSuccess = testAgainstAgility(context.player, d6, listOf(JumpUpModifier.JUMP_UP)),
            )
        }
    }
}
