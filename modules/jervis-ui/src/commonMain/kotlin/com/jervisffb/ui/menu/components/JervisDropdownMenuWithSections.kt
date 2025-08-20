package com.jervisffb.ui.menu.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.jervisffb.ui.menu.p2p.host.DropdownEntry

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun <T: DropdownEntry> JervisDropdownMenuWithSections(
    title: String,
    entries: List<Pair<String, List<T>>>,
    modifier: Modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
    selectedEntry: DropdownEntry? = null,
    onSelected: (T) -> Unit,
) {
    var expanded by remember { mutableStateOf(false) }
    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = !expanded },
    ) {
        OutlinedTextField(
            modifier = modifier.menuAnchor(type = MenuAnchorType.PrimaryNotEditable, enabled = true),
            value = selectedEntry?.name ?: "",
            onValueChange = { },
            readOnly = true,
            label = { Text(title) },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(!expanded) },
        )
        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            entries.forEachIndexed { index, (sectionTitle, items) ->
                DropdownHeader(sectionTitle.uppercase())
                items.forEach { item ->
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
                if (index < entries.lastIndex) {
                    HorizontalDivider()
                }
            }
        }
    }
}

@Composable
private fun DropdownHeader(text: String) {
    Text(
        modifier = Modifier.padding(start = 8.dp, top = 8.dp, bottom = 8.dp),
        text = text,
        style = MaterialTheme.typography.bodySmall.copy(
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold,
            color = Color.Gray
        ),
    )
}
