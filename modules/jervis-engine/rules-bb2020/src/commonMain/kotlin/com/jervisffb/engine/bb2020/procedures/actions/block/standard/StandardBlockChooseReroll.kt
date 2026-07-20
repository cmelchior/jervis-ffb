package com.jervisffb.engine.rules.bb2020.procedures.actions.block.standard

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
import com.jervisffb.engine.fsm.ActionNode
import com.jervisffb.engine.fsm.Node
import com.jervisffb.engine.fsm.Procedure
import com.jervisffb.engine.model.Game
import com.jervisffb.engine.model.Player
import com.jervisffb.engine.model.Team
import com.jervisffb.engine.model.context.BlockContext
import com.jervisffb.engine.model.context.assertContext
import com.jervisffb.engine.model.context.getContext
import com.jervisffb.engine.rules.DiceRollType
import com.jervisffb.engine.rules.Rules
import com.jervisffb.engine.rules.common.procedures.BlockDieRoll
import com.jervisffb.engine.rules.common.rerolls.DiceRerollOption
import com.jervisffb.engine.rules.common.skills.RerollSource
import com.jervisffb.engine.rules.common.skills.Skill
import com.jervisffb.engine.utils.INVALID_ACTION

/**
 * TODO FUCK. This does not keep rerolls in lock-step. We need a custom node that can
 */
object StandardBlockChooseReroll: Procedure() {
    override val initialNode: Node = ReRollSourceOrAcceptRoll
    override fun onEnterProcedure(state: Game, rules: Rules): Command? = null
    override fun onExitProcedure(state: Game, rules: Rules): Command? = null
    override fun isValid(state: Game, rules: Rules) = state.assertContext<BlockContext>()

    object ReRollSourceOrAcceptRoll : ActionNode() {
        override fun actionOwner(state: Game, rules: Rules): Team = state.getContext<BlockContext>().attacker.team
        override fun getAvailableActions(state: Game, rules: Rules): List<GameActionDescriptor> {
            val context = state.getContext<BlockContext>()
            val attackingPlayer = context.attacker
            val rerolls = getRerollOptions(rules, attackingPlayer, context.roll)
            return rerolls.ifEmpty {
                listOf(ContinueWhenReady)
            }
        }

        override fun applyAction(action: GameAction, state: Game, rules: Rules): Command {
            return when (action) {
                Continue,
                is NoRerollSelected -> ExitProcedure()
                is RerollOptionSelected -> {
                    val context = state.getRerollContext()
                    val updatedContext = context.copy(
                        source = action.getRerollSource(state),
                        rerollDice = action.getRerollDice(),
                    )
                    compositeCommandOf(
                        UpdateContext(updatedContext),
                        ExitProcedure()
                    )
                }
                else -> INVALID_ACTION(action)
            }
        }
    }

    // ------------------------------------------------------------------------------------------------------------
    // HELPER FUNCTIONS

    fun getRerollOptions(rules: Rules, attackingPlayer: Player, diceRoll: List<BlockDieRoll>): List<GameActionDescriptor> {
        // Re-rolling block dice can be pretty complex,
        // Brawler: Can reroll a single "Both Down"
        // Pro: Can reroll any single die
        // Team reroll: Can reroll all of them
        val availableSkills: List<DiceRerollOption> =
            attackingPlayer.skills
                .filter { skill: Skill<*> -> skill is RerollSource }
                .map { it as RerollSource }
                .filter { it.canReroll(attackingPlayer.team.game, DiceRollType.BLOCK, diceRoll) }
                .flatMap { it.calculateRerollOptions(DiceRollType.BLOCK, diceRoll) }

        val team = attackingPlayer.team
        val hasTeamRerolls = team.availableRerollCount > 0
        val allowedToUseTeamReroll =
            when {
                !team.game.canUseTeamRerolls -> false
                team.usedRerollThisTurn && rules.allowMultipleTeamRerollsPrTurn -> true
                !team.usedRerollThisTurn -> true
                else -> false
            }

        return if (availableSkills.isEmpty() && (!hasTeamRerolls || !allowedToUseTeamReroll)) {
            emptyList()
        } else {
            val teamRerolls: List<DiceRerollOption> = when (hasTeamRerolls && allowedToUseTeamReroll) {
                true -> {
                    rules.getAvailableTeamRerolls(team).map {
                        DiceRerollOption(it.id, diceRoll)
                    }
                }
                false -> emptyList()
            }
            listOf(SelectNoReroll(null), SelectRerollOption(availableSkills + teamRerolls))
        }
    }

}
