package com.jervisffb.ui.menu.components

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.jervisffb.ui.game.view.JervisTheme

@Composable
fun SimpleSwitch(
    label: String,
    isSelected: Boolean,
    isEnabled: Boolean = true,
    onSelected: (Boolean) -> Unit
) {
    Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        Text(
            modifier = Modifier.padding(end = 4.dp).weight(1f),
            text = label,
            color = if (isEnabled) JervisTheme.contentTextColor else JervisTheme.contentTextColor.copy(alpha = 0.6f),
        )
        JervisSwitch(
            enabled = isEnabled,
            checked = isSelected,
            onCheckedChange = { selected ->
                onSelected(selected)
            }
        )
    }
}
