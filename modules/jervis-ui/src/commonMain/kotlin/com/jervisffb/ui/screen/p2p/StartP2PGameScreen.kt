package com.jervisffb.ui.screen.p2p

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.jervisffb.engine.model.Team
import com.jervisffb.ui.screen.components.starting.StartGameComponent
import com.jervisffb.ui.view.utils.JervisButton
import kotlinx.coroutines.flow.Flow

/**
 * Screen showing the last step in starting either a "P2P Client" or "P2P Host" game.
 */
@Composable
fun StartP2PGamePage(
    homeTeam: Flow<Team?>,
    awayTeam: Flow<Team?>,
    onAcceptGame: (Boolean) -> Unit,
) {
    Column(
        modifier = Modifier.fillMaxSize(),
    ) {
        StartGameComponent(homeTeam, awayTeam)
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
            JervisButton("Reject Game", onClick = { onAcceptGame(false) }, enabled = true)
            Spacer(modifier = Modifier.width(16.dp))
            JervisButton("Start Game", onClick = { onAcceptGame(true) }, enabled = true)
        }
    }
}
