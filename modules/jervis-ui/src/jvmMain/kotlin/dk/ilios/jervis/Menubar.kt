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
import dk.ilios.jervis.ui.viewmodel.Setups
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
            Menu("Defensive") {
                Item(Setups.SETUP_3_4_4, onClick = { vm.loadSetup(Setups.SETUP_3_4_4) })
            }
            Menu("Offensive") {
                Item(Setups.SETUP_5_5_1, onClick = { vm.loadSetup(Setups.SETUP_5_5_1) })
            }
        }
    }
}
