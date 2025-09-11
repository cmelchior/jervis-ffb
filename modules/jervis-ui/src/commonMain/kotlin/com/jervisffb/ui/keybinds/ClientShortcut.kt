package com.jervisffb.ui.keybinds

/**
 * Lists all actions that have a key bind. Note, this shortcut might not
 * be available on all platforms, but we keep it in common so writing the code
 * is easier.
 *
 * In common code they are used to generate the correct hover labels with
 * shortcut data.
 */
enum class ClientShortcut(val label: String) {
    UNDO("Undo"),
    CLOSE("Close"),
    GAME_MENU("Game Menu")
}
