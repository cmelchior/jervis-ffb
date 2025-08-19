package com.jervisffb.ui.menu.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.jervisffb.ui.game.view.JervisTheme
import com.jervisffb.ui.game.view.JervisTheme.buttonTextColor
import com.jervisffb.ui.game.view.utils.JervisButton
import com.jervisffb.ui.isDigitsOnly
import com.jervisffb.ui.menu.components.teamselector.SelectTeamComponentModel
import com.jervisffb.ui.menu.utils.JervisLogo

@Composable
fun ImportTeamFromTourPlayDialog(
    viewModel: SelectTeamComponentModel,
    onDismissRequest: () -> Unit
) {
    var inputText by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }

    JervisDialog(
        title = "Import Team From TourPlay",
        icon = {
            JervisLogo()
            // TODO Find a good TourPlay logo here
            //            Image(
            //                modifier = Modifier.fillMaxWidth().padding(24.dp),
            //                painter = painterResource(Res.drawable.J),
            //                contentDescription = "",
            //                colorFilter = ColorFilter.tint(JervisTheme.white),
            //            )
        },
        width = 650.dp,
        backgroundScrim = true,
        content = { _, textColor ->
            Box(
                modifier = Modifier.weight(1f).padding(top = 16.dp),
                contentAlignment = Alignment.Center,
            ) {
                Column(
                    modifier = Modifier.padding(bottom = 8.dp),
                ){
                    Text("Enter the roster ID (found in the roster URL):")
                    Spacer(modifier = Modifier.height(8.dp))
                    TextField(
                        value = inputText,
                        onValueChange = { inputText = it },
                        isError = !inputText.isDigitsOnly(),
                        placeholder = { Text("Roster ID") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = Color.Transparent,
                            unfocusedContainerColor = Color.Transparent,
                        )
                    )
                    Text(
                        text = "Support is experimental and bugs will be present. Please report any teams that cannot be imported.",
                        fontSize = MaterialTheme.typography.bodySmall.fontSize,
                    )
                    if (error?.isNotBlank() == true) {
                        SelectionContainer {
                            Text(
                                modifier = Modifier.padding(top = 8.dp, bottom = 8.dp),
                                text = error!!,
                                color = JervisTheme.rulebookRed
                            )
                        }
                    }
                }
            }
        },
        buttons = {
            JervisButton(
                text = "Cancel",
                onClick = { onDismissRequest() },
            )
            Spacer(modifier = Modifier.weight(1f))
            JervisButton(
                text = if (isLoading) "Downloading..." else "Import Team",
                onClick = {
                    isLoading = true
                    viewModel.loadTourPlayTeamFromNetwork(
                        inputText,
                        onSuccess = {
                            isLoading = false
                            onDismissRequest()
                        },
                        onError = { msg ->
                            isLoading = false
                            error = msg
                        },
                    )
                },
                enabled = !isLoading && inputText.isNotBlank() && inputText.isDigitsOnly(),
                buttonColor = JervisTheme.rulebookBlue,
                textColor = buttonTextColor
            )
        }
    )
}
