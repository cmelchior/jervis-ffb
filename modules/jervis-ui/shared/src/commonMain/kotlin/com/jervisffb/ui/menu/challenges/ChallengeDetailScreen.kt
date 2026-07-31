package com.jervisffb.ui.menu.challenges

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import cafe.adriel.voyager.core.model.screenModelScope
import cafe.adriel.voyager.core.screen.Screen
import com.jervisffb.shared.generated.resources.Res
import com.jervisffb.shared.generated.resources.jervis_icon_thumps_up_small_selected
import com.jervisffb.ui.game.view.JervisTheme
import com.jervisffb.ui.game.view.SidebarEntryState
import com.jervisffb.ui.game.view.utils.JervisButton
import com.jervisffb.ui.game.view.utils.TitleBorder
import com.jervisffb.ui.game.view.utils.paperBackgroundWithLine
import com.jervisffb.ui.game.viewmodel.MenuViewModel
import com.jervisffb.ui.menu.JervisScreen
import com.jervisffb.ui.menu.JervisScreenModel
import com.jervisffb.ui.menu.MenuScreenWithSidebarAndTitle
import com.jervisffb.ui.menu.challenges.ChallengeStore.userState
import com.jervisffb.ui.menu.components.SmallHeader
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import org.jetbrains.compose.resources.painterResource
import kotlin.math.roundToInt
import kotlin.text.get

class ChallengeDetailScreenModel(
    challenge: ChallengeRow,
    allRows: List<ChallengeRow>
) : JervisScreenModel {

    val currentChallengeData = mutableStateOf(challenge)
    val allChallenges = mutableStateListOf<ChallengeSidebarEntry>()
    val currentIndex: StateFlow<Int>
        field = MutableStateFlow(0)

    init {
        ChallengeStore.load(listOf(challenge.challenge))
        allRows
            .mapIndexed { index, challengeData ->
                val activeChallenge = (challengeData == challenge)
                if (activeChallenge) {
                    currentIndex.value = index
                }
                ChallengeSidebarEntry(
                    // name = "${index + 1}. ${challengeData.challenge.name}${if (challengeData.userState.isSolved()) " (✓)" else ""} ",
                    name = "${if (challengeData.userState.isSolved()) "(✓) " else ""}${challengeData.challenge.name}",
                    state = when (activeChallenge) {
                        true -> SidebarEntryState.ACTIVE
                        false -> SidebarEntryState.DONE_AVAILABLE
                    },
                    alternativeBackground = false, // !activeChallenge && (index % 2 == 1),
                    challenge = challengeData,
                    onClick = { setActiveChallenge(index) }
                )
            }
            .let { allChallenges.addAll(it) }
    }

    val userState: StateFlow<ChallengeUserState> =
        combine(currentIndex, ChallengeStore.state) { index, state ->
            val id = allChallenges[index].challenge.challenge.id
            state[id] ?: ChallengeUserState()
        }
            .stateIn(screenModelScope, SharingStarted.Eagerly, ChallengeStore.userState(challenge.challenge.id))

    fun toggleFavorite() {
        val index = currentIndex.value
        allChallenges[index] = allChallenges[index].let {
            val challengeRow = it.challenge
            val updatedChallenge = challengeRow.copy(
                userState = challengeRow.userState.copy(
                    favorite = !challengeRow.userState.favorite
                )
            )
            it.copy(challenge = updatedChallenge)
        }
        // setActiveChallenge(index)
        currentChallengeData.value = allChallenges[index].challenge
        ChallengeStore.toggleFavorite(currentChallengeData.value.challenge.id)
    }

    fun setVote(voted: Boolean) {
        val index = currentIndex.value
        allChallenges[index] = allChallenges[index].let {
            val updatedChallenge = it.challenge.copy(
                userState = it.challenge.userState.copy(
                    voted = voted
                )
            )
            it.copy(challenge = updatedChallenge)
        }
        // setActiveChallenge(index)
        currentChallengeData.value = allChallenges[index].challenge
        ChallengeStore.setVote(currentChallengeData.value.challenge.id, voted)
    }

    fun setActiveChallenge(index: Int) {
        val oldIndex = currentIndex.value
        allChallenges[oldIndex] = allChallenges[oldIndex].copy(state = SidebarEntryState.DONE_AVAILABLE)
        allChallenges[index] = allChallenges[index].copy(state = SidebarEntryState.ACTIVE)
        currentIndex.value = index
        currentChallengeData.value = allChallenges[index].challenge
    }
}

class ChallengeDetailScreen(
    private val menuViewModel: MenuViewModel,
    private val viewModel: ChallengeDetailScreenModel,
) : Screen {
    @Composable
    override fun Content() {
        JervisScreen(menuViewModel) {
            ChallengeDetailContent(menuViewModel, viewModel)
        }
    }
}

