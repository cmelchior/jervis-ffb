package com.jervisffb.ui.game.state

import com.jervisffb.engine.ActionRequest
import com.jervisffb.engine.GameEngineController
import com.jervisffb.engine.GameSettings
import com.jervisffb.engine.actions.DiceRollResults
import com.jervisffb.engine.actions.DirectionSelected
import com.jervisffb.engine.actions.EndAction
import com.jervisffb.engine.actions.NoRerollSelected
import com.jervisffb.engine.actions.SelectDirection
import com.jervisffb.engine.bb2025.StandardBB2025Rules
import com.jervisffb.engine.bb2025.procedures.rerolls.StandardTeamReroll
import com.jervisffb.engine.common.procedures.FullGame
import com.jervisffb.engine.ext.d3
import com.jervisffb.engine.ext.dblock
import com.jervisffb.engine.ext.playerId
import com.jervisffb.engine.fsm.ActionNode
import com.jervisffb.engine.model.Direction
import com.jervisffb.engine.model.locations.PitchCoordinate
import com.jervisffb.engine.rules.builder.DiceRollOwner
import com.jervisffb.engine.rules.common.actions.PlayerSpecialActionType
import com.jervisffb.engine.rules.common.skills.SkillType
import com.jervisffb.test.activatePlayer
import com.jervisffb.test.bb2025.createDefaultGameStateBB2025
import com.jervisffb.test.defaultBB2025Pregame
import com.jervisffb.test.defaultKickOffHomeTeam
import com.jervisffb.test.defaultSetup
import com.jervisffb.test.ext.rollForward
import com.jervisffb.test.movePlayerTo
import com.jervisffb.test.utils.SelectSingleBlockDieResult
import com.jervisffb.test.utils.TeamRerollSelected
import com.jervisffb.ui.game.UiGameClientType
import com.jervisffb.ui.game.UiGameController
import com.jervisffb.ui.game.UiGameSnapshot
import com.jervisffb.ui.game.UiGameStatusUpdate
import com.jervisffb.ui.game.UiSnapshotAccumulator
import com.jervisffb.ui.game.UiTeamInfoUpdate
import com.jervisffb.ui.game.animations.AnimationFactory
import com.jervisffb.ui.game.animations.DirectionSelectedAnimation
import com.jervisffb.ui.game.model.UiPitchSquare
import com.jervisffb.ui.game.state.actionwheel.BallAndChainDirectionWheelController
import com.jervisffb.ui.game.state.decorators.SelectDirectionDecorator
import com.jervisffb.ui.game.state.indicators.BallAndChainDirectionArrowIndicator
import com.jervisffb.ui.game.viewmodel.MenuViewModel
import com.jervisffb.ui.menu.TeamActionMode
import com.jervisffb.ui.menu.p2p.P2PClientNetworkAdapter
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.persistentMapOf
import kotlinx.collections.immutable.toPersistentMap
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertIs
import kotlin.test.assertNull
import kotlin.test.assertTrue

/** Exercise UI snapshots against real Ball & Chain procedure nodes without starting a window. */
class BallAndChainUiTests {
    private class Fixture(val mode: TeamActionMode = TeamActionMode.ALL_TEAMS, serverDice: Boolean = false) : AutoCloseable {
        val rules = StandardBB2025Rules().update {
            diceRollsOwner = when (serverDice) {
                true -> DiceRollOwner.ROLL_ON_SERVER
                false -> DiceRollOwner.ROLL_ON_CLIENT
            }
        }
        val engine = GameEngineController(createDefaultGameStateBB2025(rules)).also {
            it.startTestMode(FullGame)
            it.rollForward(*defaultBB2025Pregame(), *defaultSetup(), *defaultKickOffHomeTeam())
        }
        val game = engine.state
        val mover = game.awayTeam["A1".playerId]
        private val menu = MenuViewModel()
        private val settings = GameSettings(rules)

        private fun manualProvider() = object : ManualActionProvider(engine, menu, mode, settings) {
            // These tests advance the engine explicitly, including dice and re-rolls.
            override suspend fun prepareForNextAction(controller: GameEngineController, actions: ActionRequest) = Unit
            override fun hasQueuedActions(): Boolean = false
        }

