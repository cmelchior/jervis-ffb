@file:OptIn(
    org.jetbrains.compose.resources.InternalResourceApi::class,
    org.jetbrains.compose.resources.ExperimentalResourceApi::class,
)
package com.jervisffb.ui.screen

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.AlertDialog
import androidx.compose.material.Button
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.MaterialTheme
import androidx.compose.material.OutlinedTextField
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
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import cafe.adriel.voyager.core.model.ScreenModel
import cafe.adriel.voyager.core.screen.Screen
import com.jervisffb.engine.model.Team
import com.jervisffb.engine.model.TeamId
import com.jervisffb.engine.rules.BB2020Rules
import com.jervisffb.engine.serialize.JervisTeamFile
import com.jervisffb.fumbbl.web.FumbblTeamLoader
import com.jervisffb.ui.CacheManager
import com.jervisffb.ui.icons.IconFactory
import com.jervisffb.ui.isDigitsOnly
import com.jervisffb.ui.view.JervisTheme
import com.jervisffb.ui.viewmodel.MenuViewModel
import dashedBorder
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import kotlin.math.ceil
import kotlin.random.Random

data class TeamInfo(
    val teamId: TeamId,
    val teamName: String,
    val teamRoster: String,
    val teamValue: Int,
    val rerolls: Int,
    val logo: ImageBitmap
)

class P2PServerScreenModel(private val menuViewModel: MenuViewModel) : ScreenModel {

    val availableTeams = MutableStateFlow<List<TeamInfo>>(emptyList())
    val selectedTeam = MutableStateFlow<TeamInfo?>(null)
    val gameName = MutableStateFlow("Game#${Random.nextInt(10_000)}")
    val port = MutableStateFlow<Int?>(8080)
    val canCreateGame = MutableStateFlow<Boolean>(false)
    val loadingTeams: MutableStateFlow<Boolean> = MutableStateFlow(true)

    init {
         loadTeamList()
    }

    private fun loadTeamList() {
        menuViewModel.navigatorContext.launch {
            CacheManager.loadTeams().map { teamFile ->
                val team = teamFile.team
                getTeamInfo(teamFile, team)
            }.let {
                availableTeams.value = it.sortedBy { it.teamName }
            }
        }
    }

    private suspend fun getTeamInfo(teamFile: JervisTeamFile, team: Team): TeamInfo {
        if (!IconFactory.hasLogo(team.id)) {
            IconFactory.saveLogo(team.id, teamFile.uiData.teamLogo ?: teamFile.rosterUiData.rosterLogo!!)
        }
        return TeamInfo(
            teamId = team.id,
            teamName = team.name,
            teamRoster = team.roster.name,
            teamValue = team.teamValue,
            rerolls = team.rerolls.size,
            logo = IconFactory.getLogo(team.id),
        )
    }

    fun startGame() {

    }

    fun setPort(port: String) {
        val newPort = port.toIntOrNull()
        if (newPort == null) {
            this.port.value = null
            this.canCreateGame.value = false
        } else {
            this.port.value = newPort
            this.canCreateGame.value = newPort in 1..65535
        }
    }

    private fun getLocalIp(): String {
        return "127.0.0.1"
    }

    private fun getPublicIp(): String {
        TODO()
    }

    fun setGameName(gameName: String) {
        this.gameName.value = gameName
        if (gameName.isBlank()) {
            canCreateGame.value = false
        } else {
            canCreateGame.value = true
        }
    }

    fun setSelectedTeam(team: TeamInfo?) {
        if (team == null || selectedTeam.value == team) {
            selectedTeam.value = null
            canCreateGame.value = false
        } else {
            selectedTeam.value = team
            canCreateGame.value = true
        }
    }

    fun setTeam(team: TeamInfo?) {
        if (team == null) {
            selectedTeam.value = null
            canCreateGame.value = false
        } else {
            selectedTeam.value = team
            canCreateGame.value = true
        }
    }

