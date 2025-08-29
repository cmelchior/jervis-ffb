package com.jervisffb.ui.utils

import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Canvas
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.Outline
import androidx.compose.ui.graphics.Paint
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.asSkiaBitmap
import androidx.compose.ui.graphics.drawscope.CanvasDrawScope
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.graphics.toComposeImageBitmap
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.changedToUp
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.input.pointer.positionChange
import androidx.compose.ui.layout.layout
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.LayoutDirection
import org.jetbrains.skia.Bitmap
import org.jetbrains.skia.Image
import org.jetbrains.skia.SamplingMode
import kotlin.math.hypot
import kotlin.math.max
import kotlin.math.min

/**
 * Helper [Modifier] that is conditionally applied only if the condition is `true`
 * This is useful when writing chains of modifiers.
 */
fun Modifier.applyIf(condition: Boolean, modifier: Modifier.() -> Modifier): Modifier {
    return if (condition) {
        this.then(modifier(this))
    } else {
        this
    }
}

/**
 * Convert a pixel value to the corresponding dp value taking the screen
 * density into account. This is useful for placing UI components using their
 * original px values in a layout that is using [Dp] to layout children.
 *
 * On Retina displays, this will scale the pixels up from 1 to 2.0.
 */
@Composable
fun pixelsToDp(px: Float): Dp {
    val density = LocalDensity.current
    return with(density) { (this.density * px).toDp() }
}

/**
 * Converts a [Painter] to a Compose [ImageBitmap], making it easier to process further.
 */
fun Painter.toImageBitmap(
    size: Size,
    density: Density,
): ImageBitmap {
    // Right now this method isn't used anywhere where LayoutDirection matters, so hard-code for now.
    val layoutDirection: LayoutDirection = LayoutDirection.Ltr
    val bitmap = ImageBitmap(size.width.toInt(), size.height.toInt())
    val canvas = androidx.compose.ui.graphics.Canvas(bitmap)
    CanvasDrawScope().draw(density, layoutDirection, canvas, size) {
        draw(size)
    }
    return bitmap
}

/**
 * Scale an image manually in discrete steps (ensuring no interpolation).
 */
fun ImageBitmap.scalePixels(scale: Int): ImageBitmap {
    if (scale <= 0) throw IllegalArgumentException("Scale must be greater than 0")
    if (scale == 1) return this
    val bitmap = Bitmap()
    bitmap.allocN32Pixels(width * scale, height * scale)
    val skiaPixMap = bitmap.peekPixels() ?: error("Could not create PixMap for: $this [${width}x$height]")
    val sourceSkiaImage = Image.makeFromBitmap(this.asSkiaBitmap())
    sourceSkiaImage.scalePixels(
        dst = skiaPixMap,
        samplingMode = SamplingMode.DEFAULT,
        cache = true
    )
    return Image.makeFromBitmap(bitmap).toComposeImageBitmap()
}

/**
 * Converts an [Outline] to a Compose [Path] that can be used to draw the outline
 * on a [androidx.compose.ui.graphics.Canvas].
 */
fun Outline.toPath(): Path = when (this) {
    is Outline.Generic -> path
    is Outline.Rectangle -> Path().apply {
        addRect(rect)
    }
    is Outline.Rounded -> Path().apply {
        addRoundRect(roundRect)
    }
}

fun ImageBitmap.getSubImage(x: Int, y: Int, width: Int, height: Int): ImageBitmap {
    val newImageBitmap = ImageBitmap(width, height)
    val canvas = Canvas(newImageBitmap)
    canvas.drawImageRect(
        image = this,
        srcOffset = IntOffset(x, y),
        srcSize = IntSize(width, height),
        dstOffset = IntOffset.Zero,
        dstSize = IntSize(width, height),
        paint = Paint() // .apply { colorFilter = ColorFilter.tint(androidx.compose.ui.graphics.Color.Unspecified) },
    )
    return newImageBitmap
}


/**
 * Darken a color using HSL. [factor] is [0-1f].
 */
