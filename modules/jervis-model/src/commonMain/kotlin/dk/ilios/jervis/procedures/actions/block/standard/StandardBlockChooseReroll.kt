package dk.ilios.jervis.procedures.actions.block.standard

import compositeCommandOf
import dk.ilios.jervis.actions.ActionDescriptor
import dk.ilios.jervis.actions.Continue
import dk.ilios.jervis.actions.ContinueWhenReady
import dk.ilios.jervis.actions.GameAction
import dk.ilios.jervis.actions.NoRerollSelected
import dk.ilios.jervis.actions.RerollOptionSelected
import dk.ilios.jervis.actions.SelectNoReroll
import dk.ilios.jervis.actions.SelectRerollOption
import dk.ilios.jervis.commands.Command
import dk.ilios.jervis.commands.SetOldContext
import dk.ilios.jervis.commands.fsm.ExitProcedure
import dk.ilios.jervis.fsm.ActionNode
import dk.ilios.jervis.fsm.Node
import dk.ilios.jervis.fsm.Procedure
import dk.ilios.jervis.model.Game
import dk.ilios.jervis.model.Player
import dk.ilios.jervis.model.Team
import dk.ilios.jervis.model.context.UseRerollContext
import dk.ilios.jervis.model.context.assertContext
import dk.ilios.jervis.model.context.getContext
import dk.ilios.jervis.procedures.BlockDieRoll
import dk.ilios.jervis.procedures.actions.block.BlockContext
import dk.ilios.jervis.rules.Rules
import dk.ilios.jervis.rules.skills.DiceRerollOption
import dk.ilios.jervis.rules.skills.DiceRollType
import dk.ilios.jervis.rules.skills.RerollSource
import dk.ilios.jervis.rules.skills.Skill
import dk.ilios.jervis.utils.INVALID_ACTION

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
                listOf(SelectRerollOption(DiceRerollOption(rules.getAvailableTeamReroll(team), diceRoll)))
            } else {
                emptyList()
            }
            listOf(SelectNoReroll(null)) + availableSkills + teamRerolls
        }
    }

}
