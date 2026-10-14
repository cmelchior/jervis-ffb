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
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.headersOf
import kotlinx.coroutines.runBlocking
import okio.ByteString.Companion.encodeUtf8
import java.io.File
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

class TourPlayRestApiTests {

    private lateinit var api: TourPlayApi

    @BeforeTest
    fun setUp() {
        api = TourPlayApi(iconMapping)
    }

    @Test
    fun teamLoader() = runBlocking {
        val rules = FumbblBB2020Rules()
        val file = api.loadRoster(44442, rules)
        val team = SerializedTeam.deserialize(rules, file.getOrThrow().team, Coach.UNKNOWN)
        assertEquals(team.name, "Lustrian Hurricanes")
    }

    @Test
    fun teamLoader2() = runBlocking {
        val rules = FumbblBB2020Rules()
        val file = api.loadRoster(131784, rules)
        val team = SerializedTeam.deserialize(rules, file.getOrThrow().team, Coach.UNKNOWN)
        assertEquals(team.name, "Gramps' Vamps")
    }

    // Test for https://github.com/cmelchior/jervis-ffb/issues/61
    @Test
    fun load214739() = runBlocking {
        val rules = StandardBB2025Rules()
        val file = api.loadRoster(214739, rules)
        val team = SerializedTeam.deserialize(rules, file.getOrThrow().team, Coach.UNKNOWN)
        assertEquals("Hafenland Capers", team.name)
    }

    // Test for https://github.com/cmelchior/jervis-ffb/issues/62
    @Test
    fun teamLoaderUsesFumbblIcons() = runBlocking {
        // Old World Alliance and Chaos Chosen between them cover roster-qualified entries,
        // bare entries and positions renamed between BB2020 and BB2025.
        listOf(
            214739L to "Old World Alliance",
            176917L to "Chaos Chosen"
        ).forEach { (id, roster) ->
            val rules = StandardBB2025Rules()
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

    // Test for https://github.com/cmelchior/jervis-ffb/issues/80 (BB7 Team)
    @Test
    fun load227396() = runBlocking {
        val rules = BB7Rules2025()
        val file = api.loadRoster(227396, rules)
        val team = SerializedTeam.deserialize(rules, file.getOrThrow().team, Coach.UNKNOWN)
        assertEquals("Bretonnridge Brawlers", team.name)
    }

    @Test
    fun load176917() = runBlocking {
        val rules = StandardBB2025Rules()
        val file = api.loadRoster(176917, rules)
        val team = SerializedTeam.deserialize(rules, file.getOrThrow().team, Coach.UNKNOWN)
        assertEquals("Khazra’s Den 26", team.name)
    }

    @Test
    fun teamLoaderKeepsTheSelectedLeague() = runBlocking {
        // TourPlay tracks which league a team plays in, and that decides which Star
        // Players it may hire. Both rosters here only play in a single league, but taking
        // the team's own choice is what makes rosters playing in several, e.g. Chaos
        // Dwarf, import correctly.
        listOf(
            176917L to RegionalSpecialRule.CHAOS_CLASH,
            170509L to RegionalSpecialRule.ELVEN_KINGDOMS_LEAGUE,
        ).forEach { (id, league) ->
            val rules = StandardBB2025Rules()
            val file = api.loadRoster(id, rules)
            val team = SerializedTeam.deserialize(rules, file.getOrThrow().team, Coach.UNKNOWN)
            assertEquals(league, team.league, team.name)
            assertTrue(league in team.allSpecialRules, team.name)
        }
    }

    @Test
    fun load170509() = runBlocking {
        val rules = StandardBB2025Rules()
        val file = api.loadRoster(170509, rules)
        val team = SerializedTeam.deserialize(rules, file.getOrThrow().team, Coach.UNKNOWN)
        assertEquals("ScrewCrew", team.name)
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
            api.loadRoster(1, rules).getOrThrow()
            api.loadRoster(2, rules).getOrThrow()
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
            api.loadRoster(1, rules).getOrThrow()
            api.loadRoster(2, rules).getOrThrow()
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
            api.loadRoster(1, StandardBB2025Rules()).getOrThrow()
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
            val result = api.loadRoster(1, StandardBB2025Rules())
            assertTrue(result.isFailure)
            assertEquals("Loading roster: 1 failed with status: 403 Forbidden", result.exceptionOrNull()?.message)
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
                assertTrue(api.loadRoster(1, StandardBB2025Rules()).isFailure)
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
                assertTrue(api.loadRoster(1, StandardBB2025Rules()).isFailure)
                assertEquals(1, requests, challenge)
            }
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
        private const val OBFUSCATED_CHALLENGE = "=FZPzvpQxzj6kyNW4vM2T0aTWsIVVQ3XHWJ6hc+eIvvkKdxq3Lf8+eiu6Fhw"
        private const val LEGACY_CHALLENGE = "dGVzdC1ub25jZS4yMDAwMDAwMDAwLjIuMTUudGVzdC1zaWduYXR1cmU="
        private const val FRESH_CHALLENGE = "nmRP6w5BxTp6Nydv4Xr2XEpYPzlWHkXcIac68QCELU+j91CpqD/4UdS86thw"
        private const val PROOF_COOKIE = "tp-pow=test-nonce.2000000000.2.c8.test-signature"
        private const val FRESH_PROOF_COOKIE = "tp-pow=fresh-nonce.2000000000.2.25.test-signature"
        private val ROSTER_JSON = """
            {
                "id": 1,
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
