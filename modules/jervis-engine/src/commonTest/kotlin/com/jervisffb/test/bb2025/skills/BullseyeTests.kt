package com.jervisffb.test.bb2025.skills

import com.jervisffb.engine.actions.Confirm
import com.jervisffb.engine.actions.DiceRollResults
import com.jervisffb.engine.actions.FieldSquareSelected
import com.jervisffb.engine.actions.PlayerSelected
import com.jervisffb.engine.ext.d6
import com.jervisffb.engine.ext.d8
import com.jervisffb.engine.ext.playerId
import com.jervisffb.engine.model.BallState
import com.jervisffb.engine.model.context.getContext
import com.jervisffb.engine.rules.bb2025.skills.Bullseye
import com.jervisffb.engine.rules.common.actions.PlayerStandardActionType
import com.jervisffb.engine.rules.common.procedures.actions.throwteammate.ThrowPlayerResult
import com.jervisffb.engine.rules.common.procedures.actions.throwteammate.ThrowTeamMateContext
import com.jervisffb.engine.rules.common.skills.SkillType
import com.jervisffb.test.JervisGameBB2025Test
import com.jervisffb.test.activatePlayer
import com.jervisffb.test.dodge
import com.jervisffb.test.ext.rollForward
import com.jervisffb.test.landingRoll
import com.jervisffb.test.moveTo
import com.jervisffb.test.qualityRoll
import com.jervisffb.test.utils.assertCoordinates
import com.jervisffb.test.utils.assertProne
import com.jervisffb.test.utils.assertStanding
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

/**
 * Class testing usage of the [Bullseye] skill.
 */
class BullseyeTests: JervisGameBB2025Test() {

    @BeforeTest
    override fun setUp() {
        setupAndStartThrowTeamMateGame()
    }

    @Test
    fun superbThrow() {
        val thrower = awayTeam["A1".playerId]
        thrower.addSkill(SkillType.BULLSEYE)
        val thrownPlayer = awayTeam["A13".playerId]
        controller.rollForward(
            *activatePlayer(thrower, PlayerStandardActionType.THROW_TEAM_MATE),
            *moveTo(14, 4),
            *dodge(),
            PlayerSelected(thrownPlayer),
            FieldSquareSelected(11, 4), // Quick Pass - No modifiers
            *qualityRoll(5.d6),
            Confirm, // Use Bullseye
            *landingRoll(6.d6)
        )
        thrownPlayer.assertStanding()
        thrownPlayer.assertCoordinates(11, 4)
    }

    @Test
    fun subparThrow() {
        val thrower = awayTeam["A1".playerId]
        thrower.addSkill(SkillType.BULLSEYE)
        val thrownPlayer = awayTeam["A13".playerId]
        controller.rollForward(
            *activatePlayer(thrower, PlayerStandardActionType.THROW_TEAM_MATE),
            *moveTo(14, 4),
            *dodge(),
            PlayerSelected(thrownPlayer),
            FieldSquareSelected(8, 4), // Short Pass
            *qualityRoll(4.d6)
        )
        assertEquals(ThrowPlayerResult.SUBPAR, state.getContext<ThrowTeamMateContext>().qualityRollResult)
        controller.rollForward(
            DiceRollResults(4.d8, 4.d8, 4.d8), // Scatter Player
            *landingRoll(6.d6)
        )

        thrownPlayer.assertStanding()
        thrownPlayer.assertCoordinates(5, 4)
    }

    @Test
    fun landOnBall() {
        val thrower = awayTeam["A1".playerId]
        thrower.addSkill(SkillType.BULLSEYE)
        val thrownPlayer = awayTeam["A13".playerId]
        controller.rollForward(
            *activatePlayer(thrower, PlayerStandardActionType.THROW_TEAM_MATE),
            *moveTo(14, 4),
            *dodge(),
            PlayerSelected(thrownPlayer),
            FieldSquareSelected(15, 5),
            *qualityRoll(6.d6),
            Confirm, // Use Bullseye
            *landingRoll(6.d6),
            7.d8
        )
        thrownPlayer.assertStanding()
        thrownPlayer.assertCoordinates(15, 5)
        val ball = state.singleBall()
        ball.assertCoordinates(15, 6)
        assertEquals(BallState.ON_GROUND, ball.state)
    }

    @Test
    fun landOnOpponentTeamMember() {
        val thrower = awayTeam["A1".playerId]
        thrower.addSkill(SkillType.BULLSEYE)
        val thrownPlayer = awayTeam["A13".playerId]
        controller.rollForward(
            *activatePlayer(thrower, PlayerStandardActionType.THROW_TEAM_MATE),
            *moveTo(14, 4),
            *dodge(),
            PlayerSelected(thrownPlayer),
            FieldSquareSelected(12, 5),
            *qualityRoll(6.d6),
            Confirm, // Use Bullseye
            DiceRollResults(1.d6, 1.d6), // AV Roll for player
            1.d8, // Bounce player
            DiceRollResults(1.d6, 1.d6), // AV Roll for thrown player
        )
        assertNull(state.activePlayer)
        thrownPlayer.assertCoordinates(11, 4)
        thrownPlayer.assertProne()
        homeTeam["H1".playerId].assertCoordinates(12, 5)
        homeTeam["H1".playerId].assertProne()
        assertEquals(awayTeam, state.activeTeam)
    }

    @Test
    fun landOnOwnTeamMember() {
        val thrower = awayTeam["A1".playerId]
        thrower.addSkill(SkillType.BULLSEYE)
        val thrownPlayer = awayTeam["A13".playerId]
        controller.rollForward(
            *activatePlayer(thrower, PlayerStandardActionType.THROW_TEAM_MATE),
            *moveTo(14, 4),
            *dodge(),
            PlayerSelected(thrownPlayer),
            FieldSquareSelected(13, 6),
            *qualityRoll(6.d6),
            Confirm, // Use Bullseye
            DiceRollResults(1.d6, 1.d6), // AV Roll for player
            5.d8, // Bounce player
            DiceRollResults(1.d6, 1.d6), // AV Roll for thrown player
        )
        assertNull(state.activePlayer)
        thrownPlayer.assertCoordinates(14, 6)
        thrownPlayer.assertProne()
        awayTeam["A2".playerId].assertCoordinates(13, 6)
        awayTeam["A2".playerId].assertProne()
        assertEquals(homeTeam, state.activeTeam)
    }
}
