package com.jervisffb.engine.rules.bb2025.procedures.skills

import com.jervisffb.engine.actions.D3Result
import com.jervisffb.engine.fsm.ActionNode
import com.jervisffb.engine.fsm.Node
import com.jervisffb.engine.model.Game
import com.jervisffb.engine.model.Player
import com.jervisffb.engine.model.context.ProcedureContext
import com.jervisffb.engine.model.context.PuntContext
import com.jervisffb.engine.model.context.assertContext
import com.jervisffb.engine.model.context.getContext
import com.jervisffb.engine.rules.DiceRollType
import com.jervisffb.engine.rules.Rules
import com.jervisffb.engine.rules.common.procedures.D3DieRoll
import com.jervisffb.engine.rules.common.procedures.actions.dicerolls.D3WithRerollProcedure
import com.jervisffb.engine.rules.common.procedures.actions.dicerolls.RerollData

/**
 * Procedure responsible for rolling the direction for Punt once the throw-in
 * template has been placed.
 *
 * See page 135 in the BB2025 rulebook.
 * See [PuntDistanceRoll] for rolling for the distance.
 */
object PuntDirectionRoll : D3WithRerollProcedure() {
    override val rollType: DiceRollType = DiceRollType.PUNT_DIRECTION
    override val initialNode: Node get() = RollDie
    override fun onEnterRollProcedure(state: Game, rules: Rules) = null
    override fun onExitRollProcedure(state: Game, rules: Rules) = null
    override fun isValid(state: Game, rules: Rules) = state.assertContext<PuntContext>()
    override fun getActionOwner(state: Game): Player = state.getContext<PuntContext>().punter

    override val RollDie: ActionNode = object : AbstractRollDie() {
        override fun updateContext(state: Game, rules: Rules, d3: D3Result): ProcedureContext {
            val context = state.getContext<PuntContext>()
            val selectedDirection = context.selectedDirection ?: error("No direction selected: $context")
            return context.copy(
                directionRoll = D3DieRoll.create(state, d3),
                kickDirection = rules.throwIn(selectedDirection, d3)
            )
        }
    }

    override val ChooseReRollSource: ActionNode = object : AbstractChooseRerollSource() {
        override fun getRerollData(state: Game, rules: Rules): RerollData {
            val ctx = state.getContext<PuntContext>()
            return RerollData(player = ctx.punter, roll = ctx.directionRoll!!, isSuccess = null)
        }
    }

    override val ReRollDie: ActionNode = object : AbstractReRollDie() {
        override fun updateContext(state: Game, rules: Rules, d3: D3Result): ProcedureContext {
            val context = state.getContext<PuntContext>()
            val selectedDirection = context.selectedDirection ?: error("No direction selected: $context")
            return context.copy(
                directionRoll = context.directionRoll!!.copyReroll(
                    rerollSource = state.getRerollContext().source,
                    rerolledResult = d3,
                ),
                kickDirection = rules.throwIn(selectedDirection, d3)
            )
        }
    }
}
