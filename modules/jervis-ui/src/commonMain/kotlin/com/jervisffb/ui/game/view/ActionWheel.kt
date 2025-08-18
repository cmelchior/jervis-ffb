package com.jervisffb.ui.game.view

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.AnimationVector1D
import androidx.compose.animation.core.FastOutLinearInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.onClick
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.DrawerDefaults.shape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.CompositingStrategy
import androidx.compose.ui.graphics.FilterQuality
import androidx.compose.ui.graphics.ImageShader
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.ShaderBrush
import androidx.compose.ui.graphics.TileMode
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.PointerEventType
import androidx.compose.ui.input.pointer.onPointerEvent
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Popup
import com.adamglin.composeshadow.dropShadow
import com.jervisffb.engine.actions.D12Result
import com.jervisffb.engine.actions.D16Result
import com.jervisffb.engine.actions.D20Result
import com.jervisffb.engine.actions.D2Result
import com.jervisffb.engine.actions.D3Result
import com.jervisffb.engine.actions.D4Result
import com.jervisffb.engine.actions.D6Result
import com.jervisffb.engine.actions.D8Result
import com.jervisffb.engine.actions.DBlockResult
import com.jervisffb.engine.actions.DieResult
import com.jervisffb.engine.model.Coin
import com.jervisffb.jervis_ui.generated.resources.Res
import com.jervisffb.jervis_ui.generated.resources.jervis_brush_chalk
import com.jervisffb.ui.game.dialogs.circle.ActionMenuItem
import com.jervisffb.ui.game.dialogs.circle.ActionWheelMenuController
import com.jervisffb.ui.game.dialogs.circle.ActionWheelMenuItem
import com.jervisffb.ui.game.dialogs.circle.ActionWheelViewModel
import com.jervisffb.ui.game.dialogs.circle.ButtonLayoutMode
import com.jervisffb.ui.game.dialogs.circle.CoinMenuItem
import com.jervisffb.ui.game.dialogs.circle.DiceMenuItem
import com.jervisffb.ui.game.dialogs.circle.TopLevelMenuItem
import com.jervisffb.ui.game.icons.ActionIcon
import com.jervisffb.ui.game.icons.DiceColor
import com.jervisffb.ui.game.icons.IconFactory
import com.jervisffb.ui.game.view.utils.D6Shape
import com.jervisffb.ui.game.view.utils.D8Shape
import com.jervisffb.ui.game.view.utils.paperBackground
import com.jervisffb.ui.reversed
import com.jervisffb.ui.toRadians
import com.jervisffb.ui.utils.applyIf
import com.jervisffb.ui.utils.jdp
import com.jervisffb.ui.utils.scalePixels
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.imageResource
import kotlin.math.cos
import kotlin.math.hypot
import kotlin.math.roundToInt
import kotlin.math.sin

/**
 * This file contains all the composable required to render a "Circular Action Menu".
 * The main entry point is [ActionWheelMenu], which is driven by an instance of
 * [ActionWheelViewModel].
 */

/**
 * Main entry point for a Circular Action Menu.
 *
 * @param viewModel The view model driving the menu.
 * @param ringSize The size of the ring.
 * @param borderSize The size of the border. Is inside [ringSize].
 * @param animationDuration The duration of animations changing the menu layout,
 * like adding or removing menus.
 */
@Composable
fun ActionWheelMenu(
    viewModel: ActionWheelViewModel,
    ringSize: Dp = 250.jdp,
    borderSize: Dp = 20.jdp,
    animationDuration: Int = 300,
    showTip: Boolean = false,
    tipRotationDegree: Float = 0f,
    onDismissRequest: (Boolean) -> Unit,
) {
    // Center of the menu in pixels
    val centerPx = with(LocalDensity.current) { Offset((ringSize/2f).toPx(), (ringSize/2f).toPx()) }
    val teamColor = remember(viewModel.owner) {
        when (viewModel.owner.isHomeTeam()) {
            true -> JervisTheme.rulebookRed
            false -> JervisTheme.rulebookBlue
        }
    }
    val hoverText: String? by viewModel.hoverText.collectAsState()

    // var hoverText by remember { mutableStateOf<String?>(viewModel.startingHoverText) }
    // val hoverText: String? by viewModel.hoverText.collectAsState()
    var topMessage = viewModel.topMessage
    val maxSize = (hypot(ringSize.value, ringSize.value)).dp
    Box(
        modifier = Modifier
            .size(maxSize)
            .graphicsLayer(clip = false)
        ,
        contentAlignment = Alignment.Center
    ) {
        val updateHover = remember(viewModel) {
            { ht: String? ->
                viewModel.hoverText.value = if (ht.isNullOrBlank() && viewModel.fallbackToStartHoverText) {
                    viewModel.startingHoverText
                } else {
                    ht
                }
            }
        }
        ActionWheelBackgroundRing(ringSize, borderSize, showTip, tipRotationDegree, onDismissRequest = onDismissRequest)
        NestedMenuLayout(
            viewModel = viewModel,
            viewModel.bottomMenu,
            (ringSize - borderSize)/2f,
            animationDuration,
            onHover = updateHover,
        )
        if (topMessage == null) {
            NestedMenuLayout(
                viewModel = viewModel,
                viewModel.topMenu,
                (ringSize - borderSize)/2f,
                animationDuration,
                onHover = updateHover,
            )
        } else {
            RingMessage(
                message = topMessage,
                angle = viewModel.topMenu.topLevelMenu.startSubAngle,
                radius = (ringSize - borderSize)/2f,
                borderColor = teamColor,
            )
        }
        HoverText(hoverText, teamColor)
    }
}

