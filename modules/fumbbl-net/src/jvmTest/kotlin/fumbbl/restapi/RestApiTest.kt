package fumbbl.restapi

import com.jervisffb.engine.rules.StandardBB2020Rules
import com.jervisffb.fumbbl.web.FumbblTeamLoader
import kotlinx.coroutines.runBlocking
import kotlin.test.Test
import kotlin.test.assertEquals

class RestApiTest {
    @Test
    fun teamLoader() = runBlocking<Unit> {
        val file = FumbblTeamLoader().loadTeam(1187712, StandardBB2020Rules)
        assertEquals(file.team.name, "Just Human Nothing More")
    }
}
