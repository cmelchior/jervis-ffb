package com.jervisffb.ui.game.view

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.hoverable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsHoveredAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
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
import androidx.compose.ui.graphics.painter.BitmapPainter
import androidx.compose.ui.input.pointer.PointerEventType
import androidx.compose.ui.input.pointer.onPointerEvent
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.jervisffb.engine.model.Direction
import com.jervisffb.engine.model.FieldSquare
import com.jervisffb.engine.model.locations.FieldCoordinate
import com.jervisffb.engine.rules.Rules
import com.jervisffb.jervis_ui.generated.resources.Res
import com.jervisffb.jervis_ui.generated.resources.jervis_brush_chalk
import com.jervisffb.ui.game.icons.IconFactory
import com.jervisffb.ui.game.model.UiFieldSquare
import com.jervisffb.ui.game.view.animation.AnimationLayer
import com.jervisffb.ui.game.viewmodel.FieldDetails
import com.jervisffb.ui.game.viewmodel.FieldViewModel
import org.jetbrains.compose.resources.imageResource
import org.jetbrains.compose.resources.painterResource

/**
 * Rendering the field is pretty complex due to how many things that can potentially happen
 * at the same time.
 *
 * Drawing the field contains the following different layers. From the bottom up:
 *
 * // Environment background
 * 1. Background image: E.g., field or room type.
 * 2. Square separators + lines + area overlay. E.g. dots between squares, chalk lines or
 *    end-zone markings. In some cases this is merged with the background image.
 * 3. Weather effects. E.g., snow, rain, wind, fog, etc. In some cases this is merged with the
 *    background image.
 *
 * // UI Feedback
 * 4. Square available for selection: Highlight field if it can be selected.
 * 5. Field Underlay: Highlight field when hovering over a square with a mouse.
 *
 * // Environment features
 * 6. Room features: For Dungeon Bowl / Gutter Bowl. This includes statues or pits.
 * 7. Trapdoors: In some cases Trapdoors could be placed on top of room features like bridges.
 *
 * // Players
 * // Since players come in different sizes, we need to use absolute positioning. This is done
 * // based on the offset of field squares as well as the calculated size for them. When using
 * // `dp` there is a small chance of rounding errors. Something to watch out for.
 * 8. Player: Render player including anything markers, like ball carried, bomb carried etc.
 * 9. Ball: If a ball or bomb is loose, it is rendered on top of the player
 *
 * // Action "dialogs"
 * 10. Direction arrows: These are rendered on top of everything
 * 11. Animation Layer: This is where animations run (is this true)
 * 12. Dice Rolls / Dialogs: These are rendered on top of everything
 *
 * Developer's Commentary:
 * I am still thinking about these layers. Maybe some of the lower layers needs to change order?
 * Especially when it comes to weather effects, then it isn't clear exactly how these should be
 * rendered. Adding them on top of another field image, is more flexible, but it is unclear if
 * it will look good? Also, corner cases might surface while implementing features that require
 * us to rework them.
 */
@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun Field(
    modifier: Modifier,
    vm: FieldViewModel,
) {
    val field: FieldDetails by vm.field.collectAsState(FieldDetails.NICE)
    val flow = remember { vm.observeField() }
    val fieldData: Map<FieldCoordinate, UiFieldSquare> by flow.collectAsState(emptyMap())

    // TODO For now just center the field and add a black background to hide any rounding errors.
    //  Ideally that should only be a pixel or two.
    BoxWithConstraints(
        modifier = modifier.fillMaxWidth().background(color = JervisTheme.rulebookGreen.copy(alpha = 0.0f)).padding(start = 32.dp, end = 32.dp, bottom = 32.dp),
        contentAlignment = Alignment.TopCenter,
    ) {

        // Calculate maximum square size based on maximum width
        // If we neeed to render field markers, we add padding for it, so we can
        // draw the borders without them interacting with field squares or the end zone.
        val borderBrushSize = 3.dp
        val borderBrushPx = if (field.drawFieldMarkers) with(LocalDensity.current) { borderBrushSize.toPx().toInt() } else 0
        val maxWidthPx = with(LocalDensity.current) { maxWidth.toPx() }
        val squareSize = ((maxWidthPx - borderBrushPx*2) / vm.rules.fieldWidth).toInt() // Must be smaller than maxWidth
        val adjustedWidth = vm.rules.fieldWidth * squareSize + borderBrushPx*2
        val adjustedHeight = vm.rules.fieldHeight * squareSize + borderBrushPx*2

        ExactPixelBox(
            modifier = Modifier
                .onGloballyPositioned { coordinates ->
                    vm.updateFieldOffSet(coordinates)
                }
                .onPointerEvent(PointerEventType.Exit) {
                    vm.exitHover()
                }
            ,
            widthPx = adjustedWidth,
            heightPx = adjustedHeight
        ) {
            // Wrap content in a box to make it easier to lay them out normally.
            Box(modifier = Modifier.fillMaxSize()) {
                BackgroundImageLayer(field)
                // Field markers have to account for padding itself since the border is part of it.
                if (field.drawFieldMarkers) {
                    FieldMarkerLayer(
                        rules = vm.rules,
                        brushWidth = borderBrushSize,
                        chalkAlpha = 0.6f
                    )
                }

                // All other content is assumed to be "inside" the border.
                Box(modifier = Modifier.fillMaxSize().padding(borderBrushSize)) {
                    FieldUnderlay(vm)
                    FieldData(vm, fieldData)
                    AnimationLayer(vm)
                }
            }
        }
    }
}

