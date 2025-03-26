package com.jervisffb.ui.menu.components.coach

import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.material.OutlinedTextField
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.isSpecified
import com.jervisffb.ui.game.view.JervisTheme
import com.jervisffb.ui.game.view.utils.JervisButton
import com.jervisffb.ui.menu.hotseat.PlayerTypeSelector
import com.jervisffb.ui.menu.p2p.host.SettingsCard

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun CoachSetupComponent(
    viewModel: CoachSetupComponentModel,
    headerWidth: Dp,
    inputWidth: Dp = Dp.Unspecified,
) {
    val coachName by viewModel.coachName.collectAsState("")
    val playerType by viewModel.playerType.collectAsState()
    val aiPlayers by viewModel.aiPlayers.collectAsState()
    val selectedAiPlayer by viewModel.selectedAiPlayer.collectAsState()
    val playerTypeOptions = listOf(
        "Human" to CoachType.HUMAN,
        "Computer" to CoachType.COMPUTER,
    )

    SettingsCard("Coach", headerWidth) {
        OutlinedTextField(
            modifier = Modifier.let { if (inputWidth.isSpecified) it.width(inputWidth) else it.fillMaxWidth() },
            value = coachName,
            onValueChange = { viewModel.updateCoachName(it) },
            label = { Text("Coach name") }
        )
        PlayerTypeSelector(playerTypeOptions, playerType, onChoice = { type: CoachType -> viewModel.updatePlayerType(type)})
    }
    if (playerType == CoachType.COMPUTER) {
        Spacer(modifier = Modifier.height(32.dp))
        SettingsCard("Select AI", headerWidth) {
            aiPlayers.forEach { ai ->
                JervisButton(
                    text = ai.name,
                    onClick = {
                        viewModel.updateSelectedAiPlayer(ai)
                    },
                    buttonColor = if (selectedAiPlayer == ai) JervisTheme.rulebookRed else JervisTheme.rulebookBlue)
            }
        }
    }
}
