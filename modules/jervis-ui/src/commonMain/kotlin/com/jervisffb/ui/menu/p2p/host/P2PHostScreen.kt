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
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.isSpecified
import androidx.compose.ui.unit.sp
import cafe.adriel.voyager.core.annotation.ExperimentalVoyagerApi
import cafe.adriel.voyager.core.lifecycle.LifecycleEffectOnce
import cafe.adriel.voyager.core.screen.Screen
import com.jervisffb.jervis_ui.generated.resources.Res
import com.jervisffb.jervis_ui.generated.resources.jervis_frontpage_griff
import com.jervisffb.ui.game.view.JervisTheme
import com.jervisffb.ui.game.view.SidebarMenu
import com.jervisffb.ui.game.view.utils.TitleBorder
import com.jervisffb.ui.game.viewmodel.MenuViewModel
import com.jervisffb.ui.menu.JervisScreen
import com.jervisffb.ui.menu.MenuScreenWithSidebarAndTitle
import com.jervisffb.ui.menu.p2p.SelectP2PTeamScreen
import com.jervisffb.ui.menu.p2p.StartP2PGamePage

interface DropdownEntry {
    val name: String
    val available: Boolean
}

class P2PHostScreen(private val menuViewModel: MenuViewModel, private val viewModel: P2PHostScreenModel) : Screen {
    @OptIn(ExperimentalVoyagerApi::class)
    @Composable
    override fun Content() {
        LifecycleEffectOnce {
            onDispose {
                viewModel.onDispose()
            }
        }
        val sidebarEntries = viewModel.sidebarEntries
        JervisScreen(menuViewModel) {
            MenuScreenWithSidebarAndTitle(
                menuViewModel,
                title = "Peer-to-Peer Game",
                icon = Res.drawable.jervis_frontpage_griff,
                topMenuRightContent = null,
                sidebarContent = {
                    val currentPage by viewModel.currentPage.collectAsState()
                    SidebarMenu(
                        entries = sidebarEntries,
                        currentPage = currentPage,
                    )
                }
            ) {
                PageContent(viewModel)
            }
        }
    }
}

@Composable
fun PageContent(viewModel: P2PHostScreenModel) {
    val currentPage by viewModel.currentPage.collectAsState()
    val pagerState = rememberPagerState(0) { viewModel.totalPages }

    // Animate going to a new page
    LaunchedEffect(currentPage) {
        pagerState.animateScrollToPage(currentPage)
    }

    Column(modifier = Modifier.padding(16.dp).fillMaxSize()) {
        HorizontalPager(
            modifier = Modifier.fillMaxWidth().weight(1f),
            userScrollEnabled = false,
            state = pagerState,
            beyondViewportPageCount = 5,
        ) { page ->
            when (page) {
                0 -> SetupGamePage(viewModel.setupGameModel, Modifier)
                1 -> SelectP2PTeamScreen(viewModel.selectTeamModel.componentModel, true, "Start Server", { viewModel.userAcceptedTeam() })
                2 -> WaitForOpponentPage(viewModel = viewModel)
                3 -> StartP2PGamePage(
                    viewModel.networkAdapter.homeTeam,
                    viewModel.networkAdapter.awayTeam,
                    onAcceptGame = { acceptedGame ->
                        viewModel.userAcceptGame(acceptedGame)
                    }
                )
            }
        }
    }
}

@Composable
fun SettingsCard(title: String, width: Dp, content: @Composable () -> Unit) {
    Box(modifier = Modifier.let { if (width.isSpecified) it.width(width) else it.fillMaxWidth() }.padding(bottom = 8.dp)) {
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
        modifier = Modifier.height(36.dp).fillMaxWidth(),
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
