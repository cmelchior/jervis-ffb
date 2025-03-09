package com.jervisffb.ui.menu.hotseat

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.ContentAlpha
import androidx.compose.material.OutlinedTextField
import androidx.compose.material.RadioButton
import androidx.compose.material.RadioButtonDefaults
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.jervisffb.ui.game.view.JervisTheme
import com.jervisffb.ui.game.view.utils.JervisButton
import com.jervisffb.ui.menu.components.LoadTeamDialog
import com.jervisffb.ui.menu.components.teamselector.TeamSelectorComponent
import com.jervisffb.ui.menu.p2p.host.SettingsCard

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun SelectHotseatTeamScreen(
    viewModel: SelectHotseatTeamScreenModel,
) {
    val coachName by viewModel.coachName.collectAsState()
    val isValidTeamSelection by viewModel.isValidTeamSelection.collectAsState(false)
    val playerType by viewModel.playerType.collectAsState()
    val aiPlayers by viewModel.aiPlayers.collectAsState()
    val selectedAiPlayer by viewModel.selectedAiPlayer.collectAsState()
    val playerTypeOptions = listOf(
        "Human" to CoachType.HUMAN,
        "Computer" to CoachType.COMPUTER,
    )
    var showImportFumbblTeamDialog by remember { mutableStateOf(false) }
    var showLoadTeamFromFileDialog by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier.fillMaxSize(),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
            ,
            verticalAlignment = Alignment.Top
        ) {
            Column {
                SettingsCard("Coach", 300.dp) {
                    OutlinedTextField(
                        modifier = Modifier.fillMaxWidth(),
                        value = coachName,
                        onValueChange = { viewModel.updateCoachName(it) },
                        label = { Text("Coach name") }
                    )
                    PlayerTypeSelector(playerTypeOptions, playerType, onChoice = { type: CoachType -> viewModel.updatePlayerType(type)})
                }
                Spacer(modifier = Modifier.height(32.dp))
                if (playerType == CoachType.COMPUTER) {
                    SettingsCard("Select AI", 300.dp) {
                        aiPlayers.forEach { ai ->
                            JervisButton(
                                text = ai.name,
                                onClick = {
                                    viewModel.updateSelectedAiPlayer(ai)
                                },
                                buttonColor = if (selectedAiPlayer == ai) JervisTheme.rulebookRed else JervisTheme.rulebookBlue)
                        }
                    }
                }
            }
            Spacer(modifier = Modifier.width(32.dp))
            TeamSelectorComponent(viewModel.teamSelectorModel)
        }
        // This row is mirrored between here and SelectHotseatTeamScreen. The reason being that
        // it is hard to capture the buttons inside the same component due to how the layout is structured.
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Start) {
            Spacer(modifier = Modifier.width(60.dp))
            JervisButton(text = "Load from file", onClick = {
                showLoadTeamFromFileDialog = !showLoadTeamFromFileDialog
            })
            Spacer(modifier = Modifier.width(16.dp))
            JervisButton(text = "Import from FUMBBL", onClick = {
                showImportFumbblTeamDialog = !showImportFumbblTeamDialog
            })
            Spacer(modifier = Modifier.weight(1f))
            JervisButton("Next", onClick = { viewModel.teamSelectionDone() }, enabled = isValidTeamSelection)
        }
    }
    if (showImportFumbblTeamDialog) {
        LoadTeamDialog(viewModel.teamSelectorModel, onCloseRequest = { showImportFumbblTeamDialog = false })
    }
}

@Composable
fun PlayerTypeSelector(options: List<Pair<String, CoachType>>, selectedType: CoachType, onChoice: (CoachType) -> Unit) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        options.forEach { (title, value) ->
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .clickable { onChoice(value) }
                    .padding(end = 12.dp)
            ) {
                RadioButton(
                    selected = (value == selectedType),
                    onClick = { onChoice(value) },
                    colors = RadioButtonDefaults.colors(
                        selectedColor = JervisTheme.rulebookRed,
                        unselectedColor = JervisTheme.contentTextColor.copy(alpha = 0.6f),
                        disabledColor = Color.LightGray.copy(alpha = ContentAlpha.disabled)
                    )
                )
                Text(
                    text = title,
                    modifier = Modifier.padding(start = 4.dp)
                )
            }
        }
    }
}