/**
 * Composable responsible for rendering a message that is replacing buttons
 * on either the bottom or top, but it is generic enough to handle any location
 */
@Composable
private fun RingMessage(
    message: String?,
    angle: Float,
    radius: Dp,
    borderColor: Color = JervisTheme.rulebookRed,
) {
    val radiusPx = with(LocalDensity.current) { radius.toPx() }
    val offset = remember(angle, message) { getOffset(angle, radiusPx) }
    Box(modifier = Modifier.offset { offset.toIntOffset() }) {
        Box(
            modifier = Modifier
                .dropShadow(
                    shape = shape,
                    color = Color.Black.copy(1f),
                    offsetX = 0.dp,
                    offsetY = 0.dp,
                    blur = 16.dp
                )
                .paperBackground()
                .border(width = 4.dp, borderColor)
                .padding(start = 32.dp, end = 32.dp, top = 16.dp, bottom = 16.dp)
        ) {
            Text(
                modifier = Modifier,
                text = message ?: "",
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )
        }
    }
}

/**
 * Top-level composable responsible for tracking a nested menu and transitions between the layers.
 * The "starting point" for the menu is defined by [TopLevelMenuItem.startSubAngle].
 *
 * This way you can have multiple nested layouts on the same ring, like buttons at the top
 * and actions at the bottom, but they do not know about each other, so can potentially overlap
 * if configured incorrectly.
 */
@Composable
private fun NestedMenuLayout(
    viewModel: ActionWheelViewModel,
    menuController: ActionWheelMenuController,
    radius: Dp,
    animationDuration: Int,
    onHover: (String?) -> Unit
) {
    val stack = remember { mutableStateListOf<ActionWheelMenuItem>() }
    var currentPrimaryMenu by remember { mutableStateOf<ActionWheelMenuItem?>(null) }
    var mode by remember { mutableStateOf(ButtonLayoutMode.STABLE) }

    CircularMenuLevel(
        mode = mode,
        activeButton = currentPrimaryMenu,
        primaryMenuLevel = currentPrimaryMenu?.parent ?: menuController.topLevelMenu,
        secondaryMenuLevel = stack.lastOrNull(),
        radius = radius,
        animationDuration = animationDuration,
        onItemSelected = { item ->
            if (item != currentPrimaryMenu && item.subMenu.isNotEmpty()) {
                mode = ButtonLayoutMode.EXPAND
                stack.add(item)
                currentPrimaryMenu = item
            } else if (item == currentPrimaryMenu) {
                mode = ButtonLayoutMode.CONTRACT
            } else {
                (item as ActionMenuItem).onClick(item.parent, item)
            }
        },
        onHover = onHover,
        onAnimationOver = {
            when (mode) {
                ButtonLayoutMode.STABLE -> { }
                ButtonLayoutMode.EXPAND -> { }
                ButtonLayoutMode.CONTRACT -> {
                    stack.removeLast()
                    currentPrimaryMenu = stack.lastOrNull()
                }
            }
            mode = ButtonLayoutMode.STABLE
        },
        onExpandChanged = { die, isExpanded ->
            viewModel.onActionOptionsExpandChange(die, isExpanded)
        }
    )
}

// Calculate the shortest distance (in degrees) to a target when moving across a ring
private fun shortestPathToTaget(current: Float, desired: Float): Float {
    val delta = ((((desired - current) % 360f) + 540f) % 360f) - 180f
    return current + delta
}

/**
 * Composable controlling a single "level" of a nested circular menu.
 * It also handles the transitions going back up the chain or diving further into
 * submenus.
 */
