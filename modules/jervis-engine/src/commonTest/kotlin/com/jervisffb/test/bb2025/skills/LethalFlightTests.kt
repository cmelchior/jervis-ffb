package com.jervisffb.test.bb2025.skills

import com.jervisffb.engine.actions.Cancel
import com.jervisffb.engine.actions.Confirm
import com.jervisffb.engine.actions.DiceRollResults
import com.jervisffb.engine.actions.FieldSquareSelected
import com.jervisffb.engine.actions.PlayerSelected
import com.jervisffb.engine.ext.d6
import com.jervisffb.engine.ext.d8
import com.jervisffb.engine.ext.playerId
import com.jervisffb.engine.rules.bb2025.skills.LethalFlight
import com.jervisffb.engine.rules.common.actions.PlayerStandardActionType
import com.jervisffb.engine.rules.common.skills.SkillType
import com.jervisffb.test.JervisGameBB2025Test
import com.jervisffb.test.activatePlayer
import com.jervisffb.test.ext.rollForward
import com.jervisffb.test.qualityRoll
import com.jervisffb.test.utils.assertKnockedOut
import com.jervisffb.test.utils.assertProne
import com.jervisffb.test.utils.assertStunned
import com.jervisffb.test.utils.makeDistracted
import kotlin.test.BeforeTest
import kotlin.test.Ignore
import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * Class testing usage of the [LethalFlight] skill.
 */
class LethalFlightTests: JervisGameBB2025Test() {

    @BeforeTest
    override fun setUp() {
        super.setUp()
        startDefaultGame()
        setupAndStartThrowTeamMateGame()
    }

    @Test
    fun useOnArmour() {
        val thrownPlayer = awayTeam["A13".playerId].also {
            it.addSkill(SkillType.LETHAL_FLIGHT)
        }
        assertEquals(10, homeTeam["H1".playerId].armorValue)
        controller.rollForward(
            *activatePlayer("A1", PlayerStandardActionType.THROW_TEAM_MATE),
            PlayerSelected(thrownPlayer),
            FieldSquareSelected(12, 5),
            *qualityRoll(6.d6),
            DiceRollResults(1.d8, 7.d8, 5.d8), // Hit target square
            DiceRollResults(3.d6, 6.d6), // Armour roll
            Confirm, // Use Lethal Flight
            DiceRollResults(1.d6, 1.d6), // Stunned
            2.d8, // Bounce to empty square
            DiceRollResults(1.d6, 1.d6), // Thrown Player Prone
        )
        thrownPlayer.assertProne()
        homeTeam["H1".playerId].assertStunned()
    }

    @Test
    fun doNotUseOnArmourIfNoEffect() {
        val thrownPlayer = awayTeam["A13".playerId].also {
            it.addSkill(SkillType.LETHAL_FLIGHT)
        }
        assertEquals(10, homeTeam["H1".playerId].armorValue)
        controller.rollForward(
            *activatePlayer("A1", PlayerStandardActionType.THROW_TEAM_MATE),
            PlayerSelected(thrownPlayer),
            FieldSquareSelected(12, 5),
            *qualityRoll(6.d6),
            DiceRollResults(1.d8, 7.d8, 5.d8), // Hit target square
            DiceRollResults(2.d6, 6.d6), // Armour roll
            2.d8, // Bounce to empty square
            DiceRollResults(1.d6, 1.d6), // Thrown Player Prone
        )
        thrownPlayer.assertProne()
        homeTeam["H1".playerId].assertProne()
    }

    @Test
    fun useOnInjury() {
        val thrownPlayer = awayTeam["A13".playerId].also {
            it.addSkill(SkillType.LETHAL_FLIGHT)
        }
        assertEquals(10, homeTeam["H1".playerId].armorValue)
        controller.rollForward(
            *activatePlayer("A1", PlayerStandardActionType.THROW_TEAM_MATE),
            PlayerSelected(thrownPlayer),
            FieldSquareSelected(12, 5),
            *qualityRoll(6.d6),
            DiceRollResults(1.d8, 7.d8, 5.d8), // Hit target square
            DiceRollResults(5.d6, 6.d6), // Armour roll
            DiceRollResults(1.d6, 6.d6), // Stunned, bumped to Knocked Out
            Confirm, // Use Lethal Flight
            Cancel, // Do not use Thick Skull
            Cancel, // Do not use Apothecary
            2.d8, // Bounce to empty square
            DiceRollResults(1.d6, 1.d6), // Thrown Player Prone
        )
        thrownPlayer.assertProne()
        homeTeam["H1".playerId].assertKnockedOut()
    }

    @Test
    fun doesNotWorkWhenDistracted() {
        val thrownPlayer = awayTeam["A13".playerId].also {
            it.addSkill(SkillType.LETHAL_FLIGHT)
            it.makeDistracted()
        }
        assertEquals(10, homeTeam["H1".playerId].armorValue)
        controller.rollForward(
            *activatePlayer("A1", PlayerStandardActionType.THROW_TEAM_MATE),
            PlayerSelected(thrownPlayer),
            FieldSquareSelected(12, 5),
            *qualityRoll(6.d6),
            DiceRollResults(1.d8, 7.d8, 5.d8), // Hit target square
            DiceRollResults(3.d6, 6.d6), // Armour roll
            2.d8, // Bounce to empty square
            DiceRollResults(1.d6, 1.d6), // Thrown Player Prone
        )
        thrownPlayer.assertProne()
        homeTeam["H1".playerId].assertProne()
    }

    @Test
    fun workOnMultiplePlayersHit() {
        val thrownPlayer = awayTeam["A13".playerId].also {
            it.addSkill(SkillType.LETHAL_FLIGHT)
        }
        assertEquals(10, homeTeam["H1".playerId].armorValue)
        controller.rollForward(
            *activatePlayer("A1", PlayerStandardActionType.THROW_TEAM_MATE),
            PlayerSelected(thrownPlayer),
            FieldSquareSelected(12, 5),
            *qualityRoll(6.d6),
            DiceRollResults(1.d8, 7.d8, 5.d8), // Hit target square
            DiceRollResults(3.d6, 6.d6), // AV roll
            Confirm, // Use Lethal Flight
            DiceRollResults(1.d6, 1.d6), // Stunned
            7.d8, // Bounce to next player
            DiceRollResults(6.d6, 6.d6), // AV roll
            DiceRollResults(1.d6, 6.d6), // Injury Roll
            Confirm, // Use Lethal Flight
            Cancel, // Do not use apothecary
            4.d8, // Bounce player
            DiceRollResults(1.d6, 1.d6), // Thrown Player Prone
        )
        thrownPlayer.assertProne()
        homeTeam["H1".playerId].assertStunned()
        homeTeam["H2".playerId].assertKnockedOut()
    }

    @Ignore
    @Test
    fun getsSPPFromCasualties() {
        // Waiting for SPP support
    }
}
