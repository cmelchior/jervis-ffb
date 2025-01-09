package com.jervisffb.ui.screen.p2pserver

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.Button
import androidx.compose.material.Card
import androidx.compose.material.MaterialTheme
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
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.jervisffb.ui.screen.LoadTeamDialog
import com.jervisffb.ui.screen.P2PServerScreenModel
import com.jervisffb.ui.screen.TeamInfo
import com.jervisffb.ui.view.JervisTheme

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun TeamSelectorPage(modifier: Modifier, viewModel: P2PServerScreenModel) {
    val availableTeams by viewModel.availableTeams.collectAsState()
    var showImportFumbblTeam by remember { mutableStateOf(false) }
    val selectedTeam: TeamInfo? by viewModel.selectedTeam.collectAsState()
    Column(
        modifier = Modifier.padding(16.dp).fillMaxSize(),
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
                        logo = team.logo,
                        onClick = { viewModel.setSelectedTeam(team) },

                        )
                }
            }
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Start) {
                Button(onClick = { }) {
                    Text(
                        text = "Load from file",
                        style = MaterialTheme.typography.body1.copy(
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                        ),
                    )
                }
                Spacer(modifier = Modifier.width(16.dp))
                Button(onClick = {
                    showImportFumbblTeam = !showImportFumbblTeam
                }) {
                    Text(
                        text = "Import from FUMBBL",
                        style = MaterialTheme.typography.body1.copy(
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                        ),
                    )
                }
                Spacer(modifier = Modifier.weight(1f))
                Button(
                    onClick = { viewModel.teamSelectionDone() },
                    enabled = (selectedTeam != null),
                ) {
                    Text("NEXT")
                }
            }
        }
    }
    if (showImportFumbblTeam) {
        LoadTeamDialog(viewModel, onCloseRequest = { showImportFumbblTeam = false })
    }
}

@Composable
fun RowScope.TeamCard(
    name: String,
    teamValue: Int,
    rerolls: Int,
    logo: ImageBitmap,
    isSelected: Boolean = false,
    emptyTeam: Boolean = false,
    onClick: (() -> Unit)?
) {
    Card(
        modifier = Modifier
            .width(300.dp)
            .let { if (onClick != null) it.clickable(!emptyTeam, onClick = onClick) else it }
        ,
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Row(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(8.dp)) {
                    val adjustedTv = teamValue / 1_000
                    Text(text = "$adjustedTv K", fontSize = 14.sp)
                    Text("$rerolls RR")
                }
                Spacer(modifier = Modifier.weight(1f))
                Image(
                    modifier = Modifier.padding(8.dp),
                    bitmap = logo,
                    contentDescription = null,
                    contentScale = ContentScale.Inside,
                )
            }
            Row (
                modifier = Modifier.background(if (isSelected) JervisTheme.accentTeamColor else JervisTheme.awayTeamColor),
                verticalAlignment = Alignment.Bottom,
            ) {
                Text(
                    modifier = Modifier.fillMaxWidth().padding(start = 8.dp, top = 8.dp, end = 8.dp, bottom = 10.dp),
                    textAlign = TextAlign.Start,
                    text = name,
                    color = JervisTheme.accentContentBackgroundColor,
                    fontWeight = FontWeight.Bold,
                )
            }
        }
    }
}

