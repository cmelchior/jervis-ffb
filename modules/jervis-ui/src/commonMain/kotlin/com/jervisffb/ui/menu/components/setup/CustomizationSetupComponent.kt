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
import androidx.compose.material.OutlinedTextField
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.jervisffb.ui.menu.components.JervisDropDownMenu
import com.jervisffb.ui.menu.components.SmallHeader

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun CustomizationSetupComponent(screenModel: CustomizationSetupComponentModel) {

    val inputFieldModifier = Modifier.padding(bottom = 8.dp).fillMaxWidth()

    val fieldWidth by screenModel.fieldWidth.collectAsState()
    val fieldHeight by screenModel.fieldHeight.collectAsState()
    val maxPlayersOnField by screenModel.maxPlayersOnField.collectAsState()

    val halfs by screenModel.halfs.collectAsState()
    val turnsPrHalf by screenModel.turnsPrHalf.collectAsState()

    val diceRollOwner by screenModel.selectedDiceRollBehavior.collectAsState()
    val undoActionBehavior by screenModel.selectedUndoActionBehavior.collectAsState()
    val foulActionBehavior by screenModel.selectedFoulActionBehavior.collectAsState()
    val kickingPlayerBehavior by screenModel.selectedKickingPlayerBehavior.collectAsState()

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
                    SmallHeader("Field", bottomPadding = smallHeaderBottomPadding)
                    OutlinedTextField(
                        modifier = inputFieldModifier.width(40.dp),
                        value = fieldWidth.value,
                        onValueChange = { screenModel.updateFieldWidth(it) },
                        enabled = true,
                        label = { Text(fieldWidth.label) },
                    )
                    OutlinedTextField(
                        modifier = inputFieldModifier,
                        value = fieldHeight.value,
                        onValueChange = { screenModel.updateFieldHeight(it) },
                        enabled = true,
                        label = { Text(fieldHeight.label) },
                    )
                    OutlinedTextField(
                        modifier = inputFieldModifier,
                        value = maxPlayersOnField.value,
                        onValueChange = { screenModel.updateMaxPlayersOnField(it) },
                        enabled = true,
                        label = { Text(maxPlayersOnField.label) },
                    )
                    SmallHeader("Duration", topPadding = smallHeaderTopPadding, bottomPadding = smallHeaderBottomPadding)
                    OutlinedTextField(
                        modifier = inputFieldModifier,
                        value = halfs.value,
                        onValueChange = { screenModel.updateHalfs(it) },
                        enabled = true,
                        label = { Text(halfs.label) },
                    )
                    OutlinedTextField(
                        modifier = inputFieldModifier,
                        value = turnsPrHalf.value,
                        onValueChange = { screenModel.updateTurnsPrHalf(it) },
                        enabled = true,
                        label = { Text(turnsPrHalf.label) }
                    )
                }
                Spacer(modifier = Modifier.width(24.dp))
                Column(
                    modifier = Modifier.weight(1f).wrapContentSize()
                ) {
                    SmallHeader("Randomness", bottomPadding = smallHeaderBottomPadding)
                    JervisDropDownMenu("Dice Rolls", enabled = true, selectedEntry = diceRollOwner, entries = screenModel.diceRollEntries) {
                        screenModel.updateDiceRollBehavior(it)
                    }
                    SmallHeader("Variants", topPadding = smallHeaderTopPadding, bottomPadding = smallHeaderBottomPadding)
                    JervisDropDownMenu("Undo Actions", enabled = true, selectedEntry = undoActionBehavior, entries = screenModel.undoActionsEntries) {
                        screenModel.updateUndoActionBehavior(it)
                    }
                    JervisDropDownMenu("Foul Action", enabled = true, selectedEntry = foulActionBehavior, entries = screenModel.foulActionBehavior) {
                        screenModel.updateFoulActionBehavior(it)
                    }
                    JervisDropDownMenu("Kicking Player", enabled = true, selectedEntry = kickingPlayerBehavior, entries = screenModel.kickingPlayerBehavior) {
                        screenModel.updateKickingPlayerBehavior(it)
                    }
                }
            }
        }
    }
}

