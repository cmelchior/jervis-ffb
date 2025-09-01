package com.jervisffb.ui.game.view.field

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.unit.IntSize
import com.jervisffb.engine.model.PlayerSize
import com.jervisffb.fumbbl.net.model.FieldCoordinate
import kotlin.math.round
import kotlin.math.roundToInt

// This includes the border (if any)
data class FieldSizeData(
    val borderBrushSizePx: Int,
    val squareSize: IntSize, // Square size on the field
    val squaresPrRow: Int,
    val squaresPrColumn: Int
) {

    // Players might to take more or less space, but still use squareSize as where to find their "center"
    val normalPlayerSize = squareSize
    val largePlayerSize = IntSize(round(squareSize.width * 4/3f).toInt(), round(squareSize.height * 4/3f).toInt())

    val totalFieldWidthPx = squaresPrRow * squareSize.width + 2*borderBrushSizePx
    val totalFieldHeightPx = squaresPrColumn * squareSize.height + 2*borderBrushSizePx
    val fieldWidthPx = squaresPrRow * squareSize.width
    val fieldHeightPx = squaresPrColumn * squareSize.height

    /**
     * Returns the modifier needed to place a square of the given size at the given coordinate.
     *
     */
    fun calculateOffset(coordinate: FieldCoordinate, size: PlayerSize): Offset {
        val modifier = when (size) {
            PlayerSize.STANDARD -> 0f
            PlayerSize.BIG_GUY -> squareSize.width * 3/4f // Square size is 33% larger
            PlayerSize.GIANT -> 0f // Giants take up 4 spaces, but top-left corner is their internal coordinate
        }

        val x = (coordinate.x * squareSize.width) - modifier/2f + borderBrushSizePx
        val y = (coordinate.y * squareSize.height) - modifier/2f + borderBrushSizePx
        return Offset(x, y)
    }

    /**
     * Returns the size of the square needed to hold the given player size.
     */
    fun getPlayerSquareSize(size: PlayerSize): IntSize {
        return when (size) {
            PlayerSize.STANDARD -> squareSize
            // Big Guys are 33% larger than standard players
            PlayerSize.BIG_GUY -> IntSize((squareSize.width * 4/3f).roundToInt(), (squareSize.height * 4/3f).roundToInt())
            // Giants take up 4 spaces
            PlayerSize.GIANT -> IntSize(squareSize.width * 4, squareSize.height  * 4)
        }

    }
}
