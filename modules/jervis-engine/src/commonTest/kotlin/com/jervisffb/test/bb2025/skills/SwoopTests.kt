package com.jervisffb.test.bb2025.skills

import com.jervisffb.engine.actions.Confirm
import com.jervisffb.engine.actions.DiceRollResults
import com.jervisffb.engine.actions.DirectionSelected
import com.jervisffb.engine.actions.PitchSquareSelected
import com.jervisffb.engine.actions.PlayerSelected
import com.jervisffb.engine.ext.d3
import com.jervisffb.engine.ext.d6
import com.jervisffb.engine.ext.d8
import com.jervisffb.engine.ext.playerId
import com.jervisffb.engine.model.Direction
import com.jervisffb.engine.model.context.getContext
import com.jervisffb.engine.rules.bb2025.skills.Swoop
import com.jervisffb.engine.rules.common.actions.PlayerStandardActionType
import com.jervisffb.engine.rules.common.procedures.actions.throwteammate.ThrowPlayerResult
import com.jervisffb.engine.rules.common.procedures.actions.throwteammate.ThrowTeamMateContext
import com.jervisffb.engine.rules.common.rerolls.RegularTeamReroll
import com.jervisffb.engine.rules.common.skills.SkillType
import com.jervisffb.test.JervisGameBB2025Test
import com.jervisffb.test.activatePlayer
import com.jervisffb.test.dodge
import com.jervisffb.test.ext.rollForward
import com.jervisffb.test.landingRoll
import com.jervisffb.test.moveTo
import com.jervisffb.test.qualityRoll
import com.jervisffb.test.swoopDirectionRoll
import com.jervisffb.test.swoopDistanceRoll
import com.jervisffb.test.utils.TeamRerollSelected
import com.jervisffb.test.utils.assertCoordinates
import com.jervisffb.test.utils.assertProne
import com.jervisffb.test.utils.assertReserves
import com.jervisffb.test.utils.assertStanding
import com.jervisffb.test.utils.makeDistracted
import com.jervisffb.test.utils.putProne
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

/**
 * Class testing usage of the [Swoop] skill.
 */
class SwoopTests: JervisGameBB2025Test() {

    @BeforeTest
    override fun setUp() {
        setupAndStartThrowTeamMateGame()
    }

    @Test
    fun useSwoopOnSuperbThrow() {
        val thrower = awayTeam["A1".playerId]
        val thrownPlayer = awayTeam["A13".playerId]
        thrownPlayer.addSkill(SkillType.SWOOP)
        controller.rollForward(
            *activatePlayer(thrower, PlayerStandardActionType.THROW_TEAM_MATE),
            *moveTo(14, 4),
            *dodge(),
            PlayerSelected(thrownPlayer),
            PitchSquareSelected(9, 4),
            *qualityRoll(6.d6), // Superb throw
        )
        assertEquals(ThrowPlayerResult.SUPERB, state.getContext<ThrowTeamMateContext>().qualityRollResult)
        controller.rollForward(
            Confirm, // Use Swoop
            DirectionSelected(Direction.LEFT),
            *swoopDirectionRoll(3.d3),
            *swoopDistanceRoll(2.d6),
            *landingRoll(6.d6)
        )
        assertNull(state.activePlayer)
        thrownPlayer.assertStanding()
        thrownPlayer.assertCoordinates(7, 2)
    }

    @Test
    fun useSwoopOnSubparThrow() {
        val thrower = awayTeam["A1".playerId]
        val thrownPlayer = awayTeam["A13".playerId]
        thrownPlayer.addSkill(SkillType.SWOOP)
        controller.rollForward(
            *activatePlayer(thrower, PlayerStandardActionType.THROW_TEAM_MATE),
            *moveTo(14, 4),
            *dodge(),
            PlayerSelected(thrownPlayer),
            PitchSquareSelected(9, 4),
            *qualityRoll(4.d6), // Subpar throw
        )
        assertEquals(ThrowPlayerResult.SUBPAR, state.getContext<ThrowTeamMateContext>().qualityRollResult)
        controller.rollForward(
            Confirm, // Use Swoop
            DirectionSelected(Direction.DOWN),
            *swoopDirectionRoll(2.d3),
            *swoopDistanceRoll(4.d6),
            *landingRoll(6.d6)
        )
        assertNull(state.activePlayer)
        thrownPlayer.assertStanding()
        thrownPlayer.assertCoordinates(9, 8)
    }