// Field Marker Layer (2). This contains end zones, square seperators and chalk lines.
@Composable
private fun BoxScope.FieldMarkerLayer(
    rules: Rules,
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
        val squareSize = size.width / rules.fieldWidth
        val wideZonePathEffect = PathEffect.dashPathEffect(
            floatArrayOf(squareSize * 2f, squareSize),
            squareSize
        )

        // Border around the  field
        drawRect(
            brush = imageBrush,
            topLeft = Offset(0f + chalkBrushSize/2, chalkBrushSize/2),
            style = androidx.compose.ui.graphics.drawscope.Stroke(width = chalkBrushSize, join = StrokeJoin.Miter),
            alpha = chalkAlpha,
            size = Size(size.width - chalkBrushSize, size.height - chalkBrushSize)
        )

        // Line of Scrimmage
        if (rules.lineOfScrimmageHome + 1 == rules.lineOfScrimmageAway) {
            // Single line of scrimmage
            drawLine(
                brush = imageBrush,
                start = Offset(rules.lineOfScrimmageAway * squareSize, chalkBrushSize),
                end = Offset(rules.lineOfScrimmageAway * squareSize, size.height - chalkBrushSize),
                strokeWidth = chalkBrushSize,
                alpha = chalkAlpha
            )
        } else {
            drawLine(
                brush = imageBrush,
                start = Offset((rules.lineOfScrimmageHome + 1) * squareSize, chalkBrushSize),
                end = Offset((rules.lineOfScrimmageHome + 1) * squareSize, size.height - chalkBrushSize),
                strokeWidth = chalkBrushSize,
                alpha = chalkAlpha
            )
            drawLine(
                brush = imageBrush,
                start = Offset(rules.lineOfScrimmageAway * squareSize, chalkBrushSize),
                end = Offset(rules.lineOfScrimmageAway * squareSize, size.height - chalkBrushSize),
                strokeWidth = chalkBrushSize,
                alpha = chalkAlpha
            )
        }

        // Wide Zone - Top (from end zone to line of scrimmage)
        drawLine(
            brush = imageBrush,
            start = Offset(
                rules.endZone * squareSize + chalkBrushSize,
                rules.wideZone * squareSize
            ),
            end = Offset(
                (rules.lineOfScrimmageHome + 1) * squareSize - chalkBrushSize/2,
                rules.wideZone * squareSize
            ),
            strokeWidth = chalkBrushSize,
            pathEffect = wideZonePathEffect,
            alpha = chalkAlpha
        )
        drawLine(
            brush = imageBrush,
            start = Offset(
                (rules.fieldWidth - rules.endZone) * squareSize - chalkBrushSize,
                rules.wideZone * squareSize
            ),
            end = Offset(
                (rules.lineOfScrimmageAway) * squareSize + chalkBrushSize/2,
                rules.wideZone * squareSize
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
                (rules.fieldHeight - rules.wideZone) * squareSize
            ),
            end = Offset(
                (rules.lineOfScrimmageHome + 1) * squareSize - chalkBrushSize/2,
                (rules.fieldHeight - rules.wideZone) * squareSize
            ),
            strokeWidth = chalkBrushSize,
            pathEffect = wideZonePathEffect,
            alpha = chalkAlpha
        )
        drawLine(
            brush = imageBrush,
            start = Offset(
                (rules.fieldWidth - rules.endZone) * squareSize - chalkBrushSize,
                (rules.fieldHeight - rules.wideZone) * squareSize
            ),
            end = Offset(
                (rules.lineOfScrimmageAway) * squareSize + chalkBrushSize/2,
                (rules.fieldHeight - rules.wideZone) * squareSize
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

// Most bottom layer (1). This is the background image for the field
@Composable
fun BoxScope.BackgroundImageLayer(field: FieldDetails) {
    Image(
        painter = BitmapPainter(IconFactory.getField(field)),
        contentDescription = field.description,
        // We live with something of the field being cropped for now.
        // Avoiding it across game types is hard, and if we want something
        // that also works across custom field sizes, it turns borderline
        // impossible.
        contentScale = ContentScale.Crop,
        alignment = Alignment.Center,
        modifier =
            Modifier
                .fillMaxSize()
                .align(Alignment.TopStart),
    )
}


@Composable
fun FieldSquares(
    vm: FieldViewModel,
    content: @Composable (modifier: Modifier, x: Int, y: Int) -> Unit,
) {
    Column(
        modifier =
            Modifier
                .fillMaxSize()
    ) {
        repeat(vm.height) { height: Int ->
            Row(
                modifier =
                    Modifier
                        .fillMaxSize()
                        .weight(1f),
            ) {
                repeat(vm.width) { width ->
                    val boxModifier = Modifier
                        .fillMaxSize()
                        // Do not set `aspectRatio` as it will resolve in small rounding errors
                        // that make squares not line up perfectly. Instead rely on the container
                        // having the correct aspect ratio that should cause all squares to be
                        // as close to uniform as possible.
                        .weight(1f)
                        .onGloballyPositioned { coordinates ->
                            vm.updateOffset(FieldCoordinate(width, height), coordinates)
                        }
                    content(boxModifier, width, height)
                }
            }
        }
    }
}

@Composable
fun FieldData(
    vm: FieldViewModel,
    fieldData: Map<FieldCoordinate, UiFieldSquare>,
) {
    // Players/Ball
    FieldSquares(vm) { modifier, x, y ->
        val squareData: UiFieldSquare? = fieldData[FieldCoordinate(x, y)]
        FieldSquare(
            modifier,
            x,
            y,
            vm,
            squareData ?: UiFieldSquare(FieldSquare(-1, -1)),
        )
    }
}


@Composable
fun FieldUnderlay(vm: FieldViewModel) {
    val highlightedSquare: FieldCoordinate? by vm.highlights().collectAsState()
    FieldSquares(vm) { modifier: Modifier, x, y ->
        val hover = (highlightedSquare?.x == x && highlightedSquare?.y == y)
        val bgColor =
            when {
                hover -> Color.Cyan.copy(alpha = 0.25f)
                else -> Color.Transparent
            }
        Box(modifier = modifier.background(bgColor))
    }
}

@OptIn(ExperimentalComposeUiApi::class)
@Composable
private fun FieldSquare(
    boxModifier: Modifier,
    width: Int,
    height: Int,
    vm: FieldViewModel,
    square: UiFieldSquare,
) {
    val bgColor = when {
        square.onSelected != null && square.requiresRoll -> Color.Yellow.copy(alpha = 0.25f)
        square.selectableDirection != null || square.directionSelected != null -> Color.Transparent // Hide square color
        square.player?.isSelectable == true -> Color.Transparent
        square.onSelected != null -> Color.Green.copy(alpha = 0.25f) // Fallback for active squares
        else -> Color.Transparent
    }

    val modifier = boxModifier
        .fillMaxSize()
        .background(color = bgColor)
        .onPointerEvent(PointerEventType.Enter) {
            vm.hoverOver(FieldCoordinate(width, height))
// Prototype: Dashed box around "active" square.
//        }.applyIf(square.showContextMenu.value) {
//            this.drawBehind {
//                val strokeWidth = 2.dp.toPx()
//                val cornerRadius = 8.dp.toPx()
//                val dashLength = 4.dp.toPx()
//                val gapLength  = 2.dp.toPx()
//                val effect = PathEffect.dashPathEffect(floatArrayOf(dashLength, gapLength), 0f)
//                val inset = strokeWidth / 2f
//                val topLeft = Offset(inset, inset)
//                val size = Size(size.width - strokeWidth, size.height - strokeWidth)
//
//                drawRoundRect(
//                    color = Color.White.copy(alpha = 0.8f),
//                    topLeft = topLeft,
//                    size = size,
//                    cornerRadius = CornerRadius(cornerRadius, cornerRadius),
//                    style = Stroke(width = strokeWidth, pathEffect = effect)
//                )
//            }
        }

    val boxWrapperModifier =
        if (square.contextMenuOptions.isNotEmpty() || square.onSelected != null || square.hoverAction != null) {
            modifier.clickable {
                square.showContextMenu.value = !square.showContextMenu.value
                if (square.hoverAction != null) {
                    square.hoverAction!!()
                } else if (square.onSelected != null) {
                    square.onSelected!!()
                }
            }
        } else {
            modifier
        }

    Box(modifier = boxWrapperModifier) {
        if (square.showContextMenu.value && !square.useActionWheel) {
            ContextPopupMenu(
                hidePopup = { dismissed ->
                    square.showContextMenu.value = false
                    if (dismissed)  {
                        square.onMenuHidden?.invoke()
                    }
                },
                commands = square.contextMenuOptions,
            )
        }
        if (square.isBallOnGround || square.isBallExiting) {
            "".any { it.isLetterOrDigit()}
            Image(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(4.dp)
                    .background(color = if (square.isBallExiting) Color.Red else Color.Transparent),
                alignment = Alignment.Center,
                contentScale = ContentScale.FillBounds,
                bitmap = IconFactory.getBall(),
                contentDescription = "",
            )
        } else if (square.futureMoveValue != null && square.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text(
                    text = square.futureMoveValue.toString(),
                    style = JervisTheme.fieldSquareTextStyle.copy(
                        color = Color.White.copy(0.75f)
                    ),
                )
            }
        } else if (square.moveUsed != null) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text(
                    text = square.moveUsed.toString(),
                    style = JervisTheme.fieldSquareTextStyle.copy(
                        color = Color.Cyan.copy(0.75f)
                    )
                )
            }
        }

        square.player?.let {
            Player(
                boxModifier,
                it,
                true,
                square.showContextMenu.value // && !square.useActionWheel
            )
        }
        square.directionSelected?.let {
            DictionImage(it, interactive = false)
        }
        square.selectableDirection?.let {
            DictionImage(it, interactive = true)
        }
        if (square.dice != 0) {
            BlockDiceIndicatorImage(square.dice)
        }
        if (square.isBlocked) {
            PlayerBlockedIndicator()
        }
    }
}

