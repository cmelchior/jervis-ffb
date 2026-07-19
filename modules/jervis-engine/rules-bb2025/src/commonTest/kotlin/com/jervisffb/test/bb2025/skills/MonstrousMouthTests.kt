package com.jervisffb.test.bb2025.skills

import com.jervisffb.engine.actions.BlockTypeSelected
import com.jervisffb.engine.actions.Cancel
import com.jervisffb.engine.actions.Confirm
import com.jervisffb.engine.actions.DiceRollResults
import com.jervisffb.engine.actions.DirectionSelected
import com.jervisffb.engine.actions.EndAction
import com.jervisffb.engine.actions.EndTurn
import com.jervisffb.engine.actions.PitchSquareSelected
import com.jervisffb.engine.actions.PlayerSelected
import com.jervisffb.engine.actions.SelectMoveType
import com.jervisffb.engine.actions.SelectPlayer
import com.jervisffb.engine.commands.SetPlayerLocation
import com.jervisffb.engine.ext.d16
import com.jervisffb.engine.ext.d6
import com.jervisffb.engine.ext.d8
import com.jervisffb.engine.ext.dblock
import com.jervisffb.engine.ext.playerId
import com.jervisffb.engine.ext.playerNo
import com.jervisffb.engine.model.Direction
import com.jervisffb.engine.model.locations.PitchCoordinate
import com.jervisffb.engine.model.modifiers.OwnedPlayerStatusEffect
import com.jervisffb.engine.model.modifiers.PlayerStatusEffectType
import com.jervisffb.engine.rules.bb2025.skills.MonstrousMouth
import com.jervisffb.engine.rules.common.actions.BlockType
import com.jervisffb.engine.rules.common.actions.PlayerSpecialActionType
import com.jervisffb.engine.rules.common.actions.PlayerStandardActionType
import com.jervisffb.engine.rules.common.skills.SkillType
import com.jervisffb.test.JervisGameBB2025Test
import com.jervisffb.test.activatePlayer
import com.jervisffb.test.boneHead
import com.jervisffb.test.chompRoll
import com.jervisffb.test.dodge
import com.jervisffb.test.ext.rollForward
import com.jervisffb.test.followUp
import com.jervisffb.test.giveBallToPlayer
import com.jervisffb.test.jump
import com.jervisffb.test.jumpTo
import com.jervisffb.test.landingRoll
import com.jervisffb.test.leap
import com.jervisffb.test.leapTo
import com.jervisffb.test.movePlayerTo
import com.jervisffb.test.moveTo
import com.jervisffb.test.pogoRoll
import com.jervisffb.test.pogoTo
import com.jervisffb.test.qualityRoll
import com.jervisffb.test.shadowPlayer
import com.jervisffb.test.skipTurns
import com.jervisffb.test.standardBlock
import com.jervisffb.test.utils.assertActive
import com.jervisffb.test.utils.assertBadlyHurt
import com.jervisffb.test.utils.assertBanned
import com.jervisffb.test.utils.assertCoordinates
import com.jervisffb.test.utils.assertKnockedOut
import com.jervisffb.test.utils.assertNoActivePlayer
import com.jervisffb.test.utils.assertProne
import com.jervisffb.test.utils.assertReserves
import com.jervisffb.test.utils.assertStanding
import com.jervisffb.test.utils.makeChomped
import com.jervisffb.test.utils.putProne
import kotlin.test.BeforeTest
import kotlin.test.Ignore
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

/**
 * Class testing usage of the [MonstrousMouth] skill.
 *
 * See page 131 in the BB2025 rulebook.
 */
class MonstrousMouthTests: JervisGameBB2025Test() {

    @BeforeTest
    override fun setUp() {
        super.setUp()
        startDefaultGame()
    }

