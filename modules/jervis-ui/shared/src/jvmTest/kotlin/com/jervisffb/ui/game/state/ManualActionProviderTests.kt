package com.jervisffb.ui.game.state

import com.jervisffb.engine.GameEngineController
import com.jervisffb.engine.GameSettings
import com.jervisffb.engine.actions.GameActionId
import com.jervisffb.engine.bb2025.StandardBB2025Rules
import com.jervisffb.engine.ext.d3
import com.jervisffb.test.bb2025.createDefaultGameStateBB2025
import com.jervisffb.ui.game.viewmodel.MenuViewModel
import com.jervisffb.ui.menu.TeamActionMode
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.async
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.withContext
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class ManualActionProviderTests {

    private class TestActionProvider(rules: StandardBB2025Rules = StandardBB2025Rules()) : ManualActionProvider(
        GameEngineController(createDefaultGameStateBB2025(rules)),
        MenuViewModel(),
        TeamActionMode.ALL_TEAMS,
        GameSettings(rules),
    ) {
        // Submit both clicks before the action dispatcher can process either one.
        suspend fun submitTogether(block: TestActionProvider.() -> Unit) {
            withContext(actionScope.coroutineContext) { block() }
            withContext(actionScope.coroutineContext) { }
        }
    }

    @Test
    fun duplicateBatchesKeepOnlyTheFirstSequence() = runTest {
        val provider = TestActionProvider()
        try {
            val id = GameActionId(262)
            val firstAction = async(start = CoroutineStart.UNDISPATCHED) { provider.getAction(id) }
            provider.submitTogether {
                userMultipleActionsSelected(id, listOf(1.d3, 2.d3), delayEvent = false)
                userMultipleActionsSelected(id, listOf(3.d3, 3.d3), delayEvent = false)
            }

            assertEquals(1.d3, firstAction.await())
            assertEquals(2.d3, provider.getAction(id + 1))
            assertFalse(provider.hasQueuedActions())

            // The duplicate's remaining actions must also be discarded, even when
            // they have ids that could match later requests.
            provider.userActionSelected(id + 2, 1.d3)
            assertEquals(1.d3, provider.getAction(id + 2))
        } finally {
            provider.stopHandler()
        }
    }

    @Test
    fun aBatchCanArriveBeforeTheGameLoopWaitsForIt() = runTest {
        val provider = TestActionProvider()
        try {
            val id = GameActionId(1)
            provider.submitTogether {
                userMultipleActionsSelected(id, listOf(1.d3, 2.d3, 3.d3), delayEvent = false)
            }

            assertEquals(1.d3, provider.getAction(id))
            assertTrue(provider.hasQueuedActions())
            assertEquals(2.d3, provider.getAction(id + 1))
            assertEquals(3.d3, provider.getAction(id + 2))
            assertFalse(provider.hasQueuedActions())
        } finally {
            provider.stopHandler()
        }
    }

    @Test
    fun aSingleActionWinningOverABatchDiscardsTheWholeBatch() = runTest {
        val provider = TestActionProvider()
        try {
            val id = GameActionId(1)
            val firstAction = async(start = CoroutineStart.UNDISPATCHED) { provider.getAction(id) }
            provider.submitTogether {
                userActionSelected(id, 1.d3)
                userMultipleActionsSelected(id, listOf(3.d3, 3.d3), delayEvent = false)
            }

            assertEquals(1.d3, firstAction.await())
            provider.userActionSelected(id + 1, 2.d3)
            assertEquals(2.d3, provider.getAction(id + 1))
            assertFalse(provider.hasQueuedActions())
        } finally {
            provider.stopHandler()
        }
    }

    @Test
    fun aStaleBatchCannotContributeActionsToTheCurrentRequest() = runTest {
        val provider = TestActionProvider()
        try {
            val id = GameActionId(2)
            provider.submitTogether {
                userMultipleActionsSelected(id - 1, listOf(3.d3, 3.d3, 3.d3), delayEvent = false)
                userActionSelected(id, 1.d3)
            }

            assertEquals(1.d3, provider.getAction(id))
            provider.userActionSelected(id + 1, 2.d3)
            assertEquals(2.d3, provider.getAction(id + 1))
            assertFalse(provider.hasQueuedActions())
        } finally {
            provider.stopHandler()
        }
    }

    @Test
    fun batchesFromAnOlderTimelineAreDiscarded() = runTest {
        val provider = TestActionProvider()
        try {
            val id = GameActionId(5, 2)
            provider.submitTogether {
                userMultipleActionsSelected(GameActionId(500, 1), listOf(3.d3, 3.d3), delayEvent = false)
                userActionSelected(id, 1.d3)
            }

            assertEquals(1.d3, provider.getAction(id))
            assertFalse(provider.hasQueuedActions())
        } finally {
            provider.stopHandler()
        }
    }

    @Test
    fun futureBatchesStillFailWithoutQueuingTheirRemainingActions() = runTest {
        val provider = TestActionProvider()
        try {
            val id = GameActionId(1)
            provider.submitTogether {
                userMultipleActionsSelected(id + 1, listOf(2.d3, 3.d3), delayEvent = false)
            }

            val error = assertFailsWith<IllegalStateException> { provider.getAction(id) }
            assertEquals("Future action id received: ${id + 1} > $id", error.message)
            assertFalse(provider.hasQueuedActions())
        } finally {
            provider.stopHandler()
        }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun delaysBelongToTheAcceptedBatch() = runTest {
        val provider = TestActionProvider()
        try {
            val id = GameActionId(1)
            val firstAction = async(start = CoroutineStart.UNDISPATCHED) { provider.getAction(id) }
            provider.submitTogether {
                userMultipleActionsSelected(id, listOf(1.d3, 2.d3), delayEvent = true)
                userMultipleActionsSelected(id, listOf(3.d3, 3.d3), delayEvent = false)
            }

            assertEquals(1.d3, firstAction.await())
            assertEquals(0L, testScheduler.currentTime)
            assertEquals(2.d3, provider.getAction(id + 1))
            assertEquals(150L, testScheduler.currentTime)
            assertFalse(provider.hasQueuedActions())

            provider.userMultipleActionsSelected(id + 2, listOf(1.d3, 2.d3), delayEvent = false)
            assertEquals(1.d3, provider.getAction(id + 2))
            assertEquals(2.d3, provider.getAction(id + 3))
            assertEquals(150L, testScheduler.currentTime)
        } finally {
            provider.stopHandler()
        }
    }
}
