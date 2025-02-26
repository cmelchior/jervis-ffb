package com.jervisffb.ui.menu.components.setup

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.ScrollableTabRow
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
import com.jervisffb.ui.game.view.JervisTheme
import com.jervisffb.ui.game.view.utils.TitleBorder
import com.jervisffb.ui.menu.components.ExposedDropdownMenuWithSections
import com.jervisffb.ui.menu.components.SimpleSwitch
import com.jervisffb.ui.menu.p2p.host.DropdownEntry
import com.jervisffb.ui.menu.p2p.host.KickOffTableEntry
import com.jervisffb.ui.menu.p2p.host.PitchEntry
import com.jervisffb.ui.menu.p2p.host.UnusualBallEntry
import com.jervisffb.ui.menu.p2p.host.WeatherTableEntry
import kotlinx.coroutines.launch

@Composable
fun SetupGameComponent(setupModel: SetupGameComponentModel, timerModel: SetupTimersComponentModel) {
    val pagerStateTop = rememberPagerState(0) { 5 }
    val pagerStateBottom = rememberPagerState(0) { 4 }
    val tabs = listOf("Standard", "BB7", "From File") // listOf("Standard", "BB7", "Dungeon Bowl", "Gutter Bowl", "From File")
    val tabs2 = listOf("Rules", "Timers", "Inducements", "Modifications")
    val coroutineScope = rememberCoroutineScope()

    Column(modifier = Modifier.fillMaxSize()) {
        val emptyIndicator = @Composable { tabPositions: List<TabPosition> ->
            // Do nothing
        }
        Box(modifier = Modifier.fillMaxSize()) {
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
                                )
                            ,
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
                ScrollableTabRow(
                    modifier = Modifier.fillMaxWidth().height(36.dp),
                    backgroundColor = Color.Transparent,
                    edgePadding = 0.dp,
                    indicator = emptyIndicator, //defaultIndicatorBottom,
                    divider = @Composable { /* None */ },
                    selectedTabIndex = pagerStateBottom.currentPage
                ) {
                    tabs2.forEachIndexed { index, title ->
                        val isSelected = (pagerStateBottom.currentPage == index)
                        Tab(
                            modifier = Modifier
                                .background(
                                    if (isSelected) JervisTheme.rulebookRed else Color.Transparent,
                                ),
                            selected = isSelected,
                            onClick = {
                                coroutineScope.launch {
                                    pagerStateBottom.animateScrollToPage(index)
                                }
                            },
                            text = {
                                val fontColor = if (isSelected) {
                                    JervisTheme.white
                                } else {
                                    JervisTheme.rulebookRed
                                }
                                Text(
                                    modifier = Modifier.padding(horizontal = 8.dp),
                                    maxLines = 1,
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
                    state = pagerStateBottom,
                ) { page ->
                    when (page) {
                        0 -> SetupRulesSection(setupModel) // Rules
                        1 -> SetupTimersComponent(timerModel) // Timers
                        2 -> SetupRulesSection(setupModel) // Inducements
                        3 -> SetupRulesSection(setupModel) // Rules
                        else -> error("Unsupported page: $page")
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun SetupRulesSection(screenModel: SetupGameComponentModel) {
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

