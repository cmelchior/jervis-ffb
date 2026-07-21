package com.jervisffb.engine.common.procedures.rerolls

import com.jervisffb.engine.actions.BlockDicePool
import com.jervisffb.engine.actions.Cancel
import com.jervisffb.engine.actions.CancelWhenReady
import com.jervisffb.engine.actions.Continue
import com.jervisffb.engine.actions.ContinueWhenReady
import com.jervisffb.engine.actions.D6DicePool
import com.jervisffb.engine.actions.DicePoolResultsSelected
import com.jervisffb.engine.actions.GameAction
import com.jervisffb.engine.actions.GameActionDescriptor
import com.jervisffb.engine.actions.SelectDicePoolResult
import com.jervisffb.engine.commands.Command
import com.jervisffb.engine.commands.SetSkillRerollUsed
import com.jervisffb.engine.commands.SetSkillUsed
import com.jervisffb.engine.commands.compositeCommandOf
import com.jervisffb.engine.commands.context.AddContext
import com.jervisffb.engine.commands.context.RemoveContext
import com.jervisffb.engine.commands.context.UpdateContext
import com.jervisffb.engine.commands.fsm.ExitProcedure
import com.jervisffb.engine.commands.fsm.GotoNode
import com.jervisffb.engine.fsm.ActionNode
import com.jervisffb.engine.fsm.Node
import com.jervisffb.engine.fsm.ParentNode
import com.jervisffb.engine.fsm.Procedure
import com.jervisffb.engine.model.Game
import com.jervisffb.engine.model.Team
import com.jervisffb.engine.model.context.UseRerollContext
import com.jervisffb.engine.model.context.getContext
import com.jervisffb.engine.common.reports.ReportProResult
import com.jervisffb.engine.common.reports.ReportSkillUsed
import com.jervisffb.engine.rules.Rules
import com.jervisffb.engine.rules.common.procedures.BlockDieRoll
import com.jervisffb.engine.rules.common.procedures.D6DieRoll
import com.jervisffb.engine.rules.common.skills.SkillType
import com.jervisffb.engine.utils.INVALID_ACTION
import com.jervisffb.engine.utils.INVALID_GAME_STATE
import com.jervisffb.engine.utils.assert

/**
 * Procedure controlling the use of Pro to reroll a die. If succesful, it will
 * set [UseRerollContext.rerollAllowed] to `true`, allowing the caller
 * to continue with the reroll.
 */
object UseProReroll : Procedure() {
    override val initialNode: Node = SelectDieToReroll
    override fun onEnterProcedure(state: Game, rules: Rules): Command? = null
    override fun onExitProcedure(state: Game, rules: Rules): Command? = null
    override fun isValid(state: Game, rules: Rules) {
        val context = state.getRerollContext()
        assert(context.player != null) { "Missing player: $context"}
        assert(context.source != null) { "Missing reroll source: $context"}
        assert(context.originalRoll.isNotEmpty()) { "No dice available to select from: $context"}
    }

    // Select die from the dice pool to reroll. We must select it before rolling for Pro because if
    // Pro fails, the die still counts as being rerolled
    object SelectDieToReroll: ActionNode() {
        override fun actionOwner(state: Game, rules: Rules): Team = state.getRerollContext().team
        override fun getAvailableActions(state: Game, rules: Rules): List<GameActionDescriptor> {
            val context = state.getRerollContext()

            // Automatically select die when there is only one available
            if (context.originalRoll.size == 1) {
                return listOf(ContinueWhenReady)
            }

            val pool = when (context.originalRoll.first()) {
                is BlockDieRoll -> {
                    @Suppress("UNCHECKED_CAST")
                    val roll = context.originalRoll as List<BlockDieRoll>
                    BlockDicePool(roll)
                }
                is D6DieRoll -> {
                    @Suppress("UNCHECKED_CAST")
                    val roll = context.originalRoll as List<D6DieRoll>
                    D6DicePool(roll)
                }
                else -> INVALID_GAME_STATE("Unsupported roll type: $context")
            }

            return listOf(SelectDicePoolResult(pool), CancelWhenReady)
        }
        override fun applyAction(action: GameAction, state: Game, rules: Rules): Command {
            val context = state.getRerollContext()

            return when (action) {
                Continue -> {
                    // Pool only consists of a single die
                    compositeCommandOf(
                        UpdateContext(context.copy(rerollDice = context.originalRoll)),
                        GotoNode(RollProDie)
                    )
                }
                Cancel -> {
                    // Pro roll was aborted
                    compositeCommandOf(
                        UpdateContext(context.copy(rerollAborted = true)),
                        ExitProcedure()
                    )
                }
                is DicePoolResultsSelected -> {
                    // A die was selected to be rerolled by the Pro skill
                    val userSelection = action.results.single().diceSelected.single()
                    val selectedDie = context.originalRoll.first { it.id == userSelection.id }
                    compositeCommandOf(
                        UpdateContext(context.copy(rerollDice = listOf(selectedDie))),
                        GotoNode(RollProDie)
                    )
                }
                else -> INVALID_ACTION(action)
            }
        }
    }

    object RollProDie: ParentNode() {
        override fun onEnterNode(state: Game, rules: Rules): Command {
            val rerollContext = state.getRerollContext()
            val rerollSource = rerollContext.source!!
            val player = rerollContext.player!!
            val rollContext = ProRollContext(player)
            return compositeCommandOf(
                ReportSkillUsed(player, SkillType.PRO),
                SetSkillUsed(player, player.getSkill(SkillType.PRO), used = true),
                SetSkillRerollUsed(rerollSource, used = true),
                AddContext(rollContext)
            )
        }
        override fun getChildProcedure(state: Game, rules: Rules): Procedure = ProRoll
        override fun onExitNode(state: Game, rules: Rules): Command {
            val rerollContext = state.getRerollContext()
            val rollContext = state.getContext<ProRollContext>()
            return compositeCommandOf(
                RemoveContext(rollContext),
                ReportProResult(rollContext, rerollContext.type),
                UpdateContext(rerollContext.copy(
                    rerollAllowed = rollContext.isSuccess
                )),
                ExitProcedure()
            )
        }
    }
}
