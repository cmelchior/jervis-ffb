package com.jervisffb.ui.viewmodel

import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.layout.LayoutCoordinates
import com.jervisffb.engine.actions.CompositeGameAction
import com.jervisffb.engine.actions.FieldSquareSelected
import com.jervisffb.engine.actions.MoveType
import com.jervisffb.engine.actions.MoveTypeSelected
import com.jervisffb.engine.model.Player
import com.jervisffb.engine.model.locations.FieldCoordinate
import com.jervisffb.engine.utils.safeTryEmit
import com.jervisffb.ui.UiGameController
import com.jervisffb.ui.animations.JervisAnimation
import com.jervisffb.ui.model.UiFieldSquare
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map

enum class FieldDetails(val resource: String, val description: String) {
    NICE("icons/cached/pitches/default/nice.png", "Nice Weather"),
}

/**
 * This class collects all the information needed to render the field. This includes all information needed for
 * each single square on the field.
 */
class FieldViewModel(
    private val uiState: UiGameController,
    private val hoverPlayerChannel: MutableSharedFlow<Player?>,
) {
    val rules = uiState.rules
    val game = uiState.state
    val aspectRatio: Float = 782f / 452f
    val width = rules.fieldWidth
    val height = rules.fieldHeight

    private val field = MutableStateFlow(FieldDetails.NICE)
    private val _highlights = MutableStateFlow<FieldCoordinate?>(null)

    // Track offsets of field squares (so we can use them to animate things between squares)
    var fieldOffset: LayoutCoordinates? = null
    val offsets: MutableMap<FieldCoordinate, LayoutCoordinates> = mutableMapOf()

    fun field(): StateFlow<FieldDetails> = field

    fun observeAnimation(): Flow<Pair<UiGameController, JervisAnimation>?> {
        return uiState.animationFlow.map { if (it != null) Pair(uiState, it) else null }
    }

    fun highlights(): StateFlow<FieldCoordinate?> = _highlights

    fun hoverOver(square: FieldCoordinate) {
        game.field[square].player.let { player: Player? ->
            hoverPlayerChannel.safeTryEmit(player)
        }
        _highlights.value = square
    }

    fun exitHover() {
        _highlights.value = null
    }

    fun observeField(): Flow<Map<FieldCoordinate, UiFieldSquare>> {
        return combine(_highlights, uiState.uiStateFlow) { mouseEnter, uiSnapshot ->
            // If a highlighted square exists, we are going to calculate the shortest path to that
            // square and annotate the path towards it as well. These decorations take precedence
            // over already existing move decorations.
            val activePlayer: Player? = uiSnapshot.game.activePlayer

            // Clear all existing highlight data.
            uiSnapshot.clearHoverData()

            uiSnapshot.pathFinder?.let { pathFinder ->
                if (
                    activePlayer != null &&
                    mouseEnter != null &&
                    activePlayer.coordinates != mouseEnter &&
                    activePlayer.movesLeft > 0 &&
                    rules.calculateMarks(game, activePlayer.team, activePlayer.coordinates) <= 0
                ) {
                    val path: List<FieldCoordinate> = pathFinder.getClosestPathTo(mouseEnter, activePlayer.movesLeft)

                    // Create the action triggered if clicking the mouse-over field.
                    val action = {
                        // If a path consists of only 1 step, always execute it, since
                        // we assume the user wants to execute it.
                        val selectedSquares = path.map {
                            CompositeGameAction(
                                listOf(MoveTypeSelected(MoveType.STANDARD), FieldSquareSelected(it))
                            )
                        }
                        if (selectedSquares.size == 1) {
                            uiState.userSelectedAction(selectedSquares.first())
                        } else {
                            uiState.userSelectedMultipleActions(selectedSquares)
                        }
                    }

                    // Annotate all fields + add action on target square
                    val currentMovesLeft = activePlayer.move - activePlayer.movesLeft
                    path.forEachIndexed { index, pathSquare ->
                        val currentSquareData = uiSnapshot.fieldSquares[pathSquare]!!

                        val shownMoveValue = when(index) {
                            0 -> currentMovesLeft + index + 1
                            else -> currentMovesLeft + index + 1
                        }

                        uiSnapshot.fieldSquares[pathSquare] = currentSquareData.copy(
                            futureMoveValue = shownMoveValue,
                            hoverAction = if (currentSquareData.model.coordinates == mouseEnter) action else null
                        )
                    }
                }
            }

            // Finally, return the potentially modified squares
            uiSnapshot.fieldSquares.toMap()
        }
    }

    fun finishAnimation() {
        uiState.notifyAnimationDone()
    }

    fun updateOffset(coordinate: FieldCoordinate, layoutCoords: LayoutCoordinates) {
        offsets[coordinate] = layoutCoords
    }

    fun updateFieldOffSet(layoutCoords: LayoutCoordinates) {
        fieldOffset = layoutCoords
    }
}
