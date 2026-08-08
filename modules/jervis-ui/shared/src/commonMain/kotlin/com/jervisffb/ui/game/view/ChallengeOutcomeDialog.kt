package com.jervisffb.ui.game.view

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredHeightIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.jervisffb.engine.actions.D6Result
import com.jervisffb.engine.challenge.ChallengeOutcome
import com.jervisffb.engine.challenge.ChallengeScore
import com.jervisffb.engine.challenge.ChallengeScoring
import com.jervisffb.engine.statistics.probability.ActionPathEvent
import com.jervisffb.engine.statistics.probability.LogicalActionPathScorer
import com.jervisffb.engine.statistics.probability.PhysicalActionPathScorer
import com.jervisffb.engine.statistics.probability.Probability
import com.jervisffb.engine.statistics.probability.ProbabilityScoreResult
import com.jervisffb.engine.statistics.probability.Surprisal
import com.jervisffb.engine.statistics.probability.SurprisalAdjustment
import com.jervisffb.shared.generated.resources.Res
import com.jervisffb.shared.generated.resources.jervis_icon_trophy
import com.jervisffb.ui.game.dialogs.DialogSize
import com.jervisffb.ui.game.view.utils.JervisButton
import com.jervisffb.ui.game.view.utils.TitleBorder
import com.jervisffb.ui.game.viewmodel.ChallengeSessionViewModel
import com.jervisffb.ui.menu.GameScreenModel
import com.jervisffb.ui.menu.components.CompactSwitch
import com.jervisffb.ui.menu.components.JervisDialog
import com.jervisffb.ui.menu.components.JervisDialogHeader
import com.jervisffb.ui.menu.components.SmallHeader
import com.jervisffb.ui.menu.utils.JervisTooltip
import com.jervisffb.ui.utils.applyIf
import com.jervisffb.ui.utils.toFixed
import com.jervisffb.ui.utils.withPrevious
import org.jetbrains.compose.resources.painterResource
import kotlin.time.Duration

data class DiceRollStat(
    val index: Int, // 1-indexed number
    val target: D6Result, // What dice were rolled
    val type: String, // What kind of dice was rolled
    val rerollSource: String?, // Type of reroll triggering this roll
    val probability: Probability, // [0, 1.0] probability of success
    val surprisal: Surprisal, // Surprisal (bits) for the value or better to be rolled
) {
    // Probability as formatted percentage 0.00% to 100.00%
    val chance = "${(probability.value * 100).toFixed(2)}%"

    val reroll = when (rerollSource != null) {
        true -> rerollSource.removeSuffix(" reroll").removeSuffix(" Reroll").trim()
        else -> "-"
    }

    val bits = surprisal.toFixed(3)
}

/**
 * Shows the result of a finished challenge attempt.
 */
