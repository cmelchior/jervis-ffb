package com.jervisffb.ui.menu.components.auth

import androidx.compose.foundation.layout.widthIn
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.jervisffb.ui.auth.JervisAuthState
import com.jervisffb.ui.game.viewmodel.MenuViewModel
import com.jervisffb.ui.menu.TopbarButton

@Composable
fun AuthTopbarButton(menuViewModel: MenuViewModel) {
    val authState: JervisAuthState by menuViewModel.authService.state.collectAsState()
    TopbarButton(
        title = authTopbarLabel(authState),
        modifier = Modifier.widthIn(max = 180.dp),
        onClick = { menuViewModel.openAuthentication() },
    )
}

internal fun authTopbarLabel(state: JervisAuthState): String = when (state) {
    JervisAuthState.Initializing,
    is JervisAuthState.Unavailable,
    JervisAuthState.SignedOut,
    -> "Login"

    is JervisAuthState.SignedIn -> state.user.displayName
        ?.trim()
        ?.takeIf(String::isNotEmpty)
        ?: state.user.email
            ?.trim()
            ?.takeIf(String::isNotEmpty)
        ?: "Account"
}
