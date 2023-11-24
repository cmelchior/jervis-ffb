package dk.ilios.analyzer

import dk.ilios.bowlbot.model.Game
import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.modules.SerializersModule
import java.io.File
import java.time.LocalDateTime

fun main(args: Array<String>) {
    val fileName = "test-game/websocket-traffic-team1-filtered.fumbbl"
//    val fileName = "game-1624379.json"
//    val fileName = "game.json"
    val gameFile = File("./replays/$fileName")
    val json = Json {
        prettyPrint = true
        serializersModule = SerializersModule {
            contextual(LocalDateTime::class, object: KSerializer<LocalDateTime> {
                override val descriptor: SerialDescriptor = PrimitiveSerialDescriptor("LocalDateTime", PrimitiveKind.STRING)

                override fun deserialize(decoder: Decoder): LocalDateTime {
                    return LocalDateTime.parse(decoder.decodeString())
                }

                override fun serialize(encoder: Encoder, value: LocalDateTime) {
                    encoder.encodeString(value.toString())
                }
            })
        }
        ignoreUnknownKeys = true
    }

    val gameCommands = gameFile.readLines()
    val cmd = gameCommands.map {
        json.parseToJsonElement(it)
    }
//    json.parseToJsonElement(ga)

//    val gameCommands: JsonElement = json.parseToJsonElement(gameFile.readText(charset = Charsets.UTF_8))
//    val cmd = json.decodeFromJsonElement<List<ServerCommand>>(gameCommands.jsonArray)
    println(cmd.joinToString(separator = ",\n"))
}

fun initialGameAndTeams(command: JsonElement): Game {
    TODO()
}
