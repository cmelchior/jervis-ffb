package com.jervisffb.ui.state.decorators

import com.jervisffb.engine.actions.CompositeGameAction
import com.jervisffb.engine.actions.FieldSquareSelected
import com.jervisffb.engine.actions.MoveType
import com.jervisffb.engine.actions.MoveTypeSelected
import com.jervisffb.engine.actions.SelectMoveType
import com.jervisffb.engine.model.Game
import com.jervisffb.engine.model.locations.FieldCoordinate
import com.jervisffb.engine.model.locations.OnFieldLocation
import com.jervisffb.ui.UiGameSnapshot
import com.jervisffb.ui.state.UiActionProvider
import com.jervisffb.ui.view.ContextMenuOption

class SelectMoveTypeDecorator: FieldActionDecorator<SelectMoveType> {
    override fun decorate(actionProvider: UiActionProvider, state: Game, snapshot: UiGameSnapshot, descriptor: SelectMoveType) {
        val player = state.activePlayer ?: error("No active player")
        val activeLocation = player.location as OnFieldLocation
        val activeSquare = snapshot.fieldSquares[activeLocation] ?: error("No square found: $activeLocation")

        // For move selection, some types of moves we want to display on the field
        // others should be a specific action that must be selected.
        // On-field moves are shortcutting the Rules engine, so we need to account for that as well
        when (descriptor.type) {
            MoveType.JUMP -> {
                activeSquare.contextMenuOptions.add(
                    ContextMenuOption(
                        "Jump",
                        { actionProvider.userActionSelected(MoveTypeSelected(MoveType.JUMP)) },
                    )
                )
            }

            MoveType.LEAP -> {
                activeSquare.contextMenuOptions.add(
                    ContextMenuOption(
                        "Leap",
                        { actionProvider.userActionSelected(MoveTypeSelected(MoveType.LEAP)) },
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
                        val square = snapshot.fieldSquares[loc]
                        snapshot.fieldSquares[loc] = square?.copy(
                            onSelected = {
                                actionProvider.userActionSelected(
                                    CompositeGameAction(
                                        listOf(
                                            MoveTypeSelected(MoveType.STANDARD),
                                            FieldSquareSelected(loc)
                                        )
                                    )
                                )
                            },
                            requiresRoll = requiresDodge || requiresRush
                        ) ?: error("Could not find square: $loc")
                    }
            }

            MoveType.STAND_UP -> {
                activeSquare.contextMenuOptions.add(
                    ContextMenuOption(
                        "Stand-Up",
                        { actionProvider.userActionSelected(MoveTypeSelected(MoveType.JUMP)) },
                    )
                )
            }
        }
    }
}
