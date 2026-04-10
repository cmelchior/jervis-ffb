package com.jervisffb.engine.rules.bb2025.procedures.actions.throwteammate

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
import com.jervisffb.engine.rules.common.procedures.D6DieRoll
import com.jervisffb.engine.rules.common.procedures.actions.dicerolls.D6WithRerollProcedure
import com.jervisffb.engine.rules.common.procedures.actions.dicerolls.RerollData

/**
 * Procedure for handling rolling for the Swoop distance.
 *
 * The result is stored in [SwoopContext]] and it is up to the caller of the procedure to
 * choose the appropriate action depending on the outcome.
 */
object SwoopDistanceRoll : D6WithRerollProcedure() {
    override fun isValid(state: Game, rules: Rules) {
        state.assertContext<SwoopContext>()
    }
    override val rollType: DiceRollType = DiceRollType.SWOOP_DISTANCE
    override val initialNode: Node get() = RollDie
    override fun onEnterRollProcedure(state: Game, rules: Rules): Command? = null
    override fun onExitRollProcedure(state: Game, rules: Rules): Command? = null
    override fun getActionOwner(state: Game): Team = state.getContext<SwoopContext>().player.team

    override val RollDie = object : AbstractRollDie() {
        override fun updateContext(state: Game, rules: Rules, d6: D6Result): ProcedureContext {
            val context = state.getContext<SwoopContext>()
            return context.copy(
                distanceRoll = D6DieRoll.create(state, d6),
            )
        }
    }

    override val ChooseReRollSource = object : AbstractChooseRerollSource() {
        override fun getRerollData(state: Game, rules: Rules): RerollData {
            val context = state.getContext<SwoopContext>()
            return RerollData(context.player, context.distanceRoll!!, null)
        }
    }

    override val ReRollDie = object : AbstractReRollDie() {
        override fun updateContext(state: Game, rules: Rules, d6: D6Result): ProcedureContext {
            val context = state.getContext<SwoopContext>()
            return context.copy(
                distanceRoll = context.distanceRoll!!.copyReroll(
                    rerollSource = state.getRerollContext().source,
                    rerolledResult = d6,
                ),
            )
        }
    }
}