@Composable
private fun CircularMenuLevel(
    mode: ButtonLayoutMode,
    activeButton: ActionWheelMenuItem?,
    primaryMenuLevel: ActionWheelMenuItem?,
    // This is only used while animating changes. Once a menu is "stable", it is
    // always sent as the `primaryMenuLevel`.
    secondaryMenuLevel: ActionWheelMenuItem?,
    radius: Dp,
    animationDuration: Int,
    onItemSelected: (ActionWheelMenuItem) -> Unit,
    onHover: (String?) -> Unit,
    onAnimationOver: () -> Unit,
    onExpandChanged: (ActionWheelMenuItem, Boolean) -> Unit,
) {
    val radiusPx = with(LocalDensity.current) { radius.toPx() }

    //    // Animation state for primary menu items
    val mainAngleAnims = remember { mutableMapOf<ActionWheelMenuItem, Animatable<Float, AnimationVector1D>>() }
    val mainAlphaAnims = remember { mutableMapOf<ActionWheelMenuItem, Animatable<Float, AnimationVector1D>>() }

    // Animation state for submenu items for the currently selected main menu)
    val subAngleAnims = remember { mutableMapOf<ActionWheelMenuItem, Animatable<Float, AnimationVector1D>>() }
    val subAlphaAnims = remember { mutableMapOf<ActionWheelMenuItem, Animatable<Float, AnimationVector1D>>() }

    Box(Modifier.size(radius), Alignment.Center) {
        // "sub-level" items should be rendered first so they are behind "top-level" menus
        secondaryMenuLevel?.subMenu?.forEachIndexed { i, item ->
            val base = when (mode) {
                ButtonLayoutMode.STABLE,
                ButtonLayoutMode.EXPAND -> activeButton!!.startSubAngle
                ButtonLayoutMode.CONTRACT -> item.startSubAngle // Should never be used because item is already in the map
            }
            val angleA = subAngleAnims.getOrPut(item) { Animatable(base) }
            val alphaA = subAlphaAnims.getOrPut(item) { Animatable(0f) }

            LaunchedEffect(mode, item, activeButton, primaryMenuLevel?.subMenu, secondaryMenuLevel.subMenu) {
                when (mode) {
                    ButtonLayoutMode.STABLE -> {
                        // println("Sub Stable: ${item.label}")
                        // Animate in new items. Existing items are already in this state.
                        launch {
                            angleA.animateTo(shortestPathToTaget(angleA.value, item.startSubAngle), tween(animationDuration))
                        }
                        launch {
                            alphaA.animateTo(1f, tween(animationDuration))
                        }
                    }
                    ButtonLayoutMode.EXPAND -> {
                        // println("Expand sub level: ${item.label}")
                        launch {
                            angleA.animateTo(shortestPathToTaget(angleA.value, item.startSubAngle), tween(animationDuration))
                        }
                        launch {
                            alphaA.animateTo(1f, tween(animationDuration))
                        }

                    }
                    ButtonLayoutMode.CONTRACT -> {
                        // println("Contract sub level: ${item.label}")
                        launch {
                            angleA.animateTo(shortestPathToTaget(angleA.value, activeButton!!.startSubAngle), tween(animationDuration))
                        }
                        launch {
                            alphaA.animateTo(0f, tween(animationDuration))
                        }
                    }
                }
            }

            // Draw button
            MenuItemButton(
                item,
                angleA.value,
                alphaA.value,
                radiusPx,
                onHover,
                onItemSelected,
                onExpandChanged,
            )
        }

        // Render "top-level" menu
        primaryMenuLevel?.subMenu?.forEachIndexed { i, item ->
            val base = item.startSubAngle
            val angleA = mainAngleAnims.getOrPut(item) { Animatable(base) }
            val alphaA = mainAlphaAnims.getOrPut(item) { Animatable(1f) }
            val isPrimary = (activeButton == item)
            LaunchedEffect(mode, item, item.startSubAngle, activeButton, primaryMenuLevel.subMenu, secondaryMenuLevel?.subMenu) {
                when (mode) {
                    ButtonLayoutMode.STABLE -> {
                        // println("Render main: ${item.label}")
                        // Animate in new items. Existing items are already in this state.
                        launch {
                            angleA.animateTo(
                                shortestPathToTaget(
                                    angleA.value,
                                    if (isPrimary) item.startMainAngle else item.startSubAngle
                                ),
                                tween(animationDuration)
                            )
                        }
                        launch {
                            // If there is no "active button" we are at the top-level and then want to show all buttons
                            alphaA.animateTo(
                                if (isPrimary || activeButton == null) 1f else 0f,
                                tween(animationDuration)
                            )
                        }
                    }
                    ButtonLayoutMode.EXPAND -> {
                        when {
                            isPrimary -> {
                                // println("Move to selected position: ${item.label}")
                                launch {
                                    angleA.animateTo(
                                        shortestPathToTaget(angleA.value, item.startMainAngle),
                                        tween(animationDuration)
                                    )
                                }
                                launch {
                                    alphaA.animateTo(
                                        1f,
                                        tween(animationDuration / 2)
                                    )
                                }
                            }
                            else -> {
                                // println("Fade out: ${item.label}")
                                launch {
                                    alphaA.animateTo(
                                        0f,
                                        tween(animationDuration / 2)
                                    )
                                }
                            }
                        }
                    }
                    ButtonLayoutMode.CONTRACT -> {
                        when {
                            isPrimary -> {
                                // println("Move primary back to starting position: ${item.label}")
                                launch {
                                    angleA.animateTo(
                                        shortestPathToTaget(angleA.value, item.startSubAngle),
                                        tween(animationDuration)
                                    )
                                }
                            }
                            else -> {
                                // println("Fade in: ${item.label}")
                                launch {
                                    alphaA.animateTo(
                                        1f,
                                        tween(animationDuration / 2)
                                    )
                                }
                            }
                        }
                    }
                }
                launch {
                    delay(animationDuration.toLong())
                    onAnimationOver()
                }
            }

            // Draw button on the ring
            MenuItemButton(
                item,
                angleA.value,
                alphaA.value,
                radiusPx,
                onHover,
                onItemSelected,
                onExpandChanged,
            )
        }
    }
}

// Composable responsible for rending the actual action/dice button
@Composable
private fun MenuItemButton(
    item: ActionWheelMenuItem,
    angle: Float,
    alpha: Float,
    radiusPx: Float,
    // Set onHover description
    onHover: (String?) -> Unit,
    onItemSelected: (ActionWheelMenuItem) -> Unit,
    onExpandChanged: (ActionWheelMenuItem, Boolean) -> Unit = { _, _ -> },
) {
    if (alpha <= 0f) return
    val offset = getOffset(angle, radiusPx)
    Box(
        modifier = Modifier
            .offset { offset.toIntOffset() }
            .alpha(alpha)
    ) {
        when (item) {
            is TopLevelMenuItem -> error("Not supported: $item")
            is ActionMenuItem -> {
                ActionButton(
                    item.label(),
                    item.icon,
                    enabled = item.enabled,
                    onHover = { onHover(it) },
                    onClick = { onItemSelected(item) }
                )
            }
            is DiceMenuItem<*> -> {
                ExpandableDiceSelector(
                    item,
                    disabled = !item.enabled,
                    canExpand = item.expandable,
                    onClick = {
                        item.onClick(item.value)
                    },
                    onExpandedChanged = onExpandChanged,
                    onHover = {
                        @Suppress("UNCHECKED_CAST")
                        val hoverText = item.onHover as (DieResult?) -> String?
                        onHover(hoverText(it))
                    },
                )
            }

            is CoinMenuItem -> {
                CoinButton(
                    coin = item,
                    disabled = false,
                )
            }
        }
    }
}

