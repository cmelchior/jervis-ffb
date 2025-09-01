package com.jervisffb.test.skills

import com.jervisffb.engine.actions.Confirm
import com.jervisffb.engine.actions.DiceRollResults
import com.jervisffb.engine.actions.EndSetup
import com.jervisffb.engine.actions.FieldSquareSelected
import com.jervisffb.engine.actions.NoRerollSelected
import com.jervisffb.engine.actions.PlayerActionSelected
import com.jervisffb.engine.actions.PlayerSelected
import com.jervisffb.engine.ext.d6
import com.jervisffb.engine.ext.d8
import com.jervisffb.engine.ext.playerId
import com.jervisffb.engine.model.BallState
import com.jervisffb.engine.model.hasSkill
import com.jervisffb.engine.model.locations.FieldCoordinate
import com.jervisffb.engine.rules.bb2020.skills.CatchSkill
import com.jervisffb.engine.rules.common.actions.PlayerStandardActionType
import com.jervisffb.engine.rules.common.skills.SkillType
import com.jervisffb.test.JervisGameTest
import com.jervisffb.test.defaultAwaySetup
import com.jervisffb.test.defaultHomeSetup
import com.jervisffb.test.defaultKickOffHomeTeam
import com.jervisffb.test.defaultPregame
import com.jervisffb.test.defaultSetup
import com.jervisffb.test.ext.rollForward
import com.jervisffb.test.setupPlayer
import com.jervisffb.test.utils.SelectSkillReroll
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 * Class testing usage of the [com.jervisffb.engine.rules.bb2020.procedures.Catch] skill
 *
 * See page 75 in the rulebook for the skill.
 * See page 51 in the rulebook for the catching description.
 */
class CatchSkillTests: JervisGameTest() {

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
            Confirm, // Start Pass section
            FieldSquareSelected(15, 1), // Select player with Catch next to thrower
            6.d6, // Throw ball
            NoRerollSelected(), // No Reroll
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

    @Test
    fun catchDeviatedBall() {
        controller.rollForward(
            *defaultPregame(),
            *defaultHomeSetup(),
            *defaultAwaySetup(endSetup = false),
            *setupPlayer("A10".playerId, FieldCoordinate(15, 7)),
            EndSetup,
            *defaultKickOffHomeTeam(
                placeKick = FieldSquareSelected(16, 7),
                deviate = DiceRollResults(4.d8, 1.d6), // Deviate so lands on player
                bounce = null
            ),
            6.d6, // Catch Landing
            PlayerSelected("A10".playerId),
            PlayerActionSelected(PlayerStandardActionType.PASS),
            Confirm, // Start Pass section
            FieldSquareSelected(15, 1), // Throw Short Pass to player with Catch
            2.d6, // Wildly Inaccurate Pass
            NoRerollSelected(), // No Reroll
        )
        assertEquals(BallState.DEVIATING, state.currentBall().state)
        controller.rollForward(
            DiceRollResults(2.d8, 6.d6), // Deviate ball, so it eventually ends up on the target anyway
            1.d6, // Fail first catch roll
            SelectSkillReroll(SkillType.CATCH), // Reroll using Catch
            5.d6 // Catch succeed
        )
        assertTrue(awayTeam["A7".playerId].hasBall())
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
            Confirm, // Start Pass section
            FieldSquareSelected(15, 1), // Throw Quick Pass to player with Catch
            3.d6, // Inaccurate Pass
            NoRerollSelected(), // No Reroll
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
    fun catchInterception() {
        // TODO
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

