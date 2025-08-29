package com.jervisffb.ui.game.view.animation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import com.jervisffb.ui.game.animations.KickOffEventAnimation
import com.jervisffb.ui.game.animations.PassAnimation
import com.jervisffb.ui.game.viewmodel.FieldViewModel

@Composable
fun AnimationLayer(
    vm: FieldViewModel,
) {
    Box(modifier = Modifier.fillMaxSize()) {
        val animationData by vm.observeAnimation().collectAsState(null)
        if (animationData?.second is KickOffEventAnimation) {
            KickOffEventResultAnimation(vm, animationData!!.second as KickOffEventAnimation)
        }
        if (animationData?.second is PassAnimation) {
            PassResultAnimation(vm, animationData!!.second as PassAnimation)
        }
    }
}
