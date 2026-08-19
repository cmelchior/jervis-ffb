package com.jervisffb.ui.game.icons

import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.unit.Density
import com.jervisffb.engine.bb2025.StandardBB2025Rules
import com.jervisffb.engine.model.Coach
import com.jervisffb.engine.model.CoachId
import com.jervisffb.engine.model.Game
import com.jervisffb.engine.model.PlayerId
import com.jervisffb.engine.model.PlayerNo
import com.jervisffb.engine.model.PositionId
import com.jervisffb.engine.model.RosterId
import com.jervisffb.engine.model.Team
import com.jervisffb.engine.model.TeamId
import com.jervisffb.engine.sprites.SingleSprite
import com.jervisffb.engine.teamBuilder
import com.jervisffb.shared.generated.resources.Res
import com.jervisffb.test.bb2025.HUMAN_LINEMAN
import com.jervisffb.test.bb2025.HUMAN_TEAM_TEST_BB2025
import io.ktor.client.HttpClient
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.client.engine.mock.respondError
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.Url
import io.ktor.http.headersOf
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.async
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNotNull
import kotlin.test.assertSame
import kotlin.test.assertTrue

class IconFactoryTests {

    @Test
    fun networkFailuresUseFallbacksAndPublishTheTeamCache() = runTest {
        var requestCount = 0
        val client = HttpClient(MockEngine {
            requestCount++
            respondError(HttpStatusCode.ServiceUnavailable)
        })
        client.use { client ->
            val factory = IconFactory(client)
            val (homeTeam, awayTeam) = createTeams()

            factory.initializeStaticAssets(Density(1f))
            factory.initializeTeamAssets(homeTeam, awayTeam)

            assertEquals(4, requestCount)
            // Smoke test, ensuring we got "something" back from the cache.
            val player = homeTeam.first()
            assertTrue(factory.loadPlayerSprite(player, true).default.width > 0)
            assertTrue(factory.getPlayerPortrait(player.id).width > 0)
        }
    }

    @Test
    fun cancellationDiscardsTheTeamCacheAndAllowsRetry() = runTest {
        var requestCount = 0
        val client = HttpClient(MockEngine {
            requestCount++
            when (requestCount) {
                1 -> throw CancellationException("Canceled by test")
                else -> respondError(HttpStatusCode.ServiceUnavailable)
            }
        })
        client.use { client ->
            val factory = IconFactory(client)
            val (homeTeam, awayTeam) = createTeams()
            val player = homeTeam.first()

            factory.initializeStaticAssets(Density(1f))
            assertFailsWith<CancellationException> {
                factory.initializeTeamAssets(homeTeam, awayTeam)
            }
            assertEquals(1, requestCount)
            assertFailsWith<IllegalStateException> {
                factory.getPlayerPortrait(player.id)
            }

            factory.initializeTeamAssets(homeTeam, awayTeam)

            assertEquals(5, requestCount)
            assertTrue(factory.getPlayerPortrait(player.id).width > 0)
        }
    }

    @Test
    fun urlResourcesUseMemoryThenDiskThenNetwork() = runTest {
        val source = SingleSprite.url("https://icon-factory.test/player.png")
        val url = Url(source.resource)
        val events = mutableListOf<String>()
        var persistedImage: ImageBitmap? = null
        val imageBytes = Res.readBytes("files/jervis/portraits/default_portrait.png")
        val client = HttpClient(MockEngine {
            events.add("network")
            respond(
                content = imageBytes,
                headers = headersOf(HttpHeaders.ContentType, ContentType.Image.PNG.toString()),
            )
        })
        client.use { client ->
            val cache = TeamResourcesCache(
                httpClient = client,
                fumbblCache = emptyMap(),
                getCachedOnDiskImage = {
                    events.add("disk-read")
                    persistedImage
                },
                saveOnDiskCachedImage = { savedUrl, image ->
                    assertEquals(url, savedUrl)
                    events.add("disk-write")
                    persistedImage = image
                },
            )

            val downloaded = assertNotNull(cache.loadSprite(source))
            assertEquals(listOf("disk-read", "network", "disk-write"), events)

            events.clear()
            assertSame(downloaded, cache.loadSprite(source))
            assertTrue(events.isEmpty())

            val newCache = TeamResourcesCache(
                httpClient = client,
                fumbblCache = emptyMap(),
                getCachedOnDiskImage = {
                    events.add("disk-read")
                    persistedImage
                },
                saveOnDiskCachedImage = { _, _ -> events.add("disk-write") },
            )
            assertSame(downloaded, newCache.loadSprite(source))
            assertEquals(listOf("disk-read"), events)
        }
    }

