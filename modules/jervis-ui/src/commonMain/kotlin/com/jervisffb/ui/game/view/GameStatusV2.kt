package com.jervisffb.ui.game.view

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.Outline
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.asSkiaBitmap
import androidx.compose.ui.graphics.drawscope.CanvasDrawScope
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.painter.BitmapPainter
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import com.jervisffb.engine.model.Team
import com.jervisffb.ui.game.icons.IconFactory
import com.jervisffb.ui.game.icons.LogoSize
import com.jervisffb.ui.game.view.utils.paperBackground
import com.jervisffb.ui.game.viewmodel.GameProgress
import com.jervisffb.ui.game.viewmodel.GameStatusViewModel
import com.jervisffb.ui.toRadians
import com.jervisffb.ui.utils.applyIf
import org.jetbrains.skia.Paint
import org.jetbrains.skia.Rect
import org.jetbrains.skia.RuntimeEffect
import org.jetbrains.skia.RuntimeShaderBuilder
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min
import kotlin.math.tan

// Game Status Layout that is compatible with a Game Screen layout for a Blood Bowl 3 inspired layout
@Composable
fun GameStatusV2(
    vm: GameStatusViewModel,
    modifier: Modifier,
) {
    val progress by vm.progress().collectAsState(GameProgress(0, 0, 0, "", 0, "", 0))
    val angle = 5f
    val topPadding = 8.dp
    Box(
        modifier = modifier
    ) {
        Row {
            TeamInfo(vm.controller.gameController.state.homeTeam, JervisTheme.rulebookRed, leftSide = true)
            Spacer(modifier = Modifier.weight(1f))
            TeamInfo(vm.controller.gameController.state.awayTeam, JervisTheme.rulebookBlue, leftSide = false)
        }
        Row {
            Spacer(modifier = Modifier.weight(1f))
            TurnTracker(Modifier.padding(top = topPadding), angle = -angle, progress.turnMax, progress.homeTeamTurn, JervisTheme.rulebookRed, vm.controller.state.activeTeam?.isHomeTeam() == true)
            ScoreCounter(Modifier.padding(top = topPadding), progress, angle)
            TurnTracker(Modifier.padding(top = topPadding), angle = angle, progress.turnMax, progress.awayTeamTurn, JervisTheme.rulebookBlue, vm.controller.state.activeTeam?.isAwayTeam() == true)
            Spacer(modifier = Modifier.weight(1f))


//
//
////        Image(
////            bitmap = IconFactory.getScorebar(),
////            contentDescription = null,
////            alignment = Alignment.Center,
////            contentScale = ContentScale.FillBounds,
////            modifier = Modifier.fillMaxSize(),
////        )
//        val textModifier = Modifier.padding(4.dp)
//
//        // Turn counter
//        Row(modifier = Modifier, verticalAlignment = Alignment.CenterVertically) {
//            Text(
//                modifier = textModifier,
//                text = "Turn",
//                fontSize = 14.sp,
//                color = Color.White,
//            )
//            Text(
//                modifier = textModifier,
//                text = "${progress.homeTeamTurn} / ${progress.awayTeamTurn}",
//                color = Color.White,
//                fontWeight = FontWeight.Bold,
//                fontSize = 20.sp,
//            )
//
//            val half = when (progress.half) {
//                1 -> "1st half"
//                2 -> "2nd half"
//                3 -> "Extra Time"
//                else -> null
//            }
//            if (half != null) {
//                Text(
//                    modifier = textModifier,
//                    text = "of $half",
//                    fontSize = 14.sp,
//                    color = Color.White,
//                )
//            }
//        }
//
        }
    }
}

