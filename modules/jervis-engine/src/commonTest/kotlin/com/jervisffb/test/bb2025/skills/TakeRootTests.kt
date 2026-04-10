package com.jervisffb.test.bb2025.skills

import com.jervisffb.engine.actions.DiceRollResults
import com.jervisffb.engine.actions.DirectionSelected
import com.jervisffb.engine.actions.EndAction
import com.jervisffb.engine.actions.EndTurn
import com.jervisffb.engine.actions.PlayerSelected
import com.jervisffb.engine.actions.SelectMoveType
import com.jervisffb.engine.actions.SelectPlayer
import com.jervisffb.engine.commands.SetBallState
import com.jervisffb.engine.ext.d6
import com.jervisffb.engine.ext.dblock
import com.jervisffb.engine.ext.playerId
import com.jervisffb.engine.model.Availability
import com.jervisffb.engine.model.Direction
import com.jervisffb.engine.model.PlayerNo
import com.jervisffb.engine.model.locations.FieldCoordinate
import com.jervisffb.engine.model.modifiers.PlayerStatusEffect
import com.jervisffb.engine.model.modifiers.PlayerStatusEffectType
import com.jervisffb.engine.rules.bb2025.skills.TakeRoot
import com.jervisffb.engine.rules.common.actions.PlayerSpecialActionType
import com.jervisffb.engine.rules.common.actions.PlayerStandardActionType
import com.jervisffb.engine.rules.common.rerolls.RegularTeamReroll
import com.jervisffb.engine.rules.common.skills.SkillType
import com.jervisffb.test.JervisGameBB2025Test
import com.jervisffb.test.activatePlayer
import com.jervisffb.test.boneHead
import com.jervisffb.test.breatheFireRoll
import com.jervisffb.test.dodge
import com.jervisffb.test.ext.rollForward
import com.jervisffb.test.moveTo
import com.jervisffb.test.skipTurns
import com.jervisffb.test.standardBlock
import com.jervisffb.test.takeRoot
import com.jervisffb.test.utils.TeamRerollSelected
import com.jervisffb.test.utils.assertCoordinates
import com.jervisffb.test.utils.assertProne
import com.jervisffb.test.utils.assertStanding
import com.jervisffb.test.utils.putProne
import kotlin.test.BeforeTest
import kotlin.test.Ignore
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

/**
 * Class testing usage of the [TakeRoot] skill
 */
class TakeRootTests: JervisGameBB2025Test() {

    @BeforeTest
    override fun setUp() {
        super.setUp()
        startDefaultGame()
    }

    @Test
    fun rollOnActivation() {
        val player = awayTeam["A1".playerId]
        player.addSkill(SkillType.TAKE_ROOT)
        controller.rollForward(
            *activatePlayer(player, PlayerStandardActionType.MOVE),
            *takeRoot(1.d6),
        )
        assertEquals(player,state.activePlayer)
        assertFalse(rules.isDistracted(player))
        assertTrue(player.hasStatusEffect(PlayerStatusEffectType.ROOTED))
    }

    @Test
    fun doesNotClearOnNextActivation() {
        val player = awayTeam["A1".playerId]
        player.addSkill(SkillType.TAKE_ROOT)
        controller.rollForward(
            *activatePlayer(player, PlayerStandardActionType.MOVE),
            *takeRoot(1.d6),
            EndAction,
            EndTurn,
            EndTurn,
            *activatePlayer(player, PlayerStandardActionType.MOVE),
        )
        assertEquals(player,state.activePlayer)
        assertTrue(player.hasStatusEffect(PlayerStatusEffectType.ROOTED))
        assertTrue(controller.getAvailableActions().actions.none { it is SelectMoveType })
    }

    @Test
    fun rollOtherNegaTraitsOnRooted() {
        val player = awayTeam["A1".playerId]
        player.apply {
            addSkill(SkillType.TAKE_ROOT)
            addSkill(SkillType.BONE_HEAD)
        }
        controller.rollForward(
            *activatePlayer(player, PlayerStandardActionType.MOVE),
            *takeRoot(1.d6),
            *boneHead(1.d6),
        )
        assertNull(state.activePlayer)
        assertTrue(player.hasStatusEffect(PlayerStatusEffectType.ROOTED))
        assertTrue(player.hasStatusEffect(PlayerStatusEffectType.BONE_HEAD))
        assertTrue(rules.isDistracted(player))
    }

