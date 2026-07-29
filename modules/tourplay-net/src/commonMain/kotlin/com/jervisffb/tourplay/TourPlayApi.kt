package com.jervisffb.tourplay

import com.jervisffb.engine.rules.Rules
import com.jervisffb.engine.rules.builder.GameVersion
import com.jervisffb.engine.serialization.FILE_FORMAT_VERSION
import com.jervisffb.engine.serialization.JervisMetaData
import com.jervisffb.engine.serialization.JervisTeamFile
import com.jervisffb.tourplay.api.TourPlayRoster
import com.jervisffb.utils.getHttpClient
import com.jervisffb.utils.jervisLogger
import io.ktor.client.request.get
import io.ktor.client.statement.bodyAsText
import io.ktor.http.isSuccess
import kotlinx.serialization.json.Json

/**
 * Wrapper around the TourPlay REST API: https://tourplay.net
 */
class TourPlayApi {

    companion object {
        val LOG = jervisLogger()
        val BASE_URL = "https://tourplay.net/api"
    }

    private val client = getHttpClient()
    private val json = Json { ignoreUnknownKeys = true }

    /**
     * Load a FUMBBL Team and convert it to a Jervis, so it can be used
     * inside Jervis games.
     */
    suspend fun loadRoster(
        rosterId: Long,
        rules: Rules,
    ): Result<JervisTeamFile> {
        try {
            val rosterData = loadTeamFromTourPlay(rosterId)
            val mapper = when (rules.baseVersion) {
                GameVersion.BB2020 -> BB2020Mapper()
                GameVersion.BB2025 -> BB2025Mapper()
            }
            val jervisRoster = mapper.convertToJervisRoster(rules, rosterData)
            val jervisTeam = mapper.convertToJervisTeam(rules, jervisRoster, rosterData)
            return Result.success(JervisTeamFile(
                metadata = JervisMetaData(fileFormat = FILE_FORMAT_VERSION),
                team = jervisTeam,
                history = null,
            ))
        } catch (ex: Exception) {
            return Result.failure(ex)
        }
    }

    private suspend fun loadTeamFromTourPlay(teamId: Long): TourPlayRoster {
        val response = client.get("$BASE_URL/rosters/$teamId") {
            with(headers) {
                append("Accept", "application/json, text/plain, */*")
                append("Referer", "https://tourplay.net/en/blood-bowl/roster/$teamId")
                append(
                    "User-Agent",
                    "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/135.0.0.0 Safari/537.36"
                )
            }
        }
        if (response.status.isSuccess()) {
            val rosterAsJson = response.bodyAsText()
            return json.decodeFromString<TourPlayRoster>(rosterAsJson)
        } else {
            throw IllegalStateException("Loading roster: $teamId failed with status: ${response.status}")
        }
    }
}
