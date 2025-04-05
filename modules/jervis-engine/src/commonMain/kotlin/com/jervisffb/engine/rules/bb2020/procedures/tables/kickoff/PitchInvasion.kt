package com.jervisffb.engine.rules.bb2020.procedures.tables.kickoff

import com.jervisffb.engine.actions.Continue
import com.jervisffb.engine.actions.ContinueWhenReady
import com.jervisffb.engine.actions.D3Result
import com.jervisffb.engine.actions.D6Result
import com.jervisffb.engine.actions.Dice
import com.jervisffb.engine.actions.GameAction
import com.jervisffb.engine.actions.GameActionDescriptor
import com.jervisffb.engine.actions.RandomPlayersSelected
import com.jervisffb.engine.actions.RollDice
import com.jervisffb.engine.actions.SelectRandomPlayers
import com.jervisffb.engine.commands.Command
import com.jervisffb.engine.commands.RemoveContext
import com.jervisffb.engine.commands.SetContext
import com.jervisffb.engine.commands.SetPlayerState
import com.jervisffb.engine.commands.compositeCommandOf
import com.jervisffb.engine.commands.fsm.ExitProcedure
import com.jervisffb.engine.commands.fsm.GotoNode
import com.jervisffb.engine.fsm.ActionNode
import com.jervisffb.engine.fsm.Node
import com.jervisffb.engine.fsm.Procedure
import com.jervisffb.engine.fsm.checkDiceRoll
import com.jervisffb.engine.fsm.checkType
import com.jervisffb.engine.model.Game
import com.jervisffb.engine.model.PlayerState
import com.jervisffb.engine.model.Team
import com.jervisffb.engine.model.context.ProcedureContext
import com.jervisffb.engine.model.context.getContext
import com.jervisffb.engine.reports.ReportDiceRoll
import com.jervisffb.engine.reports.ReportGameProgress
import com.jervisffb.engine.reports.ReportPitchInvasionRoll
import com.jervisffb.engine.rules.Rules
import com.jervisffb.engine.rules.DiceRollType
import com.jervisffb.engine.utils.INVALID_GAME_STATE

data class PitchInvasionContext(
    val kickingRoll: D6Result,
    val kickingResult: Int = 0,
    val kickingPlayersAffected: Int = 0,
    val receivingRoll: D6Result? = null,
    val receivingResult: Int = 0,
    val receivingPlayersAffected: Int = 0

): ProcedureContext

/**
 * Procedure for handling the Kick-Off Event: "Pitch Invasion" as described on page 41
 * of the rulebook.
 *
 * Developer's Commentary:
 * It isn't defined in the rules, which team resolve their roll first, so we have just
 * decided on the receiving team (it shouldn't matter either, since there is currently no
 * way to affect the rolls)
 */
object PitchInvasion : Procedure() {
    override val initialNode: Node = RollForKickingTeamFans
    override fun onEnterProcedure(state: Game, rules: Rules): Command? = null
    override fun onExitProcedure(state: Game, rules: Rules): Command = RemoveContext<PitchInvasionContext>()

    object RollForKickingTeamFans : ActionNode() {
        override fun actionOwner(state: Game, rules: Rules): Team = state.kickingTeam
        override fun getAvailableActions(state: Game, rules: Rules): List<GameActionDescriptor> = listOf(RollDice(Dice.D6))
        override fun applyAction(action: GameAction, state: Game, rules: Rules): Command {
            return checkDiceRoll<D6Result>(action) { d6 ->
                val fanFactor = state.kickingTeam.fanFactor
                compositeCommandOf(
                    ReportDiceRoll(DiceRollType.PITCH_INVASION_FAN_FACTOR, d6),
                    SetContext(PitchInvasionContext(kickingRoll = d6, kickingResult = d6.value + fanFactor)),
                    ReportPitchInvasionRoll(state.kickingTeam, d6, fanFactor),
                    GotoNode(RollForReceivingTeamFans),
                )
            }
        }
    }

    object RollForReceivingTeamFans : ActionNode() {
        override fun actionOwner(state: Game, rules: Rules): Team = state.receivingTeam
        override fun getAvailableActions(state: Game, rules: Rules): List<GameActionDescriptor> = listOf(RollDice(Dice.D6))
        override fun applyAction(action: GameAction, state: Game, rules: Rules): Command {
            return checkDiceRoll<D6Result>(action) { d6 ->
                val context = state.getContext<PitchInvasionContext>()
                val fanFactor = state.receivingTeam.fanFactor
                val result = d6.value + fanFactor

                val nextNode = when {
                    context.kickingResult >= result -> GotoNode(RollForReceivingTeamStuns)
                    context.kickingResult < result -> GotoNode(RollForKickingTeamStuns)
                    else -> INVALID_GAME_STATE("Unsupported state: $result, $context")
                }
                compositeCommandOf(
                    ReportDiceRoll(DiceRollType.PITCH_INVASION_FAN_FACTOR, d6),
                    SetContext(context.copy(receivingRoll = d6, receivingResult = result)),
                    ReportPitchInvasionRoll(state.receivingTeam, d6, fanFactor),
                    nextNode,
                )
            }
        }
    }

