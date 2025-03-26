package com.jervisffb.ui.menu.hotseat

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.ContentAlpha
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
import com.jervisffb.ui.menu.components.coach.CoachSetupComponent
import com.jervisffb.ui.menu.components.coach.CoachType
import com.jervisffb.ui.menu.components.teamselector.TeamSelectorComponent

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun SelectHotseatTeamScreen(
    viewModel: SelectHotseatTeamScreenModel,
) {
    val isValidTeamSelection by viewModel.isValidTeamSelection.collectAsState(false)
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
                CoachSetupComponent(
                    viewModel.setupCoachModel,
                    headerWidth = 300.dp,
                )
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
