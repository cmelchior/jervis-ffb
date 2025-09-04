package com.jervisffb.ui.game.viewmodel

import androidx.compose.ui.layout.LayoutCoordinates
import com.jervisffb.engine.actions.CompositeGameAction
import com.jervisffb.engine.actions.FieldSquareSelected
import com.jervisffb.engine.actions.MoveType
import com.jervisffb.engine.actions.MoveTypeSelected
import com.jervisffb.engine.model.Player
import com.jervisffb.engine.model.PlayerState
import com.jervisffb.engine.model.locations.FieldCoordinate
import com.jervisffb.engine.utils.safeTryEmit
import com.jervisffb.ui.game.UiGameController
import com.jervisffb.ui.game.UiGameSnapshot
import com.jervisffb.ui.game.animations.JervisAnimation
import com.jervisffb.ui.game.dialogs.ActionWheelInputDialog
import com.jervisffb.ui.game.model.UiFieldPlayer
import com.jervisffb.ui.game.model.UiFieldSquare
import com.jervisffb.ui.game.state.QueuedActionsResult
import com.jervisffb.ui.menu.GameScreenModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map

/**
 * Expand player data with extra callbacks related to UI hover effects.
 * TODO I think this is only used for the Player Stat Card, so we can probably
 *  find a way to remove this.
 */
data class UiPlayerTransientData(
    val onHover: (() -> Unit)?,
    val onHoverExit: (() -> Unit)?,
)

/**
 * Information needed to render PathFinder information on the board.
 */
data class UiPathFinderData(
    val coordinate: FieldCoordinate,
    // Indicate the amount of move used to reach a potential target square.
    // This number will override `moveUsed` if present
    val futureMoveDistance: Int,
    // Action triggered if this square is selected.
    val hoverAction: () -> Unit,
)

/**
 * This class collects all the information needed to render the field. This includes all information needed for
 * each single square on the field.
 */