    @Test
    fun fumbblResourcesUseResolvedUrlForDiskAndNetworkCaches() = runTest {
        val iniPath = "players/human.png"
        val resolvedUrl = Url("https://icon-factory.test/fumbbl-human.png")
        val source = SingleSprite.ini(iniPath)
        val diskReads = mutableListOf<Url>()
        val diskWrites = mutableListOf<Url>()
        var requestCount = 0
        val imageBytes = Res.readBytes("files/jervis/portraits/default_portrait.png")
        val client = HttpClient(MockEngine {
            requestCount++
            respond(
                content = imageBytes,
                headers = headersOf(HttpHeaders.ContentType, ContentType.Image.PNG.toString()),
            )
        })
        client.use { client ->
            val cache = TeamResourcesCache(
                httpClient = client,
                fumbblCache = mapOf(iniPath to resolvedUrl),
                getCachedOnDiskImage = { url ->
                    diskReads.add(url)
                    null
                },
                saveOnDiskCachedImage = { url, _ -> diskWrites.add(url) },
            )

            val image = assertNotNull(cache.loadSprite(source))
            assertSame(image, cache.loadSprite(source))
            assertEquals(listOf(resolvedUrl), diskReads)
            assertEquals(listOf(resolvedUrl), diskWrites)
            assertEquals(1, requestCount)
        }
    }

    @Test
    fun cancellationWhileSavingToDiskDoesNotPublishToMemory() = runTest {
        val source = SingleSprite.url("https://icon-factory.test/cancel-save.png")
        var requestCount = 0
        var saveCount = 0
        val imageBytes = Res.readBytes("files/jervis/portraits/default_portrait.png")
        val client = HttpClient(MockEngine {
            requestCount++
            respond(
                content = imageBytes,
                headers = headersOf(HttpHeaders.ContentType, ContentType.Image.PNG.toString()),
            )
        })
        client.use { client ->
            val cache = TeamResourcesCache(
                httpClient = client,
                fumbblCache = emptyMap(),
                getCachedOnDiskImage = { null },
                saveOnDiskCachedImage = { _, _ ->
                    saveCount++
                    if (saveCount == 1) throw CancellationException("Canceled while saving")
                },
            )

            assertFailsWith<CancellationException> { cache.loadSprite(source) }
            assertNotNull(cache.loadSprite(source))
            assertEquals(2, requestCount)
            assertEquals(2, saveCount)
        }
    }

    @Test
    fun teamInitializationCannotReplaceCacheWhileLogoIsLoading() = runTest {
        val requestStarted = CompletableDeferred<Unit>()
        val allowResponse = CompletableDeferred<Unit>()
        val imageBytes = Res.readBytes("files/jervis/portraits/default_portrait.png")
        val client = HttpClient(MockEngine {
            requestStarted.complete(Unit)
            allowResponse.await()
            respond(
                content = imageBytes,
                headers = headersOf(HttpHeaders.ContentType, ContentType.Image.PNG.toString()),
            )
        })
        client.use { client ->
            val factory = IconFactory(
                httpClient = client,
                getCachedOnDiskImage = { null },
                saveOnDiskCachedImage = { _, _ -> },
            )
            val logoTeamId = TeamId("logo-team")
            val logo = SingleSprite.url("https://icon-factory.test/logo.png")
            val (homeTeam, awayTeam) = createTeams(useRemoteImages = false)
            factory.initializeStaticAssets(Density(1f))

            val loadingLogo = async {
                factory.loadRosterIcon(logoTeamId, logo, LogoSize.LARGE)
            }
            requestStarted.await()
            factory.initializeTeamAssets(homeTeam, awayTeam)
            allowResponse.complete(Unit)

            val loadedLogo = assertNotNull(loadingLogo.await())
            assertSame(loadedLogo, factory.getLogo(logoTeamId, LogoSize.LARGE))
        }
    }

    private fun createTeams(useRemoteImages: Boolean = true): Pair<Team, Team> {
        val rules = StandardBB2025Rules()
        val homeTeam = createTeam(rules, "home", useRemoteImages)
        val awayTeam = createTeam(rules, "away", useRemoteImages)
        Game(rules, homeTeam, awayTeam)
        return homeTeam to awayTeam
    }

    private fun createTeam(rules: StandardBB2025Rules, id: String, useRemoteImages: Boolean): Team {
        val remoteSprite = when (useRemoteImages) {
            true -> SingleSprite.url("https://icon-factory.test/$id/player.png")
            false -> null
        }
        val position = HUMAN_LINEMAN.copy(
            id = PositionId("$id-lineman"),
            quantity = 1,
            icon = remoteSprite,
            portrait = remoteSprite,
        )
        val roster = HUMAN_TEAM_TEST_BB2025.copy(
            id = RosterId("$id-roster"),
            positions = listOf(position),
        )
        return teamBuilder(rules, roster) {
            this.id = TeamId(id)
            coach = Coach(CoachId("$id-coach"), "$id coach")
            name = "$id team"
            addPlayer(PlayerId("$id-player"), "$id player", PlayerNo(1), position)
        }
    }
}
