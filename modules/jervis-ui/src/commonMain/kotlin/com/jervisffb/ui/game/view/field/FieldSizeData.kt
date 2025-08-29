package com.jervisffb.ui.game.view.field

import androidx.compose.ui.unit.IntSize

// This includes the border (if any)
data class FieldSizeData(
    val borderBrushSizePx: Int,
    val squareSize: IntSize,
    val squaresPrRow: Int,
    val squaresPrColumn: Int
) {
    val totalFieldWidthPx = squaresPrRow * squareSize.width + 2*borderBrushSizePx
    val totalFieldHeightPx = squaresPrColumn * squareSize.height + 2*borderBrushSizePx
    val fieldWidthPx = squaresPrRow * squareSize.width
    val fieldHeightPx = squaresPrColumn * squareSize.height
}
