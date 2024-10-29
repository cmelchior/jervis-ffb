package com.jervisffb.ui.view

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.hoverable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsHoveredAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.BitmapPainter
import androidx.compose.ui.graphics.painter.ColorPainter
import androidx.compose.ui.input.pointer.PointerEventType
import androidx.compose.ui.input.pointer.onPointerEvent
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.jervisffb.engine.model.Direction
import com.jervisffb.engine.model.FieldSquare
import com.jervisffb.engine.model.locations.FieldCoordinate
import com.jervisffb.ui.icons.IconFactory
import com.jervisffb.ui.model.UiFieldSquare
import com.jervisffb.ui.view.animation.AnimationLayer
import com.jervisffb.ui.viewmodel.FieldDetails
import com.jervisffb.ui.viewmodel.FieldViewModel
import org.jetbrains.compose.resources.painterResource

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun Field(
    vm: FieldViewModel,
    modifier: Modifier,
) {
    val field: FieldDetails by vm.field().collectAsState()
    val flow = remember { vm.observeField() }
    val fieldData: Map<FieldCoordinate, UiFieldSquare> by flow.collectAsState(emptyMap())
    Box(
        modifier =
            modifier
                .fillMaxSize()
                .aspectRatio(vm.aspectRatio)
                .onGloballyPositioned { coordinates ->
                    vm.updateFieldOffSet(coordinates)
                }
                .onPointerEvent(PointerEventType.Exit) {
                    vm.exitHover()
                }
    ) {
        Image(
            painter = BitmapPainter(IconFactory.getField(field)),
            contentDescription = field.description,
            modifier =
                Modifier
                    .fillMaxSize()
                    .align(Alignment.TopStart),
        )
        FieldUnderlay(vm)
        FieldData(vm, fieldData)
        FieldOverlay(vm)
        AnimationLayer(vm)
    }
}

@Composable
fun FieldSquares(
    vm: FieldViewModel,
    content: @Composable (modifier: Modifier, x: Int, y: Int) -> Unit,
) {
    Column(
        modifier =
            Modifier
                .fillMaxSize(),
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

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun FieldOverlay(vm: FieldViewModel) {
    val flow = remember { vm.observeOverlays() }
    val pathInfo by flow.collectAsState(initial = null)
    FieldSquares(vm) { modifier: Modifier, x, y: Int ->
        val number = pathInfo?.pathSteps?.get(FieldCoordinate(x, y))
        val isTarget = pathInfo?.target == FieldCoordinate(x, y)
        val selectPathAction: (() -> Unit)? = pathInfo?.action
        var updatedModifier = modifier
        updatedModifier =
            if (isTarget) {
                updatedModifier
                    .onPointerEvent(PointerEventType.Enter) {
                        // The overlay needs to report onHover events when enabled
                        // because it is shadowing for the FieldData
                        vm.hoverOver(FieldCoordinate(x, y))
                    }
                    .clickable {
                        selectPathAction!!()
                    }
            } else {
                updatedModifier
            }
        Box(
            modifier = updatedModifier,
            contentAlignment = Alignment.Center,
        ) {
            if (number != null && (pathInfo?.path?.size ?: 0) > 1) {
                Text(text = number.toString(), color = Color.White, fontWeight = FontWeight.Bold, fontSize = 12.sp)
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
            squareData ?: UiFieldSquare(FieldSquare(-1, -1))
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
    var showPopup: Boolean by remember(square) { mutableStateOf(square.showContextMenu) }
    val bgColor by remember(square) {
        mutableStateOf(when {
            square.onSelected != null && square.requiresRoll -> Color.Yellow.copy(alpha = 0.25f)
            square.selectableDirection != null || square.directionSelected != null -> Color.Transparent // Hide square color
            square.onSelected != null -> Color.Green.copy(alpha = 0.25f)
            else -> Color.Transparent
        })
    }

    val boxWrapperModifier = remember(square) {
        val modifier = boxModifier
            .fillMaxSize()
            .background(color = bgColor)
            .onPointerEvent(PointerEventType.Enter) {
                vm.hoverOver(FieldCoordinate(width, height))
            }

        if (square.onSelected != null || square.contextMenuOptions.isNotEmpty()) {
            modifier.clickable {
                showPopup = !showPopup
                square.onSelected?.let {
                    it()
                }
            }
        } else {
            modifier
        }
    }

    Box(modifier = boxWrapperModifier) {
        if (showPopup) {
            ContextPopupMenu(
                hidePopup = { dimissed ->
                    showPopup = false
                    if (dimissed)  {
                        square.onMenuHidden?.let {
                            it()
                        }
                    }
                },
                commands = square.contextMenuOptions,
            )
        }
        square.player?.let {
            Player(boxModifier, it, true)
        }
        if (square.isBallOnGround || square.isBallExiting) {
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

@Composable
fun BlockDiceIndicatorImage(dice: Int) {
    val interactionSource = remember { MutableInteractionSource() }
    val isHovered by interactionSource.collectIsHoveredAsState()
    val imageRes = IconFactory.getBlockDiceRolledIndicator(dice)
    Image(
        modifier = Modifier.fillMaxSize().hoverable(interactionSource = interactionSource),
        painter = if (isHovered) painterResource(imageRes) else ColorPainter(Color.Transparent),
        contentDescription = null,
    )
}
