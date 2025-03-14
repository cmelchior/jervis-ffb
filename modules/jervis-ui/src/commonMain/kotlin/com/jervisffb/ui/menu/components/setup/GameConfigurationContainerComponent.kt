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
import androidx.compose.material.Tab
import androidx.compose.material.TabPosition
import androidx.compose.material.TabRow
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.jervisffb.ui.game.view.JervisTheme
import com.jervisffb.ui.game.view.utils.TitleBorder
import kotlinx.coroutines.launch

@Composable
fun GameConfigurationContainerComponent(viewModel: GameConfigurationContainerComponentModel) {
    val tabs = viewModel.tabs
    val selectedTab by viewModel.selectedGameTab.collectAsState()
    val pagerState = rememberPagerState(0) { tabs[selectedTab].tabs.size }
    val coroutineScope = rememberCoroutineScope()
    val showSetupTabs = tabs[selectedTab].showSetupTabs

    Column(modifier = Modifier.fillMaxSize()) {
        val emptyIndicator = @Composable { tabPositions: List<TabPosition> ->
            // Do nothing
        }
        Box(modifier = Modifier.fillMaxSize()) {
            Column(modifier = Modifier.fillMaxSize()) {
                TitleBorder()
                TabRow(
                    selectedTabIndex = selectedTab,
                    modifier = Modifier.fillMaxWidth().height(36.dp),
                    backgroundColor = Color.Transparent,
                    /* edgePadding = 0.dp, */
                    indicator = emptyIndicator,
                    divider = @Composable { /* None */ },
                ) {
                    tabs.forEachIndexed { index, tab ->
                        val isEnabled = tab.enabled
                        val isSelected = (selectedTab == index)
                        val textColor = when {
                            !isEnabled -> JervisTheme.rulebookRed.copy(alpha = 0.5f)
                            isSelected -> JervisTheme.white
                            else -> JervisTheme.rulebookRed
                        }
                        Tab(
                            modifier = Modifier
                                .background(
                                    if (isSelected) JervisTheme.rulebookRed else Color.Transparent,
                                )
                            ,
                            enabled = isEnabled,
                            selected = isSelected,
                            onClick = { viewModel.updateSelectGameType(index) },
                            text = {
                                Text(
                                    /* modifier = Modifier.padding(horizontal = 8.dp), */
                                    text = tab.tabName.uppercase(),
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                    fontWeight = FontWeight.Bold,
                                    color = textColor,
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
                    selectedTabIndex = pagerState.currentPage,
                    modifier = Modifier.fillMaxWidth().height(36.dp).alpha(if (showSetupTabs) 1f else 0f),
                    backgroundColor = Color.Transparent,
                    indicator = emptyIndicator,
                    divider = @Composable { /* None */ },
                ) {
                    tabs[selectedTab].tabs.forEachIndexed { index, setupTab ->
                        val isSelected = (pagerState.currentPage == index)
                        Tab(
                            modifier = Modifier
                                .background(
                                    if (isSelected) JervisTheme.rulebookRed else Color.Transparent,
                                ),
                            selected = isSelected,
                            onClick = {
                                coroutineScope.launch {
                                    pagerState.animateScrollToPage(index)
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
                                    text = setupTab.name.uppercase(),
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
                TitleBorder(alpha = (if (showSetupTabs) 1f else 0f))
                HorizontalPager(
                    modifier = Modifier.fillMaxWidth().align(Alignment.CenterHorizontally),
                    state = pagerState,
                ) { page ->
                    when (tabs[selectedTab].tabs[page].type) {
                        SetupTabType.LOAD_FILE -> LoadFileComponent(viewModel.loadFileModel)
                        SetupTabType.RULES -> SetupRulesComponent(viewModel.rulesModel)
                        SetupTabType.MAP -> CustomizationSetupComponent(viewModel.customizationsModel)
                        SetupTabType.TIMERS -> TimersSetupComponent(viewModel.timersModel)
                        SetupTabType.INDUCEMENTS -> InducementsSetupComponent(viewModel.inducementsModel)
                        SetupTabType.CUSTOMIZATIONS -> CustomizationSetupComponent(viewModel.customizationsModel)
                    }
                }
            }
        }
    }
}
