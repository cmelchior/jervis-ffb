package dk.ilios.jervis

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyShortcut
import androidx.compose.ui.window.FrameWindowScope
import androidx.compose.ui.window.MenuBar
import dk.ilios.jervis.ui.filePicker
import dk.ilios.jervis.ui.viewmodel.Feature
import dk.ilios.jervis.ui.viewmodel.MenuViewModel
import okio.Path

@Composable
fun FrameWindowScope.WindowMenuBar(vm: MenuViewModel) {
    var action by remember { mutableStateOf("Last action: None") }
    var isOpen by remember { mutableStateOf(true) }
    var rerollSuccessfulActions by remember { mutableStateOf(vm.isFeatureEnabled(Feature.DO_NOT_REROLL_SUCCESSFUL_ACTIONS)) }
    var selectKickingPlayer by remember { mutableStateOf(vm.isFeatureEnabled(Feature.SELECT_KICKING_PLAYER)) }
    MenuBar {
        Menu("Developer Tools", mnemonic = 'D') {
            Item("Save Game", onClick = {
                filePicker(
                    "Save File",
                    "game.jrvs",
                    "Jervis files",
                    "jrvs",
                ) { filePath: Path ->
                    vm.saveGameState(filePath)
                }
            })
            Item(
                text = "Undo Action",
                shortcut = KeyShortcut(Key.Z, meta = true),
            ) {
                vm.undoAction()
            }
        }

        Menu ("Automated Actions", mnemonic = 'A') {
            CheckboxItem(
                text = "Do not reroll successful actions",
                checked = rerollSuccessfulActions,
                onCheckedChange = {
                    rerollSuccessfulActions = !rerollSuccessfulActions
                    vm.toggleFeature(Feature.DO_NOT_REROLL_SUCCESSFUL_ACTIONS, rerollSuccessfulActions)
                }
            )
            CheckboxItem(
                text = "Select kicking player",
                checked = selectKickingPlayer,
                onCheckedChange = {
                    selectKickingPlayer = !selectKickingPlayer
                    vm.toggleFeature(Feature.SELECT_KICKING_PLAYER, selectKickingPlayer)
                }
            )
        }

        Menu ("Setups", mnemonic = 'S') {
            Menu("Kicking") {
                Item("3-4-4", onClick = { vm.loadSetup("3-4-4") })
            }

            Menu("Receiving") {
                Item("5-5-1", onClick = { vm.loadSetup("5-5-1") })
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
