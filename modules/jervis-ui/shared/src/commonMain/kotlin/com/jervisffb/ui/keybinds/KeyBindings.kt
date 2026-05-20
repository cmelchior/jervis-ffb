package com.jervisffb.ui.keybinds

import androidx.compose.ui.input.key.Key
import com.jervisffb.utils.hasMacKeyboard

data class PlatformKeyShortcut(
    val key: Key,
    val modifier: ControlKey? = null,
)

/**
 * Class wrapping the responsibility of handling key bindings for the Desktop an Web clients.
 * Keybinds are not supported on iOS.
 *
 * Due to Multiplatform representing keybinds very differently, we are using our own wrapper
 * here to represent the keybinds.
 */
object KeyBindings {

    private val keyDisplayMap = mapOf(
        // Letters
        Key.A to "A", Key.B to "B", Key.C to "C", Key.D to "D", Key.E to "E",
        Key.F to "F", Key.G to "G", Key.H to "H", Key.I to "I", Key.J to "J",
        Key.K to "K", Key.L to "L", Key.M to "M", Key.N to "N", Key.O to "O",
        Key.P to "P", Key.Q to "Q", Key.R to "R", Key.S to "S", Key.T to "T",
        Key.U to "U", Key.V to "V", Key.W to "W", Key.X to "X", Key.Y to "Y",
        Key.Z to "Z",

        // Numbers
        Key.Zero to "0", Key.One to "1", Key.Two to "2", Key.Three to "3",
        Key.Four to "4", Key.Five to "5", Key.Six to "6", Key.Seven to "7",
        Key.Eight to "8", Key.Nine to "9",

        // Function Keys
        Key.F1 to "F1", Key.F2 to "F2", Key.F3 to "F3", Key.F4 to "F4",
        Key.F5 to "F5", Key.F6 to "F6", Key.F7 to "F7", Key.F8 to "F8",
        Key.F9 to "F9", Key.F10 to "F10", Key.F11 to "F11", Key.F12 to "F12",

        // Arrow Keys
        Key.DirectionUp to "↑", Key.DirectionDown to "↓",
        Key.DirectionLeft to "←", Key.DirectionRight to "→",

        // Special Keys
        Key.Escape to "Esc", Key.Enter to "Enter", Key.Spacebar to "Space",
        Key.Tab to "Tab", Key.Backspace to "Backspace", Key.Delete to "Delete",
        Key.Insert to "Insert", Key.MoveHome to "Home", Key.MoveEnd to "End",
        Key.PageUp to "Page Up", Key.PageDown to "Page Down",

        // Modifier Keys
        Key.ShiftLeft to "Shift", Key.ShiftRight to "Shift",
        Key.CtrlLeft to "Ctrl", Key.CtrlRight to "Ctrl",
        Key.AltLeft to "Alt", Key.AltRight to "Alt",

        // Punctuation and Symbols
        Key.Comma to ",", Key.Period to ".", Key.Slash to "/",
        Key.Semicolon to ";", Key.Apostrophe to "'", Key.LeftBracket to "[",
        Key.RightBracket to "]", Key.Backslash to "\\", Key.Grave to "`",
        Key.Minus to "-", Key.Equals to "=",

        // Numpad
        Key.NumPad0 to "Num 0", Key.NumPad1 to "Num 1", Key.NumPad2 to "Num 2",
        Key.NumPad3 to "Num 3", Key.NumPad4 to "Num 4", Key.NumPad5 to "Num 5",
        Key.NumPad6 to "Num 6", Key.NumPad7 to "Num 7", Key.NumPad8 to "Num 8",
        Key.NumPad9 to "Num 9", Key.NumPadDot to "Num .", Key.NumPadDivide to "Num /",
        Key.NumPadMultiply to "Num *", Key.NumPadSubtract to "Num -",
        Key.NumPadAdd to "Num +", Key.NumPadEnter to "Num Enter",
        Key.NumPadEquals to "Num =", Key.NumLock to "Num Lock",

        // Lock Keys
        Key.CapsLock to "Caps Lock", Key.ScrollLock to "Scroll Lock",

        // Media Keys
        Key.VolumeUp to "Vol +", Key.VolumeDown to "Vol -",
        Key.VolumeMute to "Mute"
    )

    fun displayKey(key: Key): String {
        return when (key) {
            Key.MetaLeft, Key.MetaRight -> if (hasMacKeyboard()) "⌘" else "Win"
            else -> keyDisplayMap[key] ?: key.toString()
        }
    }

    fun displayKey(key: ControlKey): String {
        return when (key) {
            ControlKey.CONTROL -> "Ctrl"
            ControlKey.META -> if (hasMacKeyboard()) "⌘" else "Meta"
            ControlKey.ALT -> "Alt"
            ControlKey.SHIFT -> "Shift"
            ControlKey.PLATFORM_CTRL -> if (hasMacKeyboard()) "⌘" else "Ctrl"
        }
    }

    // TODO Find a way to customize this from settings
    private val shortcuts = mapOf(
        ClientShortcut.UNDO to PlatformKeyShortcut(key = Key.Z, modifier = ControlKey.PLATFORM_CTRL),
        ClientShortcut.GAME_MENU to PlatformKeyShortcut(key = Key.Escape, modifier = null),
    )

    fun createPlatformButtonLabel(shortcutKey: ClientShortcut): String {
        val (key, shortcut) = shortcuts[shortcutKey] ?: error("Could not find shortcut: $shortcutKey")
        return com.jervisffb.ui.utils.createPlatformButtonLabel(shortcutKey.label, key, shortcut)
    }




}
