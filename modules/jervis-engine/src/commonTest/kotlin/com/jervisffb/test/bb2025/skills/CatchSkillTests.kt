package com.jervisffb.test.bb2025.skills

import com.jervisffb.engine.actions.DiceRollResults
import com.jervisffb.engine.actions.FieldSquareSelected
import com.jervisffb.engine.actions.PassTypeSelected
import com.jervisffb.engine.actions.PlayerActionSelected
import com.jervisffb.engine.actions.PlayerSelected
import com.jervisffb.engine.ext.d6
import com.jervisffb.engine.ext.d8
import com.jervisffb.engine.ext.playerId
import com.jervisffb.engine.model.BallState
import com.jervisffb.engine.rules.bb2025.skills.CatchSkill
import com.jervisffb.engine.rules.common.actions.PassType
import com.jervisffb.engine.rules.common.actions.PlayerStandardActionType
import com.jervisffb.engine.rules.common.skills.SkillType
import com.jervisffb.test.JervisGameBB2025Test
import com.jervisffb.test.defaultKickOffHomeTeam
import com.jervisffb.test.defaultPregame
import com.jervisffb.test.defaultSetup
import com.jervisffb.test.ext.rollForward
import com.jervisffb.test.throwBall
import com.jervisffb.test.utils.SelectSkillReroll
import com.jervisffb.test.utils.hasSkill
import kotlin.test.Ignore
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 * Class testing usage of the [com.jervisffb.engine.rules.common.procedures.Catch] skill
 *
 * See page 126 in the rulebook for the skill.
 * See page 72 in the rulebook for the catching description.
 */
class CatchSkillTests: JervisGameBB2025Test() {

    @Test
    fun catchLandingAfterKickOff() {
        controller.rollForward(
            *defaultPregame(),
            *defaultSetup(),
            *defaultKickOffHomeTeam(
                placeKick = FieldSquareSelected(16, 1),
                deviate = DiceRollResults(4.d8, 1.d6), // Deviate so lands on player with Catch
                bounce = null
            ),
            1.d6, // Fail first catch roll
            SelectSkillReroll(SkillType.CATCH), // Reroll using Catch
            4.d6 // Catch succeed
        )
        assertTrue(awayTeam["A7".playerId].hasBall())
    }

    @Test
    fun catchAccuratePass() {
        controller.rollForward(
            *defaultPregame(),
            *defaultSetup(),
            *defaultKickOffHomeTeam(
                placeKick = FieldSquareSelected(13, 1),
                deviate = DiceRollResults(5.d8, 1.d6), // Deviate so lands on player
                bounce = null
            ),
            6.d6, // Catch Landing
            PlayerSelected("A6".playerId),
            PlayerActionSelected(PlayerStandardActionType.PASS),
            PassTypeSelected(PassType.STANDARD),
            FieldSquareSelected(15, 1), // Select player with Catch next to thrower
            *throwBall(6.d6),
        )
        assertEquals(BallState.ACCURATE_THROW, state.currentBall().state)
        controller.rollForward(
            1.d6, // Fail first catch roll
            SelectSkillReroll(SkillType.CATCH), // Reroll using Catch
            3.d6 // Catch succeed
        )
        assertTrue(awayTeam["A7".playerId].hasBall())
    }

    @Test
    fun catchHandOff() {
        controller.rollForward(
            *defaultPregame(),
            *defaultSetup(),
            *defaultKickOffHomeTeam(
                placeKick = FieldSquareSelected(13, 1),
                deviate = DiceRollResults(5.d8, 1.d6), // Deviate so lands on player
                bounce = null
            ),
            6.d6, // Catch Landing
            PlayerSelected("A6".playerId),
            PlayerActionSelected(PlayerStandardActionType.HAND_OFF),
            PlayerSelected("A7".playerId), // Select target of Hand-off
            1.d6, // Fail first catch roll
            SelectSkillReroll(SkillType.CATCH), // Reroll using Catch
            3.d6 // Catch succeed
        )
        assertTrue(awayTeam["A7".playerId].hasBall())
    }

    @Test
    fun catchBouncingBall() {
        awayTeam["A1".playerId].addSkill(SkillType.CATCH)
        controller.rollForward(
            *defaultPregame(),
            *defaultSetup(),
            *defaultKickOffHomeTeam(
                placeKick = FieldSquareSelected(13, 3),
                deviate = DiceRollResults(7.d8, 1.d6), // Deviate so lands on an empty square
                bounce = 7.d8, // Bounce to player with catch on [13,5]
            ),
            3.d6, // Fail first catch roll
            SelectSkillReroll(SkillType.CATCH), // Reroll using Catch
            6.d6 // Catch succeed
        )
        assertTrue(awayTeam["A1".playerId].hasBall())
    }

    @Ignore
    @Test
    fun catchDeviatedBall() {
        // No known way to test deviated balls in BB2025
    }

    @Test
    fun catchScatteredBall() {
        controller.rollForward(
            *defaultPregame(),
            *defaultSetup(),
            *defaultKickOffHomeTeam(
                placeKick = FieldSquareSelected(13, 1),
                deviate = DiceRollResults(5.d8, 1.d6), // Deviate so lands on player
                bounce = null
            ),
            6.d6, // Catch Landing
            PlayerSelected("A6".playerId),
            PlayerActionSelected(PlayerStandardActionType.PASS),
            PassTypeSelected(PassType.STANDARD),
            FieldSquareSelected(15, 1), // Throw Quick Pass to player with Catch
            *throwBall(3.d6), // Inaccurate Pass
        )
        assertEquals(BallState.SCATTERED, state.currentBall().state)
        controller.rollForward(
            DiceRollResults(2.d8, 4.d8, 8.d8), // Scatter ball, so it eventually ends up on the target anyway
            1.d6, // Fail first catch roll
            SelectSkillReroll(SkillType.CATCH), // Reroll using Catch
            5.d6 // Catch succeed
        )
        assertTrue(awayTeam["A7".playerId].hasBall())
    }

    @Test
    fun doesNotWorkWithMissingTackleZones() {
        awayTeam["A7".playerId].hasTackleZones = false
        controller.rollForward(
            *defaultPregame(),
            *defaultSetup(),
            *defaultKickOffHomeTeam(
                placeKick = FieldSquareSelected(16, 1),
                deviate = DiceRollResults(4.d8, 1.d6), // Deviate so lands on player with Catch
                bounce = null
            ),
        )
        assertTrue(awayTeam["A7".playerId].hasSkill<CatchSkill>())
        controller.rollForward(
            // Bounce, because catch doesn't work without tackle zones
            // Technically, you cannot even do the basic "catch" without tackle
            // zones, so the skill will never trigger either.
            1.d8,
        )
    }

    @Test
    fun onlyWorksOnFailedCatches() {
        controller.rollForward(
            *defaultPregame(),
            *defaultSetup(),
            *defaultKickOffHomeTeam(
                placeKick = FieldSquareSelected(16, 1),
                deviate = DiceRollResults(4.d8, 1.d6), // Deviate so lands on player with Catch
                bounce = null
            ),
        )
        assertTrue(awayTeam["A7".playerId].hasSkill<CatchSkill>())
        assertEquals(0, awayTeam.turnMarker)
        controller.rollForward(
            6.d6, // Succeed first catch
        )
        // Catch skill only works on failed catches, so we go directly to the first turn
        assertEquals(1, awayTeam.turnMarker)
    }
}

