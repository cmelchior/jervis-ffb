package dk.ilios.jervis.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.Key.Companion.Menu
import androidx.compose.ui.input.key.KeyShortcut
import androidx.compose.ui.window.FrameWindowScope
import androidx.compose.ui.window.MenuBar
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import dk.ilios.jervis.ui.viewmodel.MenuViewModel
import okio.Path
import java.io.File
import javax.swing.JFileChooser
import javax.swing.filechooser.FileNameExtensionFilter

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

