package com.jervisffb.ui.game.view.pitch

import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.geometry.Offset
import com.jervisffb.engine.model.locations.PitchCoordinate

/**
 * Converts an offset with a Pitch to the [PitchCoordinate] the event happen on.
 * The border outside the pitch is considered to be outside the pitch.
 */
fun Offset.toPitchSquare(pitchSizeData: PitchSizeData): PitchCoordinate? {
    // TODO For some reason the Y-offset is off by 5 pixels (on my Macbook Pro). Need to investigate this
    //  but for now, just account for it
    val platformX = this.x
    val platformY = this.y - 5
    val border = pitchSizeData.borderBrushSizePx

    // If we are inside the border area, do not map to any field squares
    if (platformX <= border) return null
    if (platformY <= border) return null
    if (platformX >= pitchSizeData.totalPitchWidthPx - border) return null
    if (platformY >= pitchSizeData.totalPitchHeightPx - border) return null

    // Map offset to the correct field square
    val adjustedX = (platformX - border)
    val adjustedY = (platformY - border)
    val squareX = (adjustedX / pitchSizeData.squareSize.width).toInt()
    val squareY = (adjustedY / pitchSizeData.squareSize.height).toInt()
    return PitchCoordinate(squareX, squareY)
}

/**
 * Helper function making it easier to attach pointer events that are aware of the Pitch.
 * See [Pitch] and [LocalPitchData] for more details.
 */
fun Modifier.jervisPointerEvent(
    event: SquarePointerEventType,
    coordinate: PitchCoordinate,
    function: () -> Unit
): Modifier = composed {
    val ctx = LocalPitchData.current
    val func by rememberUpdatedState(function)
    val flow= remember(ctx, event, coordinate) {
        when (event) {
            SquarePointerEventType.EnterSquare -> ctx.pointerBus.enterSquare(coordinate)
            SquarePointerEventType.ExitSquare -> ctx.pointerBus.exitSquare(coordinate)
            SquarePointerEventType.PrimaryClickSquare -> ctx.pointerBus.primaryClickSquare(coordinate)
            SquarePointerEventType.SecondaryClickSquare -> ctx.pointerBus.secondaryClickSquare(coordinate)
        }
    }
    LaunchedEffect(flow) {
        flow.collect { value ->
            if (value) func()
        }
    }
    this
}

/**
 * Helper function making it easier to attach pointer events that are aware of the Pitch.
 * See [Pitch] and [LocalPitchData] for more details.
 */
fun Modifier.pitchHoverable(
    coordinate: PitchCoordinate,
    function: (hover: Boolean) -> Unit
): Modifier = composed {
    val ctx = LocalPitchData.current
    val flow = ctx.pointerBus.hoverSquare(coordinate)
    val value: Boolean by flow.collectAsState(false)
    function(value)
    this
}

