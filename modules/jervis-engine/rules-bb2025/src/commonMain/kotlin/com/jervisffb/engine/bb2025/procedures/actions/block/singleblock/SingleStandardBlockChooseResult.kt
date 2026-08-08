package com.jervisffb.engine.bb2025.procedures.actions.block.singleblock

import com.jervisffb.engine.actions.BlockDicePool
import com.jervisffb.engine.actions.DBlockResult
import com.jervisffb.engine.actions.GameAction
import com.jervisffb.engine.actions.GameActionDescriptor
import com.jervisffb.engine.actions.SelectDicePoolResult
import com.jervisffb.engine.commands.Command
import com.jervisffb.engine.commands.buildCompositeCommand
import com.jervisffb.engine.commands.compositeCommandOf
import com.jervisffb.engine.commands.context.UpdateContext
import com.jervisffb.engine.commands.fsm.ExitProcedure
import com.jervisffb.engine.commands.probabiliy.UpdateChanceObservation
import com.jervisffb.engine.common.context.BlockContext
import com.jervisffb.engine.fsm.ActionNode
import com.jervisffb.engine.fsm.Node
import com.jervisffb.engine.fsm.Procedure
import com.jervisffb.engine.fsm.castDicePool
import com.jervisffb.engine.model.Game
import com.jervisffb.engine.model.Team
import com.jervisffb.engine.model.context.UseRerollContext
import com.jervisffb.engine.model.context.assertContext
import com.jervisffb.engine.model.context.getContext
import com.jervisffb.engine.rules.Rules
import com.jervisffb.engine.utils.INVALID_GAME_STATE

/**
 * Roll block dice for the first time.
 *
 * @see [MultipleBlockAction]
 * @see [StandardBlockStep]
 */
object SingleStandardBlockChooseResult: Procedure() {
    override val initialNode: Node = SelectBlockResult
    override fun onEnterProcedure(state: Game, rules: Rules): Command? = null
    override fun onExitProcedure(state: Game, rules: Rules): Command? = null
    override fun isValid(state: Game, rules: Rules) {
        state.assertContext<BlockContext>()
    }

    object SelectBlockResult : ActionNode() {
        override fun actionOwner(state: Game, rules: Rules): Team {
            val context = state.getContext<BlockContext>()
            return context.getTeamSelectingResult()
        }
        override fun getAvailableActions(state: Game, rules: Rules): List<GameActionDescriptor> {
            return listOf(
                SelectDicePoolResult(BlockDicePool(state.getContext<BlockContext>().roll))
            )
        }

        override fun applyAction(action: GameAction, state: Game, rules: Rules): Command {
            return castDicePool<DBlockResult>(action) { selectedDie ->
                val context = state.getContext<BlockContext>()
                var selectedIndex = -1
                for (i in context.roll.indices) {
                    // This might select another index if two dice have the same value
                    // Does it matter?
                    if (context.roll[i].result == selectedDie) {
                        selectedIndex = i
                        break
                    }
                }
                if (selectedIndex == -1) {
                    INVALID_GAME_STATE("No matching roll for $selectedDie: ${context.roll}")
                }
                compositeCommandOf(
                    UpdateContext(context.copy(resultIndex = selectedIndex)),
                    finalizeChanceBlockRoll(state, context, selectedIndex),
                    ExitProcedure()
                )
            }
        }
    }

    private fun finalizeChanceBlockRoll(
        state: Game,
        context: BlockContext,
        selectedIndex: Int,
    ): Command? {
        if (!state.collectChanceData) return null
        val rerollContext = state.getContext<UseRerollContext>()
        val rootSequence = rerollContext.chanceRollIndex ?: return null
        val observations = rerollContext.chanceObservations
        val root = observations.firstOrNull { it.index == rootSequence } ?: return null
        val selectedRoll = context.roll[selectedIndex]
        val selectedResult = observations
            .asReversed()
            .asSequence()
            .flatMap { it.dice.asSequence() }
            .firstOrNull { it.dieId == selectedRoll.id && it.result == selectedRoll.result }
            ?: INVALID_GAME_STATE("Could not find the selected block result in chance observations.")

        return buildCompositeCommand {
            observations.forEach { observation ->
                val updated = if (observation.index == rootSequence) {
                    observation.copy(
                        selectedResultIds = listOf(selectedResult.id),
                        selectedBy = context.getTeamSelectingResult().id,
                        selectedReroll = observation.selectedReroll?.copy(
                            allowed = rerollContext.rerollAllowed,
                            aborted = rerollContext.rerollAborted,
                        ),
                        finalized = true,
                    )
                } else {
                    observation.copy(finalized = true)
                }
                add(UpdateChanceObservation(observation.index, observation, updated))
            }
        }
    }
}
