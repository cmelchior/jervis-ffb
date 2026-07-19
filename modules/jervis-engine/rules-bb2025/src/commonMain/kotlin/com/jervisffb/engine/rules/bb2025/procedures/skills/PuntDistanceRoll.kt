package com.jervisffb.engine.rules.bb2025.procedures.skills

import com.jervisffb.engine.actions.D6Result
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
import com.jervisffb.engine.rules.common.procedures.D6DieRoll
import com.jervisffb.engine.rules.common.procedures.actions.dicerolls.D6WithRerollProcedure
import com.jervisffb.engine.rules.common.procedures.actions.dicerolls.RerollData

/**
 * Procedure responsible for rolling the distance for Punt once the throw-in
 * template has been placed.
 *
 * See page 135 in the BB2025 rulebook.
 * See [PuntDirectionRoll] for rolling for the direction.
 */
object PuntDistanceRoll : D6WithRerollProcedure() {
    override val rollType: DiceRollType = DiceRollType.PUNT_DISTANCE
    override val initialNode: Node get() = RollDie
    override fun getActionOwner(state: Game): Player = state.getContext<PuntContext>().punter
    override fun onEnterRollProcedure(state: Game, rules: Rules) = null
    override fun onExitRollProcedure(state: Game, rules: Rules) = null
    override fun isValid(state: Game, rules: Rules) = state.assertContext<PuntContext>()

    override val RollDie: ActionNode = object : AbstractRollDie() {
        override fun updateContext(state: Game, rules: Rules, d6: D6Result): ProcedureContext {
            val context = state.getContext<PuntContext>()
            return context.copy(
                distanceRoll = D6DieRoll.create(state, d6),
            )
        }
    }

    override val ChooseReRollSource: ActionNode = object : AbstractChooseRerollSource() {
        override fun getRerollData(state: Game, rules: Rules): RerollData {
            val ctx = state.getContext<PuntContext>()
            return RerollData(player = ctx.punter, roll = ctx.distanceRoll!!, isSuccess = null)
        }
    }

    override val ReRollDie: ActionNode = object : AbstractReRollDie() {
        override fun updateContext(state: Game, rules: Rules, d6: D6Result): ProcedureContext {
            val ctx = state.getContext<PuntContext>()
            return ctx.copy(
                distanceRoll = ctx.distanceRoll!!.copyReroll(
                    rerollSource = state.getRerollContext().source,
                    rerolledResult = d6,
                ),
            )
        }
    }
}
