package com.jervisffb.engine.rules.bb2020.procedures.tables.kickoff

import com.jervisffb.engine.actions.GameActionDescriptor
import com.jervisffb.engine.actions.D6Result
import com.jervisffb.engine.actions.Dice
import com.jervisffb.engine.actions.GameAction
import com.jervisffb.engine.actions.RollDice
import com.jervisffb.engine.commands.AddTeamReroll
import com.jervisffb.engine.commands.Command
import com.jervisffb.engine.commands.RemoveContext
import com.jervisffb.engine.commands.SetContext
import com.jervisffb.engine.commands.fsm.ExitProcedure
import com.jervisffb.engine.commands.fsm.GotoNode
import com.jervisffb.engine.fsm.ActionNode
import com.jervisffb.engine.fsm.ComputationNode
import com.jervisffb.engine.fsm.Node
import com.jervisffb.engine.fsm.Procedure
import com.jervisffb.engine.fsm.checkType
import com.jervisffb.engine.model.Game
import com.jervisffb.engine.model.Team
import com.jervisffb.engine.model.context.ProcedureContext
import com.jervisffb.engine.model.context.getContext
import com.jervisffb.engine.reports.ReportBrilliantCoachingResult
import com.jervisffb.engine.reports.ReportDiceRoll
import com.jervisffb.engine.rules.Rules
import com.jervisffb.engine.rules.bb2020.skills.BrilliantCoachingReroll
import com.jervisffb.engine.rules.bb2020.skills.DiceRollType
import com.jervisffb.engine.utils.INVALID_GAME_STATE
import com.jervisffb.engine.commands.compositeCommandOf

data class BrilliantCoachingContext(
    val kickingTeamRoll: D6Result,
    val receivingTeamRoll: D6Result? = null,
): ProcedureContext

/**
 * Procedure for handling the Kick-Off Event: "Brilliant Coaching" as described on page 41
 * of the rulebook.
 */
object BrilliantCoaching : Procedure() {
    override val initialNode: Node = KickingTeamRollDie
    override fun onEnterProcedure(state: Game, rules: Rules): Command? = null
    override fun onExitProcedure(state: Game, rules: Rules): Command = RemoveContext<BrilliantCoachingContext>()

    object KickingTeamRollDie : ActionNode() {
        override fun actionOwner(state: Game, rules: Rules): Team = state.kickingTeam
        override fun getAvailableActions(state: Game, rules: Rules): List<GameActionDescriptor> {
            return listOf(RollDice(Dice.D6))
        }
        override fun applyAction(action: GameAction, state: Game, rules: Rules): Command {
            return checkType<D6Result>(action) { d6 ->
                compositeCommandOf(
                    ReportDiceRoll(DiceRollType.BRILLIANT_COACHING, d6),
                    SetContext(BrilliantCoachingContext(d6)),
                    GotoNode(ReceivingTeamRollDie),
                )
            }
        }
    }

    object ReceivingTeamRollDie : ActionNode() {
        override fun actionOwner(state: Game, rules: Rules): Team = state.receivingTeam
        override fun getAvailableActions(state: Game, rules: Rules): List<GameActionDescriptor> {
            return listOf(RollDice(Dice.D6))
        }
        override fun applyAction(action: GameAction, state: Game, rules: Rules): Command {
            return checkType<D6Result>(action) { d6 ->
                compositeCommandOf(
                    ReportDiceRoll(DiceRollType.BRILLIANT_COACHING, d6),
                    SetContext(state.getContext<BrilliantCoachingContext>().copy(receivingTeamRoll = d6)),
                    GotoNode(ResolveBrilliantCoaching),
                )
            }
        }
    }

    object ResolveBrilliantCoaching : ComputationNode() {
        override fun apply(state: Game, rules: Rules): Command {
            val context = state.getContext<BrilliantCoachingContext>()
            val kickingResult = context.kickingTeamRoll.value + state.kickingTeam.assistantCoaches
            val receivingResult = context.receivingTeamRoll!!.value + state.receivingTeam.assistantCoaches
            return compositeCommandOf(
                when {
                    kickingResult > receivingResult -> AddTeamReroll(state.kickingTeam,
                        BrilliantCoachingReroll(state.kickingTeam)
                    )
                    kickingResult < receivingResult -> AddTeamReroll(state.receivingTeam,
                        BrilliantCoachingReroll(state.receivingTeam)
                    )
                    kickingResult == receivingResult -> null
                    else -> INVALID_GAME_STATE("Unknown case when resolving brilliant coaching: $kickingResult, $receivingResult")
                },
                ReportBrilliantCoachingResult(
                    state.kickingTeam,
                    state.receivingTeam,
                    context.kickingTeamRoll,
                    state.kickingTeam.assistantCoaches,
                    context.receivingTeamRoll,
                    state.receivingTeam.assistantCoaches,
                ),
                ExitProcedure(),
            )
        }
    }
}
