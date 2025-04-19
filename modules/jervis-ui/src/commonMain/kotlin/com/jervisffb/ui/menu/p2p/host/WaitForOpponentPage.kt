package com.jervisffb.ui.menu.p2p.host

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
import androidx.compose.material.OutlinedTextField
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.jervisffb.jervis_ui.generated.resources.Res
import com.jervisffb.jervis_ui.generated.resources.jervis_icon_menu_copy
import com.jervisffb.ui.game.view.JervisTheme
import com.jervisffb.ui.game.view.utils.TitleBorder
import kotlinx.coroutines.delay
import org.jetbrains.compose.resources.painterResource

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun WaitForOpponentPage(viewModel: P2PHostScreenModel) {
    val globalUrl: String by viewModel.globalUrl.collectAsState()
    val localUrl: String by viewModel.localUrl.collectAsState()
    val globalUrlError by viewModel.globalGameUrlError.collectAsState()
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center,
    ) {
        Column(modifier = Modifier.width(600.dp).padding(bottom = 100.dp)) {
            WaitForOpponentHeader()
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
            Row(Modifier.fillMaxWidth().padding(top = 16.dp), verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "Depending on where your opponent is connecting from, send them one of the two URLs above. Note that network setups can be tricky, so it's possible neither will work. If that happens… well, you're on your own. Sorry!",
                    color = JervisTheme.contentTextColor,
                )
            }
        }
        Row(modifier = Modifier.fillMaxWidth().align(Alignment.BottomEnd), horizontalArrangement = Arrangement.End) {
            // Buttons
        }
    }
}

@Composable
private fun WaitForOpponentHeader(color: Color = JervisTheme.rulebookRed) {
    var dotCount by remember { mutableStateOf(1) } // Track the number of dots (1 to 3)
    LaunchedEffect(Unit) {
        while (true) {
            dotCount = (dotCount % 3) + 1
            delay(500L)
        }
    }
    val loadingText = "Waiting For Opponent" + ".".repeat(dotCount)

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
