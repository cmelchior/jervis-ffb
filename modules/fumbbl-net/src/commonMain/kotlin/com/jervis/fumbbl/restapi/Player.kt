package com.jervis.fumbbl.restapi

import dk.ilios.jervis.fumbbl.net.auth.getHttpClient
import dk.ilios.jervis.utils.runBlocking
import io.ktor.client.request.get
import io.ktor.client.statement.bodyAsText
import io.ktor.utils.io.core.use
import kotlinx.serialization.KSerializer
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.json.Json

@Serializable
data class Player(
    val gender: String,
    val hasBio: Boolean,
    val id: Int,
    val injuries: String,
    val name: String,
    val number: Int,
    // val position: String,
    @SerialName("positionId")
    @Serializable(with = PositionDeserializer::class)
    val position: Position,
//    val record: Record,
    val refundable: Boolean,
//    val skillCosts: List<Any>,
    val skillStatus: SkillStatus,
    val skills: List<String>,
    val status: Int,
)

object PositionDeserializer : KSerializer<Position> {
    override val descriptor: SerialDescriptor = PrimitiveSerialDescriptor("Position", PrimitiveKind.INT)
    private val json = Json { ignoreUnknownKeys = true }

    override fun deserialize(decoder: Decoder): Position {
        val id = decoder.decodeInt()
        val contentData = fetchContentById(id)
        return json.decodeFromString(Position.serializer(), contentData)
    }

    override fun serialize(
        encoder: Encoder,
        value: Position,
    ) {
        TODO("Not supported")
    }

    private fun fetchContentById(id: Int): String {
        return runBlocking {
            val url = "https://fumbbl.com/api/position/get/$id"
            getHttpClient().use { client ->
                client.get(url).bodyAsText()
            }
        }
    }
}
