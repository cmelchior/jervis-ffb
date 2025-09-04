package com.jervisffb.ui.game.viewmodel

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import com.jervisffb.ui.game.dialogs.ActionWheelInputDialog
import com.jervisffb.ui.game.view.JervisTheme
import com.jervisffb.ui.utils.DummyLayoutCoordinates
import com.jervisffb.utils.jervisLogger
import kotlin.math.roundToInt

data class ActionWheelPlacementData(
    val showTip: Boolean,
    val tipRotationDegree: Float,
    val offset: IntOffset,
)

enum class TipPosition {
    CENTER,
    TOP,
    BOTTOM,
    LEFT,
    RIGHT,
    TOP_LEFT,
    TOP_RIGHT,
    BOTTOM_LEFT,
    BOTTOM_RIGHT,
}

/**
 * Helper class making it easier to track the size and position of the field.
 * We use this to position dialogs in relation to the field (normally in the center).
 */
data class FieldViewData(
    val screenSize: Size, // Size of the main game window
    val fieldSizePx: IntSize, // Size of the field in pixels. This includes the border.
    val fieldOffset: IntOffset, // Offset of the field in the main game window
    val squaresWidth: Int,
    val squaresHeight: Int,
) {

    companion object {
        val LOG = jervisLogger()
    }

    fun calculateActionWheelPlacement(dialog: ActionWheelInputDialog, fieldVm: FieldViewModel, wheelSizePx: Float, ringSizePx: Float): ActionWheelPlacementData {
        val squareSizePx = fieldSizePx.width/squaresWidth.toFloat()
        val ballLocation = dialog.viewModel.center
        val ballLocationOffsets = fieldVm.squareOffsets[ballLocation] ?: DummyLayoutCoordinates
        val offset = ballLocationOffsets.localToWindow(Offset.Zero)

        // Calculate 9 sections for the placement of the action wheel:
        val tipPos = chooseTipPosition(
            screenSize = JervisTheme.windowSizePx,
            fieldOffset = offset,
            squareSizePx = squareSizePx,
            focus = offset - Offset(this.fieldOffset.x.toFloat(), this.fieldOffset.y.toFloat()) + Offset(squareSizePx/2f, squareSizePx/2f),
            wheelRadius = (wheelSizePx - (wheelSizePx - ringSizePx)*0.75f)/2f,
            fieldSize = fieldSizePx,
        )

        return when (tipPos) {
            TipPosition.CENTER -> {
                ActionWheelPlacementData(
                    showTip = false,
                    tipRotationDegree = 0f,
                    offset = IntOffset(
                        x = (offset.x + ballLocationOffsets.size.width/2f - wheelSizePx/2f).roundToInt(),
                        y = (offset.y + ballLocationOffsets.size.height/2f - wheelSizePx/2f).roundToInt(),
                    )
                )
            }
            TipPosition.TOP -> {
                ActionWheelPlacementData(
                    showTip = true,
                    tipRotationDegree = 225f,
                    offset = IntOffset(
                        x = (offset.x + ballLocationOffsets.size.width/2f - wheelSizePx/2f).roundToInt(),
                        y = (offset.y + ballLocationOffsets.size.height - wheelSizePx - (squareSizePx*0.75f)).roundToInt(),
                    )
                )
            }
            TipPosition.BOTTOM -> {
                ActionWheelPlacementData(
                    showTip = true,
                    tipRotationDegree = 45f,
                    offset = IntOffset(
                        x = (offset.x + ballLocationOffsets.size.width/2f - wheelSizePx/2f).roundToInt(),
                        y = (offset.y + ballLocationOffsets.size.height - (squareSizePx/4)).roundToInt(),
                    )
                )
            }
            TipPosition.LEFT -> {
                ActionWheelPlacementData(
                    showTip = true,
                    tipRotationDegree = 135f,
                    offset = IntOffset(
                        x = (offset.x + ballLocationOffsets.size.width - wheelSizePx - squareSizePx*0.75).roundToInt(),
                        y = (offset.y + ballLocationOffsets.size.height - wheelSizePx/2f - squareSizePx/2f).roundToInt(),
                    )
                )
            }
            TipPosition.RIGHT -> {
                ActionWheelPlacementData(
                    showTip = true,
                    tipRotationDegree = 315f,
                    offset = IntOffset(
                        x = (offset.x + ballLocationOffsets.size.width - squareSizePx*0.25).roundToInt(),
                        y = (offset.y + ballLocationOffsets.size.height - wheelSizePx/2f - squareSizePx/2f).roundToInt(),
                    )
                )
            }
            TipPosition.TOP_LEFT -> {
                ActionWheelPlacementData(
                    showTip = true,
                    tipRotationDegree = 180f,
                    offset = IntOffset(
                        x = (offset.x + ballLocationOffsets.size.width/2f - wheelSizePx + (wheelSizePx - ringSizePx - squareSizePx/2)/2f).roundToInt(),
                        y = (offset.y + ballLocationOffsets.size.height/2f - wheelSizePx + (wheelSizePx - ringSizePx - squareSizePx/2)/2f).roundToInt(),
                    )
                )
            }
            TipPosition.TOP_RIGHT -> {
                ActionWheelPlacementData(
                    showTip = true,
                    tipRotationDegree = 270f,
                    offset = IntOffset(
                        x = (offset.x + ballLocationOffsets.size.width/2f - (wheelSizePx - ringSizePx - squareSizePx/2)/2f).roundToInt(),
                        y = (offset.y + ballLocationOffsets.size.height/2f - wheelSizePx + (wheelSizePx - ringSizePx - squareSizePx/2)/2f).roundToInt(),
                    )
                )
            }
            TipPosition.BOTTOM_LEFT -> {
                ActionWheelPlacementData(
                    tipRotationDegree = 90f,
                    showTip = true,
                    offset = IntOffset(
                        x = (offset.x + ballLocationOffsets.size.width/2f - wheelSizePx + (wheelSizePx - ringSizePx - squareSizePx/2)/2f).roundToInt(),
                        y = (offset.y + ballLocationOffsets.size.height/2f - (wheelSizePx - ringSizePx - squareSizePx/2)/2f).roundToInt(),
                    )
                )
            }
            TipPosition.BOTTOM_RIGHT -> {
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
        screenSize: Size,
        fieldOffset: Offset,
        squareSizePx: Float,
        focus: Offset, // Center of square in focus. Offset is from top-left corner of the field.
        wheelRadius: Float,
        fieldSize: IntSize,
    ): TipPosition {

        // With the new game UI, we have some extra space around the field
        // For now, just use the 2*size of a square as a heuristic. It probably
        // needs to be further refined.
        val leftSpace = focus.x + squareSizePx*2
        val rightSpace = fieldSize.width - focus.x + squareSizePx*2
        val topSpace = focus.y + squareSizePx*2
        val bottomSpace = fieldSize.height - focus.y + squareSizePx*2

        val hasCenterRoom = leftSpace >= wheelRadius &&
            rightSpace >= wheelRadius &&
            topSpace >= wheelRadius &&
            bottomSpace >= wheelRadius

        if (hasCenterRoom) return TipPosition.CENTER

        val verticalCenter = (rightSpace >= wheelRadius && rightSpace >= wheelRadius)
        val horizontalCenter = (bottomSpace >= wheelRadius && topSpace >= wheelRadius)

        val horizontal = when {
            rightSpace >= wheelRadius -> TipPosition.RIGHT
            leftSpace >= wheelRadius -> TipPosition.LEFT
            else -> null
        }

        val vertical = when {
            bottomSpace >= wheelRadius -> TipPosition.BOTTOM
            topSpace >= wheelRadius -> TipPosition.TOP
            else -> null
        }

        return when {
            horizontal != null && horizontalCenter -> horizontal
            vertical != null && verticalCenter -> vertical
            horizontal != null && vertical != null -> {
                when {
                    horizontal == TipPosition.RIGHT && vertical == TipPosition.BOTTOM -> TipPosition.BOTTOM_RIGHT
                    horizontal == TipPosition.RIGHT && vertical == TipPosition.TOP -> TipPosition.TOP_RIGHT
                    horizontal == TipPosition.LEFT && vertical == TipPosition.BOTTOM -> TipPosition.BOTTOM_LEFT
                    horizontal == TipPosition.LEFT && vertical == TipPosition.TOP -> TipPosition.TOP_LEFT
                    else -> TipPosition.CENTER // fallback
                }
            }
            horizontal != null -> horizontal
            vertical != null -> vertical
            // Something unexpected happened, so just hide the tip and hope for the best
            // It has only been possible to reproduce this on Web, so probably some interaction
            // with the browser is causing this. It requires futher investigation.
            else -> {
                LOG.w("Unexpected case: ($vertical, $horizontal). Fallback to CENTER")
                TipPosition.CENTER
            }
        }
    }
}
