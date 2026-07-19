package com.jervisffb.test.bb2025.skills

import com.jervisffb.engine.actions.Cancel
import com.jervisffb.engine.actions.DiceRollResults
import com.jervisffb.engine.actions.PlayerSelected
import com.jervisffb.engine.ext.d6
import com.jervisffb.engine.ext.playerId
import com.jervisffb.engine.rules.bb2025.skills.ArmBar
import com.jervisffb.engine.rules.common.actions.PlayerStandardActionType
import com.jervisffb.engine.rules.common.skills.SkillType
import com.jervisffb.test.JervisGameBB2025Test
import com.jervisffb.test.activatePlayer
import com.jervisffb.test.dodge
import com.jervisffb.test.ext.rollForward
import com.jervisffb.test.jump
import com.jervisffb.test.jumpTo
import com.jervisffb.test.leap
import com.jervisffb.test.leapTo
import com.jervisffb.test.moveTo
import com.jervisffb.test.pogoRoll
import com.jervisffb.test.pogoTo
import com.jervisffb.test.rushRoll
import com.jervisffb.test.useApothecary
import com.jervisffb.test.utils.assertActive
import com.jervisffb.test.utils.assertCoordinates
import com.jervisffb.test.utils.assertKnockedOut
import com.jervisffb.test.utils.assertNoActivePlayer
import com.jervisffb.test.utils.assertProne
import com.jervisffb.test.utils.assertStunned
import com.jervisffb.test.utils.putProne
import kotlin.test.BeforeTest
import kotlin.test.Ignore
import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * Class testing usage of the [ArmBar] skill.
 */
class ArmBarTests: JervisGameBB2025Test() {

    @BeforeTest
    override fun setUp() {
        super.setUp()
        startDefaultGame()
    }

    @Test
    fun onlyOnePlayerCanUseIt() {
        val dodger = awayTeam["A1".playerId]
        val opponent1 = homeTeam["H1".playerId]
        opponent1.addSkill(SkillType.ARM_BAR)
        val opponent2 = homeTeam["H2".playerId]
        opponent2.addSkill(SkillType.ARM_BAR)
        assertEquals(9, dodger.armorValue)
        controller.rollForward(
            *activatePlayer(dodger, PlayerStandardActionType.MOVE),
            *moveTo(14, 5),
            *dodge(1.d6),
            DiceRollResults(3.d6, 5.d6),
            PlayerSelected(opponent2),
            DiceRollResults(1.d6, 6.d6),
        )
        state.assertNoActivePlayer()
        homeTeam.assertActive()
        dodger.assertStunned()
    }

    @Test
    fun onlyWorkOnInjuryIfNotUsedOnArmour() {
        val dodger = awayTeam["A1".playerId]
        val opponent = homeTeam["H1".playerId]
        opponent.addSkill(SkillType.ARM_BAR)
        assertEquals(9, dodger.armorValue)
        controller.rollForward(
            *activatePlayer(dodger, PlayerStandardActionType.MOVE),
            *moveTo(14, 5),
            *dodge(1.d6),
            DiceRollResults(3.d6, 6.d6),
            DiceRollResults(1.d6, 6.d6),
            PlayerSelected(opponent), // Use Arm Bar on injury
            useApothecary(false),
        )
        state.assertNoActivePlayer()
        homeTeam.assertActive()
        dodger.assertKnockedOut()
    }

    @Test
    fun doesNotWorkAfterDivingTackle() {
        val dodger = awayTeam["A1".playerId]
        val opponent = homeTeam["H1".playerId].apply {
            addSkill(SkillType.ARM_BAR)
            addSkill(SkillType.DIVING_TACKLE)
        }
        assertEquals(9, dodger.armorValue)
        controller.rollForward(
            *activatePlayer(dodger, PlayerStandardActionType.MOVE),
            *moveTo(14, 5),
            *dodge(3.d6),
            PlayerSelected(opponent), // Use diving tackle
            DiceRollResults(5.d6, 5.d6),
            DiceRollResults(1.d6, 6.d6),
        )
        state.assertNoActivePlayer()
        homeTeam.assertActive()
        dodger.assertStunned()
    }