@Composable
fun ChallengeOutcomeDialog(
    session: ChallengeSessionViewModel,
    screenModel: GameScreenModel,
    onTryAgain: () -> Unit,
    onExit: () -> Unit,
) {
    val outcome by session.outcome.collectAsState()
    if (!outcome.isFinished) return

    var showStats by remember { mutableStateOf(false) }
    val summary = buildList {
        add("Time" to session.elapsed.formatAsClock())
        add("Game Actions" to session.actionCount.toString())
        add("Undos" to session.undosPerformed.toString())
        val score = session.score as? ChallengeScore.ProbabilityScore
        when (val result = score?.result) {
            is ProbabilityScoreResult.Scored -> add("Jervis Probability Score" to "${(result.successProbability.value * 100).toFixed(2)}%")
            is ProbabilityScoreResult.Unsupported -> add("Jervis Probability Score" to "Unranked")
            null -> Unit
        }
    }
    val advancedStats = buildList {
        when (session.challenge.scoring) {
            ChallengeScoring.CompletionOnly -> { /* Do nothing */ }
            is ChallengeScoring.ProbabilityScoring -> {
                val score = session.score as? ChallengeScore.ProbabilityScore
                when (val result = score?.result) {
                    is ProbabilityScoreResult.Scored -> {
                        add("Algorithm" to result.algorithmId.value)
                        when (val id = result.algorithmId) {
                            PhysicalActionPathScorer.ALGORITHM_ID -> {
                                add("Base Difficulty" to "${result.baseSurprisal.toFixed(3)} bits")
                                add("Actual extra-roll adjustment" to "${result.actualExtraRollAdjustment.toSigned(3)} bits")
                                add("Hypothetical reroll adjustment" to "${result.hypotheticalRecoveryAdjustment.toSigned(3)} bits")
                                add("Adjusted Difficulty" to "${result.surprisal.toFixed(3)} bits")
                                add("Dice Rolls" to result.eventCount.toString())
                            }
                            LogicalActionPathScorer.ALGORITHM_ID -> {
                                add("Base Difficulty" to "${result.baseSurprisal.toFixed(3)} bits")
                                add("Recovery adjustment" to "${result.rerollAdjustment.toSigned(3)} bits")
                                add("Adjusted Risk" to "${result.surprisal.toFixed(3)} bits")
                                add("Dice Rolls" to result.eventCount.toString())
                            }
                            else -> {
                                add("Base Difficulty" to "${result.baseSurprisal.toFixed(3)} bits")
                                add("Recovery adjustment" to "${result.rerollAdjustment.toSigned(3)} bits")
                                add("Adjusted Difficulty" to "${result.surprisal.toFixed(3)} bits")
                                add("Events" to result.eventCount.toString())
                            }
                        }
                    }
                    is ProbabilityScoreResult.Unsupported -> {
                        add("Reason" to result.reasons.joinToString())
                        add("Events" to result.events.size.toString())
                    }
                    null -> Unit
                }
            }
        }
    }
    val diceRolls = (session.score as? ChallengeScore.ProbabilityScore)
        ?.result
        ?.events
        ?.filterIsInstance<ActionPathEvent.PhysicalD6>()
        ?.withPrevious()
        ?.mapIndexed { index, (lastEvent, event) ->
            val probability = event.observedOutcome.probability
            val rerollSource = lastEvent?.actualRecovery?.description.orEmpty()
            DiceRollStat(
                index = index + 1,
                target = event.selectedValue,
                type = event.rollType.description,
                probability = event.observedOutcome.probability,
                rerollSource = rerollSource,
                surprisal = probability.toSurprisal()
            )
        }.orEmpty().toList()

    val dialogColor = JervisTheme.rulebookRed
    JervisDialog(
        title = {
            val completed = (outcome == ChallengeOutcome.COMPLETED)
            val title = when (completed) {
                true -> "Solved - ${session.challenge.name}"
                false ->"Failed - ${session.challenge.name}"
            }
            val tooltip = when (showStats) {
                true -> "Hide Stats Calculation"
                false -> "Show Stats Calculation"
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    modifier = Modifier
                        .applyIf(completed) {
                            padding(bottom = 2.dp)
                        }
                        .applyIf(!completed) {
                            padding(bottom = 2.dp, top = 4.dp)
                        }
                        .weight(1f),

                    text = title.uppercase(),
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    color = dialogColor,
                )
                if (completed) {
                    JervisTooltip(
                        tooltip = tooltip
                    ) {
                        CompactSwitch(
                            label = "",
                            checked = showStats,
                            onCheckedChange = { showStats = it },
                        )
                    }
                }
            }
        },
        icon = {
            Image(
                modifier = Modifier.fillMaxWidth().padding(top = 24.dp, start = 20.dp, end = 20.dp),
                painter = painterResource(Res.drawable.jervis_icon_trophy),
                contentDescription = "Trophy Icon",
                contentScale = ContentScale.FillWidth,
                colorFilter = ColorFilter.tint(JervisTheme.white),
            )
        },
        width = DialogSize.MEDIUM,
        draggable = true,
        backgroundScrim = false,
        centerOnPitch = screenModel,
        dialogColor = dialogColor,
        content = { _, textColor ->
            Column(
                modifier = Modifier.requiredHeightIn(min = 200.dp, max = 500.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 360.dp)
                        .verticalScroll(rememberScrollState()),
                ) {
                    StatisticRows(summary, textColor)
                    if (showStats) {
                        SmallHeader("Probability Evaluation", topPadding = 16.dp)
                        StatisticRows(advancedStats, textColor)
                        if (diceRolls.isNotEmpty()) {
                            SmallHeader("Dice Rolls", topPadding = 16.dp)
                            DiceStatisticRows(diceRolls, textColor)
                        }
                    }
                }
            }
        },
        buttons = {
            JervisButton(
                text = "Try Again",
                onClick = onTryAgain,
                buttonColor = JervisTheme.rulebookBlue,
                textColor = JervisTheme.buttonTextColor,
            )
            Spacer(modifier = Modifier.weight(1f))
            Spacer(modifier = Modifier.padding(horizontal = 4.dp))
            JervisButton(
                text = "Exit",
                onClick = onExit,
                buttonColor = JervisTheme.rulebookBlue,
                textColor = JervisTheme.buttonTextColor,
            )
        },
    )
}

