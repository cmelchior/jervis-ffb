package com.jervisffb.engine.rules.common.procedures.tables.weather

import com.jervisffb.engine.actions.Continue
import com.jervisffb.engine.actions.ContinueWhenReady
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
import com.jervisffb.engine.commands.context.AddContext
import com.jervisffb.engine.commands.context.RemoveContext
import com.jervisffb.engine.commands.context.UpdateContext
import com.jervisffb.engine.commands.fsm.ExitProcedure
import com.jervisffb.engine.commands.fsm.GotoNode
import com.jervisffb.engine.fsm.ActionNode
import com.jervisffb.engine.fsm.Node
import com.jervisffb.engine.fsm.Procedure
import com.jervisffb.engine.fsm.castAction
import com.jervisffb.engine.fsm.castDiceRoll
import com.jervisffb.engine.model.Game
import com.jervisffb.engine.model.PlayerDogoutState
import com.jervisffb.engine.model.Team
import com.jervisffb.engine.model.context.SwelteringHeatContext
import com.jervisffb.engine.model.context.getContext
import com.jervisffb.engine.model.locations.Dogout
import com.jervisffb.engine.reports.ReportDiceRoll
import com.jervisffb.engine.reports.ReportPlayerInjury
import com.jervisffb.engine.rules.DiceRollType
import com.jervisffb.engine.rules.Rules
import kotlin.math.min

/**
 * Procedure for handling "Sweltering Heat".
 *
 * See page 37 in the BB2020 rulebook.
 * See page 46 in the BB2025 rulebook.
 */
object SwelteringHeat : Procedure() {
    override val initialNode: Node = RollForHomeTeam
    override fun onEnterProcedure(state: Game, rules: Rules): Command {
        return AddContext(SwelteringHeatContext())
    }
    override fun onExitProcedure(state: Game, rules: Rules): Command {
        return compositeCommandOf(
            RemoveContext<SwelteringHeatContext>()
        )
    }

    object RollForHomeTeam : ActionNode() {
        override fun actionOwner(state: Game, rules: Rules): Team? = null
        override fun getAvailableActions(state: Game, rules: Rules): List<GameActionDescriptor> {
            // Skip rolling if there are no available players on the pitch.
            val onPitchPlayers = state.homeTeam.filter { it.location.isOnPitch(rules) }.map { it.id }
            return when (onPitchPlayers.isEmpty()) {
                true -> listOf(ContinueWhenReady)
                false -> listOf(RollDice(Dice.D3))
            }
        }
        override fun applyAction(action: GameAction, state: Game, rules: Rules): Command {
            return when (action) {
                Continue -> {
                    compositeCommandOf(
                        ReportNoSwelteringHeatRoll(state.homeTeam),
                        GotoNode(RollForAwayTeam)
                    )
                }

                else -> {
                    castDiceRoll<D3Result>(action) { d3 ->
                        compositeCommandOf(
                            ReportDiceRoll(DiceRollType.SWELTERING_HEAT, d3),
                            UpdateContext(state.getContext<SwelteringHeatContext>().copy(homeRoll = d3)),
                            GotoNode(SelectPlayersOnHomeTeam)
                        )
                    }
                }
            }
        }
    }

    object SelectPlayersOnHomeTeam : ActionNode() {
        override fun actionOwner(state: Game, rules: Rules): Team? = null
        override fun getAvailableActions(state: Game, rules: Rules): List<GameActionDescriptor> {
            val onPitchPlayers = state.homeTeam.filter { it.location.isOnPitch(rules) }.map { it.id }
            val affectedPlayers = state.getContext<SwelteringHeatContext>().homeRoll!!.value
            return listOf(SelectRandomPlayers(min(onPitchPlayers.size, affectedPlayers), onPitchPlayers))
        }

        override fun applyAction(action: GameAction, state: Game, rules: Rules): Command {
            return castAction<RandomPlayersSelected>(action) {
                val playersRemoved = it.getPlayers(state).flatMap { player ->
                    listOf(
                        SetPlayerState(player, PlayerDogoutState.FAINTED),
                        SetPlayerLocation(player, Dogout),
                        ReportPlayerInjury(player, PlayerDogoutState.FAINTED),
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
            // Skip rolling if there are no available players on the pitch.
            val onPitchPlayers = state.awayTeam.filter { it.location.isOnPitch(rules) }.map { it.id }
            return when (onPitchPlayers.isEmpty()) {
                true -> listOf(ContinueWhenReady)
                false -> listOf(RollDice(Dice.D3))
            }
        }

        override fun applyAction(action: GameAction, state: Game, rules: Rules): Command {
            return when (action) {
                Continue -> {
                    compositeCommandOf(
                        ReportNoSwelteringHeatRoll(state.awayTeam),
                        ExitProcedure()
                    )
                }
                else -> {
                    castDiceRoll<D3Result>(action) { d3 ->
                        compositeCommandOf(
                            ReportDiceRoll(DiceRollType.SWELTERING_HEAT, d3),
                            UpdateContext(state.getContext<SwelteringHeatContext>().copy(awayRoll = d3)),
                            GotoNode(SelectPlayersOnAwayTeam)
                        )
                    }
                }
            }
        }
    }

    object SelectPlayersOnAwayTeam : ActionNode() {
        override fun actionOwner(state: Game, rules: Rules): Team? = null
        override fun getAvailableActions(state: Game, rules: Rules): List<GameActionDescriptor> {
            val onPitchPlayers = state.awayTeam.filter { it.location.isOnPitch(rules) }.map { it.id }
            val affectedPlayers = state.getContext<SwelteringHeatContext>().awayRoll!!.value
            return listOf(SelectRandomPlayers(min(onPitchPlayers.size, affectedPlayers), onPitchPlayers))
        }

        override fun applyAction(action: GameAction, state: Game, rules: Rules): Command {
            return castAction<RandomPlayersSelected>(action) {
                val playersRemoved = it.getPlayers(state).flatMap { player ->
                    listOf(
                        SetPlayerState(player, PlayerDogoutState.FAINTED),
                        SetPlayerLocation(player, Dogout),
                        ReportPlayerInjury(player, PlayerDogoutState.FAINTED),
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
