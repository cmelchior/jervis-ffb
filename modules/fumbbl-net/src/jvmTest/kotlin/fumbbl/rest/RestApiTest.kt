package fumbbl.rest

import com.jervisffb.engine.ext.playerNo
import com.jervisffb.engine.model.Coach
import com.jervisffb.engine.rules.FumbblBB2020Rules
import com.jervisffb.engine.rules.StandardBB2025Rules
import com.jervisffb.engine.rules.common.skills.SkillType
import com.jervisffb.engine.serialization.SerializedTeam
import com.jervisffb.fumbbl.web.FumbblApi
import kotlinx.coroutines.runBlocking
import kotlin.test.BeforeTest
import kotlin.test.Ignore
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class RestApiTest {

    private lateinit var api: FumbblApi

    @BeforeTest
    fun setUp() {
        api = FumbblApi()
    }

    @Test
    fun teamLoader() = runBlocking {
        val rules = FumbblBB2020Rules()
        // Use 1187712 for a team with advanced special rules setup
        val file = api.loadTeam(1187712, rules)
        val team = SerializedTeam.deserialize(rules, file.getOrThrow().team, Coach.UNKNOWN)
        assertEquals(team.name, "Just Human Nothing More")
    }

    // Test for https://github.com/cmelchior/jervis-ffb/issues/47
    @Test
    fun loadBB2025Team() = runBlocking {
        val rules = StandardBB2025Rules()
        val file = api.loadTeam(1261198, rules)
        val team = SerializedTeam.deserialize(rules, file.getOrThrow().team, Coach.UNKNOWN)
        assertEquals(team.name, "Plane of Tuskars Sandstorm")

        val player = team[3.playerNo]
        val position = player.position
        assertEquals("Niu", player.name)
        assertEquals("Tomb Guardian", position.title)
        assertEquals(3, position.skills.size)
        assertEquals(1, player.extraSkills.size)
        assertTrue(position.skills.any { it.type == SkillType.BRAWLER })
        assertTrue(position.skills.any { it.type == SkillType.DECAY })
        assertTrue(position.skills.any { it.type == SkillType.REGENERATION })
        assertTrue(player.extraSkills.any { it.type == SkillType.BREAK_TACKLE })
    }

    @Test
    @Ignore // Manual test
    fun authenticate() = runBlocking<Unit> {
        val id = System.getenv("FUMBBL_CLIENT_ID")
        val secret = System.getenv("FUMBBL_CLIENT_SECRET")
        api.authenticate(id, secret)
        assertTrue(api.isAuthenticated())
    }
}
