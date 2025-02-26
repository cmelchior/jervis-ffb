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
import androidx.compose.material.Divider
import androidx.compose.material.OutlinedTextField
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.jervisffb.ui.game.view.JervisTheme
import com.jervisffb.ui.menu.components.JervisDropDownMenu
import com.jervisffb.ui.menu.components.SimpleSwitch
import com.jervisffb.ui.menu.p2p.host.BoxHeader

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun SetupTimersComponent(screenModel: SetupTimersComponentModel) {
    val timersEnabled by screenModel.timersEnabled.collectAsState()
    val selectedPreset by screenModel.selectedPreset.collectAsState()

    val normalGameTimeSetting by screenModel.normalGameLimit.collectAsState()
    val normalGameBuffer by screenModel.normalGameBuffer.collectAsState()
    val overtimeExtraLimit by screenModel.overtimeExtraLimit.collectAsState()
    val overtimeExtraBuffer by screenModel.overtimeExtraBuffer.collectAsState()

    val selectedOutOfTimeEntry by screenModel.outOfTimeLimit.collectAsState()
    val selectedGameLimitReachedEntry by screenModel.gameLimitReached.collectAsState()

    val setupUseBuffer by screenModel.setupUseBuffer.collectAsState()
    val setupFreeTime by screenModel.setupFreeTime.collectAsState()
    val setupMaxTime by screenModel.setupMaxTime.collectAsState()

    val teamTurnUseBuffer by screenModel.teamTurnUseBuffer.collectAsState()
    val teamTurnFreeTime by screenModel.teamTurnFreeTime.collectAsState()
    val teamTurnMaxTime by screenModel.teamTurnMaxTime.collectAsState()

    val responseUseBuffer by screenModel.responseUseBuffer.collectAsState()
    val responseFreeTime by screenModel.responseFreeTime.collectAsState()
    val responseMaxTime by screenModel.responseMaxTime.collectAsState()

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
                        screenModel.updateTimersEnabled(it)
                    }
                }
                Spacer(modifier = Modifier.width(16.dp))
                Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.CenterEnd) {
                    JervisDropDownMenu(
                        title = "Presets",
                        enabled = timersEnabled,
                        selectedEntry = selectedPreset,
                        entries = presets
                    ) {
                        screenModel.updatePreset(it)
                    }
                }
            }
            Row(
                modifier = Modifier.padding(top = 16.dp),
            ) {
                Column(
                    modifier = Modifier.weight(1f).wrapContentSize()
                ) {
                    BoxHeader("Totals", bottomPadding = 16.dp)
                    OutlinedTextField(
                        modifier = inputFieldModifier,
                        value = normalGameTimeSetting.value,
                        onValueChange = { screenModel.updateNormalGameTimeLimit(it) },
                        enabled = timersEnabled,
                        label = { Text(normalGameTimeSetting.label) },
                    )
                    OutlinedTextField(
                        modifier = inputFieldModifier,
                        value = normalGameBuffer.value,
                        onValueChange = { screenModel.updateNormalGameBuffer(it) },
                        enabled = timersEnabled,
                        label = { Text(normalGameBuffer.label) },
                    )
                    Divider(modifier = Modifier.padding(top = 16.dp, bottom = 16.dp).height(1.dp).background(JervisTheme.rulebookPaperDark.copy(0.3f)))
                    OutlinedTextField(
                        modifier = inputFieldModifier,
                        value = overtimeExtraLimit.value,
                        onValueChange = { screenModel.updateOvertimeExtraLimit(it) },
                        enabled = timersEnabled,
                        label = { Text(overtimeExtraLimit.label) },
                    )
                    OutlinedTextField(
                        modifier = inputFieldModifier,
                        value = overtimeExtraBuffer.value,
                        onValueChange = { screenModel.updateOvertimeExtraBuffer(it) },
                        enabled = timersEnabled,
                        label = { Text(overtimeExtraBuffer.label) },
                    )
                    BoxHeader("Limit Behavior", topPadding = 32.dp, bottomPadding = 16.dp)
                    JervisDropDownMenu("Out-of-time", enabled = timersEnabled, selectedEntry = selectedOutOfTimeEntry, entries = outOfTimeEntries) {
                        screenModel.updateOutOfTimeBehaviour(it)
                    }
                    JervisDropDownMenu("Game Limit Reached", enabled = timersEnabled, selectedEntry = selectedGameLimitReachedEntry, entries = gameLimitEntries) {
                        screenModel.updateGameLimitReachedBehaviour(it)
                    }
                }
                Spacer(modifier = Modifier.width(16.dp))
                Column(
                    modifier = Modifier.weight(1f).wrapContentSize()
                ) {
                    BoxHeader("Setup", bottomPadding = 16.dp)
                    SimpleSwitch("Use buffer", isSelected = setupUseBuffer, isEnabled = timersEnabled) {
                        screenModel.updateSetupUseBuffer(it)
                    }
                    OutlinedTextField(
                        modifier = inputFieldModifier,
                        value = setupFreeTime.value,
                        onValueChange = { screenModel.updateSetupFreeTime(it) },
                        enabled = timersEnabled,
                        label = { Text(setupFreeTime.label) },
                    )
                    OutlinedTextField(
                        modifier = inputFieldModifier,
                        value = setupMaxTime.value,
                        onValueChange = { screenModel.updateSetupMaxTime(it) },
                        enabled = timersEnabled && setupUseBuffer,
                        label = { Text(setupMaxTime.label) },
                    )
                    BoxHeader("Team Turn", topPadding = 32.dp, bottomPadding = 16.dp)
                    SimpleSwitch("Use buffer", teamTurnUseBuffer, isEnabled = timersEnabled) {
                        screenModel.updateTeamTurnUseBuffer(it)
                    }
                    OutlinedTextField(
                        modifier = inputFieldModifier,
                        value = teamTurnFreeTime.value,
                        onValueChange = { screenModel.updateTeamTurnFreeTime(it) },
                        enabled = timersEnabled,
                        label = { Text(teamTurnFreeTime.label) },
                    )
                    OutlinedTextField(
                        modifier = inputFieldModifier,
                        value = teamTurnMaxTime.value,
                        onValueChange = { screenModel.updateTeamTurnMaxTime(it) },
                        enabled = timersEnabled && teamTurnUseBuffer,
                        label = { Text(teamTurnMaxTime.label) },
                    )
                    BoxHeader("Out-of-turn Response", topPadding = 32.dp, bottomPadding = 16.dp)
                    SimpleSwitch("Use buffer", responseUseBuffer, isEnabled = timersEnabled) {
                        screenModel.updateResponseUseBuffer(it)
                    }
                    OutlinedTextField(
                        modifier = inputFieldModifier,
                        value = responseFreeTime.value,
                        onValueChange = { screenModel.updateResponseFreeTime(it) },
                        enabled = timersEnabled,
                        label = { Text(responseFreeTime.label) },
                    )
                    OutlinedTextField(
                        modifier = inputFieldModifier,
                        value = responseMaxTime.value,
                        onValueChange = { screenModel.updateResponseMaxTime(it) },
                        enabled = timersEnabled && responseUseBuffer,
                        label = { Text(responseMaxTime.label) },
                    )
                }
            }
        }
    }
}
