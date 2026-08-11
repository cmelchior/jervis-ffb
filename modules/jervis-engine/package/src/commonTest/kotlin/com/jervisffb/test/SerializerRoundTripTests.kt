package com.jervisffb.test

import com.jervisffb.engine.bb2020.StandardBB2020Rules
import com.jervisffb.engine.bb2025.StandardBB2025Rules
import com.jervisffb.engine.rules.Rules
import com.jervisffb.engine.serialization.JervisSerialization.jervisEngineSerializerModule
import kotlinx.serialization.PolymorphicSerializer
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 * Smoke tests that exercise the polymorphic serializer registrations for
 * both rulesets. If a subclass registration goes missing after the split
 * of `generatedSerializer.kt` into three per-module files, these tests
 * will fail at serializing time with a `SerializationException`.
 */
class SerializerRoundTripTests {

    private val json = Json {
        useArrayPolymorphism = true
        allowStructuredMapKeys = true
        serializersModule = jervisEngineSerializerModule
    }

    @Test
    fun bb2020RulesRoundTrip() {
        val original: Rules = StandardBB2020Rules()
        val serialized: JsonElement = json.encodeToJsonElement(PolymorphicSerializer(Rules::class), original)
        val restored: Rules = json.decodeFromJsonElement(PolymorphicSerializer(Rules::class), serialized)
        assertTrue(restored is StandardBB2020Rules, "Restored should be StandardBB2020Rules, was $restored")
        assertEquals(original.name, restored.name)
        assertEquals(original.baseVersion, restored.baseVersion)
    }

    @Test
    fun bb2025RulesRoundTrip() {
        val original: Rules = StandardBB2025Rules()
        val serialized: JsonElement = json.encodeToJsonElement(PolymorphicSerializer(Rules::class), original)
        val restored: Rules = json.decodeFromJsonElement(PolymorphicSerializer(Rules::class), serialized)
        assertTrue(restored is StandardBB2025Rules, "Restored should be StandardBB2025Rules, was $restored")
        assertEquals(original.name, restored.name)
        assertEquals(original.baseVersion, restored.baseVersion)
    }

}
