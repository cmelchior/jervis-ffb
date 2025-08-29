package com.jervisffb.ui.game.view.field

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.PointerEventType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import com.jervisffb.engine.model.locations.FieldCoordinate
import com.jervisffb.ui.game.view.animation.AnimationLayer
import com.jervisffb.ui.game.viewmodel.FieldDetails
import com.jervisffb.ui.game.viewmodel.FieldViewModel
import com.jervisffb.ui.menu.LocalFieldDataWrapper
import com.jervisffb.ui.utils.pixelSize

val LocalFieldData = staticCompositionLocalOf<LocalFieldDataWrapper> {
    error("CompositionLocal LocalFieldData not present")
}

/**
 * Rendering the field is pretty complex due to how many things that can potentially happen
 * at the same time.
 *
 * Drawing the field involves rendering the following different "layers". From the bottom up:
 *
 * ** Environment background **
 * 1. Background image: E.g., field or room type.
 * 2. Square separators + lines + area overlay. E.g. dots between squares, chalk lines or
 *    end-zone markings. In some cases this is merged with the background image.
 * 3. Weather effects. E.g., snow, rain, wind, fog, etc. In some cases this is merged with the
 *    background image.
 *
 * ** UI Feedback **
 * 4. Field Actions and Underlay: This layer is responsible attaching action handlers and highlights
*     for static effects like fields available for actions, tackle zones or yellow colors for fields
 *    that require a dodge.
 * 5. Hover Underlay: Highlights for dynamic effects like when a mouse hovers over the field
 *
 * ** Environment features **
 * 6. Room features: For Dungeon Bowl / Gutter Bowl. This includes statues or pits.
 * 7. Trapdoors: In some cases Trapdoors could be placed on top of room features like bridges.
 *
 * ** Players **
 * Since players come in different sizes, we need to use absolute positioning. This is done
 * based on the offset of field squares as well as the calculated size for them. When using
 * `dp` there is a small chance of rounding errors. Something to watch out for.
 *
 * 8. Player: Render player including anything markers, like ball carried, bomb carried etc.
 * 9. Ball: If a ball or bomb is loose, it is rendered on top of the player
 *
 * ** Action dialogs **
 * 10. Direction arrows: These are rendered on top of everything
 * 11. Animation Layer: This is where animations run (is this true)
 * 12. Dialog Layer: These are controlled outside the scope of this field.
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
    val field: FieldDetails by vm.fieldBackground.collectAsState(FieldDetails.NICE)

    // TODO For now just center the field and add a black background to hide any rounding errors.
    //  Ideally that should only be a pixel or two.
    BoxWithConstraints(
        modifier = modifier
            .fillMaxWidth()
            .background(color = Color.Transparent)
        ,
        contentAlignment = Alignment.TopCenter,
    ) {
        val borderBrushSize = 3.dp
        val localDensity = LocalDensity.current
        val fieldDataSize = remember(maxWidth, maxHeight) {
            // Calculate maximum square size based on maximum width
            // If we need to render field markers, we add padding for it, so we can
            // draw the borders without them interacting with field squares or the end zone.
            val borderBrushPx = if (field.drawFieldMarkers) with(localDensity) { borderBrushSize.toPx().toInt() } else 0
            val maxWidthPx = with(localDensity) { maxWidth.toPx() }
            val squareSize = ((maxWidthPx - borderBrushPx*2) / vm.rules.fieldWidth).toInt() // Must be smaller than maxWidth
            FieldSizeData(
                borderBrushSizePx = borderBrushPx,
                squareSize = IntSize(squareSize, squareSize),
                squaresPrRow = vm.rules.fieldWidth,
                squaresPrColumn = vm.rules.fieldHeight,
            )
        }
        LaunchedEffect(fieldDataSize) {
            vm.sharedFieldData.size = fieldDataSize
        }
        CompositionLocalProvider(LocalFieldData provides vm.sharedFieldData) {
            val localField = LocalFieldData.current
            if (localField.size.squareSize == IntSize.Zero) return@CompositionLocalProvider
            ExactPixelBox(
                modifier = Modifier
                    .pointerInput(localField.size) {
                        val pointerBus = localField.pointerBus
                        val fieldSizeData = localField.size
                        awaitPointerEventScope {
                            while (true) {
                                val e = awaitPointerEvent()
                                val eventSquare = e.changes.first().position.toFieldSquare(fieldSizeData)
                                when (e.type) {
                                    PointerEventType.Move  -> {
                                        if (eventSquare != null) {
                                            pointerBus.notifyMove(eventSquare)
                                        }
                                    }
                                    PointerEventType.Enter -> {
                                        if (eventSquare != null) {
                                            pointerBus.notifyEnterField(eventSquare)
                                        }
                                    }
                                    PointerEventType.Exit  -> {
                                        pointerBus.notifyExitField()
                                        vm.triggerHoverExit()
                                    }
                                    PointerEventType.Press -> {
                                        if (eventSquare != null) {
                                            pointerBus.notifyPressSquare(eventSquare)
                                        }
                                    }
                                    PointerEventType.Release -> {
                                        pointerBus.notifyReleaseSquare(eventSquare)
                                    }
                                }
                            }
                        }
                    }
                    .onGloballyPositioned { coordinates ->
                        vm.updateFieldOffSet(coordinates)
                    }
                ,
                widthPx = localField.size.totalFieldWidthPx,
                heightPx = localField.size.totalFieldHeightPx
            ) {
                // Wrap content in a box to make it easier to lay them out normally.
                Box(modifier = Modifier.fillMaxSize()) {
                    BackgroundImageLayer(field)
                    // Field markers have to account for padding itself since the border is part of it.
                    if (field.drawFieldMarkers) {
                        FieldMarkerLayer(
                            rules = vm.rules,
                            fieldDataSize = fieldDataSize,
                            brushWidth = borderBrushSize,
                            chalkAlpha = 0.6f
                        )
                    }

                    // All other content is assumed to be "inside" the border.
                    // TODO This might not be true, players and ball can leave this area
                    Box(modifier = Modifier.fillMaxSize().padding(borderBrushSize)) {
                        FieldActionsAndUnderlaysLayers(vm)
                        FieldHoverUnderlayLayer(vm)
                        WeatherEffectsLayer(vm)
                        RoomFeaturesLayer(vm)
                        TrapdoorsLayer(vm)
                        PlayerLayer(vm)
                        BallLayer(vm)
                        DirectionArrowsLayer(vm)
                        AnimationLayer(vm)
                    }
                }
            }
        }
    }
}

/**
 * Composable that draws a pixel-perfect box with the given width and height.
 * This is used to draw the field, so we avoid sub-pixel rendering artifacts
 * due to rounding in the individual squares.
 */
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


