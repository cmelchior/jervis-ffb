package com.jervisffb.ui.menu.p2p.client

import cafe.adriel.voyager.core.model.ScreenModel
import com.jervisffb.engine.model.Coach
import com.jervisffb.engine.model.CoachId
import com.jervisffb.net.GameId
import com.jervisffb.net.JervisExitCode
import com.jervisffb.ui.PROPERTIES_MANAGER
import com.jervisffb.ui.game.viewmodel.MenuViewModel
import com.jervisffb.ui.menu.components.coach.CoachSetupComponentModel
import com.jervisffb.ui.menu.p2p.AbstractClintNetworkMessageHandler
import com.jervisffb.utils.PROP_DEFAULT_CLIENT_COACH_NAME
import io.ktor.http.Url
import io.ktor.websocket.CloseReason
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

/**
 * ViewModel class for the "Join Host" subpage. This is not a full screen,
 * but is a part of a flow when joining a Peer-to-Peer host.
 *
 * The full flow is controlled in [P2PClientScreenModel]
 */
class JoinHostScreenModel(private val menuViewModel: MenuViewModel, private val model: P2PClientScreenModel) : ScreenModel {

    // Overall state of the screen
    enum class JoinState {
        INVALID_URL, // Client not joined, and cannot join because the URL is not a valid URL. Join Button is not available
        READY_JOIN, // Client has entered a valid URL. Join button should be available.
        JOINING, // Client is in the process of connecting to the entered URL
        JOINED, // Client has connected to the host
    }

    val coachSetupModel = CoachSetupComponentModel(menuViewModel) { coachName ->
        checkForValidGameUrl()
    }
    private val _gameUrl = MutableStateFlow("")
    private val _serverIp = MutableStateFlow("")
    private val _port = MutableStateFlow("")
    private val _gameId = MutableStateFlow("")
    private val _joinState = MutableStateFlow(JoinState.INVALID_URL)
    private val _joinMessage = MutableStateFlow("")
    private val _joinError = MutableStateFlow("")

    fun gameUrl(): StateFlow<String> = _gameUrl
    fun serverIp(): StateFlow<String> = _serverIp
    fun port(): StateFlow<String> = _port
    fun gameId(): StateFlow<String> = _gameId
    fun canJoin(): StateFlow<JoinState> = _joinState
    fun joinMessage(): StateFlow<String> = _joinMessage
    fun joinError(): StateFlow<String> = _joinError

    @OptIn(ExperimentalUuidApi::class)
    fun getCoach(): Coach? {
        val name = coachSetupModel.coachName.value
        return if (name.isNotBlank()) {
            Coach(CoachId(Uuid.random().toString()), name, coachSetupModel.coachType.value)
        } else {
            null
        }
    }

    init {
        menuViewModel.navigatorContext.launch {
            PROPERTIES_MANAGER.getString(PROP_DEFAULT_CLIENT_COACH_NAME)?.let {
                coachSetupModel.updateCoachName(it)
            }
        }
    }

    fun updateGameUrl(gameUrl: String, updateOtherFields: Boolean = true) {
        _gameUrl.value = gameUrl
        if (updateOtherFields) {
            updateGameUrlComponents(gameUrl)
        } else {
            checkForValidGameUrl()
        }
    }

    fun updateServerIp(string: String, updateGameUrl: Boolean = true) {
        _serverIp.value = string
        if (updateGameUrl) {
            updateGameUrlFromComponents()
        }
    }

    fun updatePort(string: String, updateGameUrl: Boolean = true) {
        _port.value = string
        if (updateGameUrl) {
            updateGameUrlFromComponents()
        }
    }

    fun updateGameId(string: String, updateGameUrl: Boolean = true) {
        _gameId.value = string
        if (updateGameUrl) {
            updateGameUrlFromComponents()
        }
    }

    fun clientJoinGame() {
        menuViewModel.navigatorContext.launch {
            val joiningUrl = gameUrl().value
            _joinMessage.value = "Joining $joiningUrl..."
            _joinState.value = JoinState.JOINING
            val coachName = coachSetupModel.coachName.value
            val coachType = coachSetupModel.coachType.value
            PROPERTIES_MANAGER.setProperty(PROP_DEFAULT_CLIENT_COACH_NAME, coachName)
            model.networkAdapter.joinHost(
                gameUrl = joiningUrl,
                coachName = coachName,
                coachType = coachType,
                gameId = GameId(_gameId.value),
                teamIfHost = null,
                handler = object: AbstractClintNetworkMessageHandler() {
                    override fun onCoachJoined(coach: Coach, isHomeCoach: Boolean) {
                        _joinError.value = ""
                        _joinMessage.value = "Joined ${_gameUrl.value} as $coachName"
                        _joinState.value = JoinState.JOINED
                        model.userJoinOrContinue()
                    }
                    override fun onDisconnected(reason: CloseReason) {
                        val errorMsg = when (reason.code) {
                            JervisExitCode.CLIENT_CLOSING.code -> "" // Not a real error
                            JervisExitCode.SERVER_CLOSING.code -> "Host closed the server."
                            JervisExitCode.GAME_NOT_ACCEPTED.code -> reason.message
                            else -> "Failed to join host [${reason.code}]: ${reason.message}"
                        }

                        // We might already have reset optimistically
                        println("${_joinState.value} -> ${_joinError.value}")
                        if (_joinState.value != JoinState.READY_JOIN) {
                            _joinMessage.value = ""
                            _joinError.value = errorMsg
                            _joinState.value = JoinState.READY_JOIN
                        }
                    }
                }
            )
        }
    }

    fun disconnectFromHost() {
        menuViewModel.backgroundContext.launch {
            model.networkAdapter.disconnect(handler = object: AbstractClintNetworkMessageHandler() {
                override fun onDisconnected(reason: CloseReason) {
                    /* We already updated the UI */
                }
            })
        }
        // Optimistically leave connection. There is nothing we want from it anywway
        _joinMessage.value = ""
        _joinError.value = ""
        _joinState.value = JoinState.READY_JOIN
    }

    // Fetch the sub-components out of the url, so we can update the other fields with it
    private fun updateGameUrlComponents(gameUrl: String) {
        try {
            val url = Url(gameUrl)
            updateServerIp(url.host, false)
            updatePort(url.port.toString(), false)
            // Unclear why first element is an empty string, just filter it for now
            url.parameters["id"]?.let {
                updateGameId(it, false)
            }
            checkForValidGameUrl()
        } catch (_: IllegalArgumentException) {
            updateServerIp("", false)
            updatePort("", false)
            updateGameId("", false)
        }
    }

    // Subcomponents were updated independently. This will update the full gameUrl as well
    private fun updateGameUrlFromComponents() {
        val newUrl = "ws://${_serverIp.value}:${_port.value}/joinGame?id=${_gameId.value}"
        updateGameUrl(newUrl, false)
    }

    private fun checkForValidGameUrl() {
        var isValid = false
        if (_serverIp.value.isNotBlank() && _port.value.isNotBlank() && _gameId.value.isNotBlank()) {
            try {
                Url(gameUrl().value) // Will throw if not a valid url
                isValid = true
            } catch (_: IllegalArgumentException) {
                isValid = false
            }
        }
        if (isValid && coachSetupModel.coachName.value.isNotBlank()) {
            _joinState.value = JoinState.READY_JOIN
        } else {
            _joinState.value = JoinState.INVALID_URL
        }
    }

    fun reset(reason: String?) {
        _joinState.value = JoinState.READY_JOIN
        _joinMessage.value = ""
        _joinError.value = reason ?: ""
    }
}
