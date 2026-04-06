package com.jervisffb.ui.game.view.animation

import androidx.compose.animation.core.FastOutLinearInEasing
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animate
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.key.Key.Companion.T
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.jervisffb.ui.dropShadow
import com.jervisffb.ui.game.animations.KickOffEventAnimation
import com.jervisffb.ui.game.animations.LogoAnimation
import com.jervisffb.ui.game.view.JervisTheme
import com.jervisffb.ui.game.viewmodel.FieldViewModel
import com.jervisffb.ui.utils.jsp
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.imageResource
import kotlin.time.Duration.Companion.seconds

@Composable
fun LogoAnimation(vm: FieldViewModel, animation: LogoAnimation) {
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
                delay(5.seconds)
                vm.notifyAnimationFinished()
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
    Column(
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .fillMaxSize()
            .padding(64.dp)
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
                this.translationY = translationY
            }
            .alpha(alpha)
    ) {
        Text(
            text = "JERVIS",
            fontFamily = JervisTheme.fontFamily(),
            color = JervisTheme.rulebookOrange,
            style = TextStyle(
                fontSize = 280.jsp,
                shadow = Shadow(
                    color = JervisTheme.black,
                    offset = Offset(10f, 10f),
                    blurRadius = 8f
                )
            )
        )
        Text(
            text = "Fantasy Football",
            fontFamily = JervisTheme.fontFamily(),
            color = JervisTheme.rulebookOrange,
            maxLines = 1,
            style = TextStyle(
                fontSize = 140.jsp,
                shadow = Shadow(
                    color = JervisTheme.black,
                    offset = Offset(5f, 5f),
                    blurRadius = 4f
                )
            )
        )
    }
}
