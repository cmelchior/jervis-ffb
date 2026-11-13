package com.jervisffb.ui.game.state.actionwheel

import com.jervisffb.engine.GameEngineController
import com.jervisffb.engine.GameSettings
import com.jervisffb.engine.actions.EndAction
import com.jervisffb.engine.actions.GameAction
import com.jervisffb.engine.actions.MoveType
import com.jervisffb.engine.actions.MoveTypeSelected
import com.jervisffb.engine.actions.NoRerollSelected
import com.jervisffb.engine.actions.PlayerActionSelected
import com.jervisffb.engine.actions.PlayerSelected
import com.jervisffb.engine.bb2025.StandardBB2025Rules
import com.jervisffb.engine.common.procedures.FullGame
import com.jervisffb.engine.ext.d6
import com.jervisffb.engine.ext.playerId
import com.jervisffb.engine.model.PlayerPitchState
import com.jervisffb.engine.rules.common.actions.PlayerStandardActionType
import com.jervisffb.engine.rules.common.skills.SkillType
import com.jervisffb.test.bb2025.createDefaultGameStateBB2025
import com.jervisffb.test.defaultBB2025Pregame
import com.jervisffb.test.defaultKickOffHomeTeam
import com.jervisffb.test.defaultSetup
import com.jervisffb.test.ext.rollForward
import com.jervisffb.test.utils.putProne
import com.jervisffb.ui.game.state.ManualActionProvider
import com.jervisffb.ui.game.viewmodel.MenuViewModel
import com.jervisffb.ui.menu.TeamActionMode
import kotlinx.coroutines.test.runTest
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

/**
 * Tests for the "Stand Up & End Action" option in [SelectPlayerActionWheelController].
 */
class SelectPlayerActionWheelControllerTests {

    private val rules = StandardBB2025Rules()
    private val state = createDefaultGameStateBB2025(rules)
    private val controller = GameEngineController(state)
    private val menuViewModel = MenuViewModel().apply {
        // Settings-based automation would make the tests depend on local settings.
        enableAutomatedActions = false
    }
    private val provider = ManualActionProvider(controller, menuViewModel, TeamActionMode.ALL_TEAMS, GameSettings(rules))
    private val player = state.getPlayerById("A1".playerId)

    @BeforeTest
    fun setUp() {
        controller.startTestMode(FullGame)
        controller.rollForward(
            *defaultBB2025Pregame(),
            *defaultSetup(),
            *defaultKickOffHomeTeam(),
        )
        player.addSkill(SkillType.REALLY_STUPID)
        player.putProne()
        controller.rollForward(PlayerSelected(player.id))
    }

    @AfterTest
    fun tearDown() {
        provider.stopHandler()
    }

    // Regression test for https://github.com/cmelchior/jervis-ffb/issues/98
    @Test
    fun standUpAfterPassingNegaTrait() = runTest {
        selectStandUpAndEndAction()
        nextAction(6.d6) // Really Stupid
        nextAction(NoRerollSelected())
        assertEquals(MoveTypeSelected(MoveType.STAND_UP), nextQueuedAction())
        assertEquals(EndAction, nextQueuedAction())
        assertNull(state.activePlayer)
        assertEquals(PlayerPitchState.STANDING, player.state)
    }

    @Test
    fun queuedActionsAreDroppedIfActivationEnds() = runTest {
        selectStandUpAndEndAction()
        nextAction(1.d6) // Fail Really Stupid
        nextAction(NoRerollSelected())
        assertNull(state.activePlayer)
        assertEquals(PlayerPitchState.PRONE, player.state)

        // The queued actions must not be applied to the next player moving.
        val nextPlayer = state.getPlayerById("A2".playerId)
        nextAction(PlayerSelected(nextPlayer.id))
        nextAction(PlayerActionSelected(PlayerStandardActionType.MOVE))
        provider.prepareForNextAction(controller, controller.getAvailableActions())
        assertFalse(provider.hasQueuedActions())
        assertEquals(nextPlayer, state.activePlayer)
    }

    // Simulates the coach clicking "Stand Up & End Action" in the action wheel
    private suspend fun selectStandUpAndEndAction() {
        val request = controller.getAvailableActions()
        provider.prepareForNextAction(controller, request)
        SelectPlayerActionWheelController.standUpAndEndAction(provider, request.id, player.id)
        val action = provider.getAction(request.id)
        assertEquals(PlayerActionSelected(PlayerStandardActionType.MOVE), action)
        controller.handleAction(action)
    }

    // Sends an action selected by the coach through the provider, the same way as the game loop does
    private suspend fun nextAction(userAction: GameAction) {
        val request = controller.getAvailableActions()
        provider.prepareForNextAction(controller, request)
        assertFalse(provider.hasQueuedActions(), "Unexpected queued action at ${controller.currentNode()}")
        provider.userActionSelected(request.id, userAction)
        controller.handleAction(provider.getAction(request.id))
    }

    // Applies the next action queued up by the provider, the same way as the game loop does
    private suspend fun nextQueuedAction(): GameAction {
        val request = controller.getAvailableActions()
        provider.prepareForNextAction(controller, request)
        assertTrue(provider.hasQueuedActions(), "Expected a queued action at ${controller.currentNode()}")
        return provider.getAction(request.id).also { controller.handleAction(it) }
    }
}
