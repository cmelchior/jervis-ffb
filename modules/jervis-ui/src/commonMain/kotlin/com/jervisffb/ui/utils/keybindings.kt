package com.jervisffb.ui.utils

import androidx.compose.ui.input.key.Key
import com.jervisffb.ui.keybinds.ControlKey

expect fun createPlatformButtonLabel(label: String, key: Key, modifier: ControlKey? = null): String
