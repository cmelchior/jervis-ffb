package com.jervisffb.ui.menu.components

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.em

@Composable
fun SimpleSwitch(label: String, isSelected: Boolean, isEnabled: Boolean = true, onSelected: (Boolean) -> Unit) {
    Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        Text(
            text = label,
            lineHeight = 1.0.em,
            color = if (isEnabled) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.onSurface.copy(0.6f),
            // color = if (isEnabled) LocalContentColor.current.copy(LocalContentAlpha.current) else LocalContentColor.current.copy(0.6f),
        )
        Spacer(modifier = Modifier.weight(1f))
        JervisSwitch(
            enabled = isEnabled,
            checked = isSelected,
            onCheckedChange = { selected ->
                onSelected(selected)
            }
        )
    }
}
