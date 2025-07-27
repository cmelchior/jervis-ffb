package com.jervisffb.ui.game.viewmodel

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.layout.LayoutCoordinates
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import com.jervisffb.engine.actions.CompositeGameAction
import com.jervisffb.engine.actions.FieldSquareSelected
import com.jervisffb.engine.actions.MoveType
import com.jervisffb.engine.actions.MoveTypeSelected
import com.jervisffb.engine.model.Player
import com.jervisffb.engine.model.PlayerState
import com.jervisffb.engine.model.locations.FieldCoordinate
import com.jervisffb.engine.rules.bb2020.tables.Weather
import com.jervisffb.engine.utils.safeTryEmit
import com.jervisffb.ui.game.UiGameController
import com.jervisffb.ui.game.animations.JervisAnimation
import com.jervisffb.ui.game.dialogs.ActionWheelInputDialog
import com.jervisffb.ui.game.model.UiFieldSquare
import com.jervisffb.ui.game.state.QueuedActionsResult
import com.jervisffb.ui.menu.GameScreenModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlin.math.roundToInt

enum class FieldDetails(
    val resource: String,
    val description: String,
    // Draw lines, dots and end zone markers (if false, it is assumed they are part of the image)
    val drawFieldMarkers: Boolean
) {
    // Use a custom field for now as we need something that can be used for both Standard and BB7
    // It would probably look better with custom fields, but this should be fine for now.
    HEAT("jervis/pitch/default/heat.png", "Sweltering Heat", true),
    SUNNY("jervis/pitch/default/sunny.png", "Very Sunny", true),
    NICE("jervis/pitch/default/nice.png", "Perfect Conditions", true),
    RAIN("jervis/pitch/default/rain.png", "Pouring Rain", true),
    BLIZZARD("jervis/pitch/default/blizzard.png", "Blizzard", true),
}

/**
 * This class collects all the information needed to render the field. This includes all information needed for
 * each single square on the field.
 */
class FieldViewModel(
    private  val screenModel: GameScreenModel,
    private val uiState: UiGameController,
    private val hoverPlayerChannel: MutableSharedFlow<Player?>,
) {
    val rules = uiState.rules
    val game = uiState.state
    val width = rules.fieldWidth
    val height = rules.fieldHeight

    val field = uiState.uiStateFlow.map { uiSnapshot ->
        val weather = uiSnapshot.game.weather
        when (weather) {
            Weather.SWELTERING_HEAT -> FieldDetails.HEAT
            Weather.VERY_SUNNY -> FieldDetails.SUNNY
            Weather.PERFECT_CONDITIONS -> FieldDetails.NICE
            Weather.POURING_RAIN -> FieldDetails.RAIN
            Weather.BLIZZARD -> FieldDetails.BLIZZARD
        }
    }

    private val _highlights = MutableStateFlow<FieldCoordinate?>(null)

    // Track offsets of field squares (so we can use them to animate things between squares)
    var fieldOffset: LayoutCoordinates? = null
    val offsets: MutableMap<FieldCoordinate, LayoutCoordinates> = mutableMapOf()

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
            val requiresStandingUp = (activePlayer?.state == PlayerState.PRONE)

            // Clear all existing highlight data.
            uiSnapshot.clearHoverData()

            // Use path finder
            uiSnapshot.pathFinder?.let { pathFinder ->
                if (showPathFinder(activePlayer, mouseEnter)) {
                    val standingUpPenalty = if (requiresStandingUp) rules.moveRequiredForStandingUp else 0
                    // TODO This logic fails when Undo'ing. Figure out why.
                    val path: List<FieldCoordinate> = pathFinder.getClosestPathTo(mouseEnter!!, (activePlayer!!.movesLeft - standingUpPenalty))

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
                    path.forEachIndexed { index, pathSquare ->
                        val currentSquareData = uiSnapshot.fieldSquares[pathSquare]!!

                        val shownMoveValue = when (index) {
                            0 -> currentMovesLeft + index + 1 + standingUpModifier
                            else -> currentMovesLeft + index + 1 + standingUpModifier
                        }

                        uiSnapshot.fieldSquares[pathSquare]?.apply {
                            futureMoveValue = shownMoveValue
                            hoverAction = if (currentSquareData.model.coordinates == mouseEnter) action else null
                        }
                    }
                }
            }

            // Finally, return the potentially modified squares
            uiSnapshot.fieldSquares.toMap()
        }
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
        offsets[coordinate] = layoutCoords
    }

    fun updateFieldOffSet(fieldLayoutCoordinates: LayoutCoordinates) {
        fieldOffset = fieldLayoutCoordinates
        screenModel.updateFieldViewData(fieldLayoutCoordinates)
    }
}

