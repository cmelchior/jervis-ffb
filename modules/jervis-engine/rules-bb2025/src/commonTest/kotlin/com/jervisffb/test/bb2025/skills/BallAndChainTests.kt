package com.jervisffb.test.bb2025.skills

import com.jervisffb.engine.actions.Cancel
import com.jervisffb.engine.actions.Confirm
import com.jervisffb.engine.actions.DiceRollResults
import com.jervisffb.engine.actions.DirectionSelected
import com.jervisffb.engine.actions.EndAction
import com.jervisffb.engine.actions.GameAction
import com.jervisffb.engine.actions.NoRerollSelected
import com.jervisffb.engine.actions.PlayerSelected
import com.jervisffb.engine.actions.SelectBlockType
import com.jervisffb.engine.actions.SelectDirection
import com.jervisffb.engine.actions.SelectPlayerAction
import com.jervisffb.engine.actions.SelectRerollOption
import com.jervisffb.engine.actions.Undo
import com.jervisffb.engine.bb2025.context.BallAndChainContext
import com.jervisffb.engine.bb2025.procedures.actions.ballandchain.BallAndChainAction
import com.jervisffb.engine.bb2025.procedures.rerolls.StandardTeamReroll
import com.jervisffb.engine.bb2025.skills.BallAndChain
import com.jervisffb.engine.bb2025.skills.Brawler
import com.jervisffb.engine.bb2025.tables.PrayerToNuffleTableResult2025
import com.jervisffb.engine.commands.SetBallLocation
import com.jervisffb.engine.commands.SetBallState
import com.jervisffb.engine.common.context.BlockContext
import com.jervisffb.engine.common.context.RiskingInjuryContext
import com.jervisffb.engine.common.modifiers.isRooted
import com.jervisffb.engine.common.modifiers.rooted
import com.jervisffb.engine.common.reports.ReportInjuryResult
import com.jervisffb.engine.ext.d3
import com.jervisffb.engine.ext.d6
import com.jervisffb.engine.ext.d8
import com.jervisffb.engine.ext.dblock
import com.jervisffb.engine.ext.playerId
import com.jervisffb.engine.model.Availability
import com.jervisffb.engine.model.Direction
import com.jervisffb.engine.model.Player
import com.jervisffb.engine.model.PlayerDogoutState
import com.jervisffb.engine.model.PlayerId
import com.jervisffb.engine.model.PlayerNo
import com.jervisffb.engine.model.PlayerPitchState
import com.jervisffb.engine.model.context.getContextOrNull
import com.jervisffb.engine.model.locations.PitchCoordinate
import com.jervisffb.engine.model.modifiers.PlayerStatusEffect
import com.jervisffb.engine.rules.common.actions.PlayerSpecialActionType
import com.jervisffb.engine.rules.common.actions.PlayerStandardActionType
import com.jervisffb.engine.rules.common.skills.SkillType
import com.jervisffb.engine.rules.common.tables.Weather
import com.jervisffb.engine.teamBuilder
import com.jervisffb.test.JervisGameBB2025Test
import com.jervisffb.test.activatePlayer
import com.jervisffb.test.bb2025.HUMAN_LINEMAN
import com.jervisffb.test.ext.rollForward
import com.jervisffb.test.giveBallToPlayer
import com.jervisffb.test.movePlayerTo
import com.jervisffb.test.rushRoll
import com.jervisffb.test.standardBlock
import com.jervisffb.test.utils.SelectSingleBlockDieResult
import com.jervisffb.test.utils.TeamRerollSelected
import com.jervisffb.test.utils.assertCoordinates
import com.jervisffb.test.utils.makeChomped
import com.jervisffb.test.utils.putProne
import com.jervisffb.test.utils.putStunned
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

class BallAndChainTests : JervisGameBB2025Test() {
    private val mover: Player get() = awayTeam["A1".playerId]
    private val target: Player get() = homeTeam["H1".playerId]

