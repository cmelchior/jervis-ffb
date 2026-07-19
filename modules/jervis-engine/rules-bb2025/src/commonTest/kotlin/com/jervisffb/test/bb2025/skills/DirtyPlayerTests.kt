package com.jervisffb.test.bb2025.skills

import com.jervisffb.engine.actions.Cancel
import com.jervisffb.engine.actions.Confirm
import com.jervisffb.engine.actions.DiceRollResults
import com.jervisffb.engine.actions.PlayerSelected
import com.jervisffb.engine.ext.d6
import com.jervisffb.engine.ext.playerId
import com.jervisffb.engine.model.PlayerDogoutState
import com.jervisffb.engine.model.PlayerPitchState
import com.jervisffb.engine.model.context.getContext
import com.jervisffb.engine.model.modifiers.ArmourModifier
import com.jervisffb.engine.rules.bb2025.skills.DirtyPlayer
import com.jervisffb.engine.rules.common.actions.PlayerStandardActionType
import com.jervisffb.engine.rules.common.procedures.tables.injury.RiskingInjuryContext
import com.jervisffb.engine.rules.common.skills.SkillType
import com.jervisffb.test.JervisGameBB2025Test
import com.jervisffb.test.activatePlayer
import com.jervisffb.test.ext.rollForward
import com.jervisffb.test.utils.assertStunned
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull

/**
 * Class testing usage of the [DirtyPlayer] skill.
 * Technically, you could reroll a shadowing roll, but since both Team Rerolls
 * and Pro don't work during the opponent turn, these cannot be used, so no
 * known reroll type exists.
 */
class DirtyPlayerTests: JervisGameBB2025Test() {

    @BeforeTest
    override fun setUp() {
        super.setUp()
        state.apply {
            awayTeam["A1".playerId].addSkill(SkillType.DIRTY_PLAYER.id())
        }
        startDefaultGame()
    }

    @Test
    fun doNotUseSkillIfNoImpact() {
        homeTeam["H1".playerId].state = PlayerPitchState.PRONE
        controller.rollForward(
            *activatePlayer("A1", PlayerStandardActionType.FOUL),
            PlayerSelected("H1".playerId),
            DiceRollResults(3.d6, 6.d6),
        )
        assertFalse(state.getContext<RiskingInjuryContext>().armourModifiers.any { it == ArmourModifier.DIRTY_PLAYER })
        controller.rollForward(
            DiceRollResults(1.d6, 2.d6),
            Confirm, // Dirty Player on Injury
        )
        homeTeam["H1".playerId].assertStunned()
        assertNull(state.activePlayer)
        assertEquals(awayTeam, state.activeTeam)
    }

    @Test
    fun useOnArmourRoll() {
        homeTeam["H1".playerId].state = PlayerPitchState.PRONE
        controller.rollForward(
            *activatePlayer("A1", PlayerStandardActionType.FOUL),
            PlayerSelected("H1".playerId),
            DiceRollResults(2.d6, 6.d6),
            Confirm, // With +1 from Dirty Player, should break armour
            DiceRollResults(1.d6, 2.d6),
        )
        homeTeam["H1".playerId].assertStunned()
        assertNull(state.activePlayer)
        assertEquals(awayTeam, state.activeTeam)
    }

    @Test
    fun useOnInjuryRoll() {
        homeTeam["H1".playerId].state = PlayerPitchState.PRONE
        controller.rollForward(
            *activatePlayer("A1", PlayerStandardActionType.FOUL),
            PlayerSelected("H1".playerId),
            DiceRollResults(3.d6, 6.d6), // Break armour without Dirty Player
            DiceRollResults(3.d6, 4.d6),
            Confirm, // With +1 from Dirty Player, should move from Stunned to KO
            Cancel // Do not use apothecary
        )
        assertEquals(PlayerDogoutState.KNOCKED_OUT, homeTeam["H1".playerId].state)
        assertNull(state.activePlayer)
        assertEquals(awayTeam, state.activeTeam)
    }

    @Test
    fun resetAfterAction() {
        homeTeam["H1".playerId].state = PlayerPitchState.PRONE
        controller.rollForward(
            *activatePlayer("A1", PlayerStandardActionType.FOUL),
            PlayerSelected("H1".playerId),
            DiceRollResults(2.d6, 6.d6),
            Confirm, // With +1 from Dirty Player, should break armour
            DiceRollResults(1.d6, 2.d6),
        )
        assertNull(state.activePlayer)
        assertFalse(awayTeam["A1".playerId].getSkill(SkillType.DIRTY_PLAYER).used)
    }
}
