package dk.ilios.jervis.procedures.actions.block.multipleblock

import compositeCommandOf
import dk.ilios.jervis.actions.ActionDescriptor
import dk.ilios.jervis.actions.Continue
import dk.ilios.jervis.actions.ContinueWhenReady
import dk.ilios.jervis.actions.GameAction
import dk.ilios.jervis.actions.NoRerollSelected
import dk.ilios.jervis.actions.RerollOptionSelected
import dk.ilios.jervis.commands.Command
import dk.ilios.jervis.commands.SetContext
import dk.ilios.jervis.commands.SetOldContext
import dk.ilios.jervis.commands.fsm.ExitProcedure
import dk.ilios.jervis.commands.fsm.GotoNode
import dk.ilios.jervis.fsm.ActionNode
import dk.ilios.jervis.fsm.Node
import dk.ilios.jervis.fsm.ParentNode
import dk.ilios.jervis.fsm.Procedure
import dk.ilios.jervis.model.Game
import dk.ilios.jervis.model.Team
import dk.ilios.jervis.model.context.assertContext
import dk.ilios.jervis.model.context.getContext
import dk.ilios.jervis.procedures.actions.block.MultipleBlockContext
import dk.ilios.jervis.procedures.actions.block.MultipleBlockDiceRoll
import dk.ilios.jervis.rules.Rules
import dk.ilios.jervis.utils.INVALID_ACTION

/**
 * Multiple Block overrides the standard behaviour for re-rolling blocks, since the coach should
 * be able to sell the combined result at all times. For that reason, this procedure is tracking
 * the state of all relevant rolls and combines the actions available.
 */
object MultipleBlockRerollDice: Procedure() {
    override val initialNode: Node = ReRollSourceOrAcceptRoll
    override fun onEnterProcedure(state: Game, rules: Rules): Command? = null
    override fun onExitProcedure(state: Game, rules: Rules): Command? = null
    override fun isValid(state: Game, rules: Rules) = state.assertContext<MultipleBlockContext>()

    object ReRollSourceOrAcceptRoll : ActionNode() {
        override fun actionOwner(state: Game, rules: Rules): Team = state.getContext<MultipleBlockContext>().attacker.team
        override fun getAvailableActions(state: Game, rules: Rules): List<ActionDescriptor> {
            val context = state.getContext<MultipleBlockContext>()
            val rerolls = context.rolls.flatMapIndexed { index: Int, actionDiceRoll: MultipleBlockDiceRoll ->
                actionDiceRoll.getRerollOptions(rules, context.attacker, index)
            }
            return rerolls.ifEmpty {
                listOf(ContinueWhenReady)
            }
        }

        override fun applyAction(action: GameAction, state: Game, rules: Rules): Command {
            val context = state.getContext<MultipleBlockContext>()
            return when (action) {
                Continue -> ExitProcedure()
                is NoRerollSelected -> {
                    val updatedContext= context.copyAndUpdateHasAcceptedResult(action.dicePoolId, true)
                    SetContext(updatedContext)
                }
                is RerollOptionSelected -> {
                    // Store the index to the current active reroll, so we can easily look it up later.
                    val updatedMbContext = context.copy(activeDefender = action.dicePoolId)
                    val rerollContext = updatedMbContext.createRerollContext(state, action)
                    compositeCommandOf(
                        SetContext(updatedMbContext),
                        SetOldContext(Game::rerollContext, rerollContext),
                        GotoNode(ReRollDie),
                    )
                }
                else -> INVALID_ACTION(action)
            }
        }
    }

    /**
     * Use the selected reroll and reroll the dice (if allowed).
     */
    object ReRollDie : ParentNode() {
        override fun getChildProcedure(state: Game, rules: Rules): Procedure {
            val context = state.getContext<MultipleBlockContext>()
            return context.getRerollDiceProcedure()
        }

        override fun onExitNode(state: Game, rules: Rules): Command {
            val context = state.getContext<MultipleBlockContext>()
            val updatedContext = context.copyAndUpdateWithLatestBlockTypeContext(state)
            return compositeCommandOf(
                SetContext(updatedContext),
                GotoNode(ReRollSourceOrAcceptRoll)
            )
        }
    }
}
