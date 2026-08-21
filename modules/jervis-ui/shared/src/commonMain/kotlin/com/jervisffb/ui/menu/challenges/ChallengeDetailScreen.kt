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
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import com.jervisffb.engine.challenge.ChallengeGoal
import com.jervisffb.engine.challenge.ChallengeRule
import com.jervisffb.engine.challenge.ChallengeScore
import com.jervisffb.engine.challenge.ChallengeScoring
import com.jervisffb.shared.generated.resources.Res
import com.jervisffb.shared.generated.resources.jervis_icon_thumps_up_small_selected
import com.jervisffb.ui.game.view.JervisTheme
import com.jervisffb.ui.game.view.utils.JervisButton
import com.jervisffb.ui.game.view.utils.TitleBorder
import com.jervisffb.ui.game.view.utils.paperBackgroundWithLine
import com.jervisffb.ui.game.viewmodel.MenuViewModel
import com.jervisffb.ui.menu.JervisScreen
import com.jervisffb.ui.menu.MenuScreenWithSidebarAndTitle
import com.jervisffb.ui.menu.challenges.data.ChallengeUserState
import com.jervisffb.ui.menu.challenges.data.ChallengeUserState.SolvedState
import com.jervisffb.ui.menu.challenges.data.ScoreboardEntry
import com.jervisffb.ui.menu.components.SmallHeader
import com.jervisffb.ui.utils.withClickableLinks
import org.jetbrains.compose.resources.painterResource

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
    val navigator = LocalNavigator.currentOrThrow
    val details by viewModel.activeChallenge.collectAsState()
    val votes = details.votes

    // Rebuild the preview whenever another challenge is selected and release
    // the running one when the page goes away. A preview is a real game, so
    // leaving it behind would leak both it and its threads.
    val density = LocalDensity.current
    LaunchedEffect(details.data.id, details.data.version) {
        viewModel.loadPreview(menuViewModel, details.data, density)
    }
    DisposableEffect(Unit) {
        onDispose { viewModel.releasePreview() }
    }
    val preview by viewModel.preview.collectAsState()

    MenuScreenWithSidebarAndTitle(
        menuViewModel,
        title = details.data.name,
        icon = null,
        topMenuLeftContent = {

        },
        topMenuRightContent = {
            // Login button
        },
        sidebarContent = {
            val flow =  remember(viewModel) { viewModel.allChallenges }
            val rows by flow.collectAsState(emptyList())
            Column(
                modifier = Modifier
                    .paperBackgroundWithLine(JervisTheme.rulebookBlue)
                ,
            ) {
                Spacer(modifier = Modifier.fillMaxHeight(0.2f))
                Spacer(modifier = Modifier.height(16.dp))
                ChallengeSidebarMenu(
                    modifier = Modifier,
                    entries = rows,
                )
                Spacer(modifier = Modifier.height(32.dp))
                Spacer(modifier = Modifier.fillMaxHeight(0.20f))
            }
        },
    ) {
        Column(
            modifier = Modifier
                .fillMaxHeight()
                .fillMaxWidth(0.7f)
                .verticalScroll(rememberScrollState())
                .padding(vertical = 16.dp)
            ,
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                ChallengeMetadataTags(
                    category = details.data.category,
                    gameVersion = details.data.gameRules.baseVersion,
                    gameType = details.data.gameRules.gameType,
                    tagLayoutDirection = LayoutDirection.Ltr,
                )
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        modifier = Modifier.padding(start = 2.dp),
                        text = "by ${details.data.author.name}",
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
                VoteControl(
                    voted = details.userState.voted,
                    communityScore = votes,
                    onVote = viewModel::setVote,
                    contentColor = creditContentColor,
                )
                FavoriteStar(isFavorite = details.userState.favorite, onToggle = viewModel::toggleFavorite)
                SolvedTrophy(state = details.userState)
                Spacer(modifier = Modifier.width(4.dp))
                JervisButton(
                    text = "Play Challenge",
                    onClick = { viewModel.playChallenge(navigator, menuViewModel) },
                    enabled = details.isPlayable,
                )
            }

            key(preview) {
                ChallengeScreenshot(preview)
            }

            Section("Description", topPadding = 0.dp) {
                val linkText = remember(details.data.description) {
                    details.data.description.withClickableLinks()
                }
                Text(text = linkText, color = JervisTheme.contentTextColor)
            }

            Section("Goal") {
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(text = formatGoal(details.data.goal), color = JervisTheme.contentTextColor)
                }
            }
            Section("Rules") {
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    if (details.data.rules.isEmpty()) {
                        Text(
                            text = "No extra rules",
                            color = JervisTheme.contentTextColor.copy(alpha = 0.6f),
                        )
                    }
                    if (details.data.rules.isNotEmpty()) {
                        Text(text = formatRules(details.data.rules), color = JervisTheme.contentTextColor)
                    }
                }
            }
            Section("Scoring") {
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(text = formatScoring(details.data.scoring), color = JervisTheme.contentTextColor)
                    if (details.userState.isSolved()) {
                        Text(text = formatUserScore(details.userState), color = JervisTheme.contentTextColor)
                    }
                }
            }
            if (details.data.scoring != ChallengeScoring.CompletionOnly) {
                Section("Scoreboard") {
                    Scoreboard(details.scoreboard, "Jervis Probability Score")
                }
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
private fun Scoreboard(entries: List<ScoreboardEntry>, scoreLabel: String) {
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
                text = scoreLabel,
                modifier = Modifier.width(220.dp),
                fontWeight = FontWeight.Bold,
                color = JervisTheme.contentTextColor,
                textAlign = TextAlign.End,
            )
        }
        TitleBorder()
        entries.forEachIndexed { index, entry ->
            Row(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
                Text("${index + 1}", modifier = Modifier.width(32.dp), color = JervisTheme.contentTextColor)
                Text(entry.coach.name, modifier = Modifier.weight(1f), color = JervisTheme.contentTextColor)
                Text(
                    text = entry.getFormattedScore(),
                    modifier = Modifier.width(80.dp),
                    color = JervisTheme.contentTextColor,
                    textAlign = TextAlign.End,
                )
            }
        }
    }
}

private fun formatGoal(goal: ChallengeGoal): String {
    return buildString {
        append("•  ")
        append(goal.description)
        if (goal.modifiers.isNotEmpty()) {
            append("\n")
            val modifiers = goal.modifiers.joinToString(separator = "\n") {
                "    •  ${it.description}"
            }
            append(modifiers)
        }
    }
}

private fun formatRules(rules: List<ChallengeRule>): String {
    return rules.joinToString(separator = "\n") {
        "•  ${it.description}"
    }
}

private fun formatScoring(scoring: ChallengeScoring<*>): String {
    return buildString {
        append("•  ")
        append(scoring.description)
    }
}

private fun formatUserScore(userState: ChallengeUserState): AnnotatedString {
    return buildAnnotatedString {
        append("•  ")
        pushStyle(SpanStyle(fontWeight = FontWeight.Bold))
        append("Completed")
        when (userState.score) {
            is ChallengeScore.CompletionOnly -> {
                append(" ${userState.getFormattedDate()}")
            }
            is ChallengeScore.ProbabilityScore -> {
                append(" ${userState.getFormattedDate()} with ${userState.getFormattedScore()}")
            }
            null -> error("Should not be called with no score")
        }
        if (userState.solved == SolvedState.BEST_IN_CLASS && userState.score !is ChallengeScore.CompletionOnly) {
            append(" (Best in Class)")
        }
        pop()
    }
}
