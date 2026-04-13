package com.jervisffb.engine.rules.bb2025.procedures.actions.throwteammate

import com.jervisffb.engine.actions.D3Result
import com.jervisffb.engine.commands.Command
import com.jervisffb.engine.fsm.Node
import com.jervisffb.engine.model.Game
import com.jervisffb.engine.model.Player
import com.jervisffb.engine.model.context.ProcedureContext
import com.jervisffb.engine.model.context.assertContext
import com.jervisffb.engine.model.context.getContext
import com.jervisffb.engine.rules.DiceRollType
import com.jervisffb.engine.rules.Rules
import com.jervisffb.engine.rules.common.procedures.D3DieRoll
import com.jervisffb.engine.rules.common.procedures.actions.dicerolls.D3WithRerollProcedure
import com.jervisffb.engine.rules.common.procedures.actions.dicerolls.RerollData

/**
 * Procedure for handling rolling for the Swoop direction.
 *
 * The result is stored in [SwoopContext]] and it is up to the caller of the procedure to
 * choose the appropriate action depending on the outcome.
 */
object SwoopDirectionRoll : D3WithRerollProcedure() {
    override val rollType: DiceRollType = DiceRollType.SWOOP_DIRECTION
    override val initialNode: Node get() = RollDie
    override fun onEnterRollProcedure(state: Game, rules: Rules): Command? = null
    override fun onExitRollProcedure(state: Game, rules: Rules): Command? = null
    override fun isValid(state: Game, rules: Rules) = state.assertContext<SwoopContext>()
    override fun getActionOwner(state: Game): Player = state.getContext<SwoopContext>().player

    override val RollDie = object : AbstractRollDie() {
        override fun updateContext(state: Game, rules: Rules, d3: D3Result): ProcedureContext {
            val context = state.getContext<SwoopContext>()
            val selectedDirection = context.selectedDirection ?: error("No direction selected: $context")
            return context.copy(
                directionRoll = D3DieRoll.create(state, d3),
                rolledDirection = rules.throwIn(selectedDirection, d3)
            )
        }
    }

    override val ChooseReRollSource = object : AbstractChooseRerollSource() {
        override fun getRerollData(state: Game, rules: Rules): RerollData {
            val context = state.getContext<SwoopContext>()
            return RerollData(context.player, context.directionRoll!!, null)
        }
    }

    override val ReRollDie = object : AbstractReRollDie() {
        override fun updateContext(state: Game, rules: Rules, d3: D3Result): ProcedureContext {
            val context = state.getContext<SwoopContext>()
            val selectedDirection = context.selectedDirection ?: error("No direction selected: $context")
            return context.copy(
                directionRoll = context.directionRoll!!.copyReroll(
                    rerollSource = state.getRerollContext().source,
                    rerolledResult = d3,
                ),
                rolledDirection = rules.throwIn(selectedDirection, d3)
            )
        }
    }
}