    fun loadTeamFromNetwork(
        teamId: String,
        onSuccess: () -> Unit,
        onError: (String) -> Unit,
    ) {
        val team = teamId.toIntOrNull() ?: error("Do something here")
        menuViewModel.navigatorContext.launch {
            try {
                val teamFile = FumbblTeamLoader().loadTeam(team, BB2020Rules())
                CacheManager.saveTeam(teamFile)
                val teamInfo = getTeamInfo(teamFile, teamFile.team)
                availableTeams.value = (availableTeams.value.filter { it.teamId != teamInfo.teamId } + teamInfo).sortedBy { it.teamName }
                onSuccess()
            } catch (e: Exception) {
                onError(e.message ?: "Unknown error")
            }
        }
    }
}

class P2PServerScreen(private val menuViewModel: MenuViewModel, private val screenModel: P2PServerScreenModel) : Screen {
    @Composable
    override fun Content() {
        MenuScreen {
            Box(modifier = Modifier.fillMaxSize().padding(24.dp)) {
                Row(modifier = Modifier.fillMaxSize()) {
                    GameCreation(modifier = Modifier.weight(1f).fillMaxSize(), screenModel)
                    Spacer(modifier = Modifier.width(16.dp).fillMaxHeight().background(color = Color.Transparent))
                    TeamSelector(modifier = Modifier.weight(1f).fillMaxSize(), screenModel)
                }
            }
        }
    }

    @Composable
    fun GameCreation(modifier: Modifier, viewModel: P2PServerScreenModel) {
        val gameName by viewModel.gameName.collectAsState("")
        val gamePort by viewModel.port.collectAsState( null)
        val canCreateGame: Boolean by screenModel.canCreateGame.collectAsState(false)
        val selectedTeam by screenModel.selectedTeam.collectAsState(null)
        Box(modifier = modifier.background(color = JervisTheme.awayTeamColor)) {
            Column(
                modifier = Modifier
                    .padding(16.dp)
                    .fillMaxSize()
                ,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    modifier = Modifier.padding(top = 32.dp, bottom = 32.dp),
                    text = "Game Settings",
                    style = MaterialTheme.typography.h2.copy(
                        fontWeight = FontWeight.Bold,
                        color = Color.White,

                    ),
                )
                OutlinedTextField(
                    value = gameName,
                    onValueChange = { viewModel.setGameName(it) },
                    label = { Text("Name") }
                )
                OutlinedTextField(
                    value = gamePort?.toString() ?: "",
                    onValueChange = { viewModel.setPort(it) },
                    label = { Text("Port") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    keyboardActions = KeyboardActions {

                    }
                )
                SelectedTeamBox(selectedTeam, viewModel)
                Spacer(modifier = Modifier.weight(1f))
                Button(
                    modifier = Modifier
                        .padding(bottom = 48.dp)
                    ,
                    onClick = { screenModel.startGame() },
                    enabled = canCreateGame,
                    colors = ButtonDefaults.buttonColors(backgroundColor = JervisTheme.homeTeamColor)
                ) {
                    Text(
                        modifier = Modifier.padding(16.dp),
                        text = "Create Game",
                        style = MaterialTheme.typography.h4.copy(
                            fontWeight = FontWeight.Bold,
                            color = JervisTheme.buttonTextColor,
                        )
                    )
                }
            }
        }
    }

