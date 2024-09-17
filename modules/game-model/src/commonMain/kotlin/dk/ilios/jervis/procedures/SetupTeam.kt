package dk.ilios.jervis.procedures

import compositeCommandOf
import dk.ilios.jervis.actions.ActionDescriptor
import dk.ilios.jervis.actions.Confirm
import dk.ilios.jervis.actions.ConfirmWhenReady
import dk.ilios.jervis.actions.DogoutSelected
import dk.ilios.jervis.actions.EndSetup
import dk.ilios.jervis.actions.EndSetupWhenReady
import dk.ilios.jervis.actions.FieldSquareSelected
import dk.ilios.jervis.actions.GameAction
import dk.ilios.jervis.actions.PlayerSelected
import dk.ilios.jervis.actions.SelectDogout
import dk.ilios.jervis.actions.SelectFieldLocation
import dk.ilios.jervis.actions.SelectPlayer
import dk.ilios.jervis.commands.Command
import dk.ilios.jervis.commands.RemoveContext
import dk.ilios.jervis.commands.SetContext
import dk.ilios.jervis.commands.SetPlayerLocation
import dk.ilios.jervis.commands.SetPlayerState
import dk.ilios.jervis.commands.fsm.ExitProcedure
import dk.ilios.jervis.commands.fsm.GotoNode
import dk.ilios.jervis.fsm.ActionNode
import dk.ilios.jervis.fsm.ComputationNode
import dk.ilios.jervis.fsm.Node
import dk.ilios.jervis.fsm.Procedure
import dk.ilios.jervis.model.DogOut
import dk.ilios.jervis.model.FieldCoordinate
import dk.ilios.jervis.model.Game
import dk.ilios.jervis.model.Player
import dk.ilios.jervis.model.PlayerState
import dk.ilios.jervis.model.Team
import dk.ilios.jervis.model.context.ProcedureContext
import dk.ilios.jervis.model.context.assertContext
import dk.ilios.jervis.model.context.getContext
import dk.ilios.jervis.rules.Rules
import dk.ilios.jervis.utils.INVALID_ACTION

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
                    checkTypeAndValue<PlayerSelected>(state, rules, action, this) { playerSelected ->
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
                    .map { SelectFieldLocation.setup(it.coordinate) }

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
