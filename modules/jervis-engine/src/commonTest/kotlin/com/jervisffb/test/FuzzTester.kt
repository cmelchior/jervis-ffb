package com.jervisffb.test

import co.touchlab.kermit.Severity
import com.jervisffb.engine.GameEngineController
import com.jervisffb.engine.actions.CompositeGameAction
import com.jervisffb.engine.actions.EndSetup
import com.jervisffb.engine.actions.FieldSquareSelected
import com.jervisffb.engine.actions.GameAction
import com.jervisffb.engine.actions.PlayerSelected
import com.jervisffb.engine.model.Coach
import com.jervisffb.engine.model.CoachId
import com.jervisffb.engine.model.Game
import com.jervisffb.engine.model.PlayerId
import com.jervisffb.engine.model.PlayerNo
import com.jervisffb.engine.model.PlayerSize
import com.jervisffb.engine.model.PlayerState
import com.jervisffb.engine.model.PositionId
import com.jervisffb.engine.model.RosterId
import com.jervisffb.engine.model.Team
import com.jervisffb.engine.model.context.getContext
import com.jervisffb.engine.model.locations.DogOut
import com.jervisffb.engine.model.locations.FieldCoordinate
import com.jervisffb.engine.rules.BB72020Rules
import com.jervisffb.engine.rules.Rules
import com.jervisffb.engine.rules.StandardBB2020Rules
import com.jervisffb.engine.rules.StandardBB2025Rules
import com.jervisffb.engine.rules.builder.GameType
import com.jervisffb.engine.rules.builder.UndoActionBehavior
import com.jervisffb.engine.rules.common.procedures.SetupTeam
import com.jervisffb.engine.rules.common.procedures.SetupTeamContext
import com.jervisffb.engine.rules.common.roster.Roster
import com.jervisffb.engine.rules.common.roster.RosterPosition
import com.jervisffb.engine.rules.common.skills.SkillType
import com.jervisffb.engine.serialize.RosterLogo
import com.jervisffb.engine.teamBuilder
import com.jervisffb.engine.utils.createRandomAction
import com.jervisffb.test.bb2020.createDefaultGameStateBB2020
import com.jervisffb.test.bb2025.createDefaultGameStateBB2025
import com.jervisffb.utils.DEFAULT_LOG_LEVEL
import com.jervisffb.utils.multiThreadDispatcher
import com.jervisffb.utils.runBlocking
import kotlinx.coroutines.launch
import kotlin.random.Random
import kotlin.test.Ignore
import kotlin.test.Test
import kotlin.test.fail

/**
 * This class can be used to fuzz-test the rule engine by running a lot of
 * games with random actions and pre-defined seeds; that makes it possible to
 * test random paths through the engine as well as being able to reproduce
 * any crashes.
 *
 * For now, this class has to be run manually.
 *
 * Some notes about performance:
 * - The log-level can have a huge impact on performance when running these
 *   tests. It is recommended to set it to `Assert` in [com.jervisffb.utils.DEFAULT_LOG_LEVEL]
 * - Disabling checking the validity can also help. See [GameEngineController] constructor.
 * - The tests are highly parallizable, but memory can be an issue
 **/
@Ignore // Comment out to run
class FuzzTester {

    @Test
    fun runRandomBB2020Games() {
        runFuzzTest(games = 100_000, batchSize = 5_000) { _: Int, seed: Long ->
            val random = Random(seed)
            val rules = StandardBB2020Rules().update {
                undoActionBehavior = UndoActionBehavior.ALLOWED
            }
            val state = createDefaultGameStateBB2020(rules)
            val controller = GameEngineController(state)
            controller.startManualMode(logAvailableActions = false)
            while (controller.stack.isNotEmpty()) {
                val userAction = getSetupAction(controller) ?: createRandomAction(controller, random, canUndo = true)
                controller.handleAction(userAction)
            }
        }
    }

