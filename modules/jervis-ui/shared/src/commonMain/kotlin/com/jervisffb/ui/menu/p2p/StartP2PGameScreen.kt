package com.jervisffb.ui.menu.p2p

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.jervisffb.engine.model.Team
import com.jervisffb.ui.game.model.ModelRef
import com.jervisffb.ui.game.view.JervisTheme
import com.jervisffb.ui.game.view.utils.JervisButton
import com.jervisffb.ui.game.view.utils.animatedDots
import com.jervisffb.ui.menu.components.starting.StartGameComponent
import kotlinx.coroutines.flow.Flow

/**
 * Screen showing the last step in starting either a "P2P Client" or "P2P Host" game.
 */
@Composable
fun StartP2PGamePage(
    homeTeam: Flow<ModelRef<Team>?>,
    awayTeam: Flow<ModelRef<Team>?>,
    onAcceptGame: (Boolean) -> Unit,
    reconnecting: Boolean = false,
    // Only ever `true` for the Host. Rejecting the game shuts the server down, which takes a moment
    // and keeps this page visible while it happens, so explain the delay and stop taking input.
    serverShuttingDown: Boolean = false,
) {
    when (serverShuttingDown) {
        true -> Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = "Shutting down the server" + animatedDots(),
                color = JervisTheme.contentTextColor.copy(alpha = 0.7f),
            )
        }
        false -> Column(
            modifier = Modifier.fillMaxSize(),
        ) {
            StartGameComponent(homeTeam, awayTeam)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                JervisButton(
                    if (reconnecting) "Cancel" else "Reject Game",
                    onClick = { onAcceptGame(false) },
                )
                Spacer(modifier = Modifier.width(16.dp))
                JervisButton(if (reconnecting) "Continue Game" else "Start Game", onClick = { onAcceptGame(true) })
            }
        }
    }
}
