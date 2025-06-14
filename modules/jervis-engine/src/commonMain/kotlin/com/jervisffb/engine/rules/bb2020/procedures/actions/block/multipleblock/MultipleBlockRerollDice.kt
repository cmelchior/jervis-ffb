package com.jervisffb.engine.rules.bb2020.procedures.actions.block.multipleblock

import com.jervisffb.engine.actions.Continue
import com.jervisffb.engine.actions.ContinueWhenReady
import com.jervisffb.engine.actions.GameAction
import com.jervisffb.engine.actions.GameActionDescriptor
import com.jervisffb.engine.actions.NoRerollSelected
import com.jervisffb.engine.actions.RerollOptionSelected
import com.jervisffb.engine.commands.Command
import com.jervisffb.engine.commands.SetOldContext
import com.jervisffb.engine.commands.compositeCommandOf
import com.jervisffb.engine.commands.fsm.ExitProcedure
import com.jervisffb.engine.commands.fsm.GotoNode
import com.jervisffb.engine.fsm.ActionNode
import com.jervisffb.engine.fsm.Node
import com.jervisffb.engine.fsm.ParentNode
import com.jervisffb.engine.fsm.Procedure
import com.jervisffb.engine.model.Game
import com.jervisffb.engine.model.Team
import com.jervisffb.engine.model.context.assertContext
import com.jervisffb.engine.model.context.getContext
import com.jervisffb.engine.rules.Rules
import com.jervisffb.engine.rules.bb2020.procedures.actions.block.MultipleBlockContext
import com.jervisffb.engine.rules.bb2020.procedures.actions.block.MultipleBlockDiceRoll
import com.jervisffb.engine.utils.INVALID_ACTION

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
        override fun getAvailableActions(state: Game, rules: Rules): List<GameActionDescriptor> {
            val context = state.getContext<MultipleBlockContext>()
            val rerolls = context.rolls.flatMapIndexed { index: Int, actionDiceRoll: MultipleBlockDiceRoll ->
                if (!actionDiceRoll.hasAcceptedResult()) {
                    actionDiceRoll.getRerollOptions(rules, context.attacker, index)
                } else {
                    emptyList()
                }
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
