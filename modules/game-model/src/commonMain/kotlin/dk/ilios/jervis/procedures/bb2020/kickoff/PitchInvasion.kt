package dk.ilios.jervis.procedures.bb2020.kickoff

import compositeCommandOf
import dk.ilios.jervis.actions.ActionDescriptor
import dk.ilios.jervis.actions.Continue
import dk.ilios.jervis.actions.ContinueWhenReady
import dk.ilios.jervis.actions.D3Result
import dk.ilios.jervis.actions.D6Result
import dk.ilios.jervis.actions.Dice
import dk.ilios.jervis.actions.GameAction
import dk.ilios.jervis.actions.RandomPlayersSelected
import dk.ilios.jervis.actions.RollDice
import dk.ilios.jervis.actions.SelectRandomPlayers
import dk.ilios.jervis.commands.Command
import dk.ilios.jervis.commands.RemoveContext
import dk.ilios.jervis.commands.SetContext
import dk.ilios.jervis.commands.SetPlayerState
import dk.ilios.jervis.commands.fsm.ExitProcedure
import dk.ilios.jervis.commands.fsm.GotoNode
import dk.ilios.jervis.fsm.ActionNode
import dk.ilios.jervis.fsm.Node
import dk.ilios.jervis.fsm.Procedure
import dk.ilios.jervis.model.Game
import dk.ilios.jervis.model.PlayerState
import dk.ilios.jervis.model.Team
import dk.ilios.jervis.model.context.ProcedureContext
import dk.ilios.jervis.model.context.getContext
import dk.ilios.jervis.reports.ReportDiceRoll
import dk.ilios.jervis.reports.ReportGameProgress
import dk.ilios.jervis.reports.ReportPitchInvasionRoll
import dk.ilios.jervis.rules.Rules
import dk.ilios.jervis.rules.skills.DiceRollType
import dk.ilios.jervis.utils.INVALID_GAME_STATE

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
        override fun getAvailableActions(state: Game, rules: Rules): List<ActionDescriptor> = listOf(RollDice(Dice.D6))
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
        override fun getAvailableActions(state: Game, rules: Rules): List<ActionDescriptor> = listOf(RollDice(Dice.D6))
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
        override fun getAvailableActions(state: Game, rules: Rules): List<ActionDescriptor> = listOf(RollDice(Dice.D3))
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
        override fun getAvailableActions(state: Game, rules: Rules): List<ActionDescriptor> {
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
                                SetPlayerState(player, PlayerState.STUNNED),
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
        override fun getAvailableActions(state: Game, rules: Rules): List<ActionDescriptor> = listOf(RollDice(Dice.D3))
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
        override fun getAvailableActions(state: Game, rules: Rules): List<ActionDescriptor> {
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
                                SetPlayerState(player, PlayerState.STUNNED),
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

    private fun selectFromTeam(affectedPlayers: Int, team: Team, rules: Rules): List<ActionDescriptor> {
        return team
            .filter { it.location.isOnField(rules) }
            .let { players ->
                if (players.isNotEmpty()) {
                    listOf(SelectRandomPlayers(affectedPlayers, players.map { it.id })
                    )
                } else {
                    listOf(ContinueWhenReady)
                }
            }
    }
}
