package dk.ilios.jervis.procedures.bb2020.kickoff

import compositeCommandOf
import dk.ilios.jervis.actions.ActionDescriptor
import dk.ilios.jervis.actions.D3Result
import dk.ilios.jervis.actions.D6Result
import dk.ilios.jervis.actions.Dice
import dk.ilios.jervis.actions.GameAction
import dk.ilios.jervis.actions.RandomPlayersSelected
import dk.ilios.jervis.actions.RollDice
import dk.ilios.jervis.actions.SelectRandomPlayers
import dk.ilios.jervis.commands.Command
import dk.ilios.jervis.commands.ExitProcedure
import dk.ilios.jervis.commands.GotoNode
import dk.ilios.jervis.commands.SetActiveTeam
import dk.ilios.jervis.commands.SetPlayerState
import dk.ilios.jervis.commands.SetTemporaryState
import dk.ilios.jervis.fsm.ActionNode
import dk.ilios.jervis.fsm.Node
import dk.ilios.jervis.fsm.Procedure
import dk.ilios.jervis.model.Game
import dk.ilios.jervis.model.PlayerState
import dk.ilios.jervis.reports.ReportPitchInvasionRoll
import dk.ilios.jervis.reports.ReportPlayerInjury
import dk.ilios.jervis.rules.Rules
import dk.ilios.jervis.utils.INVALID_GAME_STATE

/**
 * Procedure for handling the Kick-Off Event: "Pitch Invasion" as described on page 41
 * of the rulebook.
 */
object PitchInvasion: Procedure() {
    override val initialNode: Node = RollForHomeTeam
    override fun onEnterProcedure(state: Game, rules: Rules): Command? = null
    override fun onExitProcedure(state: Game, rules: Rules): Command? {
        return SetActiveTeam(state.kickingTeam)
    }

    object RollForHomeTeam : ActionNode() {
        override fun getAvailableActions(state: Game, rules: Rules): List<ActionDescriptor> = listOf(RollDice(Dice.D6))
        override fun applyAction(action: GameAction, state: Game, rules: Rules): Command {
            return checkDiceRoll<D6Result>(action) {
                compositeCommandOf(
                    SetTemporaryState(Game::pitchInvasionHomeRoll, it),
                    ReportPitchInvasionRoll(state.homeTeam, it, state.homeTeam.fanFactor),
                    GotoNode(RollForAwayTeam)
                )
            }
        }
    }

    object RollForAwayTeam : ActionNode() {
        override fun getAvailableActions(state: Game, rules: Rules): List<ActionDescriptor> = listOf(RollDice(Dice.D6))
        override fun applyAction(action: GameAction, state: Game, rules: Rules): Command {
            return checkDiceRoll<D6Result>(action) {
                val homeResult = state.pitchInvasionHomeRoll!!.result + state.homeTeam.fanFactor
                val awayResult = it.result + state.awayTeam.fanFactor
                val nextNodes = when {
                    homeResult > awayResult -> {
                        arrayOf(SetActiveTeam(state.awayTeam), GotoNode(RollForAwayTeamStuns))
                    }
                    else -> {
                        arrayOf(SetActiveTeam(state.homeTeam), GotoNode(RollForHomeTeamStuns))
                    }
                }
                compositeCommandOf(
                    SetTemporaryState(Game::pitchInvasionHomeResult, homeResult),
                    SetTemporaryState(Game::pitchInvasionAwayResult, awayResult),
                    ReportPitchInvasionRoll(state.awayTeam, it, state.awayTeam.fanFactor),
                    *nextNodes
                )
            }
        }
    }

    object RollForHomeTeamStuns : ActionNode() {
        override fun getAvailableActions(state: Game, rules: Rules): List<ActionDescriptor> = listOf(RollDice(Dice.D3))
        override fun applyAction(action: GameAction, state: Game, rules: Rules): Command {
            return checkType<D3Result>(action) {
                compositeCommandOf(
                    SetTemporaryState(Game::pitchInvasionHomeTeamPlayersAffected, it.result),
                    GotoNode(ResolveHomeTeamStuns)
                )
            }
        }
    }

    // Home team always go first, so is responsible for also triggering away team stuns if needed
    object ResolveHomeTeamStuns : ActionNode() {
        override fun getAvailableActions(state: Game, rules: Rules): List<ActionDescriptor> {
            val onFieldPlayers = state.homeTeam.filter {
                it.location.isOnField(rules)
            }
            return listOf(SelectRandomPlayers(state.pitchInvasionHomeTeamPlayersAffected, onFieldPlayers))
        }

        override fun applyAction(action: GameAction, state: Game, rules: Rules): Command {
            return checkType<RandomPlayersSelected>(action) {
                val playerStuns = it.players.flatMap { player ->
                    listOf(
                        SetPlayerState(player, PlayerState.STUNNED),
                        ReportPlayerInjury(player, PlayerState.STUNNED)
                    )
                }.toTypedArray()
                val nextNode = when {
                    (state.pitchInvasionHomeResult == state.pitchInvasionAwayResult) -> GotoNode(RollForAwayTeamStuns)
                    (state.pitchInvasionHomeResult < state.pitchInvasionAwayResult) -> ExitProcedure()
                    else -> INVALID_GAME_STATE()
                }
                return compositeCommandOf(
                    *playerStuns,
                    nextNode
                )
            }
        }
    }

    object RollForAwayTeamStuns : ActionNode() {
        override fun getAvailableActions(state: Game, rules: Rules): List<ActionDescriptor> =
            listOf(RollDice(Dice.D3))

        override fun applyAction(action: GameAction, state: Game, rules: Rules): Command {
            return checkType<D3Result>(action) {
                compositeCommandOf(
                    SetTemporaryState(Game::pitchInvasionHomeTeamPlayersAffected, it.result),
                    GotoNode(ResolveAwayTeamStuns)
                )
            }
        }
    }

    object ResolveAwayTeamStuns : ActionNode() {
        override fun getAvailableActions(state: Game, rules: Rules): List<ActionDescriptor> {
            val onFieldPlayers = state.awayTeam.filter {
                it.location.isOnField(rules)
            }
            return listOf(SelectRandomPlayers(state.pitchInvasionAwayTeamPlayersAffected, onFieldPlayers))
        }

        override fun applyAction(action: GameAction, state: Game, rules: Rules): Command {
            return checkType<RandomPlayersSelected>(action) {
                val playerStuns = it.players.flatMap { player ->
                    listOf(
                        SetPlayerState(player, PlayerState.STUNNED),
                        ReportPlayerInjury(player, PlayerState.STUNNED)
                    )
                }.toTypedArray()
                return compositeCommandOf(
                    *playerStuns,
                    ExitProcedure()
                )
            }
        }
    }
}
