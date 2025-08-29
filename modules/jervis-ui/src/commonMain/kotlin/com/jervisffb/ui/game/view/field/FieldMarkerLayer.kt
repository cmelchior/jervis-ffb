package com.jervisffb.ui.game.view.field

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageShader
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.ShaderBrush
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.TileMode
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.jervisffb.engine.rules.Rules
import com.jervisffb.jervis_ui.generated.resources.Res
import com.jervisffb.jervis_ui.generated.resources.jervis_brush_chalk
import com.jervisffb.ui.game.view.JervisTheme
import org.jetbrains.compose.resources.imageResource

/**
 * Layer 2: Background Image Layer.
 *
 * This contains the end zones, square separators and chalk lines.
 *
 * See [Field] for more details about layer ordering.
 */
@Composable
fun BoxScope.FieldMarkerLayer(
    rules: Rules,
    fieldDataSize: FieldSizeData,
    brushWidth: Dp = 3.dp,
    chalkAlpha: Float = 0.6f,
) {
    // Draw field square corners. Avoid drawing corners close to
    // edges or lines separating sections of the field.
    Column(
        modifier =
            Modifier
                .padding(brushWidth)
                .fillMaxSize()
    ) {
        repeat(rules.fieldHeight) { y: Int ->
            Row(
                modifier =
                    Modifier
                        .fillMaxSize()
                        .weight(1f),
            ) {
                repeat(rules.fieldWidth) { x ->
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .weight(1f)
                            .cornerSquares(
                                1.dp,
                                topLeft = (
                                    y > 0
                                        && x > rules.endZone
                                        && x < rules.fieldWidth - rules.endZone
                                        && x != rules.lineOfScrimmageAway
                                        && x != rules.lineOfScrimmageHome + 1
                                        && y != rules.fieldHeight - rules.wideZone
                                        && y != rules.wideZone
                                ),
                                topRight = (
                                    y > 0
                                        && x < rules.fieldWidth - rules.endZone - 1
                                        && x >= rules.endZone
                                        && x != rules.lineOfScrimmageHome
                                        && x != rules.lineOfScrimmageAway - 1
                                        && y != rules.fieldHeight - rules.wideZone
                                        && y != rules.wideZone
                                ),
                                bottomLeft = (
                                    y < rules.fieldHeight - 1
                                        && x > rules.endZone
                                        && x < rules.fieldWidth - rules.endZone
                                        && x != rules.lineOfScrimmageAway
                                        && x != rules.lineOfScrimmageHome + 1
                                        && y != rules.fieldHeight - rules.wideZone - 1
                                        && y != rules.wideZone - 1
                                ),
                                bottomRight = (
                                    y < rules.fieldHeight - 1
                                        && x < rules.fieldWidth - rules.endZone - 1
                                        && x >= rules.endZone
                                        && x != rules.lineOfScrimmageHome
                                        && x != rules.lineOfScrimmageAway - 1
                                        && y != rules.fieldHeight - rules.wideZone - 1
                                        && y != rules.wideZone - 1
                                )
                            )
                    )
                }
            }
        }
    }

    // Draw "chalk lines"
    val chalkTexture = imageResource(Res.drawable.jervis_brush_chalk)
    val imageBrush = remember(chalkTexture) {
        ShaderBrush(
            shader = ImageShader(
                image = chalkTexture,
                tileModeX = TileMode.Repeated,
                tileModeY = TileMode.Repeated,
            ),
        )
    }

    // All chalk lines
    Box(modifier = Modifier.fillMaxSize().drawWithContent {
        val chalkBrushSize = brushWidth.toPx()
        val squareSize = fieldDataSize.squareSize.width.toFloat() // Assumes square field
        val wideZonePathEffect = PathEffect.dashPathEffect(
            floatArrayOf(squareSize * 2f, squareSize),
            squareSize
        )

        // Border around the  field
        drawRect(
            brush = imageBrush,
            topLeft = Offset(0f + chalkBrushSize/2, chalkBrushSize/2),
            style = Stroke(width = chalkBrushSize, join = StrokeJoin.Miter),
            alpha = chalkAlpha,
            size = Size(size.width - chalkBrushSize, size.height - chalkBrushSize)
        )

        // Line of Scrimmage
        if (rules.lineOfScrimmageHome + 1 == rules.lineOfScrimmageAway) {
            // Single line of scrimmage
            drawLine(
                brush = imageBrush,
                start = Offset(rules.lineOfScrimmageAway * squareSize + chalkBrushSize, chalkBrushSize),
                end = Offset(rules.lineOfScrimmageAway * squareSize + chalkBrushSize, size.height - chalkBrushSize),
                strokeWidth = chalkBrushSize,
                alpha = chalkAlpha
            )
        } else {
            drawLine(
                brush = imageBrush,
                start = Offset((rules.lineOfScrimmageHome + 1) * squareSize + chalkBrushSize, chalkBrushSize),
                end = Offset((rules.lineOfScrimmageHome + 1) * squareSize + chalkBrushSize, size.height - chalkBrushSize),
                strokeWidth = chalkBrushSize,
                alpha = chalkAlpha
            )
            drawLine(
                brush = imageBrush,
                start = Offset(rules.lineOfScrimmageAway * squareSize + chalkBrushSize, chalkBrushSize),
                end = Offset(rules.lineOfScrimmageAway * squareSize + chalkBrushSize, size.height - chalkBrushSize),
                strokeWidth = chalkBrushSize,
                alpha = chalkAlpha
            )
        }

        // Wide Zone - Top (from end zone to line of scrimmage)
        drawLine(
            brush = imageBrush,
            start = Offset(
                rules.endZone * squareSize + chalkBrushSize,
                rules.wideZone * squareSize + chalkBrushSize
            ),
            end = Offset(
                (rules.lineOfScrimmageHome + 1) * squareSize - chalkBrushSize/2,
                rules.wideZone * squareSize + chalkBrushSize
            ),
            strokeWidth = chalkBrushSize,
            pathEffect = wideZonePathEffect,
            alpha = chalkAlpha
        )
        drawLine(
            brush = imageBrush,
            start = Offset(
                (rules.fieldWidth - rules.endZone) * squareSize - chalkBrushSize,
                rules.wideZone * squareSize + chalkBrushSize
            ),
            end = Offset(
                (rules.lineOfScrimmageAway) * squareSize + chalkBrushSize/2,
                rules.wideZone * squareSize + chalkBrushSize
            ),
            strokeWidth = chalkBrushSize,
            pathEffect = wideZonePathEffect,
            alpha = chalkAlpha
        )

        // Wide Zone - Bottom (from end zone to line of scrimmage)
        drawLine(
            brush = imageBrush,
            start = Offset(
                rules.endZone * squareSize + chalkBrushSize,
                (rules.fieldHeight - rules.wideZone) * squareSize + chalkBrushSize
            ),
            end = Offset(
                (rules.lineOfScrimmageHome + 1) * squareSize - chalkBrushSize/2,
                (rules.fieldHeight - rules.wideZone) * squareSize + chalkBrushSize
            ),
            strokeWidth = chalkBrushSize,
            pathEffect = wideZonePathEffect,
            alpha = chalkAlpha
        )
        drawLine(
            brush = imageBrush,
            start = Offset(
                (rules.fieldWidth - rules.endZone) * squareSize - chalkBrushSize,
                (rules.fieldHeight - rules.wideZone) * squareSize + chalkBrushSize
            ),
            end = Offset(
                (rules.lineOfScrimmageAway) * squareSize + chalkBrushSize/2,
                (rules.fieldHeight - rules.wideZone) * squareSize + chalkBrushSize
            ),
            strokeWidth = chalkBrushSize,
            pathEffect = wideZonePathEffect,
            alpha = chalkAlpha
        )
    })

    // Draw End Zone markers
    Box(modifier = Modifier
        .align(Alignment.TopStart)
        .padding(brushWidth)
        .fillMaxWidth(rules.endZone/rules.fieldWidth.toFloat())
        .fillMaxHeight()
        .checkerboardBackground(
            widthSquares = rules.endZone,
            heightSquares = rules.fieldHeight,
        )
    )
    Box(modifier = Modifier
        .align(Alignment.TopEnd)
        .padding(brushWidth)
        .fillMaxWidth(rules.endZone/rules.fieldWidth.toFloat())
        .fillMaxHeight()
        .checkerboardBackground(
            widthSquares = rules.endZone,
            heightSquares = rules.fieldHeight,
        )
    )
}

