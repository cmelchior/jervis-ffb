package com.jervisffb.test.bb2025.inducements

import com.jervisffb.engine.actions.Cancel
import com.jervisffb.engine.actions.Confirm
import com.jervisffb.engine.actions.D6Result
import com.jervisffb.engine.actions.D8Result
import com.jervisffb.engine.actions.DiceRollResults
import com.jervisffb.engine.actions.EndTurn
import com.jervisffb.engine.actions.GameAction
import com.jervisffb.engine.actions.InducementEffectSelected
import com.jervisffb.engine.actions.InducementsSelected
import com.jervisffb.engine.actions.NoRerollSelected
import com.jervisffb.engine.actions.PitchSquareSelected
import com.jervisffb.engine.actions.PlayerSelected
import com.jervisffb.engine.actions.PlayersSelected
import com.jervisffb.engine.bb2025.inducements.wizards.Fireball
import com.jervisffb.engine.bb2025.inducements.wizards.WizardType2025
import com.jervisffb.engine.bb2025.inducements.wizards.Zap
import com.jervisffb.engine.bb2025.modifiers.PlayerStatusEffectType2025
import com.jervisffb.engine.bb2025.procedures.TeamTurn
import com.jervisffb.engine.common.inducements.InducementSelectionCommon
import com.jervisffb.engine.ext.d3
import com.jervisffb.engine.ext.d6
import com.jervisffb.engine.ext.d8
import com.jervisffb.engine.ext.playerId
import com.jervisffb.engine.model.BallState
import com.jervisffb.engine.model.Player
import com.jervisffb.engine.model.PlayerKeyword
import com.jervisffb.engine.model.TurnOver
import com.jervisffb.engine.model.hasSkill
import com.jervisffb.engine.model.inducements.Timing
import com.jervisffb.engine.model.inducements.wizard.Wizard
import com.jervisffb.engine.model.modifiers.SimplePlayerStatusEffect
import com.jervisffb.engine.model.modifiers.StatModifier
import com.jervisffb.engine.rules.common.actions.PlayerStandardActionType
import com.jervisffb.engine.rules.common.skills.Duration
import com.jervisffb.engine.rules.common.skills.SkillType
import com.jervisffb.test.JervisGameBB2025Test
import com.jervisffb.test.SmartMoveTo
import com.jervisffb.test.activatePlayer
import com.jervisffb.test.bounce
import com.jervisffb.test.defaultDetermineKickingTeam
import com.jervisffb.test.defaultFanFactor
import com.jervisffb.test.defaultKickOffEvent
import com.jervisffb.test.defaultKickOffHomeTeam
import com.jervisffb.test.defaultSetup
import com.jervisffb.test.defaultWeather
import com.jervisffb.test.ext.rollForward
import com.jervisffb.test.ext.undoActions
import com.jervisffb.test.giveBallToPlayer
import com.jervisffb.test.skipTurns
import com.jervisffb.test.steadyFootingRoll
import com.jervisffb.test.utils.assertActiveTeam
import com.jervisffb.test.utils.assertBadlyHurt
import com.jervisffb.test.utils.assertCoordinates
import com.jervisffb.test.utils.assertKnockedDown
import com.jervisffb.test.utils.assertNoTurnOver
import com.jervisffb.test.utils.assertProne
import com.jervisffb.test.utils.assertReserves
import com.jervisffb.test.utils.assertStanding
import com.jervisffb.test.utils.assertStunned
import com.jervisffb.test.utils.putProne
import com.jervisffb.test.utils.putStunned
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertSame
import kotlin.test.assertTrue

class SportsWizardTests: JervisGameBB2025Test() {

    @BeforeTest
    override fun setUp() {
        super.setUp()
        startGameWithSportsWizard()
    }

    /**
     * Start a normal game where the away team has bought a Sports-Wizard.
     */
    private fun startGameWithSportsWizard(
        kickOffEvent: Array<GameAction?> = defaultKickOffEvent(),
        bounce: D8Result? = 4.d8,
    ) {
        homeTeam.currentTeamValue = 1_200_000
        awayTeam.currentTeamValue = 1_000_000

        controller.rollForward(
            *defaultFanFactor(),
            defaultWeather(),
            InducementsSelected(InducementSelectionCommon.Wizard(WizardType2025.SPORTS_WIZARD)),
            *defaultDetermineKickingTeam(),
            *defaultSetup(),
            *defaultKickOffHomeTeam(kickoffEvent = kickOffEvent, bounce = bounce),
        )
    }