    @Test
    fun chompRequires3Plus() {
        val attacker = awayTeam["A1".playerId]
        attacker.addSkill(SkillType.MONSTROUS_MOUTH)
        val defender = homeTeam["H1".playerId]
        controller.rollForward(
            *activatePlayer(attacker, PlayerSpecialActionType.CHOMP),
        )
        val players = controller.getAvailableActions().get<SelectPlayer>().players
        assertEquals(2, players.size)
        controller.rollForward(
            PlayerSelected(defender),
            *chompRoll(3.d6),
        )
        state.assertNoActivePlayer()
        assertEquals(attacker, (defender.statusEffects.single { it.type == PlayerStatusEffectType.CHOMPED } as OwnedPlayerStatusEffect).causedBy)
    }

    @Test
    fun failToChompOn2OrLess() {
        val attacker = awayTeam["A1".playerId]
        attacker.addSkill(SkillType.MONSTROUS_MOUTH)
        val defender = homeTeam["H1".playerId]
        controller.rollForward(
            *activatePlayer(attacker, PlayerSpecialActionType.CHOMP),
            PlayerSelected(defender),
            *chompRoll(2.d6),
        )
        state.assertNoActivePlayer()
        assertFalse(defender.statusEffects.any { it.type == PlayerStatusEffectType.CHOMPED })
    }

    @Test
    fun multipleChompsPerTurn() {
        val attacker1 = awayTeam["A1".playerId]
        attacker1.addSkill(SkillType.MONSTROUS_MOUTH)
        val attacker2 = awayTeam["A2".playerId]
        attacker2.addSkill(SkillType.MONSTROUS_MOUTH)
        val defender = homeTeam["H1".playerId]
        controller.rollForward(
            *activatePlayer(attacker1, PlayerSpecialActionType.CHOMP),
            PlayerSelected(defender),
            *chompRoll(3.d6),
            *activatePlayer(attacker2, PlayerSpecialActionType.CHOMP),
            PlayerSelected(defender),
            *chompRoll(2.d6),
        )
        state.assertNoActivePlayer()
        assertTrue(defender.statusEffects.any { it.type == PlayerStatusEffectType.CHOMPED })
    }

    @Test
    fun worksDuringBlitz() {
        val attacker = awayTeam["A1".playerId]
        attacker.addSkill(SkillType.MONSTROUS_MOUTH)
        val defender = homeTeam["H1".playerId]
        controller.rollForward(
            *activatePlayer(attacker, PlayerStandardActionType.BLITZ),
            PlayerSelected(defender),
            PlayerSelected(defender),
            BlockTypeSelected(BlockType.CHOMP),
            *chompRoll(3.d6),
            EndAction
        )
        state.assertNoActivePlayer()
        assertTrue(defender.statusEffects.any { it.type == PlayerStatusEffectType.CHOMPED })
    }

    @Test
    fun chompedPlayerCannotBePushed() {
        val attacker = awayTeam["A1".playerId]
        attacker.addSkill(SkillType.MONSTROUS_MOUTH)
        val defender = homeTeam["H1".playerId]
        defender.makeChomped(attacker)
        controller.rollForward(
            *activatePlayer(attacker, PlayerStandardActionType.BLOCK),
            *standardBlock(defender, 6.dblock),
            DiceRollResults(1.d6, 1.d6),
        )
        state.assertNoActivePlayer()
        defender.assertProne()
        defender.assertCoordinates(12, 5)
    }

    @Test
    fun chompedPlayerCannotBeChainpushed() {
        val attacker = awayTeam["A1".playerId]
        val defender = homeTeam["H1".playerId]
        val chainPushedDefender = homeTeam["H10".playerId]
        chainPushedDefender.makeChomped(attacker)

        SetPlayerLocation(homeTeam[4.playerNo], PitchCoordinate(11, 4)).execute(state)
        SetPlayerLocation(chainPushedDefender, PitchCoordinate(11, 5)).execute(state)
        SetPlayerLocation(homeTeam[11.playerNo], PitchCoordinate(11, 6)).execute(state)
        controller.rollForward(
            *activatePlayer("A1", PlayerStandardActionType.BLOCK),
            *standardBlock(defender, 4.dblock),
            DirectionSelected(Direction.LEFT)
        )
        state.assertNoActivePlayer()
        assertEquals(PitchCoordinate(13, 5), attacker.coordinates)
        assertEquals(PitchCoordinate(12, 5), defender.coordinates)
        assertEquals(PitchCoordinate(11, 5), chainPushedDefender.coordinates)
    }