@Composable
private fun ChallengeDetailContent(
    menuViewModel: MenuViewModel,
    viewModel: ChallengeDetailScreenModel,
    creditFontSize: TextUnit = 14.sp,
    creditContentColor: Color = JervisTheme.contentTextColor.copy(alpha = 0.7f)
) {
    val challengeRow by viewModel.currentChallengeData
    val userState by viewModel.userState.collectAsState()
    val challenge = challengeRow.challenge
    val votes = challenge.communityScore + if (userState.voted) 1 else 0

    MenuScreenWithSidebarAndTitle(
        menuViewModel,
        title = challenge.name,
        icon = null,
        topMenuLeftContent = {

        },
        topMenuRightContent = {
            // Login button
        },
        sidebarContent = {
            val currentPage by viewModel.currentIndex.collectAsState()
            Column(
                modifier = Modifier
                    .paperBackgroundWithLine(JervisTheme.rulebookBlue)
                ,
            ) {
                Spacer(modifier = Modifier.fillMaxHeight(0.2f))
                Spacer(modifier = Modifier.height(16.dp))
                ChallengeSidebarMenu(
                    modifier = Modifier,
                    entries = viewModel.allChallenges,
                )
                Spacer(modifier = Modifier.height(32.dp))
                Spacer(modifier = Modifier.fillMaxHeight(0.20f))
            }
        },
    ) {
        Column(
            modifier = Modifier
                .fillMaxHeight()
                .fillMaxWidth(0.6f)
                .verticalScroll(rememberScrollState())
                .padding(vertical = 16.dp)
            ,
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                CategoryChip(challenge.category)
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        modifier = Modifier.padding(start = 2.dp),
                        text = "by ${challenge.author}",
                        fontSize = creditFontSize,
                        lineHeight = 1.em,
                        color = creditContentColor,
                    )
                    if (votes > 0) {
                        Text(
                            text = " • ",
                            fontSize = creditFontSize,
                            lineHeight = 1.em,
                            color = creditContentColor,
                        )
                        Text(
                            text = votes.toString(),
                            fontSize = creditFontSize,
                            lineHeight = 1.em,
                            color = creditContentColor,
                        )
                        Image(
                            modifier = Modifier.size(14.dp).padding(start = 2.dp),
                            painter = painterResource(Res.drawable.jervis_icon_thumps_up_small_selected),
                            contentDescription = "+$votes Votes",
                            colorFilter = ColorFilter.tint(creditContentColor)
                        )
                    }
                }
                Spacer(modifier = Modifier.weight(1f))
                RatingControl(
                    voted = userState.voted,
                    communityScore = votes,
                    onVote = viewModel::setVote,
                    contentColor = creditContentColor,
                )
                FavoriteStar(isFavorite = userState.favorite, onToggle = viewModel::toggleFavorite)
                SolvedTrophy(state = userState)
                Spacer(modifier = Modifier.width(4.dp))
                JervisButton(
                    text = "Play Challenge",
                    onClick = { /* Not supported yet - future work */ },
                    enabled = false,
                )
            }

            ChallengeScreenshot()

            Section("Description", topPadding = 0.dp) {
                Text(text = challenge.description, color = JervisTheme.contentTextColor)
            }

            Section("Goal") {
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    challenge.rules.forEach { rule ->
                        Text(text = "•  $rule", color = JervisTheme.contentTextColor)
                    }
                }
            }
            Section("Rules") {
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    challenge.rules.forEach { rule ->
                        Text(text = "•  $rule", color = JervisTheme.contentTextColor)
                    }
                }
            }

            Section("Scoreboard") {
                Scoreboard(challenge.scoreboard)
            }

            Spacer(modifier = Modifier.height(8.dp))
        }
    }
}

@Composable
private fun Section(
    title: String,
    topPadding: Dp = 16.dp,
    content: @Composable () -> Unit,
) {
    Column(
        modifier = Modifier.padding(top = topPadding),
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        SmallHeader(title)
        content()
    }
}

@Composable
private fun Scoreboard(entries: List<ScoreboardEntry>) {
    if (entries.isEmpty()) {
        Text(
            text = "No solutions submitted yet.",
            color = JervisTheme.contentTextColor.copy(alpha = 0.6f),
        )
        return
    }
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
            Text("#", modifier = Modifier.width(32.dp), fontWeight = FontWeight.Bold, color = JervisTheme.contentTextColor)
            Text("Coach", modifier = Modifier.weight(1f), fontWeight = FontWeight.Bold, color = JervisTheme.contentTextColor)
            Text(
                text = "Success",
                modifier = Modifier.width(80.dp),
                fontWeight = FontWeight.Bold,
                color = JervisTheme.contentTextColor,
                textAlign = TextAlign.End,
            )
        }
        TitleBorder()
        entries.sortedByDescending { it.successChance }.forEachIndexed { index, entry ->
            Row(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
                Text("${index + 1}", modifier = Modifier.width(32.dp), color = JervisTheme.contentTextColor)
                Text(entry.coachName, modifier = Modifier.weight(1f), color = JervisTheme.contentTextColor)
                Text(
                    text = "${(entry.successChance * 100).roundToInt()}%",
                    modifier = Modifier.width(80.dp),
                    color = JervisTheme.contentTextColor,
                    textAlign = TextAlign.End,
                )
            }
        }
    }
}
