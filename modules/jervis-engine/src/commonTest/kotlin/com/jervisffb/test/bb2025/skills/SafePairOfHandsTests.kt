package com.jervisffb.test.bb2025.skills

import com.jervisffb.engine.actions.Cancel
import com.jervisffb.engine.actions.Confirm
import com.jervisffb.engine.actions.DiceRollResults
import com.jervisffb.engine.actions.DirectionSelected
import com.jervisffb.engine.actions.PlayerActionSelected
import com.jervisffb.engine.actions.PlayerSelected
import com.jervisffb.engine.commands.SetBallState
import com.jervisffb.engine.commands.SetPlayerLocation
import com.jervisffb.engine.ext.d6
import com.jervisffb.engine.ext.d8
import com.jervisffb.engine.ext.dblock
import com.jervisffb.engine.ext.playerId
import com.jervisffb.engine.ext.playerNo
import com.jervisffb.engine.model.BallState
import com.jervisffb.engine.model.Direction
import com.jervisffb.engine.model.locations.FieldCoordinate
import com.jervisffb.engine.rules.bb2025.skills.SafePairOfHands
import com.jervisffb.engine.rules.common.actions.PlayerSpecialActionType
import com.jervisffb.engine.rules.common.actions.PlayerStandardActionType
import com.jervisffb.engine.rules.common.skills.SkillType
import com.jervisffb.test.JervisGameBB2025Test
import com.jervisffb.test.activatePlayer
import com.jervisffb.test.breatheFireRoll
import com.jervisffb.test.catch
import com.jervisffb.test.ext.rollForward
import com.jervisffb.test.moveTo
import com.jervisffb.test.rushRoll
import com.jervisffb.test.standardBlock
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

/**
 * Class testing usage of the [SafePairOfHands] skill.
 */

class SafePairOfHandsTests: JervisGameBB2025Test() {

    @BeforeTest
    override fun setUp() {
        super.setUp()
        startDefaultGame()
    }

    @Test
    fun worksOnKnockedDown() {
        val defender = state.getPlayerById("H1".playerId)
        defender.addSkill(SkillType.SAFE_PAIR_OF_HANDS)
        SetBallState.carried(state.singleBall(), defender).execute(state)
        controller.rollForward(
            *activatePlayer("A1", PlayerStandardActionType.BLOCK),
            *standardBlock("H1", 6.dblock),
            DirectionSelected(Direction.LEFT),
            Cancel, // Do not follow up
            Confirm, // Use Safe Pair of Hands
            DirectionSelected(Direction.UP_RIGHT),
            DiceRollResults(1.d6, 1.d6),
        )
        assertNull(state.activePlayer)
        assertEquals(BallState.ON_GROUND, state.singleBall().state)
        assertEquals(FieldCoordinate(12, 4), state.singleBall().coordinates)
    }

    @Test
    fun worksOnFallingOver() {
        val runner = state.getPlayerById("A8".playerId)
        runner.addSkill(SkillType.SAFE_PAIR_OF_HANDS)
        SetBallState.carried(state.singleBall(), runner).execute(state)
        controller.rollForward(
            PlayerSelected(runner),
            PlayerActionSelected(PlayerStandardActionType.MOVE),
            *moveTo(16, 13),
            *moveTo(17, 13),
            *moveTo(18, 13),
            *moveTo(19, 13),
            *moveTo(20, 13),
            *moveTo(21, 13),
            *moveTo(22, 13),
            *moveTo(23, 13),
            *rushRoll(1.d6),
            Confirm, // Use Safe Pair of Hands
            DirectionSelected(Direction.UP),
            DiceRollResults(1.d6, 1.d6),
        )
        assertNull(state.activePlayer)
        assertEquals(BallState.ON_GROUND, state.singleBall().state)
        assertEquals(FieldCoordinate(23, 12), state.singleBall().coordinates)
    }

    @Test
    fun worksOnBeingPlacedProne() {
        val attacker = state.getPlayerById("A1".playerId)
        attacker.addSkill(SkillType.BREATHE_FIRE)
        val defender = state.getPlayerById("H1".playerId)
        defender.addSkill(SkillType.SAFE_PAIR_OF_HANDS)
        SetBallState.carried(state.singleBall(), defender).execute(state)
        controller.rollForward(
            *activatePlayer(attacker, PlayerSpecialActionType.BREATHE_FIRE),
            PlayerSelected(defender),
            *breatheFireRoll(4.d6),
            Confirm, // Use Safe Pair of Hands
            DirectionSelected(Direction.LEFT),
        )
        assertNull(state.activePlayer)
        assertEquals(BallState.ON_GROUND, state.singleBall().state)
        assertEquals(FieldCoordinate(11, 5), state.singleBall().coordinates)
    }

    @Test
    fun doesNotWorkAgainstStripBall() {
        val attacker = awayTeam["A1".playerId]
        attacker.addSkill(SkillType.STRIP_BALL)
        val defender = homeTeam["H1".playerId]
        defender.addSkill(SkillType.SAFE_PAIR_OF_HANDS)
        val ball = state.singleBall()
        SetBallState.carried(ball, defender).execute(state)
        controller.rollForward(
            *activatePlayer(attacker, PlayerStandardActionType.BLOCK),
            *standardBlock("H1", 3.dblock),
            DirectionSelected(Direction.LEFT),
            Cancel, // Do not follow up
            Confirm, // Use Strip Ball
            5.d8, // Bounce. No chance to use Safe Pair of Hands.
        )
        assertNull(state.activePlayer)
        assertEquals(FieldCoordinate(12, 5), ball.coordinates)
        assertEquals(BallState.ON_GROUND, ball.state)
    }

    @Test
    fun doesNotWorkIfNoFreeTargetSquares() {
        awayTeam["A2".playerId].addSkill(SkillType.BLOCK)
        SetPlayerLocation(homeTeam[4.playerNo], FieldCoordinate(11, 4)).execute(state)
        SetPlayerLocation(homeTeam[10.playerNo], FieldCoordinate(11, 5)).execute(state)
        SetPlayerLocation(homeTeam[11.playerNo], FieldCoordinate(11, 6)).execute(state)
        SetBallState.carried(state.singleBall(), homeTeam["H2".playerId]).execute(state)
        controller.rollForward(
            *activatePlayer("A2", PlayerStandardActionType.BLOCK),
            *standardBlock("H2", 2.dblock),
            Confirm, // Use Block Skill. Cannot use Safe Pair of Hands as there are no room
            DiceRollResults(1.d6, 1.d6),
            4.d8, // Bounce
            *catch(1.d6, reroll = null),
            4.d8 // Bounce
        )
        assertNull(state.activePlayer)
        assertEquals(BallState.ON_GROUND, state.singleBall().state)
        assertEquals(FieldCoordinate(10, 6), state.singleBall().coordinates)
    }
}