    @Test
    fun TwoPlusToAvoidBeingRooted() {
        val player = awayTeam["A1".playerId]
        player.addSkill(SkillType.TAKE_ROOT)
        controller.rollForward(
            *activatePlayer(player, PlayerStandardActionType.MOVE),
            1.d6,
            TeamRerollSelected<RegularTeamReroll>(),
            2.d6
        )
        assertEquals(player,state.activePlayer)
        assertFalse(player.hasStatusEffect(PlayerStatusEffectType.ROOTED))
        assertTrue(controller.getAvailableActions().actions.any { it is SelectMoveType })
    }

    @Test
    fun cannotMoveWhenRooted() {
        val player = awayTeam["A1".playerId]
        player.addSkill(SkillType.TAKE_ROOT)
        controller.rollForward(
            *activatePlayer(player, PlayerStandardActionType.MOVE),
            *takeRoot(1.d6),
        )
        assertEquals(player,state.activePlayer)
        assertTrue(player.hasStatusEffect(PlayerStatusEffectType.ROOTED))
        assertTrue(controller.getAvailableActions().actions.none { it is SelectMoveType })
    }

    @Test
    fun cannotMoveDuringBlitz() {
        val player = awayTeam["A1".playerId]
        player.addSkill(SkillType.TAKE_ROOT)
        controller.rollForward(
            *activatePlayer(player, PlayerStandardActionType.BLITZ),
            *takeRoot(1.d6),
            PlayerSelected(homeTeam["H3".playerId]),
        )
        assertTrue(controller.getAvailableActions().actions.none { it is SelectMoveType })
        controller.rollForward(
            EndAction,
        )
        assertEquals(awayTeam, state.activeTeam)
        assertEquals(Availability.HAS_ACTIVATED, player.available)
        assertEquals(0, awayTeam.turnData.blitzActions)
        assertTrue(player.hasStatusEffect(PlayerStatusEffectType.ROOTED))
    }

    @Test
    fun cannotMoveDuringPass() {
        val player = awayTeam["A1".playerId]
        player.addSkill(SkillType.TAKE_ROOT)
        controller.rollForward(
            *activatePlayer(player, PlayerStandardActionType.PASS),
            *takeRoot(1.d6),
        )
        assertTrue(controller.getAvailableActions().actions.none { it is SelectMoveType })
        controller.rollForward(
            EndAction,
        )
        assertEquals(awayTeam, state.activeTeam)
        assertEquals(Availability.HAS_ACTIVATED, player.available)
        assertEquals(0, awayTeam.turnData.passActions)
        assertTrue(player.hasStatusEffect(PlayerStatusEffectType.ROOTED))
    }

    @Test
    fun cannotMoveDuringHandOff() {
        val player = awayTeam["A1".playerId]
        player.addSkill(SkillType.TAKE_ROOT)
        controller.rollForward(
            *activatePlayer(player, PlayerStandardActionType.HAND_OFF),
            *takeRoot(1.d6),
        )
        assertTrue(controller.getAvailableActions().actions.none { it is SelectMoveType })
        controller.rollForward(
            EndAction,
        )
        assertEquals(awayTeam, state.activeTeam)
        assertEquals(Availability.HAS_ACTIVATED, player.available)
        assertEquals(0, awayTeam.turnData.handOffActions)
        assertTrue(player.hasStatusEffect(PlayerStatusEffectType.ROOTED))
    }

    @Test
    fun cannotMoveDuringFoul() {
        homeTeam["H3".playerId].putProne()
        val player = awayTeam["A1".playerId]
        player.addSkill(SkillType.TAKE_ROOT)
        controller.rollForward(
            *activatePlayer(player, PlayerStandardActionType.FOUL),
            *takeRoot(1.d6),
        )
        assertTrue(controller.getAvailableActions().actions.none { it is SelectMoveType })
        controller.rollForward(
            EndAction,
        )
        assertEquals(awayTeam, state.activeTeam)
        assertEquals(Availability.HAS_ACTIVATED, player.available)
        assertEquals(0, awayTeam.turnData.foulActions)
        assertTrue(player.hasStatusEffect(PlayerStatusEffectType.ROOTED))
    }