    @BeforeTest
    override fun setUp() {
        super.setUp()
        startDefaultGame()
        mover.addSkill(SkillType.BALL_AND_CHAIN)
        movePlayerTo(mover, PitchCoordinate(16, 10))
        movePlayerTo(target, PitchCoordinate(15, 10))
    }

    private fun step(direction: Direction = Direction.LEFT): Array<GameAction> = arrayOf(
        DirectionSelected(direction), 2.d3, NoRerollSelected(),
    )

    private fun start() = controller.rollForward(*activatePlayer(mover, PlayerSpecialActionType.BALL_AND_CHAIN))

    private fun pushBlock() = controller.rollForward(3.dblock, NoRerollSelected(), SelectSingleBlockDieResult(), DirectionSelected(Direction.LEFT))

    private fun assertContextsCleared() {
        assertNull(state.getContextOrNull<BallAndChainContext>())
        assertNull(state.getContextOrNull<BlockContext>())
        assertNull(state.getContextOrNull<RiskingInjuryContext>())
        assertNull(state.getRerollContextOrNull())
    }

    @Test
    fun compulsoryActionTakesPrecedenceOverOtherSpecialActions() {
        mover.addSkill(SkillType.STAB)
        mover.addSkill(SkillType.PUNT)
        controller.rollForward(PlayerSelected(mover))
        val actions = controller.getAvailableActions().get<SelectPlayerAction>().actions
        assertEquals(listOf(PlayerSpecialActionType.BALL_AND_CHAIN), actions.map { it.type })
        assertEquals(actions, rules.getAvailableActions(state, mover))
    }

    @Test
    fun canReorientAndEndAfterMoving() {
        start()
        controller.rollForward(*step(Direction.DOWN), *step(Direction.RIGHT), EndAction)
        mover.assertCoordinates(17, 11)
        assertEquals(mover.move - 2, mover.movesLeft)
        assertEquals(Availability.HAS_ACTIVATED, mover.available)
        assertNull(state.activePlayer)
        assertContextsCleared()
    }

    @Test
    fun multiplePlayersCanUseTheActionWithoutConsumingStandardAllowances() {
        val other = awayTeam["A2".playerId]
        other.addSkill(SkillType.BALL_AND_CHAIN)
        movePlayerTo(other, PitchCoordinate(19, 10))
        val allowances = listOf(awayTeam.turnData.blitzActions, awayTeam.turnData.foulActions, awayTeam.turnData.passActions)
        start()
        controller.rollForward(*step(Direction.DOWN), EndAction, *activatePlayer(other, PlayerSpecialActionType.BALL_AND_CHAIN), *step(Direction.DOWN), EndAction)
        assertEquals(allowances, listOf(awayTeam.turnData.blitzActions, awayTeam.turnData.foulActions, awayTeam.turnData.passActions))
        assertEquals(Availability.HAS_ACTIVATED, other.available)
    }

    @Test
    fun standingOpponentIsBlockedAndFollowUpCostsNoExtraMovement() {
        start()
        controller.rollForward(*step())
        pushBlock()
        mover.assertCoordinates(15, 10)
        target.assertCoordinates(14, 10)
        assertEquals(mover.move - 1, mover.movesLeft)
        assertEquals(BallAndChainAction.SelectTemplateOrientation, state.stack.currentNode())
        controller.rollForward(*step(Direction.DOWN), EndAction)
        assertContextsCleared()
    }

    @Test
    fun activeCoachSelectsResultAgainstStrongerTeamMate() {
        val teamMate = awayTeam["A2".playerId]
        movePlayerTo(target, PitchCoordinate(10, 10))
        movePlayerTo(teamMate, PitchCoordinate(15, 10))
        teamMate.baseStrength = 6
        teamMate.strength = 6
        start()
        controller.rollForward(*step(), DiceRollResults(3.dblock, 3.dblock), NoRerollSelected())
        assertEquals(awayTeam, controller.getAvailableActions().team)
        controller.rollForward(SelectSingleBlockDieResult(), DirectionSelected(Direction.LEFT), EndAction)
        mover.assertCoordinates(15, 10)
        teamMate.assertCoordinates(14, 10)
        assertContextsCleared()
    }

