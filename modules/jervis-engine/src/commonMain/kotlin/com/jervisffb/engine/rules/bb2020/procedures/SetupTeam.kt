package com.jervisffb.engine.rules.bb2020.procedures

import com.jervisffb.engine.actions.ActionDescriptor
import com.jervisffb.engine.actions.Confirm
import com.jervisffb.engine.actions.ConfirmWhenReady
import com.jervisffb.engine.actions.DogoutSelected
import com.jervisffb.engine.actions.EndSetup
import com.jervisffb.engine.actions.EndSetupWhenReady
import com.jervisffb.engine.actions.FieldSquareSelected
import com.jervisffb.engine.actions.GameAction
import com.jervisffb.engine.actions.PlayerSelected
import com.jervisffb.engine.actions.SelectDogout
import com.jervisffb.engine.actions.SelectFieldLocation
import com.jervisffb.engine.actions.SelectPlayer
import com.jervisffb.engine.commands.Command
import com.jervisffb.engine.commands.RemoveContext
import com.jervisffb.engine.commands.SetContext
import com.jervisffb.engine.commands.SetPlayerLocation
import com.jervisffb.engine.commands.SetPlayerState
import com.jervisffb.engine.commands.fsm.ExitProcedure
import com.jervisffb.engine.commands.fsm.GotoNode
import com.jervisffb.engine.fsm.ActionNode
import com.jervisffb.engine.fsm.ComputationNode
import com.jervisffb.engine.fsm.Node
import com.jervisffb.engine.fsm.Procedure
import com.jervisffb.engine.fsm.checkType
import com.jervisffb.engine.fsm.checkTypeAndValue
import com.jervisffb.engine.model.Game
import com.jervisffb.engine.model.Player
import com.jervisffb.engine.model.PlayerState
import com.jervisffb.engine.model.Team
import com.jervisffb.engine.model.context.ProcedureContext
import com.jervisffb.engine.model.context.assertContext
import com.jervisffb.engine.model.context.getContext
import com.jervisffb.engine.model.locations.DogOut
import com.jervisffb.engine.model.locations.FieldCoordinate
import com.jervisffb.engine.rules.Rules
import com.jervisffb.engine.utils.INVALID_ACTION
import com.jervisffb.engine.commands.compositeCommandOf

data class SetupTeamContext(
    val team: Team,
    var currentPlayer: Player? = null
): ProcedureContext

object SetupTeam : Procedure() {
    override val initialNode: Node = SelectPlayerOrEndSetup
    override fun onEnterProcedure(state: Game, rules: Rules): Command? = null
    override fun onExitProcedure(state: Game, rules: Rules): Command = RemoveContext<SetupTeamContext>()
    override fun isValid(state: Game, rules: Rules) = state.assertContext<SetupTeamContext>()

    object SelectPlayerOrEndSetup : ActionNode() {
        override fun actionOwner(state: Game, rules: Rules): Team = state.getContext<SetupTeamContext>().team
        override fun getAvailableActions(state: Game, rules: Rules): List<ActionDescriptor> {
            val context = state.getContext<SetupTeamContext>()
            val availablePlayers =
                context.team.filter {
                    val inReserve = (it.location == DogOut && it.state == PlayerState.RESERVE)
                    val onField = (it.location is FieldCoordinate && it.state == PlayerState.STANDING)
                    inReserve || onField
                }.map {
                    SelectPlayer(it)
                }
            return availablePlayers + EndSetupWhenReady
        }

        override fun applyAction(action: GameAction, state: Game, rules: Rules): Command {
            val context = state.getContext<SetupTeamContext>()
            return when (action) {
                EndSetup -> GotoNode(EndSetupAndValidate)
                else -> {
                    checkTypeAndValue<PlayerSelected>(state, rules, action) { playerSelected ->
                        compositeCommandOf(
                            SetContext(context.copy(currentPlayer = playerSelected.getPlayer(state))),
                            GotoNode(PlacePlayer),
                        )
                    }
                }
            }
        }
    }

    object PlacePlayer : ActionNode() {
        override fun actionOwner(state: Game, rules: Rules): Team = state.getContext<SetupTeamContext>().team
        override fun getAvailableActions(state: Game, rules: Rules): List<ActionDescriptor> {
            // Allow players to be placed on the kicking teams side. At this stage, the more
            // elaborate rules are not enforced. That will first happen in `EndSetupAndValidate`
            val context = state.getContext<SetupTeamContext>()
            val isHomeTeam = context.team.isHomeTeam()
            val freeFields: List<SelectFieldLocation> =
                state.field
                    .filter {
                        // Only select from fields on teams half
                        // TODO How does this generalize to BB7?
                        if (isHomeTeam) {
                            it.x < rules.fieldWidth.toInt() / 2
                        } else {
                            it.x >= rules.fieldWidth.toInt() / 2
                        }
                    }
                    .filter { it.isUnoccupied() }
                    .map { SelectFieldLocation.setup(it.coordinates) }

            val playerLocation = context.currentPlayer!!.location
            var playerSquare: List<SelectFieldLocation> = emptyList()
            if (playerLocation is FieldCoordinate) {
                playerSquare = listOf(SelectFieldLocation.setup(playerLocation))
            }
            return freeFields + SelectDogout + playerSquare
        }

        override fun applyAction(action: GameAction, state: Game, rules: Rules): Command {
            val context = state.getContext<SetupTeamContext>()
            val player = context.currentPlayer!!
            return when (action) {
                DogoutSelected -> {
                    compositeCommandOf(
                        SetPlayerLocation(player, DogOut),
                        SetPlayerState(player, PlayerState.RESERVE),
                        SetContext(context.copy(currentPlayer = null)),
                        GotoNode(SelectPlayerOrEndSetup),
                    )
                }
                is FieldSquareSelected -> {
                    when (context.team.isHomeTeam()) {
                        true -> if (action.coordinate.isOnAwaySide(rules)) INVALID_ACTION(action)
                        false -> if (action.coordinate.isOnHomeSide(rules)) INVALID_ACTION(action)
                    }
                    compositeCommandOf(
                        SetPlayerLocation(player, FieldCoordinate(action.x, action.y)),
                        SetPlayerState(player, PlayerState.STANDING),
                        SetContext(context.copy(currentPlayer = null)),
                        GotoNode(SelectPlayerOrEndSetup),
                    )
                }
                else -> INVALID_ACTION(action)
            }
        }
    }

    object EndSetupAndValidate : ComputationNode() {
        override fun apply(state: Game, rules: Rules): Command {
            val context = state.getContext<SetupTeamContext>()
            return if (rules.isValidSetup(state, context.team)) {
                ExitProcedure()
            } else {
                GotoNode(InformOfInvalidSetup)
            }
        }
    }

    object InformOfInvalidSetup : ActionNode() {
        override fun actionOwner(state: Game, rules: Rules): Team = state.getContext<SetupTeamContext>().team
        override fun getAvailableActions(state: Game, rules: Rules): List<ActionDescriptor> {
            return listOf(ConfirmWhenReady)
        }
        override fun applyAction(action: GameAction, state: Game, rules: Rules): Command {
            return checkType<Confirm>(action) {
                GotoNode(SelectPlayerOrEndSetup)
            }
        }
    }
}
