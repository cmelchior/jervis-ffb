package com.jervisffb.ui.screen.p2p.client

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.OutlinedTextField
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.jervisffb.engine.reports.ReportStartingExtraTime.message
import com.jervisffb.ui.screen.p2p.client.JoinHostScreenModel.JoinState
import com.jervisffb.ui.view.JervisTheme
import com.jervisffb.ui.view.utils.JervisButton
import com.jervisffb.ui.view.utils.TitleBorder

/**
 * Layout class for the "Join Host" panel.
 */
@Composable
fun JoinHostScreen(viewModel: JoinHostScreenModel, onCancel: () -> Unit, onJoin: () -> Unit) {
    val gameUrl by viewModel.gameUrl().collectAsState()
    val serverUrl by viewModel.serverIp().collectAsState()
    val port by viewModel.port().collectAsState()
    val gameId by viewModel.gameId().collectAsState()
    val joinState by viewModel.canJoin().collectAsState()
    val joiningMessage by viewModel.joinMessage().collectAsState()

    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center,
    ) {
        Column(modifier = Modifier.width(600.dp).padding(bottom = 100.dp)) {
            JoinHostHeader()
            Spacer(modifier = Modifier.height(16.dp))
            Row(
                verticalAlignment = Alignment.CenterVertically,
            ) {
                OutlinedTextField(
                    modifier = Modifier.weight(1f),
                    value = gameUrl,
                    onValueChange = { viewModel.updateGameUrl(it) },
                    singleLine = true,
                    label = { Text("Game URL") },
                )
                if (message.isNotEmpty()) {
                    Spacer(modifier = Modifier.width(16.dp))
                    ProgressMessage(message)
                }
            }
            OutlinedTextField(
                modifier = Modifier.width(400.dp),
                value = serverUrl,
                onValueChange = { viewModel.updateServerIp(it) },
                singleLine = true,
                label = { Text("Server URL") },
            )
            OutlinedTextField(
                modifier = Modifier.width(100.dp),
                value = port,
                onValueChange = { viewModel.updatePort(it) },
                singleLine = true,
                label = { Text("Port") },
            )
            OutlinedTextField(
                modifier = Modifier.width(200.dp),
                value = gameId,
                onValueChange = { viewModel.updateGameId(it) },
                singleLine = true,
                label = { Text("Game ID") },
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
            ) {
                when (joinState) {
                    JoinState.NOT_READY,
                    JoinState.READY -> {
                        JervisButton("Join", onClick = { onJoin() }, enabled = (JoinState.READY == joinState))
                    }
                    JoinState.JOINING -> {
                        JervisButton("Cancel", onClick = { onCancel() })
                    }
                }
            }
        }
//        Row(modifier = Modifier.fillMaxWidth().align(Alignment.BottomEnd), horizontalArrangement = Arrangement.Start) {
//            Spacer(modifier = Modifier.weight(1f))
//            JervisButton("REJECT GAME", onClick = { onCancel() }, enabled = (true))
//            Spacer(modifier = Modifier.width(16.dp))
//            JervisButton("ACCEPT GAME", onClick = { onCancel() }, enabled = (true))
//        }
    }
}

@Composable
private fun JoinHostHeader(color: Color = JervisTheme.rulebookRed) {
    TitleBorder(color)
    Box(
        modifier = Modifier.height(36.dp),
        contentAlignment = Alignment.CenterStart,
    ) {
        Text(
            modifier = Modifier.padding(bottom = 2.dp),
            text = "Host information".uppercase(),
            fontWeight = FontWeight.Bold,
            fontSize = 16.sp,
            color = color
        )
    }
    TitleBorder(color)
}

@Composable
private fun ProgressMessage(message: String) {

}