    @Test
    fun cannotMoveDuringSecureTheBall() {
        val player = state.getPlayerById("A8".playerId)
        player.addSkill(SkillType.TAKE_ROOT)
        controller.rollForward(
            *activatePlayer("A8", PlayerStandardActionType.SECURE_THE_BALL),
            *takeRoot(1.d6),
            EndAction
        )

        assertNull(state.activePlayer)
        assertEquals(homeTeam, state.activeTeam)
        assertTrue(player.hasStatusEffect(PlayerStatusEffectType.ROOTED))
    }

    @Test
    fun cannotMoveDuringThrowTeammate() {
        setupAndStartThrowTeamMateGame()
        val thrower = awayTeam["A1".playerId]
        thrower.addSkill(SkillType.TAKE_ROOT)
        controller.rollForward(
            *activatePlayer("A1", PlayerStandardActionType.THROW_TEAM_MATE),
            *takeRoot(1.d6),
        )
        assertTrue(controller.getAvailableActions().actions.none { it is SelectMoveType })
        controller.rollForward(
            EndAction,
        )
        assertEquals(awayTeam, state.activeTeam)
        assertEquals(Availability.HAS_ACTIVATED, thrower.available)
        assertEquals(0, awayTeam.turnData.throwTeamMateActions)
        assertTrue(thrower.hasStatusEffect(PlayerStatusEffectType.ROOTED))
    }

    @Test
    fun cannotBeThrownWhenRooted() {
        setupAndStartThrowTeamMateGame()
        controller.rollForward(
            *activatePlayer("A1", PlayerStandardActionType.THROW_TEAM_MATE),
        )
        awayTeam["A13".playerId].apply {
            addStatusEffect(PlayerStatusEffect.rooted())
        }
        assertFalse(controller.getAvailableActions().contains<SelectPlayer>())
        awayTeam["A13".playerId].apply {
            removeStatusEffect(statusEffects.first())
        }
        val availablePlayersForThrowing = controller.getAvailableActions().get<SelectPlayer>().players
        assertEquals(1, availablePlayersForThrowing.size)
    }


    @Test
    fun cannotJumpWhenRooted() {
        state.getPlayerById("H1".playerId).putProne()
        val jumpingPlayer = state.getPlayerById("A1".playerId)
        jumpingPlayer.addSkill(SkillType.TAKE_ROOT)
        controller.rollForward(
            *activatePlayer(jumpingPlayer, PlayerStandardActionType.MOVE),
            *takeRoot(1.d6),
        )
        assertTrue(controller.getAvailableActions().actions.none { it is SelectMoveType })
        controller.rollForward(
            EndAction,
        )
        assertNull(state.activePlayer)
    }

    @Test
    fun cannotLeapWhenRooted() {
        state.getPlayerById("H1".playerId).putProne()
        val jumpingPlayer = state.getPlayerById("A1".playerId)
        jumpingPlayer.apply {
            addSkill(SkillType.TAKE_ROOT)
            addSkill(SkillType.LEAP)
        }
        controller.rollForward(
            *activatePlayer(jumpingPlayer, PlayerStandardActionType.MOVE),
            *takeRoot(1.d6),
        )
        assertTrue(controller.getAvailableActions().actions.none { it is SelectMoveType })
        controller.rollForward(
            EndAction,
        )
        assertNull(state.activePlayer)
    }

    @Test
    fun cannotFollowUpWhenRooted() {
        val attacker = state.getPlayerById("A1".playerId)
        attacker.addSkill(SkillType.TAKE_ROOT)
        val defender = state.getPlayerById("H1".playerId)
        controller.rollForward(
            *activatePlayer("A1", PlayerStandardActionType.BLOCK),
            *takeRoot(1.d6),
            *standardBlock("H1", 3.dblock),
            DirectionSelected(Direction.LEFT),
        )
        assertNull(state.activePlayer)
        assertEquals(FieldCoordinate(13, 5), attacker.coordinates)
    }


