package com.jervisffb.ui.menu.components.auth

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldColors
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.jervisffb.ui.auth.JervisAuthState
import com.jervisffb.ui.auth.JervisAuthUser
import com.jervisffb.ui.auth.SignUpOutcome
import com.jervisffb.ui.game.view.JervisTheme
import com.jervisffb.ui.game.view.JervisTheme.buttonTextColor
import com.jervisffb.ui.game.view.utils.JervisButton
import com.jervisffb.ui.game.viewmodel.MenuViewModel
import com.jervisffb.ui.menu.components.JervisDialog
import com.mmk.kmpauth.core.SignInState
import com.mmk.kmpauth.core.auth.KMPAuthUserCancelledException
import com.mmk.kmpauth.google.rememberGoogleAuthState
import com.mmk.kmpauth.uihelper.SignInButtonIconAlignment
import com.mmk.kmpauth.uihelper.google.GoogleSignInButton
import kotlinx.coroutines.launch

private enum class AuthenticationMode {
    SIGN_IN,
    SIGN_UP,
    RESET_PASSWORD,
}

@Composable
fun AuthenticationDialogComponent(menuViewModel: MenuViewModel) {
    val visible: Boolean by menuViewModel.isAuthenticationDialogVisible.collectAsState()
    if (!visible) return
    AuthenticationDialog(menuViewModel)
}

