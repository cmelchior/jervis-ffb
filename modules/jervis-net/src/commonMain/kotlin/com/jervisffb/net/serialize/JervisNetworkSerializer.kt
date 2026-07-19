package com.jervisffb.net.serialize

import com.jervisffb.engine.serialize.JervisSerialization
import kotlinx.serialization.json.Json
import kotlinx.serialization.modules.SerializersModule

val jervisNetworkSerializer = Json {
    useArrayPolymorphism = true
    allowStructuredMapKeys = true // Required by Inducements
    serializersModule = SerializersModule {
        include(JervisSerialization.jervisEngineSerializerModule)
    }
}
