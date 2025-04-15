package com.jervisffb.ui.menu.fumbbl

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import com.jervisffb.ui.menu.components.NotImplementYetDialog

/**
 * Handles Settings Dialog. This is just a place holder for now.
 * I suspect a dialog is not big enough for the things that ends up going in here, we
 * probably need to create a seperate screen for it.
 */
@Composable
fun LoadReplayDialog(viewModel: FumbblScreenModel) {
    val visible: Boolean by viewModel.isReplayDialogVisible().collectAsState()
    if (!visible) return
    NotImplementYetDialog(
        title = "Load Replay",
        onDismissRequest = {
            viewModel.openReplayDialog(false)
        }
    )
}