    @Test
    fun chompedPlayerCannotFollowUpAwayFromChomper() {
        val attacker = awayTeam["A1".playerId]
        attacker.makeChomped(homeTeam["H2".playerId])
        val defender = homeTeam["H1".playerId]
        controller.rollForward(
            *activatePlayer(attacker, PlayerStandardActionType.BLOCK),
            *standardBlock(defender, 6.dblock),
            DirectionSelected(Direction.LEFT), // Push player, cannot follow up
            DiceRollResults(1.d6, 1.d6),
        )
        state.assertNoActivePlayer()
        attacker.assertCoordinates(13, 5)
        defender.assertProne()
        defender.assertCoordinates(11, 5)
    }

    @Test
    fun chompedPlayerCannotDodgeAway() {
        val player = awayTeam["A1".playerId]
        player.makeChomped(homeTeam["H2".playerId])
        controller.rollForward(
            *activatePlayer(player, PlayerStandardActionType.MOVE)
        )
        val actions = controller.getAvailableActions().actions
        assertFalse(actions.any { it is SelectMoveType })
        controller.rollForward(
            EndAction
        )
        state.assertNoActivePlayer()
    }

    @Test
    fun chompedPlayerCannotJumpAway() {
        val player = awayTeam["A1".playerId]
        player.makeChomped(homeTeam["H2".playerId])
        homeTeam["H1".playerId].putProne()
        controller.rollForward(
            *activatePlayer(player, PlayerStandardActionType.MOVE)
        )
        val actions = controller.getAvailableActions().actions
        assertFalse(actions.any { it is SelectMoveType })
        controller.rollForward(
            EndAction
        )
        state.assertNoActivePlayer()
    }

    @Test
    fun chompedPlayerCannotLeapAway() {
        val player = awayTeam["A1".playerId].apply {
            addSkill(SkillType.LEAP)
            makeChomped(homeTeam["H2".playerId])
        }
        controller.rollForward(
            *activatePlayer(player, PlayerStandardActionType.MOVE)
        )
        val actions = controller.getAvailableActions().actions
        assertFalse(actions.any { it is SelectMoveType })
        controller.rollForward(
            EndAction
        )
        state.assertNoActivePlayer()
    }

    @Test
    fun chompedPlayerCannotPogoAway() {
        val player = awayTeam["A1".playerId].apply {
            addSkill(SkillType.POGO_STICK)
            makeChomped(homeTeam["H2".playerId])
        }
        controller.rollForward(
            *activatePlayer(player, PlayerStandardActionType.MOVE)
        )
        val actions = controller.getAvailableActions().actions
        assertFalse(actions.any { it is SelectMoveType })
        controller.rollForward(
            EndAction
        )
        state.assertNoActivePlayer()
    }

    @Test
    fun chompedPlayerCannotUseShadowingToGetAway() {
        val player = awayTeam["A1".playerId]
        homeTeam["H1".playerId].apply {
            addSkill(SkillType.SHADOWING)
            makeChomped(awayTeam["A2".playerId])
        }
        controller.rollForward(
            *activatePlayer(player, PlayerStandardActionType.MOVE),
            *moveTo(14, 5),
            *dodge(6.d6),
            EndAction
        )
        state.assertNoActivePlayer()
        player.assertStanding()
    }

    @Test
    fun chompedPlayerCannotUseDivingTackleToGetAway() {
        val player = awayTeam["A1".playerId]
        homeTeam["H1".playerId].apply {
            addSkill(SkillType.DIVING_TACKLE)
            makeChomped(awayTeam["A2".playerId])
        }
        controller.rollForward(
            *activatePlayer(player, PlayerStandardActionType.MOVE),
            *moveTo(14, 5),
            *dodge(4.d6),
            EndAction
        )
        state.assertNoActivePlayer()
        player.assertStanding()
    }

