package com.jervisffb.ui.menu

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shader
import androidx.compose.ui.graphics.ShaderBrush
import androidx.compose.ui.layout.ContentScale
import com.jervisffb.jervis_ui.generated.resources.Res
import com.jervisffb.jervis_ui.generated.resources.menu_background
import com.jervisffb.ui.game.view.JervisTheme
import org.jetbrains.compose.resources.imageResource
import org.jetbrains.skia.ColorFilter
import org.jetbrains.skia.ColorMatrix
import org.jetbrains.skia.ISize


@Composable
fun PaperBackground(modifier: Modifier = Modifier) {

    // Create Noise
    val shader = Shader.makeFractalNoise(
        baseFrequencyX = 0.1f, // Adjust for desired texture
        baseFrequencyY = 0.1f,
        numOctaves = 5,
        seed = 0f,
        tileSize = ISize.make(4, 4)
    )

    // Apply a color filter to convert to grayscale
    val grayscaleShader = shader.makeWithColorFilter(
        ColorFilter.makeMatrix(
            // Use NCTS values to convert to grayscale
            // https://en.wikipedia.org/wiki/Grayscale#Converting_color_to_grayscale
            ColorMatrix(
                0.299f, 0.587f, 0.114f, 0f, 0f,   // Red to luminance
                0.299f, 0.587f, 0.114f, 0f, 0f,         // Green to luminance
                0.299f, 0.587f, 0.114f, 0f, 0f,         // Blue to luminance
                0f, 0f, 0f, 1f, 0f                      // Alpha unchanged
            )
        )
    )

    Canvas(modifier = Modifier.fillMaxSize()) {
        // Background color
        drawRect(color = JervisTheme.rulebookPaper, size = size)
        // Add Noise
        drawRect(
            size = size,
            brush = ShaderBrush(grayscaleShader),
            alpha = 0.3f,
        )
        // Re-add background color to make the noise blend more into the background
        drawRect(color = JervisTheme.rulebookPaper.copy(alpha = 0.5f), size = size)
    }
}

@Composable
fun MenuScreen(content: @Composable BoxScope.() -> Unit) {
    Box(
        modifier = Modifier.fillMaxSize().background(Color.Black),
    ) {
        PaperBackground()
        Image(
            modifier = Modifier.fillMaxSize(),
            bitmap = imageResource(Res.drawable.menu_background),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            colorFilter = androidx.compose.ui.graphics.ColorFilter.tint(JervisTheme.rulebookPaper, BlendMode.Color),
            alpha = 0.075f
        )
        content()
    }
}
