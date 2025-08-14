package com.jervisffb.ui.game.view.utils

import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.ShaderBrush
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.dp
import com.jervisffb.ui.game.view.JervisTheme
import com.jervisffb.ui.menu.intro.createGrayscaleNoiseShader
import com.jervisffb.ui.utils.applyIf

/**
 * Add noise to a background color so it mimics a paper-like texture.
 */
fun Modifier.paperBackground(color: Color = JervisTheme.rulebookPaper, shape: Shape? = RectangleShape): Modifier {
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
                alpha = 0.3f,
            )
            // Re-add background color to make the noise blend more into the background
            drawRect(color = color.copy(alpha = 0.5f), size = size)
        }
}

fun Modifier.stoneBackground(): Modifier {
    val color = Color(0x000000)
    val paperShader = createGrayscaleNoiseShader()
    return this.drawBehind {
        // Add desired background color
        drawRect(color = color, size = size)
        // Add semi-transparent noise on top
        drawRect(
            size = size,
            brush = ShaderBrush(paperShader),
            alpha = 0.8f,
        )
        // Re-add background color to make the noise blend more into the background
        drawRect(color = color.copy(alpha = 0.5f), size = size)
        drawRect(color = color.copy(alpha = 0.2f), size = size)
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
