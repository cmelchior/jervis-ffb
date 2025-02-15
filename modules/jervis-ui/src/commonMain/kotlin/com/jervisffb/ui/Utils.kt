package com.jervisffb.ui

import androidx.compose.foundation.border
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
import com.jervisffb.engine.model.Coach
import com.jervisffb.engine.model.CoachId
import com.jervisffb.engine.model.PlayerId
import com.jervisffb.engine.model.PlayerNo
import com.jervisffb.engine.model.Team
import com.jervisffb.engine.rules.StandardBB2020Rules
import com.jervisffb.engine.rules.bb2020.skills.SideStep
import com.jervisffb.engine.teamBuilder
import com.jervisffb.jervis_ui.generated.resources.Res
import com.jervisffb.resources.HUMAN_BLITZER
import com.jervisffb.resources.HUMAN_CATCHER
import com.jervisffb.resources.HUMAN_LINEMAN
import com.jervisffb.resources.HUMAN_TEAM
import com.jervisffb.resources.HUMAN_THROWER
import org.jetbrains.compose.resources.ExperimentalResourceApi
import org.jetbrains.skia.FilterBlurMode
import org.jetbrains.skia.Image
import org.jetbrains.skia.MaskFilter
import kotlin.math.PI

fun toRadians(deg: Double): Double = deg / 180.0 * PI

fun String.isDigitsOnly(): Boolean = this.all { it.isDigit() }

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


fun Modifier.debugBorder(): Modifier = this.border(1.dp, Color.Cyan)

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

fun createDefaultHomeTeam(): Team {
    return teamBuilder(StandardBB2020Rules(), HUMAN_TEAM) {
        coach = Coach(CoachId("home-coach"), "HomeCoach")
        name = "HomeTeam"
        addPlayer(PlayerId("H1"), "Lineman-1-H", PlayerNo(1), HUMAN_LINEMAN)
        addPlayer(PlayerId("H2"), "Lineman-2-H", PlayerNo(2), HUMAN_LINEMAN)
        addPlayer(PlayerId("H3"), "Lineman-3-H", PlayerNo(3), HUMAN_LINEMAN)
        addPlayer(PlayerId("H4"), "Lineman-4-H", PlayerNo(4), HUMAN_LINEMAN)
        addPlayer(PlayerId("H5"), "Thrower-5-H", PlayerNo(5), HUMAN_THROWER, listOf(SideStep()))
        addPlayer(PlayerId("H6"), "Catcher-6-H", PlayerNo(6), HUMAN_CATCHER, listOf(SideStep()))
        addPlayer(PlayerId("H7"), "Catcher-7-H", PlayerNo(7), HUMAN_CATCHER)
        addPlayer(PlayerId("H8"), "Blitzer-8-H", PlayerNo(8), HUMAN_BLITZER)
        addPlayer(PlayerId("H9"), "Blitzer-9-H", PlayerNo(9), HUMAN_BLITZER)
        addPlayer(PlayerId("H10"), "Blitzer-10-H", PlayerNo(10), HUMAN_BLITZER)
        addPlayer(PlayerId("H11"), "Blitzer-11-H", PlayerNo(11), HUMAN_BLITZER)
        addPlayer(PlayerId("H12"), "Lineman-12-H", PlayerNo(12), HUMAN_LINEMAN)
        reRolls = 4
        apothecaries = 1
        dedicatedFans = 1
        teamValue = 1_000_000
    }
}
