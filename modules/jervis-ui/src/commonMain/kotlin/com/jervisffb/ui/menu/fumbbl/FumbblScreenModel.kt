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

    // Internal tracking of UI state
    private val _showReplayDialog = MutableStateFlow<Boolean>(false)
    private val _coachName = MutableStateFlow("")
    private val _clientId = MutableStateFlow("")
    private val _clientSecret = MutableStateFlow("")
    private val _showLoginDialog = MutableStateFlow<Boolean>(false)
    private val _loginError = MutableStateFlow<String?>(null)
    private val _loginButtonAvailable = MutableStateFlow<Boolean>(false)
    private val _isLoggedIn = MutableStateFlow<Boolean>(false)

    // State flows for the UI state
    fun isReplayDialogVisible(): StateFlow<Boolean> = _showReplayDialog
    fun showLoginDialog(): StateFlow<Boolean> = _showLoginDialog
    fun loginError(): StateFlow<String?> = _loginError
    fun loginButtonAvailable(): StateFlow<Boolean> = _loginButtonAvailable
    fun coachName(): StateFlow<String> = _coachName
    fun clientId(): StateFlow<String> = _clientId
    fun clientSecret(): StateFlow<String> = _clientSecret
    fun loggedInState(): StateFlow<Boolean> = _isLoggedIn

    suspend fun initialize() {
        // Check for persisted credentials, if found we assume they are still valid
        // and mark the user as logged in (if they are too old, they will be refreshed
        // the first time they are needed
        SETTINGS_MANAGER.let { propManager ->
            _coachName.value = propManager.getString(SettingsKeys.FUMBBL_CLIENT_NAME, "")
            _clientId.value = propManager.getString(SettingsKeys.FUMBBL_CLIENT_ID, "")
            _clientSecret.value = propManager.getString(SettingsKeys.FUMBBL_CLIENT_SECRET, "")
            oauthToken = propManager.getString(SettingsKeys.FUMBBL_OAUTH_TOKEN, "")
            _isLoggedIn.value = oauthToken.isNotBlank()
        }
    }

    // User actions from the UI that involves the login dialog UI
    fun loginDialogAction(action: LoginDialogAction) {
        when (action) {
            LoginDialogAction.CANCEL -> _showLoginDialog.value = false
            LoginDialogAction.CREATE_TOKEN -> {
                val success = openUrlInBrowser(URL_OAUTH)
                if (!success) {
                    _loginError.value = "Could not open the browser. Go to $URL_OAUTH manually."
                } else {
                    _loginError.value = null
                }
            }
            LoginDialogAction.LOGIN -> {
                val coachName = _coachName.value
                api = FumbblApi(coachName)
                menuViewModel.navigatorContext.launch {
                    api!!.run {
                        authenticate(_clientId.value, _clientSecret.value)
                            .onSuccess {
                                updateOAuthToken(it.accessToken)
                                updatePersistedAuthData(
                                    oauthToken = oauthToken,
                                    coachName = _coachName.value,
                                    clientId = _clientId.value,
                                    clientSecret = _clientSecret.value,
                                )
                                _loginError.value = null
                                _showLoginDialog.value = false
                                _isLoggedIn.value = true
                            }
                            .onFailure {
                                _loginError.value = "Failed to log in: ${it.message}"
                                _isLoggedIn.value = false
                            }
                    }
                }
            }

            LoginDialogAction.LOGOUT -> {
                // TODO Clear credentials
                _isLoggedIn.value = false
                _showLoginDialog.value = false
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
        when (_isLoggedIn.value) {
            true -> {
                _loginButtonAvailable.value = isLoginInfoValid()
                _showLoginDialog.value = true
            }
            false -> {
                _showLoginDialog.value = true
            }
        }
    }

    fun updateClientId(clientId: String) {
        this._clientId.value = clientId
        _loginButtonAvailable.value = isLoginInfoValid()
    }

    fun updateClientSecret(clientSecret: String) {
        this._clientSecret.value = clientSecret
        _loginButtonAvailable.value = isLoginInfoValid()
    }

    fun updateCoachName(coachName: String) {
        this._coachName.value = coachName
        _loginButtonAvailable.value = isLoginInfoValid()
    }

    private fun updateOAuthToken(token: String) {
        oauthToken = token
    }

    private fun isLoginInfoValid(): Boolean {
        return _clientId.value.isNotBlank() && _clientSecret.value.isNotBlank() && _coachName.value.isNotBlank()
    }

    fun getCoachUrl(): String {
        return "$URL_COACH_PAGE/~${_coachName.value}"
    }

    fun openReplayDialog(show: Boolean) {
        _showReplayDialog.value = show
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
