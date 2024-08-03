package dk.ilios.jervis.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyShortcut
import androidx.compose.ui.window.FrameWindowScope
import androidx.compose.ui.window.MenuBar
import dk.ilios.jervis.ui.viewmodel.MenuViewModel
import okio.Path

@Composable
fun FrameWindowScope.WindowMenuBar(vm: MenuViewModel) {
    var action by remember { mutableStateOf("Last action: None") }
    var isOpen by remember { mutableStateOf(true) }
    var isSubmenuShowing by remember { mutableStateOf(false) }
    MenuBar {
        Menu("Developer Tools", mnemonic = 'D') {
            Item("Save Game", onClick = {
                FilePicker(
                    "Save File",
                    "game.jrvs",
                    "Jervis files",
                    "jrvs"
                ) { filePath: Path ->
                    vm.saveGameState(filePath)
                }
            })
            Item(
                text = "Undo Action",
                shortcut = KeyShortcut(Key.Z, meta = true)
            ) {
                vm.undoAction()
            }
        }
//        Menu("Help", mnemonic = 'H') {
//            Item("About", onClick = { action = "About" })
//            Item("Help", onClick = { action = "Help" })
//        }
//        Menu("File", mnemonic = 'F') {
//            Item("Copy", onClick = { }, shortcut = KeyShortcut(Key.C, ctrl = true))
//            Item("Paste", onClick = { }, shortcut = KeyShortcut(Key.V, ctrl = true))
//        }
//        Menu("Actions", mnemonic = 'A') {
//            CheckboxItem(
//                "Advanced settings",
//                checked = isSubmenuShowing,
//                onCheckedChange = {
//                    isSubmenuShowing = !isSubmenuShowing
//                }
//            )
//            if (isSubmenuShowing) {
//                Menu("Settings") {
//                    Item("Setting 1", onClick = { action = "Last action: Setting 1" })
//                    Item("Setting 2", onClick = { action = "Last action: Setting 2" })
//                }
//            }
//            Separator()
//            Item("About", icon = AboutIcon, onClick = { action = "Last action: About" })
//            Item("Exit", onClick = { isOpen = false }, shortcut = KeyShortcut(Key.Escape), mnemonic = 'E')
//        }
    }
}

