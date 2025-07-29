package com.jervisffb.ui.game.state.decorators

import com.jervisffb.engine.actions.CompositeGameAction
import com.jervisffb.engine.actions.FieldSquareSelected
import com.jervisffb.engine.actions.MoveType
import com.jervisffb.engine.actions.MoveTypeSelected
import com.jervisffb.engine.actions.SelectMoveType
import com.jervisffb.engine.model.Game
import com.jervisffb.engine.model.Player
import com.jervisffb.engine.model.context.getContext
import com.jervisffb.engine.model.locations.FieldCoordinate
import com.jervisffb.engine.model.locations.OnFieldLocation
import com.jervisffb.engine.rules.PlayerStandardActionType
import com.jervisffb.engine.rules.bb2020.procedures.ActivatePlayerContext
import com.jervisffb.ui.game.UiGameSnapshot
import com.jervisffb.ui.game.icons.ActionIcon
import com.jervisffb.ui.game.state.ManualActionProvider
import com.jervisffb.ui.game.state.QueuedActionsResult
import com.jervisffb.ui.game.view.ContextMenuOption

class SelectMoveTypeDecorator: FieldActionDecorator<SelectMoveType> {

    // Actions we allow to skip manually selecting Stand Up
    val eligibleActions = setOf(
        PlayerStandardActionType.MOVE,
        PlayerStandardActionType.FOUL,
        PlayerStandardActionType.PASS,
        PlayerStandardActionType.BLITZ,
        PlayerStandardActionType.HAND_OFF,
        PlayerStandardActionType.THROW_TEAM_MATE
    )

    override fun decorate(actionProvider: ManualActionProvider, state: Game, snapshot: UiGameSnapshot, descriptor: SelectMoveType) {
        descriptor.types.forEach {
            handleType(actionProvider, state, snapshot, it)
        }
    }

    private fun handleType(actionProvider: ManualActionProvider, state: Game, snapshot: UiGameSnapshot, type: MoveType) {
        val player = state.activePlayer ?: error("No active player")
        val activeLocation = player.location as OnFieldLocation
        val activeSquare = snapshot.fieldSquares[activeLocation] ?: error("No square found: $activeLocation")

        // For move selection, some types of moves we want to display on the field
        // others should be a specific action that must be selected.
        // On-field moves are shortcutting the Rules engine, so we need to account for that as well
        when (type) {
            MoveType.JUMP -> {
                activeSquare.contextMenuOptions.add(
                    ContextMenuOption(
                        "Jump",
                        { actionProvider.userActionSelected(MoveTypeSelected(MoveType.JUMP)) },
                        ActionIcon.JUMP
                    )
                )
            }

            MoveType.LEAP -> {
                activeSquare.contextMenuOptions.add(
                    ContextMenuOption(
                        "Leap",
                        { actionProvider.userActionSelected(MoveTypeSelected(MoveType.LEAP)) },
                        ActionIcon.LEAP
                    )
                )
            }

            MoveType.STANDARD -> {
                val requiresDodge = state.rules.calculateMarks(state, player.team, activeLocation) > 0
                val requiresRush = player.movesLeft == 0 && player.rushesLeft > 0

                // We calculate all paths here, rather than doing it in the ViewModel. Mostly because
                // it allows us to front-load slightly more computations. But it hasn't been benchmarked,
                // Maybe doing the calculation on the fly is fine.
                val allPaths = state.rules.pathFinder.calculateAllPaths(
                    state,
                    activeLocation as FieldCoordinate,
                    if (requiresDodge) 1 else player.movesLeft,
                )
                snapshot.pathFinder = allPaths

                // Also mark all fields around the player as immediately selectable
                activeLocation.getSurroundingCoordinates(state.rules, 1, includeOutOfBounds = false)
                    .filter { state.field[it].isUnoccupied() }
                    .forEach { loc ->
                        snapshot.fieldSquares[loc]?.apply {
                            onSelected = {
                                actionProvider.userActionSelected(
                                    CompositeGameAction(
                                        listOf(
                                            MoveTypeSelected(MoveType.STANDARD),
                                            FieldSquareSelected(loc)
                                        )
                                    )
                                )
                            }
                            requiresRoll = requiresDodge || requiresRush
                        } ?: error("Could not find square: $loc")
                    }
            }

            MoveType.STAND_UP -> {
                // Add Standing Up Action to the context menu.
                activeSquare.contextMenuOptions.add(
                    ContextMenuOption(
                        "Stand-Up",
                        { actionProvider.userActionSelected(MoveTypeSelected(MoveType.STAND_UP)) },
                        ActionIcon.STAND_UP
                    )
                )
                addStandUpAndMoveOptions(actionProvider, state, player, activeLocation, snapshot)
            }
        }
    }

    // Add UI options that allows the User to skip manually selecting Stand Up
    // and then move. Instead players can move directly.
    private fun addStandUpAndMoveOptions(
        actionProvider: ManualActionProvider,
        state: Game,
        player: Player,
        activeLocation: OnFieldLocation,
        snapshot: UiGameSnapshot
    ) {
        // For Standing Up, we make it easier for the player depending
        // on their Action. So if there is a move part of their
        // current action, we try to calculate what they can do after standing
        // up and allow the player to go directly that.
        val action = state.getContext<ActivatePlayerContext>().declaredAction?.type
        if (!eligibleActions.contains(action)) return

        val requiresDodge = state.rules.calculateMarks(state, player.team, activeLocation) > 0
        val requiresRush = player.move < state.rules.moveRequiredForStandingUp

        // If Player must either dodge or has less than 3 move, it requires a Rush/Dodge Roll to move anywhere, so we
        // just mark all open squares around the player as "requires a roll" to move to, but do not otherwise use the
        // PathFinder.
        if (requiresRush || requiresDodge) {
            addSelectableRushSquares(activeLocation, state, snapshot, actionProvider)
        } else {
            val allPaths = state.rules.pathFinder.calculateAllPaths(
                state,
                activeLocation as FieldCoordinate,
                (player.move - state.rules.moveRequiredForStandingUp).coerceAtLeast(0),
            )
            snapshot.pathFinder = allPaths
        }
    }

    private fun addSelectableRushSquares(
        activeLocation: OnFieldLocation,
        state: Game,
        snapshot: UiGameSnapshot,
        actionProvider: ManualActionProvider
    ) {
        activeLocation.getSurroundingCoordinates(state.rules, 1, includeOutOfBounds = false)
            .filter { state.field[it].isUnoccupied() }
            .forEach { loc ->
                val square = snapshot.fieldSquares[loc]
                snapshot.fieldSquares[loc]?.apply {
                    onSelected = {
                        actionProvider.registerQueuedActionGenerator { controller ->
                            val availableActions = controller.getAvailableActions()
                            val canMove = availableActions.contains(MoveType.STANDARD)
                            if (canMove) {
                                val action = CompositeGameAction(
                                    MoveTypeSelected(MoveType.STANDARD),
                                    FieldSquareSelected(loc)
                                )
                                QueuedActionsResult(action)
                            } else {
                                null
                            }
                        }
                        actionProvider.userActionSelected(MoveTypeSelected(MoveType.STAND_UP))
                    }
                    requiresRoll = true
                } ?: error("Could not find square: $loc")
            }
    }
}