    @Test
    fun runRandomBB2025Games() {
        if (DEFAULT_LOG_LEVEL != Severity.Assert) {
            error("Disabling logs is recommended for performance reasons when running Fuzz Tests. Either disable this comment or change the log-level")
        }
        runFuzzTest(games = 100_000, batchSize = 5_000) { _, seed->
            val random = Random(seed)
            val rules = StandardBB2025Rules().update {
                undoActionBehavior = UndoActionBehavior.ALLOWED
            }
            val homeTeam = createRandomTeamBB2025(rules, random, "H")
            val awayTeam = createRandomTeamBB2025(rules, random, "A")
            val state = createDefaultGameStateBB2025(rules, homeTeam, awayTeam)
            val controller = GameEngineController(state, validateActions = false)
            controller.startManualMode(logAvailableActions = false)
            while (controller.stack.isNotEmpty()) {
                val userAction = getSetupAction(controller) ?: createRandomAction(controller, random, canUndo = true)
                controller.handleAction(userAction)
            }
            // Check that all procedures cleaned up after themselves
            // Disabled while this change is being implemented
            if (!state.contexts.isEmpty()) {
                error("Some procedure contexts are still present after finishing a game")
            }
        }
    }

    @Test
    fun runRandomBB7Games() {
        runFuzzTest(games = 1000, batchSize = 100) { gameNo, seed ->
            val random = Random(seed)
            val rules = BB72020Rules().toBuilder().run {
                undoActionBehavior = UndoActionBehavior.ALLOWED
                build()
            }
            val state = createDefaultGameStateBB2020(rules)
            val controller = GameEngineController(state)
            controller.startManualMode(logAvailableActions = false)
            while (controller.stack.isNotEmpty()) {
                val userAction = getSetupAction(controller) ?: createRandomAction(controller, random, canUndo = true)
                controller.handleAction(userAction)
            }
        }
    }

    private fun runFuzzTest(games: Int = 100_000, batchSize: Int = 5_000, testFunc: (gameNo: Int, randomSeed: Long) -> Unit) {
        val dispatcher = multiThreadDispatcher("fuzztester", 8)
        runBlocking {
            (0 until games step batchSize).forEach { startIndex ->
                launch(dispatcher) {
                    val endIndex = (startIndex + batchSize).coerceAtMost(games)
                    for (gameNo in startIndex until endIndex) {
                        val seed = Random.nextLong()
                        try {
                            testFunc(gameNo, seed)
                        } catch (e: Exception) {
                            fail("Game $gameNo (seed: $seed) crashed with exception:\n${e.stackTraceToString()}")
                        }
                    }
                }
            }
        }
    }

    private fun getSetupAction(controller: GameEngineController): GameAction?  {
        val state = controller.state
        val stack = controller.state.stack
        return if (!stack.isEmpty() && stack.currentNode() == SetupTeam.SelectPlayerOrEndSetup) {
            val context = state.getContext<SetupTeamContext>()
            val compositeActions = mutableListOf<GameAction>()
            if (context.team.isHomeTeam()) {
                handleManualHomeKickingSetup(controller, compositeActions)
            } else {
                handleManualAwayKickingSetup(controller, compositeActions)
            }
            compositeActions.add(EndSetup)
            CompositeGameAction(compositeActions)
        } else {
            null
        }
    }

    private fun createRandomTeamBB2025(rules: Rules, random: Random, prefix: String): Team {
        val playerCount = random.nextInt(9, 17) // 9-16 inclusive
        val allSkills = SkillType.entries.toList()
        val roster = Roster(
            id = RosterId("random-team-$prefix"),
            name = "Random Team $prefix",
            tier = 1,
            numberOfRerolls = 8,
            rerollCost = 50_000,
            allowApothecary = true,
            leagues = emptyList(),
            specialRules = emptyList(),
            positions = emptyList(),
            logo = RosterLogo.NONE
        )
        return teamBuilder(rules, roster) {
            coach = Coach(CoachId("$prefix-coach"), "${prefix}Coach")
            name = "${prefix}Team"
            repeat(playerCount) { index ->
                val playerNo = index + 1
                val mv = random.nextInt(1, 10)   // MA: 1-9
                val st = random.nextInt(1, 9)    // ST: 1-8
                val ag = random.nextInt(1, 7)    // AG: 1-6
                val pa: Int? = if (random.nextBoolean()) random.nextInt(1, 7) else null  // PA: 1-6 or null
                val av = random.nextInt(3, 12)   // AV: 3-11
                val skillCount = random.nextInt(0, 7) // up to 6 skills
                val skills = allSkills.shuffled(random).take(skillCount).map { it.id() }
                val position = RosterPosition(
                    id = PositionId("$prefix-player-$playerNo"),
                    quantity = 1,
                    title = "Player $playerNo",
                    titleSingular = "Player $playerNo",
                    shortHand = "P",
                    cost = 0,
                    move = mv,
                    strength = st,
                    agility = ag,
                    passing = pa,
                    armorValue = av,
                    skills = skills,
                    primary = emptyList(),
                    secondary = emptyList(),
                    specialRules = emptyList(),
                    keywords = emptyList(),
                    size = PlayerSize.STANDARD,
                    icon = null,
                    portrait = null
                )
                addPlayer(PlayerId("$prefix$playerNo"), "Player-$playerNo-$prefix", PlayerNo(playerNo), position)
            }
        }
    }

