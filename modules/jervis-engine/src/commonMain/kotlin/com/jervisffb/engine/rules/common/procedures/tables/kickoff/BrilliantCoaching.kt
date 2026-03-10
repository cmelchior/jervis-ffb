package com.jervisffb.engine.rules.common.procedures.tables.kickoff

import com.jervisffb.engine.actions.D6Result
import com.jervisffb.engine.actions.Dice
import com.jervisffb.engine.actions.GameAction
import com.jervisffb.engine.actions.GameActionDescriptor
import com.jervisffb.engine.actions.RollDice
import com.jervisffb.engine.commands.AddTeamReroll
import com.jervisffb.engine.commands.Command
import com.jervisffb.engine.commands.compositeCommandOf
import com.jervisffb.engine.commands.context.AddContext
import com.jervisffb.engine.commands.context.RemoveContext
import com.jervisffb.engine.commands.context.UpdateContext
import com.jervisffb.engine.commands.fsm.ExitProcedure
import com.jervisffb.engine.commands.fsm.GotoNode
import com.jervisffb.engine.fsm.ActionNode
import com.jervisffb.engine.fsm.ComputationNode
import com.jervisffb.engine.fsm.Node
import com.jervisffb.engine.fsm.Procedure
import com.jervisffb.engine.fsm.castDiceRoll
import com.jervisffb.engine.model.Game
import com.jervisffb.engine.model.Team
import com.jervisffb.engine.model.context.BrilliantCoachingContext
import com.jervisffb.engine.model.context.getContext
import com.jervisffb.engine.reports.ReportBrilliantCoachingResult
import com.jervisffb.engine.reports.ReportDiceRoll
import com.jervisffb.engine.rules.DiceRollType
import com.jervisffb.engine.rules.Rules
import com.jervisffb.engine.rules.common.skills.BrilliantCoachingReroll
import com.jervisffb.engine.utils.INVALID_GAME_STATE
import com.jervisffb.engine.utils.sum

/**
 * Procedure for handling the Kick-Off Event: "Brilliant Coaching".
 *
 * See page 41 in the BB2020 rulebook.
 * See page 48 in the BB2025 rulebook.
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
            return castDiceRoll<D6Result>(action) { d6 ->
                compositeCommandOf(
                    ReportDiceRoll(DiceRollType.BRILLIANT_COACHING, d6),
                    AddContext(BrilliantCoachingContext(d6, state.kickingTeam.brilliantCoachingModifiers)),
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
            return castDiceRoll<D6Result>(action) { d6 ->
                val context = state.getContext<BrilliantCoachingContext>()
                compositeCommandOf(
                    ReportDiceRoll(DiceRollType.BRILLIANT_COACHING, d6),
                    UpdateContext(context.copy(
                        receivingTeamRoll = d6,
                        receivingTeamModifiers = state.receivingTeam.brilliantCoachingModifiers)
                    ),
                    GotoNode(ResolveBrilliantCoaching),
                )
            }
        }
    }

    object ResolveBrilliantCoaching : ComputationNode() {
        override fun apply(state: Game, rules: Rules): Command {
            val context = state.getContext<BrilliantCoachingContext>()
            val kickingResult = context.kickingTeamRoll.value + state.kickingTeam.assistantCoaches + context.kickingTeamModifiers.sum()
            val receivingResult = context.receivingTeamRoll!!.value + state.receivingTeam.assistantCoaches + context.receivingTeamModifiers.sum()
            return compositeCommandOf(
                when {
                    kickingResult > receivingResult -> AddTeamReroll(state.kickingTeam,
                        BrilliantCoachingReroll(state.kickingTeam.id)
                    )
                    kickingResult < receivingResult -> AddTeamReroll(state.receivingTeam,
                        BrilliantCoachingReroll(state.receivingTeam.id)
                    )
                    kickingResult == receivingResult -> null
                    else -> INVALID_GAME_STATE("Unknown case when resolving brilliant coaching: $kickingResult, $receivingResult")
                },
                ReportBrilliantCoachingResult(
                    state.kickingTeam,
                    state.receivingTeam,
                    context.kickingTeamRoll,
                    state.kickingTeam.assistantCoaches,
                    state.kickingTeam.brilliantCoachingModifiers,
                    context.receivingTeamRoll,
                    state.receivingTeam.assistantCoaches,
                    state.receivingTeam.brilliantCoachingModifiers
                ),
                ExitProcedure(),
            )
        }
    }
}
