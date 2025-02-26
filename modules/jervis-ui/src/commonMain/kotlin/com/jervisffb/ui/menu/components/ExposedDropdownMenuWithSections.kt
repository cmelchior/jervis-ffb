package com.jervisffb.ui.menu.components

import androidx.compose.foundation.layout.padding
import androidx.compose.material.Divider
import androidx.compose.material.DropdownMenuItem
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.ExposedDropdownMenuBox
import androidx.compose.material.MaterialTheme
import androidx.compose.material.OutlinedTextField
import androidx.compose.material.Text
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

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun <T> ExposedDropdownMenuWithSections(
    title: String,
    entries: List<Pair<String, List<DropdownEntry>>>,
    onSelected: (T) -> Unit,
) {
    var expanded by remember { mutableStateOf(false) }
    var selectedOption by remember { mutableStateOf(entries.first().second.first()) }
    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = !expanded },
    ) {
        OutlinedTextField(
            modifier = Modifier.padding(bottom = 8.dp),
            value = selectedOption.name,
            onValueChange = { },
            readOnly = true,
            label = { Text(title) },
        )
        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            entries.forEachIndexed { index, (sectionTitle, items) ->
                DropdownHeader(sectionTitle.uppercase())
                items.forEach { item ->
                    DropdownMenuItem(
                        onClick = {
                            selectedOption = item
                            expanded = false
                        }
                    ) {
                        Text(item.name)
                    }
                }
                if (index < entries.lastIndex) {
                    Divider()
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
        style = MaterialTheme.typography.body1.copy(
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold,
            color = Color.Gray
        ),
    )
}
