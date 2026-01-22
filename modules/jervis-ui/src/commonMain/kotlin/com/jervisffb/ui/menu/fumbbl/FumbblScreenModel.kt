package com.jervisffb.ui.menu.fumbbl

import cafe.adriel.voyager.core.model.ScreenModel
import com.jervis.generated.SettingsKeys
import com.jervisffb.fumbbl.web.FumbblApi
import com.jervisffb.ui.SETTINGS_MANAGER
import com.jervisffb.ui.game.viewmodel.MenuViewModel
import com.jervisffb.utils.isRegularFile
import com.jervisffb.utils.openUrlInBrowser
import com.jervisffb.utils.platformFileSystem
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import okio.Path
import okio.Path.Companion.toPath

class FumbblScreenModel(private val menuViewModel: MenuViewModel) : ScreenModel {

    val URL_GAMEFINDER = "https://fumbbl.com/p/lfg2"
    val URL_HELP = "https://fumbbl.com/help"
    val URL_NEWS = "https://fumbbl.com"

    // Url to page where the OAuth token can be created by the coach
    private val URL_COACH_PAGE = "https://fumbbl.com"
    private val URL_OAUTH = "https://fumbbl.com/p/oauth"

    enum class LoginDialogAction {
        CANCEL,
        CREATE_TOKEN,
        LOGIN,
        LOGOUT,
    }

    private var oauthToken: String = ""
    private var api: FumbblApi? = null

    // State flows for the UI state
    val isReplayDialogVisible: StateFlow<Boolean>
        field = MutableStateFlow<Boolean>(false)
    val coachName: StateFlow<String>
        field = MutableStateFlow("")
    val clientId: StateFlow<String>
        field = MutableStateFlow("")
    val clientSecret: StateFlow<String>
        field = MutableStateFlow("")
    val showLoginDialog: StateFlow<Boolean>
        field = MutableStateFlow(false)
    val loginError: StateFlow<String?>
        field = MutableStateFlow<String?>(null)
    val loginButtonAvailable: StateFlow<Boolean>
        field = MutableStateFlow(false)
    val isLoggedIn: StateFlow<Boolean>
        field = MutableStateFlow(false)

    suspend fun initialize() {
        // Check for persisted credentials, if found we assume they are still valid
        // and mark the user as logged in (if they are too old, they will be refreshed
        // the first time they are needed
        SETTINGS_MANAGER.let { propManager ->
            coachName.value = propManager.getString(SettingsKeys.FUMBBL_CLIENT_NAME, "")
            clientId.value = propManager.getString(SettingsKeys.FUMBBL_CLIENT_ID, "")
            clientSecret.value = propManager.getString(SettingsKeys.FUMBBL_CLIENT_SECRET, "")
            oauthToken = propManager.getString(SettingsKeys.FUMBBL_OAUTH_TOKEN, "")
            isLoggedIn.value = oauthToken.isNotBlank()
        }
    }

    // User actions from the UI that involves the login dialog UI
    fun loginDialogAction(action: LoginDialogAction) {
        when (action) {
            LoginDialogAction.CANCEL -> showLoginDialog.value = false
            LoginDialogAction.CREATE_TOKEN -> {
                val success = openUrlInBrowser(URL_OAUTH)
                if (!success) {
                    loginError.value = "Could not open the browser. Go to $URL_OAUTH manually."
                } else {
                    loginError.value = null
                }
            }
            LoginDialogAction.LOGIN -> {
                val coachName = coachName.value
                api = FumbblApi(coachName)
                menuViewModel.navigatorContext.launch {
                    api!!.run {
                        authenticate(clientId.value, clientSecret.value)
                            .onSuccess {
                                updateOAuthToken(it.accessToken)
                                updatePersistedAuthData(
                                    oauthToken = oauthToken,
                                    coachName = this@FumbblScreenModel.coachName.value,
                                    clientId = clientId.value,
                                    clientSecret = clientSecret.value,
                                )
                                loginError.value = null
                                showLoginDialog.value = false
                                isLoggedIn.value = true
                            }
                            .onFailure {
                                loginError.value = "Failed to log in: ${it.message}"
                                isLoggedIn.value = false
                            }
                    }
                }
            }

            LoginDialogAction.LOGOUT -> {
                // TODO Clear credentials
                isLoggedIn.value = false
                showLoginDialog.value = false
                updateOAuthToken("")
                updateCoachName("")
                updateClientId("")
                updateClientSecret("")
                updatePersistedAuthData(
                    oauthToken = "",
                    coachName = "",
                    clientId = "",
                    clientSecret = "",
                )
            }
        }
    }

    private fun updatePersistedAuthData(oauthToken: String, coachName: String, clientId: String, clientSecret: String) {
        menuViewModel.navigatorContext.launch {
            SETTINGS_MANAGER.run {
                put(SettingsKeys.FUMBBL_OAUTH_TOKEN, oauthToken)
                put(SettingsKeys.FUMBBL_CLIENT_NAME, coachName)
                put(SettingsKeys.FUMBBL_CLIENT_ID, clientId)
                put(SettingsKeys.FUMBBL_CLIENT_SECRET, clientSecret)
            }
        }
    }

    fun authMenubarActionInitiated() {
        when (isLoggedIn.value) {
            true -> {
                loginButtonAvailable.value = isLoginInfoValid()
                showLoginDialog.value = true
            }
            false -> {
                showLoginDialog.value = true
            }
        }
    }

    fun updateClientId(clientId: String) {
        this.clientId.value = clientId
        loginButtonAvailable.value = isLoginInfoValid()
    }

    fun updateClientSecret(clientSecret: String) {
        this.clientSecret.value = clientSecret
        loginButtonAvailable.value = isLoginInfoValid()
    }

    fun updateCoachName(coachName: String) {
        this.coachName.value = coachName
        loginButtonAvailable.value = isLoginInfoValid()
    }

    private fun updateOAuthToken(token: String) {
        oauthToken = token
    }

    private fun isLoginInfoValid(): Boolean {
        return clientId.value.isNotBlank() && clientSecret.value.isNotBlank() && coachName.value.isNotBlank()
    }

    fun getCoachUrl(): String {
        return "$URL_COACH_PAGE/~${coachName.value}"
    }

    fun openReplayDialog(show: Boolean) {
        isReplayDialogVisible.value = show
    }

    val availableReplayFiles: List<Path>
        get() {
            val dir = "/Users/christian.melchior/Private/jervis-ffb/replays-fumbbl".toPath()
            return if (!platformFileSystem.exists(dir)) {
                emptyList()
            } else {
                platformFileSystem.list(dir)
                    .filter { it.isRegularFile }
                    .filter { it.name.endsWith(".json") }
            }
        }
}
