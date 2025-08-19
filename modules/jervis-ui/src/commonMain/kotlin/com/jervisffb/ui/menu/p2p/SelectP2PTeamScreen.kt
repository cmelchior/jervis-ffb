package com.jervisffb.ui.menu.p2p

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.jervisffb.ui.game.view.utils.JervisButton
import com.jervisffb.ui.menu.components.ImportTeamFromFumbblDialog
import com.jervisffb.ui.menu.components.ImportTeamFromTourPlayDialog
import com.jervisffb.ui.menu.components.LoadTeamFromFileDialog
import com.jervisffb.ui.menu.components.TeamInfo
import com.jervisffb.ui.menu.components.teamselector.SelectTeamComponent
import com.jervisffb.ui.menu.components.teamselector.SelectTeamComponentModel

/**
 * The Team selector "tab" when creating P2P Host or P2P Client games.
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun SelectP2PTeamScreen(
    viewModel: SelectTeamComponentModel,
    isHost: Boolean, // Only used to shift the row buttons. There is probably a better way to do this.
    confirmTitle: String,
    onNext: () -> Unit,
) {
    val selectedTeamByOtherCoach by viewModel.unavailableTeam.collectAsState()
    val availableTeams by viewModel.availableTeams.collectAsState()
    var showImportTourPlayTeamDialog by remember { mutableStateOf(false) }
    var showImportFumbblTeamDialog by remember { mutableStateOf(false) }
    var showLoadTeamFromFileDialog by remember { mutableStateOf(false) }
    val selectedTeam: TeamInfo? by viewModel.selectedTeam.collectAsState()
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(modifier = Modifier.weight(1f).fillMaxSize()) {
            SelectTeamComponent(viewModel)
        }

        // This row is mirrored between here and SelectHotseatTeamScreen. The reason being that
        // it is hard to capture the buttons inside the same component due to how the layout is structured.
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Start) {
            Spacer(modifier = Modifier.width(if (isHost) 40.dp else 60.dp)) // Move the first button out from the sidebar image
            JervisButton(text = "Load from file", onClick = {
                showLoadTeamFromFileDialog = !showLoadTeamFromFileDialog
            })
            Spacer(modifier = Modifier.width(16.dp))
            JervisButton(text = "Import from FUMBBL", onClick = {
                showImportFumbblTeamDialog = !showImportFumbblTeamDialog
            })
            Spacer(modifier = Modifier.weight(1f))
            JervisButton(confirmTitle.uppercase(), onClick = { onNext() }, enabled = (selectedTeam != null))
        }
    }
    if (showImportTourPlayTeamDialog) {
        ImportTeamFromTourPlayDialog(viewModel, onDismissRequest = { showImportTourPlayTeamDialog = false })
    }
    if (showImportFumbblTeamDialog) {
        ImportTeamFromFumbblDialog(viewModel, onDismissRequest = { showImportFumbblTeamDialog = false })
    }
    if (showLoadTeamFromFileDialog) {
        LoadTeamFromFileDialog(viewModel, onDismissRequest = { showLoadTeamFromFileDialog = false })
    }
}
