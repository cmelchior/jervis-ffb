package com.jervisffb.ui.game.view.pitch

import androidx.compose.runtime.Composable
import com.jervisffb.ui.game.viewmodel.PitchViewModel

/**
 * Layer 7: Trap Doors.
 *
 * This layer is used to render trapdoors as found in normal Blood Bowl.
 * It is rendered above room features, since a Trapdoor might eventually be
 * placed on top of e.g. a hanging bridge.
 *
 * See [Pitch] for more details about layer ordering.
 */
@Composable
fun TrapdoorsLayer(
    vm: PitchViewModel,
) {
    // Do nothing for now.
}