    @Test
    fun standFirmSpendsMovementAndRetainsOrigin() {
        target.addSkill(SkillType.STAND_FIRM)
        checkBlockedStep(Confirm)
    }

    @Test
    fun rootedTargetSpendsMovementAndRetainsOrigin() {
        target.statusEffects.add(PlayerStatusEffect.rooted())
        checkBlockedStep()
    }

    @Test
    fun chompedTargetSpendsMovementAndRetainsOrigin() {
        mover.addSkill(SkillType.MONSTROUS_MOUTH)
        target.makeChomped(mover)
        checkBlockedStep()
    }

    private fun checkBlockedStep(vararg pushActions: GameAction) {
        start()
        controller.rollForward(*step(), 3.dblock, NoRerollSelected(), SelectSingleBlockDieResult(), *pushActions)
        mover.assertCoordinates(16, 10)
        target.assertCoordinates(15, 10)
        assertEquals(mover.move - 1, mover.movesLeft)
        assertEquals(BallAndChainAction.SelectTemplateOrientation, state.stack.currentNode())
        controller.rollForward(EndAction)
        assertContextsCleared()
    }

    @Test
    fun proneOccupantIsPushedThenArmourIsRolled() {
        target.putProne()
        start()
        controller.rollForward(*step(), DirectionSelected(Direction.LEFT))
        mover.assertCoordinates(15, 10)
        target.assertCoordinates(14, 10)
        controller.rollForward(DiceRollResults(1.d6, 1.d6), EndAction)
        assertEquals(PlayerPitchState.PRONE, target.state)
        assertFalse(state.isTurnOver())
        assertContextsCleared()
    }

    @Test
    fun stunnedOccupantRemainsStunnedWhenArmourHolds() {
        target.putStunned()
        start()
        controller.rollForward(*step(), DirectionSelected(Direction.LEFT), DiceRollResults(1.d6, 1.d6), EndAction)
        assertEquals(PlayerPitchState.STUNNED, target.state)
        assertContextsCleared()
    }

    @Test
    fun looseBallBouncesWithoutPickupOrTurnover() {
        val ball = state.singleBall()
        SetBallState.onGround(ball).execute(state)
        SetBallLocation(ball, PitchCoordinate(16, 11)).execute(state)
        start()
        controller.rollForward(*step(Direction.DOWN), 8.d8, EndAction)
        assertFalse(mover.hasBall())
        assertFalse(state.isTurnOver())
        assertContextsCleared()
    }

    @Test
    fun failedRushMovesBeforeInjuryAndSkipsArmour() {
        start()
        mover.movesLeft = 0
        controller.rollForward(*step(Direction.DOWN), *rushRoll(1.d6))
        mover.assertCoordinates(16, 11)
        controller.rollForward(DiceRollResults(1.d6, 1.d6), Cancel)
        assertEquals(PlayerDogoutState.KNOCKED_OUT, mover.state)
        assertNull(state.activePlayer)
        assertContextsCleared()
    }

    @Test
    fun failedRushCompletesBlockBeforeFallingOver() {
        start()
        mover.movesLeft = 0
        controller.rollForward(*step(), *rushRoll(1.d6))
        pushBlock()
        mover.assertCoordinates(15, 10)
        controller.rollForward(DiceRollResults(1.d6, 1.d6), Cancel)
        assertEquals(PlayerDogoutState.KNOCKED_OUT, mover.state)
        assertContextsCleared()
    }

    @Test
    fun failedRushIntoStandFirmFallsAtOrigin() {
        target.addSkill(SkillType.STAND_FIRM)
        start()
        mover.movesLeft = 0
        controller.rollForward(*step(), *rushRoll(1.d6), 3.dblock, NoRerollSelected(), SelectSingleBlockDieResult(), Confirm)
        mover.assertCoordinates(16, 10)
        controller.rollForward(DiceRollResults(1.d6, 1.d6), Cancel)
        assertEquals(PlayerDogoutState.KNOCKED_OUT, mover.state)
        assertContextsCleared()
    }

