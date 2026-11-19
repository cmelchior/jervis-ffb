package com.jervisffb.test

import com.jervisffb.engine.bb2025.StandardBB2025Rules
import com.jervisffb.engine.bb2025.skills.BallAndChain
import com.jervisffb.engine.model.PlayerId
import com.jervisffb.engine.model.PlayerNo
import com.jervisffb.engine.rules.common.skills.SkillType
import com.jervisffb.engine.serialization.JervisSerialization
import com.jervisffb.engine.teamBuilder
import com.jervisffb.resources.bb2025.GOBLIN_FANATIC
import com.jervisffb.resources.bb2025.GOBLIN_TEAM_BB2025
import com.jervisffb.resources.bb2025.StandaloneRosters2025
import com.jervisffb.resources.bb2025.StandaloneStandardTeams2025
import kotlinx.serialization.json.Json
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 * Try to serialize all standalone teams and rosters for BB2025 to ensure that
 * they do not break the runtime client.
 */
class StandaloneTests2025 {

    private val jsonSerializer = Json {
        useArrayPolymorphism = true
        serializersModule = JervisSerialization.jervisEngineSerializerModule
        prettyPrint = true
    }


    @Test
    fun bb2025StandaloneStandardTeams() {
        StandaloneStandardTeams2025.defaultTeams.entries.forEach { (fileName: String, teamData) ->
            assertTrue(jsonSerializer.encodeToString(teamData).isNotEmpty())
        }
    }

    @Test
    fun bb2025StandaloneRosters() {
        StandaloneRosters2025.defaultRosters.forEach { (fileName: String, rosterData) ->
            assertTrue(jsonSerializer.encodeToString(rosterData).isNotEmpty())
        }
    }

    @Test
    fun realGoblinFanaticLoadsAllIndependentTraits() {
        val rules = StandardBB2025Rules()
        val team = teamBuilder(rules, GOBLIN_TEAM_BB2025) {
            addPlayer(PlayerId("fanatic"), "Fanatic", PlayerNo(1), GOBLIN_FANATIC)
        }
        val player = team.first()
        assertTrue(player.getSkill(SkillType.BALL_AND_CHAIN) is BallAndChain)
        assertEquals(GOBLIN_FANATIC.skills.map { it.type }.toSet(), player.skills.map { it.type }.toSet())
    }

}