@OptIn(ExperimentalComposeUiApi::class)
@Composable
private fun ActionWheelBackgroundRing(
    ringSize: Dp,
    borderSize: Dp,
    // Whether to show the tip
    showTip: Boolean = false,
    // How many degrees to rotate the tip in degrees. 0f is top-left
    tipRotation: Float = 0f,
    ringColor: Color = JervisTheme.black,
    onDismissRequest: (Boolean) -> Unit = { }
) {
    val chalkTexture = imageResource(Res.drawable.jervis_brush_chalk)
    val imageBrush = remember {
        ShaderBrush(
            shader = ImageShader(
                image = chalkTexture.scalePixels(IconFactory.scaleFactor),
                tileModeX = TileMode.Repeated,
                tileModeY = TileMode.Repeated,
            ),
        )
    }
    val ringAlpha = 0.5f
    val padding = ((hypot(ringSize.value, ringSize.value)).dp - ringSize) / 2f
    Canvas(
        modifier = Modifier
            .fillMaxSize()
            .clickable(interactionSource = null, indication = null) {
                onDismissRequest(true)
            }
            .graphicsLayer {
                clip = false
                compositingStrategy = CompositingStrategy.Offscreen
            }
    ) {
        val radius = ringSize.toPx() / 2f
        val center = Offset(size.width / 2f, size.height / 2f)
        val tip = Offset(center.x - ringSize.toPx() / 2f, center.y - ringSize.toPx() / 2f) // tip of the droplet
        if (showTip) {
            val path = Path().apply {
                moveTo(tip.x, tip.y)
                lineTo(x = center.x, y = padding.toPx())
                arcTo(
                    rect = Rect(
                        center = center,
                        radius = radius
                    ),
                    startAngleDegrees = -90f,
                    sweepAngleDegrees = 270f,
                    forceMoveTo = true
                )
                lineTo(x = tip.x, y = tip.y)
                close()
            }

            rotate(degrees = tipRotation) {
                drawPath(
                    path = path,
                    brush = imageBrush,
                    alpha = 1f,
                    colorFilter = ColorFilter.tint(ringColor.copy(alpha = ringAlpha)),
                )
            }
        } else {
            drawCircle(
                brush = imageBrush,
                radius = radius,
                center = center,
                alpha = 1f,
                colorFilter = ColorFilter.tint(ringColor.copy(alpha = ringAlpha)),
            )
        }

        val innerRadius = radius - borderSize.toPx()
        val innerPath = Path().apply {
            addOval(Rect(center = center, radius = innerRadius))
        }

        drawPath(
            path = innerPath,
            color = Color.Transparent,
            blendMode = BlendMode.Clear
        )
    }
}

//@Composable
//private fun ActionWheelBackgroundRing(
//    ringSize: Dp,
//    borderSize: Dp,
//    // Whether to show the tip
//    showTip: Boolean = false,
//    // How many degrees to rotate the tip in degrees. 0f is top-left
//    tipRotation: Float = 0f,
//    ringColor: Color = JervisTheme.black
//) {
//    val imageBrush = ShaderBrush(createGrayscaleNoiseShader())
//    val ringAlpha = 1f
//    //val imageBrush = SolidColor(ringColor)
//    val padding = ((hypot(ringSize.value, ringSize.value)).dp - ringSize) / 2f
//    Canvas(
//        modifier = Modifier
//            .fillMaxSize()
//            .graphicsLayer {
//                clip = false
//                compositingStrategy = CompositingStrategy.Offscreen
//            }
//    ) {
//        val radius = ringSize.toPx() / 2f
//        val center = Offset(size.width / 2f, size.height / 2f)
//        val tip = Offset(center.x - ringSize.toPx() / 2f, center.y - ringSize.toPx() / 2f) // tip of the droplet
//        if (showTip) {
//            val path = Path().apply {
//                moveTo(tip.x, tip.y)
//                lineTo(x = center.x, y = padding.toPx())
//                arcTo(
//                    rect = Rect(
//                        center = center,
//                        radius = radius
//                    ),
//                    startAngleDegrees = -90f,
//                    sweepAngleDegrees = 270f,
//                    forceMoveTo = true
//                )
//                lineTo(x = tip.x, y = tip.y)
//                close()
//            }
//
//            rotate(degrees = tipRotation) {
//                // Add desired background color
//                drawPath(
//                    path = path,
//                    brush = SolidColor(JervisTheme.rulebookPaper),
//                    alpha = 1f,
//                    //colorFilter = ColorFilter.tint(JervisTheme.rulebookPaper),
//                )
//                // Add semi-transparent noise on top
//                drawRect(
//                    size = size,
//                    brush = imageBrush,
//                    alpha = 0.3f,
//                )
//                drawPath(
//                    path = path,
//                    brush = imageBrush,
//                    alpha = 1f,
//                    colorFilter = ColorFilter.tint(JervisTheme.rulebookPaper.copy(alpha = 0.5f)),
//                )
//                drawPath(
//                    path = path,
//                    brush = SolidColor(ringColor),
//                    style = Stroke(width = 6f, join = StrokeJoin.Miter),
//                )
////                drawPath(
////                    path = path,
////                    brush = imageBrush,
////                    alpha = 1f,
////                    colorFilter = ColorFilter.tint(ringColor.copy(alpha = ringAlpha)),
////                )
//            }
//        } else {
//            drawCircle(
//                brush = imageBrush,
//                radius = radius,
//                center = center,
//                alpha = 1f,
//                colorFilter = ColorFilter.tint(ringColor.copy(alpha = ringAlpha)),
//            )
//        }
//
//        val innerRadius = radius - borderSize.toPx()
//        val innerPath = Path().apply {
//            addOval(Rect(center = center, radius = innerRadius))
//        }
//
//        drawPath(
//            path = innerPath,
//            color = Color.Transparent,
//            blendMode = BlendMode.Clear
//        )
//        drawPath(
//            path = innerPath,
//            style = Stroke(width = 6f, join = StrokeJoin.Miter),
//            color = ringColor,
//        )
//
//    }
//}