data class ActionWheelPlacementData(
    val showTip: Boolean,
    val tipRotationDegree: Float,
    val offset: IntOffset,
)

enum class TipPosition {
    Center, Top, Bottom, Left, Right,
    TopLeft, TopRight, BottomLeft, BottomRight
}

/**
 * Helper class making it easier to track the size and position of the field.
 * We use this to position dialogs in relation to the field (normally in the center).
 */
data class FieldViewData(
    val size: IntSize,
    val offset: IntOffset,
    val squaresWidth: Int,
    val squaresHeight: Int,
) {

    fun calculateActionWheelPlacement(dialog: ActionWheelInputDialog, fieldVm: FieldViewModel, wheelSizePx: Float, ringSizePx: Float): ActionWheelPlacementData {
        val squareSizePx = size.width/squaresWidth.toFloat()
        val ballLocation = dialog.viewModel.center
        val ballLocationOffsets = fieldVm.offsets[ballLocation]!!
        val offset = ballLocationOffsets.localToWindow(Offset.Zero)

        // Calculate 9 sections for the placement of the action wheel:
        val tipPos = chooseTipPosition(
            focus = offset - Offset(this.offset.x.toFloat(), this.offset.y.toFloat()) + Offset(squareSizePx/2f, squareSizePx/2f),
            wheelRadius = (wheelSizePx - (wheelSizePx - ringSizePx)*0.75f)/2f,
            fieldSize = size,
        )

        return when (tipPos) {
            TipPosition.Center -> {
                ActionWheelPlacementData(
                    showTip = false,
                    tipRotationDegree = 0f,
                    offset = IntOffset(
                        x = (offset.x + ballLocationOffsets.size.width/2f - wheelSizePx/2f).roundToInt(),
                        y = (offset.y + ballLocationOffsets.size.height/2f - wheelSizePx/2f).roundToInt(),
                    )
                )
            }
            TipPosition.Top -> {
                ActionWheelPlacementData(
                    showTip = true,
                    tipRotationDegree = 225f,
                    offset = IntOffset(
                        x = (offset.x + ballLocationOffsets.size.width/2f - wheelSizePx/2f).roundToInt(),
                        y = (offset.y + ballLocationOffsets.size.height - wheelSizePx - (squareSizePx*0.75f)).roundToInt(),
                    )
                )
            }
            TipPosition.Bottom -> {
                ActionWheelPlacementData(
                    showTip = true,
                    tipRotationDegree = 45f,
                    offset = IntOffset(
                        x = (offset.x + ballLocationOffsets.size.width/2f - wheelSizePx/2f).roundToInt(),
                        y = (offset.y + ballLocationOffsets.size.height - (squareSizePx/4)).roundToInt(),
                    )
                )
            }
            TipPosition.Left -> {
                ActionWheelPlacementData(
                    showTip = true,
                    tipRotationDegree = 135f,
                    offset = IntOffset(
                        x = (offset.x + ballLocationOffsets.size.width - wheelSizePx - squareSizePx*0.75).roundToInt(),
                        y = (offset.y + ballLocationOffsets.size.height - wheelSizePx/2f - squareSizePx/2f).roundToInt(),
                    )
                )
            }
            TipPosition.Right -> {
                ActionWheelPlacementData(
                    showTip = true,
                    tipRotationDegree = 315f,
                    offset = IntOffset(
                        x = (offset.x + ballLocationOffsets.size.width - squareSizePx*0.25).roundToInt(),
                        y = (offset.y + ballLocationOffsets.size.height - wheelSizePx/2f - squareSizePx/2f).roundToInt(),
                    )
                )
            }
            TipPosition.TopLeft -> {
                ActionWheelPlacementData(
                    showTip = true,
                    tipRotationDegree = 180f,
                    offset = IntOffset(
                        x = (offset.x + ballLocationOffsets.size.width/2f - wheelSizePx + (wheelSizePx - ringSizePx - squareSizePx/2)/2f).roundToInt(),
                        y = (offset.y + ballLocationOffsets.size.height/2f - wheelSizePx + (wheelSizePx - ringSizePx - squareSizePx/2)/2f).roundToInt(),
                    )
                )
            }
            TipPosition.TopRight -> {
                ActionWheelPlacementData(
                    showTip = true,
                    tipRotationDegree = 270f,
                    offset = IntOffset(
                        x = (offset.x + ballLocationOffsets.size.width/2f - (wheelSizePx - ringSizePx - squareSizePx/2)/2f).roundToInt(),
                        y = (offset.y + ballLocationOffsets.size.height/2f - wheelSizePx + (wheelSizePx - ringSizePx - squareSizePx/2)/2f).roundToInt(),
                    )
                )
            }
            TipPosition.BottomLeft -> {
                ActionWheelPlacementData(
                    tipRotationDegree = 90f,
                    showTip = true,
                    offset = IntOffset(
                        x = (offset.x + ballLocationOffsets.size.width/2f - wheelSizePx + (wheelSizePx - ringSizePx - squareSizePx/2)/2f).roundToInt(),
                        y = (offset.y + ballLocationOffsets.size.height/2f - (wheelSizePx - ringSizePx - squareSizePx/2)/2f).roundToInt(),
                    )
                )
            }
            TipPosition.BottomRight -> {
                ActionWheelPlacementData(
                    showTip = true,
                    tipRotationDegree = 0f,
                    offset = IntOffset(
                        x = (offset.x + ballLocationOffsets.size.width/2f - (wheelSizePx - ringSizePx - squareSizePx/2)/2f).roundToInt(),
                        y = (offset.y + ballLocationOffsets.size.height/2f - (wheelSizePx - ringSizePx - squareSizePx/2)/2f).roundToInt(),
                    )
                )
            }
        }
    }

    fun chooseTipPosition(
        focus: Offset,
        wheelRadius: Float,
        fieldSize: IntSize,
    ): TipPosition {
        val leftSpace = focus.x
        val rightSpace = fieldSize.width - focus.x
        val topSpace = focus.y
        val bottomSpace = fieldSize.height - focus.y

        val hasCenterRoom = leftSpace >= wheelRadius &&
            rightSpace >= wheelRadius &&
            topSpace >= wheelRadius &&
            bottomSpace >= wheelRadius

        if (hasCenterRoom) return TipPosition.Center

        val verticalCenter = (rightSpace >= wheelRadius && rightSpace >= wheelRadius)
        val horizontalCenter = (bottomSpace >= wheelRadius && topSpace >= wheelRadius)

        val horizontal = when {
            rightSpace >= wheelRadius -> TipPosition.Right
            leftSpace >= wheelRadius -> TipPosition.Left
            else -> null
        }

        val vertical = when {
            bottomSpace >= wheelRadius -> TipPosition.Bottom
            topSpace >= wheelRadius -> TipPosition.Top
            else -> null
        }

        return when {
            horizontal != null && horizontalCenter -> horizontal
            vertical != null && verticalCenter -> vertical
            horizontal != null && vertical != null -> {
                when {
                    horizontal == TipPosition.Right && vertical == TipPosition.Bottom -> TipPosition.BottomRight
                    horizontal == TipPosition.Right && vertical == TipPosition.Top -> TipPosition.TopRight
                    horizontal == TipPosition.Left && vertical == TipPosition.Bottom -> TipPosition.BottomLeft
                    horizontal == TipPosition.Left && vertical == TipPosition.Top -> TipPosition.TopLeft
                    else -> TipPosition.Center // fallback
                }
            }
            else -> error("Unsupported case: ($vertical, $horizontal)")
        }
    }
}


