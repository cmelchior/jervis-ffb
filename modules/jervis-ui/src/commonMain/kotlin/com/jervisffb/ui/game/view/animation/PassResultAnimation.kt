package com.jervisffb.ui.game.view.animation

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animate
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.boundsInRoot
import androidx.compose.ui.layout.positionInRoot
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import com.jervisffb.ui.asDp
import com.jervisffb.ui.game.animations.PassAnimation
import com.jervisffb.ui.game.icons.IconFactory
import com.jervisffb.ui.game.viewmodel.FieldViewModel
import kotlin.time.Duration


@Composable
fun PassResultAnimation(vm: FieldViewModel, animation: PassAnimation) {

    // Kicker
    val startOffset = remember {
        vm.squareOffsets[animation.from]!!.positionInRoot() - vm.fieldOffset!!.positionInRoot()
    }
    // Landing field
    val endOffset = remember { vm.squareOffsets[animation.to]!!.positionInRoot() - vm.fieldOffset!!.positionInRoot() }
    // Size of square (which dictates size of ball)
    val targetSquareSize = remember { vm.squareOffsets[animation.to]!!.boundsInRoot() }

    val (duration, animStateCalculator) = animation.getStateCalculator(
        startOffset,
        endOffset,
    )

    BallAnimation(
        stateCalculator = animStateCalculator,
        duration = duration,
        image = IconFactory.getBall(),
        squareSize = targetSquareSize,
        animationDone = { vm.finishAnimation() }
    )
}

@Composable
fun BallAnimation(
    stateCalculator: (Float) -> PassAnimation.BallState,
    duration: Duration,
    image: ImageBitmap,
    squareSize: Rect,
    animationDone: () -> Unit,
) {
    var time by remember { mutableStateOf(0f) }

    // The stateCalculator should be unique for every new animation, so we can use that as a key.
    LaunchedEffect(stateCalculator) {
        // Scale duration so it looks like real physics, but speed up. It is chosen
        // through experimentation.
        //
        // Unclear why animateIntAsState doesn't work.
        // Probably I am just doing something wrong, but this also works.
        try {
            val timeScalingFactor = 10
            animate(
                initialValue = 0.toFloat(),
                targetValue = duration.inWholeMilliseconds.toFloat(),
                animationSpec = tween(duration.inWholeMilliseconds.toInt() / timeScalingFactor, easing = LinearEasing),
            ) { value, _ ->
                time = value.toInt() / 1_000f // Convert back to seconds
            }
        } finally {
            animationDone()
        }
    }

    val ballState by remember(time) {
        derivedStateOf {
            stateCalculator(time)
        }
    }
    Image(
        bitmap = image,
        contentDescription = null,
        modifier = Modifier
            .size(squareSize.width.asDp(), squareSize.height.asDp())
            .padding(4.dp) // Match padding from normal square
            .offset { IntOffset(ballState.x.toInt(), ballState.y.toInt()) }
            .graphicsLayer {
                scaleX = ballState.scale
                scaleY = ballState.scale
            }
    )
}
