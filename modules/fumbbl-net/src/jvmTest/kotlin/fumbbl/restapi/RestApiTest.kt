package fumbbl.restapi

import com.jervisffb.engine.rules.StandardBB2020Rules
import com.jervisffb.engine.serialize.JervisSerialization.jervisEngineModule
import com.jervisffb.engine.serialize.JervisTeamFile
import com.jervisffb.fumbbl.web.FumbblTeamLoader
import com.jervisffb.fumbbl.web.api.TeamDetails
import com.jervisffb.utils.getHttpClient
import io.ktor.client.HttpClient
import io.ktor.client.request.get
import io.ktor.client.statement.bodyAsText
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.io.File
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
                val team = json.decodeFromString<TeamDetails>(jsonText)
            }
        }

    @Test
    fun teamLoader() = runBlocking<Unit> {
        // Human team
        val json = Json {
            useArrayPolymorphism = true
            serializersModule = jervisEngineModule
            prettyPrint = true
        }
        val file = FumbblTeamLoader().loadTeam(1187712, StandardBB2020Rules)
        File(file.team.name).writeText(json.encodeToString<JervisTeamFile>(file))
        assertEquals(file.team.name, "Just Human Nothing More")
    }
}
