package dk.ilios.jervis.procedures.weather

import compositeCommandOf
import dk.ilios.jervis.actions.ActionDescriptor
import dk.ilios.jervis.actions.D3Result
import dk.ilios.jervis.actions.Dice
import dk.ilios.jervis.actions.GameAction
import dk.ilios.jervis.actions.RandomPlayersSelected
import dk.ilios.jervis.actions.RollDice
import dk.ilios.jervis.actions.SelectRandomPlayers
import dk.ilios.jervis.commands.Command
import dk.ilios.jervis.commands.ExitProcedure
import dk.ilios.jervis.commands.GotoNode
import dk.ilios.jervis.commands.RemoveContext
import dk.ilios.jervis.commands.SetActiveTeam
import dk.ilios.jervis.commands.SetContext
import dk.ilios.jervis.commands.SetPlayerLocation
import dk.ilios.jervis.commands.SetPlayerState
import dk.ilios.jervis.fsm.ActionNode
import dk.ilios.jervis.fsm.Node
import dk.ilios.jervis.fsm.Procedure
import dk.ilios.jervis.model.DogOut
import dk.ilios.jervis.model.Game
import dk.ilios.jervis.model.PlayerState
import dk.ilios.jervis.model.context.SwelteringHeatContext
import dk.ilios.jervis.model.context.getContext
import dk.ilios.jervis.reports.ReportDiceRoll
import dk.ilios.jervis.reports.ReportPlayerInjury
import dk.ilios.jervis.rules.Rules
import dk.ilios.jervis.rules.skills.DiceRollType
import dk.ilios.jervis.utils.INVALID_GAME_STATE

/**
 * Procedure for handling "Sweltering Heat" as described on page 37 in the
 * rulebook.
 */
object SwelteringHeat : Procedure() {
    override val initialNode: Node = RollForHomeTeam

    override fun onEnterProcedure(state: Game, rules: Rules): Command {
        if (state.activeTeam != state.homeTeam) {
            INVALID_GAME_STATE("Wrong active team: ${state.activeTeam}")
        }
        return SetContext(SwelteringHeatContext())
    }

    override fun onExitProcedure(state: Game, rules: Rules): Command {
        return compositeCommandOf(
            SetActiveTeam(state.homeTeam),
            RemoveContext<SwelteringHeatContext>()
        )
    }

    object RollForHomeTeam : ActionNode() {
        override fun getAvailableActions(state: Game, rules: Rules): List<ActionDescriptor> {
            return listOf(RollDice(Dice.D3))
        }

        override fun applyAction(action: GameAction, state: Game, rules: Rules): Command {
            return checkDiceRoll<D3Result>(action) { d3 ->
                compositeCommandOf(
                    ReportDiceRoll(DiceRollType.SWELTERING_HEAT, d3),
                    SetContext(state.getContext<SwelteringHeatContext>().copy(homeRoll = d3)),
                    GotoNode(RollForPlayersOnHomeTeam)
                )
            }
        }
    }

    object RollForPlayersOnHomeTeam : ActionNode() {
        override fun getAvailableActions(state: Game, rules: Rules): List<ActionDescriptor> {
            val onFieldPlayers = state.homeTeam.filter { it.location.isOnField(rules) }
            val affectedPlayers = state.getContext<SwelteringHeatContext>().homeRoll!!.value
            return listOf(SelectRandomPlayers(affectedPlayers, onFieldPlayers))
        }

        override fun applyAction(action: GameAction, state: Game, rules: Rules): Command {
            return checkType<RandomPlayersSelected>(action) {
                val playersRemoved = it.players.flatMap { player ->
                    listOf(
                        SetPlayerState(player, PlayerState.FAINTED),
                        SetPlayerLocation(player, DogOut),
                        ReportPlayerInjury(player, PlayerState.FAINTED),
                    )
                }.toTypedArray()
                return compositeCommandOf(
                    *playersRemoved,
                    SetActiveTeam(state.awayTeam),
                    GotoNode(RollForAwayTeam)
                )
            }
        }
    }

    object RollForAwayTeam : ActionNode() {
        override fun getAvailableActions(state: Game, rules: Rules): List<ActionDescriptor> {
            return listOf(RollDice(Dice.D3))
        }

        override fun applyAction(action: GameAction, state: Game, rules: Rules): Command {
            return checkDiceRoll<D3Result>(action) { d3 ->
                compositeCommandOf(
                    ReportDiceRoll(DiceRollType.SWELTERING_HEAT, d3),
                    SetContext(state.getContext<SwelteringHeatContext>().copy(awayRoll = d3)),
                    GotoNode(RollForPlayersOnAwayTeam)
                )
            }
        }
    }

    object RollForPlayersOnAwayTeam : ActionNode() {
        override fun getAvailableActions(state: Game, rules: Rules): List<ActionDescriptor> {
            val onFieldPlayers = state.awayTeam.filter { it.location.isOnField(rules) }
            val affectedPlayers = state.getContext<SwelteringHeatContext>().awayRoll!!.value
            return listOf(SelectRandomPlayers(affectedPlayers, onFieldPlayers))
        }

        override fun applyAction(action: GameAction, state: Game, rules: Rules): Command {
            return checkType<RandomPlayersSelected>(action) {
                val playersRemoved = it.players.flatMap { player ->
                    listOf(
                        SetPlayerState(player, PlayerState.FAINTED),
                        SetPlayerLocation(player, DogOut),
                        ReportPlayerInjury(player, PlayerState.FAINTED),
                    )
                }.toTypedArray()
                return compositeCommandOf(
                    *playersRemoved,
                    ExitProcedure()
                )
            }
        }
    }
}
