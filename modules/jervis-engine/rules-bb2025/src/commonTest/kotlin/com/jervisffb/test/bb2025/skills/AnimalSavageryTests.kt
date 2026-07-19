package com.jervisffb.test.bb2025.skills

import com.jervisffb.engine.actions.DiceRollResults
import com.jervisffb.engine.actions.EndAction
import com.jervisffb.engine.actions.PlayerSelected
import com.jervisffb.engine.actions.SelectPlayer
import com.jervisffb.engine.ext.d6
import com.jervisffb.engine.ext.d8
import com.jervisffb.engine.ext.playerId
import com.jervisffb.engine.model.modifiers.PlayerStatusEffectType
import com.jervisffb.engine.rules.bb2025.skills.AnimalSavagery
import com.jervisffb.engine.rules.common.actions.PlayerStandardActionType
import com.jervisffb.engine.rules.common.skills.SkillType
import com.jervisffb.test.JervisGameBB2025Test
import com.jervisffb.test.activatePlayer
import com.jervisffb.test.animalSavagery
import com.jervisffb.test.bounce
import com.jervisffb.test.dodge
import com.jervisffb.test.ext.rollForward
import com.jervisffb.test.giveBallToPlayer
import com.jervisffb.test.moveTo
import com.jervisffb.test.utils.assertActive
import com.jervisffb.test.utils.assertCoordinates
import com.jervisffb.test.utils.assertDistracted
import com.jervisffb.test.utils.assertNoActivePlayer
import com.jervisffb.test.utils.assertProne
import com.jervisffb.test.utils.assertStanding
import com.jervisffb.test.utils.assertStunned
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse

/**
 * Class testing usage of the [AnimalSavagery] skill.
 */
class AnimalSavageryTests: JervisGameBB2025Test() {

    @BeforeTest
    override fun setUp() {
        super.setUp()
        startDefaultGame()
    }

    @Test
    fun distractedIfNoAdjacentPlayerOnFailure() {
        val player = awayTeam["A10".playerId].apply {
            addSkill(SkillType.ANIMAL_SAVAGERY)
        }

        controller.rollForward(
            *activatePlayer(player, PlayerStandardActionType.MOVE),
            *animalSavagery(3.d6),
        )
        player.assertDistracted()
        state.assertNoActivePlayer()
    }


    @Test
    fun improveRollOnBlock() {
        val player = awayTeam["A1".playerId].apply {
            addSkill(SkillType.ANIMAL_SAVAGERY)
        }
        controller.rollForward(
            *activatePlayer(player, PlayerStandardActionType.BLOCK),
            *animalSavagery(2.d6),
            EndAction
        )
        state.assertNoActivePlayer()
        player.assertStanding()
        assertFalse(player.statusEffects.any { it.type == PlayerStatusEffectType.DISTRACTED })
    }

    @Test
    fun improveRollOnBlitz() {
        val player = awayTeam["A1".playerId].apply {
            addSkill(SkillType.ANIMAL_SAVAGERY)
        }
        controller.rollForward(
            *activatePlayer(player, PlayerStandardActionType.BLITZ),
            *animalSavagery(2.d6),
            EndAction
        )
        state.assertNoActivePlayer()
        player.assertStanding()
        assertEquals(0, awayTeam.turnData.blitzActions)
    }


    @Test
    fun mustUseClaws() {
        val player = awayTeam["A1".playerId].apply {
            addSkill(SkillType.ANIMAL_SAVAGERY)
            addSkill(SkillType.CLAWS)
            addSkill(SkillType.MIGHTY_BLOW)
        }
        val target = awayTeam["A2".playerId]
        controller.rollForward(
            *activatePlayer(player, PlayerStandardActionType.MOVE),
            *animalSavagery(2.d6),
            PlayerSelected(target),
            DiceRollResults(4.d6, 4.d6),
            DiceRollResults(1.d6, 1.d6),
            *moveTo(14, 5),
            *dodge(6.d6),
            EndAction
        )
        state.assertNoActivePlayer()
        player.assertStanding()
        target.assertStunned(ownTeamTurn = true)
    }

