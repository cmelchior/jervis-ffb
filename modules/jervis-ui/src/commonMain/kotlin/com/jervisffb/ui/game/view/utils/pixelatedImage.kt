package com.jervisffb.ui.game.view.utils

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asSkiaBitmap
import androidx.compose.ui.graphics.drawscope.CanvasDrawScope
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.LayoutDirection
import org.jetbrains.skia.Paint
import org.jetbrains.skia.Rect
import org.jetbrains.skia.RuntimeEffect
import org.jetbrains.skia.RuntimeShaderBuilder

/**
 * Convert an image into a pixelated image, so it looks more like an 8-bit graphic
 *
 * TODO This doesn't scale automatically when using jdp values. Probably some
 *  caching going wrong.
 */
@Composable
fun PixelatedImage(
    modifier: Modifier = Modifier,
    painter: Painter,
    pixelSize: Float = 4f
) {
    val shaderCode = """
            uniform shader img;
            uniform float2 resolution;
            uniform float pixelSize;

            half4 main(float2 fragCoord) {
                float2 pixelCoord = floor(fragCoord / pixelSize) * pixelSize;
                float2 uv = pixelCoord / resolution;
                return img.eval(uv * resolution);
            }
    """.trimIndent()

    val effect = remember { RuntimeEffect.makeForShader(shaderCode) }
    val shaderBuilder = remember(effect) { RuntimeShaderBuilder(effect) }

    BoxWithConstraints(
        modifier = modifier
    ) {
        val width = maxWidth
        val height = maxHeight
        val density = LocalDensity.current
        val widthPx = with(density) { width.toPx() }
        val heightPx = with(density) { height.toPx() }

        // Render SVG to bitmap at requested size
        val skiaBitmap = remember(painter) {
            val imageBitmap = painter.toImageBitmap(Size(widthPx, heightPx), density, LayoutDirection.Ltr)
            imageBitmap.asSkiaBitmap()
        }

        // Build the shader to pixelate the image
        val shader = remember {
            shaderBuilder.uniform(
                "resolution",
                widthPx,
                heightPx
            )
            shaderBuilder.uniform(
                "pixelSize",
                pixelSize
            )
            shaderBuilder.child(
                "img",
                skiaBitmap.makeShader()
            )
            shaderBuilder.makeShader()
        }

        Canvas(modifier = Modifier.size(width, height)) {
            drawIntoCanvas { canvas ->
                val paint = Paint().apply {
                    this.shader = shader
                }
                canvas.nativeCanvas.drawRect(Rect.makeXYWH(0f, 0f, widthPx, heightPx), paint)
            }
        }
    }
}

private fun Painter.toImageBitmap(
    size: Size,
    density: Density,
    layoutDirection: LayoutDirection,
): ImageBitmap {
    val bitmap = ImageBitmap(size.width.toInt(), size.height.toInt())
    val canvas = androidx.compose.ui.graphics.Canvas(bitmap)
    CanvasDrawScope().draw(density, layoutDirection, canvas, size) {
        draw(size)
    }
    return bitmap
}

