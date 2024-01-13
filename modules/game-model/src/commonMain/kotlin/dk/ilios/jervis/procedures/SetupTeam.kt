package dk.ilios.jervis.procedures

import compositeCommandOf
import dk.ilios.jervis.actions.GameAction
import dk.ilios.jervis.actions.ActionDescriptor
import dk.ilios.jervis.actions.DogoutSelected
import dk.ilios.jervis.actions.EndSetup
import dk.ilios.jervis.actions.EndSetupWhenReady
import dk.ilios.jervis.actions.FieldSquareSelected
import dk.ilios.jervis.actions.PlayerSelected
import dk.ilios.jervis.actions.SelectDogout
import dk.ilios.jervis.actions.SelectFieldLocation
import dk.ilios.jervis.actions.SelectPlayer
import dk.ilios.jervis.commands.Command
import dk.ilios.jervis.commands.ExitProcedure
import dk.ilios.jervis.commands.GotoNode
import dk.ilios.jervis.commands.SetActivePlayer
import dk.ilios.jervis.commands.SetPlayerAvailability
import dk.ilios.jervis.commands.SetPlayerLocation
import dk.ilios.jervis.commands.SetPlayerState
import dk.ilios.jervis.fsm.ActionNode
import dk.ilios.jervis.fsm.ComputationNode
import dk.ilios.jervis.fsm.ConfirmationNode
import dk.ilios.jervis.fsm.Node
import dk.ilios.jervis.fsm.Procedure
import dk.ilios.jervis.model.DogOut
import dk.ilios.jervis.model.FieldCoordinate
import dk.ilios.jervis.model.Game
import dk.ilios.jervis.model.PlayerState
import dk.ilios.jervis.rules.Rules
import dk.ilios.jervis.utils.INVALID_ACTION

object SetupTeam: Procedure() {
    override val initialNode: Node = SelectPlayerOrEndSetup
    override fun onEnterProcedure(state: Game, rules: Rules): Command? = null
    override fun onExitProcedure(state: Game, rules: Rules): Command? = null

    object SelectPlayerOrEndSetup: ActionNode() {

        override fun getAvailableActions(state: Game, rules: Rules): List<ActionDescriptor> {
            val availablePlayers = state.activeTeam.filter {
                val inReserve = (it.location == DogOut && it.state == PlayerState.STANDING)
                val onField = (it.location is FieldCoordinate && it.state == PlayerState.STANDING)
                inReserve || onField
            }.map {
                SelectPlayer(it)
            }
            return availablePlayers + EndSetupWhenReady
        }

        override fun applyAction(action: GameAction, state: Game, rules: Rules): Command {
            return when(action) {
                is PlayerSelected -> {
                    compositeCommandOf(
                        SetActivePlayer(action.player),
                        GotoNode(PlacePlayer)
                    )
                }
                EndSetup -> GotoNode(EndSetupAndValidate)
                else -> INVALID_ACTION(action)
            }
        }
    }

    object PlacePlayer: ActionNode() {
        override fun getAvailableActions(state: Game, rules: Rules): List<ActionDescriptor> {
            // Allow players to be placed on the kicking teams side. At this stage, the more
            // elaborate rules are not enforced. That will first happen in `EndSetupAndValidate`
            val isHomeTeam = state.activeTeam.isHomeTeam()
            val freeFields: List<SelectFieldLocation> = state.field
                .filter {
                    // Only select from fields on teams half
                    // TODO How does this generalize to BB7?
                    if (isHomeTeam) {
                        it.x < rules.fieldWidth.toInt()/2
                    } else {
                        it.x >= rules.fieldWidth.toInt()/2
                    }
                }
                .filter { it.isEmpty() }
                .map { SelectFieldLocation(it.x, it.y) }

            val playerLocation = state.activePlayer!!.location
            var playerSquare: List<SelectFieldLocation> = emptyList()
            if (playerLocation is FieldCoordinate) {
                playerSquare = listOf(SelectFieldLocation(playerLocation.x, playerLocation.y))
            }
            return freeFields + SelectDogout + playerSquare
        }

        override fun applyAction(action: GameAction, state: Game, rules: Rules): Command {
            return when(action) {
                DogoutSelected -> compositeCommandOf(
                    SetPlayerLocation(state.activePlayer!!, DogOut),
                    SetPlayerState(state.activePlayer!!, PlayerState.STANDING),
                    SetActivePlayer(null),
                    GotoNode(SelectPlayerOrEndSetup)
                )
                is FieldSquareSelected -> {
                    when (state.activeTeam.isHomeTeam()) {
                        true -> if (action.coordinate.isOnAwaySide(rules)) INVALID_ACTION(action)
                        false -> if (action.coordinate.isOnHomeSide(rules)) INVALID_ACTION(action)
                    }
                    compositeCommandOf(
                        SetPlayerLocation(state.activePlayer!!, FieldCoordinate(action.x, action.y)),
                        SetPlayerState(state.activePlayer!!, PlayerState.STANDING),
                        SetActivePlayer(null),
                        GotoNode(SelectPlayerOrEndSetup)
                    )
                }
                else -> INVALID_ACTION(action)
            }
        }
    }

    object EndSetupAndValidate: ComputationNode() {
        override fun apply(state: Game, rules: Rules): Command {
            return if (rules.isValidSetup(state)) {
                ExitProcedure()
            } else {
                GotoNode(InformOfInvalidSetup)
            }
        }
    }

    object InformOfInvalidSetup: ConfirmationNode() {
        override fun apply(state: Game, rules: Rules): Command {
            return GotoNode(SelectPlayerOrEndSetup)
        }
    }
}