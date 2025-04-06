package com.jervisffb.engine.rules.bb2020.procedures.actions.block.standard

import com.jervisffb.engine.actions.DBlockResult
import com.jervisffb.engine.actions.Dice
import com.jervisffb.engine.actions.GameAction
import com.jervisffb.engine.actions.GameActionDescriptor
import com.jervisffb.engine.actions.RollDice
import com.jervisffb.engine.actions.SelectNoReroll
import com.jervisffb.engine.actions.SelectRerollOption
import com.jervisffb.engine.commands.Command
import com.jervisffb.engine.commands.SetContext
import com.jervisffb.engine.commands.compositeCommandOf
import com.jervisffb.engine.commands.fsm.ExitProcedure
import com.jervisffb.engine.commands.fsm.GotoNode
import com.jervisffb.engine.fsm.ActionNode
import com.jervisffb.engine.fsm.Node
import com.jervisffb.engine.fsm.ParentNode
import com.jervisffb.engine.fsm.Procedure
import com.jervisffb.engine.fsm.checkDiceRollList
import com.jervisffb.engine.model.Game
import com.jervisffb.engine.model.Player
import com.jervisffb.engine.model.Team
import com.jervisffb.engine.model.context.assertContext
import com.jervisffb.engine.model.context.getContext
import com.jervisffb.engine.reports.ReportDiceRoll
import com.jervisffb.engine.rules.DiceRollType
import com.jervisffb.engine.rules.Rules
import com.jervisffb.engine.rules.bb2020.procedures.BlockDieRoll
import com.jervisffb.engine.rules.bb2020.procedures.actions.block.BlockContext
import com.jervisffb.engine.rules.bb2020.skills.DiceRerollOption
import com.jervisffb.engine.rules.bb2020.skills.RerollSource
import com.jervisffb.engine.rules.bb2020.skills.Skill
import com.jervisffb.engine.utils.INVALID_GAME_STATE
import kotlin.math.absoluteValue

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
        override fun getChildProcedure(state: Game, rules: Rules,): Procedure = state.rerollContext!!.source.rerollProcedure
        override fun onExitNode(state: Game, rules: Rules): Command {
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
        override fun getAvailableActions(state: Game, rules: Rules): List<GameActionDescriptor> {
            // TODO Some skills allow only rerolling some dice. We need to capture this somehow
            val noOfDice = state.getContext<BlockContext>().calculateNoOfBlockDice().absoluteValue
            return listOf(RollDice(List(noOfDice) { Dice.BLOCK }))
        }

        override fun applyAction(action: GameAction, state: Game, rules: Rules): Command {
            return checkDiceRollList<DBlockResult>(action) { rerolls: List<DBlockResult> ->
                val rerollContext = state.rerollContext!!
                val blockContext = state.getContext<BlockContext>()
                val updatedRoll = blockContext.roll.mapIndexed { i, blockRoll: BlockDieRoll ->
                    blockRoll.also {
                        it.rerollSource = rerollContext.source
                        it.rerolledResult = rerolls[i] // TODO This requires that the rerolls are in the same order. Is that acceptable?
                    }
                }
                compositeCommandOf(
                    ReportDiceRoll(DiceRollType.BLOCK, rerolls),
                    SetContext(blockContext.copy(roll = updatedRoll)),
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
    ): List<GameActionDescriptor> {
        // Re-rolling block dice can be pretty complex,
        // Brawler: Can reroll a single "Both Down"
        // Pro: Can reroll any single die
        // Team reroll: Can reroll all of them
        val availableSkills: List<DiceRerollOption> =
            attackingPlayer.skills
                .filter { skill: Skill -> skill is RerollSource }
                .map { it as RerollSource }
                .filter { it.canReroll(DiceRollType.BLOCK, diceRoll) }
                .flatMap { it.calculateRerollOptions(DiceRollType.BLOCK, diceRoll) }

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
                    DiceRerollOption(
                        rules.getAvailableTeamReroll(team).id,
                        diceRoll
                    ),
                )
            } else {
                emptyList()
            }
            listOf(SelectNoReroll(null, dicePoolId), SelectRerollOption(availableSkills + teamRerolls))
        }
    }
}
