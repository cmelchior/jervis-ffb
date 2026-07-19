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
import com.jervisffb.engine.model.context.ActivatePlayerContext
import com.jervisffb.engine.model.context.ProcedureContext
import com.jervisffb.engine.model.context.getContext
import com.jervisffb.engine.model.modifiers.PlayerStatusEffect
import com.jervisffb.engine.rules.DiceRollType
import com.jervisffb.engine.rules.Rules
import com.jervisffb.engine.rules.common.actions.PlayerStandardActionType
import com.jervisffb.engine.rules.common.procedures.actions.dicerolls.D6WithRerollProcedure
import com.jervisffb.engine.rules.common.procedures.actions.dicerolls.RerollData
import com.jervisffb.engine.utils.INVALID_GAME_STATE

data class UnchannelledFuryRollContext(
    val player: Player,
    val roll: D6DieRoll? = null,
    val isSuccess: Boolean = false,
) : ProcedureContext {
    val rerolled: Boolean = roll?.rerollSource != null && roll.rerolledResult != null
}

/**
 * Procedure for rolling for Unchannelled Fury* (Passive).
 *
 * See page 87 in the BB2020 rulebook.
 * See page 138 in the BB2025 rulebook.
 *
 * This procedure will update [ActivatePlayerContext] with the result of the roll.
 * It is up to the caller of this method to react to it.
 */
object UnchannelledFuryRoll: D6WithRerollProcedure() {
    override val rollType: DiceRollType = DiceRollType.UNCHANNELLED_FURY
    override val initialNode: Node get() = RollDie
    override fun onEnterRollProcedure(state: Game, rules: Rules): Command {
        val player = state.activePlayer ?: INVALID_GAME_STATE("Missing active player")
        return AddContext(UnchannelledFuryRollContext(player = player))
    }
    override fun onExitRollProcedure(state: Game, rules: Rules): Command {
        val activateContext = state.getContext<ActivatePlayerContext>()
        val context = state.getContext<UnchannelledFuryRollContext>()
        return buildCompositeCommand {
            add(RemoveContext<UnchannelledFuryRollContext>())
            if (!context.isSuccess) {
                add(AddPlayerStatusEffect(context.player, PlayerStatusEffect.unchannelledFury()))
                add(
                    UpdateContext(
                        activateContext.copy(
                            rolledForNegaTrait = true,
                            activationEndsImmediately = true,
                            markActionAsUsed = true
                        )
                    )
                )
            }
        }
    }
    override fun getActionOwner(state: Game): Player = state.getContext<ActivatePlayerContext>().player

    override val RollDie = object : AbstractRollDie() {
        override fun updateContext(state: Game, rules: Rules, d6: D6Result): ProcedureContext {
            val context = state.getContext<UnchannelledFuryRollContext>()
            val activateContext = state.getContext<ActivatePlayerContext>()
            val isSuccess = calculateSuccess(activateContext, d6)
            return context.copy(
                roll = D6DieRoll.create(state, d6),
                isSuccess = isSuccess
            )
        }
    }

    override val ChooseReRollSource = object : AbstractChooseRerollSource() {
        override fun getRerollData(state: Game, rules: Rules): RerollData {
            val context = state.getContext<UnchannelledFuryRollContext>()
            return RerollData(context.player, context.roll!!, context.isSuccess)
        }
    }

    override val ReRollDie = object : AbstractReRollDie() {
        override fun updateContext(state: Game, rules: Rules, d6: D6Result): ProcedureContext {
            val activateContext = state.getContext<ActivatePlayerContext>()
            val rollResultContext = state.getContext<UnchannelledFuryRollContext>()
            return rollResultContext.copy(
                roll = rollResultContext.roll!!.copyReroll(
                    rerollSource = state.getRerollContext().source,
                    rerolledResult = d6,
                ),
                isSuccess = calculateSuccess(activateContext, d6)
            )
        }
    }

    private fun calculateSuccess(context: ActivatePlayerContext, d6: D6Result): Boolean {
        val modifier = when (context.declaredAction!!.type) {
            PlayerStandardActionType.BLOCK,
            PlayerStandardActionType.BLITZ -> 2
            else -> 0
        }
        val isSuccess = (d6.value + modifier >= 4)
        return isSuccess
    }
}
