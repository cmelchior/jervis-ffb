package com.jervisffb.ui.auth

import com.jervisffb.BuildConfig
import kotlinx.coroutines.flow.StateFlow

data class JervisAuthConfiguration(
    val supabaseUrl: String,
    val supabasePublishableKey: String,
    val googleWebClientId: String,
    val googleIosClientId: String,
    val googleDesktopRedirectUrl: String = "http://localhost:8765/callback",
) {
    val isSupabaseConfigured: Boolean
        get() = supabaseUrl.isNotBlank() && supabasePublishableKey.isNotBlank()

    val isGoogleConfigured: Boolean
        get() = googleWebClientId.isNotBlank()

    companion object {
        fun fromBuildConfig(): JervisAuthConfiguration = JervisAuthConfiguration(
            supabaseUrl = BuildConfig.supabaseUrl.trim(),
            supabasePublishableKey = BuildConfig.supabasePublishableKey.trim(),
            googleWebClientId = BuildConfig.googleWebClientId.trim(),
            googleIosClientId = BuildConfig.googleIosClientId.trim(),
        )
    }
}

data class JervisAuthUser(
    val id: String,
    val email: String?,
    val displayName: String?,
    val photoUrl: String?,
)

sealed interface JervisAuthState {
    data object Initializing : JervisAuthState

    data class Unavailable(val reason: String) : JervisAuthState

    data object SignedOut : JervisAuthState

    data class SignedIn(val user: JervisAuthUser) : JervisAuthState
}

sealed interface SignUpOutcome {
    data class SignedIn(val user: JervisAuthUser) : SignUpOutcome

    data class ConfirmationRequired(val email: String) : SignUpOutcome
}

interface JervisAuthService {
    val state: StateFlow<JervisAuthState>
    val isGoogleSignInAvailable: Boolean

    fun initialize()

    suspend fun signInWithEmail(email: String, password: String): Result<JervisAuthUser>

    suspend fun signUpWithEmail(email: String, password: String): Result<SignUpOutcome>

    suspend fun sendPasswordResetEmail(email: String): Result<Unit>

    suspend fun signOut(): Result<Unit>
}

object JervisAuthentication {
    val service: JervisAuthService by lazy {
        SupabaseJervisAuthService(JervisAuthConfiguration.fromBuildConfig())
    }
}
