package com.jervisffb.ui.game.icons

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.toComposeImageBitmap
import com.jervisffb.engine.model.PlayerSize
import com.jervisffb.ui.game.view.JervisTheme
import com.jervisffb.ui.theme.loadNotoSansSymbols2SkiaFont
import com.jervisffb.ui.utils.darken
import com.jervisffb.ui.utils.toSkiaColor
import org.jetbrains.skia.Bitmap
import org.jetbrains.skia.Color
import org.jetbrains.skia.FontEdging
import org.jetbrains.skia.FontHinting
import org.jetbrains.skia.Image
import org.jetbrains.skia.PaintMode
import org.jetbrains.skia.TextLine
import kotlin.math.ceil
import kotlin.math.roundToInt

/**
 * Class wrapping the creation of fallback sprites if we cannot load the correct ones.
 * They are just a basic circle with the position letters in the middle.
 */
object PlayerSpriteFallbackGenerator {

    // Create a player sprite. We create this as a normal player size, so 4x(30x30) = 120x30 px
    suspend fun generatePlayerSprite(letters: String, size: PlayerSize): ImageBitmap {
        // Prepare sprite sheet image
        val (w, h) = when (size) {
            PlayerSize.STANDARD -> 120 to 30
            PlayerSize.BIG_GUY -> 160 to 40
            PlayerSize.GIANT -> TODO("Giants not supported")
        }
        val bitmap = Bitmap()
        bitmap.allocN32Pixels(w, h, false)
        bitmap.erase(Color.TRANSPARENT)
        val canvas = org.jetbrains.skia.Canvas(bitmap)

        // Load default system font
        val typeface = loadNotoSansSymbols2SkiaFont()
        val fontSize = when (size) {
            PlayerSize.STANDARD -> 14f
            PlayerSize.BIG_GUY -> 14f
            PlayerSize.GIANT -> TODO("Giants not supported")
        }
        val font = org.jetbrains.skia.Font(typeface, fontSize).apply {
            isEmboldened = false
            isSubpixel = true
            isBaselineSnapped = true
            hinting = FontHinting.NORMAL
            edging = FontEdging.SUBPIXEL_ANTI_ALIAS
        }

        // Draw sprites
        val radius = (h - 2)/2f
        val centers = floatArrayOf(h/2f, h*1.5f, h*2.5f, h*3.5f).map { x -> Offset(x, h/2f) }

        val paint = org.jetbrains.skia.Paint().apply {
            color = JervisTheme.rulebookRed.toSkiaColor()
            isAntiAlias = false
        }
        val borderPaint = org.jetbrains.skia.Paint().apply {
            color = JervisTheme.black.toSkiaColor()
            mode = PaintMode.STROKE
            strokeWidth = 1f
            isAntiAlias = false
        }
        val textPaint = org.jetbrains.skia.Paint().apply {
            color = JervisTheme.white.toSkiaColor()
            isAntiAlias = false
        }

        val redOutlinePaint = org.jetbrains.skia.Paint().apply {
            color = JervisTheme.rulebookRed.darken(0.25f).toSkiaColor()
            mode = PaintMode.STROKE
            strokeWidth = 1f
            isAntiAlias = false
        }
        val blueOutlinePaint = org.jetbrains.skia.Paint().apply {
            color = JervisTheme.rulebookBlue.darken(0.25f).toSkiaColor()
            mode = PaintMode.STROKE
            strokeWidth = 1f
            isAntiAlias = false
        }

        val text = TrackedText.make(letters, font)
        val baselineOffsetX = -(text.width / 2f)
        val baselineOffsetY = text.visibleBaselineOffsetY(font, redOutlinePaint, textPaint)

        // First two red
        paint.color = JervisTheme.rulebookRed.toSkiaColor()
        canvas.drawCircle(centers[0].x, centers[0].y, radius, paint)
        canvas.drawCircle(centers[0].x, centers[0].y, radius, borderPaint)
        canvas.drawCenteredText(
            text,
            centers[0],
            baselineOffsetX,
            baselineOffsetY,
            redOutlinePaint,
            textPaint,
        )

        canvas.drawCircle(centers[1].x, centers[1].y, radius, paint)
        canvas.drawCircle(centers[1].x, centers[1].y, radius, borderPaint)
        canvas.drawCenteredText(
            text,
            centers[1],
            baselineOffsetX,
            baselineOffsetY,
            redOutlinePaint,
            textPaint,
        )

        // Last two blue
        paint.color = JervisTheme.rulebookBlue.toSkiaColor()
        canvas.drawCircle(centers[2].x, centers[2].y, radius, paint)
        canvas.drawCircle(centers[2].x, centers[2].y, radius, borderPaint)
        canvas.drawCenteredText(
            text,
            centers[2],
            baselineOffsetX,
            baselineOffsetY,
            blueOutlinePaint,
            textPaint,
        )

        canvas.drawCircle(centers[3].x, centers[3].y, radius, paint)
        canvas.drawCircle(centers[3].x, centers[3].y, radius, borderPaint)
        canvas.drawCenteredText(
            text,
            centers[3],
            baselineOffsetX,
            baselineOffsetY,
            blueOutlinePaint,
            textPaint,
        )

        // Return generated sprite sheet
        canvas.close()
        val spriteSheet = Image.makeFromBitmap(bitmap).toComposeImageBitmap()
        return spriteSheet
    }

