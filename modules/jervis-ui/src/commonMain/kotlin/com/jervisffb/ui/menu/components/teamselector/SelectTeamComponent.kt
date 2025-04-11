package com.jervisffb.ui.menu.components.teamselector

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.jervisffb.ui.menu.components.TeamCard
import com.jervisffb.ui.menu.components.TeamInfo

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun SelectTeamComponent(
    viewModel: SelectTeamComponentModel,
) {
    val unavailableTeam by viewModel.unavailableTeam.collectAsState()
    val availableTeams by viewModel.availableTeams.collectAsState()
    val selectedTeam: TeamInfo? by viewModel.selectedTeam.collectAsState()
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        LazyVerticalGrid(
            columns = GridCells.FixedSize(300.dp),
            modifier = Modifier.fillMaxSize(),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            items(availableTeams.size) { index ->
                val team = availableTeams[index]
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