// Helper text that hovers just below the center player.
// Generally, this should be a "hover" effect when mousing over buttons
@Composable
private fun HoverText(
    message: String?,
    borderColor: Color,
) {
    val fontSize = 14.sp
    val fontWeight = FontWeight.Bold
    val textColor = Color.White
    val animationDuration = 200
    var displayedMessage by remember { mutableStateOf<String?>(null) }
    val bgAlpha = remember {
        Animatable(0f)
    }
    LaunchedEffect(message) {
        if (message == null) {
            bgAlpha.animateTo(0f, tween(
                durationMillis = (bgAlpha.value*animationDuration).roundToInt(),
                easing = LinearOutSlowInEasing.reversed()
            ))
            delay((bgAlpha.value*animationDuration).roundToInt().toLong())
            displayedMessage = null
        } else {
            displayedMessage = message
            bgAlpha.animateTo(1f, tween(
                durationMillis = ((1f-bgAlpha.value) * animationDuration).roundToInt(),
                easing = LinearEasing
            ))
        }
    }

    // There are issues with text Stroke and alpha. It doesn't seem to render correctly.
    // This is probably a bug, but haven't found a workaround yet. So for now, just use
    // a solid background color.

    // Background border
//        Text(
//            modifier = Modifier.alpha(bgAlpha.value),
//            text = displayedMessage ?: "",
//            style = MaterialTheme.typography.body1.copy(
//                color = borderColor,
//                fontWeight = fontWeight,
//                fontSize = fontSize,
//                // Shadow doesn't work well with border. We probably need a custom canvas render
//                // shadow = Shadow(
//                //     color = Color.Black,
//                //     offset = Offset(2f, 2f),
//                //     blurRadius = 8f
//                // ),
//                drawStyle = Stroke(
//                    miter = borderWidth,
//                    width = borderWidth,
//                    join = StrokeJoin.Round
//                )
//            ),
//        )
    Text(
        modifier = Modifier
            .offset(y = 60.jdp)
            .clip(RoundedCornerShape(4.dp))
            .alpha(bgAlpha.value)
            .background(borderColor)
            .padding(4.dp)
        ,
        text = displayedMessage ?: "",
        style = MaterialTheme.typography.bodySmall.copy(
            lineHeight = 1.em,
            color = textColor,
            fontWeight = fontWeight,
            fontSize = fontSize,
        ),
    )
}

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun ActionButton(
    description: String,
    icon: ActionIcon,
    enabled: Boolean = true,
    onHover: (String?) -> Unit = {},
    onClick: () -> Unit = { },
) {
    val icon = remember(icon) { IconFactory.getActionIcon(icon) }
    val colorFilter = ColorFilter.tint(JervisTheme.black.copy(0.1f), BlendMode.Darken)
    var isHover by remember { mutableStateOf(false) }
    Box(
        modifier = Modifier.alpha(if (enabled) 1f else 0.3f)
    ) {
        Image(
            modifier = Modifier
                .dropShadow(
                    shape = CircleShape,
                    color = Color.Black.copy(1f),
                    offsetX = 0.dp,
                    offsetY = 0.dp,
                    blur = 8.dp
                ),
            bitmap = icon,
            contentDescription = "Drop shadow",
            filterQuality = FilterQuality.None,
        )
        Image(
            modifier = Modifier
                .border(2.dp, Color.Black.copy(0.5f), CircleShape)
                .clip(CircleShape)
                .applyIf(enabled) {
                    this.clickable { onClick() }
                        .onPointerEvent(PointerEventType.Enter) {
                            isHover = true
                            onHover(description)
                        }
                        .onPointerEvent(PointerEventType.Exit) {
                            isHover = false
                            onHover(null)
                        }
                }
            ,
            filterQuality = FilterQuality.None,
            bitmap = icon,
            contentDescription = "",
            colorFilter = if (isHover) colorFilter else null
        )
    }
}

