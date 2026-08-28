package com.jervisffb.engine.bb2025.procedures

import com.jervisffb.engine.actions.D6Result
import com.jervisffb.engine.commands.AddPlayerStatusEffect
import com.jervisffb.engine.commands.Command
import com.jervisffb.engine.commands.buildCompositeCommand
import com.jervisffb.engine.commands.context.AddContext
import com.jervisffb.engine.commands.context.RemoveContext
import com.jervisffb.engine.commands.context.UpdateContext
import com.jervisffb.engine.common.context.ActivatePlayerContext
import com.jervisffb.engine.common.context.TakeRootRollContext
import com.jervisffb.engine.common.modifiers.rooted
import com.jervisffb.engine.common.procedures.dicerolls.D6WithPlayerRerollProcedure
import com.jervisffb.engine.common.procedures.dicerolls.RerollData
import com.jervisffb.engine.common.reports.ReportFailedTakeRoot
import com.jervisffb.engine.fsm.Node
import com.jervisffb.engine.model.Game
import com.jervisffb.engine.model.Player
import com.jervisffb.engine.model.context.ProcedureContext
import com.jervisffb.engine.model.context.getContext
import com.jervisffb.engine.model.modifiers.PlayerStatusEffect
import com.jervisffb.engine.rules.DiceRollType
import com.jervisffb.engine.rules.Rules
import com.jervisffb.engine.rules.common.procedures.D6DieRoll

/**
 * Procedure for rolling for Take Root in BB2025.
 */
object TakeRootRoll : D6WithPlayerRerollProcedure() {
    override val rollType: DiceRollType = DiceRollType.TAKE_ROOT
    override val initialNode: Node get() = RollDie

    override fun onEnterRollProcedure(state: Game, rules: Rules): Command {
        return AddContext(TakeRootRollContext(player = state.activePlayer!!))
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
                        ),
                    ),
                )
            }
        }
    }

    override fun getActionOwner(state: Game): Player = state.getContext<ActivatePlayerContext>().player

    override val RollDie = object : AbstractRollDie() {
        override fun updateContext(state: Game, rules: Rules, d6: D6Result): ProcedureContext {
            val context = state.getContext<TakeRootRollContext>()
            return context.copy(roll = D6DieRoll.create(d6), isSuccess = calculateSuccess(d6))
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
            val context = state.getContext<TakeRootRollContext>()
            return context.copy(
                roll = context.roll!!.copyReroll(
                    rerollSource = state.getRerollContext().source,
                    rerolledResult = d6,
                ),
                isSuccess = calculateSuccess(d6),
            )
        }
    }

    private fun calculateSuccess(d6: D6Result): Boolean = d6.value >= 2
}
