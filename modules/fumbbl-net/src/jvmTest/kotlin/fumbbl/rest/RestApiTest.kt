package fumbbl.rest

import com.jervisffb.engine.model.Coach
import com.jervisffb.engine.rules.FumbblBB2020Rules
import com.jervisffb.engine.serialize.SerializedTeam
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
    fun teamLoader() = runBlocking<Unit> {
        val rules = FumbblBB2020Rules()
        // Use 1187712 for a team with advanced special rules setup
        val file = api.loadTeam(1187712, rules)
        val team = SerializedTeam.deserialize(rules, file.team, Coach.UNKNOWN)
        assertEquals(team.name, "Just Human Nothing More")
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
