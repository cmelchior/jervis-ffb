package com.jervisffb.engine.rules.common.procedures.tables.kickoff

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
import com.jervisffb.engine.commands.SetPlayerState
import com.jervisffb.engine.commands.compositeCommandOf
import com.jervisffb.engine.commands.context.RemoveContext
import com.jervisffb.engine.commands.context.SetContext
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
import com.jervisffb.engine.model.context.PitchInvasionContext
import com.jervisffb.engine.model.context.getContext
import com.jervisffb.engine.reports.ReportDiceRoll
import com.jervisffb.engine.reports.ReportGameProgress
import com.jervisffb.engine.reports.ReportPitchInvasionRoll
import com.jervisffb.engine.rules.DiceRollType
import com.jervisffb.engine.rules.Rules
import com.jervisffb.engine.utils.INVALID_ACTION
import com.jervisffb.engine.utils.INVALID_GAME_STATE
import kotlin.math.min

/**
 * Procedure for handling the Kick-Off Event: "Pitch Invasion".
 *
 * See page 41 in the BB2020 rulebook.
 * See page 48 in the BB2025 rulebook.
 *
 * Developer's Commentary:
 * It isn't defined in the rules, which team resolves their roll first, so we have just
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
                    checkType<RandomPlayersSelected>(action) { randomPlayersAction ->
                        val requestedRandomPlayers = selectFromTeam(context.receivingPlayersAffected, state.receivingTeam, rules).first() as? SelectRandomPlayers
                        if (requestedRandomPlayers != null && requestedRandomPlayers.count != randomPlayersAction.players.size) {
                            INVALID_ACTION(action, "Wrong number of random players: ${randomPlayersAction.players.size} vs. ${requestedRandomPlayers.count}")
                        }
                        val playerCommands = randomPlayersAction.getPlayers(state).flatMap { player ->
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
            val context = state.getContext<PitchInvasionContext>()
            return when (action) {
                is Continue -> {
                    compositeCommandOf(
                        ReportGameProgress("${state.kickingTeam} had no eligible players"),
                        ExitProcedure(),
                    )
                }
                else -> {
                    checkType<RandomPlayersSelected>(action) { randomPlayersAction ->
                        val requestedRandomPlayers = selectFromTeam(context.kickingPlayersAffected, state.kickingTeam, rules).first() as? SelectRandomPlayers
                        if (requestedRandomPlayers != null && requestedRandomPlayers.count != randomPlayersAction.players.size) {
                            INVALID_ACTION(action, "Wrong number of random players: ${randomPlayersAction.players.size} vs. ${requestedRandomPlayers.count}")
                        }
                        val playerCommands = randomPlayersAction.getPlayers(state).flatMap { player ->
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
                    // If we have fewer players on the field than we need to select, we reduce the requested size
                    // to be equal to all players on the field.
                    listOf(
                        SelectRandomPlayers(min(affectedPlayers, players.size), players.map { it.id })
                    )
                } else {
                    listOf(ContinueWhenReady)
                }
            }
    }
}
