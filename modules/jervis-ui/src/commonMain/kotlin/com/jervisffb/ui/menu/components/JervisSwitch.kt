package com.jervisffb.ui.menu.components

import androidx.compose.material.Switch
import androidx.compose.material.SwitchDefaults
import androidx.compose.runtime.Composable
import com.jervisffb.ui.game.view.JervisTheme

@Composable
fun JervisSwitch(enabled: Boolean, checked: Boolean, onCheckedChange: (Boolean) -> Unit) {
    Switch(
        colors = SwitchDefaults.colors(
            checkedThumbColor = JervisTheme.rulebookRed,
            uncheckedThumbColor = JervisTheme.rulebookPaperMediumDark,
        ),
        enabled = enabled,
        checked = checked,
        onCheckedChange = {
            onCheckedChange(it)
        }
    )
}
