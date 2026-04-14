package com.jervisffb.ui.game.viewmodel

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import com.jervisffb.engine.model.locations.PitchCoordinate
import com.jervisffb.utils.jervisLogger
import kotlin.math.min
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
 * Helper class making it easier to track the size and position of the pitch.
 * We use this to position dialogs in relation to the pitch (normally in the center).
 */
data class PitchViewData(
    val screenSizePx: Size, // Size of the main game window in pixels. This does not include frame decor like the title bar on Mac.
    val pitchSizePx: IntSize, // Size of the pitch in pixels. This includes the border.
    val borderSizePx: Float,
    val pitchOffset: IntOffset, // Offset of the pitch in the main game window
    val columns: Int, // Number of columns in the pitch.
    val rows: Int,  // Number of rows in the pitch.
) {
    val squareWidth: Float = (pitchSizePx.width - 2*borderSizePx)/columns.toFloat()
    val squareHeight: Float = (pitchSizePx.height - 2*borderSizePx)/columns.toFloat()

    companion object {
        val LOG = jervisLogger()
    }

    fun calculateActionWheelPlacement(center: PitchCoordinate, pitchVm: PitchViewModel, wheelSizePx: Float, ringSizePx: Float): ActionWheelPlacementData {
        val squareSizePx = squareWidth
        // Offset according to root
        val ballLocationCoordinates = pitchVm.squareOffsets[center] ?: SquareLayoutCoordinates(
            coordinate = center,
            positionInRoot = Offset.Zero,
            boundsInRoot = Rect.Zero
        )
        val pitchCoordinates = pitchVm.pitchCoordinates
        val pitchOffset = pitchCoordinates.positionInRoot // Offset(borderSizePx, borderSizePx))
        val focus =  ballLocationCoordinates.positionInRoot - pitchOffset + Offset(squareSizePx/2f, squareSizePx/2f)

        // Calculate 9 sections for the placement of the action wheel:
        val tipPos = chooseTipPosition(
            screenSize = screenSizePx,
            pitchOffset = pitchOffset,
            squareSizePx = squareSizePx,
            focus = focus,
            // Factor accounts for button size on the action wheel, but it would be better
            // if it used the correct button size, rather than just guess.
            wheelRadius = (wheelSizePx - (wheelSizePx - ringSizePx)*0.45f)/2f,
            pitchSize = pitchSizePx,
        )

        val tipAdjustment = squareSizePx*0.25f // How much to adjust tip from center
        return when (tipPos) {
            TipPosition.CENTER -> {
                ActionWheelPlacementData(
                    showTip = false,
                    tipRotationDegree = 0f,
                    offset = IntOffset(
                        x = (focus.x - wheelSizePx/2f).roundToInt(),
                        y = (focus.y - wheelSizePx/2f).roundToInt(),
                    )
                )
            }
            TipPosition.TOP -> {
                ActionWheelPlacementData(
                    showTip = true,
                    tipRotationDegree = 225f,
                    offset = IntOffset(
                        x = (focus.x - wheelSizePx/2f).roundToInt(),
                        y = (focus.y - wheelSizePx - tipAdjustment).roundToInt(),
                    )
                )
            }
            TipPosition.BOTTOM -> {
                ActionWheelPlacementData(
                    showTip = true,
                    tipRotationDegree = 45f,
                    offset = IntOffset(
                        x = (focus.x - wheelSizePx/2f).roundToInt(),
                        y = (focus.y + tipAdjustment).roundToInt(),
                    )
                )
            }
            TipPosition.LEFT -> {
                ActionWheelPlacementData(
                    showTip = true,
                    tipRotationDegree = 135f,
                    offset = IntOffset(
                        x = (focus.x - wheelSizePx - tipAdjustment).roundToInt(),
                        y = (focus.y - wheelSizePx/2f).roundToInt(),
                    )
                )
            }
            TipPosition.RIGHT -> {
                ActionWheelPlacementData(
                    showTip = true,
                    tipRotationDegree = 315f,
                    offset = IntOffset(
                        x = (focus.x + tipAdjustment).roundToInt(),
                        y = (focus.y - wheelSizePx/2f).roundToInt(),
                    )
                )
            }
            TipPosition.TOP_LEFT -> {
                ActionWheelPlacementData(
                    showTip = true,
                    tipRotationDegree = 180f,
                    offset = IntOffset(
                        x = (focus.x - wheelSizePx + ((wheelSizePx - ringSizePx)/2f) - tipAdjustment).roundToInt(),
                        y = (focus.y - wheelSizePx + ((wheelSizePx - ringSizePx)/2f) - tipAdjustment).roundToInt(),
                    )
                )
            }
            TipPosition.TOP_RIGHT -> {
                ActionWheelPlacementData(
                    showTip = true,
                    tipRotationDegree = 270f,
                    offset = IntOffset(
                        x = (focus.x - ((wheelSizePx - ringSizePx)/2f) + tipAdjustment).roundToInt(),
                        y = (focus.y - wheelSizePx + ((wheelSizePx - ringSizePx)/2f) - tipAdjustment).roundToInt(),
                    )
                )
            }
            TipPosition.BOTTOM_LEFT -> {
                ActionWheelPlacementData(
                    tipRotationDegree = 90f,
                    showTip = true,
                    offset = IntOffset(
                        x = (focus.x - wheelSizePx + ((wheelSizePx - ringSizePx)/2f) - tipAdjustment).roundToInt(),
                        y = (focus.y - ((wheelSizePx - ringSizePx)/2f) + tipAdjustment).roundToInt(),
                    )
                )
            }
            TipPosition.BOTTOM_RIGHT -> {
                ActionWheelPlacementData(
                    showTip = true,
                    tipRotationDegree = 0f,
                    offset = IntOffset(
                        x = (focus.x - ((wheelSizePx - ringSizePx)/2f) + tipAdjustment).roundToInt(),
                        y = (focus.y - ((wheelSizePx - ringSizePx)/2f) + tipAdjustment).roundToInt(),
                    )
                )
            }
        }
    }

    fun chooseTipPosition(
        screenSize: Size,
        pitchOffset: Offset,
        squareSizePx: Float,
        focus: Offset, // Center of square in focus. Offset is from top-left corner of the pitch.
        wheelRadius: Float,
        pitchSize: IntSize,
    ): TipPosition {

        // With the new game UI, we have some extra space around the pitch
        // For now, just use the 2*size of a square as a heuristic. It probably
        // needs to be further refined.
        val leftSpace = focus.x + squareSizePx*2
        val rightSpace = pitchSize.width - focus.x + squareSizePx*2
        val topSpace = focus.y + squareSizePx*2
        val bottomSpace = min(screenSize.height - focus.y - pitchOffset.y, pitchSize.height - focus.y + squareSizePx*2)

        val hasCenterRoom = leftSpace >= wheelRadius &&
            rightSpace >= wheelRadius &&
            topSpace >= wheelRadius &&
            bottomSpace >= wheelRadius

        if (hasCenterRoom) return TipPosition.CENTER

        val verticalCenter = (leftSpace >= wheelRadius && rightSpace >= wheelRadius)
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
                when (horizontal) {
                    TipPosition.RIGHT if vertical == TipPosition.BOTTOM -> TipPosition.BOTTOM_RIGHT
                    TipPosition.RIGHT if vertical == TipPosition.TOP -> TipPosition.TOP_RIGHT
                    TipPosition.LEFT if vertical == TipPosition.BOTTOM -> TipPosition.BOTTOM_LEFT
                    TipPosition.LEFT if vertical == TipPosition.TOP -> TipPosition.TOP_LEFT
                    else -> TipPosition.CENTER
                    // fallback
                }
            }
            horizontal != null -> horizontal
            vertical != null -> vertical
            // Something unexpected happened, so just hide the tip and hope for the best
            // It has only been possible to reproduce this on Web, so probably some interaction
            // with the browser is causing this. It requires further investigation.
            else -> {
                LOG.w("Unexpected case: ($vertical, $horizontal). Fallback to CENTER")
                TipPosition.CENTER
            }
        }
    }
}