    private fun handleManualHomeKickingSetup(
        controller: GameEngineController,
        compositeActions: MutableList<GameAction>
    ) {
        val game: Game = controller.state
        val team = game.homeTeam

        val setup = when (game.rules.gameType) {
            GameType.STANDARD -> listOf(
                FieldCoordinate(12, 6),
                FieldCoordinate(12, 7),
                FieldCoordinate(12, 8),
                FieldCoordinate(10, 1),
                FieldCoordinate(10, 4),
                FieldCoordinate(10, 10),
                FieldCoordinate(10, 13),
                FieldCoordinate(8, 1),
                FieldCoordinate(8, 4),
                FieldCoordinate(8, 10),
                FieldCoordinate(8, 13),
            )
            GameType.BB7 -> listOf(
                FieldCoordinate(6, 2),
                FieldCoordinate(6, 5),
                FieldCoordinate(6, 8),
                FieldCoordinate(5, 1),
                FieldCoordinate(5, 4),
                FieldCoordinate(5, 6),
                FieldCoordinate(5, 9),
            )
            else -> TODO("Game type not supported yet: ${game.rules.gameType}")
        }
        setupTeam(team, compositeActions, setup)
    }

    private fun handleManualAwayKickingSetup(
        controller: GameEngineController,
        compositeActions: MutableList<GameAction>
    ) {
        val game: Game = controller.state
        val team = game.awayTeam

        val setup = when (game.rules.gameType) {
            GameType.STANDARD -> listOf(
                FieldCoordinate(13, 6),
                FieldCoordinate(13, 7),
                FieldCoordinate(13, 8),
                FieldCoordinate(15, 1),
                FieldCoordinate(15, 4),
                FieldCoordinate(15, 10),
                FieldCoordinate(15, 13),
                FieldCoordinate(17, 1),
                FieldCoordinate(17, 4),
                FieldCoordinate(17, 10),
                FieldCoordinate(17, 13),
            )
            GameType.BB7 -> listOf(
                FieldCoordinate(13, 2),
                FieldCoordinate(13, 5),
                FieldCoordinate(13, 8),
                FieldCoordinate(14, 1),
                FieldCoordinate(14, 4),
                FieldCoordinate(14, 6),
                FieldCoordinate(14, 9),
            )
            else -> TODO("Game type not supported yet: ${game.rules.gameType}")
        }

        setupTeam(team, compositeActions, setup)
    }

    private fun setupTeam(team: Team, compositeActions: MutableList<GameAction>, setup: List<FieldCoordinate>) {
        val playersTaken = mutableSetOf<PlayerId>()

        setup.forEach { fieldCoordinate: FieldCoordinate ->
            team.firstOrNull {
                val inReserve = (it.location == DogOut && it.state == PlayerState.RESERVE)
                inReserve && !playersTaken.contains(it.id)
            }?.let { selectedPlayer ->
                playersTaken.add(selectedPlayer.id)
                compositeActions.add(PlayerSelected(team[selectedPlayer.number]))
                compositeActions.add(FieldSquareSelected(fieldCoordinate))
            }
        }
    }
}
