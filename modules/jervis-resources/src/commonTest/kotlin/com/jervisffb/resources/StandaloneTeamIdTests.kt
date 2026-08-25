package com.jervisffb.resources

import com.jervisffb.engine.TeamBuilder
import com.jervisffb.engine.serialization.JervisTeamFile
import com.jervisffb.resources.bb2020.StandaloneBB7Teams2020
import com.jervisffb.resources.bb2020.StandaloneStandardTeams2020
import com.jervisffb.resources.bb2025.StandaloneBB7Teams2025
import com.jervisffb.resources.bb2025.StandaloneStandardTeams2025
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 * Check that all bundled teams have stable, explicit team ids.
 *
 * If not, [TeamBuilder] falls back to a random id. A team that relies on that
 * fallback therefore gets a new id every time it is installed, which makes it
 * impossible to tell them apart when creating P2P games.
 */
class StandaloneTeamIdTests {

    private val allTeams: Map<String, JervisTeamFile> = buildMap {
        putAll(StandaloneStandardTeams2020.defaultTeams)
        putAll(StandaloneBB7Teams2020.defaultTeams)
        putAll(StandaloneStandardTeams2025.defaultTeams)
        putAll(StandaloneBB7Teams2025.defaultTeams)
    }

    // By convention, all jervis starter teams start begin with "jervis-".
    @Test
    fun everyStandaloneTeamHasAnExplicitId() {
        allTeams.forEach { (fileName, teamFile) ->
            val id = teamFile.team.id.value
            assertTrue(
                id.startsWith("jervis-"),
                "$fileName has id '$id'. Bundled teams must set an explicit `id = TeamId(\"jervis-...\")`, " +
                    "otherwise TeamBuilder assigns a random one that changes on every install.",
            )
        }
    }

    @Test
    fun standaloneTeamIdsAreUnique() {
        val idsByTeam = allTeams.map { (fileName, teamFile) -> fileName to teamFile.team.id.value }
        val duplicates = idsByTeam.groupBy { it.second }.filter { it.value.size > 1 }
        assertTrue(duplicates.isEmpty(), "Bundled teams share team ids: $duplicates")
        assertEquals(allTeams.size, idsByTeam.map { it.second }.toSet().size)
    }
}