class FieldViewModel(
    private val screenModel: GameScreenModel,
    private val uiState: UiGameController,
    private val hoverPlayerChannel: MutableSharedFlow<Player?>,
) {
    val rules = uiState.rules
    val game = uiState.state
    val width = rules.fieldWidth
    val height = rules.fieldHeight
    val sharedFieldData = screenModel.sharedFieldData

    val fieldBackground = screenModel.fieldBackground

    private val _highlights = MutableStateFlow<FieldCoordinate?>(null)

    // Track offsets of field squares (so we can use them to animate things between squares)
    var fieldOffset: LayoutCoordinates? = null
    val squareOffsets: MutableMap<FieldCoordinate, LayoutCoordinates?> = mutableMapOf()

    fun observeAnimation(): Flow<Pair<UiGameController, JervisAnimation>?> {
        return uiState.animationFlow.map { if (it != null) Pair(uiState, it) else null }
    }

    fun highlights(): StateFlow<FieldCoordinate?> = _highlights

    fun triggerHoverEnter(square: FieldCoordinate) {
        game.field[square].player.let { player: Player? ->
            hoverPlayerChannel.safeTryEmit(player)
        }
        _highlights.value = square
    }

    fun triggerHoverExit() {
        _highlights.value = null
    }

    /**
     * This flow exposes path finder data on the field (if relevant). I.e., future
     * moves are written on each square of the field
     */
    fun observePathFinder(): Flow<Map<FieldCoordinate, UiPathFinderData>> {
        return combine(_highlights, uiState.uiStateFlow) { mouseEnter, uiSnapshot ->
            // If a highlighted square exists, we are going to calculate the shortest path to that
            // square and annotate the path towards it as well. These decorations take precedence
            // over already existing move decorations.
            val activePlayer: Player? = uiSnapshot.game.activePlayer
            val requiresStandingUp = (activePlayer?.state == PlayerState.PRONE)

            // Use path finder
            val pathList = uiSnapshot.pathFinder?.let { pathFinder ->
                if (showPathFinder(activePlayer, mouseEnter)) {
                    val standingUpPenalty = if (requiresStandingUp) rules.moveRequiredForStandingUp else 0
                    // TODO This logic fails when Undo'ing. Figure out why.
                    val path: List<FieldCoordinate> = uiSnapshot.pathFinder.getClosestPathTo(mouseEnter!!, (activePlayer!!.movesLeft - standingUpPenalty))

                    // Create the action triggered if clicking the mouse-over field.
                    val action = {
                        val actionProvider = (uiState.actionProvider)


                        fun getQueuedActionsForPath(): QueuedActionsResult {
                            val selectedSquares = path.map {
                                CompositeGameAction(
                                    listOf(MoveTypeSelected(MoveType.STANDARD), FieldSquareSelected(it))
                                )
                            }
                            return if (selectedSquares.size == 1) {
                                QueuedActionsResult(selectedSquares.first())
                            } else {
                                QueuedActionsResult(selectedSquares, true)
                            }
                        }

                        if (requiresStandingUp) {
                            // If the player is about to stand up, there is room for uncertainty
                            // in which game actions queue up, as the player might have to roll
                            // and reroll for it. In that case, we just stand up first, and queue
                            // up all move actions up until the player can actually move.
                            actionProvider.registerQueuedActionGenerator { controller ->
                                if (controller.getAvailableActions().contains(MoveType.STANDARD)) {
                                    getQueuedActionsForPath()
                                } else {
                                    null
                                }
                            }
                            // Trigger Stand-up
                            actionProvider.userActionSelected(MoveTypeSelected(MoveType.STAND_UP))
                        } else {
                            // Nothing should prevent the player from moving straight away, so
                            // just queue up all pathfinder data directly.
                            actionProvider.userMultipleActionsSelected(getQueuedActionsForPath().actions)
                        }
                    }

                    // Annotate all fields with "Move Used" amount + add action on the square
                    // that is currently being hovered over.
                    val standingUpModifier = if (requiresStandingUp) rules.moveRequiredForStandingUp else 0
                    val currentMovesLeft = activePlayer.move - activePlayer.movesLeft
                    path.mapIndexed { index, pathSquare ->
                        val currentSquareData = uiSnapshot.squares[pathSquare]!!

                        val shownMoveValue = when (index) {
                            0 -> currentMovesLeft + index + 1 + standingUpModifier
                            else -> currentMovesLeft + index + 1 + standingUpModifier
                        }

                        UiPathFinderData(
                            coordinate = pathSquare,
                            futureMoveDistance = shownMoveValue,
                            hoverAction = action
                        )
                    }
                } else {
                    emptyList()
                }
            }
            pathList?.associate { it.coordinate to it } ?: emptyMap()
        }
    }

    fun observeSnapshot(): Flow<UiGameSnapshot>  = uiState.uiStateFlow

    fun observeField(): Flow<Map<FieldCoordinate, Pair<UiFieldSquare, UiFieldPlayer?>>> {
        return combine(_highlights, uiState.uiStateFlow) { mouseEnter, uiSnapshot ->
            uiSnapshot.squares.map {
                it.key to Pair(it.value, uiSnapshot.players[it.value.player])
            }.toMap()
        }
    }

    fun observeActionWheel(): Flow<ActionWheelInputDialog?> {
        return uiState.uiStateFlow.map {
            if (it.dialogInput != null && it.dialogInput::class == ActionWheelInputDialog::class) {
                it.dialogInput as ActionWheelInputDialog
            } else {
                null
            }
        }.distinctUntilChanged()
    }

    private fun showPathFinder(
        activePlayer: Player?,
        mouseEnter: FieldCoordinate?
    ): Boolean = (
        activePlayer != null &&
            mouseEnter != null &&
            activePlayer.coordinates != mouseEnter &&
            activePlayer.movesLeft > 0 &&
            rules.calculateMarks(game, activePlayer.team, activePlayer.coordinates) <= 0
    )

    fun finishAnimation() {
        uiState.notifyAnimationDone()
    }

    fun updateOffset(coordinate: FieldCoordinate, layoutCoords: LayoutCoordinates) {
        squareOffsets[coordinate] = layoutCoords
    }

    fun updateFieldOffSet(fieldLayoutCoordinates: LayoutCoordinates) {
        fieldOffset = fieldLayoutCoordinates
        screenModel.updateFieldViewData(fieldLayoutCoordinates)
    }
}