@Composable
private fun AuthenticationDialog(menuViewModel: MenuViewModel) {
    val authService = menuViewModel.authService
    val authState: JervisAuthState by authService.state.collectAsState()
    val coroutineScope = rememberCoroutineScope()
    var mode by remember { mutableStateOf(AuthenticationMode.SIGN_IN) }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var operationInProgress by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var statusMessage by remember { mutableStateOf<String?>(null) }
    var resetEmailSent by remember { mutableStateOf(false) }

    fun close() {
        password = ""
        errorMessage = null
        statusMessage = null
        menuViewModel.openAuthentication(false)
    }

    fun signIn() {
        if (operationInProgress) return
        operationInProgress = true
        errorMessage = null
        statusMessage = null
        coroutineScope.launch {
            authService.signInWithEmail(email, password).fold(
                onSuccess = {
                    password = ""
                    close()
                },
                onFailure = { errorMessage = it.authErrorMessage("Unable to sign in.") },
            )
            operationInProgress = false
        }
    }

    fun signUp() {
        if (operationInProgress) return
        operationInProgress = true
        errorMessage = null
        statusMessage = null
        coroutineScope.launch {
            authService.signUpWithEmail(email, password).fold(
                onSuccess = { outcome ->
                    password = ""
                    when (outcome) {
                        is SignUpOutcome.SignedIn -> close()
                        is SignUpOutcome.ConfirmationRequired -> {
                            email = outcome.email
                            mode = AuthenticationMode.SIGN_IN
                            statusMessage = "Check your email to confirm the account before signing in."
                        }
                    }
                },
                onFailure = { errorMessage = it.authErrorMessage("Unable to create the account.") },
            )
            operationInProgress = false
        }
    }

    fun resetPassword() {
        if (operationInProgress) return
        operationInProgress = true
        errorMessage = null
        coroutineScope.launch {
            authService.sendPasswordResetEmail(email).fold(
                onSuccess = {
                    resetEmailSent = true
                    statusMessage = "If an account exists for this email, a password reset message has been sent."
                },
                onFailure = { errorMessage = it.authErrorMessage("Unable to send the password reset email.") },
            )
            operationInProgress = false
        }
    }

    fun signOut() {
        if (operationInProgress) return
        operationInProgress = true
        errorMessage = null
        coroutineScope.launch {
            authService.signOut().fold(
                onSuccess = { close() },
                onFailure = { errorMessage = it.authErrorMessage("Unable to sign out.") },
            )
            operationInProgress = false
        }
    }

    val googleSignInState: SignInState? = when (
        authService.isGoogleSignInAvailable &&
            authState == JervisAuthState.SignedOut &&
            mode == AuthenticationMode.SIGN_IN
    ) {
        true -> rememberGoogleAuthState { result ->
            result.fold(
                onSuccess = {
                    password = ""
                    close()
                },
                onFailure = {
                    if (it !is KMPAuthUserCancelledException) {
                        errorMessage = it.authErrorMessage("Unable to sign in with Google.")
                    }
                },
            )
        }
        false -> null
    }
    val isBusy = operationInProgress || googleSignInState?.isInProgress == true

    JervisDialog(
        title = authenticationDialogTitle(authState, mode),
        icon = null,
        width = 560.dp,
        minHeight = 320.dp,
        backgroundScrim = true,
        content = { inputColors, textColor ->
            when (val state = authState) {
                JervisAuthState.Initializing -> InitializingContent(textColor)
                is JervisAuthState.Unavailable -> UnavailableContent(state.reason, textColor)
                JervisAuthState.SignedOut -> when (mode) {
                    AuthenticationMode.SIGN_IN -> SignInContent(
                        email = email,
                        password = password,
                        enabled = !isBusy,
                        googleSignInState = googleSignInState,
                        errorMessage = errorMessage,
                        statusMessage = statusMessage,
                        inputColors = inputColors,
                        textColor = textColor,
                        onEmailChange = { email = it },
                        onPasswordChange = { password = it },
                        onGoogleSignIn = { googleSignInState?.launch() },
                        onSignUpRequest = {
                            mode = AuthenticationMode.SIGN_UP
                            errorMessage = null
                            statusMessage = null
                        },
                        onResetRequest = {
                            mode = AuthenticationMode.RESET_PASSWORD
                            resetEmailSent = false
                            errorMessage = null
                            statusMessage = null
                            password = ""
                        },
                    )
                    AuthenticationMode.SIGN_UP -> EmailAndPasswordContent(
                        email = email,
                        password = password,
                        enabled = !isBusy,
                        errorMessage = errorMessage,
                        inputColors = inputColors,
                        textColor = textColor,
                        onEmailChange = { email = it },
                        onPasswordChange = { password = it },
                    )
                    AuthenticationMode.RESET_PASSWORD -> ResetPasswordContent(
                        email = email,
                        enabled = !isBusy,
                        emailSent = resetEmailSent,
                        errorMessage = errorMessage,
                        statusMessage = statusMessage,
                        inputColors = inputColors,
                        textColor = textColor,
                        onEmailChange = { email = it },
                    )
                }
                is JervisAuthState.SignedIn -> AccountContent(
                    user = state.user,
                    errorMessage = errorMessage,
                    textColor = textColor,
                )
            }
        },
        buttons = {
            when (authState) {
                JervisAuthState.Initializing,
                is JervisAuthState.Unavailable,
                -> {
                    Spacer(modifier = Modifier.weight(1f))
                    DialogButton("Close", enabled = !isBusy, onClick = ::close)
                }
                JervisAuthState.SignedOut -> when (mode) {
                    AuthenticationMode.SIGN_IN -> {
                        DialogButton("Cancel", enabled = !isBusy, onClick = ::close)
                        Spacer(modifier = Modifier.weight(1f))
                        DialogButton(
                            text = "Sign in",
                            enabled = !isBusy && email.isNotBlank() && password.isNotEmpty(),
                            onClick = ::signIn,
                        )
                    }
                    AuthenticationMode.SIGN_UP -> {
                        DialogButton(
                            text = "Back",
                            enabled = !isBusy,
                            onClick = {
                                password = ""
                                errorMessage = null
                                mode = AuthenticationMode.SIGN_IN
                            },
                        )
                        Spacer(modifier = Modifier.weight(1f))
                        DialogButton(
                            text = "Create account",
                            enabled = !isBusy && email.isNotBlank() && password.isNotEmpty(),
                            onClick = ::signUp,
                        )
                    }
                    AuthenticationMode.RESET_PASSWORD -> {
                        DialogButton(
                            text = when (resetEmailSent) {
                                true -> "Close"
                                false -> "Back"
                            },
                            enabled = !isBusy,
                            onClick = {
                                when (resetEmailSent) {
                                    true -> close()
                                    false -> {
                                        errorMessage = null
                                        statusMessage = null
                                        mode = AuthenticationMode.SIGN_IN
                                    }
                                }
                            },
                        )
                        if (!resetEmailSent) {
                            Spacer(modifier = Modifier.weight(1f))
                            DialogButton(
                                text = "Send reset email",
                                enabled = !isBusy && email.isNotBlank(),
                                onClick = ::resetPassword,
                            )
                        }
                    }
                }
                is JervisAuthState.SignedIn -> {
                    DialogButton("Close", enabled = !isBusy, onClick = ::close)
                    Spacer(modifier = Modifier.weight(1f))
                    DialogButton("Sign out", enabled = !isBusy, onClick = ::signOut)
                }
            }
        },
        onDismissRequest = when (isBusy) {
            true -> null
            false -> ::close
        },
    )
}

