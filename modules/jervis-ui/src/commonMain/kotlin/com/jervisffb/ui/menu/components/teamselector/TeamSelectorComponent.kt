package com.jervisffb.ui.menu.components.teamselector

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.jervisffb.ui.menu.components.LoadTeamDialog
import com.jervisffb.ui.menu.components.TeamCard
import com.jervisffb.ui.menu.components.TeamInfo

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun TeamSelectorComponent(
    viewModel: TeamSelectorComponentModel,
) {
    val unavailableTeam by viewModel.unavailableTeam.collectAsState()
    val availableTeams by viewModel.availableTeams.collectAsState()
    var showImportFumbblTeamDialog by remember { mutableStateOf(false) }
    var showLoadTeamFromFileDialog by remember { mutableStateOf(false) }
    val selectedTeam: TeamInfo? by viewModel.selectedTeam.collectAsState()
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
        ) {
            FlowRow(
                modifier = Modifier.fillMaxWidth().weight(1f),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                availableTeams.forEach { team ->
                    TeamCard(
                        name = team.teamName,
                        teamValue = team.teamValue,
                        rerolls = team.rerolls,
                        isSelected = (selectedTeam?.teamId == team.teamId),
                        isEnabled = (team.teamId != unavailableTeam),
                        logo = team.logo,
                        onClick = { viewModel.setSelectedTeam(team) },

                    )
                }
            }
        }
    }
    if (showImportFumbblTeamDialog) {
        LoadTeamDialog(viewModel, onCloseRequest = { showImportFumbblTeamDialog = false })
    }
}