    private val wizard: Wizard get() = awayTeam.wizards.single()
    private val fireballSpell: Fireball get() = wizard.spells.filterIsInstance<Fireball>().single()
    private val zapSpell: Zap get() = wizard.spells.filterIsInstance<Zap>().single()

    /**
     * End the away teams turn and have their Sports-Wizard cast "Zap!" on [target].
     */
    private fun castZapOn(target: Player, roll: D6Result = 6.d6): Array<GameAction> {
        return arrayOf(
            EndTurn,
            InducementEffectSelected(zapSpell.id),
            PlayerSelected(target.id),
            roll,
            NoRerollSelected(),
        )
    }

    /**
     * End the away teams turn and have their Sports-Wizard cast "Fireball" at [target].
     */
    private fun castFireballAt(target: PitchSquareSelected): Array<GameAction> {
        return arrayOf(
            EndTurn,
            InducementEffectSelected(fireballSpell.id),
            target,
        )
    }

    @Test
    fun worksDuringCharge() {
        setupDefaultGame()
        val chargingPlayers = listOf("H6".playerId, "H7".playerId, "H8".playerId, "H9".playerId)
        startGameWithSportsWizard(
            kickOffEvent = arrayOf(
                DiceRollResults(6.d6, 4.d6), // Roll Charge!
                1.d3, // How many players to activate
                PlayersSelected(chargingPlayers),
                EndTurn, // End the Charge without activating anyone
            ),
            bounce = null,
        )

        val target = homeTeam["H1".playerId]
        controller.rollForward(
            InducementEffectSelected(zapSpell.id),
            PlayerSelected(target.id),
            6.d6,
        )
        assertTrue(zapSpell.used)
        assertTrue(target.isTransformed)
        controller.rollForward(
            bounce(4.d8)
        )
        state.assertActiveTeam(awayTeam)
    }

    @Test
    fun fireball_useAtEndOfTeamTurn() {
        val target = homeTeam["H10".playerId]
        controller.rollForward(
            *castFireballAt(PitchSquareSelected(9, 7)), // Only H10 is in the blast
            4.d6, // Hit
            NoRerollSelected(),
            DiceRollResults(1.d6, 1.d6),
        )
        assertTrue(fireballSpell.used)
        assertTrue(wizard.used)
        target.assertProne()
        state.assertActiveTeam(homeTeam)
    }

    @Test
    fun fireball_useAtEndOfOpponentTurn() {
        val target = homeTeam["H10".playerId]
        controller.rollForward(
            EndTurn,
            Cancel, // Away team does not use the wizard at the end of their own turn
            EndTurn, // Home team ends their turn
            InducementEffectSelected(fireballSpell.id),
            PitchSquareSelected(9, 7), // Only H10 is in the blast
            4.d6, // Hit. Team rerolls are not available as the away team isn't the active team
            DiceRollResults(1.d6, 1.d6), // Armour holds
        )
        assertTrue(fireballSpell.used)
        target.assertProne()
        state.assertActiveTeam(awayTeam)
    }

    @Test
    fun fireball_oneTimeUse() {
        controller.rollForward(
            *castFireballAt(PitchSquareSelected(3, 7)), // Only H11 is in the blast
            1.d6, // Miss
            NoRerollSelected(),
        )
        assertTrue(fireballSpell.used)
        assertFalse(zapSpell.used)

        // The Sports-Wizard only casts a single spell pr. game, so "Zap!" is no longer
        // available either.
        assertTrue(wizard.used)
        assertTrue(wizard.getAvailableSpells(Timing.END_OF_OWN_TURN).isEmpty())
        assertTrue(wizard.getAvailableSpells(Timing.END_OF_OPPONENT_TURN).isEmpty())

        // Which means no team is asked about inducement effects at the end of a turn anymore.
        controller.rollForward(*skipTurns(2))
        assertEquals(TeamTurn.SelectPlayerOrEndTurn, controller.currentNode())
    }