@Composable
fun DictionImage(direction: Direction, interactive: Boolean) {
    val interactionSource = remember { MutableInteractionSource() }
    val isHovered by interactionSource.collectIsHoveredAsState()
    val imageRes = IconFactory.getDirection(direction, if (interactive) isHovered else true)
    val modifier = if (interactive) {
        Modifier.fillMaxSize().hoverable(interactionSource = interactionSource)
    } else {
        Modifier.fillMaxSize()
    }
    Image(
        modifier = modifier,
        painter = painterResource(imageRes),
        contentDescription = null,
    )
}

/**
 * @param dice If negative, it means the defender has more strength. If positive,
 * it means the attacker has more strength.
 */
@Composable
fun BlockDiceIndicatorImage(dice: Int) {
//    val interactionSource = remember { MutableInteractionSource() }
//    val isHovered by interactionSource.collectIsHoveredAsState()
    val imageRes = remember(dice) { IconFactory.getBlockDiceRolledIndicator(dice) }
    Image(
        modifier = Modifier.fillMaxSize() /* .hoverable(interactionSource = interactionSource) */,
        painter = painterResource(imageRes),
        // painter = if (isHovered) painterResource(imageRes) else ColorPainter(Color.Transparent),
        contentDescription = null,
    )
}

@Composable
fun PlayerBlockedIndicator() {
    val imageRes = IconFactory.getBlockedDecoration()
    Image(
        modifier = Modifier.fillMaxSize(),
        painter = painterResource(imageRes),
        contentDescription = null,
    )
}

@Composable
private fun ExactPixelBox(modifier: Modifier, widthPx: Int, heightPx: Int, content: @Composable () -> Unit) {
    Layout(
        content = content,
        modifier = modifier,
    ) { measurables, constraints  ->
        val placeables = measurables.map {
            it.measure(Constraints.fixed(widthPx, heightPx))
        }
        layout(widthPx, heightPx) {
            placeables.forEach {
                it.place(0, 0)
            }
        }
    }
}

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