/**
 * Modifier that sets the size and position of the Composable so it matches a square on the field.
 */
fun Modifier.Companion.jervisSquare(fieldSizeData: FieldSizeData, square: FieldCoordinate): Modifier {
    return this
        .pixelSize(fieldSizeData.squareSize)
        .offset {
            IntOffset(
                x = square.x * fieldSizeData.squareSize.width,
                y = square.y * fieldSizeData.squareSize.height
            )
        }
}

/**
 * Composable that handle drawing the individual squares on the field and callback
 * to the parent to fill them out.
 */
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
                        // that make squares not line up perfectly. Instead, rely on the container
                        // having the correct aspect ratio. This should cause all squares to be
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

//@Composable
//fun FieldSquaresLayer(
//    vm: FieldViewModel,
//    fieldData: Map<FieldCoordinate, Pair<UiFieldSquare, UiPlayer?>>,
//    fieldSizeData: FieldSizeData,
//) {
//    // Players/Ball
//    FieldSquares(vm) { modifier, x, y ->
//        val squareData: UiFieldSquare? = fieldData[FieldCoordinate(x, y)]?.first
//        val playerData: UiPlayer? = fieldData[FieldCoordinate(x, y)]?.second
//        FieldSquare(
//            modifier,
//            x,
//            y,
//            vm,
//            squareData ?: UiFieldSquare(FieldSquare(-1, -1)),
//            playerData,
//            null,  // TODO We need to also pass that in here
//        )
//    }
//}