    @Test
    fun chompedPlayerCannotBeTauntedAway() {
        val attacker = awayTeam["A1".playerId]
        attacker.makeChomped(homeTeam["H2".playerId])
        val defender = homeTeam["H1".playerId]
        defender.addSkill(SkillType.TAUNT)
        controller.rollForward(
            *activatePlayer(attacker, PlayerStandardActionType.BLOCK),
            *standardBlock(defender, 3.dblock),
            DirectionSelected(Direction.LEFT), // Push player, cannot follow up
        )
        state.assertNoActivePlayer()
        attacker.assertCoordinates(13, 5)
        defender.assertStanding()
        defender.assertCoordinates(11, 5)

    }

    @Test
    fun chompedPlayerCannotBePartOfThrowTeammate() {
        setupAndStartThrowTeamMateGame()
        val thrower = awayTeam["A1".playerId]
        awayTeam["A13".playerId].makeChomped(homeTeam["H1".playerId])
        controller.rollForward(
            *activatePlayer(thrower, PlayerStandardActionType.THROW_TEAM_MATE),
            *moveTo(14, 4),
            *dodge(),
        )
        assertTrue(thrower.coordinates.getSurroundingCoordinates(rules).any {
            state.pitch[it].player?.id == "A13".playerId
        })
        val actions = controller.getAvailableActions()
        assertFalse(actions.actions.any { it is SelectPlayer })
    }

    @Ignore
    @Test
    fun chompedPlayerCannotBePartOfKickTeammate() {
        // Wait for Kick Teammate support
    }

    @Test
    fun chompedIsRemovedWhenKnockedOut() {
        val attacker = awayTeam["A1".playerId]
        attacker.addSkill(SkillType.MONSTROUS_MOUTH)
        val defender = homeTeam["H1".playerId]
        defender.makeChomped(attacker)
        controller.rollForward(
            *activatePlayer(attacker, PlayerStandardActionType.BLOCK),
            *standardBlock(defender, 6.dblock),
            DiceRollResults(6.d6, 4.d6),
            DiceRollResults(6.d6, 2.d6),
            Cancel,
        )
        state.assertNoActivePlayer()
        defender.assertKnockedOut()
        assertFalse(defender.hasStatusEffect(PlayerStatusEffectType.CHOMPED))
    }

    @Test
    fun chompedRemovedWhenChomperKnockedOut() {
        val attacker = awayTeam["A1".playerId]
        attacker.addSkill(SkillType.MONSTROUS_MOUTH)
        val defender = homeTeam["H1".playerId]
        defender.makeChomped(attacker)
        controller.rollForward(
            *activatePlayer(attacker, PlayerStandardActionType.BLOCK),
            *standardBlock(defender, 1.dblock),
            DiceRollResults(6.d6, 4.d6),
            DiceRollResults(6.d6, 2.d6),
            Cancel,
        )
        state.assertNoActivePlayer()
        attacker.assertKnockedOut()
        defender.assertStanding()
        assertFalse(defender.hasStatusEffect(PlayerStatusEffectType.CHOMPED))
    }

    @Test
    fun chompedIsRemovedWhenChomperIsPushedIntoTheCrowd() {
        val attacker = awayTeam["A1".playerId]
        val defender = homeTeam["H1".playerId]
        defender.addSkill(SkillType.MONSTROUS_MOUTH)
        attacker.makeChomped(defender)
        movePlayerTo(attacker, PitchCoordinate(24, 1))
        movePlayerTo(defender, PitchCoordinate(24, 0))
        controller.rollForward(
            *activatePlayer(attacker, PlayerStandardActionType.BLOCK),
            *standardBlock(defender, 4.dblock),
            DirectionSelected(Direction.UP),
            followUp(false),
            DiceRollResults(1.d6, 1.d6),
        )
        state.assertNoActivePlayer()
        attacker.assertStanding()
        defender.assertReserves()
        assertFalse(attacker.hasStatusEffect(PlayerStatusEffectType.CHOMPED))
    }

