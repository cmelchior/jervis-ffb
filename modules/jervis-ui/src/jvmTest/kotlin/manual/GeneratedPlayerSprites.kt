package manual

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.FilterQuality
import androidx.compose.ui.graphics.toComposeImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import com.jervisffb.ui.game.view.JervisTheme
import com.jervisffb.ui.utils.toSkiaColor
import org.jetbrains.skia.Bitmap
import org.jetbrains.skia.Canvas
import org.jetbrains.skia.FontMgr
import org.jetbrains.skia.FontStyle
import org.jetbrains.skia.Image
import org.jetbrains.skia.Paint
import org.jetbrains.skia.PaintMode
import org.jetbrains.skia.TextLine
import kotlin.math.floor
import kotlin.test.Ignore
import kotlin.test.Test

@Ignore
class GeneratedPlayerSprites {

    @Test
    fun playerSprites() {
        application {
            Window(onCloseRequest = ::exitApplication, title = "Iso Demo") {
                MaterialTheme {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = androidx.compose.ui.Alignment.Center,
                    ) {
                        PlayerSpriteDemo()
                    }
                }
            }
        }
    }
}

@Composable
private fun PlayerSpriteDemo() {
    val letters = ""
    // Prepare sprite sheet image
    val w = 120
    val h = 30
    val bitmap = Bitmap()
    bitmap.allocN32Pixels(w, h, false)
    val canvas = Canvas(bitmap)

    // Load default system font
    val mgr = FontMgr.default
    val typeface = mgr.matchFamilyStyle(null, FontStyle.NORMAL)
        ?: mgr.legacyMakeTypeface("", FontStyle.NORMAL)
    val font = org.jetbrains.skia.Font(typeface, 14f).apply {
        this.isSubpixel = false
    }

    // Draw sprites
    val radius = 14f
    val centers = floatArrayOf(15f, 45f, 75f, 105f).map { x -> Offset(x, 15f) }

    val paint = Paint().apply {
        color = JervisTheme.rulebookRed.toSkiaColor()
        isAntiAlias = false
    }
    val borderPaint = Paint().apply {
        color = JervisTheme.black.toSkiaColor()
        mode = PaintMode.STROKE
        strokeWidth = 1f
        isAntiAlias = false
    }
    val textPaint = Paint().apply {
        color = JervisTheme.white.toSkiaColor()
        isAntiAlias = false
    }

    val l = TextLine.make("S", font)
    val ascent = l.ascent
    val descent = l.descent
    val leading = l.leading

    // Not 100% sure why this is correct. It feels like it is just by accident :thinking:
    // val baselineY = centers[0].y + (0.5f*centers[0].y + 0.5f*((ascent + descent) * 0.5f + leading * 0.5f));

    // Y-value is the same, so just use the first
    val baselineY = centers[0].y + (l.height - (descent + leading)) / 2f;

    // First two red
    paint.color = JervisTheme.rulebookRed.toSkiaColor()
    canvas.drawCircle(centers[0].x, centers[0].y, radius, paint)
    canvas.drawCircle(centers[0].x, centers[0].y, radius, borderPaint)
    val baseline1X = floor(centers[0].x - (l.width / 2f)) // Round down since rounding up makes it look more "off"
    canvas.drawTextLine(l, baseline1X, baselineY, textPaint)

    canvas.drawCircle(centers[1].x, centers[1].y, radius, paint)
    canvas.drawCircle(centers[1].x, centers[1].y, radius, borderPaint)
    val baseline2X = floor(centers[1].x - (l.width / 2f)) // Round down since rounding up makes it look more "off"
    canvas.drawTextLine(l,baseline2X, baselineY, textPaint)

    // Last two blue
    paint.color = JervisTheme.rulebookBlue.toSkiaColor()
    canvas.drawCircle(centers[2].x, centers[2].y, radius, paint)
    canvas.drawCircle(centers[2].x, centers[2].y, radius, borderPaint)
    val baseline3X = floor(centers[2].x - (l.width / 2f)) // Round down since rounding up makes it look more "off"
    canvas.drawTextLine(l,baseline3X, baselineY, textPaint)

    canvas.drawCircle(centers[3].x, centers[3].y, radius, paint)
    canvas.drawCircle(centers[3].x, centers[3].y, radius, borderPaint)
    val baseline4X = floor(centers[3].x - (l.width / 2f)) // Round down since rounding up makes it look more "off"
    canvas.drawTextLine(l,baseline4X, baselineY, textPaint)

    Image(
        bitmap = Image.makeFromBitmap(bitmap).toComposeImageBitmap(),
        contentDescription = "Player Sprite",
        contentScale = ContentScale.Fit,
        modifier = Modifier.size(120.dp, 30.dp),
        filterQuality = FilterQuality.None,
    )
}

