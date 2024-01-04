package dk.ilios.jervis.fumbbl

import dk.ilios.jervis.fumbbl.net.auth.getHttpClient
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
import dk.ilios.jervis.fumbbl.net.commands.ServerCommand

/**
 * Class for controlling the websocket connection with FUMBBL.
 *
 * This class controlls
 *
 */
class FumbblWebsocketConnection() {

    private val scope = CoroutineScope(CoroutineName("FumbblWebsocket"))

    // Messages sent from the server. Users of this class
    // are required to listen to the channel.
    private val incoming: Channel<ServerCommand> = Channel()

    // Messages that should be sent to the server
    private val outgoing: Channel<ServerCommand> = Channel()

    var isClosed = false

    suspend fun start() {
        val client = getHttpClient()
        scope.launch {
            client.webSocket(
                host = "fumbbl.com",
                port = 22223,
                path = "/command"
            ) {
                launch {
                    while (this.isActive) {
                        val outgoingMessage: ServerCommand = this@FumbblWebsocketConnection.outgoing.receive()
                        println("Sending: $outgoingMessage")
                        sendSerialized<ServerCommand>(outgoingMessage)
                    }
                }
                launch {
                    while(this.isActive) {
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

    suspend fun send(command: ServerCommand) = outgoing.send(command)

    fun close() {
        isClosed = true
        incoming.close()
        outgoing.close()
        scope.cancel()
    }

}