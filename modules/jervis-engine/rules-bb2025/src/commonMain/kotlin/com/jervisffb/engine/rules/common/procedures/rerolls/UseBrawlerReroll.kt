package com.jervisffb.engine.rules.common.procedures.rerolls

import com.jervisffb.engine.actions.BlockDice
import com.jervisffb.engine.actions.BlockDicePool
import com.jervisffb.engine.actions.Cancel
import com.jervisffb.engine.actions.CancelWhenReady
import com.jervisffb.engine.actions.Continue
import com.jervisffb.engine.actions.ContinueWhenReady
import com.jervisffb.engine.actions.DBlockResult
import com.jervisffb.engine.actions.DicePoolResultsSelected
import com.jervisffb.engine.actions.GameAction
import com.jervisffb.engine.actions.GameActionDescriptor
import com.jervisffb.engine.actions.SelectDicePoolResult
import com.jervisffb.engine.commands.Command
import com.jervisffb.engine.commands.SetSkillRerollUsed
import com.jervisffb.engine.commands.SetSkillUsed
import com.jervisffb.engine.commands.compositeCommandOf
import com.jervisffb.engine.commands.context.UpdateContext
import com.jervisffb.engine.commands.fsm.ExitProcedure
import com.jervisffb.engine.commands.fsm.GotoNode
import com.jervisffb.engine.fsm.ActionNode
import com.jervisffb.engine.fsm.ComputationNode
import com.jervisffb.engine.fsm.Node
import com.jervisffb.engine.fsm.Procedure
import com.jervisffb.engine.model.Game
import com.jervisffb.engine.model.Team
import com.jervisffb.engine.reports.ReportSkillUsed
import com.jervisffb.engine.rules.Rules
import com.jervisffb.engine.rules.common.procedures.BlockDieRoll
import com.jervisffb.engine.rules.common.skills.SkillType
import com.jervisffb.engine.utils.INVALID_ACTION
import com.jervisffb.engine.utils.INVALID_GAME_STATE
import com.jervisffb.engine.utils.assert

/**
 * Procedure controlling the use of the Brawler skill to reroll a die. Brawler can only
 * be used to re-roll Both Down results, which must be selected as part of this
 * procedure.
 *
 * Note, the UI can choose to hide this and just select _any_ of the available
 * Both Down results.
 */
object UseBrawlerReroll : Procedure() {
    override val initialNode: Node = SelectBothDownToReroll
    override fun onEnterProcedure(state: Game, rules: Rules): Command {
        val rerollContext = state.getRerollContext()
        return ReportSkillUsed(rerollContext.player!!, SkillType.BRAWLER)
    }
    override fun onExitProcedure(state: Game, rules: Rules): Command? = null
    override fun isValid(state: Game, rules: Rules) {
        val context = state.getRerollContext()
        assert(context.player != null) { "Missing player: $context"}
        assert(context.source != null) { "Missing reroll source: $context"}
        assert(context.originalRoll.isNotEmpty()) { "No dice available to select from: $context"}
    }

    // Select die from the dice pool to reroll. We must select it before rolling for Pro because if
    // Pro fails, the die still counts as being rerolled
    object SelectBothDownToReroll: ActionNode() {
        override fun actionOwner(state: Game, rules: Rules): Team = state.getRerollContext().team
        override fun getAvailableActions(state: Game, rules: Rules): List<GameActionDescriptor> {
            val context = state.getRerollContext()

            // Restrict the pool to only both-down dice
            val pool = when (context.originalRoll.first()) {
                is BlockDieRoll -> {
                    @Suppress("UNCHECKED_CAST")
                    val roll = context.originalRoll as List<BlockDieRoll>
                    BlockDicePool(roll.filter { it.result.blockResult == BlockDice.BOTH_DOWN })
                }
                else -> INVALID_GAME_STATE("Unsupported roll type: $context")
            }

            // Automatically select die when there is only one available
            return if (pool.dice.size == 1) {
                listOf(ContinueWhenReady)
            } else {
                listOf(SelectDicePoolResult(pool), CancelWhenReady)
            }
        }
        override fun applyAction(action: GameAction, state: Game, rules: Rules): Command {
            val context = state.getRerollContext()
            val player = context.player ?: return INVALID_GAME_STATE("Player not found in reroll context: $context")
            return when (action) {
                Continue -> {
                    // Pool only consists of a single available die
                    val bothDownDice = context.originalRoll.filter  { (it.result as DBlockResult).blockResult == BlockDice.BOTH_DOWN }
                    compositeCommandOf(
                        UpdateContext(
                            context.copy(
                                rerollDice = bothDownDice,
                                rerollAllowed = true,
                            )
                        ),
                        GotoNode(UseSkill)
                    )
                }
                Cancel -> {
                    // Brawler roll was aborted
                    compositeCommandOf(
                        UpdateContext(context.copy(rerollAborted = true)),
                        ExitProcedure()
                    )
                }
                is DicePoolResultsSelected -> {
                    // A die was selected to be rerolled by the Brawler skill
                    val userSelection = action.results.single().diceSelected.single()
                    val selectedDie = context.originalRoll.first { it.id == userSelection.id }
                    compositeCommandOf(
                        UpdateContext(
                            context.copy(
                                rerollDice = listOf(selectedDie),
                                rerollAllowed = true,
                            )
                        ),
                        GotoNode(UseSkill)
                    )
                }
                else -> INVALID_ACTION(action)
            }
        }
    }

    object UseSkill: ComputationNode() {
        override fun apply(state: Game, rules: Rules): Command {
            val context = state.getRerollContext()
            val player = context.player ?: INVALID_GAME_STATE("Player not found in reroll context: $context")
            val rerollSource = context.source ?: INVALID_GAME_STATE("Reroll source not found in reroll context: $context")
            return compositeCommandOf(
                SetSkillUsed(player, player.getSkill(SkillType.BRAWLER), used = true),
                SetSkillRerollUsed(rerollSource, used = true),
                ExitProcedure()
            )
        }
    }
}
