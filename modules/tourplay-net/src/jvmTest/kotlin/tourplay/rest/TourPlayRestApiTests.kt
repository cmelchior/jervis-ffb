package tourplay.rest

import com.jervisffb.engine.bb2020.FumbblBB2020Rules
import com.jervisffb.engine.bb2025.BB7Rules2025
import com.jervisffb.engine.bb2025.StandardBB2025Rules
import com.jervisffb.engine.model.Coach
import com.jervisffb.engine.rules.common.roster.RegionalSpecialRule
import com.jervisffb.engine.serialization.SerializedTeam
import com.jervisffb.engine.sprites.SpriteLocation
import com.jervisffb.tourplay.TourPlayApi
import com.jervisffb.tourplay.TourPlayIconMapping
import io.ktor.client.HttpClient
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.MockRequestHandler
import io.ktor.client.engine.mock.respond
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.headersOf
import kotlinx.coroutines.runBlocking
import okio.ByteString.Companion.encodeUtf8
import java.io.File
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertNull
import kotlin.test.assertTrue

class TourPlayRestApiTests {

    private lateinit var api: TourPlayApi

    @BeforeTest
    fun setUp() {
        api = TourPlayApi(iconMapping)
    }

    @Test
    fun loadsBB2020Roster() = runBlocking {
        val rules = FumbblBB2020Rules()
        val file = api.loadRoster("86aa5433e61a461f866d2c8203090aca", rules)
        val team = SerializedTeam.deserialize(rules, file.getOrThrow().team, Coach.UNKNOWN)
        assertEquals("Sub Saurios", team.name)
    }

    @Test
    fun loadsBB2020VampireRoster() = runBlocking {
        val rules = FumbblBB2020Rules()
        val file = api.loadRoster("14e5ed87c5574692a488e6f9cd0310ab", rules)
        val team = SerializedTeam.deserialize(rules, file.getOrThrow().team, Coach.UNKNOWN)
        assertEquals("Sin Reflejos", team.name)
    }

    @Test
    fun loadsBB2025Roster() = runBlocking {
        val rules = StandardBB2025Rules()
        val file = api.loadRoster(ROSTER_ID, rules)
        val team = SerializedTeam.deserialize(rules, file.getOrThrow().team, Coach.UNKNOWN)
        assertEquals("Saphery Spelldrivers", team.name)
        assertEquals(ROSTER_UUID, team.id.value)
    }

    // Test for https://github.com/cmelchior/jervis-ffb/issues/62
    @Test
    fun teamLoaderUsesFumbblIcons() = runBlocking {
        // Cover roster-qualified entries, bare entries and positions renamed between
        // BB2020 and BB2025.
        listOf(
            Triple("2d1966f888eb4003b7a32a28eb38039a", "Old World Alliance", FumbblBB2020Rules()),
            Triple(ROSTER_ID, "High Elf", StandardBB2025Rules()),
            Triple("0698842076fc4b94b886408a5ac02774", "Nurgle", StandardBB2025Rules())
        ).forEach { (id, roster, rules) ->
            val file = api.loadRoster(id, rules)
            val team = SerializedTeam.deserialize(rules, file.getOrThrow().team, Coach.UNKNOWN)
            assertEquals(roster, team.roster.name)
            team.roster.positions.forEach { position ->
                assertEquals(SpriteLocation.FUMBBL_INI, position.icon?.type, "No icon for ${position.title}")
                assertEquals(SpriteLocation.FUMBBL_INI, position.portrait?.type, "No portrait for ${position.title}")
            }
            assertTrue(team.all { it.icon?.sprite?.type == SpriteLocation.FUMBBL_INI })
        }
    }

    @Test
    fun loadsBB7Roster() = runBlocking {
        val rules = BB7Rules2025()
        val file = api.loadRoster("10813ca2c16e49a7a0a0b5222ffe7adf", rules)
        val team = SerializedTeam.deserialize(rules, file.getOrThrow().team, Coach.UNKNOWN)
        assertEquals("Vestsylvansk Gymnastikforening", team.name)
    }

    @Test
    fun teamLoaderKeepsTheSelectedLeague() = runBlocking {
        // TourPlay tracks which league a team plays in, and that decides which Star
        // Players it may hire. Both rosters here only play in a single league, but taking
        // the team's own choice is what makes rosters playing in several, e.g. Chaos
        // Dwarf, import correctly.
        listOf(
            "0698842076fc4b94b886408a5ac02774" to RegionalSpecialRule.CHAOS_CLASH,
            ROSTER_ID to RegionalSpecialRule.ELVEN_KINGDOMS_LEAGUE,
        ).forEach { (id, league) ->
            val rules = StandardBB2025Rules()
            val file = api.loadRoster(id, rules)
            val team = SerializedTeam.deserialize(rules, file.getOrThrow().team, Coach.UNKNOWN)
            assertEquals(league, team.league, team.name)
            assertTrue(league in team.allSpecialRules, team.name)
        }
    }

