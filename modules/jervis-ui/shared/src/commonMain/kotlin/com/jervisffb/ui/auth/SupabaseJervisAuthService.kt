package com.jervisffb.ui.auth

import com.mmk.kmpauth.core.KMPAuth
import com.mmk.kmpauth.core.auth.AuthCredential
import com.mmk.kmpauth.core.auth.KMPAuthUser
import com.mmk.kmpauth.google.google
import com.mmk.kmpauth.supabase.supabase
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.Auth
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.createSupabaseClient
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineName
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class SupabaseJervisAuthService(
    private val configuration: JervisAuthConfiguration,
    private val scope: CoroutineScope = CoroutineScope(
        SupervisorJob() + Dispatchers.Default + CoroutineName("JervisAuthentication"),
    ),
) : JervisAuthService {
    private val mutableState = MutableStateFlow<JervisAuthState>(JervisAuthState.Initializing)
    private var initializationStarted = false
    private var client: SupabaseClient? = null

    override val state: StateFlow<JervisAuthState> = mutableState

    override val isGoogleSignInAvailable: Boolean =
        configuration.isSupabaseConfigured && configuration.isGoogleConfigured

    override fun initialize() {
        if (initializationStarted) return
        initializationStarted = true

        if (!configuration.isSupabaseConfigured) {
            mutableState.value = JervisAuthState.Unavailable(
                "Authentication is not configured for this build.",
            )
            return
        }

        val supabaseClient = try {
            createSupabaseClient(
                supabaseUrl = configuration.supabaseUrl,
                supabaseKey = configuration.supabasePublishableKey,
            ) {
                install(Auth)
            }
        } catch (error: Throwable) {
            mutableState.value = initializationFailure(error)
            return
        }
        client = supabaseClient

        try {
            KMPAuth.initialize {
                if (configuration.isGoogleConfigured) {
                    google(
                        serverId = configuration.googleWebClientId,
                        redirectUri = configuration.googleDesktopRedirectUrl,
                    )
                }
                supabase(supabaseClient)
            }
        } catch (error: Throwable) {
            mutableState.value = initializationFailure(error)
            return
        }

        scope.launch {
            try {
                supabaseClient.auth.awaitInitialization()
                updateState(KMPAuth.currentUser())
                KMPAuth.currentUserFlow.collect(::updateState)
            } catch (error: CancellationException) {
                throw error
            } catch (error: Throwable) {
                mutableState.value = initializationFailure(error)
            }
        }
    }

    override suspend fun signInWithEmail(
        email: String,
        password: String,
    ): Result<JervisAuthUser> = backendOrFailure()?.let { return Result.failure(it) }
        ?: KMPAuth.signIn(AuthCredential.EmailPassword(email.trim(), password)).map(::toJervisUser)

    override suspend fun signUpWithEmail(
        email: String,
        password: String,
    ): Result<SignUpOutcome> {
        backendOrFailure()?.let { return Result.failure(it) }
        val normalizedEmail = email.trim()
        return KMPAuth.signUp(normalizedEmail, password).map { user ->
            when (KMPAuth.currentUser()?.uid == user.uid) {
                true -> SignUpOutcome.SignedIn(toJervisUser(user))
                false -> SignUpOutcome.ConfirmationRequired(normalizedEmail)
            }
        }
    }

    override suspend fun sendPasswordResetEmail(email: String): Result<Unit> {
        backendOrFailure()?.let { return Result.failure(it) }
        return KMPAuth.sendPasswordResetEmail(email.trim())
    }

    override suspend fun signOut(): Result<Unit> {
        backendOrFailure()?.let { return Result.failure(it) }
        return try {
            KMPAuth.signOut()
            Result.success(Unit)
        } catch (error: CancellationException) {
            throw error
        } catch (error: Throwable) {
            Result.failure(error)
        }
    }

    private fun backendOrFailure(): Throwable? = when (val currentState = mutableState.value) {
        JervisAuthState.Initializing -> IllegalStateException("Authentication is still initializing.")
        is JervisAuthState.Unavailable -> IllegalStateException(currentState.reason)
        JervisAuthState.SignedOut,
        is JervisAuthState.SignedIn,
        -> when (client) {
            null -> IllegalStateException("Authentication is unavailable.")
            else -> null
        }
    }

    private fun updateState(user: KMPAuthUser?) {
        mutableState.value = when (user) {
            null -> JervisAuthState.SignedOut
            else -> JervisAuthState.SignedIn(toJervisUser(user))
        }
    }

    private fun toJervisUser(user: KMPAuthUser): JervisAuthUser = JervisAuthUser(
        id = user.uid,
        email = user.email,
        displayName = user.displayName,
        photoUrl = user.photoUrl,
    )

    private fun initializationFailure(error: Throwable): JervisAuthState.Unavailable =
        JervisAuthState.Unavailable(
            reason = error.message
                ?.takeIf(String::isNotBlank)
                ?.let { "Authentication could not be initialized: $it" }
                ?: "Authentication could not be initialized.",
        )
}