        private val homeProvider = when (mode) {
            TeamActionMode.AWAY_TEAM -> RemoteActionProvider(mode, engine)
            else -> manualProvider()
        }
        private val awayProvider = when (mode) {
            TeamActionMode.HOME_TEAM -> RemoteActionProvider(mode, engine)
            else -> manualProvider()
        }
        val provider = when (mode) {
            TeamActionMode.ALL_TEAMS -> LocalActionProvider(engine, settings, homeProvider, awayProvider)
            else -> P2PActionProvider(engine, settings, homeProvider, awayProvider, P2PClientNetworkAdapter())
        }
        val ui = UiGameController(UiGameClientType.HOTSEAT, mode, engine, provider, menu, emptyList())

        init {
            mover.addSkill(SkillType.BALL_AND_CHAIN)
            movePlayerTo(mover, PitchCoordinate(16, 10))
            engine.rollForward(*activatePlayer(mover, PlayerSpecialActionType.BALL_AND_CHAIN))
        }

        suspend fun accumulator(): UiSnapshotAccumulator {
            provider.prepareForNextAction(engine, engine.getAvailableActions())
            val previous = UiGameSnapshot(
                actionOwner = engine.getAvailableActions().team, delta = null, game = game,
                squares = game.pitch.associate { it.coordinates to UiPitchSquare(it.coordinates) }.toPersistentMap(),
                players = persistentMapOf(), freeBalls = emptyMap(),
                status = UiGameStatusUpdate(engine.getAvailableActions().team?.id, game),
                gameStatusText = null, unknownActions = persistentListOf(),
                homeDogoutLooksSelectable = false, homeDogoutOnClickAction = null,
                awayDogoutLooksSelectable = false, awayDogoutOnClickAction = null,
                dialogInput = null, movesUsed = persistentListOf(), weather = game.weather,
                homeTeamInfo = UiTeamInfoUpdate(game.homeTeam), awayTeamInfo = UiTeamInfoUpdate(game.awayTeam),
                movePlan = null, showReferee = false, refereeCoordinates = null,
            )
            return UiSnapshotAccumulator(MutableSharedFlow(), MutableSharedFlow(), MutableSharedFlow(), previous, ui)
        }

        suspend fun message(): String? {
            val acc = accumulator()
            ui.gameStatusMessageFactory.applyMessage(provider, acc)
            return acc.build().gameStatusText
        }

        suspend fun selectedArrows(): Map<PitchCoordinate, Direction?> {
            val acc = accumulator()
            BallAndChainDirectionArrowIndicator.decorate(engine.currentNode() as ActionNode, game, engine.getAvailableActions(), acc)
            return acc.build().squares.filterValues { it.directionSelected != null }.mapValues { it.value.directionSelected }
        }

        override fun close() = ui.stopGameEventLoop()
    }

    @Test
    fun hotseatShowsDirectionAndRerollPromptsAndRegistersTheDiceWheel() = runTest {
        Fixture().use { fixture ->
            assertEquals("Select Ball & Chain Direction or End Action", fixture.message())
            assertTrue(fixture.selectedArrows().isEmpty())
            fixture.engine.rollForward(DirectionSelected(Direction.RIGHT))
            assertEquals("Roll D3 for Ball & Chain Direction", fixture.message())
            assertTrue(fixture.selectedArrows().isEmpty())
            assertTrue(fixture.ui.actionWheelControllers.contains(BallAndChainDirectionWheelController))
            assertTrue(fixture.engine.currentNode() in BallAndChainDirectionWheelController.nodes)
            val rollAnimation = assertIs<DirectionSelectedAnimation>(AnimationFactory.getPostActionAnimation(fixture.ui, fixture.game, 1.d3))
            assertEquals(PitchCoordinate(17, 9), rollAnimation.coordinate)
            assertEquals(Direction.UP_RIGHT, rollAnimation.direction)
            assertNull(rollAnimation.previousCoordinate)
            fixture.engine.rollForward(1.d3)
            assertEquals("Accept Ball & Chain Direction Result or Re-roll D3?", fixture.message())
            assertEquals(mapOf(PitchCoordinate(17, 9) to Direction.UP_RIGHT), fixture.selectedArrows())
            fixture.engine.rollForward(TeamRerollSelected<StandardTeamReroll>())
            assertEquals("Re-roll D3 for Ball & Chain Direction", fixture.message())
            assertEquals(mapOf(PitchCoordinate(17, 9) to Direction.UP_RIGHT), fixture.selectedArrows())
            val rerollAnimation = assertIs<DirectionSelectedAnimation>(AnimationFactory.getPostActionAnimation(fixture.ui, fixture.game, DiceRollResults(2.d3)))
            assertEquals(PitchCoordinate(17, 10), rerollAnimation.coordinate)
            assertEquals(Direction.RIGHT, rerollAnimation.direction)
            assertEquals(PitchCoordinate(17, 9), rerollAnimation.previousCoordinate)
            fixture.engine.rollForward(2.d3, EndAction)
            assertEquals(PitchCoordinate(17, 10), fixture.mover.coordinates)
            assertTrue(fixture.selectedArrows().isEmpty())
        }
    }

