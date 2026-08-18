package com.jervisffb.ui.menu.components.teamselector

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import com.jervisffb.ui.game.view.JervisTheme
import com.jervisffb.ui.menu.components.JervisOutlinedTextField
import com.jervisffb.ui.menu.components.TeamCard
import com.jervisffb.ui.menu.components.TeamInfo

@Composable
fun SelectTeamComponent(
    viewModel: SelectTeamComponentModel,
    modifier: Modifier = Modifier,
) {
    val unavailableTeam by viewModel.unavailableTeam.collectAsState()
    val availableTeams by viewModel.availableTeams.collectAsState()
    val selectedTeam: TeamInfo? by viewModel.selectedTeam.collectAsState()

    // If a filter is provided, filter the available teams based on matching team or roster name.
    var filter by remember(viewModel) { mutableStateOf("") }
    val normalizedFilter = filter.trim()
    val visibleTeams = remember(availableTeams, normalizedFilter) {
        when (normalizedFilter.isBlank()) {
            true -> availableTeams
            false -> availableTeams.filter { team ->
                team.teamName.contains(normalizedFilter, ignoreCase = true)
                    || team.teamRoster.contains(normalizedFilter, ignoreCase = true)
            }
        }
    }

    BoxWithConstraints(modifier = modifier) {
        val density = LocalDensity.current
        val gridCellSize = 300.dp
        val gridSpacing = 16.dp
        val gridCellSizePx = with(density) { gridCellSize.roundToPx() }
        val gridSpacingPx = with(density) { gridSpacing.roundToPx() }
        val availableWidthPx = constraints.maxWidth
        val columnCount = ((availableWidthPx + gridSpacingPx) / (gridCellSizePx + gridSpacingPx)).coerceAtLeast(1)
        val visibleGridWidthPx = minOf(
            availableWidthPx,
            gridCellSizePx * columnCount + gridSpacingPx * (columnCount - 1),
        )
        val visibleGridWidth = with(density) { visibleGridWidthPx.toDp() }

        Column(modifier = Modifier.fillMaxSize()) {
            JervisOutlinedTextField(
                modifier = Modifier
                    .width(visibleGridWidth)
                    .padding(bottom = gridSpacing),
                value = filter,
                onValueChange = { filter = it },
                label = "Filter",
            )
            when (normalizedFilter.isNotBlank() && visibleTeams.isEmpty()) {
                true -> {
                    Box(
                        modifier = Modifier.fillMaxWidth().weight(1f),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(
                            text = "No matching teams",
                            color = JervisTheme.contentTextColor.copy(alpha = 0.7f),
                        )
                    }
                }
                false -> {
                    LazyVerticalGrid(
                        columns = GridCells.FixedSize(gridCellSize),
                        modifier = Modifier.fillMaxWidth().weight(1f),
                        horizontalArrangement = Arrangement.spacedBy(gridSpacing),
                        verticalArrangement = Arrangement.spacedBy(gridSpacing),
                    ) {
                        items(visibleTeams.size) { index ->
                            val team = visibleTeams[index]
                            TeamCard(
                                name = team.teamName,
                                race = team.teamRoster,
                                teamValue = team.teamValue,
                                rerolls = team.rerolls,
                                isSelected = (selectedTeam?.teamId == team.teamId),
                                isEnabled = (team.teamId != unavailableTeam),
                                logo = team.logo,
                                highlight = normalizedFilter,
                                onClick = { viewModel.setSelectedTeam(team) },
                            )
                        }
                    }
                }
            }
        }
    }
}