    @Test
    fun canRerollDirection() {
        val thrower = awayTeam["A1".playerId]
        val thrownPlayer = awayTeam["A13".playerId]
        thrownPlayer.addSkill(SkillType.SWOOP)
        controller.rollForward(
            *activatePlayer(thrower, PlayerStandardActionType.THROW_TEAM_MATE),
            *moveTo(14, 4),
            *dodge(),
            PlayerSelected(thrownPlayer),
            PitchSquareSelected(9, 4),
            *qualityRoll(6.d6), // Superb throw
            Confirm, // Use Swoop
            DirectionSelected(Direction.LEFT),
            3.d3,
            TeamRerollSelected<RegularTeamReroll>(),
            1.d3,
            *swoopDistanceRoll(2.d6),
            *landingRoll(6.d6)
        )
        assertNull(state.activePlayer)
        thrownPlayer.assertStanding()
        thrownPlayer.assertCoordinates(7, 6)
    }

    @Test
    fun canRerollDistance() {
        val thrower = awayTeam["A1".playerId]
        val thrownPlayer = awayTeam["A13".playerId]
        thrownPlayer.addSkill(SkillType.SWOOP)
        controller.rollForward(
            *activatePlayer(thrower, PlayerStandardActionType.THROW_TEAM_MATE),
            *moveTo(14, 4),
            *dodge(),
            PlayerSelected(thrownPlayer),
            PitchSquareSelected(9, 4),
            *qualityRoll(6.d6), // Superb throw
            Confirm, // Use Swoop
            DirectionSelected(Direction.LEFT),
            *swoopDirectionRoll(2.d3),
            1.d6,
            TeamRerollSelected<RegularTeamReroll>(),
            3.d6,
            *landingRoll(6.d6)
        )
        assertNull(state.activePlayer)
        thrownPlayer.assertStanding()
        thrownPlayer.assertCoordinates(6, 4)
    }

    // The distance roll took the player out of bounds
    @Test
    fun rolledDirectionIsOutOfBounds() {
        val thrower = awayTeam["A1".playerId]
        val thrownPlayer = awayTeam["A13".playerId]
        thrownPlayer.addSkill(SkillType.SWOOP)
        controller.rollForward(
            *activatePlayer(thrower, PlayerStandardActionType.THROW_TEAM_MATE),
            *moveTo(14, 4),
            *dodge(),
            PlayerSelected(thrownPlayer),
            PitchSquareSelected(16, 2),
            *qualityRoll(6.d6), // Superb throw
            Confirm, // Use Swoop
            DirectionSelected(Direction.UP),
            *swoopDirectionRoll(3.d3),
            *swoopDistanceRoll(6.d6), // Player goes out-of-bounds
            DiceRollResults(1.d6, 1.d6) // Stunned -> Reserves
        )
        assertNull(state.activePlayer)
        assertEquals(homeTeam, state.activeTeam)
        thrownPlayer.assertReserves()
    }

    // Player near the sideline place the template towards the end-zone,
    // but 1-2 lies on the outside of the pitch, i.e., any distance roll will
    // take the player out-of-bounds.
    @Test
    fun driftOutOfBounds() {
        val thrower = awayTeam["A1".playerId]
        val thrownPlayer = awayTeam["A13".playerId]
        thrownPlayer.addSkill(SkillType.SWOOP)
        controller.rollForward(
            *activatePlayer(thrower, PlayerStandardActionType.THROW_TEAM_MATE),
            *moveTo(14, 4),
            *dodge(),
            PlayerSelected(thrownPlayer),
            PitchSquareSelected(16, 0),
            *qualityRoll(6.d6), // Superb throw
            Confirm, // Use Swoop
            DirectionSelected(Direction.RIGHT),
            *swoopDirectionRoll(1.d3), // Direction is out-of-bounds regardless of distance rolled
            DiceRollResults(1.d6, 1.d6) // Stunned -> Reserves
        )
        assertNull(state.activePlayer)
        assertEquals(homeTeam, state.activeTeam)
        thrownPlayer.assertReserves()
    }

