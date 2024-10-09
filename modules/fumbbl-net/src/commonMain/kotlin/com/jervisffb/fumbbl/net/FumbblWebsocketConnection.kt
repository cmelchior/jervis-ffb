package com.jervisffb.fumbbl.net

import com.jervisffb.fumbbl.net.api.auth.getHttpClient
import com.jervisffb.fumbbl.net.api.commands.ClientCommand
import com.jervisffb.fumbbl.net.api.commands.ServerCommand
import io.ktor.client.plugins.websocket.receiveDeserialized
import io.ktor.client.plugins.websocket.sendSerialized
import io.ktor.client.plugins.websocket.webSocket
import io.ktor.websocket.CloseReason
import kotlinx.coroutines.CoroutineName
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.cancel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

/**
 * Class for controlling the websocket connection to FUMBBL.
 * It only controls sending/receiving messages. It is up to users of this class
 * to know which messages to send and receive.
 */
class FumbblWebsocketConnection() {
    private val scope = CoroutineScope(CoroutineName("FumbblWebsocket"))

    // Messages sent from the server. Users of this class
    // are required to listen to the channel.
    private val incoming: Channel<ServerCommand> = Channel()

    // Messages that should be sent to the server
    private val outgoing: Channel<ClientCommand> = Channel()

    var isClosed = false

    suspend fun start() {
        val client = getHttpClient()
        scope.launch {
            client.webSocket(
                host = "fumbbl.com",
                port = 22223,
                path = "/command",
            ) {
                launch {
                    while (this.isActive) {
                        val outgoingMessage: ClientCommand = this@FumbblWebsocketConnection.outgoing.receive()
                        println("Sending: $outgoingMessage")
                        sendSerialized<ClientCommand>(outgoingMessage)
                    }
                }
                launch {
                    while (this.isActive) {
                        val incomingMessage: ServerCommand = receiveDeserialized<ServerCommand>()
                        println("Received: $incomingMessage")
                        this@FumbblWebsocketConnection.incoming.send(incomingMessage)
                    }
                }
                launch {
                    val closing: CloseReason? = closeReason.await()
                    println("Closing websocket: ${closing?.toString() ?: "null"}")
                }
            }
        }
    }

    suspend fun receive(): ServerCommand = incoming.receive()

    suspend fun send(command: ClientCommand) = outgoing.send(command)

    fun close() {
        isClosed = true
        incoming.close()
        outgoing.close()
        scope.cancel()
    }
}
