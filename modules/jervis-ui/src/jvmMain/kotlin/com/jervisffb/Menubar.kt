package com.jervisffb

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyShortcut
import androidx.compose.ui.window.FrameWindowScope
import androidx.compose.ui.window.MenuBar
import com.jervisffb.engine.rules.builder.GameType
import com.jervisffb.engine.serialize.JervisSetupFile
import com.jervisffb.ui.game.viewmodel.Feature
import com.jervisffb.ui.game.viewmodel.MenuViewModel
import com.jervisffb.ui.game.viewmodel.Setups

/**
 * Menubar which is only visible for Desktop Apps.
 * We have already started implementing an NavigationDrawer on the Game Screen.
 * We should probably deprecate this and move everything there. Something to think about.
 */
@Composable
fun FrameWindowScope.WindowMenuBar(vm: MenuViewModel) {

    var action by remember { mutableStateOf("Last action: None") }
    var isOpen by remember { mutableStateOf(true) }

    var rerollSuccessfulActions by remember { mutableStateOf(vm.isFeatureEnabled(Feature.DO_NOT_REROLL_SUCCESSFUL_ACTIONS)) }
    var selectKickingPlayer by remember { mutableStateOf(vm.isFeatureEnabled(Feature.SELECT_KICKING_PLAYER)) }
    var autoEndPlayerAction by remember { mutableStateOf(vm.isFeatureEnabled(Feature.END_PLAYER_ACTION_IF_ONLY_OPTION)) }
    var selectBlockType by remember { mutableStateOf(vm.isFeatureEnabled(Feature.SELECT_BLOCK_TYPE_IF_ONLY_OPTION)) }
    var pushPlayerIntoCrowd by remember { mutableStateOf(vm.isFeatureEnabled(Feature.PUSH_PLAYER_INTO_CROWD)) }

    val setupType: GameType? by vm.setupAvailable.collectAsState()

    MenuBar {
        Menu("Developer Tools", mnemonic = 'D') {
            Item("Save Game", onClick = {
                vm.showSaveGameDialog(includeDebugState = false)
            })
            Item(
                text = "Undo Action",
                shortcut = KeyShortcut(Key.Z, meta = true),
            ) {
                vm.undoAction()
            }
        }

        Menu("Automated Actions", mnemonic = 'A') {
            CheckboxItem(
                text = "Keep successful dice rolls",
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
            CheckboxItem(
                text = "End action automatically",
                checked = autoEndPlayerAction,
                onCheckedChange = {
                    autoEndPlayerAction = !autoEndPlayerAction
                    vm.toggleFeature(Feature.END_PLAYER_ACTION_IF_ONLY_OPTION, autoEndPlayerAction)
                }
            )
            CheckboxItem(
                text = "Select Block type automatically (when no variants)",
                checked = selectBlockType,
                onCheckedChange = {
                    selectBlockType = !selectBlockType
                    vm.toggleFeature(Feature.SELECT_BLOCK_TYPE_IF_ONLY_OPTION, selectBlockType)
                }
            )
            CheckboxItem(
                text = "Push Player into the Crowd when no other push directions",
                checked = pushPlayerIntoCrowd,
                onCheckedChange = {
                    pushPlayerIntoCrowd = !pushPlayerIntoCrowd
                    vm.toggleFeature(Feature.PUSH_PLAYER_INTO_CROWD, pushPlayerIntoCrowd)
                }
            )
        }

        // For now, we just ignore the command if it isn't legal, e.g. if it is the other team that is
        // setting up. Should we instead try to disable these?
        var setups by remember { mutableStateOf<List<JervisSetupFile>>(emptyList()) }
        LaunchedEffect(setupType) {
            setups = setupType?.let { Setups.getSetups(it) } ?: emptyList()
        }
        if (setups.isNotEmpty()) {
            Menu("Setups", mnemonic = 'S') {
                setups.forEach { setup ->
                    Item(setup.name, onClick = { vm.loadSetup(setup) })
                }
            }
        }
    }
}
