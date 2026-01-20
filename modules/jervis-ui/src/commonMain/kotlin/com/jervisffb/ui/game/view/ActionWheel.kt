package com.jervisffb.ui.game.view

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutLinearInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.DrawerDefaults.shape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.dropShadow
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
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.TileMode
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.PointerEventType
import androidx.compose.ui.input.pointer.onPointerEvent
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.BaselineShift
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Popup
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
import com.jervisffb.engine.model.locations.FieldCoordinate
import com.jervisffb.jervis_ui.generated.resources.Res
import com.jervisffb.jervis_ui.generated.resources.jervis_brush_chalk
import com.jervisffb.ui.game.dialogs.ActionButtonData
import com.jervisffb.ui.game.dialogs.ButtonData
import com.jervisffb.ui.game.dialogs.ButtonId
import com.jervisffb.ui.game.dialogs.DieButtonData
import com.jervisffb.ui.game.dialogs.wheel.ButtonLayoutMode
import com.jervisffb.ui.game.dialogs.wheel.CoinMenuItem
import com.jervisffb.ui.game.dialogs.wheel.MenuExpandMode
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
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.imageResource
import kotlin.math.ceil
import kotlin.math.cos
import kotlin.math.hypot
import kotlin.math.max
import kotlin.math.roundToInt
import kotlin.math.sin
import kotlin.random.Random

sealed interface ActionWheelUiState
object HideActionWheel : ActionWheelUiState
object ShowActionWheel : ActionWheelUiState
data class ActionWheelUiStateData(
    val center: FieldCoordinate?,
    val topItems: List<ButtonData> = emptyList(),
    val topExpandMode: MenuExpandMode = MenuExpandMode.Compact(),
    val topAnimationType: ButtonLayoutMode = ButtonLayoutMode.STABLE,
    val bottomItems: List<ButtonData> = emptyList(),
    val bottomExpandMode: MenuExpandMode = MenuExpandMode.Compact(),
    val bottomAnimationType: ButtonLayoutMode = ButtonLayoutMode.STABLE,
    val onDismiss: (() -> Unit)? = null,
    val animationOnly: Boolean = false,
    val hideWhenClickOutside: Boolean = false,
): ActionWheelUiState {
    var lastActionWasUndo: Boolean = false

    // Only used in `COMPACT` mode and defines the distance in degrees between
    // each sub item.
    private val stepAngle = 45.0f

    init {
        recalculateSubMenuAngles(topItems, topExpandMode, -90f)
        recalculateSubMenuAngles(bottomItems, bottomExpandMode, 90f)
    }

    private fun recalculateSubMenuAngles(
        buttons: List<ButtonData>,
        expandMode: MenuExpandMode,
        startAngle: Float
    ) {
        if (buttons.isEmpty()) return
        buttons.forEach {
            it.defaultStartingAngle = startAngle
        }
        when (val mode = expandMode) {
            MenuExpandMode.None -> error("Not supported")
            MenuExpandMode.TwoWay -> {
                buttons.forEachIndexed { index, item ->
                    when (index) {
                        0 -> {
                            item.defaultStartingAngle = 0f
                            item.targetAngle = -90f
                        }
                        1 -> {
                            item.defaultStartingAngle = 180f
                            item.targetAngle = 90f
                        }
                        else -> error("Too many item: ${buttons.size}")
                    }
                }
            }
            is MenuExpandMode.FanOut -> {
                // If no parent, evenly distribute items in the ring.
                // For an even number of items, one will always be on the `defaultStartAngle`
                // For an odd number of items, `defaultStartAngle` will be in the middle between two items.
                // directly on `centerAngle`
                buttons.forEachIndexed { index, item ->
                    item.targetAngle = (mode.spread / buttons.size) * index + startAngle
                }
            }
            is MenuExpandMode.Compact -> {
                // Clump menu items together at `centerAngle`. For an even number of menu items
                // This means none of them will be directly on `centerAngle`.
                val offset = if (buttons.size % 2 == 0) stepAngle / 2f else 0f
                // val parentModifier = if (parent == null) 0 else 1
                val parentModifier = 0
                buttons.forEachIndexed { index, item ->
                    // This will swap direction (starting with clockwise) and gradually move out,
                    // so [cw(1), ccw(1), cw(2), ccw(2), ...]
                    val direction = if (index % 2 == 1) -1 else 1
                    val magnitude = ceil((index + parentModifier) / 2.0).toFloat()
                    item.targetAngle = (startAngle + offset + direction * magnitude * stepAngle)
                }
            }
        }
    }

    companion object {
        val None: ActionWheelUiState = ActionWheelUiStateData(center = null)
    }
}

