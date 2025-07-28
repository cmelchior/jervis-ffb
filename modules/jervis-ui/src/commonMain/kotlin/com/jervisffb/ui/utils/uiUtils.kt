package com.jervisffb.ui.utils

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Canvas
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.Outline
import androidx.compose.ui.graphics.Paint
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.asSkiaBitmap
import androidx.compose.ui.graphics.drawscope.CanvasDrawScope
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.graphics.toComposeImageBitmap
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.LayoutDirection
import org.jetbrains.skia.Bitmap
import org.jetbrains.skia.Image
import org.jetbrains.skia.SamplingMode

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
private fun Painter.toImageBitmap(
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
