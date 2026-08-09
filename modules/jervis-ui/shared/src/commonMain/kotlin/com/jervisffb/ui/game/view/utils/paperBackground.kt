package com.jervisffb.ui.game.view.utils

import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.ShaderBrush
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.asSkiaPath
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.skiaCanvas
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.unit.dp
import com.jervisffb.ui.game.view.JervisTheme
import com.jervisffb.ui.menu.intro.createGrayscaleNoiseShader
import com.jervisffb.ui.utils.applyIf
import org.jetbrains.skia.FilterBlurMode
import org.jetbrains.skia.MaskFilter
import org.jetbrains.skia.PaintMode
import org.jetbrains.skia.impl.use
import org.jetbrains.skia.Paint as SkiaPaint

/**
 * Draws a shadow for [path]. Call this before drawing the path itself so the shadow
 * remains visible only outside the foreground shape.
 */
fun DrawScope.drawPathDropShadow(
    path: Path,
    color: Color,
    offset: Offset,
    blurRadius: Float,
) {
    require(blurRadius >= 0f) { "blurRadius cannot be negative" }
    if (color.alpha <= 0f) return

    SkiaPaint().use { paint ->
        paint.color = color.toArgb()
        paint.mode = PaintMode.FILL
        paint.isAntiAlias = blurRadius > 0f

        val canvas = drawContext.canvas.skiaCanvas
        val drawShadow = {
            canvas.save()
            try {
                canvas.translate(offset.x, offset.y)
                canvas.drawPath(path.asSkiaPath(), paint)
            } finally {
                canvas.restore()
            }
        }

        if (blurRadius > 0f) {
            MaskFilter.makeBlur(FilterBlurMode.NORMAL, blurRadius, respectCTM = false).use { blurFilter ->
                paint.maskFilter = blurFilter
                drawShadow()
            }
        } else {
            drawShadow()
        }
    }
}

/**
 * Add noise to a background color so it mimics a paper-like texture.
 */
fun Modifier.paperBackground(
    color: Color = JervisTheme.rulebookPaper,
    shape: Shape? = RectangleShape
): Modifier {
    val paperShader = createGrayscaleNoiseShader(tileSize = 4)
    return this
        .applyIf(shape != null) { clip(shape!!) }
        .drawBehind {
            // Add desired background color
            drawRect(color = color, size = size)
            // Add semi-transparent noise on top
            drawRect(
                size = size,
                brush = ShaderBrush(paperShader),
                alpha = 0.3f,
            )
            // Re-add background color to make the noise blend more into the background
            drawRect(color = color.copy(alpha = 0.5f), size = size)
        }
}

fun Modifier.stoneBackground(
    shape: Shape? = RectangleShape
): Modifier {
    val color = Color(0x000000).copy(alpha = 0.8f)
    val paperShader = createGrayscaleNoiseShader()
    return this
        .applyIf(shape != null) { clip(shape!!) }
        .drawBehind {
            // Add desired background color
            drawRect(color = color, size = size)
            // Add semi-transparent noise on top
            drawRect(
                size = size,
                brush = ShaderBrush(paperShader),
                alpha = 0.5f,
            )
            // Re-add background color to make the noise blend more into the background
            drawRect(color = color.copy(alpha = 0.5f), size = size)
        }
}

/**
 * Background use for the Blue sidebar on menu screens.
 */
fun Modifier.paperBackgroundWithLine(color: Color): Modifier {
    val paperShader = createGrayscaleNoiseShader()
    return this.drawBehind {

        // Create the path we want to outline
        val path = Path().apply {
            moveTo(0f, 0f)
            lineTo(size.width, 0f)
            lineTo(size.width, size.height - 25.dp.toPx())
            lineTo(0f, size.height)
            close()
        }

        drawPath(path = path, color = color)
        drawPath(
            path = path,
            brush = ShaderBrush(paperShader),
            alpha = 0.3f,
        )
        // Re-add background color to make the noise blend more into the background
        drawPath(path = path, color = color.copy(alpha = 0.5f))
    }
}
