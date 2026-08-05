package com.jervisffb.ui.game.view.pitch

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntSize
import com.jervisffb.engine.model.PlayerSize
import com.jervisffb.fumbbl.net.model.FieldCoordinate
import kotlin.math.round
import kotlin.math.roundToInt

/**
 * Works out how big a pitch will be when drawn into [availableWidth].
 *
 * To preserve the pixelated style that FUMBBL assets have, while avoiding too
 * many rendering artifacts, squares are whole pixels. This means the final
 * total width is normally a little narrower than the space offered. Anything
 * that needs to match the pitch exactly, such as a Challenge placeholder image
 * shown while a game loads, has to go through here rather than approximating
 * it.
 */
fun calculatePitchSizeData(
    availableWidth: Dp,
    density: Density,
    pitchWidth: Int,
    pitchHeight: Int,
    borderBrushSize: Dp,
    drawPitchMarkers: Boolean,
): PitchSizeData {
    // If we need to render pitch markers, we add padding for it, so we can
    // draw the borders without them interacting with pitch squares or the end zone.
    val borderBrushPx = if (drawPitchMarkers) with(density) { borderBrushSize.toPx().toInt() } else 0
    val maxWidthPx = with(density) { availableWidth.toPx() }
    val squareSize = ((maxWidthPx - borderBrushPx * 2) / pitchWidth).toInt() // Must be smaller than maxWidth
    return PitchSizeData(
        borderBrushSizePx = borderBrushPx,
        squareSize = IntSize(squareSize, squareSize),
        squaresPrRow = pitchWidth,
        squaresPrColumn = pitchHeight,
    )
}

// This includes the border (if any)
data class PitchSizeData(
    val borderBrushSizePx: Int,
    val squareSize: IntSize, // Square size on the pitch
    val squaresPrRow: Int, // "model" squares pr. row
    val squaresPrColumn: Int // "model" squares pr. column
) {

    // Players might to take more or less space, but still use squareSize as where to find their "center"
    val normalPlayerSize = squareSize
    val largePlayerSize = IntSize(round(squareSize.width * 4/3f).toInt(), round(squareSize.height * 4/3f).toInt())

    val totalPitchWidthPx = squaresPrRow * squareSize.width + 2*borderBrushSizePx
    val totalPitchHeightPx = squaresPrColumn * squareSize.height + 2*borderBrushSizePx
    val fieldWidthPx = squaresPrRow * squareSize.width
    val fieldHeightPx = squaresPrColumn * squareSize.height

    /**
     * Returns the modifier needed to place a square of the given size at the given coordinate.
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
