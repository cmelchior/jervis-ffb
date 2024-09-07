package dk.ilios.jervis.procedures.bb2020.kickoff

import dk.ilios.jervis.commands.Command
import dk.ilios.jervis.fsm.ComputationNode
import dk.ilios.jervis.fsm.Node
import dk.ilios.jervis.fsm.Procedure
import dk.ilios.jervis.model.Game
import dk.ilios.jervis.rules.Rules

/**
 * Procedure for handling the Kick-Off Event: "Brilliant Coaching" as described on page 41
 * of the rulebook.
 */
object BrilliantCoaching : Procedure() {
    override val initialNode: Node = KickingTeamRollDie

    override fun onEnterProcedure(
        state: Game,
        rules: Rules,
    ): Command? = null

    override fun onExitProcedure(
        state: Game,
        rules: Rules,
    ): Command? = null

    object KickingTeamRollDie: ComputationNode() {
        override fun apply(state: Game, rules: Rules): Command {
            TODO("Not yet implemented")
        }

    }
//
//    object KickingTeamRollDie : ActionNode() {
//        override fun getAvailableActions(state: Game, rules: Rules): List<ActionDescriptor> {
//            return listOf(RollDice(Dice.D6))
//        }
//
//        override fun applyAction(action: GameAction, state: Game, rules: Rules): Command {
//            return checkType<D6Result>(action) {
//                compositeCommandOf(
//                    SaveTemporaryDieRoll(state.kickingTeam, it),
//                    GotoNode(ReceivingTeamRollDie),
//                )
//            }
//        }
//    }
//
//    object ReceivingTeamRollDie : ActionNode() {
//        override fun getAvailableActions(state: Game, rules: Rules): List<ActionDescriptor> {
//            return listOf(RollDice(Dice.D6))
//        }
//        override fun applyAction(action: GameAction, state: Game, rules: Rules): Command {
//            return checkType<D6Result>(action) {
//                compositeCommandOf(
//                    SaveTemporaryDieRoll(state.receivingTeam, it),
//                    GotoNode(ResolveCheeringFans),
//                )
//            }
//        }
//    }
//
//    object ResolveCheeringFans : ComputationNode() {
//        override fun apply(state: Game, rules: Rules,
//        ): Command {
//            val kickingTeamDie = state.kickingTeam.temporaryData.dieRoll.last()
//            val receivingTeamDie = state.receivingTeam.temporaryData.dieRoll.last()
//            val kickingResult = kickingTeamDie.value + state.kickingTeam.cheerLeaders
//            val receivingResult = receivingTeamDie.value + state.receivingTeam.cheerLeaders
//            return when {
//                kickingResult == receivingResult -> {
//                    compositeCommandOf(
//                        ReportCheeringFansResult(
//                            ReportCheeringFansResult.State.DRAW,
//                            kickingTeamDie,
//                            state.kickingTeam.cheerLeaders,
//                            receivingTeamDie,
//                            state.receivingTeam.cheerLeaders,
//                        ),
//                        ExitProcedure(),
//                    )
//                }
//                kickingResult > receivingResult -> {
//                    compositeCommandOf(
//                        ReportCheeringFansResult(
//                            ReportCheeringFansResult.State.KICKER_WINS,
//                            kickingTeamDie,
//                            state.kickingTeam.cheerLeaders,
//                            receivingTeamDie,
//                            state.receivingTeam.cheerLeaders,
//                        ),
//                        GotoNode(WinnerRollsOnPrayersToNuffle(state.kickingTeam)),
//                    )
//                }
//                else -> {
//                    compositeCommandOf(
//                        ReportCheeringFansResult(
//                            ReportCheeringFansResult.State.RECEIVER_WINS,
//                            kickingTeamDie,
//                            state.kickingTeam.cheerLeaders,
//                            receivingTeamDie,
//                            state.receivingTeam.cheerLeaders,
//                        ),
//                        GotoNode(WinnerRollsOnPrayersToNuffle(state.receivingTeam)),
//                    )
//                }
//            }
//        }
//    }
}