@Composable
fun CoinButton(
    modifier: Modifier = Modifier,
    coin: CoinMenuItem,
    disabled: Boolean = false,
    onClick: (Coin) -> Unit = { },
    onAnimationDone: () -> Unit = {},
    dropShadow: Boolean = true
) {
    val buttonSize = IconFactory.getCoinSizeDp(coin.value)
    val (buttonWidth, buttonHeight) = buttonSize

    Box(
        modifier = modifier
            .alpha(if (disabled) 0.3f else 1f)
        ,
        contentAlignment = Alignment.CenterStart,
    ) {
        if (!coin.animationDone) {
            CoinAnimation(
                coin.animatingFrom!!,
                coin.value,
                content = { value ->
                    CoinImage(
                        value,
                        buttonSize,
                        enabled = false,
                        onClick = { },
                        dropShadow = dropShadow,
                    )
                },
                onAnimationEnd = {
                    coin.animationDone = true
                    onAnimationDone()
                }
            )
        } else {
            CoinImage(
                coin.value,
                buttonSize,
                enabled = true,
                onClick = onClick,
                dropShadow = dropShadow,
            )
        }
    }
}

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun CoinImage(
    coin: Coin,
    size: DpSize,
    enabled: Boolean,
    onClick: (Coin) -> Unit = {},
    dropShadow: Boolean,
) {
    Box(
        modifier = Modifier
            .size(size)
        ,
        contentAlignment = Alignment.Center
    ) {

        val bitmap = IconFactory.getCoinIcon(coin)
        var colorFilter: ColorFilter? by remember { mutableStateOf(null) }
        Image(
            bitmap = bitmap,
            contentDescription = coin.name,
            modifier = Modifier.fillMaxSize()
                .applyIf(dropShadow) {
                    dropShadow(
                        shape = CircleShape,
                        color = JervisTheme.black.copy(1f),
                        offsetX = 0.dp,
                        offsetY = 0.dp,
                        blur = 12.dp,
                    )
                }
                .applyIf(!dropShadow) {
                    this.dropShadow(
                        shape = CircleShape,
                        color = JervisTheme.black.copy(0.3f),
                        offsetX = 0.dp,
                        offsetY = 0.dp,
                        blur = 4.dp
                    )
                }
                .applyIf(enabled) {
                    onPointerEvent(PointerEventType.Enter) {
                        colorFilter = ColorFilter.tint(JervisTheme.black.copy(0.1f), BlendMode.Darken)
                    }
                        .onPointerEvent(PointerEventType.Exit) {
                            colorFilter = null
                        }
                        .onPointerEvent(PointerEventType.Press) {
                            onClick(coin)
                        }
                }
                .clip(CircleShape)
            ,
            contentScale = ContentScale.Fit,
            filterQuality = FilterQuality.None,
            colorFilter = colorFilter,
        )
    }
}


/**
 * Composable responsible for handling a single die that can be expanded into a
 * value selector. The options will always expand to the right side.
 *
 * It also supports being animated to a new value. This is done by rotating up
 * in the air before landing, similar to a dice roll.
 *
 * TODO Check location of die and how much space is on the screen before
 *  selecting whether to expand to the right or left side. We could also consider
 *  using different layouts, i.e. more "boxed" when closer to the edge rather
 *  that swapping direction.
 */