    @Test
    fun failedRushAndAttackerDownCauseOnlyOneInjury() {
        start()
        mover.movesLeft = 0
        controller.rollForward(*step(), *rushRoll(1.d6), 1.dblock, NoRerollSelected(), SelectSingleBlockDieResult(), DiceRollResults(1.d6, 1.d6), Cancel)
        assertEquals(PlayerDogoutState.KNOCKED_OUT, mover.state)
        assertNull(state.activePlayer)
        assertContextsCleared()
    }

    @Test
    fun blizzardRushFailureOnTwoIsAlsoDelayed() {
        state.weather = Weather.BLIZZARD
        start()
        mover.movesLeft = 0
        controller.rollForward(*step(Direction.DOWN), *rushRoll(2.d6))
        mover.assertCoordinates(16, 11)
        controller.rollForward(DiceRollResults(1.d6, 1.d6), Cancel)
        assertEquals(PlayerDogoutState.KNOCKED_OUT, mover.state)
    }

    @Test
    fun crowdStunnedGoesToReservesEvenAfterFailedRush() {
        movePlayerTo(mover, PitchCoordinate(16, 0))
        start()
        mover.movesLeft = 0
        controller.rollForward(*step(Direction.UP), *rushRoll(1.d6), DiceRollResults(1.d6, 1.d6))
        assertEquals(PlayerDogoutState.RESERVE, mover.state)
        assertNull(state.activePlayer)
        assertContextsCleared()
    }

    @Test
    fun allTemplateOrientationsAreAvailableAtTheEdge() {
        movePlayerTo(mover, PitchCoordinate(16, 0))
        start()
        assertEquals(4, controller.getAvailableActions().get<SelectDirection>().directions.size)
    }

    @Test
    fun ignoresDodgeTentaclesAndShadowing() {
        target.addSkill(SkillType.TENTACLES)
        target.addSkill(SkillType.SHADOWING)
        start()
        controller.rollForward(*step(Direction.RIGHT), EndAction)
        mover.assertCoordinates(17, 10)
        target.assertCoordinates(15, 10)
        assertContextsCleared()
    }

    @Test
    fun rootedPlayerCanMoveAndFollowUpWithoutLosingRooted() {
        mover.statusEffects.add(PlayerStatusEffect.rooted())
        start()
        controller.rollForward(*step())
        pushBlock()
        mover.assertCoordinates(15, 10)
        assertTrue(mover.isRooted())
        controller.rollForward(EndAction)
    }

    @Test
    fun directionRerollRetainsOrientation() {
        start()
        controller.rollForward(DirectionSelected(Direction.RIGHT), 1.d3, TeamRerollSelected<StandardTeamReroll>(), 2.d3, EndAction)
        mover.assertCoordinates(17, 10)
        assertContextsCleared()
    }

    @Test
    fun movementAndRushesAreExhaustedExactlyOnce() {
        start()
        repeat(mover.move) { controller.rollForward(*step(Direction.RIGHT)) }
        repeat(rules.rushesPrAction) { controller.rollForward(*step(Direction.RIGHT), *rushRoll()) }
        assertEquals(0, mover.movesLeft)
        assertEquals(0, mover.rushesLeft)
        assertFalse(controller.getAvailableActions().contains<SelectDirection>())
        controller.rollForward(EndAction)
        assertContextsCleared()
    }

    @Test
    fun sprintAddsOnlyOneExtraRush() {
        mover.addSkill(SkillType.SPRINT)
        start()
        mover.movesLeft = 0
        repeat(rules.rushesPrAction + 1) { controller.rollForward(*step(Direction.RIGHT), *rushRoll()) }
        assertEquals(0, mover.rushesLeft)
        assertFalse(controller.getAvailableActions().contains<SelectDirection>())
        controller.rollForward(EndAction)
    }

