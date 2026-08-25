package com.jervisffb.test

import com.jervisffb.engine.serialization.JervisSerialization
import com.jervisffb.resources.bb2020.StandaloneBB7Teams2020
import com.jervisffb.resources.bb2020.StandaloneRosters2020
import com.jervisffb.resources.bb2020.StandaloneStandardTeams2020
import kotlinx.serialization.json.Json
import kotlin.test.Test
import kotlin.test.assertTrue

/**
 * Try to serialize all standalone teams and rosters for BB2020 to ensure that
 * they do not break the runtime client.
 */
class StandaloneTests2020 {

    private val jsonSerializer = Json {
        useArrayPolymorphism = true
        serializersModule = JervisSerialization.jervisEngineSerializerModule
        prettyPrint = true
    }

    @Test
    fun bb2020StandardTeams() {
        StandaloneStandardTeams2020.defaultTeams.entries.forEach { (fileName: String, teamData) ->
            assertTrue(jsonSerializer.encodeToString(teamData).isNotEmpty())
        }
    }

    @Test
    fun bb2020StandaloneRosters() {
        StandaloneRosters2020.defaultRosters.forEach { (fileName: String, rosterData) ->
            assertTrue(jsonSerializer.encodeToString(rosterData).isNotEmpty())
        }
    }

    @Test
    fun bb2020SevensTeams() {
        StandaloneBB7Teams2020.defaultTeams.entries.forEach { (fileName: String, teamData) ->
            assertTrue(jsonSerializer.encodeToString(teamData).isNotEmpty())
        }
    }
}
