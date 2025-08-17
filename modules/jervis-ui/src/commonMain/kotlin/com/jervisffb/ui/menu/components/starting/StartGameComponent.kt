package com.jervisffb.ui.menu.components.starting

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PagerScope
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Tab
import androidx.compose.material3.TabPosition
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.jervisffb.engine.model.Team
import com.jervisffb.ui.game.view.JervisTheme
import com.jervisffb.ui.game.view.TeamTable
import com.jervisffb.ui.game.view.utils.TitleBorder
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ColumnScope.StartGameComponent(
    homeTeam: Flow<Team?>,
    awayTeam: Flow<Team?>,
) {
    val homeTeam: Team? by homeTeam.collectAsState(null)
    val awayTeam: Team? by awayTeam.collectAsState(null)

    val pagerStateTop = rememberPagerState(0) { 2 }
    val tabs = listOf("Home Team", "Away Team")
    val coroutineScope = rememberCoroutineScope()

    val emptyIndicator = @Composable { tabPositions: List<TabPosition> ->
        // Do nothing
    }

    Box(modifier = Modifier.fillMaxSize().weight(1f)) {
        Column(modifier = Modifier.fillMaxSize()) {
            TitleBorder()
            TabRow(
                modifier = Modifier.fillMaxWidth().height(36.dp),
                containerColor = Color.Transparent,
                selectedTabIndex = pagerStateTop.currentPage,
                indicator = emptyIndicator,
                divider = @Composable { /* None */ },
            ) {
                tabs.forEachIndexed { index, title ->
                    val isSelected = (pagerStateTop.currentPage == index)
                    Tab(
                        modifier = Modifier
                            .background(
                                if (isSelected) JervisTheme.rulebookRed else Color.Transparent,
                            ),
                        selected = isSelected,
                        onClick = {
                            coroutineScope.launch {
                                pagerStateTop.animateScrollToPage(index)
                            }
                        },
                        text = {
                            val fontColor = if (isSelected) {
                                JervisTheme.white
                            } else {
                                JervisTheme.rulebookRed
                            }
                            Text(
                                text = title.uppercase(),
                                fontWeight = FontWeight.Bold,
                                color = fontColor,
                                fontSize = 16.sp
                            )
                        },
                        selectedContentColor = JervisTheme.rulebookRed,
                        unselectedContentColor = JervisTheme.white,
                    )
                }
            }
            TitleBorder()
            HorizontalPager(
                modifier = Modifier.fillMaxSize(),
                state = pagerStateTop,
                beyondViewportPageCount = 5
            ) { page ->
                when (page) {
                    0 -> TeamData(homeTeam, true)
                    1 -> TeamData(awayTeam, false)
                    else -> error("Invalid page index: $page")
                }
            }
        }
    }
}

@Composable
private fun PagerScope.TeamData(team: Team?, isOnHomeTeam: Boolean = true) {
    Column(
        modifier = Modifier.fillMaxWidth().verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        BoxWithConstraints(
            modifier = Modifier.defaultMinSize(950.dp).padding(top = 0.dp),
            contentAlignment = Alignment.TopCenter,
        ) {
            val width = if (950.dp < this.minWidth) 950.dp else this.minWidth
            if (team != null) {
                TeamTable(width, team, isOnHomeTeam)
            } else {
                // Figure out what to do here
                Text("No team data available.")
            }
        }
    }
}