    @Test
    fun chompedIsRemovedWhenMovedToCasualtyBox() {
        val attacker = awayTeam["A1".playerId]
        attacker.addSkill(SkillType.MONSTROUS_MOUTH)
        val defender = homeTeam["H1".playerId]
        defender.makeChomped(attacker)
        controller.rollForward(
            *activatePlayer(attacker, PlayerStandardActionType.BLOCK),
            *standardBlock(defender, 6.dblock),
            DiceRollResults(6.d6, 4.d6),
            DiceRollResults(6.d6, 6.d6),
            1.d16,
            Cancel,
        )
        state.assertNoActivePlayer()
        defender.assertBadlyHurt()
        assertFalse(defender.hasStatusEffect(PlayerStatusEffectType.CHOMPED))
    }

    @Test
    fun chompedIsRemovedWhenChomperMovedToCasualtyBox() {
        val attacker = awayTeam["A1".playerId]
        attacker.addSkill(SkillType.MONSTROUS_MOUTH)
        val defender = homeTeam["H1".playerId]
        defender.makeChomped(attacker)
        controller.rollForward(
            *activatePlayer(attacker, PlayerStandardActionType.BLOCK),
            *standardBlock(defender, 1.dblock),
            DiceRollResults(6.d6, 4.d6),
            DiceRollResults(6.d6, 6.d6),
            1.d16,
            Cancel,
        )
        state.assertNoActivePlayer()
        attacker.assertBadlyHurt()
        defender.assertStanding()
        assertFalse(defender.hasStatusEffect(PlayerStatusEffectType.CHOMPED))
    }

    @Test
    fun looseChompIfPushingBackChomper() {
        val defender = homeTeam["H1".playerId]
        defender.addSkill(SkillType.MONSTROUS_MOUTH)
        val attacker = awayTeam["A1".playerId]
        attacker.makeChomped(defender)
        controller.rollForward(
            *activatePlayer(attacker, PlayerStandardActionType.BLOCK),
            *standardBlock(defender, 3.dblock),
            DirectionSelected(Direction.LEFT),
            followUp(false),
        )
        state.assertNoActivePlayer()
        attacker.assertStanding()
        attacker.assertCoordinates(13, 5)
        assertFalse(attacker.hasStatusEffect(PlayerStatusEffectType.CHOMPED))
    }

    @Test
    fun cannotFollowUpOnChomperSidestepNextToChompedPlayer() {
        val defender = homeTeam["H1".playerId].apply {
            addSkill(SkillType.MONSTROUS_MOUTH)
            addSkill(SkillType.SIDESTEP)
        }
        val attacker = awayTeam["A1".playerId]
        attacker.makeChomped(defender)
        controller.rollForward(
            *activatePlayer(attacker, PlayerStandardActionType.BLOCK),
            *standardBlock(defender, 3.dblock),
            Confirm,
            DirectionSelected(Direction.UP_RIGHT),
        )
        state.assertNoActivePlayer()
        attacker.assertStanding()
        attacker.assertCoordinates(13, 5)
        defender.assertCoordinates(13, 4)
        assertTrue(attacker.hasStatusEffect(PlayerStatusEffectType.CHOMPED))
    }

    @Test
    fun looseChompedIfBanned() {
        val fouler = awayTeam["A1".playerId].apply {
            makeChomped(homeTeam["H1".playerId])
        }
        val target = homeTeam["H1".playerId].apply {
            addSkill(SkillType.MONSTROUS_MOUTH)
            putProne()
        }

        controller.rollForward(
            *activatePlayer(fouler, PlayerStandardActionType.FOUL),
            PlayerSelected(target), // Start foul
            DiceRollResults(2.d6, 2.d6), // Roll double -> Sent off
            Confirm,
            1.d6, // Roll You're Outta Here
        )
        homeTeam.assertActive()
        state.assertNoActivePlayer()
        fouler.assertBanned()
        target.assertProne()
        assertFalse(fouler.hasStatusEffect(PlayerStatusEffectType.CHOMPED))
    }

    @Test
    fun looseChompedIfChomperIsBanned() {
        val fouler = awayTeam["A1".playerId].apply {
            addSkill(SkillType.MONSTROUS_MOUTH)
        }
        val target = homeTeam["H1".playerId].apply {
            makeChomped(fouler)
            putProne()
        }

        controller.rollForward(
            *activatePlayer(fouler, PlayerStandardActionType.FOUL),
            PlayerSelected(target), // Start foul
            DiceRollResults(2.d6, 2.d6), // Roll double -> Sent off
            Confirm,
            1.d6, // Roll You're Outta Here
        )
        homeTeam.assertActive()
        state.assertNoActivePlayer()
        fouler.assertBanned()
        target.assertProne()
        assertFalse(target.hasStatusEffect(PlayerStatusEffectType.CHOMPED))
    }