fun Color.darken(factor: Float): Color {
    val hsl = rgbToHsl(red, green, blue)
    hsl[2] = (hsl[2] * (1f - factor)).coerceIn(0f, 1f)
    return hslToColor(hsl)
}

/**
 * Darken a color using HSL. [factor] is [0-1f].
 */
fun Color.lighten(factor: Float): Color {
    val hsl = rgbToHsl(red, green, blue)
    hsl[2] = (hsl[2] + (1f - hsl[2]) * factor).coerceIn(0f, 1f)
    return hslToColor(hsl)
}

/**
 * Convert a Compose [Color] to a Skia [org.jetbrains.skia.Color]
 */
fun Color.toSkiaColor(): Int {
    val red = (red * 255).toInt()
    val green = (green * 255).toInt()
    val blue = (blue * 255).toInt()
    return org.jetbrains.skia.Color.makeRGB(r = red, g = green, b = blue)
}

/**
 * For some reason Compose really likes to turn clicks into drags when using
 * a touchpad.
 *
 * This helper customizes the experiences, so we can control when a "drag" gets
 * turned into a "click". Note, this and `onClick` should not be combined on the
 * same button.
 *
 * Note, if the drag starts just in the corner of the button, and you drag outside
 * it, it will still be registered as a click.
 */
fun Modifier.onClickWithSmallDragControl(
    slopPx: Float = 6f, // Within these many pixels a drag is converted to OnClick
    onClick: () -> Unit
) = pointerInput(Unit) {
    awaitEachGesture {
        val down = awaitFirstDown(requireUnconsumed = false)
        var dx = 0f
        var dy = 0f
        var exceeded = false
        var change = down
        change.consume()
        while (change.pressed) {
            val event = awaitPointerEvent(PointerEventPass.Main)
            val c = event.changes.first { it.id == change.id }
            val d = c.positionChange()
            dx += d.x; dy += d.y
            if (hypot(dx.toDouble(), dy.toDouble()) > slopPx) {
                exceeded = true
                break
            }
            change = c
            if (c.changedToUp()) break
            c.consume()
        }
        if (!exceeded && !change.pressed) {
            onClick()
        }
    }
}

/**
 * Modifier that sets the size of the Composable using pixel values.
 */
fun Modifier.pixelSize(size: IntSize) =
    this.layout { measurable, _ ->
        val placeable = measurable.measure(Constraints.fixed(size.width, size.height))
        layout(size.width, size.height) { placeable.place(0, 0) }
    }

// Copy from ChatGPT, so requires a more thorough review
private fun rgbToHsl(r: Float, g: Float, b: Float): FloatArray {
    val max = max(r, max(g, b))
    val min = min(r, min(g, b))
    val delta = max - min
    var h = 0f
    var s: Float
    val l = (max + min) / 2f
    if (delta == 0f) {
        h = 0f
        s = 0f
    } else {
        s = if (l > 0.5f) delta / (2f - max - min) else delta / (max + min)
        h = when (max) {
            r -> ((g - b) / delta + if (g < b) 6 else 0)
            g -> ((b - r) / delta + 2)
            else -> ((r - g) / delta + 4)
        }
        h /= 6f
    }
    return floatArrayOf(h, s, l)
}

// Copy from ChatGPT, so requires a more thorough review
private fun hslToColor(hsl: FloatArray): Color {
    val (h, s, l) = hsl
    if (s == 0f) return Color(l, l, l)
    val q = if (l < 0.5f) l * (1 + s) else l + s - l * s
    val p = 2 * l - q
    fun hue2rgb(p: Float, q: Float, t: Float): Float {
        var tt = t
        if (tt < 0f) tt += 1f
        if (tt > 1f) tt -= 1f
        return when {
            tt < 1f / 6f -> p + (q - p) * 6f * tt
            tt < 1f / 2f -> q
            tt < 2f / 3f -> p + (q - p) * (2f / 3f - tt) * 6f
            else -> p
        }
    }
    val r = hue2rgb(p, q, h + 1f / 3f)
    val g = hue2rgb(p, q, h)
    val b = hue2rgb(p, q, h - 1f / 3f)
    return Color(r, g, b)
}
