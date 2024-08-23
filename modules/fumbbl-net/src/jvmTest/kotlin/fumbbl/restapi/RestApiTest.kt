package fumbbl.restapi

import com.jervis.fumbbl.FumbblTeamLoader
import com.jervis.fumbbl.restapi.Team
import dk.ilios.jervis.fumbbl.net.auth.getHttpClient
import dk.ilios.jervis.rules.BB2020Rules
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
                val team = json.decodeFromString<Team>(jsonText)
            }
        }

    @Test
    fun teamLoader() =
        runBlocking<Unit> {
            // Human team
            val team = FumbblTeamLoader.loadTeam(1187712, BB2020Rules)
            assertEquals(team.name, "Just Human Nothing More")
        }
}