    @Test
    fun chompedRemovedIfChomperIsKnockedDown()  {
        val defender = homeTeam["H1".playerId].apply {
            addSkill(SkillType.MONSTROUS_MOUTH)
        }
        val attacker = awayTeam["A1".playerId].apply {
            addSkill(SkillType.BLOCK)
            makeChomped(defender)
        }
        controller.rollForward(
            *activatePlayer(attacker, PlayerStandardActionType.BLOCK),
            *standardBlock(defender, 2.dblock),
            Confirm, // Use Block
            DiceRollResults(6.d6, 1.d6),
        )
        state.assertNoActivePlayer()
        attacker.assertStanding()
        defender.assertProne()
        assertFalse(attacker.hasStatusEffect(PlayerStatusEffectType.CHOMPED))
    }

    @Test
    fun chompedPlayerKnockedDownDoesNotRemoveChomped() {
        val attacker = awayTeam["A1".playerId].apply {
            addSkill(SkillType.MONSTROUS_MOUTH)
            addSkill(SkillType.BLOCK)
        }
        val defender = homeTeam["H1".playerId].apply {
            makeChomped(attacker)
        }
        controller.rollForward(
            *activatePlayer(attacker, PlayerStandardActionType.BLOCK),
            *standardBlock(defender, 2.dblock),
            Confirm, // Use Block
            DiceRollResults(6.d6, 1.d6),
        )
        state.assertNoActivePlayer()
        attacker.assertStanding()
        defender.assertProne()
        assertTrue(defender.hasStatusEffect(PlayerStatusEffectType.CHOMPED))
    }

    @Test
    fun canBeChompedMultipleTimes() {
        val attacker1 = awayTeam["A1".playerId].apply {
            addSkill(SkillType.MONSTROUS_MOUTH)
        }
        val attacker2 = awayTeam["A2".playerId].apply {
            addSkill(SkillType.MONSTROUS_MOUTH)
        }
        val defender = homeTeam["H1".playerId]
        controller.rollForward(
            *activatePlayer(attacker1, PlayerSpecialActionType.CHOMP),
            PlayerSelected(defender),
            *chompRoll(3.d6),
            *activatePlayer(attacker2, PlayerSpecialActionType.CHOMP),
            PlayerSelected(defender),
            *chompRoll(4.d6),
        )
        state.assertNoActivePlayer()
        assertEquals(2, defender.statusEffects.count { it.type == PlayerStatusEffectType.CHOMPED })
    }

    @Test
    fun canChompMultiplePlayers() {
        val attacker = awayTeam["A1".playerId].apply {
            addSkill(SkillType.MONSTROUS_MOUTH)
        }
        val defender1 = homeTeam["H1".playerId]
        val defender2 = homeTeam["H1".playerId]
        controller.rollForward(
            *activatePlayer(attacker, PlayerSpecialActionType.CHOMP),
            PlayerSelected(defender1),
            *chompRoll(3.d6),
            EndTurn,
            EndTurn,
            *activatePlayer(attacker, PlayerSpecialActionType.CHOMP),
            PlayerSelected(defender2),
            *chompRoll(5.d6),
        )
        state.assertNoActivePlayer()
        assertTrue(defender1.hasStatusEffect(PlayerStatusEffectType.CHOMPED))
        assertTrue(defender2.hasStatusEffect(PlayerStatusEffectType.CHOMPED))
    }

    @Test
    fun distractedChomperRemovesChomp() {
        val attacker = awayTeam["A1".playerId].apply {
            addSkill(SkillType.MONSTROUS_MOUTH)
            addSkill(SkillType.BONE_HEAD)
        }
        val defender = homeTeam["H1".playerId]
        defender.makeChomped(attacker)
        controller.rollForward(
            *activatePlayer(attacker, PlayerStandardActionType.MOVE),
            *boneHead(1.d6),
        )
        state.assertNoActivePlayer()
        assertTrue(rules.isDistracted(attacker))
        assertFalse(defender.hasStatusEffect(PlayerStatusEffectType.CHOMPED))
    }

