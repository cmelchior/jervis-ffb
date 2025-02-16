package com.jervisffb.ui.screen.p2p.host

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.OutlinedTextField
import androidx.compose.material.ScrollableTabRow
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.jervisffb.ui.view.JervisTheme
import com.jervisffb.ui.view.utils.JervisButton
import com.jervisffb.ui.view.utils.TitleBorder
import kotlinx.coroutines.launch

@Composable
fun SetupGamePage(screenModel: P2PHostScreenModel, modifier: Modifier, viewModel: P2PHostScreenModel) {
    val gameName by viewModel.gameName.collectAsState("")
    val gamePort by viewModel.port.collectAsState( null)
    val canCreateGame: Boolean by screenModel.canCreateGame.collectAsState(false)

    Column(
        modifier = modifier.fillMaxSize(),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
            ,
            verticalAlignment = Alignment.Top
        ) {
            SettingsCard("Details", 300.dp) {
                OutlinedTextField(
                    value = gameName,
                    onValueChange = { viewModel.setGameName(it) },
                    label = { Text("Name") }
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = gamePort?.toString() ?: "",
                    onValueChange = { viewModel.setPort(it) },
                    label = { Text("Port") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    keyboardActions = KeyboardActions {

                    }
                )
//                Row(
//                    verticalAlignment = Alignment.CenterVertically,
//                ) {
//                    Text(
//                        text = "IP: 85.191.6.149"
//                    )
//                }
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.fillMaxSize()) {
                val pagerStateTop = rememberPagerState(0) { 5 }
                val pagerStateBottom = rememberPagerState(0) { 4 }
                val tabs = listOf("Standard", "BB7", "From File") // listOf("Standard", "BB7", "Dungeon Bowl", "Gutter Bowl", "From File")
                val tabs2 = listOf("Modifications", "Timers", "Inducements", "Rules")
                val coroutineScope = rememberCoroutineScope()

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
                            state = pagerStateBottom,
                        ) { page ->
                            when (page) {
                                0 -> StandardGameSetup(screenModel)
                                else -> StandardGameSetup(screenModel)
                            }
                        }
                    }
                }
            }
        }
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
            JervisButton(text = "NEXT", onClick = { viewModel.gameSetupDone() })
        }
    }
}
