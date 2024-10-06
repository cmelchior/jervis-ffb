package fumbbl.restapi

import com.jervisffb.fumbbl.net.api.auth.getHttpClient
import com.jervisffb.engine.rules.StandardBB2020Rules
import io.ktor.client.HttpClient
import io.ktor.client.request.get
import io.ktor.client.statement.bodyAsText
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.json.Json
import kotlin.test.Test
import kotlin.test.assertEquals

class RestApiTest {
    @Test
    fun smokeTest() =
        runBlocking {
            val json =
                Json {
                    ignoreUnknownKeys = true
                }
            getHttpClient().use { client: HttpClient ->
                val jsonText = client.get("https://fumbbl.com/api/team/get/1158751").bodyAsText()
                val team = json.decodeFromString<com.jervisffb.fumbbl.web.api.Team>(jsonText)
            }
        }

    @Test
    fun teamLoader() =
        runBlocking<Unit> {
            // Human team
            val team = com.jervisffb.fumbbl.web.FumbblTeamLoader.loadTeam(1187712, StandardBB2020Rules)
            assertEquals(team.name, "Just Human Nothing More")
        }
}
