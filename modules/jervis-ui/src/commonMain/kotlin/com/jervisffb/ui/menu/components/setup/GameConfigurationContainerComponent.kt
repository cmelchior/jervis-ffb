package com.jervisffb.ui.menu.components.setup

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
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
import kotlinx.coroutines.launch

@Composable
fun GameConfigurationContainerComponent(componentModel: GameConfigurationContainerComponentModel) {
    val pagerStateTop = rememberPagerState(0) { 5 }
    val pagerStateBottom = rememberPagerState(0) { 4 }
    val tabs = listOf("Continue From File", "Standard", "BB7", "Dungeon Bowl", "Gutter Bowl") // listOf("Standard", "BB7", "Dungeon Bowl", "Gutter Bowl", "From File")
    val tabs2 = listOf("Rules", "Timers", "Inducements", "Customizations")
    val coroutineScope = rememberCoroutineScope()

    Column(modifier = Modifier.fillMaxSize()) {
        val emptyIndicator = @Composable { tabPositions: List<TabPosition> ->
            // Do nothing
        }
        Box(modifier = Modifier.fillMaxSize()) {
            Column(modifier = Modifier.fillMaxSize()) {
                TitleBorder()
                ScrollableTabRow(
                    modifier = Modifier.fillMaxWidth().height(36.dp),
                    backgroundColor = Color.Transparent,
                    selectedTabIndex = pagerStateTop.currentPage,
                    edgePadding = 0.dp,
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
                                    modifier = Modifier.padding(horizontal = 8.dp),
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
                TabRow(
                    modifier = Modifier.fillMaxWidth().height(36.dp),
                    backgroundColor = Color.Transparent,
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
                    modifier = Modifier.fillMaxWidth().align(Alignment.CenterHorizontally),
                    state = pagerStateBottom,
                ) { page ->
                    when (page) {
                        0 -> SetupRulesComponent(componentModel.rulesModel) // Rules
                        1 -> TimersSetupComponent(componentModel.timersModel) // Timers
                        2 -> InducementsSetupComponent(componentModel.inducementsModel) // Inducements
                        3 -> CustomizationSetupComponent(componentModel.customizationsModel) // Rules
                        else -> error("Unsupported page: $page")
                    }
                }
            }
        }
    }
}
