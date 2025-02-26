package com.jervisffb.ui.menu.components

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.LocalContentAlpha
import androidx.compose.material.LocalContentColor
import androidx.compose.material.Switch
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier

@Composable
fun SimpleSwitch(label: String, isSelected: Boolean, isEnabled: Boolean = true, onSelected: (Boolean) -> Unit) {
    Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        Text(
            text = label,
            color = if (isEnabled) LocalContentColor.current.copy(LocalContentAlpha.current) else LocalContentColor.current.copy(0.6f),
        )
        Spacer(modifier = Modifier.weight(1f))
        Switch(
            enabled = isEnabled,
            checked = isSelected,
            onCheckedChange = {
                onSelected(it)
            }
        )
    }
}