    @Test
    fun cannotBePushedBackWhenRooted() {
        val attacker = state.getPlayerById("A1".playerId)
        val defender = state.getPlayerById("H1".playerId)
        defender.apply {
            addSkill(SkillType.TAKE_ROOT)
            addStatusEffect(PlayerStatusEffect.rooted())
        }
        controller.rollForward(
            *activatePlayer("A1", PlayerStandardActionType.BLOCK),
            *standardBlock("H1", 3.dblock),
        )
        assertNull(state.activePlayer)
        attacker.assertCoordinates(13, 5)
        attacker.assertStanding()
        defender.assertCoordinates(12, 5)
        defender.assertStanding()
    }

    @Test
    fun rootedPreventsStripBall() {
        val attacker = awayTeam["A1".playerId]
        attacker.addSkill(SkillType.STRIP_BALL)
        val defender = homeTeam["H1".playerId]
        defender.apply {
            addSkill(SkillType.TAKE_ROOT)
            addStatusEffect(PlayerStatusEffect.rooted())
        }
        val ball = state.singleBall()
        SetBallState.carried(ball, defender).execute(state)
        controller.rollForward(
            *activatePlayer(attacker, PlayerStandardActionType.BLOCK),
            *standardBlock("H1", 3.dblock),
        )
        assertNull(state.activePlayer)
        assertTrue(defender.hasBall())
    }

    @Test
    fun clearStatusAtEndOfDrive() {
        val player = awayTeam["A1".playerId]
        player.addSkill(SkillType.TAKE_ROOT)
        controller.rollForward(
            *activatePlayer(player, PlayerStandardActionType.MOVE),
            *takeRoot(1.d6),
            EndAction
        )
        assertTrue(player.hasStatusEffect(PlayerStatusEffectType.ROOTED))
        controller.rollForward(
            *skipTurns(16)
        )
        assertEquals(2, state.halfNo)
        assertFalse(player.hasStatusEffect(PlayerStatusEffectType.ROOTED))
    }

    @Test
    fun clearStatusOnKnockedDown() {
        val attacker = state.getPlayerById("A1".playerId)
        val defender = state.getPlayerById("H1".playerId)
        defender.apply {
            addSkill(SkillType.TAKE_ROOT)
            addStatusEffect(PlayerStatusEffect.rooted())
        }
        controller.rollForward(
            *activatePlayer(attacker, PlayerStandardActionType.BLOCK),
            *standardBlock(defender, 6.dblock),
            DiceRollResults(1.d6, 1.d6), // Cannot Pushback before being Knocked Down
        )
        assertNull(state.activePlayer)
        assertFalse(defender.hasStatusEffect(PlayerStatusEffectType.ROOTED))
        defender.assertCoordinates(12, 5)
        defender.assertProne()
    }

    @Test
    fun clearStatusOnPlacedProne() {
        val attacker = state.getPlayerById("A1".playerId)
        attacker.addSkill(SkillType.BREATHE_FIRE)
        val defender = state.getPlayerById("H1".playerId)
        defender.apply {
            addSkill(SkillType.TAKE_ROOT)
            addStatusEffect(PlayerStatusEffect.rooted())
        }
        controller.rollForward(
            *activatePlayer(attacker, PlayerSpecialActionType.BREATHE_FIRE),
            PlayerSelected(defender),
            *breatheFireRoll(4.d6),
        )
        assertNull(state.activePlayer)
        defender.assertProne()
        assertFalse(defender.hasStatusEffect(PlayerStatusEffectType.ROOTED))
    }

    @Ignore
    @Test
    fun clearStatusWhenUsingDivingTackle() {
        TODO("Waiting for Diving Tackle support")
    }

    @Ignore
    @Test
    fun rootedPreventsTrickster() {
        TODO("Waiting Trickster support")

    }

    @Test
    fun rootedPreventsShadowing() {
        val shadowingPlayer = homeTeam[PlayerNo(2)]
        val movingPlayer = awayTeam[PlayerNo(1)]
        shadowingPlayer.apply {
            addSkill(SkillType.SHADOWING)
            addSkill(SkillType.TAKE_ROOT)
            addStatusEffect(PlayerStatusEffect.rooted())
        }
        assertTrue(shadowingPlayer.coordinates.isAdjacent(rules, movingPlayer.coordinates))
        controller.rollForward(
            *activatePlayer(movingPlayer, PlayerStandardActionType.MOVE),
            *moveTo(14, 5), // Move away from both shadowing players
            *dodge(6.d6),
            EndAction,
        )
        assertNull(state.activePlayer)
    }
}
