package com.jervisffb.ui.game.view.utils

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.HorizontalDivider
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.jervisffb.ui.game.view.JervisTheme
import com.jervisffb.ui.utils.jdp
import org.jetbrains.skia.PaintMode
import org.jetbrains.skia.impl.use
import kotlin.math.roundToInt
import kotlin.math.sqrt
import org.jetbrains.skia.Canvas as SkiaCanvas
import org.jetbrains.skia.Paint as SkiaPaint


@Composable
fun TitleBorder(color: Color = JervisTheme.rulebookRed, alpha: Float = 1f, thickness: Dp = 3.dp) {
    EmbossedHorizontalDivider(modifier = Modifier.alpha(1f), thickness = 2.jdp)
//    Box {
//        HorizontalDivider(
//            modifier = Modifier.alpha(alpha).offset(2.dp, 2.dp),
//            color = JervisTheme.black.copy(alpha = 0.7f * alpha),
//            thickness = thickness
//        )
//        HorizontalDivider(
//            modifier = Modifier.alpha(alpha).offset(-2.dp, -2.dp),
//            color = JervisTheme.white.copy(alpha = 0.3f * alpha),
//            thickness = thickness
//        )
//        HorizontalDivider(
//            modifier = Modifier.alpha(alpha),
//            color = color,
//            thickness = thickness
//        )
//    }
}

@Composable
fun OrangeTitleBorder(alpha: Float = 1f) {
    TitleBorder(JervisTheme.rulebookOrange, alpha)
}

@Composable
fun EmbossedHorizontalDivider(
    baseColor: Color = JervisTheme.rulebookOrange,
    thickness: Dp = 1.dp,
    modifier: Modifier = Modifier,
    raised: Boolean = false,
) {
    val highlight = baseColor.mix(Color.White, 0.25f)
    val shadow = baseColor.mix(Color.Black, 0.30f)

    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(baseColor),
    ) {
        HorizontalDivider(
            thickness = thickness,
            color = if (raised) highlight else shadow,
        )
        HorizontalDivider(
            thickness = thickness,
            color = baseColor,
        )
        HorizontalDivider(
            thickness = thickness,
            color = if (raised) shadow else highlight,
        )
    }
}

/**
 * Draws a three-stroke line that appears pressed into, or raised from, [baseColor].
 * [lightDirection] is used only to determine which side receives the highlight.
 */
fun DrawScope.drawEmbossedLine(
    start: Offset,
    end: Offset,
    baseColor: Color,
    thickness: Float,
    depth: Float = thickness,
    lightDirection: Offset = Offset(-1f, -1f),
    raised: Boolean = false,
) {
    require(thickness >= 0f) { "thickness cannot be negative" }
    require(depth >= 0f) { "depth cannot be negative" }
    if (thickness == 0f || baseColor.alpha <= 0f) return

    val deltaX = end.x - start.x
    val deltaY = end.y - start.y
    val lineLength = sqrt(deltaX * deltaX + deltaY * deltaY)
    if (lineLength == 0f) return

    var normalX = -deltaY / lineLength
    var normalY = deltaX / lineLength
    if (normalX * lightDirection.x + normalY * lightDirection.y < 0f) {
        normalX = -normalX
        normalY = -normalY
    }

    val lightOffset = Offset(normalX * depth, normalY * depth)
    val highlight = baseColor.mix(Color.White, 0.25f)
    val shadow = baseColor.mix(Color.Black, 0.30f)
    val lightSideColor = if (raised) highlight else shadow
    val oppositeSideColor = if (raised) shadow else highlight

    if (depth > 0f) {
        drawLine(
            color = lightSideColor,
            start = start + lightOffset,
            end = end + lightOffset,
            strokeWidth = thickness,
        )
        drawLine(
            color = oppositeSideColor,
            start = start - lightOffset,
            end = end - lightOffset,
            strokeWidth = thickness,
        )
    }
    drawLine(
        color = baseColor,
        start = start,
        end = end,
        strokeWidth = thickness,
    )
}

/**
 * Draws an embossed line directly into a low-resolution raster canvas.
 *
 * Coordinates, depth, and stroke width are snapped to whole raster pixels and
 * anti-aliasing is disabled so nearest-neighbor upscaling preserves hard pixels.
 */
fun SkiaCanvas.drawEmbossedLine(
    start: Offset,
    end: Offset,
    baseColor: Color,
    thickness: Float,
    depth: Float = thickness,
    lightDirection: Offset = Offset(-1f, -1f),
    raised: Boolean = false,
) {
    require(thickness >= 0f) { "thickness cannot be negative" }
    require(depth >= 0f) { "depth cannot be negative" }
    if (thickness == 0f || baseColor.alpha <= 0f) return

    val pixelStart = start.roundToPixel()
    val pixelEnd = end.roundToPixel()
    val deltaX = pixelEnd.x - pixelStart.x
    val deltaY = pixelEnd.y - pixelStart.y
    val lineLength = sqrt(deltaX * deltaX + deltaY * deltaY)
    if (lineLength == 0f) return

    var normalX = -deltaY / lineLength
    var normalY = deltaX / lineLength
    if (normalX * lightDirection.x + normalY * lightDirection.y < 0f) {
        normalX = -normalX
        normalY = -normalY
    }

    val pixelDepth = depth.roundToInt().coerceAtLeast(if (depth > 0f) 1 else 0).toFloat()
    val lightOffset = Offset(
        x = (normalX * pixelDepth).toInt().toFloat(),
        y = (normalY * pixelDepth).toInt().toFloat(),
    )
    val highlight = baseColor.mix(Color.White, 0.25f)
    val shadow = baseColor.mix(Color.Black, 0.30f)
    val lightSideColor = if (raised) highlight else shadow
    val oppositeSideColor = if (raised) shadow else highlight

    SkiaPaint().use { paint ->
        paint.mode = PaintMode.STROKE
        paint.strokeWidth = thickness.roundToInt().coerceAtLeast(1).toFloat()
        paint.isAntiAlias = false

        fun drawPixelLine(color: Color, offset: Offset = Offset.Zero) {
            paint.color = color.toArgb()
            drawLine(
                pixelStart.x + offset.x,
                pixelStart.y + offset.y,
                pixelEnd.x + offset.x,
                pixelEnd.y + offset.y,
                paint,
            )
        }

        if (pixelDepth > 0f) {
            drawPixelLine(lightSideColor, lightOffset)
            drawPixelLine(oppositeSideColor, -lightOffset)
        }
        drawPixelLine(baseColor)
    }
}

private fun Offset.roundToPixel(): Offset = Offset(
    x = x.roundToInt().toFloat(),
    y = y.roundToInt().toFloat(),
)

private fun Color.mix(other: Color, amount: Float): Color {
    val t = amount.coerceIn(0f, 1f)

    return Color(
        red = red + (other.red - red) * t,
        green = green + (other.green - green) * t,
        blue = blue + (other.blue - blue) * t,
        alpha = alpha,
    )
}