    @Test
    fun fireball_4PlusToHit() {
        val missed = homeTeam["H6".playerId]
        val hit = homeTeam["H7".playerId]
        controller.rollForward(
            *castFireballAt(PitchSquareSelected(11, 1)), // H6 is in the target square, H7 is next to it
            3.d6, // H6 is missed on a 3
            NoRerollSelected(),
            4.d6, // H7 is hit on a 4
            NoRerollSelected(),
            DiceRollResults(1.d6, 1.d6), // Armour holds
        )
        missed.assertStanding()
        hit.assertProne()
    }

    @Test
    fun fireball_rollArmourForProneOrStunnedPlayers() {
        // For stunned/prone players we roll for AV without treating the player as Knocked Down first.
        val pronePlayer = homeTeam["H9".playerId]
        val stunnedPlayer = homeTeam["H8".playerId]
        pronePlayer.putProne()
        stunnedPlayer.putStunned()
        assertEquals(9, pronePlayer.armorValue)
        assertEquals(9, stunnedPlayer.armorValue)

        controller.rollForward(
            *castFireballAt(PitchSquareSelected(11, 13)), // H9 is in the target square, H8 is next to it
            4.d6, // H9 is hit
            NoRerollSelected(),
        )
        // The next roll is the Armour roll, the player is never Knocked Down on the way there.
        pronePlayer.assertProne()

        controller.rollForward(
            DiceRollResults(4.d6, 4.d6), // 8 + 1 from the Fireball breaks AV 9+, just like for a standing player
            DiceRollResults(1.d6, 1.d6), // Stunned
            4.d6, // H8 is hit
            NoRerollSelected(),
        )
        // A player that was already down is only moved further down the injury ladder.
        pronePlayer.assertStunned()
        stunnedPlayer.assertStunned()

        controller.rollForward(
            DiceRollResults(1.d6, 1.d6), // Armour holds
        )
        // Not breaking the armour of an already Stunned player leaves them as they were.
        stunnedPlayer.assertStunned()
    }

    @Test
    fun fireball_armourModifierOnAllHit() {
        val target = homeTeam["H9".playerId]
        val neighbour = homeTeam["H8".playerId]
        assertEquals(9, target.armorValue)
        assertEquals(9, neighbour.armorValue)
        controller.rollForward(
            *castFireballAt(PitchSquareSelected(11, 13)), // H9 is in the target square, H8 is next to it
            4.d6, // H9 is hit
            NoRerollSelected(),
            DiceRollResults(4.d6, 4.d6), // 8 + 1 from the Fireball breaks AV 9+
            DiceRollResults(1.d6, 1.d6), // Stunned
            4.d6, // H8 is hit
            NoRerollSelected(),
            DiceRollResults(4.d6, 4.d6), // The modifier also applies to the second player hit
            DiceRollResults(1.d6, 1.d6), // Stunned
        )
        target.assertStunned()
        neighbour.assertStunned()
    }

    @Test
    fun fireball_steadyFootingAvoidsFireball() {
        val target = homeTeam["H10".playerId]
        target.addSkill(SkillType.STEADY_FOOTING)
        controller.rollForward(
            *castFireballAt(PitchSquareSelected(9, 7)), // Only H10 is in the blast
            4.d6, // Hit
            NoRerollSelected(),
            Confirm, // Use Steady Footing
            *steadyFootingRoll(6.d6, null),
        )
        target.assertStanding()
        assertEquals(TeamTurn.SelectPlayerOrEndTurn, controller.currentNode())
        state.assertActiveTeam(homeTeam)
    }

    @Test
    fun fireball_noTurnoverIfHittingPronePlayer() {
        val pronePlayer = homeTeam["H9".playerId]
        val standingPlayer = homeTeam["H8".playerId]
        pronePlayer.putProne()
        standingPlayer.assertStanding()

        controller.rollForward(
            EndTurn,
            Cancel, // Away team does not use the wizard at the end of their own turn
            EndTurn, // Home team ends their turn
            InducementEffectSelected(fireballSpell.id),
            PitchSquareSelected(11, 13), // H9 is in the target square, H8 is next to it
            4.d6, // H9 is hit
            DiceRollResults(4.d6, 4.d6), // Armour is broken
            DiceRollResults(1.d6, 1.d6), // Stunned
        )
        // A player that was already down cannot be knocked down, so it is not a turnover.
        state.assertNoTurnOver()
        pronePlayer.assertStunned(ownTeamTurn = true)

        // Knocking down a standing player on the active team is a turnover, though.
        controller.rollForward(4.d6) // H8 is hit
        standingPlayer.assertKnockedDown()
        assertEquals(TurnOver.STANDARD, state.turnOver)
    }

