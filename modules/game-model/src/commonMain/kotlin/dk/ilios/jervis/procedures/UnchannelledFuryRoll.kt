package dk.ilios.jervis.procedures

import buildCompositeCommand
import compositeCommandOf
import dk.ilios.jervis.actions.ActionDescriptor
import dk.ilios.jervis.actions.Continue
import dk.ilios.jervis.actions.ContinueWhenReady
import dk.ilios.jervis.actions.D6Result
import dk.ilios.jervis.actions.Dice
import dk.ilios.jervis.actions.GameAction
import dk.ilios.jervis.actions.NoRerollSelected
import dk.ilios.jervis.actions.RerollOptionSelected
import dk.ilios.jervis.actions.RollDice
import dk.ilios.jervis.actions.SelectNoReroll
import dk.ilios.jervis.commands.AddPlayerTemporaryEffect
import dk.ilios.jervis.commands.Command
import dk.ilios.jervis.commands.RemoveContext
import dk.ilios.jervis.commands.SetContext
import dk.ilios.jervis.commands.SetHasTackleZones
import dk.ilios.jervis.commands.SetOldContext
import dk.ilios.jervis.commands.fsm.ExitProcedure
import dk.ilios.jervis.commands.fsm.GotoNode
import dk.ilios.jervis.fsm.ActionNode
import dk.ilios.jervis.fsm.Node
import dk.ilios.jervis.fsm.ParentNode
import dk.ilios.jervis.fsm.Procedure
import dk.ilios.jervis.model.Game
import dk.ilios.jervis.model.Player
import dk.ilios.jervis.model.context.ProcedureContext
import dk.ilios.jervis.model.context.UseRerollContext
import dk.ilios.jervis.model.context.getContext
import dk.ilios.jervis.model.modifiers.TemporaryEffect
import dk.ilios.jervis.reports.ReportDiceRoll
import dk.ilios.jervis.rules.PlayerActionType
import dk.ilios.jervis.rules.Rules
import dk.ilios.jervis.rules.skills.DiceRollType
import dk.ilios.jervis.utils.INVALID_ACTION
import dk.ilios.jervis.utils.calculateAvailableRerollsFor

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
                add(AddPlayerTemporaryEffect(context.player, TemporaryEffect.unchannelledFury()))
                add(SetContext(activateContext.copy(
                    rolledForNegaTrait = true,
                    activationEndsImmediately = true,
                    markActionAsUsed = true
                )))
            }
        }
    }

    object RollDie : ActionNode() {
        override fun actionOwner(state: Game, rules: Rules) = state.getContext<ActivatePlayerContext>().player.team
        override fun getAvailableActions(state: Game, rules: Rules): List<ActionDescriptor> = listOf(RollDice(Dice.D6))
        override fun applyAction(action: GameAction, state: Game, rules: Rules): Command {
            return checkDiceRoll<D6Result>(action) { d6 ->
                val activateContext = state.getContext<ActivatePlayerContext>()
                val isSuccess = calculateSuccess(activateContext, d6)
                val rollContext = UnchannelledFuryRollContext(
                    state.activePlayer!!,
                    D6DieRoll(d6),
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
        override fun getAvailableActions(state: Game, rules: Rules): List<ActionDescriptor> {
            val context = state.getContext<UnchannelledFuryRollContext>()
            val availableRerolls = calculateAvailableRerollsFor(
                rules = rules,
                player = context.player,
                type = DiceRollType.UNCHANNELLED_FURY,
                roll = context.roll,
                firstRollWasSuccess = context.isSuccess
            )
            return if (availableRerolls.isEmpty()) {
                listOf(ContinueWhenReady)
            } else {
                listOf(SelectNoReroll(context.isSuccess)) + availableRerolls
            }
        }

        override fun applyAction(action: GameAction, state: Game, rules: Rules): Command {
            return when (action) {
                Continue -> ExitProcedure()
                NoRerollSelected -> ExitProcedure()
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
        override fun getAvailableActions(state: Game, rules: Rules): List<ActionDescriptor> = listOf(RollDice(Dice.D6))
        override fun applyAction(action: GameAction, state: Game, rules: Rules): Command {
            return checkDiceRoll<D6Result>(action) { d6 ->
                val activateContext = state.getContext<ActivatePlayerContext>()
                val rollResultContext = state.getContext<UnchannelledFuryRollContext>()
                val isSuccess = calculateSuccess(activateContext, d6)
                val rollContext = rollResultContext.copy(
                    roll = rollResultContext.roll.copy(
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
            PlayerActionType.BLOCK,
            PlayerActionType.BLITZ -> 2
            // TODO How to detect special actions that replaces Blitz/Block actions
            else -> 0
        }
        val isSuccess = (d6.value + modifier >= 4)
        return isSuccess
    }
}
