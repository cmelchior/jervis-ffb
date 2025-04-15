package com.jervisffb.ui.menu.components

import androidx.compose.runtime.Composable
import com.jervisffb.ui.menu.components.teamselector.SelectTeamComponentModel

@Composable
fun LoadTeamFromFileDialog(
    viewModel: SelectTeamComponentModel,
    onDismissRequest: () -> Unit
) {
    NotImplementYetDialog(
        "Load Team File",
        onDismissRequest = onDismissRequest,
    )
}