data class ItemAnimatable(
    // Used for position around the wheel
    val angleDegree : Animatable<Float, *>,
    val alpha: Animatable<Float, *> = Animatable(0f),
    // Only use for dice roll animations
    var isAnimating: MutableState<Boolean> = mutableStateOf(false),
    val yOffset: Animatable<Float, *> = Animatable(0f), //  = remember(startingValue, endValue) { Animatable(0f) }
    val rotation: Animatable<Float, *> = Animatable(0f), //  = remember(startingValue, endValue) { Animatable(0f) }
    var displayFace: MutableState<DieResult?> = mutableStateOf(null) //  by remember(startingValue, endValue) { mutableStateOf(startingValue) }
)

/**
 * This file contains all the composable required to render a "Circular Action Menu".
 * The main entry point is [ActionWheel], which is driven by an instance of
 * [ActionWheelViewModel].
 */

@Composable
fun ActionWheel(
    uiState: ActionWheelUiStateData,
    ringSize: Dp = 250.jdp,
    borderSize: Dp = 20.jdp,
    maxSize: Dp = 300.jdp,
    showTip: Boolean = false,
    tipRotationDegree: Float = 0f,
    onAnimationFinished: () -> Unit,
) {
    val animationDurationMs = 300
    var previousState by remember { mutableStateOf<ActionWheelUiStateData?>(null) }
    val topAnimatable = remember { mutableStateMapOf<ButtonId, ItemAnimatable>() }
    val bottomAnimatable = remember { mutableStateMapOf<ButtonId, ItemAnimatable>() }
    // Track buttons until they removed again (alpha = 0). We cache them here to decouple all current buttons from the
    // ActionWheelUiState, which should only care about the "current" state.
    val topRenderButtons = remember { mutableStateListOf<ButtonData>() }
    val bottomRenderButtons = remember { mutableStateListOf<ButtonData>() }

    var buttonsEnabled by remember(uiState) { mutableStateOf(false) }
    var hoverText by remember { mutableStateOf<String?>(null) }
    // If a button has temporary primary focus, it will dim all other remaining buttons and disable their onClick handlers
    var temporaryPrimaryFocus by remember { mutableStateOf<ButtonId?>(null) }

    LaunchedEffect(Unit) {
        bottomRenderButtons.clear()
        bottomRenderButtons.addAll(uiState.bottomItems)
        topRenderButtons.clear()
        topRenderButtons.addAll(uiState.bottomItems)
    }

    // Trigger animations on state changes.
    // This effect is also responsible for tracking previous items until they are fully gone.
    LaunchedEffect(uiState) {
        hoverText = null
        if (uiState.lastActionWasUndo) {
            topAnimatable.clear()
            bottomAnimatable.clear()
            previousState = null
        }

        val from = previousState
        val to = uiState

        // 1) Make sure render list has union(from, to)
        val bottomUnion = (from?.bottomItems.orEmpty() + to.bottomItems).reversed().distinctBy { it.id }.reversed()
        bottomRenderButtons.clear()
        bottomRenderButtons.addAll(bottomUnion)
        val topUnion = (from?.topItems.orEmpty() + to.topItems).reversed().distinctBy { it.id }.reversed()
        topRenderButtons.clear()
        topRenderButtons.addAll(topUnion)

        // 2) Run animations. This will animate disappearing items to alpha = 0
        runWheelAnimations(
            from = from,
            to = to,
            topAnims = topAnimatable,
            bottomAnims = bottomAnimatable,
            animationDurationMs = animationDurationMs,
        )

        // 3) After animation completes, remove items that are gone
        val stillPresentBottomIds = to.bottomItems.map { it.id }.toSet()
        bottomRenderButtons.removeAll { it.id !in stillPresentBottomIds }
        val stillPresentTopIds = to.topItems.map { it.id }.toSet()
        topRenderButtons.removeAll { it.id !in stillPresentTopIds }

        previousState = uiState
        buttonsEnabled = true
        onAnimationFinished()
    }

    Box(
        modifier = Modifier
            .size(maxSize)
            .alpha(1f)
        ,
        contentAlignment = Alignment.Center
    ) {
        ActionWheelBackgroundRing(
            ringSize,
            borderSize,
            showTip,
            tipRotationDegree,
            // Ring should appear slightly before items
            animationDuration = if (uiState.lastActionWasUndo) 0 else animationDurationMs - 100,
        )
        WheelButtons(
            animationsCache = topAnimatable,
            buttons = topRenderButtons,
            wheelRadius = (ringSize - borderSize)/2f,
            buttonsClickable = buttonsEnabled,
            onHover = { text -> hoverText = text },
            primaryFocus = temporaryPrimaryFocus,
            onPrimaryFocus = { id, isPrimary ->
                temporaryPrimaryFocus = if (isPrimary) id else null
                buttonsEnabled = !isPrimary
            }
        )

        WheelButtons(
            animationsCache = bottomAnimatable,
            buttons = bottomRenderButtons,
            wheelRadius = (ringSize - borderSize)/2f,
            buttonsClickable = buttonsEnabled,
            onHover = { text -> hoverText = text },
            primaryFocus = temporaryPrimaryFocus,
            onPrimaryFocus = { id, isPrimary ->
                temporaryPrimaryFocus = if (isPrimary) id else null
                buttonsEnabled = !isPrimary
            }
        )
        HoverText(hoverText, JervisTheme.homeTeamColor)
    }
}
private suspend fun runWheelAnimations(
    from: ActionWheelUiStateData?,
    to: ActionWheelUiStateData,
    topAnims: MutableMap<ButtonId, ItemAnimatable>,
    bottomAnims: MutableMap<ButtonId, ItemAnimatable>,
    animationDurationMs: Int,
) = coroutineScope {
    val topJob = async {
        animateRegion(
            animationsCache = topAnims,
            to = to.topItems,
            from = from?.topItems.orEmpty(),
            animationMode = if (to.lastActionWasUndo) ButtonLayoutMode.UNDO else to.topAnimationType,
            animationDuration = animationDurationMs
        )
    }
    val bottomJob = async {
        animateRegion(
            animationsCache = bottomAnims,
            to = to.bottomItems,
            from = from?.bottomItems.orEmpty(),
            animationMode = if (to.lastActionWasUndo) ButtonLayoutMode.UNDO else to.bottomAnimationType,
            animationDuration = animationDurationMs
        )
    }


    topJob.await()
    bottomJob.await()
}

