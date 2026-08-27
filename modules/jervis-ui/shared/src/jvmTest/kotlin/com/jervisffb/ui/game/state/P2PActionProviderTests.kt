package com.jervisffb.ui.game.state

import com.jervisffb.engine.ActionRequest
import com.jervisffb.engine.GameEngineController
import com.jervisffb.engine.GameSettings
import com.jervisffb.engine.actions.D3Result
import com.jervisffb.engine.actions.Dice
import com.jervisffb.engine.actions.GameAction
import com.jervisffb.engine.actions.GameActionDescriptor
import com.jervisffb.engine.actions.GameActionId
import com.jervisffb.engine.actions.Revert
import com.jervisffb.engine.actions.RollDice
import com.jervisffb.engine.bb2025.StandardBB2025Rules
import com.jervisffb.engine.commands.Command
import com.jervisffb.engine.commands.compositeCommandOf
import com.jervisffb.engine.commands.fsm.GotoNode
import com.jervisffb.engine.common.commands.SetDrive
import com.jervisffb.engine.common.procedures.FullGame
import com.jervisffb.engine.ext.d3
import com.jervisffb.engine.fsm.ActionNode
import com.jervisffb.engine.fsm.Procedure
import com.jervisffb.engine.model.Game
import com.jervisffb.engine.model.Team
import com.jervisffb.engine.rules.DiceRollType
import com.jervisffb.engine.rules.Rules
import com.jervisffb.test.bb2025.createDefaultGameStateBB2025
import com.jervisffb.ui.game.UiGameController
import com.jervisffb.ui.game.UiSnapshotAccumulator
import com.jervisffb.ui.menu.LocalPitchDataWrapper
import com.jervisffb.ui.menu.p2p.P2PClientNetworkAdapter
import kotlinx.coroutines.async
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.withTimeout
import kotlinx.coroutines.yield
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotEquals
import kotlin.time.Duration.Companion.seconds

class P2PActionProviderTests {

    private object RepeatingD3Rolls : Procedure() {
        override val initialNode = RollAgain

        override fun onEnterProcedure(state: Game, rules: Rules): Command? = null

        override fun onExitProcedure(state: Game, rules: Rules): Command? = null

        object RollAgain : ActionNode() {
            override fun actionOwner(state: Game, rules: Rules): Team? = null

            override fun getAvailableActions(state: Game, rules: Rules): List<GameActionDescriptor> =
                listOf(RollDice(Dice.D3, type = DiceRollType.FAN_FACTOR))

            override fun applyAction(action: GameAction, state: Game, rules: Rules): Command {
                val roll = action as D3Result
                return compositeCommandOf(
                    SetDrive(state.driveNo + roll.value),
                    GotoNode(this),
                )
            }
        }
    }

    private class WaitingActionProvider : UiActionProvider() {
        private val actions = Channel<AsyncGameAction>(Channel.UNLIMITED)

        override fun startHandler() = Unit

        override fun actionHandled(team: Team?, action: GameAction) = Unit

        override fun updateSharedData(sharedData: LocalPitchDataWrapper) = Unit

        override fun init(controller: UiGameController) = Unit

        override suspend fun prepareForNextAction(controller: GameEngineController, actions: ActionRequest) = Unit

        override fun decorateAvailableActions(actions: ActionRequest, acc: UiSnapshotAccumulator) = Unit

        override fun decorateSelectedAction(action: GameAction, acc: UiSnapshotAccumulator) = Unit

        override suspend fun getAction(id: GameActionId): GameAction {
            return actions.receive().also { assertEquals(id, it.id) }.action
        }

        override fun userActionSelected(id: GameActionId, action: GameAction) {
            check(actions.trySend(AsyncGameAction(id, action)).isSuccess)
        }

        override fun userMultipleActionsSelected(
            startingId: GameActionId,
            actions: List<GameAction>,
            delayEvent: Boolean,
        ) = Unit

        override fun registerQueuedActionGenerator(generator: QueuedActionsGenerator) = Unit

        override fun hasQueuedActions(): Boolean = false
    }

    private class Fixture(startingProcedure: Procedure = FullGame) {
        val rules = StandardBB2025Rules()
        val engine = GameEngineController(createDefaultGameStateBB2025(rules)).also {
            it.startTestMode(startingProcedure)
        }
        private val homeProvider = WaitingActionProvider()
        private val awayProvider = WaitingActionProvider()
        val provider = P2PActionProvider(
            engine = engine,
            settings = GameSettings(rules),
            homeProvider = homeProvider,
            awayProvider = awayProvider,
            networkAdapter = P2PClientNetworkAdapter(),
        )

