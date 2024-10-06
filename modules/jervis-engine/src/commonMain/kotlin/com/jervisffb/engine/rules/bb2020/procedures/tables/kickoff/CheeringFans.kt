package com.jervisffb.engine.rules.bb2020.procedures.tables.kickoff

import compositeCommandOf
import com.jervisffb.engine.actions.ActionDescriptor
import com.jervisffb.engine.actions.D6Result
import com.jervisffb.engine.actions.Dice
import com.jervisffb.engine.actions.GameAction
import com.jervisffb.engine.actions.RollDice
import com.jervisffb.engine.commands.Command
import com.jervisffb.engine.commands.RemoveContext
import com.jervisffb.engine.commands.SetContext
import com.jervisffb.engine.commands.fsm.ExitProcedure
import com.jervisffb.engine.commands.fsm.GotoNode
import com.jervisffb.engine.fsm.ActionNode
import com.jervisffb.engine.fsm.ComputationNode
import com.jervisffb.engine.fsm.Node
import com.jervisffb.engine.fsm.ParentNode
import com.jervisffb.engine.fsm.Procedure
import com.jervisffb.engine.model.Game
import com.jervisffb.engine.model.Team
import com.jervisffb.engine.model.context.ProcedureContext
import com.jervisffb.engine.model.context.assertContext
import com.jervisffb.engine.model.context.getContext
import com.jervisffb.engine.rules.bb2020.procedures.PrayersToNuffleRoll
import com.jervisffb.engine.rules.bb2020.procedures.PrayersToNuffleRollContext
import com.jervisffb.engine.reports.ReportCheeringFansResult
import com.jervisffb.engine.reports.ReportDiceRoll
import com.jervisffb.engine.rules.Rules
import com.jervisffb.engine.rules.bb2020.skills.DiceRollType

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

        override fun applyAction(action: GameAction, state: Game, rules: Rules): Command {
            return checkDiceRoll<D6Result>(action) { d6 ->
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
            return checkDiceRoll<D6Result>(action) { d6 ->
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
