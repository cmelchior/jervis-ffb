package com.jervisffb.ui.utils

import androidx.compose.ui.input.key.Key
import com.jervisffb.ui.keybinds.ControlKey

actual fun createPlatformButtonLabel(label: String, key: Key, modifier: ControlKey?): String {
    return label // Shortcuts are not available on iOS
}