        suspend fun applyLocalAction(action: GameAction) {
            provider.prepareForNextAction(engine, engine.getAvailableActions())
            val id = engine.nextActionIndex()
            provider.userActionSelected(id, action)
            assertEquals(action, provider.getAction(id))
            engine.handleAction(action)
        }

        suspend fun reconcileServerEvents(): List<GameAction> {
            val reconciliationActions = mutableListOf<GameAction>()
            while (true) {
                provider.prepareForNextAction(engine, engine.getAvailableActions())
                if (!provider.hasQueuedActions()) break

                check(reconciliationActions.size < 100) { "Server reconciliation did not finish" }
                val action = provider.getAction(engine.nextActionIndex())
                reconciliationActions.add(action)
                engine.handleAction(action)
                provider.actionHandled(null, action)
            }
            return reconciliationActions
        }

        fun close() {
            provider.stopHandler()
        }
    }

    @Test
    fun serverRejectionInterruptsAProviderWaitingForTheNextAction() = runTest {
        val fixture = Fixture()
        try {
            fixture.engine.handleAction(1.d3)
            fixture.provider.prepareForNextAction(fixture.engine, fixture.engine.getAvailableActions())

            val action = async { fixture.provider.getAction(fixture.engine.nextActionIndex()) }
            yield()
            assertFalse(action.isCompleted)

            fixture.provider.queueRejectedAction(GameActionId(1))

            assertEquals(Revert, withTimeout(5.seconds) { action.await() })
            fixture.engine.handleAction(Revert)
            assertEquals(GameActionId(0, 2), fixture.engine.currentActionId())
        } finally {
            fixture.close()
        }
    }

    @Test
    fun rejectionRollsBackThroughTheRejectedAction() = runTest {
        val fixture = Fixture()
        try {
            fixture.engine.handleAction(1.d3)
            fixture.engine.handleAction(2.d3)
            fixture.provider.queueRejectedAction(GameActionId(1))

            repeat(2) {
                fixture.provider.prepareForNextAction(fixture.engine, fixture.engine.getAvailableActions())
                val revert = fixture.provider.getAction(fixture.engine.nextActionIndex())
                assertEquals(Revert, revert)
                fixture.engine.handleAction(revert)
                fixture.provider.actionHandled(null, revert)
            }

            assertEquals(GameActionId(0, 3), fixture.engine.currentActionId())
        } finally {
            fixture.close()
        }
    }

    @Test
    fun synchronizedActionReceivedBeforeRejectionIsAppliedAfterRevert() = runTest {
        val fixture = Fixture()
        try {
            fixture.engine.handleAction(1.d3)
            fixture.provider.prepareForNextAction(fixture.engine, fixture.engine.getAvailableActions())

            // The server chose a different action at counter 1 while this client was applying its
            // own action. Network ordering allows the synchronized action to arrive before the
            // rejection of the client's conflicting action.
            fixture.provider.queueServerAction(GameActionId(1), 2.d3)
            fixture.provider.queueRejectedAction(GameActionId(1))

            val revert = fixture.provider.getAction(fixture.engine.nextActionIndex())
            assertEquals(Revert, revert)
            fixture.engine.handleAction(revert)
            fixture.provider.actionHandled(null, revert)

            fixture.provider.prepareForNextAction(fixture.engine, fixture.engine.getAvailableActions())
            val synchronizedAction = fixture.provider.getAction(fixture.engine.nextActionIndex())
            assertEquals(2.d3, synchronizedAction)
            fixture.engine.handleAction(synchronizedAction)
            fixture.provider.actionHandled(null, synchronizedAction)

            // The server stays on generation 1, while Revert starts generation 2 locally. They
            // still describe the same shared action counter and the authoritative action applies.
            assertEquals(GameActionId(1, 2), fixture.engine.currentActionId())

            // The rejection refers to the local action on generation 1, which was already
            // removed while reconciling the authoritative action. It must not cause another
            // Revert after that action has been applied on generation 2.
            fixture.provider.prepareForNextAction(fixture.engine, fixture.engine.getAvailableActions())
            val nextId = fixture.engine.nextActionIndex()
            fixture.provider.userActionSelected(nextId, 3.d3)
            assertEquals(3.d3, fixture.provider.getAction(nextId))
        } finally {
            fixture.close()
        }
    }

