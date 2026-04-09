package com.jervisffb.engine.rules.common.procedures.actions.block

import com.jervisffb.engine.actions.D6Result
import com.jervisffb.engine.commands.Command
import com.jervisffb.engine.commands.context.UpdateContext
import com.jervisffb.engine.fsm.Node
import com.jervisffb.engine.model.Game
import com.jervisffb.engine.model.Player
import com.jervisffb.engine.model.Team
import com.jervisffb.engine.model.context.ActivatePlayerContext
import com.jervisffb.engine.model.context.ProcedureContext
import com.jervisffb.engine.model.context.assertContext
import com.jervisffb.engine.model.context.getContext
import com.jervisffb.engine.rules.DiceRollType
import com.jervisffb.engine.rules.Rules
import com.jervisffb.engine.rules.common.procedures.D6DieRoll
import com.jervisffb.engine.rules.common.procedures.actions.dicerolls.D6WithRerollProcedure
import com.jervisffb.engine.rules.common.procedures.actions.dicerolls.RerollData

data class FoulAppearanceContext(
    val attacker: Player,
    val defender: Player,
    val roll: D6DieRoll? = null,
    val isSuccess: Boolean = false,
): ProcedureContext

/**
 * Procedure for handling Foul Appearance when triggered as part of a Block or a
 * Special Action.
 *
 * If the roll is failed, the current action ends immediately. This is handled
 * here, so callers of this procedure just need to exit as quickly as possible.
 */
object FoulAppearanceRoll: D6WithRerollProcedure() {
    override val rollType: DiceRollType = DiceRollType.FOUL_APPEARANCE
    override val initialNode: Node get() = RollDie
    override fun onEnterRollProcedure(state: Game, rules: Rules): Command? = null
    override fun onExitRollProcedure(state: Game, rules: Rules): Command? {
        val context = state.getContext<FoulAppearanceContext>()
        return when (context.isSuccess) {
            true -> null
            false -> {
                val activePlayerContext = state.getContext<ActivatePlayerContext>()
                UpdateContext(activePlayerContext.copy(
                    activationEndsImmediately = true,
                    markActionAsUsed = true
                ))
            }
        }
    }
    override fun isValid(state: Game, rules: Rules) = state.assertContext<FoulAppearanceContext>()
    override fun getActionOwner(state: Game): Team = state.getContext<FoulAppearanceContext>().attacker.team

    override val RollDie = object : AbstractRollDie() {
        override fun updateContext(state: Game, rules: Rules, d6: D6Result): ProcedureContext {
            val context = state.getContext<FoulAppearanceContext>()
            return context.copy(
                roll = D6DieRoll.create(state, d6),
                isSuccess = isSuccessful(d6)
            )
        }
    }

    override val ChooseReRollSource = object : AbstractChooseRerollSource() {
        override fun getRerollData(state: Game, rules: Rules): RerollData {
            val context = state.getContext<FoulAppearanceContext>()
            return RerollData(context.attacker, context.roll!!, context.isSuccess)
        }
    }

    override val ReRollDie = object : AbstractReRollDie() {
        override fun updateContext(state: Game, rules: Rules, d6: D6Result): ProcedureContext {
            val context = state.getContext<FoulAppearanceContext>()
            return context.copy(
                roll = context.roll!!.copyReroll(
                    rerollSource = state.rerollContext!!.source,
                    rerolledResult = d6,
                ),
                isSuccess = isSuccessful(d6)
            )
        }
    }

    // -- HELPER FUNCTIONS --
    private fun isSuccessful(d6: D6Result): Boolean {
        return d6.value >= 2
    }
}
