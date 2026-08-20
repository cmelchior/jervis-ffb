package com.jervisffb.ui.menu.p2p

import com.jervisffb.engine.bb2025.StandardBB2025Rules
import com.jervisffb.engine.model.Coach
import com.jervisffb.engine.model.CoachId
import com.jervisffb.engine.model.CoachType
import com.jervisffb.engine.model.TeamId
import com.jervisffb.engine.serialization.SerializedTeam
import com.jervisffb.net.messages.GameStateSyncMessage
import com.jervisffb.net.messages.P2PClientState
import com.jervisffb.net.messages.P2PHostState
import com.jervisffb.resources.bb2025.BB2025StandaloneStandardTeams
import com.jervisffb.ui.game.model.ModelRef
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull

/**
 * Tests for how [P2PClientNetworkAdapter] tracks the state of a game session.
 *
 * The interesting case is rejoining: the Host can go back, shut the server down and start a new
 * one. When they pick the same team again, everything the Client is told on the second join is
 * equal to what it already has, so nothing may be left over from the previous session.
 */
class P2PClientNetworkAdapterTests {

    private val rules = StandardBB2025Rules()
    private val hostCoach = Coach(CoachId("host-coach"), "Host", CoachType.HUMAN)
    private val clientCoach = Coach(CoachId("client-coach"), "Client", CoachType.HUMAN)

    // Reuse a bundled team, so the team id is realistic.
    private val hostTeam: SerializedTeam =
        BB2025StandaloneStandardTeams.defaultTeams.getValue("human-starter-team-bb2025.jrt").team

    private fun syncMessage(homeTeam: SerializedTeam?) = GameStateSyncMessage(
        rules = rules,
        coaches = listOfNotNull(hostCoach),
        spectators = emptyList(),
        hostState = P2PHostState.WAIT_FOR_CLIENT,
        clientState = P2PClientState.SELECT_TEAM,
        homeTeam = homeTeam,
        awayTeam = null,
    )

    @Test
    fun connectingClearsStateFromThePreviousSession() {
        val adapter = P2PClientNetworkAdapter()
        val handler = adapter.GameStateMessageHandler()

        handler.onConnected()
        handler.onGameSync(syncMessage(hostTeam))
        assertEquals(hostTeam.id, adapter.homeTeam.value?.model?.id)
        assertNotNull(adapter.homeCoach.value)

        // The Host shut the server down, and we are connecting to the new one.
        handler.onConnected()
        assertNull(adapter.homeTeam.value, "Host team from the previous session leaked into the new one")
        assertNull(adapter.awayTeam.value)
        assertNull(adapter.homeCoach.value)
        assertNull(adapter.awayCoach.value)
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun rejoiningWithTheSameHostTeamIsVisibleToCollectors() = runTest {
        val adapter = P2PClientNetworkAdapter()
        val handler = adapter.GameStateMessageHandler()
        val seen = mutableListOf<ModelRef<*>?>()
        val job = launch(UnconfinedTestDispatcher(testScheduler)) {
            adapter.homeTeam.toList(seen)
        }

        handler.onConnected()
        handler.onGameSync(syncMessage(hostTeam))
        handler.onConnected()
        handler.onGameSync(syncMessage(hostTeam))
        job.cancel()

        // `ModelRef` only compares its key, and `MutableStateFlow` drops values equal to the
        // current one. Without the reset on connect, the second sync would be silently swallowed
        // and anything driven by this flow, such as marking the Host team unavailable in the
        // Client's team selector, would never run again.
        val teamIds = seen.map { (it?.key as? TeamId)?.value }
        assertEquals(listOf(null, hostTeam.id.value, null, hostTeam.id.value), teamIds)
    }

    @Test
    fun coachesCanJoinAgainInANewSession() {
        val adapter = P2PClientNetworkAdapter()
        val handler = adapter.GameStateMessageHandler()

        handler.onConnected()
        handler.onCoachJoined(hostCoach, isHomeCoach = true)
        handler.onCoachJoined(clientCoach, isHomeCoach = false)
        assertEquals(hostCoach.id, adapter.homeCoach.value?.id)
        assertEquals(clientCoach.id, adapter.awayCoach.value?.id)

        // Both slots being full previously made the adapter ignore every later join, so a rejoin
        // left the Client working from the coaches of the session that had already ended.
        handler.onConnected()
        handler.onCoachJoined(hostCoach, isHomeCoach = true)
        assertEquals(hostCoach.id, adapter.homeCoach.value?.id)
        assertNull(adapter.awayCoach.value)
    }
}