//@OptIn(ExperimentalComposeUiApi::class)
//@Composable
//private fun FieldSquare(
//    boxModifier: Modifier,
//    width: Int,
//    height: Int,
//    vm: FieldViewModel,
//    square: UiFieldSquare,
//    player: UiPlayer? = null,
//    playerTransientData: UiPlayerTransientData? = null,
//) {
//    val bgColor = when {
//        square.onSelected != null && square.requiresRoll -> Color.Yellow.copy(alpha = 0.25f)
//        square.selectableDirection != null || square.directionSelected != null -> Color.Transparent // Hide square color
//        player?.isSelectable == true -> Color.Transparent
//        square.onSelected != null -> JervisTheme.availableActionBackground // Fallback for active squares
//        else -> Color.Transparent
//    }
//
//    val modifier = boxModifier
//        .fillMaxSize()
//        .background(color = bgColor)
//        .onPointerEvent(PointerEventType.Enter) {
//            vm.triggerHoverEnter(FieldCoordinate(width, height))
//// Prototype: Dashed box around "active" square.
////        }.applyIf(square.showContextMenu.value) {
////            this.drawBehind {
////                val strokeWidth = 2.dp.toPx()
////                val cornerRadius = 8.dp.toPx()
////                val dashLength = 4.dp.toPx()
////                val gapLength  = 2.dp.toPx()
////                val effect = PathEffect.dashPathEffect(floatArrayOf(dashLength, gapLength), 0f)
////                val inset = strokeWidth / 2f
////                val topLeft = Offset(inset, inset)
////                val size = Size(size.width - strokeWidth, size.height - strokeWidth)
////
////                drawRoundRect(
////                    color = Color.White.copy(alpha = 0.8f),
////                    topLeft = topLeft,
////                    size = size,
////                    cornerRadius = CornerRadius(cornerRadius, cornerRadius),
////                    style = Stroke(width = strokeWidth, pathEffect = effect)
////                )
////            }
//        }
//
//    val boxWrapperModifier =
//        if (square.contextMenuOptions.isNotEmpty() || square.onSelected != null || vm.uiEphemeralData.squares[square.model.coordinates]?.hoverAction != null) {
//            modifier.clickable {
//                square.showContextMenu = !square.showContextMenu
//                vm.uiEphemeralData.squares[square.model.coordinates]?.let {
//                    it.hoverAction?.invoke()
//                } ?: square.onSelected?.invoke()
//                ?: error("Missing onSelected action: ${square.model.coordinates}")
//            }
//        } else {
//            modifier
//        }
//
//    Box(modifier = boxWrapperModifier) {
//        if (square.showContextMenu && !square.useActionWheel) {
//            ContextPopupMenu(
//                hidePopup = { dismissed ->
//                    square.showContextMenu = false
//                    if (dismissed) {
//                        square.onMenuHidden?.invoke()
//                    }
//                },
//                commands = square.contextMenuOptions,
//            )
//        }
//        if (square.isBallOnGround || square.isBallExiting) {
//            "".any { it.isLetterOrDigit()}
//            Image(
//                modifier = Modifier
//                    .fillMaxSize()
//                    .padding(4.dp)
//                    .background(color = if (square.isBallExiting) Color.Red else Color.Transparent),
//                alignment = Alignment.Center,
//                contentScale = ContentScale.FillBounds,
//                bitmap = IconFactory.getBall(),
//                contentDescription = "",
//            )
//        } else if (vm.uiEphemeralData.squares[square.model.coordinates]?.futureMoveValue != null && square.isEmpty()) {
//            val moveValue = vm.uiEphemeralData.squares[square.model.coordinates]?.futureMoveValue?.toString() ?: ""
//            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
//                Text(
//                    text = moveValue,
//                    style = JervisTheme.fieldSquareTextStyle.copy(
//                        color = Color.White.copy(0.75f)
//                    ),
//                )
//            }
//        } else if (square.moveUsed != null) {
//            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
//                Text(
//                    text = square.moveUsed.toString(),
//                    style = JervisTheme.fieldSquareTextStyle.copy(
//                        color = Color.Cyan.copy(0.75f)
//                    )
//                )
//            }
//        }
//
//        player?.let {
//            Player(
//                boxModifier,
//                player,
//                playerTransientData,
//                true,
//                square.showContextMenu // && !square.useActionWheel
//            )
//        }
//        square.directionSelected?.let {
//            DictionImage(it, interactive = false)
//        }
//        square.selectableDirection?.let {
//            DictionImage(it, interactive = true)
//        }
//        if (square.dice != 0) {
//            BlockDiceIndicatorImage(square.dice)
//        }
//        if (square.isBlocked) {
//            PlayerBlockedIndicator()
//        }
//    }
//}


