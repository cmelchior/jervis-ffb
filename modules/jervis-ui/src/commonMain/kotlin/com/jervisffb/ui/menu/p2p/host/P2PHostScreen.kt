package com.jervisffb.ui.menu.p2p.host

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
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
import com.jervisffb.engine.model.Team
import com.jervisffb.engine.model.TeamId
import com.jervisffb.engine.rules.bb2020.tables.KickOffTable
import com.jervisffb.engine.rules.bb2020.tables.WeatherTable
import com.jervisffb.jervis_ui.generated.resources.Res
import com.jervisffb.jervis_ui.generated.resources.frontpage_wall_player
import com.jervisffb.ui.game.view.JervisTheme
import com.jervisffb.ui.game.view.SidebarMenu
import com.jervisffb.ui.game.view.utils.TitleBorder
import com.jervisffb.ui.game.viewmodel.MenuViewModel
import com.jervisffb.ui.menu.JervisScreen
import com.jervisffb.ui.menu.MenuScreenWithSidebarAndTitle
import com.jervisffb.ui.menu.p2p.StartP2PGamePage
import com.jervisffb.ui.menu.p2p.TeamSelectorPage

data class TeamInfo(
    val teamId: TeamId,
    val teamName: String,
    val teamRoster: String,
    val teamValue: Int,
    val rerolls: Int,
    val logo: ImageBitmap,
    val teamData: Team?, // For now just keep a reference to the original team. Might change later if teams are loaded on the server
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

class P2PServerScreen(private val menuViewModel: MenuViewModel, private val screenModel: P2PHostScreenModel) : Screen {
    @Composable
    override fun Content() {
        JervisScreen(menuViewModel) {
            MenuScreenWithSidebarAndTitle(
                menuViewModel,
                title = "Peer-to-Peer Game",
                icon = Res.drawable.frontpage_wall_player,
                topMenuRightContent = null,
                sidebarContent = {
                    val currentPage by screenModel.currentPage.collectAsState()
                    SidebarMenu(
                        entries = listOf("1. Configure Game", "2. Select Team", "3. Wait For Opponent", "4. Start Game"),
                        currentPage = currentPage,
                        onClick = { page: Int -> screenModel.goBackToPage(page) }
                    )
                }
            ) {
                PageContent(screenModel)
            }
        }
    }
}

@Composable
fun PageContent(screenModel: P2PHostScreenModel) {
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
                0 -> SetupGamePage(screenModel.setupGameModel, Modifier)
                1 -> TeamSelectorPage(screenModel.selectTeamModel, "Start Server", { screenModel.teamSelectionDone() })
                2 -> WaitForOpponentPage(viewModel = screenModel)
                3 -> StartP2PGamePage(
                    screenModel.controller.homeTeam,
                    screenModel.controller.awayTeam,
                    onAcceptGame = { acceptedGame ->
                        screenModel.userAcceptGame(acceptedGame)
                    }
                )
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
fun ColumnScope.BoxHeader(
    text: String,
    color: Color = JervisTheme.rulebookRed,
    topPadding: Dp = 0.dp,
    bottomPadding: Dp = 0.dp,
) {
    if (topPadding > 0.dp) {
        Spacer(modifier = Modifier.height(topPadding))
    }
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
    if (bottomPadding > 0.dp) {
        Spacer(modifier = Modifier.height(bottomPadding))
    }
}
