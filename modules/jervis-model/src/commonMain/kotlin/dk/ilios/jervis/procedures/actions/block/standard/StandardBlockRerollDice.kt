package dk.ilios.jervis.procedures.actions.block.standard

import compositeCommandOf
import dk.ilios.jervis.actions.ActionDescriptor
import dk.ilios.jervis.actions.DBlockResult
import dk.ilios.jervis.actions.Dice
import dk.ilios.jervis.actions.GameAction
import dk.ilios.jervis.actions.RollDice
import dk.ilios.jervis.actions.SelectNoReroll
import dk.ilios.jervis.actions.SelectRerollOption
import dk.ilios.jervis.commands.Command
import dk.ilios.jervis.commands.SetContext
import dk.ilios.jervis.commands.fsm.ExitProcedure
import dk.ilios.jervis.commands.fsm.GotoNode
import dk.ilios.jervis.fsm.ActionNode
import dk.ilios.jervis.fsm.Node
import dk.ilios.jervis.fsm.ParentNode
import dk.ilios.jervis.fsm.Procedure
import dk.ilios.jervis.model.Game
import dk.ilios.jervis.model.Player
import dk.ilios.jervis.model.Team
import dk.ilios.jervis.model.context.assertContext
import dk.ilios.jervis.model.context.getContext
import dk.ilios.jervis.procedures.BlockDieRoll
import dk.ilios.jervis.procedures.actions.block.BlockContext
import dk.ilios.jervis.rules.Rules
import dk.ilios.jervis.rules.skills.DiceRerollOption
import dk.ilios.jervis.rules.skills.DiceRollType
import dk.ilios.jervis.rules.skills.RerollSource
import dk.ilios.jervis.rules.skills.Skill
import dk.ilios.jervis.utils.INVALID_GAME_STATE

/**
 * Use a reroll and then reroll the block dice (if allowed).
 */
object StandardBlockRerollDice: Procedure() {
    override val initialNode: Node = UseRerollSource
    override fun onEnterProcedure(state: Game, rules: Rules): Command? = null
    override fun onExitProcedure(state: Game, rules: Rules): Command? = null
    override fun isValid(state: Game, rules: Rules) {
        if (state.rerollContext == null) INVALID_GAME_STATE("Missing reroll context.")
        state.assertContext<BlockContext>()
    }

    object UseRerollSource : ParentNode() {
        override fun getChildProcedure(state: Game, rules: Rules, ): Procedure = state.rerollContext!!.source.rerollProcedure
        override fun onExitNode( state: Game, rules: Rules): Command {
            // useRerollResult must be set by the procedure running which determines if a reroll is allowed
            return if (state.rerollContext!!.rerollAllowed) {
                GotoNode(ReRollDie)
            } else {
                ExitProcedure()
            }
        }
    }

    object ReRollDie : ActionNode() {
        override fun actionOwner(state: Game, rules: Rules): Team = state.getContext<BlockContext>().attacker.team
        override fun getAvailableActions(state: Game, rules: Rules): List<ActionDescriptor> {
            val noOfDice = calculateNoOfBlockDice(state)
            return listOf(RollDice(List(noOfDice) { Dice.BLOCK }))
        }

        override fun applyAction(action: GameAction, state: Game, rules: Rules): Command {
            return checkDiceRollList<DBlockResult>(action) { rolls: List<DBlockResult> ->
                val roll =
                    rolls.map { blockRoll: DBlockResult ->
                        BlockDieRoll(originalRoll = blockRoll)
                    }
                compositeCommandOf(
                    SetContext(state.getContext<BlockContext>().copy(roll = roll)),
                    ExitProcedure(),
                )
            }
        }
    }

    // ------------------------------------------------------------------------------------------------------------
    // HELPER FUNCTIONS

    fun getRerollOptions(
        rules: Rules,
        attackingPlayer: Player,
        dicePoolId: Int,
        diceRoll: List<BlockDieRoll>
    ): List<ActionDescriptor> {
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
                .map { rerollOption: DiceRerollOption -> SelectRerollOption(rerollOption, dicePoolId) }

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
                        option = DiceRerollOption(rules.getAvailableTeamReroll(team), diceRoll),
                        dicePoolId = dicePoolId,
                    )
                )
            } else {
                emptyList()
            }
            listOf(SelectNoReroll(null, dicePoolId)) + availableSkills + teamRerolls
        }
    }
}