    @Test
    fun synchronizedActionsRemainOrderedWhileLocalTimelineIsReconciled() = runTest {
        val fixture = Fixture()
        try {
            fixture.engine.handleAction(1.d3)
            fixture.engine.handleAction(2.d3)
            fixture.provider.prepareForNextAction(fixture.engine, fixture.engine.getAvailableActions())

            fixture.provider.queueServerAction(GameActionId(1), 3.d3)
            fixture.provider.queueServerAction(GameActionId(2), 1.d3)
            fixture.provider.queueRejectedAction(GameActionId(1))
            fixture.provider.queueRejectedAction(GameActionId(2))

            repeat(2) {
                val revert = fixture.provider.getAction(fixture.engine.nextActionIndex())
                assertEquals(Revert, revert)
                fixture.engine.handleAction(revert)
                fixture.provider.actionHandled(null, revert)
                fixture.provider.prepareForNextAction(fixture.engine, fixture.engine.getAvailableActions())
            }

            listOf(3.d3, 1.d3).forEach { expectedAction ->
                val synchronizedAction = fixture.provider.getAction(fixture.engine.nextActionIndex())
                assertEquals(expectedAction, synchronizedAction)
                fixture.engine.handleAction(synchronizedAction)
                fixture.provider.actionHandled(null, synchronizedAction)
                fixture.provider.prepareForNextAction(fixture.engine, fixture.engine.getAvailableActions())
            }

            assertEquals(GameActionId(2, 3), fixture.engine.currentActionId())
        } finally {
            fixture.close()
        }
    }

    @Test
    fun twoClientsConvergeAfterOptimisticActions() = runTest {
        val firstClient = Fixture(RepeatingD3Rolls)
        val secondClient = Fixture(RepeatingD3Rolls)
        val authoritativeEngine = Fixture(RepeatingD3Rolls)
        try {
            val actionsPerClient = 11
            val firstClientActions = List(actionsPerClient) { ((it % 3) + 1).d3 }
            val secondClientActions = List(actionsPerClient) { (((it + 1) % 3) + 1).d3 }
            val authoritativeActions = List(actionsPerClient) { (((it + 2) % 3) + 1).d3 }

            assertEquals(firstClient.engine.currentActionId(), secondClient.engine.currentActionId())
            assertEquals(firstClient.engine.state.driveNo, secondClient.engine.state.driveNo)

            firstClientActions.forEach { firstClient.applyLocalAction(it) }
            secondClientActions.forEach { secondClient.applyLocalAction(it) }
            authoritativeActions.forEach { authoritativeEngine.engine.handleAction(it) }

            assertEquals(GameActionId(actionsPerClient), firstClient.engine.currentActionId())
            assertEquals(GameActionId(actionsPerClient), secondClient.engine.currentActionId())
            assertNotEquals(firstClientActions, secondClientActions)
            assertNotEquals(firstClient.engine.state.driveNo, secondClient.engine.state.driveNo)

            // Simulate the server delivering the complete authoritative timeline before it
            // rejects either client's buffered optimistic actions.
            listOf(firstClient, secondClient).forEach { client ->
                authoritativeActions.forEachIndexed { index, action ->
                    client.provider.queueServerAction(GameActionId(index + 1), action)
                }
                repeat(actionsPerClient) { index ->
                    client.provider.queueRejectedAction(GameActionId(index + 1))
                }
            }

            val expectedReconciliation = List(actionsPerClient) { Revert } + authoritativeActions
            assertEquals(expectedReconciliation, firstClient.reconcileServerEvents())
            assertEquals(expectedReconciliation, secondClient.reconcileServerEvents())

            val authoritativeHistory = authoritativeEngine.engine.history.flatMap { delta ->
                delta.steps.map { it.action }
            }
            listOf(firstClient, secondClient).forEach { client ->
                val clientHistory = client.engine.history.flatMap { delta ->
                    delta.steps.map { it.action }
                }
                assertEquals(authoritativeHistory, clientHistory)
                assertEquals(authoritativeEngine.engine.currentNode(), client.engine.currentNode())
                assertEquals(authoritativeEngine.engine.state.driveNo, client.engine.state.driveNo)
                assertEquals(GameActionId(actionsPerClient, actionsPerClient + 1), client.engine.currentActionId())
            }
        } finally {
            firstClient.close()
            secondClient.close()
            authoritativeEngine.close()
        }
    }

}
