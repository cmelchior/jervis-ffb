package com.jervisffb.engine.rules.common.procedures.actions.block

import com.jervisffb.engine.actions.D6Result
import com.jervisffb.engine.commands.Command
import com.jervisffb.engine.fsm.Node
import com.jervisffb.engine.model.Game
import com.jervisffb.engine.model.Player
import com.jervisffb.engine.model.context.ProcedureContext
import com.jervisffb.engine.model.context.assertContext
import com.jervisffb.engine.model.context.getContext
import com.jervisffb.engine.rules.DiceRollType
import com.jervisffb.engine.rules.Rules
import com.jervisffb.engine.rules.common.procedures.D6DieRoll
import com.jervisffb.engine.rules.common.procedures.actions.dicerolls.D6WithRerollProcedure
import com.jervisffb.engine.rules.common.procedures.actions.dicerolls.RerollData

/**
 * Implement the Projectile Vomit Roll as described on page 133 in the BB2025
 * rulebook.
 *
 * The result is stored in [ProjectileVomitContext] and it is up to the caller to
 * determine what to do with the result.
 */
object ProjectileVomitRoll: D6WithRerollProcedure() {
    override val rollType: DiceRollType = DiceRollType.PROJECTILE_VOMIT
    override val initialNode: Node get() = RollDie
    override fun onEnterRollProcedure(state: Game, rules: Rules): Command? = null
    override fun onExitRollProcedure(state: Game, rules: Rules): Command? = null
    override fun isValid(state: Game, rules: Rules) = state.assertContext<ProjectileVomitContext>()
    override fun getActionOwner(state: Game): Player = state.getContext<ProjectileVomitContext>().attacker

    override val RollDie = object : AbstractRollDie() {
        override fun updateContext(state: Game, rules: Rules, d6: D6Result): ProcedureContext {
            val context = state.getContext<ProjectileVomitContext>()
            return context.copy(
                vomitRoll = D6DieRoll.create(state, d6),
                isSuccess = isSuccess(d6),
            )
        }
    }

    override val ChooseReRollSource = object : AbstractChooseRerollSource() {
        override fun getRerollData(state: Game, rules: Rules): RerollData {
            val context = state.getContext<ProjectileVomitContext>()
            return RerollData(context.attacker, context.vomitRoll!!, context.isSuccess)
        }
    }

    override val ReRollDie = object : AbstractReRollDie() {
        override fun updateContext(state: Game, rules: Rules, d6: D6Result): ProcedureContext {
            val context = state.getContext<ProjectileVomitContext>()
            return context.copy(
                vomitRoll = context.vomitRoll!!.copyReroll(
                    rerollSource = state.getRerollContext().source,
                    rerolledResult = d6,
                ),
                isSuccess = isSuccess(d6),
            )
        }
    }

    // -- HELPER METHODS --
    private fun isSuccess(d6: D6Result): Boolean {
        return d6.value >= 2
    }
}
