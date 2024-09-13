package dk.ilios.jervis.fumbbl.net.serialization

import dk.ilios.jervis.utils.ReflectionUtils
import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlin.reflect.KClass

/**
 * Shared interface between all enums used in the websocket protocol.
 */
interface FumbblEnum {
    val id: String
}

open class FumbblEnumSerializer<E>(
    private val kClass: KClass<E>,
) : KSerializer<E> where E : Enum<E>, E : FumbblEnum {
    override val descriptor: SerialDescriptor =
        PrimitiveSerialDescriptor(
            "dk.ilios.jervis.fumbbl.net.serialization.FumbblEnumSerializer",
            PrimitiveKind.STRING,
        )

    override fun serialize(
        encoder: Encoder,
        value: E,
    ) {
        encoder.encodeString(value.id)
    }

    override fun deserialize(decoder: Decoder): E =
        decoder.decodeString().let { value ->
            ReflectionUtils.getEnumConstants(kClass).firstOrNull {
                it.id == value
            } ?: throw IllegalStateException("Cannot find enum with label $value in ${kClass.simpleName}")
        }
}