@Composable
private fun RowScope.TurnTracker(
    modifier: Modifier,
    angle: Float = 10f,
    turnMax: Int =  8,
    currentTurn: Int = 0,
    teamColor: Color,
    activeTeam: Boolean
) {
    Row(modifier = modifier) {
        for (turnNo in 1..turnMax) {
            val (borderColor, contentColor, alpha) = when {
                currentTurn < turnNo -> Triple(Color.White.copy(0.7f),JervisTheme.white.copy(0.15f), 1f)
                currentTurn == turnNo && activeTeam -> Triple(Color.Transparent,teamColor, 1f)
                currentTurn >= turnNo -> Triple(Color.Transparent,JervisTheme.white.copy(alpha = 0.2f), 0.6f)
                else -> error("Unsupported state: ($currentTurn, $turnNo) ")
            }
            ParallelogramButton(
                modifier = Modifier.width(36.dp).height(32.dp).alpha(alpha),
                onClick = { },
                angleDegrees = angle,
                containerColor = contentColor,
                borderColor = borderColor,
                borderWidth = 1.5.dp,
            ) {
                Text(
                    text = turnNo.toString(),
                    lineHeight = 1.em,
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    style = MaterialTheme.typography.h4.copy(
                        shadow = Shadow(
                            color = Color.Black,
                            offset = Offset(2f, 2f),
                            blurRadius = 0f
                        )
                    )
                )
            }
        }
    }
}

@Composable
private fun ScoreCounter(modifier: Modifier, progress: GameProgress, angle: Float = 5f) {
    val bigPadding = 5.dp
    val smallPadding = 2.dp
    val counterWidth = 40.dp
    val counterHeight = 48.dp
    val counterStyle = MaterialTheme.typography.h4.copy(
        shadow = Shadow(
            color = Color.Black,
            offset = Offset(2f, 2f),
            blurRadius = 0f
        )
    )
    Row(modifier = modifier, verticalAlignment = Alignment.Top, horizontalArrangement = Arrangement.Center) {
        ParallelogramButton(
            onClick = { },
            angleDegrees = -angle,
//            containerColor = JervisTheme.rulebookRed, //.copy(alpha = 0.7f),
//            borderColor = Color.Transparent, // JervisTheme.redDiceTop.copy(alpha = 0.7f),
            modifier = Modifier.padding(start = smallPadding).width(counterWidth).height(counterHeight),
        ) {
            Text(
                text = "${progress.homeTeamScore}",
                color = Color.White,
                fontWeight = FontWeight.Bold,
                fontSize = 28.sp,
                style = counterStyle
            )
        }
        GameStatusBox(bigPadding, angle)
        ParallelogramButton(
            onClick = { },
            angleDegrees = angle,
//            containerColor = JervisTheme.rulebookBlue,
//            borderColor = JervisTheme.rulebookBlue,
            modifier = Modifier.padding(end = smallPadding).width(counterWidth).height(counterHeight),
        ) {
            Text(
                text = "${progress.homeTeamScore}",
                color = Color.White,
                fontWeight = FontWeight.Bold,
                fontSize = 28.sp,
                style = counterStyle
            )
        }
    }
}