    @Test
    fun stripBallDoesntWorkOnChomp() {
        val attacker = awayTeam["A1".playerId].apply {
            addSkill(SkillType.MONSTROUS_MOUTH)
            addSkill(SkillType.STRIP_BALL)
        }
        val defender = homeTeam["H1".playerId].apply {
            makeChomped(attacker)
        }
        giveBallToPlayer(defender)
        controller.rollForward(
            *activatePlayer(attacker, PlayerSpecialActionType.CHOMP),
            PlayerSelected(defender),
            *chompRoll(6.d6),
            EndTurn,
            EndTurn,
            *activatePlayer(attacker, PlayerStandardActionType.BLOCK),
            *standardBlock(defender, 3.dblock),
        )
        state.assertNoActivePlayer()
        attacker.assertStanding()
        defender.assertStanding()
        defender.assertCoordinates(12, 5)
        assertTrue(defender.hasBall())
        assertTrue(defender.hasStatusEffect(PlayerStatusEffectType.CHOMPED))
    }

    @Ignore
    @Test
    fun tricksterRemovesChompedIfChompeeMoves() {
        // TODO Waiting for Trickster support
    }

    @Ignore
    @Test
    fun tricksterRemovesChompedIfChomperMoves() {
        // TODO Waiting for Trickster support
    }

    @Ignore
    @Test
    fun ballAndChainPlayerCanMoveWhenChomped() {
        // TODO Waiting for Ball and Chain support
    }

    @Test
    fun chompRemovedWhenChomperDodges() {
        val player = awayTeam["A1".playerId].apply {
            addSkill(SkillType.MONSTROUS_MOUTH)
        }
        val chomped = homeTeam["H1".playerId].apply {
            makeChomped(player)
        }
        controller.rollForward(
            *activatePlayer(player, PlayerStandardActionType.MOVE),
            *moveTo(14, 5),
            *dodge(6.d6),
            EndAction,
        )
        state.assertNoActivePlayer()
        assertFalse(chomped.hasStatusEffect(PlayerStatusEffectType.CHOMPED))
    }

    @Test
    fun chompRemovedWhenChomperJumps() {
        val player = awayTeam["A1".playerId].apply {
            addSkill(SkillType.MONSTROUS_MOUTH)
        }
        homeTeam["H1".playerId].putProne()
        val chomped = homeTeam["H2".playerId].apply {
            makeChomped(player)
        }
        controller.rollForward(
            *activatePlayer(player, PlayerStandardActionType.MOVE),
            *jumpTo(11, 4),
            *jump(6.d6),
            EndAction,
        )
        state.assertNoActivePlayer()
        assertFalse(chomped.hasStatusEffect(PlayerStatusEffectType.CHOMPED))
    }

    @Test
    fun chompRemovedWhenChomperLeaps() {
        val player = awayTeam["A1".playerId].apply {
            addSkill(SkillType.MONSTROUS_MOUTH)
            addSkill(SkillType.LEAP)
        }
        val chomped = homeTeam["H2".playerId].apply {
            makeChomped(player)
        }
        controller.rollForward(
            *activatePlayer(player, PlayerStandardActionType.MOVE),
            *leapTo(15, 4),
            Confirm, // Use Leap modifier
            *leap(6.d6),
            EndAction,
        )
        state.assertNoActivePlayer()
        assertFalse(chomped.hasStatusEffect(PlayerStatusEffectType.CHOMPED))
    }

    @Test
    fun chompRemovedWhenChomperPogo() {
        val player = awayTeam["A1".playerId].apply {
            addSkill(SkillType.MONSTROUS_MOUTH)
            addSkill(SkillType.POGO_STICK)
        }
        val chomped = homeTeam["H2".playerId].apply {
            makeChomped(player)
        }
        controller.rollForward(
            *activatePlayer(player, PlayerStandardActionType.MOVE),
            *pogoTo(15, 4),
            *pogoRoll(6.d6),
            EndAction,
        )
        state.assertNoActivePlayer()
        assertFalse(chomped.hasStatusEffect(PlayerStatusEffectType.CHOMPED))
    }

