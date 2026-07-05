package com.jervisffb.test

import com.jervisffb.engine.GameEngineController
import com.jervisffb.engine.actions.SetBallState
import com.jervisffb.engine.actions.SetPlayerState
import com.jervisffb.engine.actions.Undo
import com.jervisffb.engine.model.BallId
import com.jervisffb.engine.model.BallState
import com.jervisffb.engine.model.PlayerId
import com.jervisffb.engine.model.PlayerIntermediateState
import com.jervisffb.engine.model.PlayerState
import com.jervisffb.engine.model.locations.PitchCoordinate
import com.jervisffb.engine.rules.Rules
import com.jervisffb.engine.rules.StandardBB2020Rules
import com.jervisffb.engine.rules.builder.UndoActionBehavior
import com.jervisffb.engine.rules.common.procedures.FullGame
import com.jervisffb.engine.utils.InvalidActionException
import com.jervisffb.test.bb2020.createDefaultGameStateBB2020
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

class DevModeGameEngineControllerTests {

    private lateinit var controller: GameEngineController

    private fun createGameController(rules: Rules = devModeRules()): GameEngineController {
        val state = createDefaultGameStateBB2020(rules)
        controller = GameEngineController(state)
        controller.startTestMode(FullGame)
        return controller
    }

    private fun devModeRules(): Rules = StandardBB2020Rules().update {
        allowPlayerEditsDuringGame = true
    }

    private fun devModeRulesWithUndo(): Rules = StandardBB2020Rules().update {
        allowPlayerEditsDuringGame = true
        undoActionBehavior = UndoActionBehavior.ALLOWED
    }

    @Test
    fun setPlayerStateMovesPlayerAndSetsState() {
        val controller = createGameController()
        val player = controller.state.homeTeam[PlayerId("H1")]

        controller.handleAction(SetPlayerState(PlayerId("H1"), state = PlayerState.PRONE, x = 5, y = 10))

        assertEquals(PlayerState.PRONE, player.state)
        assertEquals(PitchCoordinate(5, 10), player.location)
        val square = controller.state.pitch[PitchCoordinate(5, 10)]
        assertEquals(player, square.player)
    }

    @Test
    fun setPlayerStateTackleZonesDerivedFromState() {
        val controller = createGameController()
        val player = controller.state.homeTeam[PlayerId("H1")]

        controller.handleAction(SetPlayerState(PlayerId("H1"), state = PlayerState.PRONE, x = 5, y = 10))
        assertFalse(player.hasTackleZones)

        controller.handleAction(SetPlayerState(PlayerId("H1"), state = PlayerState.STANDING, x = 6, y = 10))
        assertTrue(player.hasTackleZones)
    }

    @Test
    fun setPlayerStateRejectsOutOfBoundsCoordinate() {
        val controller = createGameController()

        assertFailsWith<InvalidActionException> {
            controller.handleAction(SetPlayerState(PlayerId("H1"), state = PlayerState.STANDING, x = -1, y = 5))
        }
    }

    @Test
    fun setPlayerStateIsUndoable() {
        val controller = createGameController(devModeRulesWithUndo())
        val player = controller.state.homeTeam[PlayerId("H1")]
        val originalLocation = player.location
        val originalState = player.state

        controller.handleAction(SetPlayerState(PlayerId("H1"), state = PlayerState.STUNNED, x = 7, y = 7))
        assertEquals(PitchCoordinate(7, 7), player.location)
        assertEquals(PlayerState.STUNNED, player.state)

        controller.handleAction(Undo)
        assertEquals(originalLocation, player.location)
        assertEquals(originalState, player.state)
    }

    @Test
    fun setPlayerStateResetsIntermediateState() {
        val controller = createGameController()
        val player = controller.state.homeTeam[PlayerId("H1")]
        player.intermediateState = PlayerIntermediateState.KNOCKED_DOWN

        controller.handleAction(SetPlayerState(PlayerId("H1"), state = PlayerState.STANDING, x = 5, y = 5))

        assertEquals(PlayerState.STANDING, player.state)
        assertNull(player.intermediateState)
    }

    @Test
    fun setBallStatePlacesBallOnPitch() {
        val controller = createGameController()
        val ball = controller.state.balls.first()

        controller.handleAction(SetBallState(BallId.DEFAULT, x = 8, y = 8, ballState = BallState.ON_GROUND))

        assertEquals(PitchCoordinate(8, 8), ball.coordinates)
        assertEquals(BallState.ON_GROUND, ball.state)
        val square = controller.state.pitch[PitchCoordinate(8, 8)]
        assertTrue(square.balls.contains(ball))
    }

    @Test
    fun setBallStateCanPlaceBallWithCarrier() {
        val controller = createGameController()
        val ball = controller.state.balls.first()
        val carrierId = PlayerId("H1")

        controller.handleAction(SetPlayerState(carrierId, state = PlayerState.STANDING, x = 5, y = 5))
        controller.handleAction(SetBallState(BallId.DEFAULT, x = 5, y = 5, ballState = BallState.CARRIED, carriedBy = carrierId))

        assertEquals(BallState.CARRIED, ball.state)
        assertNotNull(ball.carriedBy)
        assertEquals(carrierId, ball.carriedBy?.id)
        assertEquals(PitchCoordinate(5, 5), ball.resolvedLocation())
    }

    @Test
    fun setBallStateKeepsExistingBallStateWhenNotSpecified() {
        val controller = createGameController()
        val ball = controller.state.balls.first()
        val originalState = ball.state

        controller.handleAction(SetBallState(BallId.DEFAULT, x = 10, y = 5))

        assertEquals(PitchCoordinate(10, 5), ball.coordinates)
        assertEquals(originalState, ball.state)
    }

    @Test
    fun setBallStateIsUndoable() {
        val controller = createGameController(devModeRulesWithUndo())
        val ball = controller.state.balls.first()
        val originalCoordinates = ball.coordinates
        val originalState = ball.state

        controller.handleAction(SetBallState(BallId.DEFAULT, x = 12, y = 3, ballState = BallState.ON_GROUND))
        assertEquals(PitchCoordinate(12, 3), ball.coordinates)
        assertEquals(BallState.ON_GROUND, ball.state)

        controller.handleAction(Undo)
        assertEquals(originalCoordinates, ball.coordinates)
        assertEquals(originalState, ball.state)
    }
}
