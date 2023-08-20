package dk.ilios.analyzer

import dk.ilios.analyzer.fumbbl.model.change.GameSetStartedChange
import dk.ilios.analyzer.fumbbl.model.change.ModelChange
import dk.ilios.analyzer.fumbbl.net.commands.*
import dk.ilios.bowlbot.model.Game
import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encodeToString
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.json.*
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.modules.contextual
import kotlinx.serialization.modules.polymorphic
import java.io.File
import java.time.Instant
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
