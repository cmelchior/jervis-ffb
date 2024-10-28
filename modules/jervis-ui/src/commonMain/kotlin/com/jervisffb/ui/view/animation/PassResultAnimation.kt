package com.jervisffb.ui.view.animation

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animate
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.boundsInParent
import androidx.compose.ui.layout.positionInRoot
import androidx.compose.ui.unit.IntOffset
import com.jervisffb.ui.PassAnimation
import com.jervisffb.ui.asDp
import com.jervisffb.ui.icons.IconFactory
import com.jervisffb.ui.viewmodel.FieldViewModel
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch

@Composable
fun PassResultAnimation(vm: FieldViewModel, animation: PassAnimation) {

    // Kicker
    val startOffset = remember { vm.offsets[animation.from]!!.positionInRoot() - vm.fieldOffset!!.positionInRoot() }
    // Landing field
    val endOffset = remember { vm.offsets[animation.to]!!.positionInRoot() - vm.fieldOffset!!.positionInRoot() }
    // Size of scare (which dictates size of ball)
    val size = remember { vm.offsets[animation.from]!!.boundsInParent() }
    // Scale the time of the kick based on the distance
    val time = (animation.from.realDistanceTo(animation.to) * 100).toInt()

    AnimatedBallMovement(
        startOffset = startOffset,
        endOffset = endOffset,
        squareSize = size,
        duration = time,
        image = IconFactory.getBall(),
        animationDone = { vm.finishAnimation() }
    )
}

//@Composable
//fun convertPxToDp(px: Float): Dp {
//    val density = LocalDensity.current
//    return remember(px, density) { with(density) { px.toDp() } }
//}

@Composable
fun AnimatedBallMovement(
    startOffset: Offset,
    endOffset: Offset,
    squareSize: Rect,
    duration: Int,
    image: ImageBitmap,
    animationDone: () -> Unit
) {
    // See https://easings.net for more types
    val easingFunc = LinearEasing
    var offsetX by remember {  mutableStateOf(0f) }
    var offsetY by remember { mutableStateOf(0f) }
    var scale by remember { mutableStateOf(1f) }
    val maxScale = 2.0f

    LaunchedEffect(Unit) {
        coroutineScope {
            launch {
                animate(
                    initialValue = 1f,
                    targetValue = maxScale,
                    animationSpec = tween(durationMillis = duration/2, easing = LinearEasing)
                ) { value, _ ->
                    scale = value
                }
                animate(
                    initialValue = maxScale,
                    targetValue = 1f,
                    animationSpec = tween(durationMillis = duration/2, easing = LinearEasing )
                ) { value, _ ->
                    scale = value
                }
            }
            launch {
                animate(
                    initialValue = startOffset.x,
                    targetValue = endOffset.x,
                    animationSpec = tween(durationMillis = duration, easing = easingFunc)
                ) { value, _ ->
                    offsetX = value
                }
            }
            launch {
                animate(
                    initialValue = startOffset.y,
                    targetValue = endOffset.y,
                    animationSpec = tween(durationMillis = duration, easing = easingFunc),
                ) { value, _ ->
                    offsetY = value
                    if (offsetY == endOffset.y) {
                        animationDone()
                    }
                }
            }
        }
    }

    Image(
        bitmap = image,
        contentDescription = null,
        modifier = Modifier
            .size(squareSize.width.asDp(), squareSize.height.asDp())
            .offset { IntOffset(offsetX.toInt(), offsetY.toInt()) }
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            }
    )
}
