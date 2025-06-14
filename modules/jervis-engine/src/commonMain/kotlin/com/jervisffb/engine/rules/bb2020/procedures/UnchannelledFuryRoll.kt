package com.jervisffb.engine.rules.bb2020.procedures

import com.jervisffb.engine.actions.Continue
import com.jervisffb.engine.actions.ContinueWhenReady
import com.jervisffb.engine.actions.D6Result
import com.jervisffb.engine.actions.Dice
import com.jervisffb.engine.actions.GameAction
import com.jervisffb.engine.actions.GameActionDescriptor
import com.jervisffb.engine.actions.NoRerollSelected
import com.jervisffb.engine.actions.RerollOptionSelected
import com.jervisffb.engine.actions.RollDice
import com.jervisffb.engine.actions.SelectNoReroll
import com.jervisffb.engine.commands.AddPlayerTemporaryEffect
import com.jervisffb.engine.commands.Command
import com.jervisffb.engine.commands.context.RemoveContext
import com.jervisffb.engine.commands.context.SetContext
import com.jervisffb.engine.commands.SetOldContext
import com.jervisffb.engine.commands.buildCompositeCommand
import com.jervisffb.engine.commands.compositeCommandOf
import com.jervisffb.engine.commands.fsm.ExitProcedure
import com.jervisffb.engine.commands.fsm.GotoNode
import com.jervisffb.engine.fsm.ActionNode
import com.jervisffb.engine.fsm.Node
import com.jervisffb.engine.fsm.ParentNode
import com.jervisffb.engine.fsm.Procedure
import com.jervisffb.engine.fsm.checkDiceRoll
import com.jervisffb.engine.model.Game
import com.jervisffb.engine.model.Player
import com.jervisffb.engine.model.context.ProcedureContext
import com.jervisffb.engine.model.context.UseRerollContext
import com.jervisffb.engine.model.context.getContext
import com.jervisffb.engine.reports.ReportDiceRoll
import com.jervisffb.engine.rules.DiceRollType
import com.jervisffb.engine.rules.PlayerStandardActionType
import com.jervisffb.engine.rules.Rules
import com.jervisffb.engine.utils.INVALID_ACTION
import com.jervisffb.engine.utils.calculateAvailableRerollsFor

data class UnchannelledFuryRollContext(
    val player: Player,
    val roll: D6DieRoll,
    val isSuccess: Boolean
) : ProcedureContext {
    val rerolled: Boolean = roll.rerollSource != null && roll.rerolledResult != null
}

/**
 * Procedure for rolling for Unchannelled Fury* as described on page 87 in the rulebook.
 *
 * This procedure will update [ActivatePlayerContext] with the result of the roll.
 * It is up to the caller of this method to react to it.
 */
object UnchannelledFuryRoll: Procedure() {
    override val initialNode: Node = RollDie
    override fun onEnterProcedure(state: Game, rules: Rules): Command? = null
    override fun onExitProcedure(state: Game, rules: Rules): Command {
        val activateContext = state.getContext<ActivatePlayerContext>()
        val context = state.getContext<UnchannelledFuryRollContext>()

        return buildCompositeCommand {
            add(RemoveContext<UnchannelledFuryRollContext>())
            if (!context.isSuccess) {
                add(AddPlayerTemporaryEffect(context.player, com.jervisffb.engine.model.modifiers.TemporaryEffect.unchannelledFury()))
                add(
                    SetContext(activateContext.copy(
                        rolledForNegaTrait = true,
                        activationEndsImmediately = true,
                        markActionAsUsed = true
                    ))
                )
            }
        }
    }