    @Test
    fun chompRemovedWhenChomperUsesShadowing() {
        val shadower = awayTeam["A1".playerId].apply {
            addSkill(SkillType.SHADOWING)
            addSkill(SkillType.MONSTROUS_MOUTH)
        }
        val mover = homeTeam["H1".playerId]
        val chomped = homeTeam["H2".playerId].apply {
            makeChomped(shadower)
        }
        controller.rollForward(
            EndTurn,
            *activatePlayer(mover, PlayerStandardActionType.MOVE),
            *moveTo(11, 4),
            *dodge(6.d6),
            *shadowPlayer(shadower, 6.d6),
        )
        assertTrue(chomped.hasStatusEffect(PlayerStatusEffectType.CHOMPED))
        controller.rollForward(
            *moveTo(10, 3),
            *dodge(6.d6),
            *shadowPlayer(shadower, 6.d6),
            EndAction
        )
        state.assertNoActivePlayer()
        assertFalse(chomped.hasStatusEffect(PlayerStatusEffectType.CHOMPED))
    }

    @Test
    fun chompRemovedWhenChomperUsesDivingTackle() {
        val mover = awayTeam["A1".playerId]
        val tackler = homeTeam["H1".playerId].apply {
            addSkill(SkillType.MONSTROUS_MOUTH)
            addSkill(SkillType.DIVING_TACKLE)
        }
        val chomped = awayTeam["A2".playerId].apply {
            makeChomped(tackler)
        }
        assertEquals(3, mover.agility)
        controller.rollForward(
            *activatePlayer(mover, PlayerStandardActionType.MOVE),
            *moveTo(14, 5),
            *dodge(4.d6),
            PlayerSelected(tackler),
            DiceRollResults(1.d6, 1.d6)
        )
        assertNull(state.activePlayer)
        mover.assertProne()
        tackler.assertProne()
        assertFalse(chomped.hasStatusEffect(PlayerStatusEffectType.CHOMPED))
    }

    @Test
    fun chompRemovedWhenChomperIsThrownUsingThrowTeammate() {
        setupAndStartThrowTeamMateGame()
        val thrower = awayTeam["A1".playerId]
        val thrownPlayer = awayTeam["A13".playerId].apply {
            addSkill(SkillType.MONSTROUS_MOUTH)
        }
        val chomped = homeTeam["H1".playerId].apply {
            makeChomped(thrownPlayer)
        }
        controller.rollForward(
            *activatePlayer(thrower, PlayerStandardActionType.THROW_TEAM_MATE),
            PlayerSelected(thrownPlayer),
            PitchSquareSelected(11, 5),
            *qualityRoll(6.d6),
            DiceRollResults(4.d8, 4.d8, 4.d8),
            *landingRoll(6.d6)
        )
        state.assertNoActivePlayer()
        assertFalse(chomped.hasStatusEffect(PlayerStatusEffectType.CHOMPED))
    }

    @Ignore
    @Test
    fun chompRemovedWhenChomperIsThrownUsingKickTeammate() {
        // Wait for Kick Teammate to be implemented
    }

    @Ignore
    @Test
    fun chompRemovedChomperUsesIllCarryYou() {
        // Wait for I'll Carry You to be implemented

    }

    @Test
    fun chompRemovedAtEndOfDrive() {
        val chomper = awayTeam["A1".playerId].apply {
            addSkill(SkillType.MONSTROUS_MOUTH)
        }
        val chomped = homeTeam["H1".playerId].apply {
            makeChomped(chomper)
        }
        controller.rollForward(
            *skipTurns(16),
        )
        assertFalse(chomped.hasStatusEffect(PlayerStatusEffectType.CHOMPED))
    }

    @Ignore
    @Test
    fun chompedIsNotRemovedIfFailingTentacles() {
        // Wait for Tentacles to be implemented
    }
}