private fun authenticationDialogTitle(
    state: JervisAuthState,
    mode: AuthenticationMode,
): String = when (state) {
    JervisAuthState.Initializing -> "Login"
    is JervisAuthState.Unavailable -> "Login unavailable"
    JervisAuthState.SignedOut -> when (mode) {
        AuthenticationMode.SIGN_IN -> "Login to Jervis"
        AuthenticationMode.SIGN_UP -> "Create a Jervis account"
        AuthenticationMode.RESET_PASSWORD -> "Reset password"
    }
    is JervisAuthState.SignedIn -> "Jervis account"
}

@Composable
private fun ColumnScope.InitializingContent(textColor: Color) {
    Spacer(modifier = Modifier.weight(1f))
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        CircularProgressIndicator(
            modifier = Modifier.size(24.dp),
            color = JervisTheme.rulebookRed,
        )
        Spacer(modifier = Modifier.width(12.dp))
        Text("Restoring your session…", color = textColor)
    }
    Spacer(modifier = Modifier.weight(1f))
}

@Composable
private fun UnavailableContent(reason: String, textColor: Color) {
    Text(
        text = reason,
        color = textColor,
    )
    Spacer(modifier = Modifier.height(12.dp))
    Text(
        text = "Configure the Supabase URL and publishable key to enable login.",
        color = textColor.copy(alpha = 0.75f),
        fontSize = 12.sp,
    )
}

