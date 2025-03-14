package com.jervisffb.ui.menu.hotseat

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.jervisffb.ui.game.view.utils.JervisButton
import com.jervisffb.ui.menu.components.setup.GameConfigurationContainerComponent

@Composable
fun SetupHotseatGamePage(viewModel: SetupHotseatGameScreenModel, modifier: Modifier) {
    val isSetupValid: Boolean by viewModel.gameConfigModel.isSetupValid.collectAsState(false)
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
            GameConfigurationContainerComponent(viewModel.gameConfigModel)
        }
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
            JervisButton(text = "Next", enabled = isSetupValid, onClick = { viewModel.gameSetupDone() })
        }
    }
}
