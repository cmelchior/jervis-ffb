package dk.ilios.jervis.procedures.tables.kickoff

import compositeCommandOf
import dk.ilios.jervis.actions.ActionDescriptor
import dk.ilios.jervis.actions.D6Result
import dk.ilios.jervis.actions.Dice
import dk.ilios.jervis.actions.GameAction
import dk.ilios.jervis.actions.RollDice
import dk.ilios.jervis.commands.Command
import dk.ilios.jervis.commands.RemoveContext
import dk.ilios.jervis.commands.SetContext
import dk.ilios.jervis.commands.fsm.ExitProcedure
import dk.ilios.jervis.commands.fsm.GotoNode
import dk.ilios.jervis.fsm.ActionNode
import dk.ilios.jervis.fsm.ComputationNode
import dk.ilios.jervis.fsm.Node
import dk.ilios.jervis.fsm.ParentNode
import dk.ilios.jervis.fsm.Procedure
import dk.ilios.jervis.model.Game
import dk.ilios.jervis.model.Team
import dk.ilios.jervis.model.context.ProcedureContext
import dk.ilios.jervis.model.context.assertContext
import dk.ilios.jervis.model.context.getContext
import dk.ilios.jervis.procedures.PrayersToNuffleRoll
import dk.ilios.jervis.procedures.PrayersToNuffleRollContext
import dk.ilios.jervis.reports.ReportCheeringFansResult
import dk.ilios.jervis.reports.ReportDiceRoll
import dk.ilios.jervis.rules.Rules
import dk.ilios.jervis.rules.skills.DiceRollType

data class CheeringFansContext(
    val kickingTeamRoll: D6Result,
    val receivingTeamRoll: D6Result? = null,
    val winner: Team? = null,
): ProcedureContext

/**
 * Procedure for handling the Kick-Off Event: "Cheering Fans" as described on page 41
 * of the rulebook.
 */
object CheeringFans : Procedure() {
    override val initialNode: Node = KickingTeamRollDie
    override fun onEnterProcedure(state: Game, rules: Rules): Command? = null
    override fun onExitProcedure(state: Game, rules: Rules): Command = RemoveContext<CheeringFansContext>()
    override fun isValid(state: Game, rules: Rules) = state.assertContext<CheeringFansContext>()

    object KickingTeamRollDie : ActionNode() {
        override fun actionOwner(state: Game, rules: Rules): Team = state.kickingTeam
        override fun getAvailableActions(state: Game, rules: Rules): List<ActionDescriptor> {
            return listOf(RollDice(Dice.D6))
        }

        override fun applyAction(action: GameAction,state: Game, rules: Rules): Command {
            return checkType<D6Result>(action) { d6 ->
                compositeCommandOf(
                    ReportDiceRoll(DiceRollType.CHEERING_FANS, d6),
                    SetContext(CheeringFansContext(d6)),
                    GotoNode(ReceivingTeamRollDie),
                )
            }
        }
    }

    object ReceivingTeamRollDie : ActionNode() {
        override fun actionOwner(state: Game, rules: Rules): Team = state.receivingTeam
        override fun getAvailableActions(state: Game, rules: Rules): List<ActionDescriptor> {
            return listOf(RollDice(Dice.D6))
        }
        override fun applyAction(action: GameAction, state: Game, rules: Rules): Command {
            return checkType<D6Result>(action) { d6 ->
                compositeCommandOf(
                    ReportDiceRoll(DiceRollType.CHEERING_FANS, d6),
                    SetContext(state.getContext<CheeringFansContext>().copy(receivingTeamRoll = d6)),
                    GotoNode(ResolveCheeringFans),
                )
            }
        }
    }

    object ResolveCheeringFans : ComputationNode() {
        override fun apply(state: Game, rules: Rules): Command {
            val context = state.getContext<CheeringFansContext>()
            val kickingTeamResult = context.kickingTeamRoll.value + state.kickingTeam.cheerLeaders
            val receivingTeamResult = context.receivingTeamRoll!!.value + state.receivingTeam.cheerLeaders
            return when {
                kickingTeamResult == receivingTeamResult -> {
                    compositeCommandOf(
                        ReportCheeringFansResult(
                            state.kickingTeam,
                            state.receivingTeam,
                            context.kickingTeamRoll,
                            state.kickingTeam.cheerLeaders,
                            context.receivingTeamRoll,
                            state.receivingTeam.cheerLeaders,
                        ),
                        ExitProcedure(),
                    )
                }
                kickingTeamResult > receivingTeamResult -> {
                    compositeCommandOf(
                        ReportCheeringFansResult(
                            state.kickingTeam,
                            state.receivingTeam,
                            context.kickingTeamRoll,
                            state.kickingTeam.cheerLeaders,
                            context.receivingTeamRoll,
                            state.receivingTeam.cheerLeaders,
                        ),
                        SetContext(state.getContext<CheeringFansContext>().copy(winner = state.kickingTeam)),
                        GotoNode(WinnerRollsOnPrayersToNuffle),
                    )
                }
                else -> {
                    compositeCommandOf(
                        ReportCheeringFansResult(
                            state.kickingTeam,
                            state.receivingTeam,
                            context.kickingTeamRoll,
                            state.kickingTeam.cheerLeaders,
                            context.receivingTeamRoll,
                            state.receivingTeam.cheerLeaders,
                        ),
                        SetContext(state.getContext<CheeringFansContext>().copy(winner = state.receivingTeam)),
                        GotoNode(WinnerRollsOnPrayersToNuffle),
                    )
                }
            }
        }
    }

    object WinnerRollsOnPrayersToNuffle : ParentNode() {
        override fun onEnterNode(state: Game, rules: Rules): Command {
            val context = state.getContext<CheeringFansContext>()
            return SetContext(PrayersToNuffleRollContext(context.winner!!, 1))
        }
        override fun getChildProcedure(state: Game, rules: Rules): Procedure = PrayersToNuffleRoll
        override fun onExitNode(state: Game, rules: Rules): Command = ExitProcedure()
    }
}