@Composable
private fun SignInContent(
    email: String,
    password: String,
    enabled: Boolean,
    googleSignInState: SignInState?,
    errorMessage: String?,
    statusMessage: String?,
    inputColors: @Composable (String) -> TextFieldColors,
    textColor: Color,
    onEmailChange: (String) -> Unit,
    onPasswordChange: (String) -> Unit,
    onGoogleSignIn: () -> Unit,
    onSignUpRequest: () -> Unit,
    onResetRequest: () -> Unit,
) {
    StatusMessages(statusMessage, errorMessage)
    EmailAndPasswordFields(
        email = email,
        password = password,
        enabled = enabled,
        inputColors = inputColors,
        onEmailChange = onEmailChange,
        onPasswordChange = onPasswordChange,
    )
    Spacer(modifier = Modifier.height(8.dp))
    Row(modifier = Modifier.fillMaxWidth()) {
        AuthTextLink("Create account", enabled, onSignUpRequest)
        Spacer(modifier = Modifier.weight(1f))
        AuthTextLink("Forgot password?", enabled, onResetRequest)
    }
    if (googleSignInState != null) {
        Spacer(modifier = Modifier.height(20.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            HorizontalDivider(modifier = Modifier.weight(1f), color = textColor.copy(alpha = 0.3f))
            Text(
                modifier = Modifier.padding(horizontal = 12.dp),
                text = "or",
                color = textColor.copy(alpha = 0.7f),
            )
            HorizontalDivider(modifier = Modifier.weight(1f), color = textColor.copy(alpha = 0.3f))
        }
        Spacer(modifier = Modifier.height(12.dp))
        GoogleSignInButton(
            modifier = Modifier.fillMaxWidth().height(44.dp),
            iconAlignment = SignInButtonIconAlignment.Start,
            onClick = {
                if (enabled && !googleSignInState.isInProgress) {
                    onGoogleSignIn()
                }
            },
        )
        if (googleSignInState.isInProgress) {
            Text(
                modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                text = "Waiting for Google sign-in…",
                color = textColor.copy(alpha = 0.75f),
                fontSize = 12.sp,
                textAlign = TextAlign.Center,
            )
        }
    }
}

@Composable
private fun EmailAndPasswordContent(
    email: String,
    password: String,
    enabled: Boolean,
    errorMessage: String?,
    inputColors: @Composable (String) -> TextFieldColors,
    textColor: Color,
    onEmailChange: (String) -> Unit,
    onPasswordChange: (String) -> Unit,
) {
    Text(
        text = "Create an account using your email address. You must confirm the address before signing in.",
        color = textColor,
    )
    Spacer(modifier = Modifier.height(12.dp))
    StatusMessages(statusMessage = null, errorMessage = errorMessage)
    EmailAndPasswordFields(
        email = email,
        password = password,
        enabled = enabled,
        inputColors = inputColors,
        onEmailChange = onEmailChange,
        onPasswordChange = onPasswordChange,
    )
}

@Composable
private fun EmailAndPasswordFields(
    email: String,
    password: String,
    enabled: Boolean,
    inputColors: @Composable (String) -> TextFieldColors,
    onEmailChange: (String) -> Unit,
    onPasswordChange: (String) -> Unit,
) {
    OutlinedTextField(
        modifier = Modifier.fillMaxWidth(),
        value = email,
        onValueChange = onEmailChange,
        label = { Text("Email") },
        singleLine = true,
        enabled = enabled,
        colors = inputColors(email),
    )
    Spacer(modifier = Modifier.height(8.dp))
    OutlinedTextField(
        modifier = Modifier.fillMaxWidth(),
        value = password,
        onValueChange = onPasswordChange,
        label = { Text("Password") },
        singleLine = true,
        enabled = enabled,
        visualTransformation = PasswordVisualTransformation(),
        colors = inputColors(password),
    )
}

@Composable
private fun ResetPasswordContent(
    email: String,
    enabled: Boolean,
    emailSent: Boolean,
    errorMessage: String?,
    statusMessage: String?,
    inputColors: @Composable (String) -> TextFieldColors,
    textColor: Color,
    onEmailChange: (String) -> Unit,
) {
    when (emailSent) {
        true -> StatusMessages(statusMessage, errorMessage)
        false -> {
            Text(
                text = "Enter your email address and we will send instructions for resetting your password.",
                color = textColor,
            )
            Spacer(modifier = Modifier.height(12.dp))
            StatusMessages(statusMessage, errorMessage)
            OutlinedTextField(
                modifier = Modifier.fillMaxWidth(),
                value = email,
                onValueChange = onEmailChange,
                label = { Text("Email") },
                singleLine = true,
                enabled = enabled,
                colors = inputColors(email),
            )
        }
    }
}

@Composable
private fun AccountContent(
    user: JervisAuthUser,
    errorMessage: String?,
    textColor: Color,
) {
    Text(
        text = "Signed in as",
        color = textColor.copy(alpha = 0.7f),
        fontSize = 12.sp,
    )
    val identity = user.displayName?.takeIf(String::isNotBlank)
        ?: user.email?.takeIf(String::isNotBlank)
        ?: "Jervis user"
    Text(
        modifier = Modifier.padding(top = 4.dp),
        text = identity,
        color = textColor,
        fontSize = 18.sp,
        fontWeight = FontWeight.Bold,
    )
    if (user.displayName.isNullOrBlank().not() && user.email.isNullOrBlank().not()) {
        Text(
            modifier = Modifier.padding(top = 4.dp),
            text = user.email.orEmpty(),
            color = textColor.copy(alpha = 0.75f),
        )
    }
    StatusMessages(statusMessage = null, errorMessage = errorMessage)
}

@Composable
private fun StatusMessages(statusMessage: String?, errorMessage: String?) {
    SelectionContainer {
        if (statusMessage != null) {
            Text(
                modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
                text = statusMessage,
                color = JervisTheme.rulebookGreen,
                fontSize = 13.sp,
            )
        }
        if (errorMessage != null) {
            Text(
                modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
                text = errorMessage,
                color = JervisTheme.rulebookRed,
                fontSize = 13.sp,
            )
        }
    }
}

@Composable
private fun AuthTextLink(text: String, enabled: Boolean, onClick: () -> Unit) {
    Text(
        modifier = Modifier
            .clickable(enabled = enabled, onClick = onClick)
            .padding(vertical = 4.dp),
        text = text,
        color = JervisTheme.rulebookBlue.copy(alpha = when (enabled) {
            true -> 1f
            false -> 0.5f
        }),
        fontSize = 13.sp,
        fontWeight = FontWeight.Medium,
    )
}

@Composable
private fun DialogButton(text: String, enabled: Boolean, onClick: () -> Unit) {
    JervisButton(
        text = text,
        enabled = enabled,
        onClick = onClick,
        buttonColor = JervisTheme.rulebookBlue,
        textColor = buttonTextColor,
    )
}

private fun Throwable.authErrorMessage(fallback: String): String =
    message?.takeIf(String::isNotBlank) ?: fallback
