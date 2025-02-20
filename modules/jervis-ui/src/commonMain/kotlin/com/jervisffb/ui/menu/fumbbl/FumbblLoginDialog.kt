package com.jervisffb.ui.menu.fumbbl

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
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
import androidx.compose.material.OutlinedTextField
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.material.TextFieldColors
import androidx.compose.material.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.focus.FocusManager
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onPreviewKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.platform.LocalFocusManager
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
import com.jervisffb.ui.game.view.utils.paperBackground
import org.jetbrains.compose.resources.painterResource

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun FumbblLoginDialog(viewModel: FumbblScreenModel) {
    val showDialog: Boolean by viewModel.showLoginDialog().collectAsState()
    val isLoggedIn: Boolean by viewModel.loggedInState().collectAsState()

    val dialogColor = JervisTheme.rulebookRed
    val textColor = JervisTheme.contentTextColor
    val buttonTextColor = JervisTheme.white
    val inputDialogColors: @Composable (String) -> TextFieldColors = { text: String ->
        TextFieldDefaults.outlinedTextFieldColors(
            focusedLabelColor = dialogColor,
            focusedBorderColor = dialogColor,
            unfocusedLabelColor = if (text.isEmpty()) textColor.copy(alpha = 0.4f) else textColor,
            unfocusedBorderColor = textColor,
            textColor = textColor,
        )
    }

    if (!showDialog) return

    Dialog(
        onDismissRequest = { /* Do not allow dismissing the dialog */ },
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
                    .background(dialogColor)
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
                    if (isLoggedIn) {
                        LogOutDialogContent(
                            viewModel = viewModel,
                            dialogColor,
                            textColor,
                            inputDialogColors
                        )
                    } else {
                        LogInDialogContent(
                            viewModel = viewModel,
                            dialogColor,
                            textColor,
                            inputDialogColors
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ColumnScope.LogOutDialogContent(
    viewModel: FumbblScreenModel,
    dialogColor: Color,
    textColor: Color,
    inputDialogColors: @Composable ((String) -> TextFieldColors)
) {
    val coachName: String by viewModel.coachName().collectAsState()

    LoginDialogHeader("Log out of FUMBBL", dialogColor)
    TitleBorder(dialogColor)
    Column(modifier = Modifier.weight(1f).padding(top = 8.dp)) {
        Text(
            text = """
                    You are logged in as ${coachName}. Would you like to log out?
                """.trimIndent(),
            color = textColor
        )
    }
    Row(
        modifier = Modifier.fillMaxWidth().padding(top = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.End
    ) {
        JervisButton(
            text = "Cancel",
            onClick = { viewModel.loginDialogAction(FumbblScreenModel.LoginDialogAction.CANCEL) },
            buttonColor = JervisTheme.rulebookBlue,
            textColor = buttonTextColor
        )
        Spacer(modifier = Modifier.weight(1f))
        JervisButton(
            text = "Logout",
            onClick = { viewModel.loginDialogAction(FumbblScreenModel.LoginDialogAction.LOGOUT) },
            buttonColor = JervisTheme.rulebookBlue,
            textColor = buttonTextColor,
        )
    }
}

@Composable
private fun ColumnScope.LogInDialogContent(viewModel: FumbblScreenModel, dialogColor: Color, textColor: Color, inputDialogColors: @Composable ((String) -> TextFieldColors)) {
    val focusManager = LocalFocusManager.current
    val loginError: String? by viewModel.loginError().collectAsState()
    val isLoginAvailable: Boolean by viewModel.loginButtonAvailable().collectAsState()
    val coachName: String by viewModel.coachName().collectAsState()
    val clientID by viewModel.clientId().collectAsState()
    val clientSecret by viewModel.clientSecret().collectAsState()

    LoginDialogHeader("Log in to FUMBBL", dialogColor)
    TitleBorder(dialogColor)
    Column(modifier = Modifier.weight(1f).padding(top = 8.dp)) {
        LoginInputField(
            focusManager = focusManager,
            value = coachName,
            onValueChange = { viewModel.updateCoachName(it) },
            label = "Username / Coach Name",
            colors = inputDialogColors(coachName),
        )
        Spacer(modifier = Modifier.height(4.dp))
        LoginInputField(
            focusManager = focusManager,
            value = clientID,
            onValueChange = { viewModel.updateClientId(it) },
            label = "Client ID",
            colors = inputDialogColors(clientID),
        )
        Spacer(modifier = Modifier.height(4.dp))
        LoginInputField(
            focusManager = focusManager,
            value = clientSecret,
            onValueChange = { viewModel.updateClientSecret(it) },
            label = "Client Secret",
            colors = inputDialogColors(clientSecret),
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = """
                    To log in to FUMBBL through the Jervis Client, you need to create an OAuth Application
                    on the FUMBBL website. Follow the link below, create a new application and copy 
                    "Client ID" and "Client Secret" back here.
                """.trimIndent().replace("\n", ""),
            fontSize = 12.sp,
            color = textColor
        )
    }
    Row(
        modifier = Modifier.fillMaxWidth().padding(top = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.End
    ) {
        JervisButton(
            text = "Cancel",
            onClick = { viewModel.loginDialogAction(FumbblScreenModel.LoginDialogAction.CANCEL) },
            buttonColor = JervisTheme.rulebookBlue,
            textColor = buttonTextColor
        )
        Spacer(modifier = Modifier.weight(1f))
        JervisButton(
            text = "Create Secret",
            onClick = { viewModel.loginDialogAction(FumbblScreenModel.LoginDialogAction.CREATE_TOKEN) },
            buttonColor = JervisTheme.rulebookBlue,
            textColor = buttonTextColor
        )
        Spacer(modifier = Modifier.width(8.dp))
        JervisButton(
            text = "Login",
            onClick = { viewModel.loginDialogAction(FumbblScreenModel.LoginDialogAction.LOGIN) },
            buttonColor = JervisTheme.rulebookBlue,
            textColor = buttonTextColor,
            enabled = isLoginAvailable
        )
    }
}

@Composable
private fun ColumnScope.LoginInputField(
    focusManager: FocusManager,
    value: String,
    onValueChange: (value: String) -> Unit,
    label: String,
    colors: TextFieldColors
) {
    OutlinedTextField(
        modifier = Modifier
            .fillMaxWidth()
            .onPreviewKeyEvent {
                if (it.key == Key.Tab && it.type == KeyEventType.KeyDown) {
                    focusManager.moveFocus(FocusDirection.Next)
                    true
                } else {
                    false
                }
            }
        ,
        value = value,
        onValueChange = { value -> onValueChange(value) },
        label = { Text(label) },
        colors = colors,
    )
}

@Composable
private fun ColumnScope.LoginDialogHeader(title: String, dialogColor: Color) {
    Box(
        modifier = Modifier.height(36.dp),
        contentAlignment = Alignment.CenterStart,
    ) {
        Text(
            modifier = Modifier.padding(bottom = 2.dp),
            text = "FUMBBL Login".uppercase(),
            fontWeight = FontWeight.Bold,
            fontSize = 16.sp,
            color = dialogColor
        )
    }
}
