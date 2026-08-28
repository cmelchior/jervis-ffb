package com.jervisffb.ui.menu.p2p.host

import androidx.compose.animation.Crossfade
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.jervisffb.shared.generated.resources.Res
import com.jervisffb.shared.generated.resources.jervis_icon_menu_copy
import com.jervisffb.ui.game.view.JervisTheme
import com.jervisffb.ui.game.view.utils.TitleBorder
import com.jervisffb.ui.game.view.utils.animatedDots
import org.jetbrains.compose.resources.painterResource

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun WaitForOpponentPage(viewModel: P2PHostScreenModel) {
    val globalUrl: String by viewModel.globalGameUrl.collectAsState()
    val localUrl: String by viewModel.localGameUrl.collectAsState()
    val globalUrlError by viewModel.globalGameUrlError.collectAsState()
    val serverShuttingDown by viewModel.isServerShuttingDown.collectAsState()
    val connectingToServer by viewModel.isConnectingToServer.collectAsState()
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center,
    ) {
        Crossfade(
            targetState = connectingToServer,
            label = "Connecting to game server",
        ) { connecting ->
            when (connecting) {
                true -> Text("Connecting", color = JervisTheme.contentTextColor)
                false -> Column(modifier = Modifier.width(600.dp).padding(bottom = 100.dp)) {
                    WaitForOpponentHeader(serverShuttingDown)
                    Spacer(modifier = Modifier.height(16.dp))
                    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                        OutlinedTextField(
                            modifier = Modifier.weight(1f),
                            value = if (globalUrlError.isNullOrEmpty()) globalUrl else (globalUrlError ?: ""),
                            onValueChange = { },
                            readOnly = true,
                            isError = globalUrlError != null,
                            singleLine = true,
                            label = { Text("Game URL (Global URL)") },
                        )
                        Box(
                            modifier = Modifier
                                .padding(start = 4.dp, top = 16.dp, bottom = 8.dp)
                                .size(48.dp)
                                .offset(x = 4.dp)
                                .clip(shape = RoundedCornerShape(4.dp))
                                .clickable {
                                    viewModel.userCopyUrlToClipboard(globalUrl)
                                }
                            ,
                            contentAlignment = Alignment.Center,
                        ) {
                            Image(
                                modifier = Modifier.fillMaxSize(0.8f).aspectRatio(1f),
                                colorFilter = ColorFilter.tint(JervisTheme.rulebookRed) ,
                                painter = painterResource(Res.drawable.jervis_icon_menu_copy),
                                contentDescription = "Copy URL",
                            )
                        }
                    }
                    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                        OutlinedTextField(
                            modifier = Modifier.weight(1f),
                            value = localUrl,
                            onValueChange = { },
                            readOnly = true,
                            singleLine = true,
                            label = { Text("Game URL (Local Network URL)") },
                        )
                        Box(
                            modifier = Modifier
                                .padding(start = 4.dp, top = 16.dp, bottom = 8.dp)
                                .size(48.dp)
                                .offset(x = 4.dp)
                                .clip(shape = RoundedCornerShape(4.dp))
                                .clickable {
                                    viewModel.userCopyUrlToClipboard(localUrl)
                                }
                            ,
                            contentAlignment = Alignment.Center,
                        ) {
                            Image(
                                modifier = Modifier.fillMaxSize(0.8f).aspectRatio(1f),
                                colorFilter = ColorFilter.tint(JervisTheme.rulebookRed) ,
                                painter = painterResource(Res.drawable.jervis_icon_menu_copy),
                                contentDescription = "Copy URL",
                            )
                        }
                    }
                    // Hide connection information text while the server is shutting down. It no longer serves a purpose.
                    if (!serverShuttingDown) {
                        Row(Modifier.fillMaxWidth().padding(top = 16.dp), verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "Depending on where your opponent is connecting from, send them one of the two URLs above. Note that network setups can be tricky, so it's possible neither will work. If that happens… well, you're on your own to figure out which IP address to use. Sorry!",
                                color = JervisTheme.contentTextColor,
                            )
                        }
                    }
                }
            }
        }
        Row(modifier = Modifier.fillMaxWidth().align(Alignment.BottomEnd), horizontalArrangement = Arrangement.End) {
            // Buttons
        }
    }
}

@Composable
private fun WaitForOpponentHeader(serverShuttingDown: Boolean, color: Color = JervisTheme.rulebookRed) {
    // Going back to "Configure Game" or "Select Team" keeps this page visible until the server
    // has released its port, so inform user that this is happening rather than just looking frozen.
    val title = if (serverShuttingDown) "Shutting Down The Server" else "Waiting For Opponent"
    val loadingText = title + animatedDots()

    TitleBorder(color)
    Box(
        modifier = Modifier.height(36.dp),
        contentAlignment = Alignment.CenterStart,
    ) {
        Text(
            modifier = Modifier.padding(bottom = 2.dp),
            text = loadingText.uppercase(),
            fontWeight = FontWeight.Bold,
            fontSize = 16.sp,
            color = color
        )
    }
    TitleBorder(color)
}