    @Test
    fun mustUseMightyBlow() {
        val player = awayTeam["A1".playerId].apply {
            addSkill(SkillType.ANIMAL_SAVAGERY)
            addSkill(SkillType.CLAWS)
            addSkill(SkillType.MIGHTY_BLOW)
        }
        val target = awayTeam["A2".playerId].apply {
            baseArmorValue = 8
            armorValue = 8
        }
        controller.rollForward(
            *activatePlayer(player, PlayerStandardActionType.MOVE),
            *animalSavagery(2.d6),
            PlayerSelected(target),
            DiceRollResults(3.d6, 4.d6),
            DiceRollResults(1.d6, 1.d6),
            *moveTo(14, 5),
            *dodge(6.d6),
            EndAction
        )
        state.assertNoActivePlayer()
        player.assertStanding()
        target.assertStunned(ownTeamTurn = true)
    }

    @Test
    fun canContinueActionAfterBlockingPlayer() {
        val player = awayTeam["A1".playerId].apply {
            addSkill(SkillType.ANIMAL_SAVAGERY)
        }
        val target = awayTeam["A2".playerId]
        controller.rollForward(
            *activatePlayer(player, PlayerStandardActionType.MOVE),
            *animalSavagery(2.d6),
            PlayerSelected(target),
            DiceRollResults(1.d6, 1.d6),
            *moveTo(14, 5),
            *dodge(6.d6),
            EndAction
        )
        state.assertNoActivePlayer()
        awayTeam.assertActive()
        player.assertStanding()
    }

    @Test
    fun cannotChooseOpponentTeamMembersForBlock() {
        val player = awayTeam["A1".playerId].apply {
            addSkill(SkillType.ANIMAL_SAVAGERY)
        }
        val target = awayTeam["A2".playerId]
        controller.rollForward(
            *activatePlayer(player, PlayerStandardActionType.MOVE),
            *animalSavagery(2.d6),
        )
        val players = controller.getAvailableActions().get<SelectPlayer>().players
        assertEquals(1, players.size)
        assertEquals("A2".playerId, players.single())
    }

    @Test
    fun ironHardSkinDoesNotPreventClawsAndMightyBlow() {
        val player = awayTeam["A1".playerId].apply {
            addSkill(SkillType.ANIMAL_SAVAGERY)
            addSkill(SkillType.CLAWS)
            addSkill(SkillType.MIGHTY_BLOW)
        }
        val target = awayTeam["A2".playerId].apply {
            addSkill(SkillType.IRON_HARD_SKIN)
        }
        controller.rollForward(
            *activatePlayer(player, PlayerStandardActionType.MOVE),
            *animalSavagery(2.d6),
            PlayerSelected(target),
            DiceRollResults(4.d6, 4.d6),
            DiceRollResults(1.d6, 1.d6),
            *moveTo(14, 5),
            *dodge(6.d6),
            EndAction
        )
        state.assertNoActivePlayer()
        player.assertStanding()
        target.assertStunned(ownTeamTurn = true)
    }

    @Test
    fun turnoverIfKnockedDownPlayerHadTheBall() {
        val player = awayTeam["A1".playerId].apply {
            addSkill(SkillType.ANIMAL_SAVAGERY)
        }
        val target = awayTeam["A2".playerId].apply {
            giveBallToPlayer(this)
        }
        controller.rollForward(
            *activatePlayer(player, PlayerStandardActionType.MOVE),
            *animalSavagery(2.d6),
            PlayerSelected(target),
            DiceRollResults(1.d6, 1.d6),
            bounce(5.d8),
        )
        state.assertNoActivePlayer()
        homeTeam.assertActive()
        target.assertProne()
        state.singleBall().assertCoordinates(14, 6)
    }
}
