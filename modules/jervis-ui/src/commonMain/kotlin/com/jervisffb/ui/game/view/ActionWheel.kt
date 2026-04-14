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
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
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
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.draw.dropShadow
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.CompositingStrategy
import androidx.compose.ui.graphics.FilterQuality
import androidx.compose.ui.graphics.ImageShader
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathFillType
import androidx.compose.ui.graphics.ShaderBrush
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.TileMode
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.PointerEventType
import androidx.compose.ui.input.pointer.onPointerEvent
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
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
import com.jervisffb.engine.reports.ReportStartingExtraTime.message
import com.jervisffb.engine.rules.DiceRollType
import com.jervisffb.jervis_ui.generated.resources.Res
import com.jervisffb.jervis_ui.generated.resources.jervis_brush_chalk
import com.jervisffb.ui.SETTINGS_MANAGER
import com.jervisffb.ui.game.dialogs.wheel.ActionButtonData
import com.jervisffb.ui.game.dialogs.wheel.ButtonData
import com.jervisffb.ui.game.dialogs.wheel.ButtonId
import com.jervisffb.ui.game.dialogs.wheel.ButtonLayoutMode
import com.jervisffb.ui.game.dialogs.wheel.CoinButtonData
import com.jervisffb.ui.game.dialogs.wheel.DieButtonData
import com.jervisffb.ui.game.icons.ActionIcon
import com.jervisffb.ui.game.icons.DiceColor
import com.jervisffb.ui.game.icons.IconFactory
import com.jervisffb.ui.game.view.utils.D6Shape
import com.jervisffb.ui.game.view.utils.D8Shape
import com.jervisffb.ui.menu.dice.BB2025DiceColorConfig
import com.jervisffb.ui.toRadians
import com.jervisffb.ui.utils.applyIf
import com.jervisffb.ui.utils.jdp
import com.jervisffb.ui.utils.jsp
import com.jervisffb.ui.utils.scalePixels
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.imageResource
import kotlin.math.cos
import kotlin.math.hypot
import kotlin.math.roundToInt
import kotlin.math.sin
import kotlin.time.Duration.Companion.milliseconds

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
 */