@Composable
private fun RowScope.GameStatusBox(padding: Dp = 0.dp, angle: Float) {
    val shape = remember(angle) { TrapezoidShape(angle) }
    Surface(
        modifier = Modifier
            .padding(start = padding, end = padding).paperBackground(shape = shape, color = JervisTheme.gameStatusBackground)
            .width(150.dp)
            .height(64.dp)
        ,
        shape = shape,
        color = Color.Transparent,
        // contentColor = contentColor,
        border = BorderStroke(4.dp, JervisTheme.white),
        elevation = 0.dp,
    ) {
        Column(
            modifier = Modifier.alpha(0.8f)
                .fillMaxSize()
                .padding(8.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(
                text = "End Turn".uppercase(),
                color = JervisTheme.white,
                lineHeight = 1.em,
                letterSpacing = 2.sp,
                // fontFamily = JervisTheme.fontFamily(),
                style = MaterialTheme.typography.body1.copy(
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Medium,
                    shadow = Shadow(
                        color = Color.Black,
                        offset = Offset(2f, 2f),
                        blurRadius = 0f
                    )
                )
            )
            Text(
                text = " ∞".uppercase(),
                color = JervisTheme.white,
                // fontFamily = JervisTheme.fontFamily(),
                style = MaterialTheme.typography.body1.copy(
                    fontSize = 28.sp,
                    letterSpacing = 1.5.sp,
                    fontWeight = FontWeight.Bold,
                    lineHeight = 1.em,
                    shadow = Shadow(
                        color = Color.Black,
                        offset = Offset(2f, 2f),
                        blurRadius = 0f
                    )
                )
            )
        }
    }
//    Box(
//        modifier = Modifier
//            .padding(start = padding, end = padding)
//            .width(150.dp)
//            .height(64.dp)
//            .clip(TrapezoidShape(angle))
//            .paperBackground(color = JervisTheme.gameStatusBackground)
////                .background(Color.White)
//    )
}

/**
 * Composable responsible for the team logo, name and coach name in upper left/right corner of the screen.
 */
@Composable
private fun TeamInfo(
    team: Team,
    backgroundColor: Color,
    leftSide: Boolean
) {
    val backgroundShape = ParallelogramShape(if (leftSide) -10f else 10f)
    val textPadding = 60.dp
    Box(
        modifier = Modifier
            .padding(8.dp)
//            .dropShadow(shape = CircleShape, color = backgroundColor, blur = 2.dp)
        ,
        contentAlignment = if (leftSide) Alignment.CenterStart else Alignment.CenterEnd
    ) {
        Column(
            modifier = Modifier
                .applyIf(leftSide) { padding(start = 44.dp) }
                .applyIf(!leftSide) { padding(end = 44.dp) }
            ,
            horizontalAlignment = if (leftSide) Alignment.Start else Alignment.End,
        ) {
            Box(modifier = Modifier
                .clip(backgroundShape)
                .width(200.dp)
                .height(24.dp)
                .background(JervisTheme.black)
                ,
                contentAlignment = Alignment.Center
            ) {
                Text(
                    modifier = Modifier
                        .fillMaxWidth()
                        .applyIf(leftSide) { padding(start = textPadding) }
                        .applyIf(!leftSide) { padding(end = textPadding) }
                    ,
                    textAlign = if (leftSide) TextAlign.Start else TextAlign.End,
                    text = team.coach.name,
                    color = Color.White,
                    // fontStyle = FontStyle.Italic,
                    lineHeight = 1.em,
                    fontSize = 12.sp
                )
            }
            Box(modifier = Modifier
                .clip(backgroundShape)
                .width(300.dp)
                .height(28.dp)
                .background(backgroundColor)
                ,
                contentAlignment = Alignment.Center
            ) {
                Text(
                    modifier = Modifier
                        .fillMaxWidth()
                        .applyIf(leftSide) { padding(start = textPadding) }
                        .applyIf(!leftSide) { padding(end = textPadding) }
                    ,
                    textAlign = if (leftSide) TextAlign.Start else TextAlign.End,
                    text = team.name,
                    color = Color.White,
                    lineHeight = 1.em,
                    overflow = TextOverflow.Ellipsis,
                    letterSpacing = 1.sp,
                    maxLines = 1,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = JervisTheme.fontFamily(),
                )
            }
        }
        PixelatedImageWithShader(
            modifier = Modifier.padding(8.dp).size(90.dp),
            painter = BitmapPainter(IconFactory.getLogo(team.id, LogoSize.SMALL)),
            pixelSize = 2f,
        )
//        Image(
//            modifier = Modifier.padding(8.dp).size(90.dp),
//            bitmap = IconFactory.getLogo(team.id, LogoSize.SMALL),
//            contentDescription = team.name,
//            contentScale = ContentScale.Fit,
//        )
    }
}

/**
 * A [Shape] that fits a parallelogram inside the given bounds.
 * The top edge is horizontally shifted by `tan(angleDegrees) * height`
 * relative to the bottom edge, and the shape is clamped to remain fully inside its bounds.
 */
class ParallelogramShape(
    private val angleDegrees: Float
) : Shape {
    override fun createOutline(size: Size, layoutDirection: androidx.compose.ui.unit.LayoutDirection, density: Density): Outline {
        val h = size.height
        val w = size.width

        // Horizontal shift of the TOP edge relative to the bottom edge
        val s = (tan(toRadians(angleDegrees.toDouble())) * h).toFloat()

        // Clamp so the shape stays within [0, w]
        val clamped = if ((-w + 1f) > (w - 1f)) {
            s // Fallback. Should not happen unless layout is extremely unusual
        } else {
            s.coerceIn(-w + 1f, w - 1f)
        }
        val innerWidth = (w - abs(clamped)).coerceAtLeast(1f)

        val topLeftX = max(0f, clamped)
        val bottomLeftX = max(0f, -clamped)
        val topRightX = topLeftX + innerWidth
        val bottomRightX = bottomLeftX + innerWidth

        val path = Path().apply {
            moveTo(topLeftX, 0f)
            lineTo(topRightX, 0f)
            lineTo(bottomRightX, h)
            lineTo(bottomLeftX, h)
            close()
        }
        return Outline.Generic(path)
    }
}

/**
 * A trapezoid where the bottom edge is shorter than the top.
 * `angleDegrees` controls how much each side slopes inward.
 */
class TrapezoidShape(
    private val angleDegrees: Float
) : Shape {
    override fun createOutline(
        size: Size,
        layoutDirection: LayoutDirection,
        density: Density
    ): Outline {
        val h = size.height
        val w = size.width

        // Horizontal shift per side for bottom edge
        val shift = (tan(toRadians(angleDegrees.toDouble())) * h).toFloat()

        // Clamp so bottom doesn't collapse
        val clamped = shift.coerceAtMost((w / 2) - 1f)
        val bottomWidth = (w - 2 * abs(clamped)).coerceAtLeast(1f)

        val topLeftX = 0f
        val topRightX = w
        val bottomLeftX = clamped
        val bottomRightX = w - clamped

        val path = Path().apply {
            moveTo(topLeftX, 0f)
            lineTo(topRightX, 0f)
            lineTo(bottomRightX, h)
            lineTo(bottomLeftX, h)
            close()
        }
        return Outline.Generic(path)
    }
}


@Composable
fun ParallelogramButton(
    onClick: () -> Unit,
    angleDegrees: Float = 15f,
    modifier: Modifier = Modifier,
    containerColor: Color = JervisTheme.white.copy(alpha = 0.2f),
    contentColor: Color = JervisTheme.black,
    borderWidth: Dp = 2.dp,
    borderColor: Color = JervisTheme.white.copy(alpha = 0.7f),
    enabled: Boolean = true,
    content: @Composable () -> Unit
) {
    val shape = remember(angleDegrees) { ParallelogramShape(angleDegrees) }
    Surface(
        modifier = modifier,
        shape = shape,
        color = if (enabled) containerColor else containerColor.copy(alpha = 0.6f),
        contentColor = contentColor,
        border = if (borderWidth > 0.dp) BorderStroke(borderWidth, borderColor) else null,
        elevation = 0.dp,
    ) {
        Box(
            modifier = Modifier,
            contentAlignment = Alignment.Center
        ) {
            content()
        }
    }
}

@Composable
fun PixelatedImageWithShader(
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
        val skiaBitmap = remember {
            painter.intrinsicSize
            val imageBitmap = painter.toImageBitmap(fitInside(painter.intrinsicSize, Size(widthPx, heightPx)), density)
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

        Canvas(modifier = Modifier
        ) {
            drawIntoCanvas { canvas ->
                val paint = Paint().apply {
                    this.shader = shader
                }
                canvas.nativeCanvas.drawRect(Rect.makeXYWH(0f, 0f, skiaBitmap.width.toFloat(), skiaBitmap.height.toFloat()), paint)
            }
        }
    }
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

fun fitInside(
    original: Size,
    maxSize: Size
): Size {
    val scale = min(
        maxSize.width / original.width,
        maxSize.height
            / original.height
    )
    return Size(
        (original.width  * scale),
        (original.height * scale)
    )
}