@Composable
private fun OutcomeSectionHeader(title: String, topPadding: Dp = 24.dp, bottomPadding: Dp = 8.dp) {
    Spacer(modifier = Modifier.height(topPadding))
    JervisDialogHeader(title, JervisTheme.rulebookRed)
    TitleBorder(JervisTheme.rulebookRed)
    Spacer(modifier = Modifier.height(bottomPadding))
}

@Composable
private fun DiceStatisticRows(
    stats: List<DiceRollStat>,
    textColor: Color,
) {
    val hasReroll = remember { stats.any { it.reroll.isNotBlank() } }
    Column {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp),
        ) {
            Text(modifier = Modifier.weight(1.5f), text = "Type", color = textColor, fontWeight = FontWeight.Bold)
            Text(modifier = Modifier.weight(1f), text = "Roll", color = textColor, fontWeight = FontWeight.Bold)
            if (hasReroll) {
                Text(modifier = Modifier.weight(1f), text = "Reroll", color = textColor, fontWeight = FontWeight.Bold)
            }
            Text(modifier = Modifier.weight(0.5f + (if (hasReroll) 0.0f else 1f)), text = "Bits", color = textColor, textAlign = TextAlign.End, fontWeight = FontWeight.Bold)
        }
        stats.forEachIndexed { index, stat ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .applyIf(index % 2 == 0) {
                        background(JervisTheme.rulebookPaperMediumDark)
                    }
                    .padding(vertical = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Text(modifier = Modifier.weight(1.5f), text = stat.type, color = textColor, textAlign = TextAlign.Start)
                Text(modifier = Modifier.weight(1f), text = "  ${stat.target.value}", color = textColor, textAlign = TextAlign.Start)
                if (hasReroll) {
                    Text(modifier = Modifier.weight(1f), text = stat.reroll, color = textColor, textAlign = TextAlign.Start)
                }
                Text(modifier = Modifier.weight(0.5f + (if (hasReroll) 0.0f else 1f)), text = stat.bits, color = textColor, textAlign = TextAlign.End)
            }
        }
    }
}

@Composable
private fun StatisticRows(
    stats: List<Pair<String, String>>,
    textColor: Color,
) {
    Column {
        stats.forEachIndexed { index, (label, value) ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .applyIf(index % 2 == 1) {
                        background(JervisTheme.rulebookPaperMediumDark)
                    }
                    .padding(vertical = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Text(text = label, color = textColor)
                Spacer(modifier = Modifier.padding(horizontal = 8.dp))
                Text(text = value, color = textColor)
            }
        }
    }
}

private fun SurprisalAdjustment.toSigned(decimals: Int): String = value.toSigned(decimals)

private fun Double.toSigned(decimals: Int): String = when {
    this > 0.0 -> "+${toFixed(decimals)}"
    else -> toFixed(decimals)
}

private fun Duration.formatAsClock(): String {
    val totalSeconds = inWholeSeconds
    val minutes = totalSeconds / 60
    val seconds = totalSeconds % 60
    return "${minutes.toString().padStart(2, '0')}:${seconds.toString().padStart(2, '0')}"
}
