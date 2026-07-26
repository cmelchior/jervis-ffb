package com.jervisffb.test

import com.jervisffb.engine.serialization.JervisSerialization
import com.jervisffb.resources.bb2020.BB2020StandaloneBB7Teams
import com.jervisffb.resources.bb2020.BB2020StandaloneRosters
import com.jervisffb.resources.bb2020.BB2020StandaloneStandardTeams
import kotlinx.serialization.json.Json
import kotlin.test.Test
import kotlin.test.assertTrue

/**
 * Try to serialize all standalone teams and rosters for BB2020 to ensure that
 * they do not break the runtime client.
 */
class BB2020StandaloneTests {

    private val jsonSerializer = Json {
        useArrayPolymorphism = true
        serializersModule = JervisSerialization.jervisEngineSerializerModule
        prettyPrint = true
    }

    @Test
    fun bb2020StandardTeams() {
        BB2020StandaloneStandardTeams.defaultTeams.entries.forEach { (fileName: String, teamData) ->
            assertTrue(jsonSerializer.encodeToString(teamData).isNotEmpty())
        }
    }

    @Test
    fun bb2020StandaloneRosters() {
        BB2020StandaloneRosters.defaultRosters.forEach { (fileName: String, rosterData) ->
            assertTrue(jsonSerializer.encodeToString(rosterData).isNotEmpty())
        }
    }

    @Test
    fun bb2020SevensTeams() {
        BB2020StandaloneBB7Teams.defaultTeams.entries.forEach { (fileName: String, teamData) ->
            assertTrue(jsonSerializer.encodeToString(teamData).isNotEmpty())
        }
    }
}
