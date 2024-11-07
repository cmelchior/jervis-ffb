package com.jervisffb.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Canvas
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.Paint
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.graphics.toComposeImageBitmap
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import com.jervisffb.jervis_ui.generated.resources.Res
import org.jetbrains.compose.resources.ExperimentalResourceApi
import org.jetbrains.skia.FilterBlurMode
import org.jetbrains.skia.Image
import org.jetbrains.skia.MaskFilter
import kotlin.math.PI

fun toRadians(deg: Double): Double = deg / 180.0 * PI

/**
 * Convert a pixel value to the corresponding dp value taking the screen
 * density into account.
 *
 * On Retina displays, this will scale the pixels up.
 */
@Composable
fun pixelsToDp(px: Float): Dp {
    val density = LocalDensity.current
    return with(density) { (this.density * px).toDp() }
}

/**
 * Convert a Pixel value back to its Dp value
 *
 * This is e.g. used when using LayoutCoordinates to define the size of another
 * composable.
 */
@Composable
fun Float.asDp(): Dp {
    val density = LocalDensity.current
    return remember(this, density) { with(density) { this@asDp.toDp() } }
}

@OptIn(ExperimentalResourceApi::class)
internal suspend fun Res.loadImage(path: String): ImageBitmap {
    return Image.makeFromEncoded(readBytes("drawable/$path")).toComposeImageBitmap()
}

@OptIn(ExperimentalResourceApi::class)
internal suspend fun Res.loadFileAsImage(path: String): ImageBitmap {
    return Image.makeFromEncoded(readBytes("files/$path")).toComposeImageBitmap()
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


fun Modifier.coloredShadow(
    color: Color,
    blurRadius: Float,
    offsetY: Dp,
    offsetX: Dp,
) = then(
    drawBehind {
        drawIntoCanvas { canvas ->
            val paint = Paint()
            val frameworkPaint = paint.asFrameworkPaint()

            if (blurRadius != 0f) {
                frameworkPaint.maskFilter = MaskFilter.makeBlur(FilterBlurMode.NORMAL, blurRadius / 2, true)
            }

            frameworkPaint.color = color.toArgb()

            val centerX = size.width / 2 + offsetX.toPx()
            val centerY = size.height / 2 + offsetY.toPx()
            val radius = size.width.coerceAtLeast(size.height) / 2

            canvas.drawCircle(Offset(centerX, centerY), radius, paint)
        }
    }
)

fun Modifier.dropShadow(
    color: Color = Color.Black,
    offsetX: Dp = 0.dp,
    offsetY: Dp = 0.dp,
    blurRadius: Dp = 0.dp,
) = then(
    drawBehind {
        drawIntoCanvas { canvas ->
            val paint = Paint()
            val frameworkPaint = paint.asFrameworkPaint()
            if (blurRadius != 0.dp) {
//                frameworkPaint.maskFilter = (BlurMaskFilter(blurRadius.toPx(), FilterBlurMode.NORMAL))
                frameworkPaint.maskFilter = MaskFilter.makeBlur(FilterBlurMode.NORMAL, blurRadius.toPx())
            }
            frameworkPaint.color = color.toArgb()

            val leftPixel = offsetX.toPx()
            val topPixel = offsetY.toPx()
            val rightPixel = size.width + topPixel
            val bottomPixel = size.height + leftPixel

            canvas.drawRect(
                left = leftPixel,
                top = topPixel,
                right = rightPixel,
                bottom = bottomPixel,
                paint = paint,
            )
        }
    }
)
