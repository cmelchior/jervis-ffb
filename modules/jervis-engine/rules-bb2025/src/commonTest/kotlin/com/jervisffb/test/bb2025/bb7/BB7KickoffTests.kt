package com.jervisffb.test.bb2025.bb7

import com.jervisffb.engine.actions.Confirm
import com.jervisffb.engine.actions.DiceRollResults
import com.jervisffb.engine.actions.PitchSquareSelected
import com.jervisffb.engine.actions.PlayerSelected
import com.jervisffb.engine.actions.SelectPitchLocation
import com.jervisffb.engine.ext.d6
import com.jervisffb.engine.ext.d8
import com.jervisffb.engine.ext.playerId
import com.jervisffb.engine.model.BallState
import com.jervisffb.engine.rules.common.skills.SkillType
import com.jervisffb.test.JervisGameBB72025Test
import com.jervisffb.test.defaultBB2020Pregame
import com.jervisffb.test.defaultKickOffEvent
import com.jervisffb.test.ext.rollForward
import com.jervisffb.test.utils.assertActive
import com.jervisffb.test.utils.assertCoordinates
import com.jervisffb.test.utils.assertNoActivePlayer
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 * Tests for BB7 specific rules not covered by [com.jervisffb.test.bb2020.tables.KickOffEventTests].
 */
class BB7KickoffTests: JervisGameBB72025Test() {

    @Test
    fun deviate2D6OnKickOff() {
        controller.rollForward(
            *defaultBB2020Pregame(),
            *defaultSetup(),
            *defaultKickOffHomeTeam(
                placeKick = PitchSquareSelected(14, 5),
                deviate = DiceRollResults(5.d8, 6.d6, 2.d6), // Deviate to [16, 5]
                bounce = 2.d8 // Bounce to [16,4]
            )
        )
        state.singleBall().assertCoordinates(16, 4)
        assertEquals(BallState.ON_GROUND, state.singleBall().state)
    }

    @Test
    fun placeKick_notInNoMansLand() {
        controller.rollForward(
            *defaultBB2020Pregame(),
            *defaultSetup()
        )
        controller.rollForward(
            PlayerSelected("H1".playerId)
        )
        val action = controller.getAvailableActions().get<SelectPitchLocation>()
        assertEquals(77, action.squares.size)
        assertTrue(action.squares.none {
            it.x < rules.lineOfScrimmageAway
        })
    }


    @Test
    fun touchback_notAwardedWhenLandingInNoMansLand() {
        controller.rollForward(
            *defaultBB2020Pregame(),
            *defaultSetup(),
            *defaultKickOffHomeTeam(
                placeKick = PitchSquareSelected(14, 5),
                deviate = DiceRollResults(4.d8, 6.d6, 6.d6), // Deviate to [8, 5]
                bounce = 2.d8 // Bounce to [8,4]
            )
        )
        state.singleBall().assertCoordinates(8, 4)
        assertEquals(BallState.ON_GROUND, state.singleBall().state)
        awayTeam.assertActive()
        state.assertNoActivePlayer()
        assertEquals(1, awayTeam.turnMarker)
    }

    // Player failed to catch ball and ball bounced into No Man's Land.
    @Test
    fun touchback_afterCatch_notAwardedWhenLandingInNoMansLand() {
        controller.rollForward(
            *defaultBB2020Pregame(),
            *defaultSetup(),
            *defaultKickOffHomeTeam(
                placeKick = PitchSquareSelected(15, 5),
                deviate = DiceRollResults(4.d8, 2.d6, 6.d6), // Deviate to [13, 5]
                bounce = null,
            ),
            1.d6, // Fail catch
            4.d8, // Bounce to 12, 5
        )
        state.singleBall().assertCoordinates(12, 5)
        assertEquals(BallState.ON_GROUND, state.singleBall().state)
        awayTeam.assertActive()
        state.assertNoActivePlayer()
        assertEquals(1, awayTeam.turnMarker)
    }

    @Test
    fun usingKickOnDeviate() {
        val kicker = homeTeam["H1".playerId].also {
            it.addSkill(SkillType.KICK)
        }
        controller.rollForward(
            *defaultBB2020Pregame(),
            *defaultSetup(),
            PlayerSelected(kicker),
            PitchSquareSelected(15, 5),
            DiceRollResults(4.d8, 2.d6, 6.d6), // Deviate to [13, 5]
            Confirm, // Reduced to [14, 5]
            *defaultKickOffEvent(),
            2.d8 // Bounce to [14,4]
        )
        state.singleBall().assertCoordinates(14, 4)
        assertEquals(BallState.ON_GROUND, state.singleBall().state)
        awayTeam.assertActive()
        state.assertNoActivePlayer()
        assertEquals(1, awayTeam.turnMarker)
    }
}
