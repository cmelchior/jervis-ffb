package com.jervisffb.ui.screen.p2p.client

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.core.screen.Screen
import com.jervisffb.jervis_ui.generated.resources.Res
import com.jervisffb.jervis_ui.generated.resources.frontpage_wall_player
import com.jervisffb.ui.screen.JervisScreen
import com.jervisffb.ui.screen.MenuScreenWithSidebarAndTitle
import com.jervisffb.ui.screen.SidebarEntry
import com.jervisffb.ui.screen.p2p.TeamSelectorPage
import com.jervisffb.ui.view.JervisTheme
import com.jervisffb.ui.view.utils.paperBackgroundWithLine
import com.jervisffb.ui.viewmodel.MenuViewModel

class P2PClientScreen(private val menuViewModel: MenuViewModel, private val screenModel: P2PClientScreenModel) : Screen {
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
                    val onClick = { page: Int -> screenModel.goBackToPage(page) }
                    val entries = listOf("1. Select Team", "2. Join Host", "3. Start Game")
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
                            SidebarEntry(entry, selected = selected, onClick = clickHandler)
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
private fun PageContent(screenModel: P2PClientScreenModel) {
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
                0 -> TeamSelectorPage(
                    viewModel = screenModel.selectTeamModel,
                    onNext = { screenModel.teamSelectionDone() }
                )
                1 -> JoinHostScreen(
                    viewModel = screenModel.joinHostModel,
                    onCancel = { screenModel.clientRejectGame() },
                    onJoin = { screenModel.clientAcceptGame() }
                )
//                0 -> //GameSetupPage(screenModel, Modifier, screenModel)
//                1 -> TeamSelectorPage(Modifier, screenModel)
//                2 -> WaitForOpponentPage(viewModel = screenModel)
                else -> Box(modifier = Modifier.fillMaxSize()) {}
            }
        }
    }
}
