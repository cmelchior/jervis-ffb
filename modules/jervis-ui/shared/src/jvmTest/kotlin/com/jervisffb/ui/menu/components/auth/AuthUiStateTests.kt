package com.jervisffb.ui.menu.components.auth

import com.jervisffb.ui.auth.JervisAuthService
import com.jervisffb.ui.auth.JervisAuthState
import com.jervisffb.ui.auth.JervisAuthUser
import com.jervisffb.ui.auth.SignUpOutcome
import com.jervisffb.ui.game.viewmodel.MenuViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class AuthUiStateTests {
    @Test
    fun topbarLabelUsesDisplayNameThenEmailThenAccount() {
        assertEquals("Coach", authTopbarLabel(signedIn(displayName = "Coach", email = "coach@example.com")))
        assertEquals("coach@example.com", authTopbarLabel(signedIn(displayName = null, email = "coach@example.com")))
        assertEquals("Account", authTopbarLabel(signedIn(displayName = " ", email = null)))
    }

    @Test
    fun topbarUsesLoginForEverySignedOutState() {
        assertEquals("Login", authTopbarLabel(JervisAuthState.Initializing))
        assertEquals("Login", authTopbarLabel(JervisAuthState.SignedOut))
        assertEquals("Login", authTopbarLabel(JervisAuthState.Unavailable("Missing configuration")))
    }

    @Test
    fun menuViewModelInitializesServiceAndControlsGlobalDialog() {
        val service = FakeAuthService()
        val viewModel = MenuViewModel(service)

        assertEquals(1, service.initializeCalls)
        assertFalse(viewModel.isAuthenticationDialogVisible.value)

        viewModel.openAuthentication()
        assertTrue(viewModel.isAuthenticationDialogVisible.value)

        viewModel.openAuthentication(false)
        assertFalse(viewModel.isAuthenticationDialogVisible.value)
    }

    private fun signedIn(displayName: String?, email: String?): JervisAuthState.SignedIn =
        JervisAuthState.SignedIn(
            JervisAuthUser(
                id = "user-id",
                email = email,
                displayName = displayName,
                photoUrl = null,
            ),
        )

    private class FakeAuthService : JervisAuthService {
        override val state: StateFlow<JervisAuthState> = MutableStateFlow(JervisAuthState.SignedOut)
        override val isGoogleSignInAvailable: Boolean = false
        var initializeCalls: Int = 0

        override fun initialize() {
            initializeCalls++
        }

        override suspend fun signInWithEmail(email: String, password: String): Result<JervisAuthUser> =
            error("Not used")

        override suspend fun signUpWithEmail(email: String, password: String): Result<SignUpOutcome> =
            error("Not used")

        override suspend fun sendPasswordResetEmail(email: String): Result<Unit> = error("Not used")

        override suspend fun signOut(): Result<Unit> = error("Not used")
    }
}
