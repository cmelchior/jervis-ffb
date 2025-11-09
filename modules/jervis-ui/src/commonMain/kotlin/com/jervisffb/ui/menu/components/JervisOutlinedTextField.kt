package com.jervisffb.ui.menu.components

import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign

@Composable
fun JervisOutlinedTextField(
    modifier: Modifier = Modifier,
    value: String,
    onValueChange: (String) -> Unit,
    readOnly: Boolean = false,
    label: String,
    placeholderText: String? = null,
    textAlign: TextAlign = TextAlign.Start
) {
    OutlinedTextField(
        modifier = modifier,
        value = value,
        onValueChange = onValueChange,
        readOnly = readOnly,
        textStyle = LocalTextStyle.current.copy(textAlign = textAlign),
        label = { Text(label) },
        placeholder = placeholderText?.let { { Text(it) } },
    )
}
