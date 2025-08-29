package com.jervisffb.ui.game.view.field

import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.geometry.Offset
import com.jervisffb.engine.model.locations.FieldCoordinate

/**
 * Converts an offset with a Field to the [FieldCoordinate] the event happen on.
 * The border outside the field is considered to be outside the field.
 */
fun Offset.toFieldSquare(fieldSizeData: FieldSizeData): FieldCoordinate? {
    // TODO For some reason the Y-offset is off by 5 pixels (on my Macbook Pro). Need to investigate this
    //  but for now, just account for it
    val platformX = this.x
    val platformY = this.y - 5
    val border = fieldSizeData.borderBrushSizePx

    // If we are inside the border area, do not map to any field squares
    if (platformX <= border) return null
    if (platformY <= border) return null
    if (platformX >= fieldSizeData.totalFieldWidthPx - border) return null
    if (platformY >= fieldSizeData.totalFieldHeightPx - border) return null

    // Map offset to the correct field square
    val adjustedX = (platformX - border)
    val adjustedY = (platformY - border)
    val squareX = (adjustedX / fieldSizeData.squareSize.width).toInt()
    val squareY = (adjustedY / fieldSizeData.squareSize.height).toInt()
    return FieldCoordinate(squareX, squareY)
}

/**
 * Helper function making it easier to attach pointer events that are aware of the Field.
 * See [Field] and [LocalFieldData] for more details.
 */
fun Modifier.jervisPointerEvent(
    event: FieldPointerEventType,
    coordinate: FieldCoordinate,
    function: () -> Unit
): Modifier = composed {
    val ctx = LocalFieldData.current
    val func by rememberUpdatedState(function)
    val flow= remember(ctx, event, coordinate) {
        when (event) {
            FieldPointerEventType.EnterSquare -> ctx.pointerBus.enterSquare(coordinate)
            FieldPointerEventType.ExitSquare -> ctx.pointerBus.exitSquare(coordinate)
            FieldPointerEventType.ClickSquare -> ctx.pointerBus.clickSquare(coordinate)
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
 * Helper function making it easier to attach pointer events that are aware of the Field.
 * See [Field] and [LocalFieldData] for more details.
 */
fun Modifier.fieldHoverable(
    coordinate: FieldCoordinate,
    function: (hover: Boolean) -> Unit
): Modifier = composed {
    val ctx = LocalFieldData.current
    val flow = ctx.pointerBus.hoverSquare(coordinate)
    val value: Boolean by flow.collectAsState(false)
    function(value)
    this
}

