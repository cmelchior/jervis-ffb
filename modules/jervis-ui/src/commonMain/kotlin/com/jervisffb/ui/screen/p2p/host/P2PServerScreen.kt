package com.jervisffb.ui.screen.p2p.host

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
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
import cafe.adriel.voyager.core.screen.Screen
import com.jervisffb.engine.model.BallType
import com.jervisffb.engine.model.PitchType
import com.jervisffb.engine.model.StadiumType
import com.jervisffb.engine.model.TeamId
import com.jervisffb.engine.rules.bb2020.tables.KickOffTable
import com.jervisffb.engine.rules.bb2020.tables.WeatherTable
import com.jervisffb.jervis_ui.generated.resources.Res
import com.jervisffb.jervis_ui.generated.resources.frontpage_wall_player
import com.jervisffb.ui.isDigitsOnly
import com.jervisffb.ui.screen.p2p.TeamSelectorPage
import com.jervisffb.ui.screen.p2p.TeamSelectorScreenModel
import com.jervisffb.ui.view.JervisTheme
import com.jervisffb.ui.view.utils.TitleBorder
import com.jervisffb.ui.view.utils.paperBackgroundWithLine
import com.jervisffb.ui.viewmodel.MenuViewModel

data class TeamInfo(
    val teamId: TeamId,
    val teamName: String,
    val teamRoster: String,
    val teamValue: Int,
    val rerolls: Int,
    val logo: ImageBitmap,
    val teamData: com.jervisffb.engine.model.Team?, // For now just keep a reference to the original team. Might change later if teams are loaded on the server
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

class P2PServerScreen(private val menuViewModel: MenuViewModel, private val screenModel: P2PServerScreenModel) : Screen {
    @Composable
    override fun Content() {
        _root_ide_package_.com.jervisffb.ui.screen.JervisScreen(menuViewModel) {
            _root_ide_package_.com.jervisffb.ui.screen.MenuScreenWithSidebarAndTitle(
                menuViewModel,
                title = "Peer-to-Peer Game",
                icon = Res.drawable.frontpage_wall_player,
                topMenuRightContent = null,
                sidebarContent = {
                    val currentPage by screenModel.currentPage.collectAsState()
                    val onClick = { page: Int -> screenModel.goBackToPage(page) }
                    val entries = listOf("1. Configure Game", "2. Select Team", "3. Wait For Opponent", "4. Start Game")
                    Column(
                        modifier = Modifier.paperBackgroundWithLine(JervisTheme.rulebookBlue)
                            .padding(start = 16.dp, top = 0.dp, end = 16.dp, bottom = 8.dp),
                    ) {
                        Spacer(modifier = Modifier.fillMaxHeight(0.2f))
                        Spacer(modifier = Modifier.height(16.dp))
                        entries.forEachIndexed { index, entry ->
                            val selected = (index == currentPage)
                            val isPrevious = (index < currentPage)
                            val clickHandler: () -> Unit = if (isPrevious) ({ onClick(index) }) else ({ })
                            _root_ide_package_.com.jervisffb.ui.screen.SidebarEntry(
                                entry,
                                selected = selected,
                                onClick = clickHandler
                            )
                        }
                        Spacer(modifier = Modifier.fillMaxHeight(0.20f))
                    }
                }
            ) {
                PageContent(screenModel)
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
                0 -> SetupGamePage(screenModel, Modifier, screenModel)
                1 -> TeamSelectorPage(screenModel.selectTeamModel, { screenModel.teamSelectionDone() })
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
fun ColumnScope.BoxHeader(text: String, color: Color = JervisTheme.rulebookRed) {
    TitleBorder(color)
    Box(
        modifier = Modifier.height(36.dp),
        contentAlignment = Alignment.CenterStart,
    ) {
        Text(
            modifier = Modifier.padding(bottom = 2.dp),
            text = text.uppercase(),
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