@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun <T : DieResult> ExpandableDiceSelector(
    die: DiceMenuItem<T>,
    // When using multiple selectors, the other selectors are disabled (grayed out)
    // while one is open.
    disabled: Boolean = false,
    // if `false`. Clicking the primary dice button will just be treated as selecting that dice value
    canExpand: Boolean = true,
    onClick: (T) -> Unit = { },
    onHover: (DieResult?) -> Unit = { },
    onExpandedChanged: (ActionWheelMenuItem, Boolean) -> Unit = { _, _ -> },
    onAnimationDone: () -> Unit = {}
) {

    // Current state tracking
    val density = LocalDensity.current
    val diceValue = die.value
    val diceList = die.diceList
    val shadowColor = Color.Black
    var expanded by remember { mutableStateOf(false) }

    // Properties we animate when expanded and deflating the selector
    val expandDurationMs = 200
    val animation = tween<Float>(expandDurationMs, easing = FastOutLinearInEasing)
    val bgWidthDp = remember { Animatable(0f) }
    val bgAlpha = remember { Animatable(0f) }

    // Padding between background bar and dice buttons
    val backgroundPadding = 8.dp
    val spacingBetweenItems = backgroundPadding / 2f

    val rows = when (die.value) {
        is D12Result -> 3
        is D16Result -> 4
        is D20Result -> 4
        is D2Result -> 1
        is D3Result -> 1
        is D4Result -> 2
        is D6Result -> 2
        is D8Result -> 2
        is DBlockResult -> 2
    }
    val itemsPrRow = diceList.size / rows
    val buttonSize = IconFactory.getDiceSizeDp(diceValue)
    val (buttonWidth, buttonHeight) = buttonSize
    val maxWidthDp = (backgroundPadding * 2) + (spacingBetweenItems * (itemsPrRow - 1)) + (buttonWidth * itemsPrRow)
    val backgroundHeight = (buttonHeight*rows + backgroundPadding) + (backgroundPadding/2f)*(rows-1)

    // Determine the placement of popup
    val (popupDirection, popupOffset) = remember(die) {
        when (die.preferLtr) {
            true -> {
                val padding = with(density) { (backgroundPadding/2).toPx().roundToInt() }
                val adjustment = with(density) {
                    backgroundPadding.toPx().roundToInt() * -1
                }
                LayoutDirection.Ltr to IntOffset(adjustment, -padding)
            }
            false -> {
                val padding = with(density) { (backgroundPadding/2).toPx().roundToInt() }
                val adjustment = with(density) {
                    (maxWidthDp - buttonWidth - backgroundPadding - backgroundPadding / 2).toPx().roundToInt()
                }
                LayoutDirection.Rtl to IntOffset(-adjustment, -padding)
            }
        }
    }

    // Handle opening and closing animation of the dice selector
    if (expanded) {
        LaunchedEffect(die) {
            bgWidthDp.snapTo(maxWidthDp.value)
            // launch { bgWidthDp.animateTo(maxWidthDp.value, animation) }
            launch { bgAlpha.animateTo(1f) }
        }
    } else {
        LaunchedEffect(die) {
            bgWidthDp.snapTo(0f)
            // launch { bgWidthDp.animateTo(0f, animation) }
            launch { bgAlpha.animateTo(0f) }
        }
    }

    // The visible button, but the normal and jumping one.
    Box(
        modifier = Modifier.alpha(if (disabled) 0.3f else 1f)
        ,
        contentAlignment = Alignment.CenterStart,
    ) {
        if (!die.animationDone) {
            DiceAnimation(
                die.animatingFrom!!,
                options = diceList,
                diceValue,
                content = { value ->
                    DiceButton(
                        DpSize(buttonWidth, buttonHeight),
                        1f,
                        value = value,
                        enabled = false,
                        dropShadow = true,
                        dropShadowColor = shadowColor,
                    )
                },
                onAnimationEnd = {
                    die.animationDone = true
                    onAnimationDone()
                }
            )
        } else {
            DiceButton(
                DpSize(buttonWidth,  buttonHeight),
                1f,
                value = diceValue,
                enabled = !disabled,
                onClick = {
                    if (canExpand) {
                        expanded = !expanded
                        onExpandedChanged(die, expanded)
                    } else {
                        die.valueSelected(diceValue)
                        onClick(diceValue)
                    }
                },
                onHover = onHover,
                dropShadow = true,
                dropShadowColor = shadowColor
            )
        }
    }

    if (expanded) {
        Popup(
            alignment = Alignment.TopStart,
            offset = popupOffset,
            onDismissRequest = {
                expanded = false
                onHover(null)
                onExpandedChanged(die, expanded)
            }
        ) {
            CompositionLocalProvider(LocalLayoutDirection provides popupDirection) {
                Box(
                    modifier = Modifier
                        .padding(start = backgroundPadding / 2f)
                        .alpha(bgAlpha.value)
                        .height(backgroundHeight)
                        .width(maxWidthDp - backgroundPadding)
                ) {
                    // Expanding background
                    Box(
                        modifier = Modifier
                            .alpha(0.5f)
                            .width(bgWidthDp.value.dp)
                            .fillMaxHeight()
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color.Black.copy(alpha = bgAlpha.value))
                    )
                    // All buttons in the button group
                    Column(
                        modifier = Modifier.fillMaxHeight().padding(top = backgroundPadding/2f, bottom = backgroundPadding/2f),
                        verticalArrangement = Arrangement.SpaceBetween,
                    ) {
                        val diceRow = diceList.chunked(diceList.size / rows)
                        diceRow.forEachIndexed { rowIndex, row ->
                            Row(
                                modifier =
                                    Modifier
                                        .height(buttonHeight)
                                        .padding(start = backgroundPadding/2)
                                ,
                                horizontalArrangement = Arrangement.spacedBy(spacingBetweenItems),
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                // Generally options are sorted, but we always have the currently
                                // selected value in the front
                                row.forEachIndexed { index, label ->
                                    val currentBgWidth = bgWidthDp.value.dp
                                    val alpha = when (index == 0 || currentBgWidth > 52.dp * (index + 1)) {
                                        true -> bgAlpha.value
                                        false -> 0f
                                    }
                                    DiceButton(
                                        buttonSize,
                                        alpha,
                                        value = label,
                                        enabled = !disabled,
                                        onClick = {
                                            if (!expanded) {
                                                expanded = true
                                            } else {
                                                expanded = false
                                                die.valueSelected(die.diceList[rowIndex*(diceList.size / rows) + index])
                                            }
                                            if (rowIndex != 0 || index != 0) {
                                                onHover(null)
                                            }
                                            onExpandedChanged(die, expanded)
                                        },
                                        dropShadow = false,
                                        onHover = onHover,
                                        dropShadowColor = Color.Black,
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

// Composable responsible for doing the dice animation i.e., jump up and spin.
// But generic enough to run the animation on any content.
// A `blur` effect is passed to the `content` which
// provides a `blur` hint to the content being rotated.
@Composable
private fun <T: DieResult> DiceAnimation(
    startingValue: T,
    options: List<T>,
    endValue: T,
    content: @Composable (DieResult) -> Unit = {  _ -> },
    onAnimationEnd: () -> Unit = {},
) {
    val yOffset = remember(startingValue, endValue) { Animatable(0f) }
    val rotation = remember(startingValue, endValue) { Animatable(0f) }
    var displayFace by remember(startingValue, endValue) { mutableStateOf(startingValue) }

    // Total animation is 300 ms
    LaunchedEffect(startingValue, endValue) {
        val randJob = launch {
            // For "complex" dice like Block dice, changing face too quick
            // makes it look really messy. With this approach we change once
            // near the top of the arc before switching to correct result.
            // From manual testing, this seems to be a good trade-off between
            // "hiding" the true result a bit and making it look more "random".
            displayFace = startingValue
            delay(100)
            displayFace = options.random()
            delay(100)
            displayFace = endValue
        }

        // Parallel rotation while airborne
        val rotJob = launch {
            rotation.animateTo(
                targetValue = 360f,
                animationSpec = tween(300, easing = LinearEasing)
            )
        }

        // Jump up
        yOffset.animateTo(
            targetValue = -75f,
            animationSpec = tween(150, easing = LinearOutSlowInEasing)
        )

        // Start falling and stop rotation
        yOffset.animateTo(
            targetValue = 0f,
            animationSpec = tween(150, easing = FastOutLinearInEasing)
        )
        rotJob.cancel()
        randJob.cancel()
        rotation.snapTo(0f)
        displayFace = endValue
        onAnimationEnd()
    }

    Box(
        modifier = Modifier
            .graphicsLayer {
                translationY = yOffset.value
                rotationZ = rotation.value
            }
        ,
        contentAlignment = Alignment.Center
    ) {
        content(displayFace)
    }
}

@Composable
private fun CoinAnimation(
    startingValue: Coin,
    endValue: Coin,
    content: @Composable (Coin) -> Unit = {  _ -> },
    onAnimationEnd: () -> Unit = {},
) {
    val animationDurationMs = 300L
    val yOffset = remember(startingValue, endValue) { Animatable(0f) }
    val rotation = remember(startingValue, endValue) { Animatable(0f) }
    var displayFace by remember(startingValue, endValue) { mutableStateOf(startingValue) }

    // Total animation is 300 ms
    LaunchedEffect(startingValue, endValue) {
        val randJob = launch {
            // For "complex" dice like Block dice, changing face too quick
            // makes it look really messy. With this approach we change once
            // near the top of the arc before switching to correct result.
            // From manual testing, this seems to be a good trade-off between
            // "hiding" the true result a bit and making it look more "random".
            displayFace = startingValue
            delay(animationDurationMs/2)
            displayFace = endValue
        }

        // Parallel rotation while airborne
        val rotJob = launch {
            rotation.animateTo(
                targetValue = 360f,
                animationSpec = tween(300, easing = LinearEasing)
            )
        }

        // Jump up
        yOffset.animateTo(
            targetValue = -75f,
            animationSpec = tween(150, easing = LinearOutSlowInEasing)
        )

        // Start falling and stop rotation
        yOffset.animateTo(
            targetValue = 0f,
            animationSpec = tween(150, easing = FastOutLinearInEasing)
        )
        rotJob.cancel()
        randJob.cancel()
        rotation.snapTo(0f)
        displayFace = endValue
        onAnimationEnd()
    }

    Box(
        modifier = Modifier
            .graphicsLayer {
                translationY = yOffset.value
                rotationZ = rotation.value
            }
        ,
        contentAlignment = Alignment.Center
    ) {
        content(displayFace)
    }
}

@OptIn(ExperimentalComposeUiApi::class)
@Composable
private fun DiceButton(
    buttonSize: DpSize,
    alpha: Float,
    value: DieResult,
    enabled: Boolean = true,
    onHover: (DieResult?) -> Unit = {},
    onClick: () -> Unit = {},
    dropShadowColor: Color = Color.Black,
    dropShadow: Boolean = true,
) {
    val useSelectedColorAsHover = false
    var hover: Boolean by remember { mutableStateOf(false) }
    var colorFilter by remember { mutableStateOf<ColorFilter?>(null) }
    val bitmap = if ((useSelectedColorAsHover && hover)) {
        val color = when (value) {
            is D3Result,
            is D6Result -> DiceColor.YELLOW
            else -> DiceColor.DEFAULT
        }
        IconFactory.getDiceIcon(value, color)
    } else {
        val color = when (value) {
            is D3Result,
            is D6Result -> DiceColor.BROWN
            else -> DiceColor.DEFAULT
        }
        IconFactory.getDiceIcon(value, color)
    }
    Box(
        modifier = Modifier
            .size(buttonSize)
            .alpha(alpha)
        ,
        contentAlignment = Alignment.Center
    ) {
        Image(
            bitmap = bitmap,
            contentDescription = value.value.toString(),
            modifier = Modifier.fillMaxSize()
                .applyIf(dropShadow) {
                    dropShadow(
                        shape = when (value) {
                            is D8Result -> D8Shape
                            is D6Result -> D6Shape
                            else -> RoundedCornerShape(4.dp)
                        },
                        color = dropShadowColor.copy(1f),
                        offsetX = 0.dp,
                        offsetY = 0.dp,
                        blur = 12.dp,
                    )
                }
                .applyIf(true) {
                    this
                        .onPointerEvent(PointerEventType.Enter) {
                            hover = true
                            onHover(value)
                            if (!useSelectedColorAsHover) {
                                colorFilter = ColorFilter.tint(JervisTheme.black.copy(0.1f), BlendMode.Darken)
                            }
                        }
                        .onPointerEvent(PointerEventType.Exit) {
                            hover = false
                            onHover(null)
                            if (!useSelectedColorAsHover) {
                                colorFilter = null
                            }
                        }
                        .onPointerEvent(PointerEventType.Press) {
                            onClick()
                        }
                }
            ,
            contentScale = ContentScale.Fit,
            filterQuality = FilterQuality.None,
            colorFilter = colorFilter,
        )
    }
}

/**
 * Calculates the offset for displacing an item from the center of a circle
 * to the radius for a given angle in degrees
 */
private fun getOffset(angle: Float, radius: Float): Offset {
    val rad = toRadians(angle.toDouble())
    return Offset(
        (cos(rad).toFloat() * radius),
        (sin(rad).toFloat() * radius)
    )
}

private fun Offset.toIntOffset(): IntOffset = IntOffset(x.roundToInt(), y.roundToInt())
