package com.jervisffb.ui.game.view.field

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import com.jervis.generated.SettingsKeys
import com.jervisffb.engine.model.locations.FieldCoordinate
import com.jervisffb.ui.SETTINGS_MANAGER
import com.jervisffb.ui.game.view.JervisTheme
import com.jervisffb.ui.game.viewmodel.FieldViewModel

/**
 * Layer 5: Hover Underlay:
 *
 * This layer controls background highlights for when the mouse hovers over the
 * square.
 *
 * TODO: Should this behave differently for Giants which are bigger than one square?
 *
 * See [Field] for more details about layer ordering.
 */
@Composable
fun FieldHoverUnderlayLayer(
    vm: FieldViewModel,
) {
    val fieldSizeData = LocalFieldData.current.size
    val highlightedSquare: FieldCoordinate? by vm.highlights().collectAsState()
    val showHover by SETTINGS_MANAGER.observeBooleanKey(SettingsKeys.JERVIS_UI_SHOW_MOUSE_OVER_EFFECT_ON_SQUARE_VALUE, true).collectAsState(true)
    if (highlightedSquare != null && showHover && highlightedSquare!!.isOnField(vm.game.rules)) {
        Box(
            modifier = Modifier
                .jervisSquare(fieldSizeData, highlightedSquare!!)
                .background(JervisTheme.hoverColor)
        )
    }
}
