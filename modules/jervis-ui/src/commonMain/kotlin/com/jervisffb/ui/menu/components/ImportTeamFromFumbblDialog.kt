package com.jervisffb.ui.menu.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.unit.dp
import com.jervisffb.jervis_ui.generated.resources.Res
import com.jervisffb.jervis_ui.generated.resources.logo_fumbbl_small
import com.jervisffb.ui.game.view.JervisTheme
import com.jervisffb.ui.game.view.JervisTheme.buttonTextColor
import com.jervisffb.ui.game.view.utils.JervisButton
import com.jervisffb.ui.isDigitsOnly
import com.jervisffb.ui.menu.components.teamselector.SelectTeamComponentModel
import org.jetbrains.compose.resources.painterResource

@Composable
fun ImportTeamFromFumbblDialog(
    viewModel: SelectTeamComponentModel,
    onDismissRequest: () -> Unit
) {
    var inputText by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }

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
        width = 650.dp,
        content = { _, textColor ->
            Box(
                modifier = Modifier.weight(1f).padding(top = 16.dp),
                contentAlignment = Alignment.Center,
            ) {
                Column(
                    modifier = Modifier.padding(bottom = 16.dp),
                ){
                    Text("Enter the team ID (found in the team URL):")
                    Spacer(modifier = Modifier.height(8.dp))
                    TextField(
                        value = inputText,
                        onValueChange = { inputText = it },
                        isError = !inputText.isDigitsOnly(),
                        placeholder = { Text("Team ID") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    if (error?.isNotBlank() == true) {
                        Text(modifier = Modifier.padding(top = 8.dp, bottom = 8.dp), text = error!!, color = JervisTheme.rulebookRed)
                    }
                }
            }
        },
        buttons = {
            Spacer(modifier = Modifier.weight(1f))
            JervisButton(
                text = "Cancel",
                onClick = { onDismissRequest() },
            )
            Spacer(modifier = Modifier.width(16.dp))
            JervisButton(
                text = if (isLoading) "Downloading..." else "Import Team",
                onClick = {
                    isLoading = true
                    viewModel.loadTeamFromNetwork(
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