    @Composable
    private fun SelectedTeamBox(
        selectedTeam: TeamInfo?,
        viewModel: P2PServerScreenModel
    ) {
        BoxWithConstraints {
            if (selectedTeam != null) {
                Box(
                    modifier = Modifier
                        .width(maxWidth * 0.5f)
                        .height(175.dp)
                        .padding(top = 32.dp)
                ) {
                    Row(modifier = Modifier.fillMaxSize()) {
                        TeamInfo(
                            name = selectedTeam.teamName,
                            teamValue = selectedTeam.teamValue,
                            rerolls = selectedTeam.rerolls,
                            logo = selectedTeam.logo,
                            onClick = { viewModel.setSelectedTeam(null) },
                        )
                    }
                }
            } else {
                Box(
                    modifier = Modifier
                        .alpha(0.75f)
                        .width(maxWidth * 0.5f)
                        .height(175.dp)
                        .padding(top = 32.dp)
                        .dashedBorder(width = 2.dp, color = Color.White, on = 10.dp, off = 10.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Select Team on the right",
                        color = Color.White,
                        style = MaterialTheme.typography.body2.copy(fontWeight = FontWeight.Bold)
                    )
                }
            }
        }
    }

    @Composable
    fun TeamSelector(modifier: Modifier, viewModel: P2PServerScreenModel) {
        val availableTeams by viewModel.availableTeams.collectAsState()
        var showImportFumbblTeam by remember { mutableStateOf(false) }
        Box(modifier = modifier.background(color = JervisTheme.awayTeamColor)) {
            Column(
                modifier = Modifier
                    .padding(16.dp)
                    .fillMaxSize()
                ,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    modifier = Modifier.padding(top = 32.dp, bottom = 32.dp),
                    text = "Available Teams",
                    style = MaterialTheme.typography.h2.copy(
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                    ),
                )
                Row {
                    Button(onClick = { }, modifier = Modifier.weight(1f)) {
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
                    }, modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Import from FUMBBL",
                            style = MaterialTheme.typography.body1.copy(
                                fontWeight = FontWeight.Bold,
                                color = Color.White,
                            ),
                        )
                    }
                }
                Column(
                    modifier = Modifier
                        .padding(top = 16.dp)
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                ) {
                    val lines = if (availableTeams.isEmpty()) 0 else (ceil(availableTeams.size / 2f)).toInt()
                    repeat(lines) { line ->
                        val team1 = availableTeams[line * 2]
                        val team2 = availableTeams.getOrNull(line * 2 + 1)
                        Row(modifier = Modifier.fillMaxWidth()) {
                            TeamInfo(
                                name = team1.teamName,
                                teamValue = team1.teamValue,
                                rerolls = team1.rerolls,
                                logo = team1.logo,
                                onClick = { viewModel.setSelectedTeam(team1) },
                            )
                            Spacer(modifier = Modifier.width(16.dp))
                            if (team2 != null) {
                                TeamInfo(
                                    name = team2.teamName,
                                    teamValue = team2.teamValue,
                                    logo = team2.logo,
                                    rerolls = team2.rerolls,
                                    onClick = { viewModel.setSelectedTeam(team2) },
                                )
                            } else {
                                Box(modifier = Modifier.weight(1f))
                            }
                        }
                        if (line < lines - 1) {
                            Spacer(modifier = Modifier.height(16.dp))
                        }
                    }
                }
            }
        }
        if (showImportFumbblTeam) {
            println("Show Import FUMBBL Team Dialog")
            LoadTeamDialog(viewModel, onCloseRequest = { showImportFumbblTeam = false })
        } else {
            println("Hide Import FUMBBL Team Dialog")
        }
    }
}

@Composable
fun LoadTeamDialog(
    viewModel: P2PServerScreenModel,
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

@Composable
fun RowScope.TeamInfo(
    name: String,
    teamValue: Int,
    rerolls: Int,
    logo: ImageBitmap,
    emptyTeam: Boolean = false,
    onClick: (() -> Unit)?
) {
    Box(
        modifier = Modifier
            .weight(1f)
            .background(Color.White)
            .let { if (onClick != null) it.clickable(!emptyTeam, onClick = onClick) else it }
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
                modifier = Modifier.background(JervisTheme.accentTeamColor),
                verticalAlignment = Alignment.Bottom,
            ) {
                Text(
                    modifier = Modifier.fillMaxWidth().padding(8.dp),
                    textAlign = TextAlign.Start,
                    text = name,
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                )
            }
        }
    }
}