    @Test
    fun doesNotWorkOnPogo() {
        val pogoingPlayer = state.getPlayerById("A1".playerId)
        pogoingPlayer.addSkill(SkillType.POGO_STICK)
        val opponent = homeTeam["H1".playerId]
        opponent.addSkill(SkillType.ARM_BAR)

        controller.rollForward(
            *activatePlayer("A1", PlayerStandardActionType.MOVE),
            *pogoTo(11, 4),
            *pogoRoll(2.d6),
            DiceRollResults(5.d6, 4.d6),
            DiceRollResults(1.d6, 2.d6)
        )
        pogoingPlayer.assertStunned()
        state.assertNoActivePlayer()
        homeTeam.assertActive()
    }

    @Test
    fun worksOnJumpInStartingSquare() {
        val jumper = awayTeam["A1".playerId]
        val opponent = homeTeam["H2".playerId]
        opponent.addSkill(SkillType.ARM_BAR)
        homeTeam["H1".playerId].putProne()
        assertEquals(9, jumper.armorValue)
        controller.rollForward(
            *activatePlayer(jumper, PlayerStandardActionType.MOVE),
            *jumpTo(11, 4),
            *jump(1.d6),
            DiceRollResults(3.d6, 5.d6),
            PlayerSelected(opponent),
            DiceRollResults(1.d6, 6.d6),
        )
        jumper.assertCoordinates(13, 5)
        state.assertNoActivePlayer()
        homeTeam.assertActive()
        jumper.assertStunned()
    }

    @Test
    fun worksOnJumpInLandingSquare() {
        val jumper = awayTeam["A1".playerId]
        val opponent = homeTeam["H2".playerId]
        opponent.addSkill(SkillType.ARM_BAR)
        homeTeam["H1".playerId].putProne()
        assertEquals(9, jumper.armorValue)
        controller.rollForward(
            *activatePlayer(jumper, PlayerStandardActionType.MOVE),
            *jumpTo(11, 4),
            *jump(2.d6),
            DiceRollResults(5.d6, 5.d6),
            DiceRollResults(1.d6, 2.d6),
            PlayerSelected(opponent),
        )
        jumper.assertCoordinates(11, 4)
        state.assertNoActivePlayer()
        homeTeam.assertActive()
        jumper.assertStunned()
    }

    @Test
    fun worksOnFailedRushDuringJump() {
        val jumper = awayTeam["A1".playerId]
        jumper.movesLeft = 0
        val opponent = homeTeam["H2".playerId]
        opponent.addSkill(SkillType.ARM_BAR)
        homeTeam["H1".playerId].putProne()
        assertEquals(9, jumper.armorValue)
        controller.rollForward(
            *activatePlayer(jumper, PlayerStandardActionType.MOVE),
            *jumpTo(11, 4),
            *rushRoll(1.d6),
            DiceRollResults(3.d6, 5.d6),
            PlayerSelected(opponent),
            DiceRollResults(1.d6, 2.d6),
        )
        jumper.assertCoordinates(13, 5)
        state.assertNoActivePlayer()
        homeTeam.assertActive()
        jumper.assertStunned()
    }

    @Test
    fun worksOnLeapInStartingSquare() {
        val leaper = awayTeam["A1".playerId]
        leaper.addSkill(SkillType.LEAP)
        val opponent = homeTeam["H2".playerId]
        opponent.addSkill(SkillType.ARM_BAR)
        homeTeam["H1".playerId].putProne()
        assertEquals(9, leaper.armorValue)
        controller.rollForward(
            *activatePlayer(leaper, PlayerStandardActionType.MOVE),
            *leapTo(15, 4),
            *leap(1.d6),
            DiceRollResults(3.d6, 5.d6),
            PlayerSelected(opponent),
            DiceRollResults(1.d6, 6.d6),
        )
        leaper.assertCoordinates(13, 5)
        state.assertNoActivePlayer()
        homeTeam.assertActive()
        leaper.assertStunned()
    }

