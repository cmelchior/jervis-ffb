package com.jervisffb.ui.game.view.animation

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animate
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import com.jervisffb.ui.game.animations.FanFactorResultAnimation
import com.jervisffb.ui.game.view.JervisTheme
import com.jervisffb.ui.game.viewmodel.FieldViewModel
import com.jervisffb.ui.utils.jdp
import com.jervisffb.ui.utils.jsp
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.time.Duration.Companion.milliseconds

@Composable
fun FanFactorResultAnimation(
    vm: FieldViewModel,
    animation: FanFactorResultAnimation,
) {
    var nameAlpha by remember { mutableStateOf(0f) }
    var homeFansOffsetX by remember { mutableStateOf(-600f) }
    var homeFansAlpha by remember { mutableStateOf(0f) }
    var awayFansOffsetX by remember { mutableStateOf(600f) }
    var awayFansAlpha by remember { mutableStateOf(0f) }
    var alpha by remember { mutableStateOf(1f) }

    LaunchedEffect(animation) {
        // Reset values in case the animation runs multiple times
        nameAlpha = 0f
        homeFansOffsetX = -600f
        homeFansAlpha = 0f
        awayFansOffsetX = 600f
        awayFansAlpha = 0f
        alpha = 1f

        // Step 1: Team names fade in
        animate(
            initialValue = 0f,
            targetValue = 1f,
            animationSpec = tween(durationMillis = animation.teamFadeInDurationMillis, easing = FastOutSlowInEasing),
        ) { value, _ -> nameAlpha = value }

        // Step 2: Home fans slide in from the left
        coroutineScope {
            launch {
                animate(
                    initialValue = -600f,
                    targetValue = 0f,
                    animationSpec = tween(durationMillis = animation.valueTranslateDurationMillis, easing = FastOutSlowInEasing),
                ) { value, _ -> homeFansOffsetX = value }
            }
            launch {
                animate(
                    initialValue = 0f,
                    targetValue = 1f,
                    animationSpec = tween(durationMillis = animation.valueFadeDurationMillis, easing = LinearEasing),
                ) { value, _ -> homeFansAlpha = value }
            }
        }

        // Step 3: Away fans slide in from the right
        coroutineScope {
            launch {
                animate(
                    initialValue = 600f,
                    targetValue = 0f,
                    animationSpec = tween(durationMillis = animation.valueTranslateDurationMillis, easing = FastOutSlowInEasing),
                ) { value, _ -> awayFansOffsetX = value }
            }
            launch {
                animate(
                    initialValue = 0f,
                    targetValue = 1f,
                    animationSpec = tween(durationMillis = animation.valueFadeDurationMillis, easing = LinearEasing),
                ) { value, _ -> awayFansAlpha = value }
            }
        }

        delay(animation.fadeOutDelayMills.milliseconds)

        // Step 4. Fade everything out
        animate(
            initialValue = 1f,
            targetValue = 0f,
            animationSpec = tween(durationMillis = animation.teamFadeInDurationMillis, easing = LinearEasing),
        ) { value, _ -> alpha = value }

        vm.notifyAnimationFinished()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(64.dp)
            .alpha(alpha),
        contentAlignment = Alignment.Center,
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            // Team names appear first
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .alpha(nameAlpha),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                AnimationText(
                    modifier = Modifier.weight(1f),
                    text = animation.homeTeam.name, size = 48.jsp
                )
                AnimationText(
                    modifier = Modifier.alpha(0f).padding(horizontal = 64.jdp), text = "VS", size = 48.jsp
                )
                AnimationText(
                    modifier = Modifier.weight(1f),
                    text = animation.awayTeam.name, size = 48.jsp
                )
            }

            Spacer(modifier = Modifier.height(16.jdp))

            // Fan numbers flow in from each side
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Box(modifier = Modifier
                    .graphicsLayer { translationX = homeFansOffsetX }
                    .alpha(homeFansAlpha)
                    .background(JervisTheme.black)
                    .padding(16.jdp)
                ) {
                    AnimationText(
                        text = animation.totalHomeFans,
                    )
                }
                // Make sure to have same spacer as above
                AnimationText(
                    modifier = Modifier.alpha(0f).padding(horizontal = 64.jdp).alpha(0f), text = "VS", size = 48.jsp
                )
                Box(modifier = Modifier
                    .graphicsLayer { translationX = awayFansOffsetX }
                    .alpha(awayFansAlpha)
                    .background(JervisTheme.black)
                    .padding(16.jdp)
                ) {
                    AnimationText(
                        text = animation.totalAwayFans,
                    )
                }
            }
        }
    }
}

@Composable
private fun AnimationText(
    modifier: Modifier = Modifier,
    text: String,
    size: TextUnit = 64.jsp,
    color: Color = JervisTheme.white,
) {
    Text(
        textAlign = TextAlign.Center,
        modifier = modifier,
        text = text,
        lineHeight = 1.em,
        fontSize = size,
        fontWeight = FontWeight.Bold,
        fontFamily = JervisTheme.fontFamily(),
        color = color
    )
}
