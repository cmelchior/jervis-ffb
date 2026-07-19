package com.jervisffb.test.bb2025.skills

import com.jervisffb.engine.actions.Cancel
import com.jervisffb.engine.actions.Confirm
import com.jervisffb.engine.actions.DiceRollResults
import com.jervisffb.engine.actions.PitchSquareSelected
import com.jervisffb.engine.actions.PlayerSelected
import com.jervisffb.engine.ext.d6
import com.jervisffb.engine.ext.d8
import com.jervisffb.engine.ext.playerId
import com.jervisffb.engine.ext.playerNo
import com.jervisffb.engine.model.PlayerId
import com.jervisffb.engine.model.locations.PitchCoordinate
import com.jervisffb.engine.rules.bb2025.skills.Kick
import com.jervisffb.engine.rules.common.actions.PlayerStandardActionType
import com.jervisffb.engine.rules.common.skills.SkillType
import com.jervisffb.test.JervisGameBB2025Test
import com.jervisffb.test.SmartMoveTo
import com.jervisffb.test.activatePlayer
import com.jervisffb.test.defaultKickOffEvent
import com.jervisffb.test.defaultKickOffHomeTeam
import com.jervisffb.test.defaultPregame
import com.jervisffb.test.defaultSetup
import com.jervisffb.test.ext.rollForward
import com.jervisffb.test.utils.assertActive
import com.jervisffb.test.utils.assertNoActivePlayer
import com.jervisffb.test.utils.makeDistracted
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * Class testing usage of the [Kick] skill.
 */
class KickTests: JervisGameBB2025Test() {

    @BeforeTest
    override fun setUp() {
        super.setUp()
    }

    @Test
    fun kickIsOptional() {
        homeTeam["H10".playerId].addSkill(SkillType.KICK)
        controller.rollForward(
            *defaultPregame(),
            *defaultSetup(),
            PlayerSelected(PlayerId("H10")), // Select Kicker
            PitchSquareSelected(19, 7), // Center of Away Half,
            DiceRollResults(4.d8, 1.d6), // Land on [18,7]
            Cancel, // Do not use Kick
            *defaultKickOffEvent(),
            4.d8 // Bounce to [17,7]
        )
    }

    @Test
    fun kickReducesDistance() {
        homeTeam["H10".playerId].addSkill(SkillType.KICK)
        controller.rollForward(
            *defaultPregame(),
            *defaultSetup(),
            PlayerSelected(PlayerId("H10")), // Select Kicker
            PitchSquareSelected(19, 7), // Center of Away Half,
            DiceRollResults(2.d8, 5.d6), // Land on [19,2]
            Confirm, // Use Kick, reduce to [19, 4]
            *defaultKickOffEvent(),
            5.d8 // Bounce to [20,4]
        )
        assertEquals(PitchCoordinate(20, 4), state.getBall().coordinates)
    }

    @Test
    fun doesNotWorkIfDistractedByEndOfDrive() {
        controller.rollForward(
            *defaultPregame(),
            *defaultSetup(),
            *defaultKickOffHomeTeam(
                placeKick = PitchSquareSelected(25, 0),
                deviate = DiceRollResults(2.d8, 1.d6), // Out-of-bounds
                kickoffEvent = defaultKickOffEvent(),
                bounce = null,
            ),
            PlayerSelected("A6".playerId), // Give ball to this player and start turn
        )
        val scoringPlayer = awayTeam[6.playerNo]
        scoringPlayer.movesLeft = 20 // Give player enough move to reach the End Zone in one turn.
        val awayKicker = awayTeam["A10".playerId].apply {
            addSkill(SkillType.KICK)
            makeDistracted()
        }
        controller.rollForward(
            *activatePlayer(scoringPlayer, PlayerStandardActionType.MOVE),
            SmartMoveTo(0, 3), // Score
            Confirm,
            *defaultSetup(homeFirst = false),
            PlayerSelected(PlayerId("A10")), // Select Kicker
            PitchSquareSelected(6, 7), // Center of Home Half,
            DiceRollResults(4.d8, 1.d6), // Land on [5,7]. Kick cannot be used.
            *defaultKickOffEvent(),
            4.d8 // Bounce to [4,7]
        )
        state.assertNoActivePlayer()
        homeTeam.assertActive()
    }
}
