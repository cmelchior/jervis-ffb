package com.jervisffb.ui.view.animation

import androidx.compose.animation.core.FastOutLinearInEasing
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animate
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import com.jervisffb.ui.animations.KickOffEventAnimation
import com.jervisffb.ui.viewmodel.FieldViewModel
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.imageResource

@Composable
fun KickOffEventResultAnimation(vm: FieldViewModel, animation: KickOffEventAnimation) {
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
