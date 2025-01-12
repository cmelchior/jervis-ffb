package com.jervisffb.ui.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.AlertDialog
import androidx.compose.material.Button
import androidx.compose.material.DropdownMenuItem
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.ExposedDropdownMenuBox
import androidx.compose.material.MaterialTheme
import androidx.compose.material.OutlinedTextField
import androidx.compose.material.Switch
import androidx.compose.material.TabRowDefaults.Divider
import androidx.compose.material.Text
import androidx.compose.material.TextField
import androidx.compose.material.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import cafe.adriel.voyager.core.model.ScreenModel
import cafe.adriel.voyager.core.screen.Screen
import com.jervisffb.engine.model.BallType
import com.jervisffb.engine.model.PitchType
import com.jervisffb.engine.model.StadiumType
import com.jervisffb.engine.model.Team
import com.jervisffb.engine.model.TeamId
import com.jervisffb.engine.rules.BB2020Rules
import com.jervisffb.engine.rules.bb2020.tables.KickOffTable
import com.jervisffb.engine.rules.bb2020.tables.SpringWeatherTable
import com.jervisffb.engine.rules.bb2020.tables.StandardKickOffEventTable
import com.jervisffb.engine.rules.bb2020.tables.StandardWeatherTable
import com.jervisffb.engine.rules.bb2020.tables.SummerWeatherTable
import com.jervisffb.engine.rules.bb2020.tables.WeatherTable
import com.jervisffb.engine.rules.bb2020.tables.WinterWeatherTable
import com.jervisffb.engine.serialize.JervisTeamFile
import com.jervisffb.fumbbl.web.FumbblTeamLoader
import com.jervisffb.jervis_ui.generated.resources.Res
import com.jervisffb.jervis_ui.generated.resources.frontpage_ball
import com.jervisffb.ui.CacheManager
import com.jervisffb.ui.icons.IconFactory
import com.jervisffb.ui.isDigitsOnly
import com.jervisffb.ui.screen.p2pserver.GameSetupPage
import com.jervisffb.ui.screen.p2pserver.TeamSelectorPage
import com.jervisffb.ui.screen.p2pserver.WaitForOpponentPage
import com.jervisffb.ui.view.JervisTheme
import com.jervisffb.ui.view.utils.TitleBorder
import com.jervisffb.ui.viewmodel.MenuViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import kotlin.random.Random

data class TeamInfo(
    val teamId: TeamId,
    val teamName: String,
    val teamRoster: String,
    val teamValue: Int,
    val rerolls: Int,
    val logo: ImageBitmap
)

interface DropdownEntry {
    val name: String
    val available: Boolean
}

data class WeatherTableEntry(
    override val name: String,
    val table: WeatherTable,
    override val available: Boolean,
): DropdownEntry

data class KickOffTableEntry(
    override val name: String,
    val table: KickOffTable,
    override val available: Boolean,
): DropdownEntry

data class PitchEntry(
    override val name: String,
    val pitch: PitchType,
    override val available: Boolean = false
): DropdownEntry

data class StadiumEntry(
    override val name: String,
    val stadium: StadiumRule,
    override val available: Boolean = false
): DropdownEntry

interface UnusualBallRule
data object NoUnusualBall: UnusualBallRule
data object RollOnUnusualBallTable: UnusualBallRule
data class SpecificUnusualBall(val type: BallType): UnusualBallRule

interface StadiumRule
data object NoStadium: StadiumRule
data object RollForStadiumUsed: StadiumRule
data class SpecificStadium(val type: StadiumType): StadiumRule

data class UnusualBallEntry(
    override val name: String,
    val ball: UnusualBallRule,
    override val available: Boolean,
): DropdownEntry

class P2PServerScreenModel(private val menuViewModel: MenuViewModel) : ScreenModel {

    // Which page are currently being shown
    val totalPages = 4
    val currentPage = MutableStateFlow(0)

    val validGameSetup = MutableStateFlow(true)
    val validTeamSelection = MutableStateFlow(false)
    val validWaitingForOpponent = MutableStateFlow(false)