    @Test
    fun failedRushPushesAndRollsProneOccupantArmourBeforeFalling() {
        target.putProne()
        start()
        mover.movesLeft = 0
        controller.rollForward(*step(), *rushRoll(1.d6), DirectionSelected(Direction.LEFT), DiceRollResults(1.d6, 2.d6))
        mover.assertCoordinates(15, 10)
        assertEquals(PlayerPitchState.PRONE, target.state)
        controller.rollForward(DiceRollResults(1.d6, 1.d6), Cancel)
        assertEquals(PlayerDogoutState.KNOCKED_OUT, mover.state)
        assertContextsCleared()
    }

    @Test
    fun failedRushBouncesLooseBallBeforeFalling() {
        val ball = state.singleBall()
        SetBallState.onGround(ball).execute(state)
        SetBallLocation(ball, PitchCoordinate(16, 11)).execute(state)
        start()
        mover.movesLeft = 0
        controller.rollForward(*step(Direction.DOWN), *rushRoll(1.d6), 8.d8, DiceRollResults(1.d6, 1.d6), Cancel)
        assertEquals(PlayerDogoutState.KNOCKED_OUT, mover.state)
        assertContextsCleared()
    }

    @Test
    fun opponentTurnKnockdownSkipsArmourAndConvertsStunned() {
        mover.removeSkill(mover.getSkill(SkillType.BALL_AND_CHAIN))
        target.addSkill(SkillType.BALL_AND_CHAIN)
        controller.rollForward(*activatePlayer(mover, PlayerStandardActionType.BLOCK), *standardBlock(target, 6.dblock),
            DirectionSelected(Direction.LEFT), Cancel, DiceRollResults(1.d6, 1.d6), Cancel)
        assertEquals(PlayerDogoutState.KNOCKED_OUT, target.state)
        assertFalse(state.isTurnOver())
        assertContextsCleared()
    }

    @Test
    fun wrestleResolvesBothTraitInjuriesWithoutTurnover() {
        target.addSkill(SkillType.BALL_AND_CHAIN)
        target.addSkill(SkillType.WRESTLE)
        start()
        controller.rollForward(*step(), 2.dblock, NoRerollSelected(), SelectSingleBlockDieResult(), Confirm,
            DiceRollResults(1.d6, 1.d6), Cancel,
            DiceRollResults(1.d6, 1.d6), Cancel)
        assertEquals(PlayerDogoutState.KNOCKED_OUT, mover.state)
        assertEquals(PlayerDogoutState.KNOCKED_OUT, target.state)
        assertEquals(awayTeam, state.activeTeam)
        assertFalse(state.isTurnOver())
        assertNull(state.activePlayer)
        assertContextsCleared()
    }

    @Test
    fun pileDriverTriggersPlacedProneInjuryAndEndsAction() {
        mover.addSkill(SkillType.PILE_DRIVER)
        start()
        controller.rollForward(*step(), 6.dblock, NoRerollSelected(), SelectSingleBlockDieResult(), DirectionSelected(Direction.LEFT),
            DiceRollResults(1.d6, 3.d6), Confirm, DiceRollResults(1.d6, 2.d6),
            DiceRollResults(1.d6, 1.d6), Cancel)
        assertEquals(PlayerDogoutState.KNOCKED_OUT, mover.state)
        assertEquals(awayTeam, state.activeTeam)
        assertFalse(state.isTurnOver())
        assertNull(state.activePlayer)
        assertContextsCleared()
    }

    @Test
    fun unsuccessfulStabStillRequiresArmour() {
        mover.removeSkill(mover.getSkill(SkillType.BALL_AND_CHAIN))
        mover.addSkill(SkillType.STAB)
        target.addSkill(SkillType.BALL_AND_CHAIN)
        controller.rollForward(*activatePlayer(mover, PlayerSpecialActionType.STAB), PlayerSelected(target), DiceRollResults(1.d6, 1.d6))
        assertEquals(PlayerPitchState.STANDING, target.state)
        assertContextsCleared()
    }