    @Test
    fun doesNotWorkIfDistracted() {
        val thrower = awayTeam["A1".playerId]
        val thrownPlayer = awayTeam["A13".playerId]
        thrownPlayer.apply {
            addSkill(SkillType.SWOOP)
            makeDistracted()
        }
        controller.rollForward(
            *activatePlayer(thrower, PlayerStandardActionType.THROW_TEAM_MATE),
            *moveTo(14, 4),
            *dodge(),
            PlayerSelected(thrownPlayer),
            PitchSquareSelected(9, 4),
            *qualityRoll(6.d6),
            DiceRollResults(2.d8, 4.d8, 8.d8),
            DiceRollResults(1.d6, 1.d6) // Auto fail landing, player Falls Over
        )
        assertNull(state.activePlayer)
        assertEquals(awayTeam, state.activeTeam)
        thrownPlayer.assertProne()
    }

    @Test
    fun doesNotWorkIfProne() {
        val thrower = awayTeam["A1".playerId]
        val thrownPlayer = awayTeam["A13".playerId]
        thrownPlayer.apply {
            addSkill(SkillType.SWOOP)
            putProne()
        }
        controller.rollForward(
            *activatePlayer(thrower, PlayerStandardActionType.THROW_TEAM_MATE),
            *moveTo(14, 4),
            *dodge(),
            PlayerSelected(thrownPlayer),
            PitchSquareSelected(9, 4),
            *qualityRoll(6.d6),
            DiceRollResults(2.d8, 4.d8, 8.d8),
            DiceRollResults(1.d6, 1.d6) // Auto fail landing, player Falls Over
        )
        assertNull(state.activePlayer)
        assertEquals(awayTeam, state.activeTeam)
        thrownPlayer.assertProne()
    }

    @Test
    fun doesNotWorkOnFumbledThrows() {
        val thrower = awayTeam["A1".playerId]
        val thrownPlayer = awayTeam["A13".playerId]
        thrownPlayer.addSkill(SkillType.SWOOP)
        controller.rollForward(
            *activatePlayer(thrower, PlayerStandardActionType.THROW_TEAM_MATE),
            *moveTo(14, 4),
            *dodge(),
            PlayerSelected(thrownPlayer),
            PitchSquareSelected(9, 4),
            *qualityRoll(1.d6),
            7.d8, // Bounce Player
            *landingRoll(6.d6),
        )
        assertNull(state.activePlayer)
        assertEquals(awayTeam, state.activeTeam)
        thrownPlayer.assertStanding()
        thrownPlayer.assertCoordinates(14, 5)
    }

    @Test
    fun doesNotWorkIfBullseyeIsUsed() {
        val thrower = awayTeam["A1".playerId]
        thrower.addSkill(SkillType.BULLSEYE)
        val thrownPlayer = awayTeam["A13".playerId]
        thrownPlayer.addSkill(SkillType.SWOOP)
        controller.rollForward(
            *activatePlayer(thrower, PlayerStandardActionType.THROW_TEAM_MATE),
            *moveTo(14, 4),
            *dodge(),
            PlayerSelected(thrownPlayer),
            PitchSquareSelected(11, 4), // Quick Pass - No modifiers
            *qualityRoll(5.d6),
            Confirm, // Use Bullseye
            *landingRoll(6.d6)
        )
        thrownPlayer.assertStanding()
        thrownPlayer.assertCoordinates(11, 4)
    }
}