    @Test
    fun reusesProofForLaterRosterLoads() = runBlocking {
        var requests = 0
        withApi({ request ->
            when (++requests) {
                1 -> respond("Access denied.", HttpStatusCode.Forbidden, headersOf("X-TP-PoW-Challenge", OBFUSCATED_CHALLENGE))
                else -> {
                    assertEquals(PROOF_COOKIE, request.headers[HttpHeaders.Cookie])
                    if (requests == 3) assertNull(request.headers["X-TP-PoW-Retry"])
                    respond(ROSTER_JSON)
                }
            }
        }) { api ->
            val rules = StandardBB2025Rules()
            api.loadRoster(ROSTER_ID, rules).getOrThrow()
            api.loadRoster(ANOTHER_ROSTER_ID, rules).getOrThrow()
            assertEquals(3, requests)
        }
    }

    @Test
    fun replacesRejectedProof() = runBlocking {
        var requests = 0
        withApi({ request ->
            when (++requests) {
                1 -> respond("Access denied.", HttpStatusCode.Forbidden, headersOf("X-TP-PoW-Challenge", OBFUSCATED_CHALLENGE))
                2 -> respond(ROSTER_JSON)
                3 -> {
                    assertEquals(PROOF_COOKIE, request.headers[HttpHeaders.Cookie])
                    respond("Access denied.", HttpStatusCode.Forbidden, headersOf("X-TP-PoW-Challenge", FRESH_CHALLENGE))
                }
                else -> {
                    assertEquals(FRESH_PROOF_COOKIE, request.headers[HttpHeaders.Cookie])
                    assertEquals("1", request.headers["X-TP-PoW-Retry"])
                    respond(ROSTER_JSON)
                }
            }
        }) { api ->
            val rules = StandardBB2025Rules()
            api.loadRoster(ROSTER_ID, rules).getOrThrow()
            api.loadRoster(ANOTHER_ROSTER_ID, rules).getOrThrow()
            assertEquals(4, requests)
        }
    }

    @Test
    fun answersASecondChallengeDuringTheSameLoad() = runBlocking {
        var requests = 0
        withApi({ request ->
            when (++requests) {
                1 -> respond("Access denied.", HttpStatusCode.Forbidden, headersOf("X-TP-PoW-Challenge", OBFUSCATED_CHALLENGE))
                2 -> {
                    assertEquals(PROOF_COOKIE, request.headers[HttpHeaders.Cookie])
                    respond("Access denied.", HttpStatusCode.Forbidden, headersOf("X-TP-PoW-Challenge", FRESH_CHALLENGE))
                }
                else -> {
                    assertEquals(FRESH_PROOF_COOKIE, request.headers[HttpHeaders.Cookie])
                    assertEquals("2", request.headers["X-TP-PoW-Retry"])
                    respond(ROSTER_JSON)
                }
            }
        }) { api ->
            api.loadRoster(ROSTER_ID, StandardBB2025Rules()).getOrThrow()
            assertEquals(3, requests)
        }
    }

    @Test
    fun stopsAfterTwoRetries() = runBlocking {
        var requests = 0
        withApi({
            requests++
            respond("Access denied.", HttpStatusCode.Forbidden, headersOf("X-TP-PoW-Challenge", OBFUSCATED_CHALLENGE))
        }) { api ->
            val result = api.loadRoster(ROSTER_ID, StandardBB2025Rules())
            assertTrue(result.isFailure)
            assertEquals("Loading roster: $ROSTER_ID failed with status: 403 Forbidden", result.exceptionOrNull()?.message)
            assertEquals(3, requests)
        }
    }

    @Test
    fun doesNotRetryOrdinaryHttpErrors() = runBlocking {
        listOf(HttpStatusCode.Unauthorized, HttpStatusCode.Forbidden, HttpStatusCode.NotFound).forEach { status ->
            var requests = 0
            withApi({
                requests++
                respond("Access denied.", status)
            }) { api ->
                assertTrue(api.loadRoster(ROSTER_ID, StandardBB2025Rules()).isFailure)
                assertEquals(1, requests)
            }
        }
    }

    @Test
    fun doesNotRetryInvalidChallenges() = runBlocking {
        listOf(
            "invalid!",
            "nonce.invalid.2.15.signature".encodeUtf8().base64(),
            "nonce.2000000000.0.15.signature".encodeUtf8().base64(),
            "nonce.2000000000.7.15.signature".encodeUtf8().base64(),
            "nonce.2000000000.2".encodeUtf8().base64(),
            "nonce.2000000000.2.15.signature;extra=cookie".encodeUtf8().base64(),
        ).forEach { challenge ->
            var requests = 0
            withApi({
                requests++
                respond("Access denied.", HttpStatusCode.Forbidden, headersOf("X-TP-PoW-Challenge", challenge))
            }) { api ->
                assertTrue(api.loadRoster(ROSTER_ID, StandardBB2025Rules()).isFailure)
                assertEquals(1, requests, challenge)
            }
        }
    }

