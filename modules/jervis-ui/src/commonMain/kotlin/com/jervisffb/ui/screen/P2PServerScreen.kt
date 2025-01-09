package com.jervisffb.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.AlertDialog
import androidx.compose.material.Button
import androidx.compose.material.DropdownMenuItem
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.ExposedDropdownMenuBox
import androidx.compose.material.MaterialTheme
import androidx.compose.material.OutlinedTextField
import androidx.compose.material.ScrollableTabRow
import androidx.compose.material.Switch
import androidx.compose.material.Tab
import androidx.compose.material.TabPosition
import androidx.compose.material.TabRow
import androidx.compose.material.TabRowDefaults
import androidx.compose.material.TabRowDefaults.Divider
import androidx.compose.material.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material.Text
import androidx.compose.material.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
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
import com.jervisffb.ui.CacheManager
import com.jervisffb.ui.dropShadow
import com.jervisffb.ui.icons.IconFactory
import com.jervisffb.ui.isDigitsOnly
import com.jervisffb.ui.screen.p2pserver.TeamSelectorPage
import com.jervisffb.ui.screen.p2pserver.WaitForOpponentPage
import com.jervisffb.ui.view.JervisTheme
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
}

class P2PServerScreen(private val menuViewModel: MenuViewModel, private val screenModel: P2PServerScreenModel) : Screen {
    @Composable
    override fun Content() {
        MenuScreenWithTitle("Peer-to-Peer Game") {
            Box(modifier = Modifier
                .fillMaxSize()
                .padding(start = 24.dp, top = 48.dp, end = 24.dp, bottom = 24.dp)
                .background(color = JervisTheme.contentBackgroundColor)
                .border(width = 8.dp, color = JervisTheme.awayTeamColor)

            ) {
                Box(modifier = Modifier
                    .fillMaxSize()
                    .dropShadow(
                        color = Color.Black,
                        blurRadius = 4.dp
                    )
                    .background(color = JervisTheme.contentBackgroundColor)
                ) {
                    PageContent(screenModel)
                }
            }
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
    val tabs = listOf("1. Configure Game", "2. Select Team", "3. Wait for Opponent", "4. Start Game")
    val pagerState = rememberPagerState(0) { tabs.size }
    val scope = rememberCoroutineScope()
    val defaultIndicator = @Composable { tabPositions: List<TabPosition> ->
        TabRowDefaults.Indicator(
            modifier = Modifier
                .tabIndicatorOffset(tabPositions[pagerState.currentPage])
                .padding(horizontal = 16.dp)
                .height(6.dp)
            ,
            color = JervisTheme.homeTeamColor,
        )
    }

    // Animate going to a new page
    LaunchedEffect(currentPage) {
        pagerState.animateScrollToPage(currentPage)
    }

    Column(modifier = Modifier.padding(16.dp).fillMaxSize()) {
        ScrollableTabRow(
            selectedTabIndex = pagerState.currentPage,
            backgroundColor = JervisTheme.contentBackgroundColor,
            edgePadding = 0.dp,
            indicator = defaultIndicator,
            divider = @Composable { /* None */ }
        ){
            tabs.forEachIndexed { index, title ->
                Tab(
                    modifier = Modifier.padding(
                        start = 16.dp,
                        top = 0.dp,
                        end = 16.dp,
                        bottom = 6.dp
                    ),
                    text = {
                        Text(
                            modifier = Modifier.align(Alignment.CenterHorizontally),
                            fontFamily = JervisTheme.fontFamily(),
                            fontSize = 24.sp,
                            text = title.uppercase(),
                        )
                    },
                    selected = (pagerState.currentPage == index),
                    onClick = {
                        screenModel.goBackToPage(index)
                    },
                    enabled = (index < currentPage)
                )
            }
        }
        HorizontalPager(
            modifier = Modifier.fillMaxWidth().weight(1f),
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

@Composable
fun GameSetupPage(screenModel: P2PServerScreenModel, modifier: Modifier, viewModel: P2PServerScreenModel) {
    val gameName by viewModel.gameName.collectAsState("")
    val gamePort by viewModel.port.collectAsState( null)
    val canCreateGame: Boolean by screenModel.canCreateGame.collectAsState(false)
    val selectedTeam by screenModel.selectedTeam.collectAsState(null)

    Column(
        modifier = modifier.fillMaxSize().padding(16.dp),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
            ,
            verticalAlignment = Alignment.Top
        ) {
            SettingsCard("DETAILS") {
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
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = "IP: 85.191.6.149"
                    )
                }
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.fillMaxSize()) {
                val pagerStateTop = rememberPagerState(0) { 5 }
                val pagerStateBottom = rememberPagerState(0) { 4 }
                val tabs = listOf("Standard", "BB7", "Dungeon Bowl", "Gutter Bowl", "From File")
                val tabs2 = listOf("Tables/Modifications", "Timers", "Inducements", "Rules")
                val coroutineScope = rememberCoroutineScope()
                Box(modifier = Modifier.fillMaxSize()) {
                    Column(modifier = Modifier.fillMaxSize()) {
                        TabRow(
                            backgroundColor = Color.Transparent,
                            selectedTabIndex = pagerStateTop.currentPage
                        ) {
                            tabs.forEachIndexed { index, title ->
                                Tab(
                                    selected = pagerStateTop.currentPage == index,
                                    onClick = {
                                        coroutineScope.launch {
                                            pagerStateTop.animateScrollToPage(index)
                                        }
                                    },
                                    text = { Text(title) }
                                )
                            }
                        }
                        TabRow(
                            backgroundColor = Color.Transparent,
                            selectedTabIndex = pagerStateBottom.currentPage
                        ) {
                            tabs2.forEachIndexed { index, title ->
                                Tab(
                                    selected = pagerStateBottom.currentPage == index,
                                    onClick = {
                                        coroutineScope.launch {
                                            pagerStateBottom.animateScrollToPage(index)
                                        }
                                    },
                                    text = { Text(title) }
                                )
                            }
                        }
                        HorizontalPager(
                            modifier = Modifier.fillMaxSize(),
                            state = pagerStateBottom,
                        ) { page ->
                            when (page) {
                                0 -> StandardGameSetup(screenModel)
                                else -> StandardGameSetup(screenModel)
                            }
                        }
                    }
                }
            }
        }
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
            Button(
                onClick = { viewModel.gameSetupDone() },
            ) {
                Text("NEXT")
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
        Column(
            modifier = Modifier.padding(16.dp).wrapContentSize().verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
            ExposedDropdownMenuWithSections<WeatherTableEntry>("Weather Table", screenModel.weatherTables) {
                screenModel.setWeatherTable(it)
            }
            ExposedDropdownMenuWithSections<KickOffTableEntry>("Kick-off Table", screenModel.kickOffTables) {
                screenModel.setKickOffTable(it)
            }
            ExposedDropdownMenuWithSections<PitchEntry>("Pitch", screenModel.pitches) {
                screenModel.setPitch(it)
            }
            ExposedDropdownMenuWithSections<UnusualBallEntry>("Ball", screenModel.unusualBallList) {
                screenModel.setUnusualBall(it)
            }
            ExposedDropdownMenuWithSections<DropdownEntry>("Stadia of the Old World", screenModel.stadia) {
                // Do nothing
            }
            SimpleSwitch("Match Events", false) {

            }
        }
    }
}

@Composable
fun SimpleSwitch(label: String, isSelected: Boolean, onSelected: (Boolean) -> Unit) {
    var isOn by remember { mutableStateOf(isSelected) }
    Row(modifier = Modifier.width(400.dp), verticalAlignment = Alignment.CenterVertically) {
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
fun SettingsCard(title: String, content: @Composable () -> Unit) {
    Box(modifier = Modifier.padding(bottom = 8.dp)) {
        Column(modifier = Modifier.padding(start = 16.dp, top = 8.dp, end = 16.dp, bottom = 16.dp)) {
            BoxHeader(title)
            content()
        }
    }
}

@Composable
fun BoxHeader(text: String) {
    Text(
        modifier = Modifier.padding(bottom = 8.dp),
        text = text,
        style = MaterialTheme.typography.body1.copy(
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold,
            color = Color.Gray
        ),
    )
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
