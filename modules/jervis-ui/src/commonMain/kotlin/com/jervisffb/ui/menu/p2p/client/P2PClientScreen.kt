package com.jervisffb.ui.menu.p2p.client

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.core.annotation.ExperimentalVoyagerApi
import cafe.adriel.voyager.core.lifecycle.LifecycleEffectOnce
import cafe.adriel.voyager.core.screen.Screen
import com.jervisffb.jervis_ui.generated.resources.Res
import com.jervisffb.jervis_ui.generated.resources.frontpage_wall_player
import com.jervisffb.ui.game.view.SidebarMenu
import com.jervisffb.ui.game.viewmodel.MenuViewModel
import com.jervisffb.ui.menu.JervisScreen
import com.jervisffb.ui.menu.MenuScreenWithSidebarAndTitle
import com.jervisffb.ui.menu.p2p.StartP2PGamePage
import com.jervisffb.ui.menu.p2p.TeamSelectorPage

class P2PClientScreen(private val menuViewModel: MenuViewModel, private val screenModel: P2PClientScreenModel) : Screen {
    @OptIn(ExperimentalVoyagerApi::class)
    @Composable
    override fun Content() {
        LifecycleEffectOnce {
            onDispose {
                screenModel.onDispose()
            }
        }
        JervisScreen(menuViewModel) {
            MenuScreenWithSidebarAndTitle(
                menuViewModel,
                title = "Peer-to-Peer Game",
                icon = Res.drawable.frontpage_wall_player,
                topMenuRightContent = null,
                sidebarContent = {
                    val currentPage by screenModel.currentPage.collectAsState()
                    SidebarMenu(
                        entries = listOf("1. Join Host", "2. Select Team", "3. Start Game"),
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
                0 -> JoinHostScreen(
                    viewModel = screenModel.joinHostModel,
                    onJoin = {
                        if (screenModel.lastValidPage >= 1) {
                            screenModel.hostJoinedDone()
                        } else {
                            screenModel.joinHostModel.clientJoinGame()
                        }
                    },
                    onCancel = { screenModel.joinHostModel.disconnectFromHost() },
                )
                1 -> TeamSelectorPage(
                    viewModel = screenModel.selectTeamModel,
                    confirmTitle = "Next",
                    onNext = { screenModel.teamSelectionDone() }
                )
                2 -> StartP2PGamePage(
                    screenModel.controller.homeTeam,
                    screenModel.controller.awayTeam,
                    onAcceptGame = { acceptedGame ->
                        screenModel.userAcceptGame(acceptedGame)
                    }
                )
                else -> error("Invalid page index: $page")
            }
        }
    }
}
