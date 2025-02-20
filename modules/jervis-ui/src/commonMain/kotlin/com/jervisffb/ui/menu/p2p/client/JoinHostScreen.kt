package com.jervisffb.ui.menu.p2p.client

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
import com.jervisffb.ui.game.view.JervisTheme
import com.jervisffb.ui.game.view.utils.JervisButton
import com.jervisffb.ui.game.view.utils.TitleBorder

/**
 * Layout class for the "Join Host" panel.
 */
@Composable
fun JoinHostScreen(viewModel: JoinHostScreenModel, onJoin: () -> Unit, onCancel: () -> Unit, ) {
    val gameUrl by viewModel.gameUrl().collectAsState()
    val serverUrl by viewModel.serverIp().collectAsState()
    val port by viewModel.port().collectAsState()
    val gameId by viewModel.gameId().collectAsState()
    val joinState by viewModel.canJoin().collectAsState()
    val joiningMessage by viewModel.joinMessage().collectAsState()
    val joiningError by viewModel.joinError().collectAsState()
    val coachName: String by viewModel.coachName().collectAsState()
    Box(
        modifier = Modifier.fillMaxSize().padding(top = 0.dp),
        contentAlignment = Alignment.TopCenter,
    ) {
        Column(modifier = Modifier.width(600.dp)) {
            when (joinState) {
                JoinHostScreenModel.JoinState.INVALID_URL,
                JoinHostScreenModel.JoinState.READY_JOIN -> EnterHostDataContent(gameUrl, viewModel, joiningError, serverUrl, port, gameId, coachName)
                JoinHostScreenModel.JoinState.JOINING -> MessageContent("Joining Host", joiningMessage)
                JoinHostScreenModel.JoinState.JOINED -> MessageContent("Joined Host", joiningMessage)
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
            ) {
                when (joinState) {
                    JoinHostScreenModel.JoinState.INVALID_URL,
                    JoinHostScreenModel.JoinState.READY_JOIN -> {
                        JervisButton("Join", onClick = { onJoin() }, enabled = (joinState == JoinHostScreenModel.JoinState.READY_JOIN))
                    }
                    JoinHostScreenModel.JoinState.JOINED -> {
                        JervisButton("Disconnect", onClick = { onCancel() }, enabled = true)
                        Spacer(modifier = Modifier.width(16.dp))
                        JervisButton("Continue", onClick = { onJoin() }, enabled = true)
                    }
                    JoinHostScreenModel.JoinState.JOINING -> {
                        JervisButton("Joining", onClick = { /* Do nothing */ }, enabled = false)
                    }
                }
            }
        }
    }
}

@Composable
private fun EnterHostDataContent(
    gameUrl: String,
    viewModel: JoinHostScreenModel,
    joiningError: String,
    serverUrl: String,
    port: String,
    gameId: String,
    coachName: String
) {
    JoinHostHeader("Coach Information")
    Spacer(modifier = Modifier.height(16.dp))
    OutlinedTextField(
        modifier = Modifier.width(300.dp),
        value = coachName,
        onValueChange = { viewModel.updateCoachName(it) },
        singleLine = true,
        label = { Text("Coach Name") },
    )
    Spacer(modifier = Modifier.height(32.dp))
    JoinHostHeader("Host information")
    Spacer(modifier = Modifier.height(16.dp))

    if (joiningError.isNotEmpty()) {
        Row(modifier = Modifier.padding(bottom = 16.dp)) {
            ProgressMessage(joiningError)
        }
    }
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
    }
    OutlinedTextField(
        modifier = Modifier.width(400.dp),
        value = serverUrl,
        onValueChange = { viewModel.updateServerIp(it) },
        singleLine = true,
        label = { Text("IP Address") },
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
}

@Composable
private fun MessageContent(
    header: String,
    message: String,
) {
    JoinHostHeader(header)
    Spacer(modifier = Modifier.height(16.dp))
    Row(
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(text = message, color = JervisTheme.contentTextColor)
    }
    Spacer(modifier = Modifier.height(16.dp))
}

@Composable
private fun JoinHostHeader(title: String, color: Color = JervisTheme.rulebookRed) {
    TitleBorder(color)
    Box(
        modifier = Modifier.height(36.dp),
        contentAlignment = Alignment.CenterStart,
    ) {
        Text(
            modifier = Modifier.padding(bottom = 2.dp),
            text = title.uppercase(),
            fontWeight = FontWeight.Bold,
            fontSize = 16.sp,
            color = color
        )
    }
    TitleBorder(color)
}

@Composable
private fun ProgressMessage(message: String) {
    Text(message, color = Color.Red, fontWeight = FontWeight.Bold)
}
