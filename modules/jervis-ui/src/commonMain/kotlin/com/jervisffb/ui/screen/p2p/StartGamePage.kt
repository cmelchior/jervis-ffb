package com.jervisffb.ui.screen.p2p

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PagerScope
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.Tab
import androidx.compose.material.TabPosition
import androidx.compose.material.TabRow
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.jervisffb.ui.view.JervisTheme
import com.jervisffb.ui.view.TeamTable
import com.jervisffb.ui.view.utils.JervisButton
import com.jervisffb.ui.view.utils.TitleBorder
import kotlinx.coroutines.launch

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun StartGamePage(
    viewModel: StartGameScreenModel,
    onAcceptGame: (Boolean) -> Unit,
) {
    Column(
        modifier = Modifier.fillMaxSize(),
    ) {
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
                    backgroundColor = Color.Transparent,
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
//                                            fontFamily = JervisTheme.fontFamily(),
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
                ) { page ->
                    when (page) {
                        0 -> TeamData()
                        1 -> TeamData()
                        else -> error("Invalid page index: $page")
                    }
                }
            }
        }
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
            JervisButton("Reject Game", onClick = { onAcceptGame(false) }, enabled = true)
            Spacer(modifier = Modifier.width(16.dp))
            JervisButton("Start Game", onClick = { onAcceptGame(true) }, enabled = true)
        }
    }
}

@Composable
private fun PagerScope.TeamData() {
    Column(
        modifier = Modifier.fillMaxWidth().verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        BoxWithConstraints(
            modifier = Modifier.defaultMinSize(950.dp).padding(top = 0.dp),
            contentAlignment = Alignment.TopCenter,
        ) {
            val width = if (950.dp < this.minWidth) 950.dp else this.minWidth
            TeamTable(width)
        }
    }
}
