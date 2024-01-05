package dk.ilios.analyzer

import dk.ilios.jervis.fumbbl.net.commands.NetCommand
import dk.ilios.jervis.fumbbl.net.commands.ServerCommand
import dk.ilios.jervis.model.Game
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
import java.io.File
import java.nio.charset.Charset
import java.time.LocalDateTime

fun main(args: Array<String>) {
//    val fileName = "../../replays/human-starter-game/websocket-traffic-team1.fumbbl"
//    val fileName = "../../replays/human-starter-game/websocket-traffic-team2.fumbbl"
    val fileName = "../../replays/game-1624379.json"
//        val fileName = "../../replays-fumbbl/game-1624379.json"
    //    val fileName = "game-1624379.json"
//    val fileName = "game.json"
//    val gameFile = File("./replays-fumbbl/$fileName")
    val gameFile = File(fileName)
    val json = Json {
        prettyPrint = true
        serializersModule = SerializersModule {
            contextual(LocalDateTime::class, object: KSerializer<LocalDateTime> {
                override val descriptor: SerialDescriptor = PrimitiveSerialDescriptor("LocalDateTime", PrimitiveKind.STRING)

                override fun deserialize(decoder: Decoder): LocalDateTime {
                    try {
                        return LocalDateTime.parse(decoder.decodeString())
                    } catch (ex: Throwable) {
                        throw ex
                    }
                }

                override fun serialize(encoder: Encoder, value: LocalDateTime) {
                    encoder.encodeString(value.toString())
                }
            })
        }
        ignoreUnknownKeys = true
    }

    // Test if file is an json array
    val fileContent = gameFile.readText(Charsets.UTF_8).trim()
    val isJson = (fileContent.startsWith("[") && fileContent.endsWith("]"))
    if (isJson) {
//        println(fileContent.subSequence(280360 - 100, 280360 + 100))
        val fileAsJson: JsonElement = json.parseToJsonElement(fileContent)
        val gameCommands = json.decodeFromJsonElement<List<NetCommand>>(fileAsJson.jsonArray)
        println(gameCommands.size)
    } else {
        val gameCommands: List<NetCommand> = gameFile.readLines().map { jsonString ->
            try {
                val el = json.parseToJsonElement(jsonString)
                json.decodeFromJsonElement<NetCommand>(el)
            } catch (ex: Throwable) {
                println(jsonString)
                throw ex
            }
        }
    }
}
