package com.jervisffb.engine.rules.bb2025.procedures.actions.block.singleblock

import com.jervisffb.engine.actions.Continue
import com.jervisffb.engine.actions.ContinueWhenReady
import com.jervisffb.engine.actions.GameAction
import com.jervisffb.engine.actions.GameActionDescriptor
import com.jervisffb.engine.actions.NoRerollSelected
import com.jervisffb.engine.actions.RerollOptionSelected
import com.jervisffb.engine.actions.SelectNoReroll
import com.jervisffb.engine.actions.SelectRerollOption
import com.jervisffb.engine.commands.Command
import com.jervisffb.engine.commands.compositeCommandOf
import com.jervisffb.engine.commands.context.UpdateContext
import com.jervisffb.engine.commands.fsm.ExitProcedure
import com.jervisffb.engine.commands.fsm.GotoNode
import com.jervisffb.engine.fsm.ActionNode
import com.jervisffb.engine.fsm.Node
import com.jervisffb.engine.fsm.ParentNode
import com.jervisffb.engine.fsm.Procedure
import com.jervisffb.engine.model.Game
import com.jervisffb.engine.model.Team
import com.jervisffb.engine.model.context.BlockContext
import com.jervisffb.engine.model.context.assertContext
import com.jervisffb.engine.model.context.getContext
import com.jervisffb.engine.rules.Rules
import com.jervisffb.engine.utils.INVALID_ACTION
import com.jervisffb.engine.utils.INVALID_GAME_STATE
import com.jervisffb.engine.utils.calculateAvailableRerollsForBlock

/**
 * TODO FUCK. This does not keep rerolls in lock-step. We need a custom node that can
 */
object SingleStandardBlockChooseReroll: Procedure() {
    override val initialNode: Node = ChooseRerollSourceOrAcceptRoll
    override fun onEnterProcedure(state: Game, rules: Rules): Command? = null
    override fun onExitProcedure(state: Game, rules: Rules): Command? = null
    override fun isValid(state: Game, rules: Rules) = state.assertContext<BlockContext>()

    object ChooseRerollSourceOrAcceptRoll : ActionNode() {
        override fun actionOwner(state: Game, rules: Rules): Team = state.getContext<BlockContext>().attacker.team
        override fun getAvailableActions(state: Game, rules: Rules): List<GameActionDescriptor> {
            val context = state.getContext<BlockContext>()
            val attackingPlayer = context.attacker
            val rerollOptions = calculateAvailableRerollsForBlock(attackingPlayer, context.roll)
            return when (rerollOptions.isEmpty()) {
                true -> listOf(ContinueWhenReady)
                false -> listOf(SelectNoReroll(rollSuccessful = null), SelectRerollOption(rerollOptions))
            }
        }

        override fun applyAction(action: GameAction, state: Game, rules: Rules): Command {
            return when (action) {
                Continue,
                is NoRerollSelected -> ExitProcedure()
                is RerollOptionSelected -> {
                    val context = state.getRerollContext()
                    val rerollContext = context.copy(
                        source = action.getRerollSource(state),
                        selectedRerollOption = action.option
                    )
                    compositeCommandOf(
                        UpdateContext(rerollContext),
                        GotoNode(UseReroll)
                    )
                }
                else -> INVALID_ACTION(action)
            }
        }
    }

    object UseReroll: ParentNode() {
        override fun getChildProcedure(state: Game, rules: Rules): Procedure {
            return state.getRerollContextOrNull()?.source?.rerollProcedure ?: INVALID_GAME_STATE("Missing reroll source: ${state.getRerollContextOrNull()}")
        }
        override fun onExitNode(state: Game, rules: Rules): Command {
            return ExitProcedure()
        }
    }
}
