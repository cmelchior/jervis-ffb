package com.jervisffb.ui.menu.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
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
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.unit.dp
import com.jervisffb.jervis_ui.generated.resources.Res
import com.jervisffb.jervis_ui.generated.resources.logo_fumbbl_small
import com.jervisffb.ui.IssueTracker
import com.jervisffb.ui.game.dialogs.DialogSize
import com.jervisffb.ui.game.view.JervisTheme
import com.jervisffb.ui.game.view.JervisTheme.buttonTextColor
import com.jervisffb.ui.game.view.utils.JervisButton
import com.jervisffb.ui.isDigitsOnly
import com.jervisffb.ui.menu.components.teamselector.SelectTeamComponentModel
import com.jervisffb.utils.openUrlInBrowser
import org.jetbrains.compose.resources.painterResource

@Composable
fun ImportTeamFromFumbblDialog(
    viewModel: SelectTeamComponentModel,
    onDismissRequest: () -> Unit
) {
    var inputText by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    var errorWrapper by remember { mutableStateOf<Pair<String, Throwable?>?>(null) }

    JervisDialog(
        title = "Import Team From FUMBBL",
        icon = {
            Image(
                modifier = Modifier.fillMaxWidth().padding(24.dp),
                painter = painterResource(Res.drawable.logo_fumbbl_small),
                contentDescription = "",
                colorFilter = ColorFilter.tint(JervisTheme.white),
            )
        },
        width = DialogSize.MEDIUM,
        backgroundScrim = true,
        content = { _, textColor ->
            Box(
                modifier = Modifier.weight(1f).padding(top = 16.dp),
                contentAlignment = Alignment.Center,
            ) {
                Column(
                    modifier = Modifier.padding(bottom = 8.dp),
                ){
                    Text("Enter the team ID (found in the team URL):")
                    Spacer(modifier = Modifier.height(8.dp))
                    TextField(
                        value = inputText,
                        onValueChange = { inputText = it },
                        isError = !inputText.isDigitsOnly(),
                        placeholder = { Text("Team ID") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = Color.Transparent,
                            unfocusedContainerColor = Color.Transparent,
                            errorContainerColor = JervisTheme.rulebookRed.copy(alpha = 0.3f),
                        )
                    )
                    Text(
                        text = "Support is experimental and bugs will be present. Please report any teams that cannot be imported.",
                        fontSize = MaterialTheme.typography.bodySmall.fontSize,
                    )
                    if (errorWrapper != null) {
                        Text(modifier = Modifier.padding(top = 8.dp, bottom = 8.dp), text = errorWrapper?.first ?: "Unknown error", color = JervisTheme.rulebookRed)
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
            if (errorWrapper != null) {
                JervisButton(
                    text = "Report Issue",
                    onClick = {
                        val reportIssueUrl = IssueTracker.createIssueUrlFromException(
                            title = "Cannot load team from FUMBBL",
                            body = "Team ID: $inputText",
                            errorWrapper!!.second!!
                        )
                        openUrlInBrowser(reportIssueUrl)
                    },
                    enabled = !isLoading && inputText.isNotBlank() && inputText.isDigitsOnly(),
                    buttonColor = JervisTheme.rulebookBlue,
                    textColor = buttonTextColor
                )
                Spacer(modifier = Modifier.width(16.dp))
            }
            JervisButton(
                text = if (isLoading) "Downloading..." else "Import Team",
                onClick = {
                    isLoading = true
                    viewModel.loadFumbblTeamFromNetwork(
                        inputText,
                        onSuccess = {
                            isLoading = false
                            onDismissRequest()
                        },
                        onError = { msg, error ->
                            isLoading = false
                            errorWrapper = msg to error
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
