package com.jervisffb.ui.menu.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.jervisffb.ui.game.view.JervisTheme

@Composable
fun SimpleSwitch(
    label: String,
    isSelected: Boolean,
    description: String = "",
    isEnabled: Boolean = true,
    innerPadding: Dp = 0.dp,
    onSelected: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(innerPadding)
        , verticalAlignment = Alignment.CenterVertically
    ) {
        Column(
            modifier = Modifier.padding(end = 4.dp).weight(1f),
        ) {
            Text(
                text = label,
                color = if (isEnabled) JervisTheme.contentTextColor else JervisTheme.contentTextColor.copy(alpha = 0.6f),
            )
            if (description.isNotBlank()) {
                Text(
                    text = description,
                    fontSize = 12.sp,
                    color = if (isEnabled) JervisTheme.contentTextColor.copy(alpha = 0.7f) else JervisTheme.contentTextColor.copy(alpha = 0.35f),
                )
            }
        }
        JervisSwitch(
            enabled = isEnabled,
            checked = isSelected,
            onCheckedChange = { selected ->
                onSelected(selected)
            }
        )
    }
}

