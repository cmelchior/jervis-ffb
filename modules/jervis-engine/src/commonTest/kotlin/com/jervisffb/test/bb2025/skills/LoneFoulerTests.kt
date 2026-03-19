package com.jervisffb.test.bb2025.skills

import com.jervisffb.engine.actions.Cancel
import com.jervisffb.engine.actions.Confirm
import com.jervisffb.engine.actions.DiceRollResults
import com.jervisffb.engine.actions.PlayerSelected
import com.jervisffb.engine.ext.d6
import com.jervisffb.engine.ext.playerId
import com.jervisffb.engine.model.PlayerState
import com.jervisffb.engine.model.context.getContext
import com.jervisffb.engine.model.modifiers.DefensiveAssistsArmourModifier
import com.jervisffb.engine.model.modifiers.OffensiveAssistArmourModifier
import com.jervisffb.engine.rules.bb2025.skills.LoneFouler
import com.jervisffb.engine.rules.common.actions.PlayerStandardActionType
import com.jervisffb.engine.rules.common.procedures.tables.injury.RiskingInjuryContext
import com.jervisffb.engine.rules.common.skills.SkillType
import com.jervisffb.test.JervisGameBB2025Test
import com.jervisffb.test.SmartMoveTo
import com.jervisffb.test.activatePlayer
import com.jervisffb.test.ext.rollForward
import com.jervisffb.test.utils.assertProne
import com.jervisffb.test.utils.assertStanding
import com.jervisffb.test.utils.assertStunned
import com.jervisffb.test.utils.putProne
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

/**
 * Class testing usage of the [LoneFouler] skill.
 */
class LoneFoulerTests: JervisGameBB2025Test() {

    @Test
    override fun setUp() {
        super.setUp()
        startDefaultGame()
    }

    @Test
    fun failedRollsTakeIntoAccountDirtyPlayer() {
        val fouler = awayTeam["A6".playerId]
        fouler.apply {
            addSkill(SkillType.DIRTY_PLAYER)
            addSkill(SkillType.LONE_FOULER)
        }
        val target = homeTeam["H1".playerId]
        target.putProne()
        assertEquals(9, target.armorValue)
        // Armour roll only succeeed because of Dirty Player
        controller.rollForward(
            *activatePlayer("A6", PlayerStandardActionType.FOUL),
            SmartMoveTo(13, 4),
            PlayerSelected("H1".playerId), // Start foul
            DiceRollResults(6.d6, 2.d6),
            Confirm, // Use Dirty Player
            DiceRollResults(1.d6, 2.d6),
        )
        assertNull(state.activePlayer)
        fouler.assertStanding()
        target.assertStunned()
    }

    @Test
    fun doesNotWorkIfOffensiveAssists() {
        val fouler = awayTeam["A6".playerId]
        fouler.addSkill(SkillType.LONE_FOULER)
        homeTeam["H2".playerId].putProne()
        val target = homeTeam["H1".playerId]
        target.putProne()
        controller.rollForward(
            *activatePlayer("A6", PlayerStandardActionType.FOUL),
            SmartMoveTo(13, 4),
            PlayerSelected(target), // Start foul
        )
        val context = state.getContext<RiskingInjuryContext>()
        assertTrue(context.armourModifiers.any { it is OffensiveAssistArmourModifier })
        assertTrue(context.armourModifiers.none { it is DefensiveAssistsArmourModifier })
        controller.rollForward(
            DiceRollResults(2.d6, 1.d6), // Fail to break armour
        )
        assertNull(state.activePlayer)
        fouler.assertStanding()
        target.assertProne()
    }

    @Test
    fun doesNotWorkIfDefensiveAssists() {
        val fouler = awayTeam["A6".playerId]
        fouler.addSkill(SkillType.LONE_FOULER)
        val target = homeTeam["H1".playerId]
        listOf("A1", "A2", "A3", "H1").forEach {
            state.getPlayerById(it.playerId).putProne()
        }
        controller.rollForward(
            *activatePlayer("A6", PlayerStandardActionType.FOUL),
            SmartMoveTo(11, 5),
            PlayerSelected(target), // Start foul
        )
        val context = state.getContext<RiskingInjuryContext>()
        assertTrue(context.armourModifiers.none { it is OffensiveAssistArmourModifier })
        assertTrue(context.armourModifiers.any { it is DefensiveAssistsArmourModifier })
        controller.rollForward(
            DiceRollResults(2.d6, 1.d6), // Fail to break armour
        )
        assertNull(state.activePlayer)
        fouler.assertStanding()
        target.assertProne()
    }

    @Test
    fun dirtyPlayerIsResetOnReroll() {
        val fouler = awayTeam["A6".playerId]
        fouler.apply {
            addSkill(SkillType.DIRTY_PLAYER)
            addSkill(SkillType.LONE_FOULER)
        }
        listOf("A1", "A2", "A3", "H1").forEach {
            state.getPlayerById(it.playerId).putProne()
        }
        val target = homeTeam["H1".playerId]
        assertEquals(9, target.armorValue)
        controller.rollForward(
            *activatePlayer("A6", PlayerStandardActionType.FOUL),
            SmartMoveTo(13, 4),
            PlayerSelected("H1".playerId), // Start foul
            DiceRollResults(2.d6, 2.d6),
            Confirm, // Use Lone Fouler
            DiceRollResults(4.d6, 5.d6),
            DiceRollResults(1.d6, 6.d6),
            Confirm, // Use Dirty Player on Injury
            Cancel, // Do not use Apothecary
        )
        assertNull(state.activePlayer)
        fouler.assertStanding()
        assertEquals(PlayerState.KNOCKED_OUT, target.state)
    }

    @Test
    fun doesNotWorkWhenPutTheBootInIsUsed() {
        val fouler = awayTeam["A6".playerId]
        fouler.addSkill(SkillType.LONE_FOULER)
        val target = homeTeam["H1".playerId]
        target.putProne()
        val assister = awayTeam["A1".playerId]
        assister.addSkill(SkillType.PUT_THE_BOOT_IN)
        assertEquals(9, target.armorValue)
        controller.rollForward(
            *activatePlayer("A6", PlayerStandardActionType.FOUL),
            SmartMoveTo(13, 4),
            PlayerSelected("H1".playerId), // Start foul
            DiceRollResults(2.d6, 3.d6), // Cannot reroll failed AV due to Put the Boot In
        )
        assertNull(state.activePlayer)
        fouler.assertStanding()
        target.assertProne()
    }
}
