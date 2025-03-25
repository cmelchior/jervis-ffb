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
import com.jervisffb.ui.menu.p2p.SelectP2PTeamScreen
import com.jervisffb.ui.menu.p2p.StartP2PGamePage

class P2PClientScreen(private val menuViewModel: MenuViewModel, private val viewModel: P2PClientScreenModel) : Screen {
    @OptIn(ExperimentalVoyagerApi::class)
    @Composable
    override fun Content() {
        val sidebarEntries = viewModel.sidebarEntries
        LifecycleEffectOnce {
            onDispose {
                viewModel.onDispose()
            }
        }
        JervisScreen(menuViewModel) {
            MenuScreenWithSidebarAndTitle(
                menuViewModel,
                title = "Peer-to-Peer Game",
                icon = Res.drawable.frontpage_wall_player,
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
private fun PageContent(viewModel: P2PClientScreenModel) {
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
        ) { page ->
            when (page) {
                0 -> JoinHostScreen(
                    viewModel = viewModel.joinHostModel,
                    onJoin = {
                        if (viewModel.lastValidPage >= 1) {
                            viewModel.hostJoinedDone()
                        } else {
                            viewModel.joinHostModel.clientJoinGame()
                        }
                    },
                    onCancel = { viewModel.joinHostModel.disconnectFromHost() },
                )
                1 -> SelectP2PTeamScreen(
                    viewModel = viewModel.selectTeamModel.componentModel,
                    confirmTitle = "Next",
                    onNext = { viewModel.teamSelectionDone() }
                )
                2 -> StartP2PGamePage(
                    viewModel.networkAdapter.homeTeam,
                    viewModel.networkAdapter.awayTeam,
                    onAcceptGame = { acceptedGame ->
                        viewModel.userAcceptGame(acceptedGame)
                    }
                )
                else -> error("Invalid page index: $page")
            }
        }
    }
}
