package com.jervisffb.engine.rules.common.procedures

import com.jervisffb.engine.actions.D6Result
import com.jervisffb.engine.commands.AddPlayerStatusEffect
import com.jervisffb.engine.commands.Command
import com.jervisffb.engine.commands.buildCompositeCommand
import com.jervisffb.engine.commands.context.AddContext
import com.jervisffb.engine.commands.context.RemoveContext
import com.jervisffb.engine.commands.context.UpdateContext
import com.jervisffb.engine.fsm.Node
import com.jervisffb.engine.model.Game
import com.jervisffb.engine.model.Player
import com.jervisffb.engine.model.Team
import com.jervisffb.engine.model.context.ActivatePlayerContext
import com.jervisffb.engine.model.context.ProcedureContext
import com.jervisffb.engine.model.context.getContext
import com.jervisffb.engine.model.modifiers.PlayerStatusEffect
import com.jervisffb.engine.reports.ReportFailedTakeRoot
import com.jervisffb.engine.rules.DiceRollType
import com.jervisffb.engine.rules.Rules
import com.jervisffb.engine.rules.common.procedures.actions.dicerolls.D6WithRerollProcedure
import com.jervisffb.engine.rules.common.procedures.actions.dicerolls.RerollData

data class TakeRootRollContext(
    val player: Player,
    val roll: D6DieRoll? = null,
    val isSuccess: Boolean = false
) : ProcedureContext {
    val rerolled: Boolean = roll?.rerollSource != null && roll.rerolledResult != null
}

/**
 * Procedure for rolling for Take Root as described on page 137 in the BB2025 rulebook.
 *
 * This procedure will update [ActivatePlayerContext] with the result of the roll.
 * It is up to the caller of this method to react to it.
 */
object TakeRootRoll: D6WithRerollProcedure() {
    override val rollType: DiceRollType = DiceRollType.TAKE_ROOT
    override val initialNode: Node get() = RollDie
    override fun onEnterRollProcedure(state: Game, rules: Rules): Command {
        val player = state.activePlayer!!
        return AddContext(TakeRootRollContext(player = player))
    }
    override fun onExitRollProcedure(state: Game, rules: Rules): Command {
        val activateContext = state.getContext<ActivatePlayerContext>()
        val context = state.getContext<TakeRootRollContext>()
        return buildCompositeCommand {
            add(RemoveContext(context))
            if (!context.isSuccess) {
                addAll(
                    AddPlayerStatusEffect(context.player, PlayerStatusEffect.rooted()),
                    ReportFailedTakeRoot(context.player),
                    UpdateContext(
                        activateContext.copy(
                            rolledForNegaTrait = true,
                            markActionAsUsed = true,
                        )
                    )
                )
            }
        }
    }
    override fun getActionOwner(state: Game): Team = state.getContext<ActivatePlayerContext>().player.team

    override val RollDie = object : AbstractRollDie() {
        override fun updateContext(state: Game, rules: Rules, d6: D6Result): ProcedureContext {
            val context = state.getContext<TakeRootRollContext>()
            val isSuccess = calculateSuccess(d6)
            return context.copy(
                roll = D6DieRoll.create(state, d6),
                isSuccess = isSuccess
            )
        }
    }

    override val ChooseReRollSource = object : AbstractChooseRerollSource() {
        override fun getRerollData(state: Game, rules: Rules): RerollData {
            val context = state.getContext<TakeRootRollContext>()
            return RerollData(context.player, context.roll!!, context.isSuccess)
        }
    }

    override val ReRollDie = object : AbstractReRollDie() {
        override fun updateContext(state: Game, rules: Rules, d6: D6Result): ProcedureContext {
            val rollResultContext = state.getContext<TakeRootRollContext>()
            return rollResultContext.copy(
                roll = rollResultContext.roll!!.copyReroll(
                    rerollSource = state.getRerollContext().source,
                    rerolledResult = d6,
                ),
                isSuccess = calculateSuccess(d6)
            )
        }
    }

    // -- HELPER METHODS --

    private fun calculateSuccess(d6: D6Result): Boolean {
        val isSuccess = d6.value >= 2
        return isSuccess
    }
}