    val availableTeams = MutableStateFlow<List<TeamInfo>>(emptyList())
    val selectedTeam = MutableStateFlow<TeamInfo?>(null)
    val gameName = MutableStateFlow("Game#${Random.nextInt(10_000)}")
    val port = MutableStateFlow<Int?>(8080)
    val canCreateGame = MutableStateFlow<Boolean>(false)
    val loadingTeams: MutableStateFlow<Boolean> = MutableStateFlow(true)

    val selectedWeatherTable = MutableStateFlow<WeatherTableEntry?>(null)
    val selectedKickOffTable = MutableStateFlow<KickOffTableEntry?>(null)
    val selectedUnusualBall = MutableStateFlow<UnusualBallEntry?>(null)
    val selectedPitch = MutableStateFlow<PitchEntry?>(null)

    val weatherTables = listOf(
        "Rulebook" to listOf(
            WeatherTableEntry("Standard", StandardWeatherTable, true),
        ),
        "Death Zone" to listOf(
            WeatherTableEntry("Spring", SpringWeatherTable, false),
            WeatherTableEntry("Summer", SummerWeatherTable, false),
            WeatherTableEntry("Autumn", SummerWeatherTable, false),
            WeatherTableEntry("Winter", WinterWeatherTable, false),
            WeatherTableEntry("Subterranean", StandardWeatherTable, false),
            WeatherTableEntry("Primordial", StandardWeatherTable, false),
            WeatherTableEntry("Graveyard", StandardWeatherTable, false),
            WeatherTableEntry("Desolate Wasteland", StandardWeatherTable, false),
            WeatherTableEntry("Mountainous", StandardWeatherTable, false),
            WeatherTableEntry("Coastal", StandardWeatherTable, false),
            WeatherTableEntry("Desert", StandardWeatherTable, false),
        )
    )

    val kickOffTables = listOf(
        "Rulebook" to listOf(
            KickOffTableEntry("Standard", StandardKickOffEventTable, true),
        ),
        "Spike Magazine 15 (Amazons)" to listOf(
            KickOffTableEntry("Temple-City", StandardKickOffEventTable, false),
        )
    )

    val unusualBallList = listOf(
        "Rulebook" to listOf(
            UnusualBallEntry("Normal Ball", NoUnusualBall, true)
        ),
        "Death Zone" to listOf(
            UnusualBallEntry("Roll On Unusual Balls Table", RollOnUnusualBallTable, false),
            UnusualBallEntry("Explodin'", SpecificUnusualBall(BallType.EXPLODIN), false),
            UnusualBallEntry("Deamonic", SpecificUnusualBall(BallType.DEAMONIC), false),
            UnusualBallEntry("Stacked Lunch", SpecificUnusualBall(BallType.STACKED_LUNCH), false),
            UnusualBallEntry("Draconic", SpecificUnusualBall(BallType.DRACONIC), false),
            UnusualBallEntry("Spiteful Sprite", SpecificUnusualBall(BallType.SPITEFUL_SPRITE), false),
            UnusualBallEntry("Master-hewn", SpecificUnusualBall(BallType.MASTER_HEWN), false),
            UnusualBallEntry("Extra Spiky", SpecificUnusualBall(BallType.EXTRA_SPIKY), false),
            UnusualBallEntry("Greedy Nurgling", SpecificUnusualBall(BallType.GREEDY_NURGLING), false),
            UnusualBallEntry("Dark Majesty", SpecificUnusualBall(BallType.DARK_MAJESTY), false),
            UnusualBallEntry("Shady Special", SpecificUnusualBall(BallType.SHADY_SPECIAL), false),
            UnusualBallEntry("Soulstone", SpecificUnusualBall(BallType.SOULSTONE), false),
            UnusualBallEntry("Frozen", SpecificUnusualBall(BallType.FROZEN_BALL), false),
            UnusualBallEntry("Sacred Egg", SpecificUnusualBall(BallType.SACRED_EGG), false),
            UnusualBallEntry("Snotling Ball-suite", SpecificUnusualBall(BallType.SNOTLING_BALL_SUIT), false),
            UnusualBallEntry("Limpin' Squig", SpecificUnusualBall(BallType.LIMPIN_SQUIG), false),
            UnusualBallEntry("Warpstone Brazier", SpecificUnusualBall(BallType.WARPSTONE_BRAZIER), false),
        ),
        "Spike Magazine 14 (Norse)" to listOf(
            UnusualBallEntry("Hammer of Legend", SpecificUnusualBall(BallType.HAMMER_OF_LEGEND), false),
            UnusualBallEntry("The Runestone", SpecificUnusualBall(BallType.THE_RUNESTONE), false),
        ),
        "Spike Magazine 15 (Amazons)" to listOf(
            UnusualBallEntry("Crystal Skull", SpecificUnusualBall(BallType.CRYSTAL_SKULL), false),
            UnusualBallEntry("Snake-swallowed", SpecificUnusualBall(BallType.SNAKE_SWALLOWED), false),
        ),
    )

