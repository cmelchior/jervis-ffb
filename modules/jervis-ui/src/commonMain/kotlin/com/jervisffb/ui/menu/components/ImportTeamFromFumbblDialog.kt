package com.jervisffb.ui.menu.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.material.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.jervisffb.jervis_ui.generated.resources.Res
import com.jervisffb.jervis_ui.generated.resources.logo_fumbbl_small
import com.jervisffb.ui.game.view.JervisTheme
import com.jervisffb.ui.game.view.JervisTheme.buttonTextColor
import com.jervisffb.ui.game.view.utils.JervisButton
import com.jervisffb.ui.game.view.utils.TitleBorder
import com.jervisffb.ui.game.view.utils.bannerBackground
import com.jervisffb.ui.game.view.utils.paperBackground
import com.jervisffb.ui.isDigitsOnly
import com.jervisffb.ui.menu.components.teamselector.SelectTeamComponentModel
import org.jetbrains.compose.resources.painterResource

@Composable
fun ImportTeamFromFumbblDialog(
    viewModel: SelectTeamComponentModel,
    onCloseRequest: () -> Unit
) {
    val dialogColor = JervisTheme.rulebookRed
    val textColor = JervisTheme.contentTextColor

    Dialog(
        onDismissRequest = { onCloseRequest },
        properties = DialogProperties(
            usePlatformDefaultWidth = false
        ),
    ) {
        Surface(
            elevation = 8.dp,
            modifier = Modifier
                .width(650.dp)
                .defaultMinSize(minHeight = 200.dp, minWidth = 650.dp)
                .paperBackground(color = JervisTheme.rulebookPaper)
            ,
            shape = MaterialTheme.shapes.medium,
            border = BorderStroke(8.dp, color = dialogColor),
            color = JervisTheme.rulebookPaper,
            contentColor = textColor,
        ) {
            Row(Modifier
                .fillMaxWidth()
                .height(IntrinsicSize.Min)
                .paperBackground(JervisTheme.rulebookPaper)
            ) {
                Box(modifier = Modifier
                    .width(130.dp)
                    .fillMaxHeight()
                    .padding(start = 24.dp)
                    .bannerBackground()

                ) {
                    Image(
                        modifier = Modifier.fillMaxWidth().padding(24.dp),
                        painter = painterResource(Res.drawable.logo_fumbbl_small),
                        contentDescription = "",
                        colorFilter = ColorFilter.tint(JervisTheme.white),
                    )
                }
                Column(modifier = Modifier
                    .padding(start  = 24.dp, top = 16.dp, end = 24.dp, bottom = 16.dp)
                    .wrapContentHeight()
                ) {
                    ImportTeamFromFumbblDialogContent(
                        viewModel,
                        dialogColor,
                        textColor,
                        onCancel = { onCloseRequest() },
                    )
                }
            }
        }
    }
}

@Composable
private fun ColumnScope.ImportTeamFromFumbblDialogContent(
    viewModel: SelectTeamComponentModel,
    dialogColor: Color,
    textColor: Color,
    onCancel: () -> Unit = {},
) {
    var inputText by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }

    SettingsDialogHeader("Import Team From FUMBBL", dialogColor)
    TitleBorder(dialogColor)
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
    Row(
        modifier = Modifier.fillMaxWidth().padding(top = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.End
    ) {
        Spacer(modifier = Modifier.weight(1f))
        JervisButton(
            text = "Cancel",
            onClick = { onCancel() },
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
                        onCancel()
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
}

@Composable
private fun ColumnScope.SettingsDialogHeader(title: String, dialogColor: Color) {
    Box(
        modifier = Modifier.height(36.dp),
        contentAlignment = Alignment.CenterStart,
    ) {
        Text(
            modifier = Modifier.padding(bottom = 2.dp),
            text = title.uppercase(),
            fontWeight = FontWeight.Bold,
            fontSize = 16.sp,
            color = dialogColor
        )
    }
}
