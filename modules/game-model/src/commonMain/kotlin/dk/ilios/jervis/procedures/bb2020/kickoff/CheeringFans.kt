package dk.ilios.jervis.procedures.bb2020.kickoff

import compositeCommandOf
import dk.ilios.jervis.actions.ActionDescriptor
import dk.ilios.jervis.actions.D6Result
import dk.ilios.jervis.actions.Dice
import dk.ilios.jervis.actions.GameAction
import dk.ilios.jervis.actions.RollDice
import dk.ilios.jervis.commands.Command
import dk.ilios.jervis.commands.DeleteTemporaryDieRoll
import dk.ilios.jervis.commands.ExitProcedure
import dk.ilios.jervis.commands.GotoNode
import dk.ilios.jervis.commands.SaveTemporaryDieRoll
import dk.ilios.jervis.fsm.ActionNode
import dk.ilios.jervis.fsm.ComputationNode
import dk.ilios.jervis.fsm.Node
import dk.ilios.jervis.fsm.ParentNode
import dk.ilios.jervis.fsm.Procedure
import dk.ilios.jervis.model.Game
import dk.ilios.jervis.model.Team
import dk.ilios.jervis.procedures.PrayersToNuffleRoll
import dk.ilios.jervis.reports.ReportCheeringFansResult
import dk.ilios.jervis.rules.Rules

/**
 * Procedure for handling the Kick-Off Event: "Cheering Fans" as described on page 41
 * of the rulebook.
 */
object CheeringFans : Procedure() {
    override val initialNode: Node = KickingTeamRollDie

    override fun onEnterProcedure(
        state: Game,
        rules: Rules,
    ): Command? = null

    override fun onExitProcedure(
        state: Game,
        rules: Rules,
    ): Command? {
        return compositeCommandOf(
            DeleteTemporaryDieRoll(state.kickingTeam),
            DeleteTemporaryDieRoll(state.receivingTeam),
        )
    }

    object KickingTeamRollDie : ActionNode() {
        override fun getAvailableActions(
            state: Game,
            rules: Rules,
        ): List<ActionDescriptor> {
            return listOf(RollDice(Dice.D6))
        }

        override fun applyAction(
            action: GameAction,
            state: Game,
            rules: Rules,
        ): Command {
            return checkType<D6Result>(action) {
                compositeCommandOf(
                    SaveTemporaryDieRoll(state.kickingTeam, it),
                    GotoNode(ReceivingTeamRollDie),
                )
            }
        }
    }

    object ReceivingTeamRollDie : ActionNode() {
        override fun getAvailableActions(
            state: Game,
            rules: Rules,
        ): List<ActionDescriptor> {
            return listOf(RollDice(Dice.D6))
        }

        override fun applyAction(
            action: GameAction,
            state: Game,
            rules: Rules,
        ): Command {
            return checkType<D6Result>(action) {
                compositeCommandOf(
                    SaveTemporaryDieRoll(state.receivingTeam, it),
                    GotoNode(ResolveCheeringFans),
                )
            }
        }
    }

    object ResolveCheeringFans : ComputationNode() {
        override fun apply(
            state: Game,
            rules: Rules,
        ): Command {
            val kickingTeamDie = state.kickingTeam.temporaryData.dieRoll.last()
            val receivingTeamDie = state.receivingTeam.temporaryData.dieRoll.last()
            val kickingResult = kickingTeamDie.value + state.kickingTeam.cheerLeaders
            val receivingResult = receivingTeamDie.value + state.receivingTeam.cheerLeaders
            return when {
                kickingResult == receivingResult -> {
                    compositeCommandOf(
                        ReportCheeringFansResult(
                            ReportCheeringFansResult.State.DRAW,
                            kickingTeamDie,
                            state.kickingTeam.cheerLeaders,
                            receivingTeamDie,
                            state.receivingTeam.cheerLeaders,
                        ),
                        ExitProcedure(),
                    )
                }
                kickingResult > receivingResult -> {
                    compositeCommandOf(
                        ReportCheeringFansResult(
                            ReportCheeringFansResult.State.KICKER_WINS,
                            kickingTeamDie,
                            state.kickingTeam.cheerLeaders,
                            receivingTeamDie,
                            state.receivingTeam.cheerLeaders,
                        ),
                        GotoNode(WinnerRollsOnPrayersOfNuffle(state.kickingTeam)),
                    )
                }
                else -> {
                    compositeCommandOf(
                        ReportCheeringFansResult(
                            ReportCheeringFansResult.State.RECEIVER_WINS,
                            kickingTeamDie,
                            state.kickingTeam.cheerLeaders,
                            receivingTeamDie,
                            state.receivingTeam.cheerLeaders,
                        ),
                        GotoNode(WinnerRollsOnPrayersOfNuffle(state.receivingTeam)),
                    )
                }
            }
        }
    }

    class WinnerRollsOnPrayersOfNuffle(private val team: Team) : ParentNode() {
        override fun onEnterNode(
            state: Game,
            rules: Rules,
        ): Command? {
//            if (state.activeTeam != team) {
// //                return SetActiveTeam(team)
//            }
            return null
        }

        override fun getChildProcedure(state: Game, rules: Rules): Procedure = PrayersToNuffleRoll
        override fun onExitNode(state: Game, rules: Rules): Command = ExitProcedure()
    }
}