    @Test
    fun successfulStabConvertsStunnedAfterArmourBreaks() {
        mover.removeSkill(mover.getSkill(SkillType.BALL_AND_CHAIN))
        mover.addSkill(SkillType.STAB)
        target.addSkill(SkillType.BALL_AND_CHAIN)
        controller.rollForward(*activatePlayer(mover, PlayerSpecialActionType.STAB), PlayerSelected(target),
            DiceRollResults(6.d6, 6.d6), DiceRollResults(1.d6, 1.d6), Cancel)
        assertEquals(PlayerDogoutState.KNOCKED_OUT, target.state)
        assertContextsCleared()
    }

    @Test
    fun thickSkullCannotPreventKnockout() {
        mover.addSkill(SkillType.THICK_SKULL)
        start()
        mover.movesLeft = 0
        controller.rollForward(*step(Direction.DOWN), *rushRoll(1.d6), DiceRollResults(4.d6, 4.d6), Confirm, Cancel)
        assertEquals(PlayerDogoutState.KNOCKED_OUT, mover.state)
        assertContextsCleared()
    }

    @Test
    fun apothecaryStunnedRecoveryStillBecomesKnockout() {
        start()
        mover.movesLeft = 0
        controller.rollForward(*step(Direction.DOWN), *rushRoll(1.d6), DiceRollResults(1.d6, 1.d6), Confirm)
        assertEquals(PlayerDogoutState.KNOCKED_OUT, mover.state)
        val report = controller.history.flatMap { it.steps }.flatMap { it.logs }.filterIsInstance<ReportInjuryResult>().last()
        assertEquals("${mover.name} was Knocked Out", report.message)
        assertContextsCleared()
    }

    @Test
    fun trapdoorRemovalEndsActionWithoutExtraRushInjury() {
        state.homeTeam.activePrayersToNuffle.add(PrayerToNuffleTableResult2025.TREACHEROUS_TRAPDOOR)
        state.pitch[16, 11].hasTrapdoor = true
        start()
        mover.movesLeft = 0
        controller.rollForward(*step(Direction.DOWN), *rushRoll(1.d6), 1.d6, DiceRollResults(1.d6, 1.d6))
        assertEquals(PlayerDogoutState.RESERVE, mover.state)
        assertNull(state.activePlayer)
        assertContextsCleared()
    }

    @Test
    fun followUpTrapdoorIsResolvedOnceBeforeReturning() {
        state.homeTeam.activePrayersToNuffle.add(PrayerToNuffleTableResult2025.TREACHEROUS_TRAPDOOR)
        state.pitch[15, 10].hasTrapdoor = true
        start()
        controller.rollForward(*step())
        pushBlock()
        controller.rollForward(1.d6, DiceRollResults(1.d6, 1.d6))
        assertEquals(PlayerDogoutState.RESERVE, mover.state)
        assertNull(state.activePlayer)
        assertContextsCleared()
    }

    @Test
    fun tournamentSkillCombinationsCanBeAddedOrLoaded() {
        mover.addSkill(SkillType.FRENZY)
        target.addSkill(SkillType.FRENZY)
        target.addSkill(SkillType.BALL_AND_CHAIN)
        assertTrue(mover.skills.any { it.type == SkillType.FRENZY })
        assertTrue(target.skills.any { it.type == SkillType.BALL_AND_CHAIN })

        val team = teamBuilder(rules, homeTeam.roster) {
            addPlayer(
                PlayerId("custom-fanatic"), "Custom Fanatic", PlayerNo(1),
                HUMAN_LINEMAN.copy(skills = listOf(SkillType.BALL_AND_CHAIN.id(), SkillType.FRENZY.id())),
                skills = listOf(SkillType.LEAP.id()),
            )
        }
        assertEquals(setOf(SkillType.BALL_AND_CHAIN, SkillType.FRENZY, SkillType.LEAP), team.first().skills.map { it.type }.toSet())
    }

    @Test
    fun nestedBlockCanBeUndoneAndReplayed() {
        start()
        val before = controller.history.size
        controller.rollForward(*step())
        pushBlock()
        controller.rollForward(EndAction)
        val replay = controller.history.drop(before).map { it.steps.first().action }
        val usedMoves = mover.movesLeft
        repeat(replay.size) { controller.handleAction(Undo) }
        mover.assertCoordinates(16, 10)
        target.assertCoordinates(15, 10)
        replay.forEach { controller.handleAction(it) }
        mover.assertCoordinates(15, 10)
        target.assertCoordinates(14, 10)
        assertEquals(usedMoves, mover.movesLeft)
        assertContextsCleared()
    }


