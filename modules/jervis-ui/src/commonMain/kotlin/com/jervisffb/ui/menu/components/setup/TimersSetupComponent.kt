package com.jervisffb.ui.menu.components.setup

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Divider
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.jervisffb.ui.game.view.JervisTheme
import com.jervisffb.ui.menu.components.JervisDropDownMenu
import com.jervisffb.ui.menu.components.SimpleSwitch
import com.jervisffb.ui.menu.components.SmallHeader

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun TimersSetupComponent(viewModel: SetupTimersComponentModel) {
    val timersEnabled by viewModel.timersEnabled.collectAsState()
    val selectedPreset by viewModel.selectedPreset.collectAsState()

    val normalGameTimeSetting by viewModel.normalGameLimit.collectAsState()
    val normalGameBuffer by viewModel.normalGameBuffer.collectAsState()
    val overtimeExtraLimit by viewModel.overtimeExtraLimit.collectAsState()
    val overtimeExtraBuffer by viewModel.overtimeExtraBuffer.collectAsState()

    val selectedOutOfTimeEntry by viewModel.outOfTimeLimit.collectAsState()
    val selectedGameLimitReachedEntry by viewModel.gameLimitReached.collectAsState()

    val setupUseBuffer by viewModel.setupUseBuffer.collectAsState()
    val setupFreeTime by viewModel.setupActionTime.collectAsState()
    val setupMaxTime by viewModel.setupFreeTime.collectAsState()

    val teamTurnUseBuffer by viewModel.teamTurnUseBuffer.collectAsState()
    val teamTurnFreeTime by viewModel.teamTurnActionTime.collectAsState()
    val teamTurnMaxTime by viewModel.teamTurnFreeTime.collectAsState()

    val responseUseBuffer by viewModel.responseUseBuffer.collectAsState()
    val responseFreeTime by viewModel.responseActionTime.collectAsState()
    val responseMaxTime by viewModel.responseFreeTime.collectAsState()

    val inputFieldModifier = Modifier.padding(bottom = 8.dp).fillMaxWidth()

    Box(
        modifier = Modifier.fillMaxSize().padding(top = 16.dp),
        contentAlignment = Alignment.TopCenter
    ) {
        Column(
            modifier = Modifier.width(750.dp).verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Start
            ) {
                Box(modifier = Modifier.weight(1f)) {
                    SimpleSwitch("Timers Enabled", timersEnabled) {
                        viewModel.updateTimersEnabled(it)
                    }
                }
                Spacer(modifier = Modifier.width(16.dp))
                Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.CenterEnd) {
                    JervisDropDownMenu(
                        title = "Clock Presets",
                        enabled = timersEnabled,
                        selectedEntry = selectedPreset,
                        entries = presets
                    ) {
                        viewModel.updatePreset(it)
                    }
                }
            }
            Row(
                modifier = Modifier.padding(top = 16.dp),
            ) {
                Column(
                    modifier = Modifier.weight(1f).wrapContentSize()
                ) {
                    SmallHeader("Totals", bottomPadding = smallHeaderBottomPadding)
                    OutlinedTextField(
                        modifier = inputFieldModifier,
                        value = normalGameTimeSetting.value,
                        onValueChange = { viewModel.updateNormalGameTimeLimit(it) },
                        enabled = timersEnabled,
                        label = { Text(normalGameTimeSetting.label) },
                    )
                    OutlinedTextField(
                        modifier = inputFieldModifier,
                        value = normalGameBuffer.value,
                        onValueChange = { viewModel.updateNormalGameBuffer(it) },
                        enabled = timersEnabled,
                        label = { Text(normalGameBuffer.label) },
                    )
                    HorizontalDivider(
                        modifier = Modifier.padding(top = 16.dp, bottom = 16.dp),
                        thickness = 1.dp,
                        color = JervisTheme.rulebookPaperDark.copy(0.3f),
                    )
                    OutlinedTextField(
                        modifier = inputFieldModifier,
                        value = overtimeExtraLimit.value,
                        onValueChange = { viewModel.updateOvertimeExtraLimit(it) },
                        enabled = timersEnabled,
                        label = { Text(overtimeExtraLimit.label) },
                    )
                    OutlinedTextField(
                        modifier = inputFieldModifier,
                        value = overtimeExtraBuffer.value,
                        onValueChange = { viewModel.updateOvertimeExtraBuffer(it) },
                        enabled = timersEnabled,
                        label = { Text(overtimeExtraBuffer.label) },
                    )
                    SmallHeader("Limit Behavior", topPadding = smallHeaderTopPadding, bottomPadding = smallHeaderBottomPadding)
                    JervisDropDownMenu("Out-of-time", enabled = timersEnabled, selectedEntry = selectedOutOfTimeEntry, entries = outOfTimeEntries) {
                        viewModel.updateOutOfTimeBehaviour(it)
                    }
                    JervisDropDownMenu("Game Limit Reached", enabled = timersEnabled, selectedEntry = selectedGameLimitReachedEntry, entries = gameLimitEntries) {
                        viewModel.updateGameLimitReachedBehaviour(it)
                    }
                }
                Spacer(modifier = Modifier.width(16.dp))
                Column(
                    modifier = Modifier.weight(1f).wrapContentSize()
                ) {
                    SmallHeader("Setup", bottomPadding = smallHeaderBottomPadding)
                    SimpleSwitch("Use Game Buffer", isSelected = setupUseBuffer, isEnabled = timersEnabled) {
                        viewModel.updateSetupUseBuffer(it)
                    }
                    OutlinedTextField(
                        modifier = inputFieldModifier,
                        value = setupFreeTime.value,
                        onValueChange = { viewModel.updateSetupActionTime(it) },
                        enabled = timersEnabled,
                        label = { Text(setupFreeTime.label) },
                    )
                    if (setupUseBuffer) {
                        OutlinedTextField(
                            modifier = inputFieldModifier,
                            value = setupMaxTime.value,
                            onValueChange = { viewModel.updateSetupFreeTime(it) },
                            enabled = timersEnabled && setupUseBuffer,
                            label = { Text(setupMaxTime.label) },
                        )
                    }
                    SmallHeader("Team Turn", topPadding = smallHeaderTopPadding, bottomPadding = 8.dp)
                    SimpleSwitch("Use Game Buffer", teamTurnUseBuffer, isEnabled = timersEnabled) {
                        viewModel.updateTeamTurnUseBuffer(it)
                    }
                    OutlinedTextField(
                        modifier = inputFieldModifier,
                        value = teamTurnFreeTime.value,
                        onValueChange = { viewModel.updateTeamTurnActionTime(it) },
                        enabled = timersEnabled,
                        label = { Text(teamTurnFreeTime.label) },
                    )
                    if (teamTurnUseBuffer) {
                        OutlinedTextField(
                            modifier = inputFieldModifier,
                            value = teamTurnMaxTime.value,
                            onValueChange = { viewModel.updateTeamTurnFreeTime(it) },
                            enabled = timersEnabled && teamTurnUseBuffer,
                            label = { Text(teamTurnMaxTime.label) },
                        )
                    }
                    SmallHeader("Out-of-turn Response", topPadding = smallHeaderTopPadding, bottomPadding = 8.dp)
                    SimpleSwitch("Use Game Buffer", responseUseBuffer, isEnabled = timersEnabled) {
                        viewModel.updateResponseUseBuffer(it)
                    }
                    OutlinedTextField(
                        modifier = inputFieldModifier,
                        value = responseFreeTime.value,
                        onValueChange = { viewModel.updateResponseActionTime(it) },
                        enabled = timersEnabled,
                        label = { Text(responseFreeTime.label) },
                    )
                    if (responseUseBuffer) {
                        OutlinedTextField(
                            modifier = inputFieldModifier,
                            value = responseMaxTime.value,
                            onValueChange = { viewModel.updateResponseFreeTime(it) },
                            enabled = timersEnabled && responseUseBuffer,
                            label = { Text(responseMaxTime.label) },
                        )
                    }
                }
            }
        }
    }
}