    object RollDie : ActionNode() {
        override fun actionOwner(state: Game, rules: Rules) = state.getContext<ActivatePlayerContext>().player.team
        override fun getAvailableActions(state: Game, rules: Rules): List<GameActionDescriptor> = listOf(RollDice(Dice.D6))
        override fun applyAction(action: GameAction, state: Game, rules: Rules): Command {
            return checkDiceRoll<D6Result>(action) { d6 ->
                val activateContext = state.getContext<ActivatePlayerContext>()
                val isSuccess = calculateSuccess(activateContext, d6)
                val rollContext = UnchannelledFuryRollContext(
                    state.activePlayer!!,
                    D6DieRoll.create(state, d6),
                    isSuccess
                )
                return compositeCommandOf(
                    ReportDiceRoll(DiceRollType.UNCHANNELLED_FURY, d6),
                    SetContext(rollContext),
                    GotoNode(ChooseReRollSource),
                )
            }
        }
    }

    object ChooseReRollSource : ActionNode() {
        override fun actionOwner(state: Game, rules: Rules) = state.getContext<UnchannelledFuryRollContext>().player.team
        override fun getAvailableActions(state: Game, rules: Rules): List<GameActionDescriptor> {
            val context = state.getContext<UnchannelledFuryRollContext>()
            val availableRerolls = calculateAvailableRerollsFor(
                rules = rules,
                player = context.player,
                type = DiceRollType.UNCHANNELLED_FURY,
                roll = context.roll,
                firstRollWasSuccess = context.isSuccess
            )
            return if (availableRerolls == null) {
                listOf(ContinueWhenReady)
            } else {
                listOf(SelectNoReroll(context.isSuccess)) + availableRerolls
            }
        }

        override fun applyAction(action: GameAction, state: Game, rules: Rules): Command {
            return when (action) {
                Continue -> ExitProcedure()
                is NoRerollSelected -> ExitProcedure()
                is RerollOptionSelected -> {
                    val rerollContext = UseRerollContext(DiceRollType.UNCHANNELLED_FURY, action.getRerollSource(state))
                    compositeCommandOf(
                        SetOldContext(Game::rerollContext, rerollContext),
                        GotoNode(UseRerollSource),
                    )
                }
                else -> INVALID_ACTION(action)
            }
        }
    }

    object UseRerollSource : ParentNode() {
        override fun getChildProcedure(state: Game, rules: Rules): Procedure {
            return state.rerollContext!!.source.rerollProcedure
        }
        override fun onExitNode(state: Game, rules: Rules): Command {
            val context = state.rerollContext!!
            return if (context.rerollAllowed) {
                GotoNode(ReRollDie)
            } else {
                ExitProcedure()
            }
        }
    }

    object ReRollDie : ActionNode() {
        override fun actionOwner(state: Game, rules: Rules) = state.getContext<UnchannelledFuryRollContext>().player.team
        override fun getAvailableActions(state: Game, rules: Rules): List<GameActionDescriptor> = listOf(RollDice(Dice.D6))
        override fun applyAction(action: GameAction, state: Game, rules: Rules): Command {
            return checkDiceRoll<D6Result>(action) { d6 ->
                val activateContext = state.getContext<ActivatePlayerContext>()
                val rollResultContext = state.getContext<UnchannelledFuryRollContext>()
                val isSuccess = calculateSuccess(activateContext, d6)
                val rollContext = rollResultContext.copy(
                    roll = rollResultContext.roll.copyReroll(
                        rerollSource = state.rerollContext!!.source,
                        rerolledResult = d6,
                    ),
                    isSuccess = isSuccess
                )
                compositeCommandOf(
                    SetContext(rollContext),
                    ExitProcedure(),
                )
            }
        }
    }

    private fun calculateSuccess(context: ActivatePlayerContext, d6: D6Result): Boolean {
        val modifier = when (context.declaredAction!!.type) {
            PlayerStandardActionType.BLOCK,
            PlayerStandardActionType.BLITZ -> 2
            // TODO How to detect special actions that replaces Blitz/Block actions
            else -> 0
        }
        val isSuccess = (d6.value + modifier >= 4)
        return isSuccess
    }
}
