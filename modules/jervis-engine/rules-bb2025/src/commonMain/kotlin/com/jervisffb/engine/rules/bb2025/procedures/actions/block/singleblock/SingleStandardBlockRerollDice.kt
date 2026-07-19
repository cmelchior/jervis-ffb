package com.jervisffb.engine.rules.bb2025.procedures.actions.block.singleblock

import com.jervisffb.engine.actions.DBlockResult
import com.jervisffb.engine.actions.Dice
import com.jervisffb.engine.actions.GameAction
import com.jervisffb.engine.actions.GameActionDescriptor
import com.jervisffb.engine.actions.RollDice
import com.jervisffb.engine.commands.Command
import com.jervisffb.engine.commands.compositeCommandOf
import com.jervisffb.engine.commands.context.UpdateContext
import com.jervisffb.engine.commands.fsm.ExitProcedure
import com.jervisffb.engine.fsm.ActionNode
import com.jervisffb.engine.fsm.Node
import com.jervisffb.engine.fsm.Procedure
import com.jervisffb.engine.fsm.castDiceRollList
import com.jervisffb.engine.model.Game
import com.jervisffb.engine.model.Team
import com.jervisffb.engine.model.context.BlockContext
import com.jervisffb.engine.model.context.assertContext
import com.jervisffb.engine.model.context.getContext
import com.jervisffb.engine.reports.ReportDiceRoll
import com.jervisffb.engine.rules.DiceRollType
import com.jervisffb.engine.rules.Rules
import com.jervisffb.engine.utils.INVALID_GAME_STATE
import com.jervisffb.engine.utils.assert
import kotlin.collections.forEachIndexed

/**
 * Use a reroll and then reroll the block dice (if allowed).
 */
object SingleStandardBlockRerollDice: Procedure() {
    override val initialNode: Node = ReRollDie
    override fun onEnterProcedure(state: Game, rules: Rules): Command? = null
    override fun onExitProcedure(state: Game, rules: Rules): Command? = null
    override fun isValid(state: Game, rules: Rules) {
        assert(state.getRerollContextOrNull() != null)
        state.assertContext<BlockContext>()
    }

    object ReRollDie : ActionNode() {
        override fun actionOwner(state: Game, rules: Rules): Team = state.getContext<BlockContext>().attacker.team
        override fun getAvailableActions(state: Game, rules: Rules): List<GameActionDescriptor> {
            val rerollContext = state.getRerollContext()
            val dice = rerollContext.rerollDice ?: INVALID_GAME_STATE("Cannot determine number of dice: $rerollContext")
            return listOf(RollDice(List(dice.size) { Dice.BLOCK }))
        }

        override fun applyAction(action: GameAction, state: Game, rules: Rules): Command {
            // TODO The dice must be rolled in the order they appear in the Reroll option.
            //  Is this an acceptable restriction? As a minimum it should be documented somewhere.
            return castDiceRollList<DBlockResult>(action) { rerolls: List<DBlockResult> ->
                val rerollContext = state.getRerollContext()
                val blockContext = state.getContext<BlockContext>()
                val rerollOptionDice = rerollContext.rerollDice ?: INVALID_GAME_STATE("Cannot determine number of dice: $rerollContext")
                val updatedRoll = blockContext.roll.toMutableList()
                rerolls.forEachIndexed { i, blockRoll ->
                    val idToUpdate = rerollOptionDice[i].id
                    val indexToUpdate = updatedRoll.indexOfFirst { it.id == idToUpdate }
                    updatedRoll[indexToUpdate] = updatedRoll[indexToUpdate].copyReroll(
                        rerollSource = rerollContext.source,
                        rerolledResult = blockRoll
                    )
                }
                compositeCommandOf(
                    ReportDiceRoll(DiceRollType.BLOCK, rerolls),
                    UpdateContext(blockContext.copy(roll = updatedRoll)),
                    ExitProcedure(),
                )
            }
        }
    }
}
