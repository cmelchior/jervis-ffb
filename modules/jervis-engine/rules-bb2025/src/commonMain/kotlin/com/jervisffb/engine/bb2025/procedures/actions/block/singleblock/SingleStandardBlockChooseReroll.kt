package com.jervisffb.engine.bb2025.procedures.actions.block.singleblock

import com.jervisffb.engine.actions.Continue
import com.jervisffb.engine.actions.ContinueWhenReady
import com.jervisffb.engine.actions.GameAction
import com.jervisffb.engine.actions.GameActionDescriptor
import com.jervisffb.engine.actions.NoRerollSelected
import com.jervisffb.engine.actions.RerollOptionSelected
import com.jervisffb.engine.actions.SelectNoReroll
import com.jervisffb.engine.actions.SelectRerollOption
import com.jervisffb.engine.commands.Command
import com.jervisffb.engine.commands.buildCompositeCommand
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
import com.jervisffb.engine.model.context.UseRerollContext
import com.jervisffb.engine.model.context.assertContext
import com.jervisffb.engine.model.context.getContext
import com.jervisffb.engine.rules.Rules
import com.jervisffb.engine.rules.common.procedures.BlockDieRoll
import com.jervisffb.engine.rules.common.utils.calculateAvailableRerollsForBlock
import com.jervisffb.engine.utils.INVALID_ACTION
import com.jervisffb.engine.utils.INVALID_GAME_STATE

/**
 * TODO FUCK. This does not keep rerolls in lock-step. We need a custom node that can
 */
object SingleStandardBlockChooseReroll: Procedure() {
    override val initialNode: Node = ChooseRerollSourceOrAcceptRoll
    override fun onEnterProcedure(state: Game, rules: Rules): Command? = null
    override fun onExitProcedure(state: Game, rules: Rules): Command? = null
    override fun isValid(state: Game, rules: Rules) {
        state.assertContext<BlockContext>()
        state.assertContext<UseRerollContext>()
    }

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
                        rerollDice = action.getRerollDice(),
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
            val rerollContext = state.getRerollContext()

            return buildCompositeCommand {
                // Reroll was aborted, reset state so we can retry later
                if (rerollContext.rerollAborted) {
                    add(UpdateContext(rerollContext.copy(
                        source = null,
                        rerollDice = null,
                    )))
                }

                // If re-roll is not allowed, we still need to mark the roll as attempted
                // as it prevents other rerolls. At least this is how Pro works, and for now
                // we assume this policy will carry over to other skills if they are ever added.
                // Team Mascot has a similar mechanic, but allows you to use a different reroll
                // immediately, so this is not handled here.
                if (!rerollContext.rerollAllowed) {
                    val blockContext = state.getContext<BlockContext>()
                    val updatedRolls = blockContext.roll.toMutableList()
                    rerollContext.rerollDice?.forEach { die ->
                        die as? BlockDieRoll ?: INVALID_GAME_STATE("Unsupported die: $die")
                        val updatedDie = die.copyReroll(
                            rerollSource = rerollContext.source,
                            rerolledResult = die.result // Even though not rerolled, we treat it as such by storing the original roll here.
                        )
                        updatedRolls[updatedRolls.indexOfFirst { it.id == die.id }] = updatedDie
                    }

                    add(UpdateContext(blockContext.copy(roll = updatedRolls)))
                }

                add(ExitProcedure())
            }
        }
    }
}