private suspend fun animateRegion(
    animationsCache: MutableMap<ButtonId, ItemAnimatable>,
    from: List<ButtonData>,
    to: List<ButtonData>,
    animationMode: ButtonLayoutMode,
    animationDuration: Int,
) = coroutineScope {

    val shortAnimation = (animationDuration * 2/3f).roundToInt()
    val appearing = to.filter { new -> from.none { it.id == new.id } }
    val staying   = to.filter { new -> from.any { it.id == new.id } }
    val disappearing = from.filter { old -> to.none { it.id == old.id } }
    val animatingRoll = to.filter { it.animateRoll != null }

    // Debug information
    //     println("AnimationMode [$animationDuration]: $animationMode")
    //     println("Appearing: ${appearing.size}")
    //     println("Staying: ${staying.size}")
    //     println("Disappearing: ${disappearing.size}")

    // Make sure that all buttons are in the animation cache
    appearing.forEach {
        animationsCache.getOrPut(it.id) {
            ItemAnimatable(
                Animatable(it.defaultStartingAngle),
            )
        }
    }

    when (animationMode) {
        ButtonLayoutMode.STABLE -> {
            // Do nothing
        }
        ButtonLayoutMode.DELAY -> {

        }
        ButtonLayoutMode.ANIMATING_ROLL -> {
            disappearing.forEach {
                animationsCache[it.id]!!.let { anim ->
                    launch {
                        anim.alpha.animateTo(0f, tween(shortAnimation))
                    }
                }
            }
            if (disappearing.isNotEmpty()) {
                delay(shortAnimation.toLong())
            }
            animatingRoll.forEach {
                launch {
                    animationsCache[it.id]!!.let { anim ->
                        anim.alpha.snapTo(1f)
                        anim.angleDegree.snapTo(it.targetAngle)
                        anim.isAnimating.value = true
                        val randJob = launch {
                            // For "complex" dice like Block dice, changing face too quick
                            // makes it look really messy. With this approach we change once
                            // near the top of the arc before switching to correct result.
                            // From manual testing, this seems to be a good trade-off between
                            // "hiding" the true result a bit and making it look more "random".
                            it.animateRoll?.let { rollAnim ->
                                anim.displayFace.value = rollAnim.startingValue
                                delay(100)
                                anim.displayFace.value = rollAnim.intermediateValue
                                delay(100)
                                anim.displayFace.value = rollAnim.endValue
                            }
                        }

                        // Rotation while airborne
                        val rotJob = launch {
                            anim.rotation.animateTo(
                                targetValue = 360f,
                                animationSpec = tween(300, easing = LinearEasing)
                            )
                        }

                        // Jump up in the air
                        anim.yOffset.animateTo(
                            targetValue = -75f,
                            animationSpec = tween(150, easing = LinearOutSlowInEasing)
                        )

                        // Fall to the ground again
                        anim.yOffset.animateTo(
                            targetValue = 0f,
                            animationSpec = tween(150, easing = FastOutLinearInEasing)
                        )
                        rotJob.cancel()
                        randJob.cancel()
                        anim.rotation.snapTo(0f)
                        anim.displayFace.value = it.animateRoll!!.endValue
                        it.animateRoll?.additionalDelayAfterRoll?.let { additionalDelay ->
                            delay(additionalDelay)
                        }
                        anim.isAnimating.value = false
                    }
                }
            }
        }
        ButtonLayoutMode.EXPEND_NEW_SUBMENU -> {
            disappearing.forEach {
                animationsCache[it.id]!!.let { anim ->
                    launch {
                        anim.alpha.animateTo(0f, tween(shortAnimation))
                    }
                }
            }
            appearing.forEach {
                animationsCache[it.id]!!.let { anim ->
                    launch {
                        anim.alpha.animateTo(1f, tween(animationDuration))
                    }
                    launch {
                        val target = shortestPathToTaget(anim.angleDegree.value, it.targetAngle)
                        anim.angleDegree.animateTo(target, tween(animationDuration))
                    }
                }
            }
            staying.forEach {
                animationsCache[it.id]!!.let { anim ->
                    launch {
                        anim.angleDegree.animateTo(it.targetAngle, tween(animationDuration))
                    }
                }
            }
        }
        ButtonLayoutMode.CONTRACT_NEW_SUBMENU -> {
            appearing.forEach {
                animationsCache[it.id]!!.let { anim ->
                    launch {
                        anim.alpha.animateTo(1f, tween(animationDuration))
                    }
                    launch {
                        anim.angleDegree.snapTo(it.targetAngle)
                    }
                }
            }
            disappearing.forEach {
                animationsCache[it.id]!!.let { anim ->
                    launch {
                        anim.alpha.animateTo(0f, tween(shortAnimation))
                        // anim.alpha.animateTo(0f, tween((shortAnimation * 0.8).roundToInt()))
                    }
                    launch {
                        val target = shortestPathToTaget(anim.angleDegree.value, it.defaultStartingAngle)
                        anim.angleDegree.animateTo(target, tween(shortAnimation))
                    }
                }
            }
            staying.forEach {
                animationsCache[it.id]!!.let { anim ->
                    launch {
                        anim.angleDegree.animateTo(it.targetAngle, tween(animationDuration))
                    }
                }
            }
        }

        ButtonLayoutMode.UNDO -> {
            appearing.forEach {
                animationsCache[it.id]!!.let { anim ->
                    launch {
                        anim.angleDegree.snapTo(it.targetAngle)
                        anim.alpha.snapTo(1f)
                    }
                }
            }
            staying.forEach {
                animationsCache[it.id]!!.let { anim ->
                    launch {
                        anim.angleDegree.snapTo(it.targetAngle)
                        anim.alpha.snapTo(1f)
                    }
                }
            }
            disappearing.forEach {
                animationsCache[it.id]!!.let { anim ->
                    launch {
                        anim.angleDegree.snapTo(it.defaultStartingAngle)
                        anim.alpha.snapTo(0f)
                    }
                }
            }
        }
    }
}