    @Test
    fun acceptsUuidRosterIds() = runBlocking {
        listOf(ROSTER_ID).forEach { rosterId ->
            withApi({ request ->
                assertEquals("https://tourplay.net/api/rosters/$ROSTER_ID", request.url.toString())
                assertEquals("https://tourplay.net/en/blood-bowl/roster/$ROSTER_ID", request.headers[HttpHeaders.Referrer])
                respond(ROSTER_JSON)
            }) { api ->
                val team = api.loadRoster(rosterId, StandardBB2025Rules()).getOrThrow().team
                assertEquals(ROSTER_ID, team.id.value.replace("-", "").lowercase())
            }
        }
    }

    @Test
    fun reportsUnexpectedHtmlResponse() = runBlocking {
        var requests = 0
        withApi({
            requests++
            respond("<!doctype html><html></html>", headers = headersOf(HttpHeaders.ContentType, ContentType.Text.Html.toString()))
        }) { api ->
            val result = api.loadRoster(ROSTER_ID, StandardBB2025Rules())
            val error = assertIs<IllegalStateException>(result.exceptionOrNull())
            assertTrue(error.message!!.contains("TourPlay returned HTML instead of roster JSON"))
            assertEquals(1, requests)
        }
    }

    private suspend fun withApi(handler: MockRequestHandler, block: suspend (TourPlayApi) -> Unit) {
        val client = HttpClient(MockEngine) {
            engine { addHandler(handler) }
        }
        try {
            block(TourPlayApi(client = client))
        } finally {
            client.close()
        }
    }

    companion object {
        private const val ROSTER_ID = "b1f1dbdc77b746948d273ce3977ddff6"
        private const val ROSTER_UUID = "b1f1dbdc-77b7-4694-8d27-3ce3977ddff6"
        private const val ANOTHER_ROSTER_ID = "23ae793222f6429d9f7054fdfac1c70f"
        private const val OBFUSCATED_CHALLENGE = "=FZPzvpQxzj6kyNW4vM2T0aTWsIVVQ3XHWJ6hc+eIvvkKdxq3Lf8+eiu6Fhw"
        private const val LEGACY_CHALLENGE = "dGVzdC1ub25jZS4yMDAwMDAwMDAwLjIuMTUudGVzdC1zaWduYXR1cmU="
        private const val FRESH_CHALLENGE = "nmRP6w5BxTp6Nydv4Xr2XEpYPzlWHkXcIac68QCELU+j91CpqD/4UdS86thw"
        private const val PROOF_COOKIE = "tp-pow=test-nonce.2000000000.2.c8.test-signature"
        private const val FRESH_PROOF_COOKIE = "tp-pow=fresh-nonce.2000000000.2.25.test-signature"
        private val ROSTER_JSON = """
            {
                "id": "$ROSTER_ID",
                "assistantCoaches": 0, "cheerLeaders": 0, "fanFactor": 1,
                "ruleSet": 4, "necromancer": false, "reRolls": 2,
                "shortTeamName": "Test", "teamColor": "000000",
                "teamName": "Test Team", "teamRace": "Human",
                "treasury": 0, "extraGoldQuantity": 0,
                "rosterMaster": {
                    "id": 1, "teamRace": 11, "name": "Human",
                    "lineUpMasters": [], "prizeReRoll": 50000,
                    "apothecary": true, "necromancer": false,
                    "teamRosterType": 0, "starPlayersMasters": [],
                    "ruleSet": 4, "teamSpecialRules": 0,
                    "selectableTeamSpecialRules": 0, "tier": 1
                },
                "teamSpecialRules": 0,
                "hasMatchesInProgress": false, "hasMatchesPlayed": false,
                "freeHireAndFireOrder": false, "isRedrafting": false,
                "partialPaymentStadium": 0, "teamValue": 1000,
                "teamValueVanilla": 1000, "inducementsValue": 0,
                "tournamentType": 0, "isHiddenRoster": false,
                "isFrozenRoster": false, "hasSppAdvancement": false,
                "isUsingGoldPiecesBudget": false, "state": 0,
                "player": {
                    "applicationUserId": "test", "userNameToShow": "Test Coach",
                    "avatarUrl": "", "country": "DK"
                },
                "coachRank": { "score": 0.0 }, "inscriptions": [],
                "hasOldPlayers": false, "lineUps": [], "lastMatches": []
            }
        """.trimIndent()
    }
}

private val iconMapping: TourPlayIconMapping by lazy {
    // The mapping files are Compose resources owned by `jervis-ui`, which this module does
    // not depend on, so the test reads them straight off disk instead.
    val resourceDir = File("../jervis-ui/shared/src/commonMain/composeResources/files")
    TourPlayIconMapping().apply {
        initialize(
            fumbblIcons = File(resourceDir, "fumbbl/icons.ini").readText(),
            fumbblExtraIcons = File(resourceDir, "fumbbl/icons-extra.ini").readText(),
            tourPlayIconSets = File(resourceDir, "tourplay/icons-iconsets.ini").readText(),
            tourPlayPortraits = File(resourceDir, "tourplay/icons-portraits.ini").readText(),
        )
    }
}
