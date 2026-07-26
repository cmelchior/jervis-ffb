package com.jervisffb.ui.game.view

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.jervisffb.shared.generated.resources.Res
import com.jervisffb.shared.generated.resources.jervis_icon_backward
import com.jervisffb.shared.generated.resources.jervis_icon_fast_forward
import com.jervisffb.shared.generated.resources.jervis_icon_fast_rewind
import com.jervisffb.shared.generated.resources.jervis_icon_forward
import com.jervisffb.shared.generated.resources.jervis_icon_pause
import com.jervisffb.shared.generated.resources.jervis_skip_to_end
import com.jervisffb.shared.generated.resources.jervis_skip_to_start
import com.jervisffb.ui.game.state.ReplayDirection
import com.jervisffb.ui.game.state.ReplayPlayback
import com.jervisffb.ui.game.viewmodel.PanelBackground
import com.jervisffb.ui.game.viewmodel.ReplayControllerViewModel
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.painterResource

/**
 * Composable responsible for showing the buttons that control the replay speed.
 */
@Composable
fun ReplayControllerPanel(
    vm: ReplayControllerViewModel,
    background: PanelBackground,
    modifier: Modifier = Modifier,
) {
    val playback by vm.playback.collectAsState()
    Row(
        modifier = modifier
            .background(background.color)
            .padding(horizontal = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center,
    ) {
        // Speed label shown on the left while playing backwards.
        SpeedLabel(
            playback = playback,
            visibleFor = ReplayDirection.BACKWARD,
        )
        ControlIcon(
            icon = Res.drawable.jervis_skip_to_start,
            contentDescription = "Jump to start",
            active = false,
            onClick = vm::jumpToStart,
        )
        ControlIcon(
            icon = Res.drawable.jervis_icon_fast_rewind,
            contentDescription = "Fast backward",
            active = playback.direction == ReplayDirection.BACKWARD && playback.speed > 1,
            onClick = vm::fastBackward,
        )
        ControlIcon(
            icon = Res.drawable.jervis_icon_backward,
            contentDescription = "Backward",
            active = playback.direction == ReplayDirection.BACKWARD && playback.speed == 1,
            onClick = vm::backward,
        )
        ControlIcon(
            icon = Res.drawable.jervis_icon_pause,
            contentDescription = "Pause",
            active = playback.direction == ReplayDirection.PAUSED,
            onClick = vm::pause,
        )
        ControlIcon(
            icon = Res.drawable.jervis_icon_forward,
            contentDescription = "Forward",
            active = playback.direction == ReplayDirection.FORWARD && playback.speed == 1,
            onClick = vm::forward,
        )
        ControlIcon(
            icon = Res.drawable.jervis_icon_fast_forward,
            contentDescription = "Fast forward",
            active = playback.direction == ReplayDirection.FORWARD && playback.speed > 1,
            onClick = vm::fastForward,
        )
        ControlIcon(
            icon = Res.drawable.jervis_skip_to_end,
            contentDescription = "Jump to end",
            active = false,
            onClick = vm::jumpToEnd,
        )
        // Speed label: shown on the right while playing forwards.
        SpeedLabel(
            playback = playback,
            visibleFor = ReplayDirection.FORWARD,
        )
    }
}

@Composable
private fun ControlIcon(
    icon: DrawableResource,
    contentDescription: String,
    active: Boolean,
    onClick: () -> Unit,
) {
    Image(
        painter = painterResource(icon),
        contentDescription = contentDescription,
        contentScale = ContentScale.Fit,
        colorFilter = ColorFilter.tint(if (active) JervisTheme.awayTeamColor else JervisTheme.white),
        modifier = Modifier
            .size(48.dp)
            .clip(RoundedCornerShape(4.dp))
            .clickable { onClick() }
            .padding(6.dp),
    )
}

/**
 * Fixed-width slot showing the current speed (e.g. "2x"). It only renders text when
 * [playback] is moving in [visibleFor], but always reserves its width so the row of
 * icons does not shift as playback direction changes.
 */
@Composable
private fun SpeedLabel(
    playback: ReplayPlayback,
    visibleFor: ReplayDirection,
) {
    Box(modifier = Modifier.width(32.dp), contentAlignment = Alignment.Center) {
        if (playback.direction == visibleFor && playback.speed != Int.MAX_VALUE) {
            Text(
                text = "${playback.speed}x",
                color = JervisTheme.white,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
            )
        }
    }
}
