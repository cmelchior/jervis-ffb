package com.jervisffb.ui.screen.hotseat

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.jervisffb.engine.model.Team
import com.jervisffb.ui.screen.components.starting.StartGameComponent
import com.jervisffb.ui.view.utils.JervisButton
import kotlinx.coroutines.flow.Flow

@Composable
fun StartHotseatGamePage(
    homeTeam: Flow<Team?>,
    awayTeam: Flow<Team?>,
    onAcceptGame: (Boolean) -> Unit,
) {
    Column(
        modifier = Modifier.fillMaxSize(),
    ) {
        StartGameComponent(homeTeam, awayTeam)
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
            JervisButton("Start Game", onClick = { onAcceptGame(true) }, enabled = true)
        }
    }
}
