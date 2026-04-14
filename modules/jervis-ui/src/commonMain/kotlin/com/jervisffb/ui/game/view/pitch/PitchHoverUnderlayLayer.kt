package com.jervisffb.ui.game.view.pitch

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.jervis.generated.SettingsKeys
import com.jervisffb.engine.model.locations.PitchCoordinate
import com.jervisffb.ui.SETTINGS_MANAGER
import com.jervisffb.ui.game.view.JervisTheme
import com.jervisffb.ui.game.viewmodel.PitchViewModel
import kotlinx.coroutines.launch

/**
 * Layer 5: Hover Underlay:
 *
 * This layer controls background highlights for when the mouse hovers over the
 * square.
 *
 * TODO: Should this behave differently for Giants which are bigger than one square?
 *
 * See [Pitch] for more details about layer ordering.
 */
@Composable
fun PitchHoverUnderlayLayer(
    vm: PitchViewModel,
) {
    val pitchSizeData = LocalPitchData.current.size
    val highlightedSquare: PitchCoordinate? by vm.highlights.collectAsState()
    val showHover by SETTINGS_MANAGER.observeBooleanKey(SettingsKeys.JERVIS_UI_SHOW_MOUSE_OVER_EFFECT_ON_SQUARE_VALUE, true).collectAsState(true)
    // When the Action Wheel is showing, we do not want to show the hover effect (regardless of it being enabled or not)
    // as it looks confusing with too many things happening at once. The onHover on players will still trigger to show
    // player cards.
    var showingPrimaryWheel by remember { mutableStateOf(false) }
    val showingSecondaryWheel by remember { vm.sharedPitchData.isActionWheelVisible }
    LaunchedEffect(vm) {
        launch {
            vm.actionWheelViewModel.observe().collect {
                showingPrimaryWheel = vm.sharedPitchData.isActionWheelVisible.value
            }
        }
    }

    val isShowingActionWheel = showingPrimaryWheel || showingSecondaryWheel
    if (!isShowingActionWheel && highlightedSquare != null && showHover && highlightedSquare!!.isOnPitch(vm.game.rules)) {
        Box(
            modifier = Modifier
                .jervisSquare(pitchSizeData, highlightedSquare!!)
                .background(JervisTheme.hoverColor)
        )
    }
}