    @Test
    fun endingBeforeCommittingMovementPreservesAvailability() {
        start()
        controller.rollForward(EndAction)
        assertEquals(Availability.AVAILABLE, mover.available)
        assertFalse((mover.getSkill(SkillType.BALL_AND_CHAIN) as BallAndChain).isSpecialActionUsed)
        assertContextsCleared()
    }

    @Test
    fun internalBlockCannotBeReplacedAndDoesNotEnableBrawler() {
        mover.addSkill(SkillType.STAB)
        mover.addSkill(SkillType.CHAINSAW)
        mover.addSkill(SkillType.BRAWLER)
        start()
        controller.rollForward(*step())
        assertFalse(controller.getAvailableActions().contains<SelectBlockType>())
        controller.rollForward(2.dblock)
        val rerolls = controller.getAvailableActions().get<SelectRerollOption>().options
        assertTrue(rerolls.none { it.getRerollSource(state) is Brawler })
    }

    @Test
    fun failedRushIntoEndZoneCannotScoreBeforeFalling() {
        movePlayerTo(mover, PitchCoordinate(1, 10))
        giveBallToPlayer(mover)
        start()
        mover.movesLeft = 0
        controller.rollForward(*step(), *rushRoll(1.d6))
        assertEquals(0, state.awayTouchdowns)
        controller.rollForward(DiceRollResults(1.d6, 1.d6), Cancel, 5.d8)
        assertEquals(0, state.awayTouchdowns)
        assertContextsCleared()
    }

    @Test
    fun failedRushFollowUpIntoEndZoneCannotScoreBeforeFalling() {
        movePlayerTo(mover, PitchCoordinate(1, 10))
        movePlayerTo(target, PitchCoordinate(0, 10))
        giveBallToPlayer(mover)
        start()
        mover.movesLeft = 0
        controller.rollForward(*step(), *rushRoll(1.d6))
        pushBlock()
        controller.rollForward(DiceRollResults(1.d6, 1.d6)) // Defender's crowd injury
        assertEquals(0, state.awayTouchdowns)
        mover.assertCoordinates(0, 10)
        controller.rollForward(DiceRollResults(1.d6, 1.d6), Cancel, 5.d8)
        assertEquals(0, state.awayTouchdowns)
        assertContextsCleared()
    }


    @Test
    fun opponentOnlyArmourSkillsDoNotApplyAgainstTeamMates() {
        val teamMate = awayTeam["A2".playerId]
        movePlayerTo(target, PitchCoordinate(10, 10))
        movePlayerTo(teamMate, PitchCoordinate(15, 10))
        mover.addSkill(SkillType.CLAWS)
        mover.addSkill(SkillType.MIGHTY_BLOW)
        start()
        controller.rollForward(*step(), 6.dblock, NoRerollSelected(), SelectSingleBlockDieResult(), DirectionSelected(Direction.LEFT),
            DiceRollResults(4.d6, 4.d6))
        assertEquals(PlayerPitchState.PRONE, teamMate.state)
        assertContextsCleared()
    }


    @Test
    fun defenderRemovedThroughTrapdoorDoesNotAlsoSufferBlockInjury() {
        state.homeTeam.activePrayersToNuffle.add(PrayerToNuffleTableResult2025.TREACHEROUS_TRAPDOOR)
        state.pitch[14, 10].hasTrapdoor = true
        start()
        controller.rollForward(*step(), 6.dblock, NoRerollSelected(), SelectSingleBlockDieResult(), DirectionSelected(Direction.LEFT),
            1.d6, DiceRollResults(1.d6, 1.d6), EndAction)
        assertEquals(PlayerDogoutState.RESERVE, target.state)
        mover.assertCoordinates(15, 10)
        assertContextsCleared()
    }

}
