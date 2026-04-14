package com.jervisffb.ui.menu.components.setup

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.jervisffb.ui.menu.components.JervisDropDownMenu
import com.jervisffb.ui.menu.components.SimpleSwitch
import com.jervisffb.ui.menu.components.SmallHeader

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun CustomizationSetupComponent(viewModel: CustomizationSetupComponentModel) {

    val inputFieldModifier = Modifier.padding(bottom = 8.dp).fillMaxWidth()

    val pitchWidth by viewModel.pitchWidth.collectAsState()
    val pitchHeight by viewModel.pitchHeight.collectAsState()
    val endZone by viewModel.endZone.collectAsState()
    val lineOfScrimmageHome by viewModel.lineOfScrimmageHome.collectAsState()
    val lineOfScrimmageAway by viewModel.lineOfScrimmageAway.collectAsState()
    val wideZone by viewModel.wideZone.collectAsState()

    val maxPlayersOnPitch by viewModel.maxPlayersOnPitch.collectAsState()
    val maxPlayersInWideZone by viewModel.maxPlayersInWideZone.collectAsState()

    val halfs by viewModel.halfs.collectAsState()
    val turnsPrHalf by viewModel.turnsPrHalf.collectAsState()

    val diceRollOwner by viewModel.selectedDiceRollBehavior.collectAsState()
    val undoActionBehavior by viewModel.selectedUndoActionBehavior.collectAsState()
    val foulActionBehavior by viewModel.selectedFoulActionBehavior.collectAsState()
    val kickingPlayerBehavior by viewModel.selectedKickingPlayerBehavior.collectAsState()
    val allowPlayerEdits by viewModel.allowPlayerEdits.collectAsState()

    Box(
        modifier = Modifier.fillMaxSize().padding(top = 16.dp),
        contentAlignment = Alignment.TopCenter
    ) {
        Column(
            modifier = Modifier.width(750.dp).verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Row(
                modifier = Modifier.padding(top = 16.dp),
            ) {
                Column(
                    modifier = Modifier.weight(1f).wrapContentSize()
                ) {
                    SmallHeader("Pitch", bottomPadding = smallHeaderBottomPadding)
                    OutlinedTextField(
                        modifier = inputFieldModifier.width(40.dp),
                        value = pitchWidth.value,
                        onValueChange = { viewModel.updateFieldWidth(it) },
                        enabled = true,
                        label = { Text(pitchWidth.label) },
                    )
                    OutlinedTextField(
                        modifier = inputFieldModifier,
                        value = pitchHeight.value,
                        onValueChange = { viewModel.updateFieldHeight(it) },
                        enabled = true,
                        label = { Text(pitchHeight.label) },
                    )
                    OutlinedTextField(
                        modifier = inputFieldModifier,
                        value = lineOfScrimmageHome.value,
                        onValueChange = { viewModel.updateLineOfScrimmageHome(it) },
                        enabled = true,
                        label = { Text(lineOfScrimmageHome.label) },
                    )
                    OutlinedTextField(
                        modifier = inputFieldModifier,
                        value = lineOfScrimmageAway.value,
                        onValueChange = { viewModel.updateLineOfScrimmageAway(it) },
                        enabled = true,
                        label = { Text(lineOfScrimmageAway.label) },
                    )
                    OutlinedTextField(
                        modifier = inputFieldModifier,
                        value = wideZone.value,
                        onValueChange = { viewModel.updateWideZone(it) },
                        enabled = true,
                        label = { Text(wideZone.label) },
                    )
                    OutlinedTextField(
                        modifier = inputFieldModifier,
                        value = endZone.value,
                        onValueChange = { viewModel.updateEndZone(it) },
                        enabled = true,
                        label = { Text(endZone.label) },
                    )
                    OutlinedTextField(
                        modifier = inputFieldModifier,
                        value = maxPlayersOnPitch.value,
                        onValueChange = { viewModel.updateMaxPlayersOnField(it) },
                        enabled = true,
                        label = { Text(maxPlayersOnPitch.label) },
                    )
                    OutlinedTextField(
                        modifier = inputFieldModifier,
                        value = maxPlayersInWideZone.value,
                        onValueChange = { viewModel.updateMaxPlayersInWideZone(it) },
                        enabled = true,
                        label = { Text(maxPlayersInWideZone.label) },
                    )
                }
                Spacer(modifier = Modifier.width(24.dp))
                Column(
                    modifier = Modifier.weight(1f).wrapContentSize()
                ) {
                    SmallHeader("Duration", bottomPadding = smallHeaderBottomPadding)
                    OutlinedTextField(
                        modifier = inputFieldModifier,
                        value = halfs.value,
                        onValueChange = { viewModel.updateHalfs(it) },
                        enabled = true,
                        label = { Text(halfs.label) },
                    )
                    OutlinedTextField(
                        modifier = inputFieldModifier,
                        value = turnsPrHalf.value,
                        onValueChange = { viewModel.updateTurnsPrHalf(it) },
                        enabled = true,
                        label = { Text(turnsPrHalf.label) }
                    )
                    SmallHeader("Variants", topPadding = smallHeaderTopPadding, bottomPadding = smallHeaderBottomPadding)
                    JervisDropDownMenu("Undo Actions", enabled = true, selectedEntry = undoActionBehavior, entries = viewModel.undoActionsEntries) {
                        viewModel.updateUndoActionBehavior(it)
                    }
                    JervisDropDownMenu("Foul Action", enabled = true, selectedEntry = foulActionBehavior, entries = viewModel.foulActionBehavior) {
                        viewModel.updateFoulActionBehavior(it)
                    }
                    JervisDropDownMenu("Kicking Player", enabled = true, selectedEntry = kickingPlayerBehavior, entries = viewModel.kickingPlayerBehavior) {
                        viewModel.updateKickingPlayerBehavior(it)
                    }
                    SmallHeader("Dev Settings", topPadding = smallHeaderTopPadding, bottomPadding = smallHeaderBottomPadding)
                    JervisDropDownMenu("Dice Rolls", enabled = true, selectedEntry = diceRollOwner, entries = viewModel.diceRollEntries) {
                        viewModel.updateDiceRollBehavior(it)
                    }
                    if (viewModel.isHotseat) {
                        // TODO Make this globally available when we have refactored editing a player to go
                        //  through "dev game actions" that work across the network protocol
                        SimpleSwitch("Allow Player Edits", isSelected = allowPlayerEdits) {
                            viewModel.updateAllowPlayerEdits(it)
                        }
                    }
                }
            }
        }
    }
}

