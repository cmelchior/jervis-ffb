package com.jervisffb.ui.game.view

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.content.MediaType.Companion.Text
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.jervisffb.engine.challenge.ChallengeOutcome
import com.jervisffb.engine.challenge.ChallengeScoring
import com.jervisffb.shared.generated.resources.Res
import com.jervisffb.shared.generated.resources.jervis_icon_trophy
import com.jervisffb.ui.game.dialogs.DialogSize
import com.jervisffb.ui.game.view.utils.JervisButton
import com.jervisffb.ui.game.viewmodel.ChallengeSessionViewModel
import com.jervisffb.ui.menu.GameScreenModel
import com.jervisffb.ui.menu.components.JervisDialog
import com.jervisffb.ui.utils.applyIf
import org.jetbrains.compose.resources.painterResource
import kotlin.time.Duration

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

    val stats = buildList {
        add("Time" to session.elapsed.formatAsClock())
        add("Actions" to session.actionCount.toString())
        when (session.challenge.scoring) {
            ChallengeScoring.CompletionOnly -> { /* Do nothing */ }
            ChallengeScoring.JervisRiskScoring -> {
                add("Jervis Risk Score" to "TODO")
            }
        }
    }

    JervisDialog(
        title = if (outcome == ChallengeOutcome.COMPLETED) "Challenge Solved" else "Challenge Failed",
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
        content = { _, textColor ->
            Text(
                modifier = Modifier.padding(bottom = 8.dp),
                fontWeight = FontWeight.Bold,
                text = session.challenge.name,
                color = textColor,
            )
            Column {
                stats.forEachIndexed { index, (label, value) ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .applyIf(index % 2 == 1) {
                                background(JervisTheme.rulebookPaperMediumDark)
                            }
                            .padding(vertical = 4.dp)
                        ,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(text = label, color = textColor)
                        Spacer(modifier = Modifier.padding(horizontal = 8.dp))
                        Text(text = value, color = textColor)
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
            JervisButton(
                text = "Exit",
                onClick = onExit,
                buttonColor = JervisTheme.rulebookBlue,
                textColor = JervisTheme.buttonTextColor,
            )
        },
    )
}

private fun Duration.formatAsClock(): String {
    val totalSeconds = inWholeSeconds
    val minutes = totalSeconds / 60
    val seconds = totalSeconds % 60
    return "${minutes.toString().padStart(2, '0')}:${seconds.toString().padStart(2, '0')}"
}
