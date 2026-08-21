package com.jervisffb.ui.game.view.animation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import com.jervisffb.ui.game.animations.ConfettiAnimation
import com.jervisffb.ui.game.animations.DirectionSelectedAnimation
import com.jervisffb.ui.game.animations.FanFactorResultAnimation
import com.jervisffb.ui.game.animations.KickOffEventAnimation
import com.jervisffb.ui.game.animations.LogoAnimation
import com.jervisffb.ui.game.animations.PassAnimation
import com.jervisffb.ui.game.viewmodel.PitchViewModel

@Composable
fun AnimationLayer(
    vm: PitchViewModel,
) {
    Box(modifier = Modifier.fillMaxSize()) {
        val animationFlow = remember { vm.observeAnimation() }
        val animationData by animationFlow.collectAsState(null)
        when (val animation = animationData?.second) {
            is KickOffEventAnimation -> {
                KickOffEventResultAnimation(vm, animation)
            }
            is PassAnimation -> {
                PassResultAnimation(vm, animation)
            }
            is FanFactorResultAnimation -> {
                FanFactorResultAnimation(vm, animation)
            }
            is ConfettiAnimation -> {
                TouchdownAnimation(vm, animation)
            }
            is LogoAnimation -> {
                LogoAnimation(vm, animation)
            }
            is DirectionSelectedAnimation -> {
                DirectionSelectedAnimation(vm, animation)
            }
            else -> { /* Do nothing */ }
        }
    }
}
