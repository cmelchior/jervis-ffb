package com.jervisffb.ui.menu.p2p

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.AlertDialog
import androidx.compose.material.Button
import androidx.compose.material.Card
import androidx.compose.material.Text
import androidx.compose.material.TextField
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
import com.jervisffb.ui.game.view.JervisTheme
import com.jervisffb.ui.game.view.utils.JervisButton
import com.jervisffb.ui.game.view.utils.TitleBorder
import com.jervisffb.ui.isDigitsOnly
import com.jervisffb.ui.menu.p2p.host.TeamInfo

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun TeamSelectorPage(
    viewModel: TeamSelectorScreenModel,
    confirmTitle: String,
    onNext: () -> Unit,
) {
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
                        logo = team.logo,
                        onClick = { viewModel.setSelectedTeam(team) },

                    )
                }
            }
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
                JervisButton(confirmTitle.uppercase(), onClick = { onNext() }, enabled = (selectedTeam != null))
            }
        }
    }
    if (showImportFumbblTeamDialog) {
        LoadTeamDialog(viewModel, onCloseRequest = { showImportFumbblTeamDialog = false })
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
    Box(
        modifier = Modifier
            .width(300.dp)
            .background(JervisTheme.rulebookPaperMediumDark.copy(alpha = 0.5f))
            .border(width = if (isSelected) 3.dp else 0.dp, color = if (isSelected) JervisTheme.rulebookRed else Color.Transparent)
            .let { if (onClick != null) it.clickable(!emptyTeam, onClick = onClick) else it }
        ,
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Column (
                modifier = Modifier.fillMaxWidth(), //.padding(start = 8.dp, top = 8.dp, end = 8.dp, bottom = 0.dp),
//                verticalAlignment = Alignment.Top,
            ) {
                val color = JervisTheme.rulebookRed
//                val color = if (isSelected) JervisTheme.rulebookGreen else JervisTheme.rulebookRed
//                Divider(
//                    modifier = Modifier
//                        .padding(bottom = 2.dp)
//                        .wrapContentWidth()
//                        .height(2.dp)
//                    ,
//                    color = JervisTheme.rulebookBlue,
//                )
                TitleBorder(color)
                Box(
                    modifier = Modifier.fillMaxWidth().background(color),
                    contentAlignment = Alignment.CenterStart,
                ) {
                    Text(
                        modifier = Modifier.padding(start = 8.dp, bottom = 2.dp),
                        text = name.uppercase(),
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
//                        color = if (isSelected) JervisTheme.rulebookOrange else JervisTheme.white
                        color = JervisTheme.white
                    )
                }
                TitleBorder(color)
//
//
//                BoxHeader(name, color = JervisTheme.rulebookRed)
//                Text(
//                    modifier = Modifier.fillMaxWidth().padding(start = 8.dp, top = 8.dp, end = 8.dp, bottom = 10.dp),
//                    textAlign = TextAlign.Start,
//                    text = name,
//                    color = JervisTheme.accentContentBackgroundColor,
//                    fontWeight = FontWeight.Bold,
//                )
            }
            Row(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(start = 8.dp, bottom = 8.dp, top = 4.dp)) {
                    val adjustedTv = teamValue / 1_000
                    Text(text = "$adjustedTv K", color = JervisTheme.contentTextColor)
                    Text("$rerolls RR", color = JervisTheme.contentTextColor)
                }
                Spacer(modifier = Modifier.weight(1f))
                Image(
                    modifier = Modifier.padding(8.dp),
                    bitmap = logo,
                    contentDescription = null,
                    contentScale = ContentScale.Inside,
                )
            }
//            Divider(
//                modifier = Modifier
//                    .padding(top = 2.dp)
//                    .wrapContentWidth()
//                    .height(2.dp)
//                ,
//                color = JervisTheme.rulebookBlue,
//            )
        }
    }
}

@Composable
fun RowScope.TeamCardV0(
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

@Composable
fun LoadTeamDialog(
    viewModel: TeamSelectorScreenModel,
    onCloseRequest: () -> Unit
) {
    var inputText by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }
    println("Show LoadTeamDialog")
    AlertDialog(
        onDismissRequest = { /* Do nothing */ },
        title = { Text("Import FUMBBL Team") },
        text = {
            Column {
                Text("Enter the team ID (found in the team URL):")
                Spacer(modifier = Modifier.height(8.dp))
                TextField(
                    value = inputText,
                    onValueChange = { inputText = it },
                    isError = !inputText.isDigitsOnly(),
                    placeholder = { Text("Team ID") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                if (error?.isNotBlank() == true) {
                    Text(modifier = Modifier.padding(top = 8.dp, bottom = 8.dp), text = error!!, color = Color.Red)
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    isLoading = true
                    viewModel.loadTeamFromNetwork(
                        inputText,
                        onSuccess = {
                            isLoading = false
                            onCloseRequest()
                        },
                        onError = { msg ->
                            isLoading = false
                            error = msg
                        },
                    )
                },
                enabled = !isLoading && inputText.isNotBlank() && inputText.isDigitsOnly()
            ) {
                Text(if (isLoading) "Downloading..." else "Import Team")
            }
        },
        dismissButton = {
            Button(onClick = onCloseRequest) {
                Text("Cancel")
            }
        }
    )
}
