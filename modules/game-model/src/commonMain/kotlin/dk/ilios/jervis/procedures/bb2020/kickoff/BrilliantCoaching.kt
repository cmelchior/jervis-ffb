package dk.ilios.jervis.procedures.bb2020.kickoff

import compositeCommandOf
import dk.ilios.jervis.actions.ActionDescriptor
import dk.ilios.jervis.actions.D6Result
import dk.ilios.jervis.actions.Dice
import dk.ilios.jervis.actions.GameAction
import dk.ilios.jervis.actions.RollDice
import dk.ilios.jervis.commands.AddTeamReroll
import dk.ilios.jervis.commands.Command
import dk.ilios.jervis.commands.ExitProcedure
import dk.ilios.jervis.commands.GotoNode
import dk.ilios.jervis.commands.RemoveContext
import dk.ilios.jervis.commands.SetContext
import dk.ilios.jervis.fsm.ActionNode
import dk.ilios.jervis.fsm.ComputationNode
import dk.ilios.jervis.fsm.Node
import dk.ilios.jervis.fsm.Procedure
import dk.ilios.jervis.model.Game
import dk.ilios.jervis.model.context.ProcedureContext
import dk.ilios.jervis.model.context.getContext
import dk.ilios.jervis.reports.ReportBrilliantCoachingResult
import dk.ilios.jervis.reports.ReportDiceRoll
import dk.ilios.jervis.rules.Rules
import dk.ilios.jervis.rules.skills.BrilliantCoachingReroll
import dk.ilios.jervis.rules.skills.DiceRollType
import dk.ilios.jervis.utils.INVALID_GAME_STATE

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
        override fun getAvailableActions(state: Game, rules: Rules): List<ActionDescriptor> {
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
        override fun getAvailableActions(state: Game, rules: Rules): List<ActionDescriptor> {
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
                    kickingResult > receivingResult -> AddTeamReroll(state.kickingTeam, BrilliantCoachingReroll(state.kickingTeam))
                    kickingResult < receivingResult -> AddTeamReroll(state.receivingTeam, BrilliantCoachingReroll(state.receivingTeam))
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