    @Test
    fun p2pOtherCoachSeesWaitingMessagesForEachDirectionDecision() = runTest {
        Fixture(TeamActionMode.HOME_TEAM).use { fixture ->
            assertTrue(fixture.message()!!.startsWith("Waiting for Opponent to select"))
            fixture.engine.rollForward(DirectionSelected(Direction.RIGHT))
            assertEquals("Waiting for Opponent to Roll D3 for Ball & Chain Direction", fixture.message())
            fixture.engine.rollForward(1.d3)
            assertEquals("Waiting for Opponent to Accept or Re-roll Ball & Chain Direction", fixture.message())
            fixture.engine.rollForward(TeamRerollSelected<StandardTeamReroll>())
            assertEquals("Waiting for Opponent to Re-roll D3 for Ball & Chain Direction", fixture.message())
        }
    }

    @Test
    fun serverDiceShowServerWaitingMessageOnBothClients() = runTest {
        listOf(TeamActionMode.AWAY_TEAM, TeamActionMode.HOME_TEAM).forEach { mode ->
            Fixture(mode, serverDice = true).use { fixture ->
                fixture.engine.rollForward(DirectionSelected(Direction.RIGHT))
                assertEquals("Waiting for Server to Roll D3 for Ball & Chain Direction", fixture.message())
            }
        }
    }

    @Test
    fun teamMateBlockResultPromptsBelongToActiveCoachOnBothClients() = runTest {
        listOf(TeamActionMode.AWAY_TEAM, TeamActionMode.HOME_TEAM).forEach { mode ->
            Fixture(mode).use { fixture ->
                val teamMate = fixture.game.awayTeam["A2".playerId]
                movePlayerTo(teamMate, PitchCoordinate(15, 10))
                teamMate.baseStrength = 6
                teamMate.strength = 6
                fixture.engine.rollForward(DirectionSelected(Direction.LEFT), 2.d3, NoRerollSelected(),
                    com.jervisffb.engine.actions.DiceRollResults(3.dblock, 3.dblock), NoRerollSelected())
                assertEquals(when (mode) {
                    TeamActionMode.AWAY_TEAM -> "Select Block Result"
                    else -> "Waiting for opponent to Select Block Result"
                }, fixture.message())
                fixture.engine.rollForward(SelectSingleBlockDieResult(), DirectionSelected(Direction.LEFT), EndAction)
            }
        }
    }

    @Test
    fun edgeSelectorKeepsCrowdOrientationAvailableWithoutAutomaticallyChoosingIt() = runTest {
        Fixture().use { fixture ->
            movePlayerTo(fixture.mover, PitchCoordinate(16, 0))
            val acc = fixture.accumulator()
            SelectDirectionDecorator.decorate(fixture.provider, fixture.game,
                fixture.engine.getAvailableActions().get<SelectDirection>(), fixture.game.awayTeam, true, acc)
            val snapshot = acc.build()
            assertTrue(snapshot.unknownActions.isEmpty())
            assertEquals(3, snapshot.squares.values.count { it.selectableDirection != null })
            assertEquals(1, snapshot.squares[fixture.mover.coordinates]!!.contextMenuOptions.size)
            assertFalse(snapshot.squares[fixture.mover.coordinates]!!.contextMenuOptions.isEmpty())
        }
    }

    @Test
    fun rolledCrowdDirectionShowsSelectedArrowAtTheOrigin() = runTest {
        Fixture().use { fixture ->
            val origin = PitchCoordinate(16, 0)
            movePlayerTo(fixture.mover, origin)
            fixture.engine.rollForward(DirectionSelected(Direction.UP))
            val animation = assertIs<DirectionSelectedAnimation>(AnimationFactory.getPostActionAnimation(fixture.ui, fixture.game, 2.d3))
            assertEquals(origin, animation.coordinate)
            assertEquals(Direction.UP, animation.direction)
            fixture.engine.rollForward(2.d3)
            assertEquals(mapOf(origin to Direction.UP), fixture.selectedArrows())
        }
    }
}
