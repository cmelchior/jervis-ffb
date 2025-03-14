package com.jervisffb.ui.menu.hotseat

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
import cafe.adriel.voyager.core.screen.Screen
import com.jervisffb.jervis_ui.generated.resources.Res
import com.jervisffb.jervis_ui.generated.resources.frontpage_wall_player
import com.jervisffb.ui.game.view.SidebarMenu
import com.jervisffb.ui.game.viewmodel.MenuViewModel
import com.jervisffb.ui.menu.JervisScreen
import com.jervisffb.ui.menu.MenuScreenWithSidebarAndTitle
import kotlinx.coroutines.flow.map

class HotseatScreen(private val menuViewModel: MenuViewModel, private val screenModel: HotseatScreenModel) : Screen {
    @Composable
    override fun Content() {
        val sidebarEntries = screenModel.sidebarEntries
        JervisScreen(menuViewModel) {
            MenuScreenWithSidebarAndTitle(
                menuViewModel,
                title = "Hotseat Game",
                icon = Res.drawable.frontpage_wall_player,
                topMenuRightContent = null,
                sidebarContent = {
                    val currentPage by screenModel.currentPage.collectAsState()
                    SidebarMenu(
                        entries = sidebarEntries,
                        currentPage = currentPage,
                        // onClick = { page: Int -> screenModel.goBackToPage(page) }
                    )
                }
            ) {
                PageContent(screenModel)
            }
        }
    }
}

@Composable
fun PageContent(screenModel: HotseatScreenModel) {
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
                0 -> SetupHotseatGamePage(screenModel.setupGameModel, Modifier)
                1 -> SelectHotseatTeamScreen(screenModel.selectHomeTeamModel)
                2 -> SelectHotseatTeamScreen(screenModel.selectAwayTeamModel)
                3 -> StartHotseatGamePage(
                    screenModel.selectedHomeTeam.map { it?.teamData },
                    screenModel.selectedAwayTeam.map { it?.teamData },
                    onAcceptGame = { acceptedGame ->
                        screenModel.startGame()
                    }
                )
            }
        }
    }
}
