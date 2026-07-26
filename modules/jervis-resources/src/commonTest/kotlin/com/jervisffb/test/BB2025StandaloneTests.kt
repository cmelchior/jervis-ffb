package com.jervisffb.test

import com.jervisffb.engine.serialization.JervisSerialization
import com.jervisffb.resources.bb2025.BB2025StandaloneRosters
import com.jervisffb.resources.bb2025.BB2025StandaloneStandardTeams
import kotlinx.serialization.json.Json
import kotlin.test.Test
import kotlin.test.assertTrue

/**
 * Try to serialize all standalone teams and rosters for BB2025 to ensure that
 * they do not break the runtime client.
 */
class BB2025StandaloneTests {

    private val jsonSerializer = Json {
        useArrayPolymorphism = true
        serializersModule = JervisSerialization.jervisEngineSerializerModule
        prettyPrint = true
    }


    @Test
    fun bb2025StandaloneStandardTeams() {
        BB2025StandaloneStandardTeams.defaultTeams.entries.forEach { (fileName: String, teamData) ->
            assertTrue(jsonSerializer.encodeToString(teamData).isNotEmpty())
        }
    }

    @Test
    fun bb2025StandaloneRosters() {
        BB2025StandaloneRosters.defaultRosters.forEach { (fileName: String, rosterData) ->
            assertTrue(jsonSerializer.encodeToString(rosterData).isNotEmpty())
        }
    }
}
