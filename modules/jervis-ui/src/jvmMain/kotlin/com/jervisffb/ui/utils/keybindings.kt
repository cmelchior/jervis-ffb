package com.jervisffb.ui.utils

import androidx.compose.ui.input.key.Key
import com.jervisffb.ui.keybinds.ControlKey
import com.jervisffb.ui.keybinds.KeyBindings

actual fun createPlatformButtonLabel(label: String, key: Key, modifier: ControlKey?): String {
    return buildString {
        append(label)
        append(" (")
        if (modifier != null) {
            append("${KeyBindings.displayKey(modifier)}+")
        }
        append("${KeyBindings.displayKey(key)})")
    }
}
