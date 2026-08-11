package com.jervisffb.ui.menu.challenges

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import com.jervisffb.engine.challenge.ChallengeCategory
import com.jervisffb.shared.generated.resources.Res
import com.jervisffb.shared.generated.resources.jervis_frontpage_mummy
import com.jervisffb.shared.generated.resources.jervis_icon_thumps_up_small_selected
import com.jervisffb.ui.game.view.JervisTheme
import com.jervisffb.ui.game.view.utils.TitleBorder
import com.jervisffb.ui.game.view.utils.paperBackground
import com.jervisffb.ui.game.view.utils.paperBackgroundWithLine
import com.jervisffb.ui.game.viewmodel.MenuViewModel
import com.jervisffb.ui.menu.JervisScreen
import com.jervisffb.ui.menu.MenuScreenWithSidebarAndTitle
import com.jervisffb.ui.menu.challenges.data.ChallengeRow
import com.jervisffb.ui.menu.components.CompactSwitch
import com.jervisffb.ui.menu.fumbbl.MenuSidebarButton
import com.jervisffb.ui.utils.applyIf
import org.jetbrains.compose.resources.painterResource
import kotlin.time.Duration.Companion.milliseconds

class ChallengesListScreen(
    private val menuViewModel: MenuViewModel,
    private val viewModel: ChallengesListScreenModel,
) : Screen {
    @Composable
    override fun Content() {
        JervisScreen(menuViewModel) {
            ChallengesListContent(menuViewModel, viewModel)
        }
    }
}

