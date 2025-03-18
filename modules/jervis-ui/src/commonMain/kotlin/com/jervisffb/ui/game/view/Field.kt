package com.jervisffb.ui.game.view

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
import androidx.compose.ui.input.pointer.PointerEventType
import androidx.compose.ui.input.pointer.onPointerEvent
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.unit.dp
import com.jervisffb.engine.model.Direction
import com.jervisffb.engine.model.FieldSquare
import com.jervisffb.engine.model.locations.FieldCoordinate
import com.jervisffb.ui.game.icons.IconFactory
import com.jervisffb.ui.game.model.UiFieldSquare
import com.jervisffb.ui.game.view.animation.AnimationLayer
import com.jervisffb.ui.game.viewmodel.FieldDetails
import com.jervisffb.ui.game.viewmodel.FieldViewModel
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
                },
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
    var showPopup: Boolean by remember(square) { mutableStateOf(square.showContextMenu) }

    val bgColor by remember(square) {
        mutableStateOf(when {
            square.onSelected != null && square.requiresRoll -> Color.Yellow.copy(alpha = 0.25f)
            square.selectableDirection != null || square.directionSelected != null -> Color.Transparent // Hide square color
            square.player?.isSelectable == true -> Color.Transparent
            square.onSelected != null -> Color.Green.copy(alpha = 0.25f) // Fallback for active squares
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

        if (square.contextMenuOptions.isNotEmpty() || square.onSelected != null || square.hoverAction != null) {
            modifier.clickable {
                showPopup = !showPopup
                if (square.hoverAction != null) {
                    square.hoverAction()
                } else {
                    square.onSelected?.let {
                        it()
                    }
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
            Player(boxModifier, it, true)
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
//    val interactionSource = remember { MutableInteractionSource() }
//    val isHovered by interactionSource.collectIsHoveredAsState()
    val imageRes = IconFactory.getBlockDiceRolledIndicator(dice)
    Image(
        modifier = Modifier.fillMaxSize() /* .hoverable(interactionSource = interactionSource) */,
        painter = painterResource(imageRes),
        // painter = if (isHovered) painterResource(imageRes) else ColorPainter(Color.Transparent),
        contentDescription = null,
    )
}
