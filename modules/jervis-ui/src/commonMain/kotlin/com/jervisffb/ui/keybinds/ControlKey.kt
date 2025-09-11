package com.jervisffb.ui.keybinds    // Describe the various control keys that can be used


/**
 * Lists all possible modifier keys across all platforms. These are used to setup
 * keyboard shortcuts in a cross-platform manner.
 */
enum class ControlKey {
    CONTROL, // Control or Ctrl on all platforms
    META, // Command (on Mac), Windows key (on Windows), Meta (on Linux)
    ALT, // Alt (on Windows/Linux), Option (on Mac)
    SHIFT, // Shift on all platforms
    PLATFORM_CTRL, // Ctrl on Windows/Linux, Command on Mac
}
