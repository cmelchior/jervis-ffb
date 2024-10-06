package com.jervisffb.engine.rules.bb2020.procedures.actions.block.standard

import compositeCommandOf
import com.jervisffb.engine.actions.ActionDescriptor
import com.jervisffb.engine.actions.Continue
import com.jervisffb.engine.actions.ContinueWhenReady
import com.jervisffb.engine.actions.GameAction
import com.jervisffb.engine.actions.NoRerollSelected
import com.jervisffb.engine.actions.RerollOptionSelected
import com.jervisffb.engine.actions.SelectNoReroll
import com.jervisffb.engine.actions.SelectRerollOption
import com.jervisffb.engine.commands.Command
import com.jervisffb.engine.commands.SetOldContext
import com.jervisffb.engine.commands.fsm.ExitProcedure
import com.jervisffb.engine.fsm.ActionNode
import com.jervisffb.engine.fsm.Node
import com.jervisffb.engine.fsm.Procedure
import com.jervisffb.engine.model.Game
import com.jervisffb.engine.model.Player
import com.jervisffb.engine.model.Team
import com.jervisffb.engine.model.context.UseRerollContext
import com.jervisffb.engine.model.context.assertContext
import com.jervisffb.engine.model.context.getContext
import com.jervisffb.engine.rules.bb2020.procedures.BlockDieRoll
import com.jervisffb.engine.rules.bb2020.procedures.actions.block.BlockContext
import com.jervisffb.engine.rules.Rules
import com.jervisffb.engine.rules.bb2020.skills.DiceRerollOption
import com.jervisffb.engine.rules.bb2020.skills.DiceRollType
import com.jervisffb.engine.rules.bb2020.skills.RerollSource
import com.jervisffb.engine.rules.bb2020.skills.Skill
import com.jervisffb.engine.utils.INVALID_ACTION

/**
 * TODO FUCK. This does not keep rerolls in lock-step. We need a custom node that can
 */
object StandardBlockChooseReroll: Procedure() {
    override val initialNode: Node = ReRollSourceOrAcceptRoll
    override fun onEnterProcedure(state: Game, rules: Rules): Command? = null
    override fun onExitProcedure(state: Game, rules: Rules): Command? = null
    override fun isValid(state: Game, rules: Rules) {
        state.assertContext<BlockContext>()
    }

    object ReRollSourceOrAcceptRoll : ActionNode() {
        override fun actionOwner(state: Game, rules: Rules): Team = state.getContext<BlockContext>().attacker.team
        override fun getAvailableActions(state: Game, rules: Rules): List<ActionDescriptor> {
            val context = state.getContext<BlockContext>()
            val attackingPlayer = context.attacker

            val rerolls = getRerollOptions(rules, attackingPlayer, context.roll)
            return rerolls.ifEmpty {
                listOf(ContinueWhenReady)
            }
        }

        override fun applyAction(action: GameAction, state: Game, rules: Rules): Command {
            return when (action) {
                // TODO What is the difference between Continue and NoRerollSelected
                Continue,
                is NoRerollSelected -> {
                    compositeCommandOf(
                        SetOldContext(Game::rerollContext, null),
                        ExitProcedure()
                    )
                }
                is RerollOptionSelected -> {
                    val rerollContext = UseRerollContext(DiceRollType.BLOCK, action.getRerollSource(state))
                    compositeCommandOf(
                        SetOldContext(Game::rerollContext, rerollContext),
                        ExitProcedure()
                    )
                }
                else -> INVALID_ACTION(action)
            }
        }
    }


    // ------------------------------------------------------------------------------------------------------------
    // HELPER FUNCTIONS

    fun getRerollOptions(rules: Rules, attackingPlayer: Player, diceRoll: List<BlockDieRoll>): List<ActionDescriptor> {
        // Re-rolling block dice can be pretty complex,
        // Brawler: Can reroll a single "Both Down"
        // Pro: Can reroll any single die
        // Team reroll: Can reroll all of them
        val availableSkills: List<SelectRerollOption> =
            attackingPlayer.skills
                .filter { skill: Skill -> skill is RerollSource }
                .map { it as RerollSource }
                .filter { it.canReroll(DiceRollType.BLOCK, diceRoll) }
                .flatMap { it.calculateRerollOptions(DiceRollType.BLOCK, diceRoll) }
                .map { rerollOption: DiceRerollOption -> SelectRerollOption(rerollOption) }

        val team = attackingPlayer.team
        val hasTeamRerolls = team.availableRerollCount > 0
        val allowedToUseTeamReroll =
            when (team.usedRerollThisTurn) {
                true -> rules.allowMultipleTeamRerollsPrTurn
                false -> true
            }

        return if (availableSkills.isEmpty() && (!hasTeamRerolls || !allowedToUseTeamReroll)) {
            emptyList()
        } else {
            val teamRerolls = if (hasTeamRerolls && allowedToUseTeamReroll) {
                listOf(
                    SelectRerollOption(
                    DiceRerollOption(
                        rules.getAvailableTeamReroll(
                            team
                        ), diceRoll
                    )
                )
                )
            } else {
                emptyList()
            }
            listOf(SelectNoReroll(null)) + availableSkills + teamRerolls
        }
    }

}
