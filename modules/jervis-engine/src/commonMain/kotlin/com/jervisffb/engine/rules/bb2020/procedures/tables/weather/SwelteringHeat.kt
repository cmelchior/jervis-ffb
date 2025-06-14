package com.jervisffb.engine.rules.bb2020.procedures.tables.weather

import com.jervisffb.engine.actions.D3Result
import com.jervisffb.engine.actions.Dice
import com.jervisffb.engine.actions.GameAction
import com.jervisffb.engine.actions.GameActionDescriptor
import com.jervisffb.engine.actions.RandomPlayersSelected
import com.jervisffb.engine.actions.RollDice
import com.jervisffb.engine.actions.SelectRandomPlayers
import com.jervisffb.engine.commands.Command
import com.jervisffb.engine.commands.SetPlayerLocation
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
import com.jervisffb.engine.model.context.SwelteringHeatContext
import com.jervisffb.engine.model.context.getContext
import com.jervisffb.engine.model.locations.DogOut
import com.jervisffb.engine.reports.ReportDiceRoll
import com.jervisffb.engine.reports.ReportPlayerInjury
import com.jervisffb.engine.rules.DiceRollType
import com.jervisffb.engine.rules.Rules

/**
 * Procedure for handling "Sweltering Heat" as described on page 37 in the
 * rulebook.
 */
object SwelteringHeat : Procedure() {
    override val initialNode: Node = RollForHomeTeam
    override fun onEnterProcedure(state: Game, rules: Rules): Command {
        return SetContext(SwelteringHeatContext())
    }
    override fun onExitProcedure(state: Game, rules: Rules): Command {
        return compositeCommandOf(
            RemoveContext<SwelteringHeatContext>()
        )
    }

    object RollForHomeTeam : ActionNode() {
        override fun actionOwner(state: Game, rules: Rules): Team? = null
        override fun getAvailableActions(state: Game, rules: Rules): List<GameActionDescriptor> {
            return listOf(RollDice(Dice.D3))
        }
        override fun applyAction(action: GameAction, state: Game, rules: Rules): Command {
            return checkDiceRoll<D3Result>(action) { d3 ->
                compositeCommandOf(
                    ReportDiceRoll(DiceRollType.SWELTERING_HEAT, d3),
                    SetContext(state.getContext<SwelteringHeatContext>().copy(homeRoll = d3)),
                    GotoNode(SelectPlayersOnHomeTeam)
                )
            }
        }
    }

    object SelectPlayersOnHomeTeam : ActionNode() {
        override fun actionOwner(state: Game, rules: Rules): Team? = null
        override fun getAvailableActions(state: Game, rules: Rules): List<GameActionDescriptor> {
            val onFieldPlayers = state.homeTeam.filter { it.location.isOnField(rules) }.map { it.id }
            val affectedPlayers = state.getContext<SwelteringHeatContext>().homeRoll!!.value
            return listOf(SelectRandomPlayers(affectedPlayers, onFieldPlayers))
        }

        override fun applyAction(action: GameAction, state: Game, rules: Rules): Command {
            return checkType<RandomPlayersSelected>(action) {
                val playersRemoved = it.getPlayers(state).flatMap { player ->
                    listOf(
                        SetPlayerState(player, PlayerState.FAINTED),
                        SetPlayerLocation(player, DogOut),
                        ReportPlayerInjury(player, PlayerState.FAINTED),
                    )
                }.toTypedArray()
                return compositeCommandOf(
                    *playersRemoved,
                    GotoNode(RollForAwayTeam)
                )
            }
        }
    }

    object RollForAwayTeam : ActionNode() {
        override fun actionOwner(state: Game, rules: Rules): Team? = null
        override fun getAvailableActions(state: Game, rules: Rules): List<GameActionDescriptor> {
            return listOf(RollDice(Dice.D3))
        }

        override fun applyAction(action: GameAction, state: Game, rules: Rules): Command {
            return checkDiceRoll<D3Result>(action) { d3 ->
                compositeCommandOf(
                    ReportDiceRoll(DiceRollType.SWELTERING_HEAT, d3),
                    SetContext(state.getContext<SwelteringHeatContext>().copy(awayRoll = d3)),
                    GotoNode(SelectPlayersOnAwayTeam)
                )
            }
        }
    }

    object SelectPlayersOnAwayTeam : ActionNode() {
        override fun actionOwner(state: Game, rules: Rules): Team? = null
        override fun getAvailableActions(state: Game, rules: Rules): List<GameActionDescriptor> {
            val onFieldPlayers = state.awayTeam.filter { it.location.isOnField(rules) }.map { it.id }
            val affectedPlayers = state.getContext<SwelteringHeatContext>().awayRoll!!.value
            return listOf(SelectRandomPlayers(affectedPlayers, onFieldPlayers))
        }

        override fun applyAction(action: GameAction, state: Game, rules: Rules): Command {
            return checkType<RandomPlayersSelected>(action) {
                val playersRemoved = it.getPlayers(state).flatMap { player ->
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
