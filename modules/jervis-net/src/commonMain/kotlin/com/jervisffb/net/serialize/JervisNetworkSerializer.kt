package com.jervisffb.net.serialize

import com.jervisffb.engine.serialization.JervisSerialization
import kotlinx.serialization.json.Json
import kotlinx.serialization.modules.SerializersModule

val jervisNetworkSerializer = Json {
    useArrayPolymorphism = true
    allowStructuredMapKeys = true // Required by Inducements
    serializersModule = SerializersModule {
        include(JervisSerialization.jervisEngineSerializerModule)
    }
}