    @Test
    fun worksOnLeapInLandingSquare() {
        val leaper = awayTeam["A1".playerId]
        leaper.addSkill(SkillType.LEAP)
        val opponent = homeTeam["H2".playerId]
        opponent.addSkill(SkillType.ARM_BAR)
        homeTeam["H1".playerId].putProne()
        assertEquals(9, leaper.armorValue)
        controller.rollForward(
            *activatePlayer(leaper, PlayerStandardActionType.MOVE),
            *leapTo(15, 5),
            *leap(2.d6),
            DiceRollResults(5.d6, 5.d6),
            DiceRollResults(1.d6, 2.d6),
            PlayerSelected(opponent),
        )
        leaper.assertCoordinates(15, 5)
        state.assertNoActivePlayer()
        homeTeam.assertActive()
        leaper.assertStunned()
    }

    @Test
    fun worksOnFailedRushDuringLeap() {
        val leaper = awayTeam["A1".playerId].apply {
            movesLeft = 0
            addSkill(SkillType.LEAP)
        }
        val opponent = homeTeam["H2".playerId]
        opponent.addSkill(SkillType.ARM_BAR)
        controller.rollForward(
            *activatePlayer(leaper, PlayerStandardActionType.MOVE),
            *leapTo(15, 4),
            *rushRoll(1.d6),
            DiceRollResults(5.d6, 5.d6),
            DiceRollResults(5.d6, 2.d6),
            PlayerSelected(opponent),
            useApothecary(false),
        )
        state.assertNoActivePlayer()
        homeTeam.assertActive()
        leaper.assertKnockedOut()
    }

    @Test
    fun workOnDodge() {
        val dodger = awayTeam["A1".playerId]
        val opponent = homeTeam["H1".playerId]
        opponent.addSkill(SkillType.ARM_BAR)
        assertEquals(9, dodger.armorValue)
        controller.rollForward(
            *activatePlayer(dodger, PlayerStandardActionType.MOVE),
            *moveTo(14, 5),
            *dodge(1.d6),
            DiceRollResults(3.d6, 5.d6),
            PlayerSelected(opponent),
            DiceRollResults(1.d6, 6.d6),
        )
        state.assertNoActivePlayer()
        homeTeam.assertActive()
        dodger.assertStunned()
    }

    // To simply the action flow, there is no need to use the skill on the Armour Roll
    // unless it is what causes it to break.
    @Test
    fun doNotUseOnArmourIfNotBreakingIt() {
        val dodger = awayTeam["A1".playerId]
        val opponent = homeTeam["H1".playerId]
        opponent.addSkill(SkillType.ARM_BAR)
        assertEquals(9, dodger.armorValue)
        controller.rollForward(
            *activatePlayer(dodger, PlayerStandardActionType.MOVE),
            *moveTo(14, 5),
            *dodge(1.d6),
            DiceRollResults(2.d6, 5.d6),
        )
        state.assertNoActivePlayer()
        homeTeam.assertActive()
        dodger.assertProne()
    }

    // To simply the action flow, there is no need to use the skill on the Armour Roll
    // if it is already broken.
    @Test
    fun doNotUseOnArmourIfAlreadyBroken() {
        val dodger = awayTeam["A1".playerId]
        val opponent = homeTeam["H1".playerId]
        opponent.addSkill(SkillType.ARM_BAR)
        assertEquals(9, dodger.armorValue)
        controller.rollForward(
            *activatePlayer(dodger, PlayerStandardActionType.MOVE),
            *moveTo(14, 5),
            *dodge(1.d6),
            DiceRollResults(6.d6, 5.d6),
            DiceRollResults(1.d6, 2.d6),
            Cancel
        )
        state.assertNoActivePlayer()
        homeTeam.assertActive()
        dodger.assertStunned()
    }

    @Ignore
    @Test
    fun getSPPWhenUsingSkill() {
        // Waiting for SPP support
    }
}
