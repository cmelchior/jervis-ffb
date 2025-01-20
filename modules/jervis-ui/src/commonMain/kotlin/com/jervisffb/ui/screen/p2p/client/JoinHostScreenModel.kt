package com.jervisffb.ui.screen.p2p.client

import cafe.adriel.voyager.core.model.ScreenModel
import com.jervisffb.ui.viewmodel.MenuViewModel
import io.ktor.http.Url
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

/**
 * ViewModel class for the "Join Host" subpage. This is not a full screen,
 * but is a part of a flow when joining a Peer-to-Peer host.
 *
 * The full flow is controlled in [P2PClientScreenModel]
 */
class JoinHostScreenModel(private val menuViewModel: MenuViewModel) : ScreenModel {

    enum class JoinState {
        NOT_READY,
        READY,
        JOINING
    }

    private val _gameUrl = MutableStateFlow("")
    private val _serverIp = MutableStateFlow("")
    private val _port = MutableStateFlow("")
    private val _gameId = MutableStateFlow("")
    private val _joinState = MutableStateFlow(JoinState.NOT_READY)
    private val _joinMessage = MutableStateFlow("")

    fun gameUrl(): StateFlow<String> = _gameUrl
    fun serverIp(): StateFlow<String> = _serverIp
    fun port(): StateFlow<String> = _port
    fun gameId(): StateFlow<String> = _gameId
    fun canJoin(): StateFlow<JoinState> = _joinState
    fun joinMessage(): StateFlow<String> = _joinMessage

    fun updateGameUrl(gameUrl: String, updateOtherFields: Boolean = true) {
        _gameUrl.value = gameUrl
        if (updateOtherFields) {
            updateGameUrlComponents(gameUrl)
        }
        checkForValidGameUrl()
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

    // Fetch the sub-components out of the url, so we can update the other fields with it
    private fun updateGameUrlComponents(gameUrl: String) {
        try {
            val url = Url(gameUrl)
            updateServerIp(url.host, false)
            updatePort(url.port.toString(), false)
            url.parameters["id"]?.let { updateGameId(it, false) }
        } catch (_: IllegalArgumentException) {
            updateServerIp("", false)
            updatePort("", false)
            updateGameId("", false)
        }
    }

    // Subcomponents was updated independently. This will update the full gameUrl as well
    private fun updateGameUrlFromComponents() {
        val newUrl = "http://${_serverIp.value}:${_port.value}/joinGame?id=${_gameId.value}"
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
        if (isValid) {
            _joinState.value = JoinState.READY
        } else {
            _joinState.value = JoinState.NOT_READY
        }
    }








}
