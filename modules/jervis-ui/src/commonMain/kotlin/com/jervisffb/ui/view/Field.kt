package com.jervisffb.ui.view

import androidx.compose.animation.core.FastOutLinearInEasing
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animate
import androidx.compose.animation.core.tween
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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.painter.BitmapPainter
import androidx.compose.ui.graphics.painter.ColorPainter
import androidx.compose.ui.input.pointer.PointerEventType
import androidx.compose.ui.input.pointer.onPointerEvent
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.jervisffb.engine.model.Direction
import com.jervisffb.engine.model.FieldSquare
import com.jervisffb.engine.model.locations.FieldCoordinate
import com.jervisffb.ui.KickOffEventAnimation
import com.jervisffb.ui.icons.IconFactory
import com.jervisffb.ui.model.UiFieldSquare
import com.jervisffb.ui.viewmodel.FieldDetails
import com.jervisffb.ui.viewmodel.FieldViewModel
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.imageResource
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
                    val boxModifier = Modifier.fillMaxSize().weight(1f)
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
fun AnimationLayer(vm: FieldViewModel) {
    val animationData by vm.observeAnimation().collectAsState(null)
    if (animationData?.second is KickOffEventAnimation) {
        KickOffEventResult(vm, animationData!!.second as KickOffEventAnimation)
    }
}

@Composable
fun KickOffEventResult(vm: FieldViewModel, animation: KickOffEventAnimation) {
    var scale by remember { mutableStateOf(0f) }
    var alpha by remember { mutableStateOf(1f) }
    var translationY by remember { mutableStateOf(0f) }
    LaunchedEffect(animation) {
        // Reset values in case, the animation runs multiple times
        scale = 0.0f
        alpha = 1.0f
        translationY = 0f
        coroutineScope {
            launch {
                animate(
                    initialValue = 0.0f,
                    targetValue = 1f,
                    animationSpec = tween(durationMillis = 500, easing = FastOutLinearInEasing),
                ) { value: Float, _: Float ->
                    scale = value
                }
                delay(500)
                animate(
                    initialValue = 1f,
                    targetValue = 0f,
                    animationSpec = tween(durationMillis = 500, easing = LinearEasing),
                ) { value: Float, _: Float ->
                    alpha = value
                }
                vm.finishAnimation()
            }
            launch {
                // Let the image come in from a lower position rather than directly from the center
                // Makes it look more dynamic
                animate(
                    initialValue = 100.0f,
                    targetValue = 0f,
                    animationSpec = tween(durationMillis = 500, easing = FastOutSlowInEasing),
                ) { value: Float, _: Float ->
                    translationY = value
                }
            }
        }
    }
    Image(
        bitmap = imageResource(animation.image),
        contentDescription = null,
        alignment = Alignment.Center,
        contentScale = ContentScale.Fit,
        modifier = Modifier
            .fillMaxSize()
            .padding(64.dp)
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
                this.translationY = translationY
            }
            .alpha(alpha)
    )
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
                modifier = Modifier.fillMaxSize().padding(4.dp).background(color = if (square.isBallExiting) Color.Red else Color.Transparent),
                alignment = Alignment.Center,
                contentScale = ContentScale.FillBounds,
                bitmap = IconFactory.getBall(),
                contentDescription = "",
            )
        }
        square.selectableDirection?.let {
            DictionImage(it)
        }
        if (square.dice != 0) {
            BlockDiceIndicatorImage(square.dice)
        }
    }
}

@Composable
fun DictionImage(direction: Direction) {
    val interactionSource = remember { MutableInteractionSource() }
    val isHovered by interactionSource.collectIsHoveredAsState()
    val imageRes = IconFactory.getDirection(direction, isHovered)
    Image(
        modifier = Modifier.fillMaxSize().hoverable(interactionSource = interactionSource),
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
