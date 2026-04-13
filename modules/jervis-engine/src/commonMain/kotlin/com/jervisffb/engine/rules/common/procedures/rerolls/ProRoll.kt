package com.jervisffb.engine.rules.common.procedures.rerolls

import com.jervisffb.engine.actions.D6Result
import com.jervisffb.engine.commands.Command
import com.jervisffb.engine.fsm.Node
import com.jervisffb.engine.model.Game
import com.jervisffb.engine.model.Player
import com.jervisffb.engine.model.context.ProcedureContext
import com.jervisffb.engine.model.context.assertContext
import com.jervisffb.engine.model.context.getContext
import com.jervisffb.engine.model.getSkill
import com.jervisffb.engine.rules.DiceRollType
import com.jervisffb.engine.rules.Rules
import com.jervisffb.engine.rules.bb2025.skills.Pro
import com.jervisffb.engine.rules.common.procedures.D6DieRoll
import com.jervisffb.engine.rules.common.procedures.actions.dicerolls.D6WithRerollProcedure
import com.jervisffb.engine.rules.common.procedures.actions.dicerolls.RerollData

data class ProRollContext(
    val player: Player,
    val roll: D6DieRoll? = null,
    val isSuccess: Boolean = false,
): ProcedureContext

/**
 * Procedure controlling the Pro roll, i.e., when a player with the Pro
 * skill wants to re-roll a die.
 */
object ProRoll: D6WithRerollProcedure() {
    override val rollType: DiceRollType = DiceRollType.PRO
    override val initialNode: Node get() = RollDie
    override fun onEnterRollProcedure(state: Game, rules: Rules): Command? = null
    override fun onExitRollProcedure(state: Game, rules: Rules): Command? = null
    override fun isValid(state: Game, rules: Rules) = state.assertContext<ProRollContext>()
    override fun getActionOwner(state: Game): Player = state.getContext<ProRollContext>().player

    override val RollDie = object : AbstractRollDie() {
        override fun updateContext(state: Game, rules: Rules, d6: D6Result): ProcedureContext {
            val context = state.getContext<ProRollContext>()
            return context.copy(
                roll = D6DieRoll.create(state, d6),
                isSuccess = isSuccess(context.player, d6)
            )
        }
    }

    override val ChooseReRollSource = object : AbstractChooseRerollSource() {
        override fun getRerollData(state: Game, rules: Rules): RerollData {
            val context = state.getContext<ProRollContext>()
            return RerollData(context.player, context.roll!!, context.isSuccess)
        }
    }

    override val ReRollDie = object : AbstractReRollDie() {
        override fun updateContext(state: Game, rules: Rules, d6: D6Result): ProcedureContext {
            val context = state.getContext<ProRollContext>()
            return context.copy(
                roll = context.roll!!.copyReroll(
                    rerollSource = state.getRerollContext().source,
                    rerolledResult = d6,
                ),
                isSuccess = isSuccess(context.player, d6)
            )
        }
    }

    private fun isSuccess(player: Player, roll: D6Result): Boolean {
        val target = player.getSkill<Pro>().successTarget
        return roll.value >= target
    }
}
