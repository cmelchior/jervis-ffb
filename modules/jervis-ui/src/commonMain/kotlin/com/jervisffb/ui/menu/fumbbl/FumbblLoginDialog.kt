package com.jervisffb.ui.menu.fumbbl

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldColors
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.jervisffb.jervis_ui.generated.resources.Res
import com.jervisffb.jervis_ui.generated.resources.logo_fumbbl_small
import com.jervisffb.ui.game.view.JervisTheme
import com.jervisffb.ui.game.view.JervisTheme.buttonTextColor
import com.jervisffb.ui.game.view.utils.JervisButton
import com.jervisffb.ui.menu.components.JervisDialog
import org.jetbrains.compose.resources.painterResource

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun FumbblLoginDialog(viewModel: FumbblScreenModel) {
    val showDialog: Boolean by viewModel.showLoginDialog().collectAsState()
    val isLoggedIn: Boolean by viewModel.loggedInState().collectAsState()
    if (!showDialog) return
    val icon = @Composable {
        Image(
            modifier = Modifier.fillMaxWidth().padding(24.dp),
            painter = painterResource(Res.drawable.logo_fumbbl_small),
            contentDescription = "",
            colorFilter = ColorFilter.tint(JervisTheme.white),
        )
    }
    when (isLoggedIn) {
        true -> LogoutDialog(viewModel, icon)
        false -> LoginDialog(viewModel, icon)
    }
}

@Composable
private fun LogoutDialog(viewModel: FumbblScreenModel, icon: @Composable () -> Unit) {
    val coachName: String by viewModel.coachName().collectAsState()
    JervisDialog(
        title = "Log out of FUMBBL",
        icon = icon,
        width = 650.dp,
        content = { inputDialogColors: @Composable (String) -> TextFieldColors, textColor: Color ->
            Text(
                text = "You are logged in as ${coachName}. Would you like to log out?",
                color = textColor
            )
        },
        buttons = {
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
    )
}

@Composable
private fun LoginDialog(viewModel: FumbblScreenModel, icon: @Composable () -> Unit,) {
    val focusManager = LocalFocusManager.current
    val loginError: String? by viewModel.loginError().collectAsState()
    val isLoginAvailable: Boolean by viewModel.loginButtonAvailable().collectAsState()
    val coachName: String by viewModel.coachName().collectAsState()
    val clientID by viewModel.clientId().collectAsState()
    val clientSecret by viewModel.clientSecret().collectAsState()
    JervisDialog(
        title = "Log in to FUMBBL",
        icon = icon,
        content = { inputDialogColors: @Composable (String) -> TextFieldColors, textColor: Color ->
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
            if (loginError?.isNotEmpty() == true) {
                Text(
                    text = loginError ?: "",
                    fontSize = 12.sp,
                    color = JervisTheme.rulebookRed,
                )
            }
            Text(
                text = """
                        To log in to FUMBBL through the Jervis Client, you need to create an OAuth Application 
                        on the FUMBBL website. Follow the link below, create a new application and copy 
                        "Client ID" and "Client Secret" back here.
                """.trimIndent().replace("\n", ""),
                fontSize = 12.sp,
                color = textColor
            )

        },
        buttons = {
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
    )
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
