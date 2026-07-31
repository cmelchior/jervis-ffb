package com.jervisffb.ui.menu.components

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import com.jervisffb.ui.game.viewmodel.MenuViewModel
import com.jervisffb.ui.menu.components.NotImplementYetDialog

/**
 * Dialog used during development. Will popup a dialog saying "Not
 * implemented yet".
 */
@Composable
fun NotImplementedYetDialogComponent(viewModel: MenuViewModel) {
    val dialogData: Pair<String, Boolean> by viewModel.isNotImplementedYetDialogVisible.collectAsState()
    if (!dialogData.second) return
    NotImplementYetDialog(
        dialogData.first,
        onDismissRequest = { viewModel.openNotImplementedYet(dialogData.first, false) }
    )
}

