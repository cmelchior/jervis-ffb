package com.jervisffb.ui.menu.p2p.host

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.OutlinedTextField
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.jervisffb.ui.game.view.utils.JervisButton
import com.jervisffb.ui.menu.components.setup.SetupGameComponent

@Composable
fun SetupGamePage(setupModel: SetupGameScreenModel, modifier: Modifier) {
    val coachName by setupModel.coachName.collectAsState("")
    val gameName by setupModel.gameName.collectAsState("")
    val gamePort by setupModel.port.collectAsState( null)
    val isSetupValid: Boolean by setupModel.isSetupValid.collectAsState(false)

    Column(
        modifier = modifier.fillMaxSize(),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
            ,
            verticalAlignment = Alignment.Top
        ) {
            Column {
                SettingsCard("Coach", 300.dp) {
                    OutlinedTextField(
                        modifier = Modifier.fillMaxWidth(),
                        value = coachName,
                        onValueChange = { setupModel.updateCoachName(it) },
                        label = { Text("Coach name") }
                    )
                }
                Spacer(modifier = Modifier.height(32.dp))
                SettingsCard("Game", 300.dp) {
                    OutlinedTextField(
                        modifier = Modifier.fillMaxWidth(),
                        value = gameName,
                        onValueChange = { setupModel.setGameName(it) },
                        label = { Text("Game Name") }
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        modifier = Modifier.width(100.dp),
                        value = gamePort?.toString() ?: "",
                        onValueChange = { setupModel.setPort(it) },
                        label = { Text("Port") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        keyboardActions = KeyboardActions {

                        }
                    )
                }
            }
            Spacer(modifier = Modifier.width(32.dp))
            SetupGameComponent(setupModel.setupGameScreenModel, setupModel.setupTimersScreenModel)
        }
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
            JervisButton(text = "Next", enabled = isSetupValid, onClick = { setupModel.gameSetupDone() })
        }
    }
}
