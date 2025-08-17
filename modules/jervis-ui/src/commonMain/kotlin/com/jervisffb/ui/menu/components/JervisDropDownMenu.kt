package com.jervisffb.ui.menu.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.jervisffb.ui.menu.p2p.host.DropdownEntry

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun <T: DropdownEntry> JervisDropDownMenu(
    title: String,
    entries: List<T>,
    enabled: Boolean = true,
    modifier: Modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
    selectedEntry: T? = entries.firstOrNull(),
    onSelected: (T) -> Unit,
) {
    var expanded by remember { mutableStateOf(false) }
    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = !expanded },
    ) {
        OutlinedTextField(
            modifier = modifier,
            value = selectedEntry?.name ?: "",
            onValueChange = { },
            enabled = enabled,
            readOnly = true,
            label = { Text(title) },
        )
        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            entries.forEachIndexed { index, item ->
                DropdownMenuItem(
                    text = {
                        Text(item.name)
                    },
                    onClick = {
                        expanded = false
                        onSelected(item)
                    }
                )
            }
        }
    }
}