@Composable
fun ActionWheel(
    uiState: ActionWheelUiState,
    offsetDelegate: (ActionWheelUiState?) -> Unit,
    ringSize: Dp = 250.jdp,
    borderSize: Dp = 20.jdp,
    maxSize: Dp = 300.jdp,
    maxRingAlpha: Float = 0.50f,
    showTip: Boolean = false,
    tipRotationDegree: Float = 0f,
    onAnimationFinished: () -> Unit,
) {
    val animationDurationMs = when {
        uiState.enableAnimation && uiState.isHiding() -> 100 // Hiding should be faster
        uiState.enableAnimation -> 300
        else -> 0
    }
    var previousState by remember { mutableStateOf<ActionWheelUiState>(NoActionWheel) }
    val topAnimatable = remember { mutableStateMapOf<ButtonId, ItemAnimatable>() }
    val bottomAnimatable = remember { mutableStateMapOf<ButtonId, ItemAnimatable>() }
    // Track buttons until they removed again (alpha = 0). We cache them here to decouple all current buttons from the
    // ActionWheelUiState, which should only care about the "current" state.
    val topRenderButtons = remember { mutableStateListOf<ButtonData>() }
    val bottomRenderButtons = remember { mutableStateListOf<ButtonData>() }
    val ringAlpha = remember { Animatable(0f) }

    var buttonsEnabled by remember(uiState) { mutableStateOf(false) }
    var hoverText by remember { mutableStateOf<String?>(null) }
    // If a button has temporary primary focus, it will dim all other remaining buttons and disable their onClick handlers
    var temporaryPrimaryFocus by remember { mutableStateOf<ButtonId?>(null) }

    LaunchedEffect(Unit) {
        bottomRenderButtons.clear()
        bottomRenderButtons.addAll(uiState.bottomItems)
        topRenderButtons.clear()
        topRenderButtons.addAll(uiState.topItems)
    }

    // Update the wheel position synchronously before the frame is drawn, so the wheel
    // never renders at a stale position (e.g. when bottomMessage stays the same across
    // two states that are at different pitch coordinates).
    DisposableEffect(uiState) {
        offsetDelegate(uiState)
        onDispose { }
    }

    // Trigger animations on state changes.
    // This effect is also responsible for tracking previous items until they are fully gone.
    LaunchedEffect(uiState) {
        hoverText = null
        if (uiState.animationOnly) {
            topAnimatable.clear()
            bottomAnimatable.clear()
            // With "animation only", we normally does not want to go back to the
            // "previous state", so we always reset it here.
            previousState = NoActionWheel
        }

        val from = previousState
        val to = uiState

        // 1) Make sure render list has union(from, to)
        val bottomUnion = (from.bottomItems + to.bottomItems).reversed().distinctBy { it.id }.reversed()
        bottomRenderButtons.clear()
        bottomRenderButtons.addAll(bottomUnion)
        val topUnion = (from.topItems + to.topItems).reversed().distinctBy { it.id }.reversed()
        topRenderButtons.clear()
        topRenderButtons.addAll(topUnion)

        // 2) Run animations. This will animate disappearing items to alpha = 0
        runWheelAnimations(
            from = from,
            to = to,
            topAnims = topAnimatable,
            bottomAnims = bottomAnimatable,
            animationDurationMs = animationDurationMs,
            maxRingAlpha = if (uiState.animationOnly) 0f else maxRingAlpha,
            ringAlpha = ringAlpha,
        )

        // 3) After animation completes, remove items that are gone
        val stillPresentBottomIds = to.bottomItems.map { it.id }.toSet()
        bottomRenderButtons.removeAll { it.id !in stillPresentBottomIds }
        val stillPresentTopIds = to.topItems.map { it.id }.toSet()
        topRenderButtons.removeAll { it.id !in stillPresentTopIds }

        // When a Wheel is being hidden, we want to reset everything, so we don't accidentially show old
        // state when showing the wheel the next time.
        if (to.isHiding()) {
            bottomRenderButtons.clear()
            bottomAnimatable.clear()
            topRenderButtons.clear()
            topAnimatable.clear()
        }

        previousState = uiState
        buttonsEnabled = true
        onAnimationFinished()
    }
    //  Do not render anything if there is no ui, state, but allow the stack tracking logic to run
    // as that allows us to reset things correctly.
    Box(
        modifier = Modifier
            .size(maxSize)
        ,
        contentAlignment = Alignment.Center
    ) {
        ActionWheelBackgroundRing(
            ringAlpha,
            ringSize,
            borderSize,
            showTip,
            tipRotationDegree,
        )
        WheelButtons(
            animationsCache = topAnimatable,
            buttons = topRenderButtons,
            wheelRadius = (ringSize - borderSize) / 2f,
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
            wheelRadius = (ringSize - borderSize) / 2f,
            buttonsClickable = buttonsEnabled,
            onHover = { text -> hoverText = text },
            primaryFocus = temporaryPrimaryFocus,
            onPrimaryFocus = { id, isPrimary ->
                temporaryPrimaryFocus = if (isPrimary) id else null
                buttonsEnabled = !isPrimary
            }
        )

        if (hoverText.isNullOrEmpty() && (uiState.bottomMessage?.isNotBlank() == true)) {
            RingMessage(
                message = uiState.bottomMessage ?: "",
                angle = 90f,
                radius = (ringSize - borderSize) / 2f,
            )
        } else {
            TooltipText(hoverText)
        }
    }
}
private suspend fun runWheelAnimations(
    from: ActionWheelUiState,
    to: ActionWheelUiState,
    topAnims: MutableMap<ButtonId, ItemAnimatable>,
    bottomAnims: MutableMap<ButtonId, ItemAnimatable>,
    animationDurationMs: Int,
    maxRingAlpha: Float,
    ringAlpha: Animatable<Float, AnimationVector1D>,
) = coroutineScope {
    val topJob = async {
        animateRegion(
            animationsCache = topAnims,
            to = to.topItems,
            from = from.topItems,
            animationMode = if (to.isLastActionUndo()) ButtonLayoutMode.EXPAND_UNDO else to.topAnimationType,
            animationDuration = animationDurationMs
        )
    }
    val bottomJob = async {
        animateRegion(
            animationsCache = bottomAnims,
            to = to.bottomItems,
            from = from.bottomItems,
            animationMode = if (to.isLastActionUndo()) ButtonLayoutMode.EXPAND_UNDO else to.bottomAnimationType,
            animationDuration = animationDurationMs
        )
    }
    val ringJob = async {
        val target = if (to.topItems.isNotEmpty() || to.bottomItems.isNotEmpty()) maxRingAlpha else 0f
        val isUndo = to.isLastActionUndo()
        if (isUndo) {
            ringAlpha.snapTo(target)
        } else {
            ringAlpha.animateTo(target, tween(animationDurationMs, easing = LinearEasing))
        }
    }
    awaitAll(topJob, bottomJob, ringJob)
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
    //    println("Items: ${from.size} vs. ${to.size}")
    //    println("AnimationMode [$animationDuration]: $animationMode")
    //    println("Appearing: ${appearing.size}")
    //    println("Staying: ${staying.size}")
    //    println("Disappearing: ${disappearing.size}")

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
                delay(shortAnimation.toLong().milliseconds)
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
                                delay(100.milliseconds)
                                anim.displayFace.value = rollAnim.intermediateValue
                                delay(100.milliseconds)
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
                animationsCache[it.id]?.let { anim ->
                    launch {
                        anim.alpha.animateTo(0f, tween(shortAnimation))
                    }
                } ?: error("Missing animation for ${it.id}")
            }
            appearing.forEach {
                animationsCache[it.id]?.let { anim ->
                    launch {
                        anim.alpha.animateTo(1f, tween(animationDuration))
                    }
                    launch {
                        val target = shortestPathToTaget(anim.angleDegree.value, it.targetAngle)
                        anim.angleDegree.animateTo(target, tween(animationDuration))
                    }
                } ?: error("Missing animation for ${it.id}")
            }
            staying.forEach {
                animationsCache[it.id]?.let { anim ->
                    launch {
                        anim.angleDegree.animateTo(it.targetAngle, tween(animationDuration))
                    }
                } ?: error("Missing animation for ${it.id}")
            }
        }
        ButtonLayoutMode.CONTRACT_NEW_SUBMENU -> {
            appearing.forEach {
                animationsCache[it.id]?.let { anim ->
                    launch {
                        anim.alpha.animateTo(1f, tween(animationDuration))
                    }
                    launch {
                        anim.angleDegree.snapTo(it.targetAngle)
                    }
                } ?: error("Missing animation for ${it.id}")
            }
            disappearing.forEach {
                animationsCache[it.id]?.let { anim ->
                    launch {
                        anim.alpha.animateTo(0f, tween(shortAnimation))
                        // anim.alpha.animateTo(0f, tween((shortAnimation * 0.8).roundToInt()))
                    }
                    launch {
                        val target = shortestPathToTaget(anim.angleDegree.value, it.defaultStartingAngle)
                        anim.angleDegree.animateTo(target, tween(shortAnimation))
                    }
                } ?: error("Missing animation for ${it.id}")
            }
            staying.forEach {
                animationsCache[it.id]?.let { anim ->
                    launch {
                        anim.angleDegree.animateTo(it.targetAngle, tween(animationDuration))
                    }
                } ?: error("Missing animation for ${it.id}")
            }
        }

        ButtonLayoutMode.EXPAND_UNDO -> {
            appearing.forEach {
                animationsCache[it.id]?.let { anim ->
                    launch {
                        anim.angleDegree.snapTo(it.targetAngle)
                        anim.alpha.snapTo(1f)
                    }
                } ?: error("Missing animation for ${it.id}")
            }
            staying.forEach {
                animationsCache[it.id]?.let { anim ->
                    launch {
                        anim.angleDegree.snapTo(it.targetAngle)
                        anim.alpha.snapTo(1f)
                    }
                } ?: error("Missing animation for ${it.id}")
            }
            disappearing.forEach {
                animationsCache[it.id]?.let { anim ->
                    launch {
                        anim.angleDegree.snapTo(it.defaultStartingAngle)
                        anim.alpha.snapTo(0f)
                    }
                } ?: error("Missing animation for ${it.id}")
            }
        }
        ButtonLayoutMode.HIDE -> {
            appearing.forEach {
                animationsCache[it.id]?.let { anim ->
                    launch {
                        anim.alpha.animateTo(0f, tween(shortAnimation))
                    }
                } ?: error("Missing animation for ${it.id}")
            }
            staying.forEach {
                animationsCache[it.id]?.let { anim ->
                    launch {
                        anim.alpha.animateTo(0f, tween(shortAnimation))
                    }
                } ?: error("Missing animation for ${it.id}")
            }
            disappearing.forEach {
                animationsCache[it.id]?.let { anim ->
                    launch {
                        anim.alpha.animateTo(0f, tween(shortAnimation))
                    }
                } ?: error("Missing animation for ${it.id}")
            }
        }
        ButtonLayoutMode.CONTRACT_UNDO -> {
            appearing.forEach {
                animationsCache[it.id]?.let { anim ->
                    launch {
                        anim.alpha.snapTo(0f)
                    }
                } ?: error("Missing animation for ${it.id}")
            }
            staying.forEach {
                animationsCache[it.id]?.let { anim ->
                    launch {
                        anim.alpha.snapTo(0f)
                    }
                } ?: error("Missing animation for ${it.id}")
            }
            disappearing.forEach {
                animationsCache[it.id]?.let { anim ->
                    launch {
                        anim.alpha.snapTo(0f)
                    }
                } ?: error("Missing animation for ${it.id}")
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
        buttons.reversed().forEachIndexed { _, item ->
            key(item.id) {
                val animationData = animationsCache[item.id]
                if (animationData != null) {
                    MenuItemButton(
                        item,
                        animationData,
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
}

/**
 * Composable responsible for rendering a message that is replacing buttons
 * on either the bottom or top, but it is generic enough to handle any location
 */
@Composable
private fun RingMessage(
    message: String,
    angle: Float,
    radius: Dp,
) {
    val radiusPx = with(LocalDensity.current) { radius.toPx() }
    val offset = remember(angle, message) { getOffset(angle, radiusPx) }

    // Message box with a "brush" background
    // Attempt to align it so it looks on the line with the team features row
    Box(
        modifier = Modifier
            // Let the text box expand to any length. Any text here should be "reasonable".
            .wrapContentSize(unbounded = true)
            .offset { offset.toIntOffset() }
        ,
        contentAlignment = Alignment.BottomCenter
    ) {
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
        Box(
            modifier = Modifier
                .defaultMinSize(minHeight = 54.jdp)
                .drawWithContent {
                    // Create fade brush for left and right edges
                    val fadeBrush = Brush.horizontalGradient(
                        0f to Color.Transparent,
                        0.25f to Color.Black,
                        0.75f to Color.Black,
                        1f to Color.Transparent,
                        startX = 0f,
                        endX = size.width
                    )
                    // Draw the background with the fade mask
                    with(drawContext.canvas) {
                        saveLayer(
                            bounds = Rect(0f, 0f, size.width, size.height),
                            paint = androidx.compose.ui.graphics.Paint()
                        )
                        drawRect(
                            brush = imageBrush,
                            size = size,
                            alpha = 0.8f,
                            colorFilter = ColorFilter.tint(JervisTheme.black)
                        )
                        drawRect(
                            brush = SolidColor(JervisTheme.black.copy(0.2f)),
                            size = size
                        )
                        drawRect(
                            brush = fadeBrush,
                            size = size,
                            blendMode = BlendMode.DstIn
                        )
                        restore()
                    }
                    drawContent()
                }
            ,
            contentAlignment = Alignment.Center
        ) {
            Text(
                modifier = Modifier.padding(horizontal = 100.jdp, vertical = 16.jdp),
                text = message,
                textAlign = TextAlign.Center,
                lineHeight = 1.em,
                maxLines = 1,
                fontWeight = FontWeight.Bold,
                fontSize = 20.jsp,
                color = JervisTheme.white,
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
    radiusPx: Float,
    isOnClickEnabled: Boolean,
    isFullyVisible: Boolean,
    // Show or hide a hover description inside the wheel
    onHover: (String?) -> Unit,
    onPrimaryFocus: (ButtonId, Boolean) -> Unit = { _, _ -> },
) {
    val angle by animationData.angleDegree.asState()
    val alpha by animationData.alpha.asState()
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
                    isOnClickEnabled = isOnClickEnabled && item.enabled,
                    isFullyVisible = isFullyVisible,
                    onClick = item.action,
                    onHover = onHover,
                    alpha = alpha,
                    onExpandedChanged = onPrimaryFocus,
                )
            }
            is CoinButtonData -> {
                CoinButton(
                    coin = item,
                    onHover = onHover,
                    onClick = item.action,
                    alpha = alpha,
                    animationData = animationData
                )
            }
        }
    }
}

@OptIn(ExperimentalComposeUiApi::class)
@Composable
private fun ActionWheelBackgroundRing(
    ringAlpha: Animatable<Float, AnimationVector1D>,
    ringSize: Dp,
    borderSize: Dp,
    // Whether to show the tip
    showTip: Boolean = false,
    // How many degrees to rotate the tip in degrees. 0f is top-left
    tipRotation: Float = 0f,
    ringColor: Color = JervisTheme.black,
) {
    // We only ever fade in the ring. If it ever fades out again, this is handled
    // by fading out the entire action wheel in upper layers.
    // var visible by remember(initialVisible) { mutableStateOf(initialVisible) }
    val chalkTexture = imageResource(Res.drawable.jervis_brush_chalk)
    val imageBrush = remember(chalkTexture) {
        ShaderBrush(
            shader = ImageShader(
                image = chalkTexture.scalePixels(IconFactory.scaleFactor),
                tileModeX = TileMode.Repeated,
                tileModeY = TileMode.Repeated,
            ),
        )
    }
    val alpha by ringAlpha.asState()
    val padding = ((hypot(ringSize.value, ringSize.value)).dp - ringSize) / 2f

    Canvas(
        modifier = Modifier
            .fillMaxSize()
            .alpha(alpha)
    ) {
        val radius = ringSize.toPx() / 2f
        val center = Offset(size.width / 2f, size.height / 2f)
        val innerRadius = radius - borderSize.toPx()
        val tip = Offset(center.x - ringSize.toPx() / 2f, center.y - ringSize.toPx() / 2f) // tip of the droplet
        if (showTip) {
            val path = Path().apply {
                fillType = PathFillType.EvenOdd
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
                addOval(Rect(center = center, radius = innerRadius))
            }

            rotate(degrees = tipRotation) {
                drawPath(
                    path = path,
                    brush = imageBrush,
                    colorFilter = ColorFilter.tint(ringColor),
                )
            }
        } else {
            drawCircle(
                brush = imageBrush,
                radius = (radius + innerRadius) / 2f,
                center = center,
                colorFilter = ColorFilter.tint(ringColor),
                style = Stroke(width = borderSize.toPx()),
            )
        }
    }
}

// Helper text that hovers just below the center player.
// Generally, this should be a "hover" effect when mousing over buttons
@Composable
private fun TooltipText(
    message: String?,
    backgroundColor: Color = JervisTheme.black.copy(0.8f),
    fontSize: TextUnit = 16.jsp,
    fontWeight: FontWeight = FontWeight.Bold,
    textColor: Color = Color.White,
) {
    // In a previous commit, the TooltipText had an animation duration. The
    // rationale was to avoid "flicker" when moving quickly over many buttons.
    // That also worked, but it was causing other issues as the background
    // size was changing, which looked worse, so the animation was removed again
    // If we introduce animations here, we should probably also animate between
    // the different tooltip sizes, but this needs more investigation.

    // On CMP, getBoundingBox uses RectHeightStyle.MAX which returns
    // line-height bounds (font metrics), not ink bounds. So we cannot use the
    // bounding box top/bottom for vertical centering. Instead:
    //   - Horizontal: getBoundingBox left/right (accurate)
    //   - Vertical: anchored to firstBaseline; capHeight ≈ fontSize * 0.72
    //     (cap height is stable at ~70-73% of em size across common UI fonts)
    // See https://youtrack.jetbrains.com/issue/CMP-2477
    val fontSizePx = with(LocalDensity.current) { fontSize.toPx() }
    val capHeightPx = fontSizePx * 0.72f
    // Reset all layout state when displayedMessage changes so we never show text
    // before the background has measured its bounds (onTextLayout fires one frame late).
    var layoutReady by remember(message) { mutableStateOf(false) }
    var textLeft by remember(message) { mutableStateOf(0f) }
    var textRight by remember(message) { mutableStateOf(0f) }
    var firstBaseline by remember(message) { mutableStateOf(0f) }
    Text(
        modifier = Modifier
            .offset(y = 55.jdp)
            .drawBehind {
                if (textRight > textLeft && firstBaseline > 0f) {
                    val padding = 8.dp.toPx()
                    drawRoundRect(
                        color = backgroundColor,
                        topLeft = Offset(textLeft - padding, firstBaseline - capHeightPx - padding),
                        size = Size(textRight - textLeft + padding * 2, capHeightPx + padding * 2),
                        cornerRadius = CornerRadius(4.dp.toPx()),
                    )
                }
            },
        onTextLayout = { result ->
            firstBaseline = result.firstBaseline
            val text = result.layoutInput.text.toString()
            val indices = text.indices.filter { !text[it].isWhitespace() }
            if (indices.isNotEmpty()) {
                val boxes = indices.map { result.getBoundingBox(it) }
                textLeft = boxes.minOf { it.left }
                textRight = boxes.maxOf { it.right }
            } else {
                textLeft = 0f
                textRight = 0f
            }
            layoutReady = true
        },
        text = message ?: "",
        style = MaterialTheme.typography.bodySmall.copy(
            fontFamily = JervisTheme.defaultFontFamily(), // Needed to support direction arrows on Web
            fontSize = fontSize,
            color = textColor,
            fontWeight = fontWeight,
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
    val colorFilter = remember { ColorFilter.tint(JervisTheme.black.copy(0.1f), BlendMode.Darken) }
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
                            // Action Buttons take precedence over anything the other Pitch Layers might do,
                            // so consume all events here.
                            e.changes.forEach { it.consume() }
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
                                    // See above.
                                }
                                PointerEventType.Release -> {
                                    onClick()
                                }
                            }
                        }
                    }
                }
            }
    ) {
        Image(
            modifier = Modifier.size(72.jdp), // 1.25x scaling = same as Dice Icon scaling
            contentScale = ContentScale.Fit,
            filterQuality = FilterQuality.High,
            bitmap = icon,
            contentDescription = "",
            colorFilter = if (isHover) colorFilter else null
        )
    }
}

