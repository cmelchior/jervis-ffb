package com.jervisffb.ui.screen.p2p.client

import cafe.adriel.voyager.core.model.ScreenModel
import com.jervisffb.engine.model.Coach
import com.jervisffb.engine.model.CoachId
import com.jervisffb.net.GameId
import com.jervisffb.ui.PROPERTIES_MANAGER
import com.jervisffb.ui.screen.p2p.AbstractClintNetworkMessageHandler
import com.jervisffb.ui.viewmodel.MenuViewModel
import com.jervisffb.utils.PROP_DEFAULT_COACH_NAME
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

    private val _gameUrl = MutableStateFlow("")
    private val _serverIp = MutableStateFlow("")
    private val _port = MutableStateFlow("")
    private val _gameId = MutableStateFlow("")
    private val _joinState = MutableStateFlow(JoinState.INVALID_URL)
    private val _joinMessage = MutableStateFlow("")
    private val _joinError = MutableStateFlow("")
    private val _coachName = MutableStateFlow("")

    fun gameUrl(): StateFlow<String> = _gameUrl
    fun serverIp(): StateFlow<String> = _serverIp
    fun port(): StateFlow<String> = _port
    fun gameId(): StateFlow<String> = _gameId
    fun canJoin(): StateFlow<JoinState> = _joinState
    fun joinMessage(): StateFlow<String> = _joinMessage
    fun joinError(): StateFlow<String> = _joinError
    fun coachName(): StateFlow<String> = _coachName

    @OptIn(ExperimentalUuidApi::class)
    fun getCoach(): Coach? {
        val name = _coachName.value
        return if (name.isNotBlank()) {
            Coach(CoachId(Uuid.random().toString()), name)
        } else {
            null
        }
    }

    init {
        menuViewModel.navigatorContext.launch {
            PROPERTIES_MANAGER.getString(PROP_DEFAULT_COACH_NAME)?.let {
                updateCoachName(it)
            }
        }
        // TODO Hide this behind Dev flag
//        updateGameUrl("ws://localhost:8080/test")
//        updateCoachName("TestClient")
    }

    fun updateCoachName(name: String) {
        _coachName.value = name
        if (_coachName.value.isNotBlank()) {
            checkForValidGameUrl()
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
            val coachName = _coachName.value
            PROPERTIES_MANAGER.setProperty(PROP_DEFAULT_COACH_NAME, coachName)
            model.controller.joinHost(
                gameUrl = joiningUrl,
                coachName = coachName,
                gameId = GameId(_gameId.value),
                teamIfHost = null,
                handler = object: AbstractClintNetworkMessageHandler() {

                    override fun onCoachJoined(coach: Coach, isHomeCoach: Boolean) {
                        _joinError.value = ""
                        _joinMessage.value = "Joined ${_gameUrl.value} as $coachName"
                        _joinState.value = JoinState.JOINED
                        model.hostJoinedDone()
                    }

                    override fun onDisconnected(reason: CloseReason) {
                        _joinMessage.value = ""
                        _joinError.value = "Failed to join [${reason.code}]: ${reason.message}"
                        _joinState.value = JoinState.READY_JOIN
                    }
                }
            )
        }
    }

    fun disconnectFromHost() {
        menuViewModel.navigatorContext.launch {
            model.controller.disconnect(handler = object: AbstractClintNetworkMessageHandler() {
                override fun onDisconnected(reason: CloseReason) { /* We already updated the UI */ }
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
            url.parameters.get("id")?.let {
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
        if (isValid && _coachName.value.isNotBlank()) {
            _joinState.value = JoinState.READY_JOIN
        } else {
            _joinState.value = JoinState.INVALID_URL
        }
    }

    fun reset() {
        _joinState.value = JoinState.READY_JOIN
        _joinMessage.value = ""
        _joinError.value = ""
    }
}