@Composable
private fun WheelButtons(
    animationsCache: MutableMap<ButtonId, ItemAnimatable>,
    buttons: List<ButtonData>,
    wheelRadius: Dp,
    buttonsClickable: Boolean,
    onHover: (String?) -> Unit,
    primaryFocus: ButtonId? = null,
    onPrimaryFocus: (ButtonId, Boolean) -> Unit = { _, _ -> },
) {
    val radiusPx = with(LocalDensity.current) { wheelRadius.toPx() }
    Box(Modifier.size(wheelRadius), Alignment.Center) {
        buttons.reversed().forEachIndexed { i, item ->
            val animationData = animationsCache[item.id]
            if (animationData != null) {
                val angle by animationData.angleDegree.asState()
                val alpha by animationData.alpha.asState()
                MenuItemButton(
                    item,
                    animationData,
                    angle,
                    alpha,
                    radiusPx,
                    isOnClickEnabled = buttonsClickable || (item.id == primaryFocus),
                    isFullyVisible = (primaryFocus == null) || (item.id == primaryFocus),
                    onHover,
                    onPrimaryFocus,
                )
            }
        }
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
                .dropShadow(shape = shape) {
                    this.color = Color.Black.copy(1f)
                    this.offset = Offset.Zero
                    this.radius = 16.dp.toPx()
                }
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

// Calculate the shortest distance (in degrees) to a target when moving across a ring
private fun shortestPathToTaget(current: Float, desired: Float): Float {
    val delta = ((((desired - current) % 360f) + 540f) % 360f) - 180f
    return current + delta
}

// Composable responsible for rending the actual action/dice button
@Composable
private fun MenuItemButton(
    item: ButtonData,
    animationData: ItemAnimatable,
    angle: Float,
    alpha: Float,
    radiusPx: Float,
    isOnClickEnabled: Boolean,
    isFullyVisible: Boolean,
    // Show or hide a hover description inside the wheel
    onHover: (String?) -> Unit,
    onPrimaryFocus: (ButtonId, Boolean) -> Unit = { _, _ -> },
) {
    if (alpha <= 0f) return
    val offset = getOffset(angle, radiusPx)
    Box(
        // It would be nice to be able to control `alpha` from this location, but there seems to be
        // issues with this when using the new 1.9 `.dropShadow()`. So for now, it is up to each
        // item composable to drive their animation updates
        modifier = Modifier
            .offset {offset.toIntOffset() }
    ) {
        when (item) {
            is ActionButtonData -> {
                ActionButton(
                    item.label,
                    alpha,
                    item.icon,
                    isOnClickEnabled = if (!isOnClickEnabled) false else item.enabled,
                    isFullyVisible = if (!isFullyVisible) false else item.enabled,
                    onHover = onHover,
                    onClick = item.action
                )
            }
            is DieButtonData<*> -> {
                @Suppress("UNCHECKED_CAST")
                ExpandableDiceSelector(
                    item as DieButtonData<DieResult>,
                    animationData,
                    isOnClickEnabled = isOnClickEnabled,
                    isFullyVisible = isFullyVisible,
                    onClick = item.action,
                    onHover = onHover,
                    alpha = alpha,
                    onExpandedChanged = onPrimaryFocus,
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
    animationDuration: Int,
) {
    // We only ever fade in the ring. If it ever fades out again, this is handled
    // by fading out the entire action wheel in upper layers.
    var visible by remember { mutableStateOf(true) }
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

    AnimatedVisibility(
        visible = visible,
        enter = fadeIn(
            animationSpec = tween(durationMillis = max(0, animationDuration), easing = FastOutLinearInEasing)
        ),
        exit = fadeOut(
            animationSpec = tween(durationMillis = max(0, animationDuration), easing = FastOutLinearInEasing)
        )
    ) {
        Canvas(
            modifier = Modifier
                .fillMaxSize()
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
}

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
    val animationDuration = 100
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

    val baselineShift = remember(displayedMessage) {
        // Aligning the text in the center of a colored background is pretty tricky
        // due to different fonts and how Compose treat ascent / descent. This is
        // mostly a problem for the direction hover as it includes unicode arrows.
        // So for now we just account that specific case. Until we can come up with
        // a better way of doing it.
        if (displayedMessage?.startsWith("Direction: ") == true) {
            BaselineShift.None
        } else {
            BaselineShift(-0.05f)
        }
    }
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
            fontFamily = JervisTheme.defaultFontFamily(), // Needed to support direction arrows on Web
            fontSize = fontSize,
            color = textColor,
            fontWeight = fontWeight,
            baselineShift = baselineShift
        ),
    )
}

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun ActionButton(
    description: () -> String,
    alpha: Float,
    icon: ActionIcon,
    isOnClickEnabled: Boolean = true,
    isFullyVisible: Boolean = true,
    onHover: (String?) -> Unit = {},
    onClick: () -> Unit = { },
) {
    val icon = remember(icon) { IconFactory.getActionIcon(icon) }
    val colorFilter = ColorFilter.tint(JervisTheme.black.copy(0.1f), BlendMode.Darken)
    var isHover by remember(icon) { mutableStateOf(false) }
    // Modifier order is a mess to get shadows to animate correctly. The current setup works,
    // but I suspect its performance could be improved. But this requires further experimentation.
    Box(
        modifier = Modifier
            .dropShadow(CircleShape) {
                color = Color.Black.copy(0.75f * alpha)
                offset = Offset.Zero
                radius = 8.dp.toPx()
                this.alpha = alpha
            }
            .graphicsLayer {
                this.alpha = alpha
                compositingStrategy = CompositingStrategy.Offscreen
            }
            .clip(CircleShape)
            .border(2.dp, Color.Black.copy(0.5f), CircleShape)
            .applyIf(isOnClickEnabled) {
                this.pointerInput(Unit) {
                    awaitPointerEventScope {
                        while (true) {
                            val e = awaitPointerEvent()
                            when (e.type) {
                                PointerEventType.Enter -> {
                                    isHover = true
                                    onHover(description())
                                }
                                PointerEventType.Exit  -> {
                                    isHover = false
                                    onHover(null)
                                }
                                PointerEventType.Press -> {
                                    // We are about to trigger onClick, so prevent
                                    // other layers from reacting too early
                                    e.changes.forEach { it.consume() }
                                }
                                PointerEventType.Release -> {
                                    e.changes.forEach { it.consume() }
                                    onClick()
                                }
                            }
                        }
                    }
                }
            }
    ) {
        Image(
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
                    dropShadow(shape = CircleShape) {
                        color = JervisTheme.black.copy(1f)
                        offset = Offset.Zero
                        radius = 12.dp.toPx()
                    }
                }
                .applyIf(!dropShadow) {
                    dropShadow(shape = CircleShape) {
                        color = JervisTheme.black.copy(0.3f)
                        offset = Offset.Zero
                        radius = 4.dp.toPx()
                    }
                }
                .applyIf(enabled) {
                    this.pointerInput(Unit) {
                        awaitPointerEventScope {
                            while (true) {
                                val e = awaitPointerEvent()
                                when (e.type) {
                                    PointerEventType.Enter -> {
                                        colorFilter = ColorFilter.tint(JervisTheme.black.copy(0.1f), BlendMode.Darken)
                                    }
                                    PointerEventType.Exit  -> {
                                        colorFilter = null
                                    }
                                    PointerEventType.Press -> {
                                        // We are about to trigger onClick, so prevent
                                        // other layers from reacting too early
                                        e.changes.forEach { it.consume() }
                                        onClick(coin)
                                    }
                                    PointerEventType.Release -> {
                                        e.changes.forEach { it.consume() }
                                    }
                                }
                            }
                        }
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
fun ExpandableDiceSelector(
    button: DieButtonData<DieResult>,
    animationData: ItemAnimatable,
    isOnClickEnabled: Boolean = true,
    isFullyVisible: Boolean = true,
    onClick: () -> Unit = { },
    onHover: (String?) -> Unit = { },
    alpha: Float = 1f,
    onExpandedChanged: (ButtonId, Boolean) -> Unit = { _, _ -> },
) {

    // Current state tracking
    var currentDiceValue by remember(button) { mutableStateOf(button.diceValue) }
    val density = LocalDensity.current
    val diceList = button.options.toMutableList().apply {
        remove(button.diceValue)
        add(0, button.diceValue)
    }
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

    val rows = when (button.diceValue) {
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
    val buttonSize = IconFactory.getDiceSizeDp(currentDiceValue)
    val (buttonWidth, buttonHeight) = buttonSize
    val maxWidthDp = (backgroundPadding * 2) + (spacingBetweenItems * (itemsPrRow - 1)) + (buttonWidth * itemsPrRow)
    val backgroundHeight = (buttonHeight*rows + backgroundPadding) + (backgroundPadding/2f)*(rows-1)

    // Determine the placement of popup
    val (popupDirection, popupOffset) = remember(button) {
        when (button.preferLtr) {
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
        LaunchedEffect(button) {
            bgWidthDp.snapTo(maxWidthDp.value)
            // launch { bgWidthDp.animateTo(maxWidthDp.value, animation) }
            launch { bgAlpha.animateTo(1f) }
        }
    } else {
        LaunchedEffect(button) {
            bgWidthDp.snapTo(0f)
            // launch { bgWidthDp.animateTo(0f, animation) }
            launch { bgAlpha.animateTo(0f) }
        }
    }

    val isAnimating by animationData.isAnimating
    val yOffset by animationData.yOffset.asState()
    val rotation by animationData.rotation.asState()
    val displayFace by animationData.displayFace

    // The visible button, both the normal and jumping one.
    Box(
        modifier = Modifier
            .alpha(if (!isFullyVisible) 0.3f else 1f)
            .applyIf(isAnimating) {
                graphicsLayer {
                    translationY = yOffset
                    rotationZ = rotation
                }
            }
        ,
        contentAlignment = Alignment.CenterStart,
    ) {
        DiceButton(
            DpSize(buttonWidth,  buttonHeight),
            alpha,
            value = if (isAnimating) displayFace!! else currentDiceValue,
            label = button.label,
            clickEnabled = isOnClickEnabled,
            onHover = {
                if (!isAnimating) {
                    onHover(it)
                }
            },
            onClick = {
                if (button.expandable) {
                    expanded = !expanded
                    onExpandedChanged(button.id, expanded)
                } else {
                    button.diceValue = currentDiceValue
                    onClick()
                }
            },
            dropShadowColor = shadowColor,
        )
    }

    if (expanded) {
        Popup(
            alignment = Alignment.TopStart,
            offset = popupOffset,
            onDismissRequest = {
                expanded = false
                onHover(null)
                onExpandedChanged(button.id, expanded)
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
                                row.forEachIndexed { index, dieValue ->
                                    val currentBgWidth = bgWidthDp.value.dp
                                    val alpha = when (index == 0 || currentBgWidth > 52.dp * (index + 1)) {
                                        true -> bgAlpha.value
                                        false -> 0f
                                    }
                                    DiceButton(
                                        buttonSize,
                                        alpha,
                                        value = dieValue,
                                        label = { null }, // Hide onHover values for the dice selector (for now)
                                        clickEnabled = true,
                                        onHover = onHover,
                                        onClick = {
                                            if (!expanded) {
                                                expanded = true
                                            } else {
                                                expanded = false
                                                currentDiceValue = diceList[rowIndex*(diceList.size / rows) + index]
                                                button.diceValue = currentDiceValue
                                            }
                                            if (rowIndex != 0 || index != 0) {
                                                onHover(null)
                                            }
                                            onExpandedChanged(button.id, expanded)
                                        },
                                        dropShadow = false,
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

@Composable
private fun SimpleDiceButton(
    button: DieButtonData<*>,
    animationData: ItemAnimatable,
    isOnClickEnabled: Boolean = true,
    isFullyVisible: Boolean = true,
    onClick: () -> Unit = { },
    onHover: (String?) -> Unit = { },
    alpha: Float = 1f,
) {
    val shadowColor = Color.Black
    val buttonSize = IconFactory.getDiceSizeDp(button.diceValue)
    val (buttonWidth, buttonHeight) = buttonSize

    val isAnimating by animationData.isAnimating
    val yOffset by animationData.yOffset.asState()
    val rotation by animationData.rotation.asState()
    val displayFace by animationData.displayFace

    // The visible button, both the normal and jumping one.
    Box(
        modifier = Modifier.alpha(if (isFullyVisible) 1f else 0.3f),
        contentAlignment = Alignment.CenterStart,
    ) {
        Box(
            modifier = Modifier
                .applyIf(isAnimating) {
                    graphicsLayer {
                        translationY = yOffset
                        rotationZ = rotation
                    }
                }
            ,
            contentAlignment = Alignment.Center
        ) {
            DiceButton(
                DpSize(buttonWidth,  buttonHeight),
                alpha,
                value = if (isAnimating) displayFace!! else button.diceValue,
                label = button.label,
                clickEnabled = (isAnimating || !isOnClickEnabled),
                onHover = onHover,
                onClick = onClick,
                dropShadowColor = shadowColor,
            )
        }
    }
}

@OptIn(ExperimentalComposeUiApi::class)
@Composable
private fun DiceButton(
    buttonSize: DpSize,
    alpha: Float,
    value: DieResult,
    label: () -> String?,
    clickEnabled: Boolean = true,
    onHover: (String?) -> Unit = {},
    onClick: () -> Unit = {},
    dropShadowColor: Color = Color.Black,
    dropShadow: Boolean = true,
) {
    val instanceId = remember { Random.nextLong() }
    val useSelectedColorAsHover = false
    var hover: Boolean by remember { mutableStateOf(false) }
    var colorFilter by remember { mutableStateOf<ColorFilter?>(null) }
    val shape: Shape = remember(value) {
        when (value) {
            is D8Result -> D8Shape
            is D6Result -> D6Shape
            else -> RoundedCornerShape(4.dp)
        }
    }
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

    fun showHoverEffect() {
        onHover(label())
        if (!useSelectedColorAsHover) {
            colorFilter = ColorFilter.tint(JervisTheme.black.copy(0.1f), BlendMode.Darken)
        }
    }

    LaunchedEffect(clickEnabled) {
        if (clickEnabled && hover) {
            showHoverEffect()
        }
    }

    Box(
        modifier = Modifier
            .size(buttonSize)
            .onPointerEvent(PointerEventType.Enter) {
                if (!hover) {
                    hover = true
                    showHoverEffect()
                }
            }
            .onPointerEvent(PointerEventType.Exit) {
                if (hover) {
                    hover = false
                    onHover(null)
                    if (!useSelectedColorAsHover) {
                        colorFilter = null
                    }
                }
            }
            .applyIf(dropShadow) {
                dropShadow(shape = shape) {
                    color = dropShadowColor.copy(alpha = 0.75f)
                    offset = Offset.Zero
                    radius = 12.dp.toPx()
                    this.alpha = alpha * alpha
                }
            }
            .graphicsLayer {
                this.alpha = alpha
                this.compositingStrategy = CompositingStrategy.Offscreen
                this.shape = shape
                this.clip = true
            }
            .applyIf(clickEnabled) {
                onPointerEvent(PointerEventType.Press) {
                    onClick()
                }
            }
        ,
        contentAlignment = Alignment.Center
    ) {
        Image(
            bitmap = bitmap,
            contentDescription = value.value.toString(),
            modifier = Modifier.fillMaxSize(),
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