    val pitches = listOf(
        "Rulebook" to listOf(
            PitchEntry("Standard", PitchType.STANDARD, true),
        ),
        "Spike Magazine 14 (Norse)" to listOf(
            PitchEntry("Frozen Lake", PitchType.FROZEN_LAKE, false),
        ),
        "Spike Magazine 15 (Amazons)" to listOf(
            PitchEntry("Overgrown Jungle", PitchType.OVERGROWN_JUNGLE, false),
        )
    )

    val stadia = listOf(
        "Death Zone" to listOf(
            StadiumEntry("Disabled", NoStadium, true),
            StadiumEntry("Enabled", RollForStadiumUsed, false),
        ),
        "Unusual Playing Surface" to listOf(
            StadiumEntry("Ankle-Deep Water", SpecificStadium(StadiumType.ANKLE_DEEP_WATER), false),
            StadiumEntry("Sloping Pitch", SpecificStadium(StadiumType.SLOPING_PITCH), false),
            StadiumEntry("Ice", SpecificStadium(StadiumType.ICE), false),
            StadiumEntry("Astrogranite", SpecificStadium(StadiumType.ASTROGRANITE), false),
            StadiumEntry("Uneven Footing", SpecificStadium(StadiumType.UNEVEN_FOOTING), false),
            StadiumEntry("Solid Stone", SpecificStadium(StadiumType.SOLID_STONE), false),
        ),
    )



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

    fun setWeatherTable(entry: WeatherTableEntry) {
        selectedWeatherTable.value = entry
    }

    fun setKickOffTable(entry: KickOffTableEntry) {
        selectedKickOffTable.value = entry
    }

    fun setUnusualBall(entry: UnusualBallEntry) {
        selectedUnusualBall.value = entry
    }

    fun setPitch(entry: PitchEntry) {
        selectedPitch.value = entry
    }

    fun gameSetupDone() {
        // Should anything be saved here?
        currentPage.value = 1
    }

    fun teamSelectionDone() {
        // Should anything be saved here
        currentPage.value = 2
    }

    fun goBackToPage(previousPage: Int) {
        if (previousPage >= currentPage.value) {
            error("It is only allowed to go back: $previousPage")
        }
        currentPage.value = previousPage
    }

    fun userAcceptGame(acceptGame: Boolean) {
        // TODO
    }
}