@Composable
fun CoinButton(
    modifier: Modifier = Modifier,
    coin: CoinButtonData,
    onHover: (String?) -> Unit = {},
    onClick: () -> Unit = { },
    alpha: Float = 1f,
    dropShadow: Boolean = true,
    animationData: ItemAnimatable,
) {
    val buttonSize = IconFactory.getCoinSizeDp(coin.value)
    val isAnimating by animationData.isAnimating
    val yOffset by animationData.yOffset.asState()
    val rotation by animationData.rotation.asState()
    val displayFace: DieResult? by animationData.displayFace
    val coinFace = when {
        !isAnimating -> coin.value
        (displayFace?.value == 1) -> Coin.HEAD
        (displayFace?.value == 2) -> Coin.TAIL
        else -> Coin.HEAD
    }

    // The visible button, both the normal and jumping one.
    Box(
        modifier = Modifier
            .alpha(alpha)
            .applyIf(isAnimating) {
                graphicsLayer {
                    translationY = yOffset
                    rotationZ = rotation
                }
            }
        ,
        contentAlignment = Alignment.CenterStart,
    ) {
        CoinImage(
            coin = coinFace,
            buttonSize,
            enabled = coin.enabled,
            onHover = { hover ->
                onHover(if (hover) coin.label() else null)
            },
            onClick = onClick,
            dropShadow = dropShadow,
        )
    }
}

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun CoinImage(
    coin: Coin,
    size: DpSize,
    enabled: Boolean,
    onHover: (Boolean) -> Unit = {},
    onClick: () -> Unit = {},
    dropShadow: Boolean,
) {
    Box(
        modifier = Modifier
            .padding(6.jdp)
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
                    dropShadow(CircleShape) {
                        color = JervisTheme.black.copy(0.6f)
                        offset = Offset.Zero
                        radius = 6.jdp.toPx()
                    }
                }
                .applyIf(enabled) {
                    this.pointerInput(Unit) {
                        awaitPointerEventScope {
                            while (true) {
                                val e = awaitPointerEvent()
                                // Action Buttons take precedence over anything the other Pitch Layers might do,
                                // so consume all events here.
                                e.changes.forEach { it.consume() }
                                when (e.type) {
                                    PointerEventType.Enter -> {
                                        colorFilter = ColorFilter.tint(JervisTheme.black.copy(0.1f), BlendMode.Darken)
                                        onHover(true)
                                    }
                                    PointerEventType.Exit  -> {
                                        colorFilter = null
                                        onHover(false)
                                    }
                                    PointerEventType.Press -> {
                                        // We are about to trigger onClick, so prevent
                                        // other layers from reacting too early
                                        onClick()
                                    }
                                    PointerEventType.Release -> {
                                        // See above
                                    }
                                }
                            }
                        }
                    }
                }
                .clip(CircleShape)
            ,
            contentScale = ContentScale.Fit,
            filterQuality = FilterQuality.Low,
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
    val diceList = remember(button) {
        button.options.toMutableList().apply {
            remove(button.diceValue)
            add(0, button.diceValue)
        }
    }
    val shadowColor = Color.Black
    var expanded by remember { mutableStateOf(false) }

    // Properties we animate when expanded and deflating the selector
    val bgWidthDp = remember { Animatable(0f) }
    val bgAlpha = remember { Animatable(0f) }

    // Padding between background bar and dice buttons
    val backgroundPadding = 8.dp
    val spacingBetweenItems = backgroundPadding / 2f

    val rows = remember(button.diceValue) {
        when (button.diceValue) {
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
    }
    val itemsPrRow = diceList.size / rows
    val buttonSize = remember(currentDiceValue) { IconFactory.getDiceSizeDp(currentDiceValue) }
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
            diceRollType = button.diceRollType,
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
                                        diceRollType = button.diceRollType,
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

@OptIn(ExperimentalComposeUiApi::class)
@Composable
private fun DiceButton(
    buttonSize: DpSize,
    alpha: Float,
    value: DieResult,
    label: () -> String?,
    diceRollType: DiceRollType,
    clickEnabled: Boolean = true,
    onHover: (String?) -> Unit = {},
    onClick: () -> Unit = {},
    dropShadowColor: Color = Color.Black,
    dropShadow: Boolean = true,
) {
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

    val diceConfig = remember(diceRollType) { BB2025DiceColorConfig.configFor(diceRollType) }
    val settingsFlow = remember(diceConfig) {
        SETTINGS_MANAGER
            .observeStringKey(diceConfig.settingsKey, diceConfig.defaultColor.name)
            .map { colorDescription ->
                DiceColor.entries.find { it.name == colorDescription } ?: DiceColor.DEFAULT
            }
    }
    val diceColor by settingsFlow.collectAsState(diceConfig.defaultColor)
    val bitmap = if ((useSelectedColorAsHover && hover)) {
        val color = when (value) {
            is D3Result,
            is D6Result -> DiceColor.YELLOW
            else -> DiceColor.DEFAULT
        }
        IconFactory.getDiceIcon(value, color)
    } else {
        IconFactory.getDiceIcon(value, diceColor)
    }

    fun showHoverEffect() {
        onHover(label())
        if (!useSelectedColorAsHover) {
            colorFilter = ColorFilter.tint(JervisTheme.black.copy(0.1f), BlendMode.Darken)
        }
    }

    LaunchedEffect(clickEnabled, hover) {
        if (clickEnabled && hover) {
            showHoverEffect()
        }
    }

    Box(
        modifier = Modifier
            .size(buttonSize)
            .onPointerEvent(PointerEventType.Enter) {
                if (!hover && clickEnabled) {
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
