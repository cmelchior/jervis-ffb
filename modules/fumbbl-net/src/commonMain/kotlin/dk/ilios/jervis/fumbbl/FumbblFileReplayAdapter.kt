package dk.ilios.jervis.fumbbl

import dk.ilios.jervis.fumbbl.model.Game
import dk.ilios.jervis.fumbbl.net.commands.ClientCommand
import dk.ilios.jervis.fumbbl.net.commands.ServerCommand
import dk.ilios.jervis.fumbbl.net.commands.ServerCommandGameState
import dk.ilios.jervis.fumbbl.net.commands.ServerCommandReplay
import dk.ilios.jervis.utils.platformFileSystem
import kotlinx.coroutines.CoroutineName
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.datetime.LocalDateTime
import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.decodeFromJsonElement
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.modules.SerializersModule
import okio.Path

/**
 * Class for loading a FUMBBL replay file and convert it into a stream of
 * events that is consumable by a Jervis Game Model. This way, it is
 * possible to replay the entire FUMBBL game inside Jervis.
 *
 * Note, this requires that the FUMBBL and Jervis Rules are set up the same way.
 *
 */
class FumbblFileReplayAdapter(private val file: Path) : FumbblAdapter {
    private val scope = CoroutineScope(CoroutineName("FumbblFileReader") + Dispatchers.Default)

    // Messages sent from the server. Users of this class
    // are required to listen to the channel.
    private val incomingMessages: Channel<ServerCommandReplay> = Channel()

    // Messages that should be sent to the server
    private val outgoingMessages: Channel<ServerCommand> = Channel()

    private val gameState: Channel<Game> = Channel(capacity = 1)

    override var isClosed = false

    val json =
        Json {
            prettyPrint = true
            serializersModule =
                SerializersModule {
                    contextual(
                        LocalDateTime::class,
                        object : KSerializer<LocalDateTime> {
                            override val descriptor: SerialDescriptor =
                                PrimitiveSerialDescriptor("LocalDateTime", PrimitiveKind.STRING)

                            override fun deserialize(decoder: Decoder): LocalDateTime {
                                try {
                                    return LocalDateTime.parse(decoder.decodeString())
                                } catch (ex: Throwable) {
                                    throw ex
                                }
                            }

                            override fun serialize(
                                encoder: Encoder,
                                value: LocalDateTime,
                            ) {
                                encoder.encodeString(value.toString())
                            }
                        },
                    )
                }
            ignoreUnknownKeys = true
        }

    override suspend fun start() {
        scope.launch {
            val fileContent = platformFileSystem.read(file) { readUtf8() }.trim()
            // Some of the replay files are not JSON arrays, but just an JSON object pr. line.
            val isJson = (fileContent.startsWith("[") && fileContent.endsWith("]"))
            val gameCommands: List<ServerCommand>
            if (isJson) {
                val fileAsJson: JsonElement = json.parseToJsonElement(fileContent)
                gameCommands = json.decodeFromJsonElement<List<ServerCommand>>(fileAsJson.jsonArray)
            } else {
                gameCommands =
                    fileContent.lines().map { jsonString ->
                        try {
                            val el = json.parseToJsonElement(jsonString)
                            json.decodeFromJsonElement<ServerCommand>(el)
                        } catch (ex: Throwable) {
                            println(jsonString)
                            throw ex
                        }
                    }
            }
            
            for (element: ServerCommand in gameCommands) {
                if (isActive) {
                    val cmd = element
                    if (cmd is ServerCommandGameState) {
                        gameState.send(cmd.game)
                    } else {
                        if (cmd is ServerCommandReplay) {
                            incomingMessages.send(cmd)
                        } else {
                            ignoreCommand(cmd)
                        }
                    }
                } else {
                    break
                }
            }
        }
    }

    private fun ignoreCommand(cmd: ServerCommand) {
        println("Ignoring: ${cmd::class.simpleName}")
    }

    // Only call this once.
    override suspend fun getGame(): Game {
        return gameState.receive().also {
            gameState.cancel()
        }
    }

    override suspend fun receive(): ServerCommandReplay = incomingMessages.receive()

    override suspend fun send(command: ClientCommand) = TODO("Not yet implemented")

    override fun close() {
        isClosed = true
        incomingMessages.close()
        outgoingMessages.close()
        scope.cancel()
    }
}
