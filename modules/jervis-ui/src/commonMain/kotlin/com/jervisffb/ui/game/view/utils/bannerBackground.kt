package com.jervisffb.ui.game.view.utils

import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import com.jervisffb.ui.game.view.JervisTheme

/**
 * Background that creates a Banner-like effect. The banner ends in a point,
 * and it has a colored border.
 */
fun Modifier.bannerBackground(bannerColor: Color = JervisTheme.rulebookRed): Modifier {
    return this.drawBehind {
        val width = size.width
        val height = size.height
        val bottomPadding = 40.dp.toPx()
        val topHeight = width / 2.5f
        val borderWidth = 4.dp

        val path = Path().apply {
            moveTo(0f, 0f)
            lineTo(0f, height - topHeight - bottomPadding)
            lineTo(width / 2, height - bottomPadding)
            lineTo(width, height - topHeight - bottomPadding)
            lineTo(width, 0f)
            close()
        }

        // Draw the banner
        drawPath(path, bannerColor)

        // Draw the border
        val borderPath = Path().apply {
            val offset = 8.dp.toPx()
            moveTo(offset, 0f)
            // (borderWidth / 2).toPx() is make it look nicer when the border "breaks", if it breaks
            // at the exact same y value as the background, the line towards the center will look off.
            lineTo(offset, height - topHeight - bottomPadding - (borderWidth).toPx())
            lineTo(width / 2, height - bottomPadding - offset - (borderWidth / 2f).toPx())
            lineTo(width - offset, height - topHeight - bottomPadding - (borderWidth).toPx())
            lineTo(width - offset, 0f)
        }

        val stroke = Stroke(
            width = borderWidth.toPx(),
            join = StrokeJoin.Miter
        )
        drawPath(borderPath, JervisTheme.white, style = stroke)
    }
}