class P2PServerScreen(private val menuViewModel: MenuViewModel, private val screenModel: P2PServerScreenModel) : Screen {
    @Composable
    override fun Content() {
        MenuScreenWithSidebarAndTitle(
            title = "Peer-to-Peer Game",
            icon = Res.drawable.frontpage_ball,
            currentPageFlow = screenModel.currentPage,
            onClick = { page -> screenModel.goBackToPage(page) },
        ) {
            PageContent(screenModel)
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
fun PageContent(screenModel: P2PServerScreenModel) {
    val currentPage by screenModel.currentPage.collectAsState()
    val pagerState = rememberPagerState(0) { screenModel.totalPages }

    // Animate going to a new page
    LaunchedEffect(currentPage) {
        pagerState.animateScrollToPage(currentPage)
    }

    Column(modifier = Modifier.padding(16.dp).fillMaxSize()) {
        HorizontalPager(
            modifier = Modifier.fillMaxWidth().weight(1f),
            userScrollEnabled = false,
            state = pagerState,
        ) { page ->
            when (page) {
                0 -> GameSetupPage(screenModel, Modifier, screenModel)
                1 -> TeamSelectorPage(Modifier, screenModel)
                2 -> WaitForOpponentPage(viewModel = screenModel)
                3 -> Box(modifier = Modifier.fillMaxSize()) {}
            }
        }
    }
}



@OptIn(ExperimentalLayoutApi::class)
@Composable
fun StandardGameSetup(screenModel: P2PServerScreenModel) {
    Box(
        modifier = Modifier.fillMaxSize().padding(top = 16.dp),
        contentAlignment = Alignment.TopCenter
    ) {
        Row(horizontalArrangement = Arrangement.Center) {
            Column(
                modifier = Modifier.weight(1f).padding(16.dp).wrapContentSize().verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                ExposedDropdownMenuWithSections<WeatherTableEntry>("Weather Table", screenModel.weatherTables) {
                    screenModel.setWeatherTable(it)
                }
                ExposedDropdownMenuWithSections<PitchEntry>("Pitch", screenModel.pitches) {
                    screenModel.setPitch(it)
                }
                ExposedDropdownMenuWithSections<DropdownEntry>("Stadia of the Old World", screenModel.stadia) {
                    // Do nothing
                }
            }
            Column(
                modifier = Modifier.weight(1f).padding(16.dp).wrapContentSize().verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                ExposedDropdownMenuWithSections<KickOffTableEntry>("Kick-off Table", screenModel.kickOffTables) {
                    screenModel.setKickOffTable(it)
                }
                ExposedDropdownMenuWithSections<UnusualBallEntry>("Ball", screenModel.unusualBallList) {
                    screenModel.setUnusualBall(it)
                }
                SimpleSwitch("Match Events", false) {

                }
            }
        }


    }
}

@Composable
fun SimpleSwitch(label: String, isSelected: Boolean, onSelected: (Boolean) -> Unit) {
    var isOn by remember { mutableStateOf(isSelected) }
    Row(modifier = Modifier.width(TextFieldDefaults.MinWidth), verticalAlignment = Alignment.CenterVertically) {
        Text(text = label)
        Spacer(modifier = Modifier.weight(1f))
        Switch(
            checked = isOn, // Current state
            onCheckedChange = { isOn = it } // Update state when toggled
        )
    }
}

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun <T> ExposedDropdownMenuWithSections(
    title: String,
    entries: List<Pair<String, List<DropdownEntry>>>,
    onSelected: (T) -> Unit,
) {
    var expanded by remember { mutableStateOf(false) }
    var selectedOption by remember { mutableStateOf(entries.first().second.first()) }
    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = !expanded },
    ) {
        OutlinedTextField(
            modifier = Modifier.padding(bottom = 8.dp),
            value = selectedOption.name,
            onValueChange = { },
            readOnly = true,
            label = { Text(title) },
        )
        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            entries.forEachIndexed { index, (sectionTitle, items) ->
                DropdownHeader(sectionTitle.uppercase())
                items.forEach { item ->
                    DropdownMenuItem(
                        onClick = {
                            selectedOption = item
                            expanded = false
                        }
                    ) {
                        Text(item.name)
                    }
                }
                if (index < entries.lastIndex) {
                    Divider()
                }
            }
        }
    }
}


@Composable
fun SettingsCard(title: String, width: Dp, content: @Composable () -> Unit) {
    Box(modifier = Modifier.width(width).padding(bottom = 8.dp)) {
        Column(modifier = Modifier.wrapContentSize()/*.padding(start = 16.dp, top = 8.dp, end = 16.dp, bottom = 16.dp)*/) {
            BoxHeader(title)
            Spacer(modifier = Modifier.height(16.dp))
            content()
        }
    }
}

@Composable
fun BoxHeader(text: String, color: Color = JervisTheme.rulebookRed) {
    TitleBorder(color)
    Box(
        modifier = Modifier.height(36.dp),
        contentAlignment = Alignment.CenterStart,
    ) {
        Text(
            modifier = Modifier.padding(bottom = 2.dp),
            text = text.uppercase(),
//            fontFamily = JervisTheme.fontFamily(),
            fontWeight = FontWeight.Bold,
            fontSize = 16.sp,
            color = color
        )
    }
    TitleBorder(color)
}

@Composable
fun DropdownHeader(text: String) {
    Text(
        modifier = Modifier.padding(start = 8.dp, top = 8.dp, bottom = 8.dp),
        text = text,
        style = MaterialTheme.typography.body1.copy(
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold,
            color = Color.Gray
        ),
    )
}
