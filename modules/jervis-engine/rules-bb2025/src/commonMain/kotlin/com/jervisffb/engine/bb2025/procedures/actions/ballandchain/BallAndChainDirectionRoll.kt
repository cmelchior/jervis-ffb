package com.jervisffb.engine.bb2025.procedures.actions.ballandchain

import com.jervisffb.engine.actions.D3Result
import com.jervisffb.engine.bb2025.context.BallAndChainContext
import com.jervisffb.engine.common.procedures.dicerolls.D3WithPlayerRerollProcedure
import com.jervisffb.engine.common.procedures.dicerolls.RerollData
import com.jervisffb.engine.fsm.ActionNode
import com.jervisffb.engine.fsm.Node
import com.jervisffb.engine.model.Game
import com.jervisffb.engine.model.Player
import com.jervisffb.engine.model.context.ProcedureContext
import com.jervisffb.engine.model.context.assertContext
import com.jervisffb.engine.model.context.getContext
import com.jervisffb.engine.rules.DiceRollType
import com.jervisffb.engine.rules.Rules
import com.jervisffb.engine.rules.common.procedures.D3DieRoll

/**
 * Direction roll for Ball & Chain.
 *
 * See page 124 in the Blood Bowl 2025 rulebook.
 */
object BallAndChainDirectionRoll : D3WithPlayerRerollProcedure() {
    override val rollType: DiceRollType = DiceRollType.BALL_AND_CHAIN_DIRECTION
    override val initialNode: Node get() = RollDie
    override fun onEnterRollProcedure(state: Game, rules: Rules) = null
    override fun onExitRollProcedure(state: Game, rules: Rules) = null
    override fun isValid(state: Game, rules: Rules) = state.assertContext<BallAndChainContext>()
    override fun getActionOwner(state: Game): Player = state.getContext<BallAndChainContext>().player

    override val RollDie: ActionNode = object : AbstractRollDie() {
        override fun updateContext(state: Game, rules: Rules, d3: D3Result): ProcedureContext {
            val context = state.getContext<BallAndChainContext>()
            val selectedDirection = context.orientation
            return context.copy(
                directionRoll = D3DieRoll.create(d3),
                destination = context.origin.move(rules.throwIn(selectedDirection, d3), 1)
            )
        }
    }

    override val ChooseReRollSource = object : AbstractChooseRerollSource() {
        override fun getRerollData(state: Game, rules: Rules): RerollData {
            val ctx = state.getContext<BallAndChainContext>()
            return RerollData(player = ctx.player, roll = ctx.directionRoll!!, isSuccess = null)
        }
    }

    override val ReRollDie: ActionNode = object : AbstractReRollDie() {
        override fun updateContext(state: Game, rules: Rules, d3: D3Result): ProcedureContext {
            val context = state.getContext<BallAndChainContext>()
            val selectedDirection = context.orientation
            return context.copy(
                directionRoll = context.directionRoll!!.copyReroll(
                    rerollSource = state.getRerollContext().source,
                    rerolledResult = d3,
                ),
                destination = context.origin.move(rules.throwIn(selectedDirection, d3), 1)
            )
        }
    }
}