    object RollForReceivingTeamStuns : ActionNode() {
        override fun actionOwner(state: Game, rules: Rules): Team? = null
        override fun getAvailableActions(state: Game, rules: Rules): List<GameActionDescriptor> = listOf(RollDice(Dice.D3))
        override fun applyAction(action: GameAction, state: Game, rules: Rules): Command {
            return checkType<D3Result>(action) { d3 ->
                val context = state.getContext<PitchInvasionContext>()
                compositeCommandOf(
                    ReportDiceRoll(DiceRollType.PITCH_INVASION_PLAYERS_AFFECTED, d3),
                    SetContext(context.copy(receivingPlayersAffected = d3.value)),
                    GotoNode(SelectReceivingTeamAffectedPlayers),
                )
            }
        }
    }

    object SelectReceivingTeamAffectedPlayers: ActionNode() {
        override fun actionOwner(state: Game, rules: Rules): Team? = null
        override fun getAvailableActions(state: Game, rules: Rules): List<GameActionDescriptor> {
            val context = state.getContext<PitchInvasionContext>()
            return selectFromTeam(context.receivingPlayersAffected, state.receivingTeam, rules)
        }
        override fun applyAction(action: GameAction, state: Game, rules: Rules): Command {
            val context = state.getContext<PitchInvasionContext>()
            val nextNode = if (context.kickingResult == context.receivingResult) GotoNode(RollForKickingTeamStuns) else ExitProcedure()
            return when (action) {
                is Continue -> {
                    compositeCommandOf(
                        ReportGameProgress("${state.receivingTeam} had no eligible players"),
                        nextNode
                    )
                }
                else -> {
                    checkType<RandomPlayersSelected>(action) {
                        val playerCommands = it.getPlayers(state).flatMap { player ->
                            listOf(
                                SetPlayerState(player, PlayerState.STUNNED, hasTackleZones = false),
                                ReportGameProgress("${player.name} was Stunned by the crowd")
                            )
                        }.toTypedArray()
                        compositeCommandOf(
                            *playerCommands,
                            nextNode
                        )
                    }
                }
            }
        }
    }

    object RollForKickingTeamStuns : ActionNode() {
        override fun actionOwner(state: Game, rules: Rules): Team? = null
        override fun getAvailableActions(state: Game, rules: Rules): List<GameActionDescriptor> = listOf(RollDice(Dice.D3))
        override fun applyAction(action: GameAction, state: Game, rules: Rules): Command {
            return checkType<D3Result>(action) { d3 ->
                val context = state.getContext<PitchInvasionContext>()
                compositeCommandOf(
                    ReportDiceRoll(DiceRollType.PITCH_INVASION_PLAYERS_AFFECTED, d3),
                    SetContext(context.copy(kickingPlayersAffected = d3.value)),
                    GotoNode(SelectKickingTeamAffectedPlayers),
                )
            }
        }
    }

    object SelectKickingTeamAffectedPlayers: ActionNode() {
        override fun actionOwner(state: Game, rules: Rules): Team? = null
        override fun getAvailableActions(state: Game, rules: Rules): List<GameActionDescriptor> {
            val context = state.getContext<PitchInvasionContext>()
            return selectFromTeam(context.kickingPlayersAffected, state.kickingTeam, rules)
        }
        override fun applyAction(action: GameAction, state: Game, rules: Rules): Command {
            return when (action) {
                is Continue -> {
                    compositeCommandOf(
                        ReportGameProgress("${state.kickingTeam} had no eligible players"),
                        ExitProcedure(),
                    )
                }
                else -> {
                    checkType<RandomPlayersSelected>(action) {
                        val playerCommands = it.getPlayers(state).flatMap { player ->
                            listOf(
                                SetPlayerState(player, PlayerState.STUNNED, hasTackleZones = false),
                                ReportGameProgress("${player.name} was Stunned by the crowd")
                            )
                        }.toTypedArray()
                        compositeCommandOf(
                            *playerCommands,
                            ExitProcedure()
                        )
                    }
                }
            }
        }
    }

    private fun selectFromTeam(affectedPlayers: Int, team: Team, rules: Rules): List<GameActionDescriptor> {
        return team
            .filter { it.location.isOnField(rules) }
            .let { players ->
                if (players.isNotEmpty()) {
                    listOf(
                        SelectRandomPlayers(affectedPlayers, players.map { it.id })
                    )
                } else {
                    listOf(ContinueWhenReady)
                }
            }
    }
}
