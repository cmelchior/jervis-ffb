package com.jervisffb.ui.menu.components

import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import com.jervisffb.ui.game.view.JervisTheme

@Composable
fun JervisSwitch(enabled: Boolean, checked: Boolean, onCheckedChange: (Boolean) -> Unit) {
    Switch(
        colors = SwitchDefaults.colors(
            uncheckedThumbColor = JervisTheme.rulebookRed,
            uncheckedTrackColor = Color.Transparent,
            disabledCheckedTrackColor = JervisTheme.rulebookRed.copy(alpha = 0.38f),
        ),
        enabled = enabled,
        checked = checked,
        onCheckedChange = {
            onCheckedChange(it)
        }
    )
}