    @Test
    fun zap_endOfTeamTurn() {
        val target = homeTeam["H1".playerId]
        controller.rollForward(
            *castZapOn(target)
        )
        assertTrue(zapSpell.used)
        assertTrue(target.isTransformed)
        assertTrue(target.keywords.contains(PlayerKeyword.FROG))
        assertEquals("Frog", target.position.titleSingular)
        state.assertActiveTeam(homeTeam)
    }

    @Test
    fun zap_endOfOpponentTurn() {
        val target = homeTeam["H1".playerId]
        controller.rollForward(
            EndTurn,
            Cancel, // Away team does not use the wizard at the end of their own turn
            EndTurn, // Home team ends their turn
            InducementEffectSelected(zapSpell.id),
            PlayerSelected(target.id),
            6.d6, // Team rerolls are not available as the away team isn't the active team
        )
        assertTrue(zapSpell.used)
        assertTrue(target.isTransformed)
        state.assertActiveTeam(awayTeam)
    }

    @Test
    fun zap_oneTimeUse() {
        val target = homeTeam["H1".playerId]
        controller.rollForward(
            *castZapOn(target)
        )
        assertTrue(zapSpell.used)
        assertFalse(fireballSpell.used)

        // The Sports-Wizard only casts a single spell pr. game, so "Fireball" is no longer
        // available either.
        assertTrue(wizard.used)
        assertTrue(wizard.getAvailableSpells(Timing.END_OF_OWN_TURN).isEmpty())
        assertTrue(wizard.getAvailableSpells(Timing.END_OF_OPPONENT_TURN).isEmpty())

        // Which means no team is asked about inducement effects at the end of a turn anymore.
        controller.rollForward(*skipTurns(2))
        assertEquals(TeamTurn.SelectPlayerOrEndTurn, controller.currentNode())
    }

    @Test
    fun zap_lessThanStrength() {
        val target = homeTeam["H1".playerId]
        assertEquals(3, target.strength)
        controller.rollForward(
            *castZapOn(target, 2.d6)
        )
        assertFalse(target.isTransformed)
        target.assertStanding()
        // The spell is spent, even if the target resisted it.
        assertTrue(zapSpell.used)
    }

    @Test
    fun zap_equalToStrengthOrHigher() {
        val target = homeTeam["H1".playerId]
        assertEquals(3, target.strength)
        controller.rollForward(
            *castZapOn(target, 3.d6)
        )
        assertTrue(target.isTransformed)

        // Anything above the target's Strength works just as well.
        controller.undoActions(2)
        assertFalse(target.isTransformed)
        controller.rollForward(4.d6, NoRerollSelected())
        assertTrue(target.isTransformed)
    }

    @Test
    fun zap_bounceBall() {
        val target = homeTeam["H1".playerId]
        giveBallToPlayer(target)
        controller.rollForward(
            *castZapOn(target)
        )
        assertTrue(target.isTransformed)
        // A Frog cannot hold the ball, so it bounces out of their square.
        controller.rollForward(4.d8)
        assertFalse(target.hasBall())
        val ball = state.singleBall()
        ball.assertCoordinates(11, 5)
        assertEquals(BallState.ON_GROUND, ball.state)
    }

    @Test
    fun zap_pronePlayer() {
        val target = homeTeam["H1".playerId]
        target.putProne()
        controller.rollForward(
            *castZapOn(target)
        )
        assertTrue(target.isTransformed)
        // Being turned into a Frog does not change the state of the player.
        target.assertProne()
    }

    @Test
    fun zap_noApothecary() {
        val target = homeTeam["H9".playerId]
        controller.rollForward(
            *castZapOn(target)
        )
        target.putProne() // Make the Frog a legal target for a Foul
        assertEquals(5, target.armorValue)
        assertTrue(target.hasSkill(SkillType.STUNTY))

        controller.rollForward(
            EndTurn, // Home team ends their turn
            *activatePlayer("A9", PlayerStandardActionType.FOUL),
            SmartMoveTo(12, 13),
            PlayerSelected(target),
            DiceRollResults(2.d6, 3.d6), // Armour is broken
            DiceRollResults(4.d6, 6.d6), // Casualty on the Stunty Injury Table (no double, so no send-off)
        )
        // A Frog does not roll on the Casualty Table and cannot be treated by an Apothecary,
        // so the injury is resolved without any further input.
        assertTrue(target.isTransformed)
        target.assertBadlyHurt()
        assertFalse(homeTeam.teamApothecaries.single().used)
        assertEquals(TeamTurn.SelectPlayerOrEndTurn, controller.currentNode())
    }