    // Helper class making it easier to control exact layout of text on player icons
    private class TrackedText(
        val lines: List<TextLine>,
        val xOffsets: FloatArray,
        val width: Float,
        val metricsLine: TextLine,
    ) {
        companion object {
            fun make(text: String, font: org.jetbrains.skia.Font, letterSpacing: Float = 0.10f /* In Percent */): TrackedText {
                val lines = text.map { letter -> TextLine.make(letter.toString(), font) }
                val xOffsets = FloatArray(lines.size)
                val tracking = (font.size * letterSpacing) // .roundToInt().toFloat()
                var x = 0f
                lines.forEachIndexed { index, line ->
                    xOffsets[index] = x
                    x += line.width
                    if (index < lines.lastIndex) {
                        x += tracking
                    }
                }
                return TrackedText(
                    lines = lines,
                    xOffsets = xOffsets,
                    width = x,
                    metricsLine = TextLine.make(text, font),
                )
            }
        }
    }

    private fun org.jetbrains.skia.Canvas.drawCenteredText(
        text: TrackedText,
        center: Offset,
        baselineOffsetX: Float,
        baselineOffsetY: Float,
        outlinePaint: org.jetbrains.skia.Paint,
        fillPaint: org.jetbrains.skia.Paint,
    ) {
        val x = (center.x + baselineOffsetX).roundToInt().toFloat()
        val y = (center.y + baselineOffsetY).roundToInt().toFloat()
        text.lines.forEachIndexed { index, line ->
            val letterX = x + text.xOffsets[index]
            drawTextLine(line, letterX, y, outlinePaint)
            drawTextLine(line, letterX, y, fillPaint)
        }
    }

    private fun TrackedText.visibleBaselineOffsetY(
        font: org.jetbrains.skia.Font,
        outlinePaint: org.jetbrains.skia.Paint,
        fillPaint: org.jetbrains.skia.Paint,
    ): Float {
        val padding = 4
        val metrics = font.metrics
        val top = minOf(metrics.top, metricsLine.ascent)
        val bottom = maxOf(metrics.bottom, metricsLine.descent)
        val width = ceil(this.width + padding * 2f).roundToInt().coerceAtLeast(1)
        val height = ceil(bottom - top + padding * 2f).roundToInt().coerceAtLeast(1)
        val baselineY = padding - top
        val bitmap = Bitmap()
        bitmap.allocN32Pixels(width, height, false)
        bitmap.erase(Color.TRANSPARENT)

        val canvas = org.jetbrains.skia.Canvas(bitmap)
        lines.forEachIndexed { index, line ->
            val x = padding + xOffsets[index]
            canvas.drawTextLine(line, x, baselineY, outlinePaint)
            canvas.drawTextLine(line, x, baselineY, fillPaint)
        }
        canvas.close()

        val pixels = bitmap.peekPixels()
            ?: return -((metricsLine.ascent + metricsLine.descent) / 2f)
        var topY = height
        var bottomY = -1
        for (y in 0 until height) {
            for (x in 0 until width) {
                if (pixels.getAlphaF(x, y) > 0f) {
                    topY = minOf(topY, y)
                    bottomY = maxOf(bottomY, y)
                }
            }
        }
        if (bottomY < topY) {
            return -((metricsLine.ascent + metricsLine.descent) / 2f)
        }

        val visibleCenterY = (topY + bottomY + 1) / 2f
        return baselineY - visibleCenterY
    }

}