@Composable
private fun ChallengesListContent(menuViewModel: MenuViewModel, viewModel: ChallengesListScreenModel) {
    val navigator = LocalNavigator.currentOrThrow
    val rows by viewModel.visibleChallenges.collectAsState()
    val isInitializing by viewModel.isInitializing.collectAsState()
    val activeCategories by viewModel.activeCategories.collectAsState()
    val hideSolved by viewModel.hideSolved.collectAsState()
    val showOnlyFavorites by viewModel.showOnlyFavorites.collectAsState()
    var showContent by remember { mutableStateOf(false) }

    LaunchedEffect(isInitializing) {
        if (isInitializing) {
            showContent = false
        }
    }

    MenuScreenWithSidebarAndTitle(
        menuViewModel,
        title = "Challenges (Prototype)",
        icon = Res.drawable.jervis_frontpage_mummy,
        topMenuLeftContent = {

        },
        topMenuRightContent = {
            // Login button
        },
        sidebarContent = {
            Column(
                modifier = Modifier
                    .paperBackgroundWithLine(JervisTheme.rulebookBlue)
                    .padding(start = 16.dp, top = 0.dp, end = 16.dp, bottom = 8.dp)
                ,
            ) {
                Spacer(modifier = Modifier.fillMaxHeight(0.2f))
                Spacer(modifier = Modifier.height(16.dp))
                // SidebarBoxHeader("Links", color = JervisTheme.rulebookOrange)
                // Spacer(modifier = Modifier.height(8.dp))
                Column(
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    MenuSidebarButton("List", selected = true, onClick = { /* Do nothing */ })
                    MenuSidebarButton("Create New", onClick = {
                        menuViewModel.openNotImplementedYet("Create New Challenge", true)
                    })
                    MenuSidebarButton("My Challenges", onClick = {
                        menuViewModel.openNotImplementedYet("My Challenges", true)
                    })
                }
                Spacer(modifier = Modifier.fillMaxHeight(0.20f))
            }
        },
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth(0.85f)
                .fillMaxHeight()
        ) {
            FlowRow(
                modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                ChallengeCategory.entries.forEach { category ->
                    CompactSwitch(category.label, category in activeCategories) { viewModel.toggleCategory(category) }
                }
                CompactSwitch("Hide solved", hideSolved) { viewModel.setHideSolved(it) }
                CompactSwitch("Only Favorites", showOnlyFavorites) { viewModel.setShowOnlyFavorites(it) }
            }
            TitleBorder()
            Box(modifier = Modifier.padding(top = 16.dp).fillMaxWidth()) {
                DelayedChallengeLoadingIndicator(
                    isLoading = isInitializing,
                    modifier = Modifier.align(Alignment.Center),
                    minimumShowTime = 500.milliseconds,
                    onLoadingFinished = { showContent = true },
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        val loadColor = JervisTheme.contentTextColor.copy(alpha = 0.8f)
                        CircularProgressIndicator(color = loadColor)
                        Text(
                            text = "Fetching Challenges...",
                            color = loadColor,
                        )
                    }
                }
                if (showContent && rows.isEmpty()) {
                    Text(
                        text = "No challenges match the selected filters.",
                        modifier = Modifier.align(Alignment.Center),
                        color = JervisTheme.contentTextColor.copy(alpha = 0.6f),
                    )
                } else if (showContent) {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(vertical = 12.dp),
                        verticalArrangement = Arrangement.spacedBy(0.dp),
                    ) {
                        itemsIndexed(
                            items = rows,
                            key = { index: Int, row: ChallengeRow -> row.data.id.value },
                            contentType = { index: Int, row: ChallengeRow -> null },
                        ) { index: Int, row: ChallengeRow ->
                            ChallengeListRow(
                                row = row,
                                alternateRow = (index % 2 == 1),
                                onOpen = { viewModel.openChallenge(navigator, row) },
                                onToggleFavorite = { viewModel.toggleFavorite(row.data.id, !row.userState.favorite) },
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ChallengeListRow(
    row: ChallengeRow,
    alternateRow: Boolean,
    onOpen: () -> Unit,
    onToggleFavorite: () -> Unit,
    headerTextSize: TextUnit = 16.sp,
    subTextColor: Color = JervisTheme.contentTextColor.copy(alpha = 0.6f),
    subTextSize: TextUnit = 12.sp
) {
    val votes = row.votes
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .applyIf(alternateRow) {
                paperBackground(JervisTheme.rulebookPaperMediumDark.copy(alpha = 0.1f))
            }
            .clickable { onOpen() }
            .padding(horizontal = 12.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            FavoriteStar(isFavorite = row.userState.favorite, onToggle = onToggleFavorite)
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
            }
        }
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = row.data.name,
                fontWeight = FontWeight.Bold,
                fontSize = headerTextSize,
                color = JervisTheme.contentTextColor,
            )
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "by ${row.data.author.name}",
                    fontSize = subTextSize,
                    color = subTextColor,
                )
                if (votes > 0) {
                    Text(
                        text = " • ",
                        fontSize = subTextSize,
                        color = subTextColor,
                    )
                    Text(
                        text = votes.toString(),
                        fontSize = subTextSize,
                        color = subTextColor,
                    )
                    Image(
                        modifier = Modifier.size(14.dp).padding(start = 2.dp),
                        painter = painterResource(Res.drawable.jervis_icon_thumps_up_small_selected),
                        contentDescription = "+$votes Votes",
                        colorFilter = ColorFilter.tint(subTextColor)
                    )
                }
                // TODO Should we show categories here instead?
                //    Text(
                //        text = " • ",
                //        fontSize = subTextSize,
                //        color = subTextColor,
                //    )
                //    val background = categoryColor(row.challenge.category)
                //    Text(
                //        modifier = Modifier
                //            .background(background, RoundedCornerShape(2.dp))
                //            .padding(horizontal = 2.dp, vertical = 1.dp),
                //        text = row.challenge.category.label.uppercase(),
                //        fontSize = 10.sp,
                //        lineHeight = 1.em,
                //        fontWeight = FontWeight.Medium,
                //        color = categoryTextColor(row.challenge.category),
                //        style = LocalTextStyle.current.copy(
                //            lineHeightStyle = LineHeightStyle(
                //                alignment = LineHeightStyle.Alignment.Center,
                //                trim = LineHeightStyle.Trim.Both,
                //            ),
                //        ),
                //    )
            }
        }
        CategoryChip(row.data.category)
        SolvedTrophy(state = row.userState)
    }
}