    @Test
    fun zap_returnToNormalAtEndOfDrive() {
        val target = homeTeam["H1".playerId]
        val square = target.coordinates
        val normalPosition = target.position
        val normalKeywords = target.keywords.toList()

        // Level-ups are lost while a Frog, so this should disappear and come back again.
        target.addSkill(rules.createSkill(target, SkillType.BLOCK.id()))
        // Temporary effects are kept and keep applying while a Frog.
        target.addStatModifier(StatModifier(StatModifier.Type.MA, -1, "Dodgy Snack (-1 MV)", Duration.END_OF_GAME))
        rules.updatePlayerStat(target, StatModifier.Type.MA)
        val normalMoveWithSnack = target.move

        controller.rollForward(
            *castZapOn(target)
        )

        assertTrue(target.isTransformed)
        assertEquals("Frog", target.position.titleSingular)
        // Frog MA 5, still reduced by the Dodgy Snack the player brought with them.
        assertEquals(4, target.move)
        assertEquals(1, target.strength)
        assertEquals(2, target.agility)
        assertNull(target.passing)
        assertEquals(5, target.armorValue)
        assertTrue(target.hasSkill(SkillType.STUNTY))
        assertTrue(target.keywords.contains(PlayerKeyword.FROG))
        assertFalse(target.hasSkill(SkillType.BLOCK))

        // The player keeps their identity, so every reference still points at them.
        assertSame(target, homeTeam[target.number])
        assertSame(target, state.pitch[square].player)

        // Transforming must be undoable like any other change to the game state.
        controller.undoActions(1)
        assertFalse(target.isTransformed)
        assertEquals(normalPosition, target.position)
        assertEquals(normalMoveWithSnack, target.move)
        assertTrue(target.hasSkill(SkillType.BLOCK))
        assertFalse(target.hasSkill(SkillType.STUNTY))
        controller.rollForward(NoRerollSelected())
        assertTrue(target.isTransformed)

        // Anything temporary picked up as a Frog follows the player back out again.
        val distracted = SimplePlayerStatusEffect(PlayerStatusEffectType2025.DISTRACTED, Duration.END_OF_GAME)
        target.addStatusEffect(distracted)
        target.addSkill(rules.createSkill(target, SkillType.PRO.id(), Duration.END_OF_GAME))

        // Run out the half, which also ends the drive.
        controller.rollForward(*skipTurns(15))

        assertFalse(target.isTransformed)
        assertEquals(normalPosition, target.position)
        assertEquals(normalKeywords, target.keywords)
        assertEquals(normalMoveWithSnack, target.move)
        assertFalse(target.hasSkill(SkillType.STUNTY))
        assertTrue(target.hasSkill(SkillType.BLOCK))
        assertTrue(target.hasSkill(SkillType.PRO))
        assertTrue(target.statusEffects.contains(distracted))
        assertSame(target, homeTeam[target.number])
    }

    @Test
    fun zap_returnToNormalAfterCasualtyAndEndOfDrive() {
        val target = homeTeam["H9".playerId]
        val normalPosition = target.position
        controller.rollForward(
            *castZapOn(target)
        )
        target.putProne() // Make the Frog a legal target for a Foul
        controller.rollForward(
            EndTurn, // Home team ends their turn
            *activatePlayer("A9", PlayerStandardActionType.FOUL),
            SmartMoveTo(12, 13),
            PlayerSelected(target),
            DiceRollResults(2.d6, 3.d6), // Armour is broken
            DiceRollResults(4.d6, 6.d6), // Casualty on the Stunty Injury Table (no double, so no send-off)
        )
        // A Frog stays a Frog until the end of the drive, even in the dugout.
        assertTrue(target.isTransformed)
        target.assertBadlyHurt()

        // Run out the half, which also ends the drive.
        controller.rollForward(*skipTurns(14))

        // The player returns to normal with no ill effects, ready for the next drive.
        assertFalse(target.isTransformed)
        assertEquals(normalPosition, target.position)
        target.assertReserves()
    }
}
