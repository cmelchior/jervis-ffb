package com.jervisffb.ui.game.view.animation

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import com.jervisffb.ui.ICON_FACTORY
import com.jervisffb.ui.game.animations.DirectionSelectedAnimation
import com.jervisffb.ui.game.view.pitch.LocalPitchData
import com.jervisffb.ui.game.view.pitch.jervisSquare
import com.jervisffb.ui.game.viewmodel.PitchViewModel
import kotlinx.coroutines.delay
import org.jetbrains.compose.resources.painterResource
import kotlin.time.Duration.Companion.milliseconds

@Composable
fun DirectionSelectedAnimation(
    vm: PitchViewModel,
    animation: DirectionSelectedAnimation,
) {
    val selectedArrowAlpha by animateFloatAsState(
        targetValue = 1f,
        animationSpec = tween(animation.fadeDurationMillis),
        label = "direction selected arrow fade",
    )

    Box(
        modifier = Modifier.jervisSquare(LocalPitchData.current.size, animation.coordinate),
    ) {
        Image(
            modifier = Modifier
                .fillMaxSize()
                .alpha(selectedArrowAlpha),
            painter = painterResource(ICON_FACTORY.getDirection(animation.direction, active = true)),
            contentDescription = null,
        )
    }

    LaunchedEffect(animation) {
        delay(animation.durationMillis.milliseconds)
        vm.notifyAnimationFinished()
    }
}
