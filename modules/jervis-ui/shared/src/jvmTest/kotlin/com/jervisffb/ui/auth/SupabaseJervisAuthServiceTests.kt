package com.jervisffb.ui.auth

import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertIs
import kotlin.test.assertTrue

class SupabaseJervisAuthServiceTests {
    @Test
    fun missingSupabaseConfigurationDisablesAuthentication() = runTest {
        val service = SupabaseJervisAuthService(
            JervisAuthConfiguration(
                supabaseUrl = "",
                supabasePublishableKey = "",
                googleWebClientId = "",
                googleIosClientId = "",
            ),
        )

        service.initialize()

        assertIs<JervisAuthState.Unavailable>(service.state.value)
        assertFalse(service.isGoogleSignInAvailable)
        assertTrue(service.signInWithEmail("coach@example.com", "password").isFailure)
    }

    @Test
    fun googleRequiresSupabaseAndGoogleConfiguration() {
        val missingSupabase = SupabaseJervisAuthService(
            JervisAuthConfiguration("", "", "web-client-id", "ios-client-id"),
        )
        val missingGoogle = SupabaseJervisAuthService(
            JervisAuthConfiguration("https://example.supabase.co", "publishable-key", "", ""),
        )

        assertFalse(missingSupabase.isGoogleSignInAvailable)
        assertFalse(missingGoogle.isGoogleSignInAvailable)
    }
}
