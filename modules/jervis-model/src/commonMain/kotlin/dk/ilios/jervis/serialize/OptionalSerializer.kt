//package dk.ilios.jervis.serialize
//
//import dk.ilios.jervis.model.Optional
//import kotlinx.serialization.ExperimentalSerializationApi
//import kotlinx.serialization.InternalSerializationApi
//import kotlinx.serialization.KSerializer
//import kotlinx.serialization.builtins.IntArraySerializer
//import kotlinx.serialization.descriptors.SerialDescriptor
//import kotlinx.serialization.descriptors.SerialKind
//import kotlinx.serialization.descriptors.buildClassSerialDescriptor
//import kotlinx.serialization.descriptors.buildSerialDescriptor
//import kotlinx.serialization.encoding.Decoder
//import kotlinx.serialization.encoding.Encoder
//
//class OptionalSerializer : KSerializer<Optional<*>> {
//    private val delegateSerializer = IntArraySerializer()
//
//    @OptIn(ExperimentalSerializationApi::class)
//    override val descriptor = SerialDescriptor("Color", delegateSerializer.descriptor)
//
//    override fun serialize(encoder: Encoder, value: Optional<*>) {
//        val data = intArrayOf(
//            (value.rgb shr 16) and 0xFF,
//            (value.rgb shr 8) and 0xFF,
//            value.rgb and 0xFF
//        )
//        encoder.encodeSerializableValue(delegateSerializer, data)
//    }
//
//    override fun deserialize(decoder: Decoder): Optional<*> {
//        val array = decoder.decodeSerializableValue(delegateSerializer)
//        return Color((array[0] shl 16) or (array[1] shl 8) or array[2])
//    }
//}