// Draw corner "indicators" on a square. Each square is responsible for rendering
// their part of it. So the size should be the same across the entire field.
private fun Modifier.cornerSquares(
    squareSize: Dp,
    color: Color = JervisTheme.white.copy(0.9f),
    topLeft: Boolean = true,
    topRight: Boolean = true,
    bottomLeft: Boolean = true,
    bottomRight: Boolean = true,
) = this.then(
    Modifier.drawBehind {
        val sizePx = squareSize.toPx()
        val paint = SolidColor(color)
        val square = Size(sizePx, sizePx)
        if (topLeft) drawRect(paint, Offset(0f, 0f), square)
        if (topRight) drawRect(paint, Offset(size.width - sizePx, 0f), square)
        if (bottomLeft) drawRect(paint, Offset(0f, size.height - sizePx), square)
        if (bottomRight) drawRect(paint, Offset(size.width - sizePx, size.height - sizePx), square)
    }
)

// Create the checkerboard background used by the end zones.
private fun Modifier.checkerboardBackground(
    squaresPrFieldSquare: Int = 3,
    widthSquares: Int,
    heightSquares: Int,
    color1: Color = JervisTheme.white.copy(alpha = 0.15f),
    color2: Color = JervisTheme.rulebookRed.copy(alpha = 0.15f),
): Modifier = this.then(
    Modifier.drawWithContent {
        drawContent()
        // We are pre-calculated width/height, so we are 100% sure that height/width
        // is always having an aspect ratio of 1.0.
        val squareWidthPx = size.width / (squaresPrFieldSquare * widthSquares)
        val squareHeightPx = size.height / (squaresPrFieldSquare * heightSquares)
        val rows = (size.height / squareHeightPx).toInt()
        val cols = (size.width / squareWidthPx).toInt()

        for (row in 0 until rows) {
            for (col in 0 until cols) {
                val isEven = (row + col) % 2 == 0
                drawRect(
                    color = if (isEven) color1 else color2,
                    topLeft = Offset(col * squareWidthPx, row * squareHeightPx),
                    size = Size(squareWidthPx, squareHeightPx)
                )
            }
        }
    }
)
