package com.jervisffb.fuzzer.cli

import com.jervisffb.engine.GameEngineController
import com.jervisffb.engine.actions.CompositeGameAction
import com.jervisffb.engine.actions.EndSetup
import com.jervisffb.engine.actions.GameAction
import com.jervisffb.engine.actions.PitchSquareSelected
import com.jervisffb.engine.actions.PlayerSelected
import com.jervisffb.engine.bb2020.BB72020Rules
import com.jervisffb.engine.bb2020.StandardBB2020Rules
import com.jervisffb.engine.bb2025.StandardBB2025Rules
import com.jervisffb.engine.common.context.SetupTeamContext
import com.jervisffb.engine.common.procedures.SetupTeam
import com.jervisffb.engine.model.Coach
import com.jervisffb.engine.model.CoachId
import com.jervisffb.engine.model.Game
import com.jervisffb.engine.model.PlayerDogoutState
import com.jervisffb.engine.model.PlayerId
import com.jervisffb.engine.model.PlayerKeyword
import com.jervisffb.engine.model.PlayerNo
import com.jervisffb.engine.model.PlayerSize
import com.jervisffb.engine.model.PositionId
import com.jervisffb.engine.model.RosterId
import com.jervisffb.engine.model.Team
import com.jervisffb.engine.model.context.getContext
import com.jervisffb.engine.model.locations.Dogout
import com.jervisffb.engine.model.locations.PitchCoordinate
import com.jervisffb.engine.rules.Rules
import com.jervisffb.engine.rules.builder.GameType
import com.jervisffb.engine.rules.builder.UndoActionBehavior
import com.jervisffb.engine.rules.common.roster.Roster
import com.jervisffb.engine.rules.common.roster.RosterPosition
import com.jervisffb.engine.rules.common.skills.SkillType
import com.jervisffb.engine.sprites.RosterLogo
import com.jervisffb.engine.statistics.GameStatistics
import com.jervisffb.engine.statistics.probability.scorer.PhysicalActionPathScorer
import com.jervisffb.engine.statistics.probability.scorer.ProbabilityScoreResult
import com.jervisffb.engine.teamBuilder
import com.jervisffb.engine.utils.createRandomAction
import com.jervisffb.test.bb2020.createDefaultGameStateBB2020
import com.jervisffb.test.bb2025.createDefaultGameStateBB2025
import com.jervisffb.utils.multiThreadDispatcher
import com.jervisffb.utils.runBlocking
import kotlinx.coroutines.launch
import java.util.concurrent.atomic.AtomicInteger
import kotlin.collections.forEach
import kotlin.random.Random
import kotlin.random.nextInt
import kotlin.time.Clock

fun runFuzzTest(
    games: Int,
    batchSize: Int,
    masterSeed: Long?,
    threads: Int,
    failureCount: AtomicInteger,
    testFunc: (gameNo: Int, randomSeed: Long) -> Unit,
) {
    val dispatcher = multiThreadDispatcher("fuzztester", threads)
    runBlocking {
        (0 until games step batchSize).forEach { startIndex ->
            launch(dispatcher) {
                val endIndex = (startIndex + batchSize).coerceAtMost(games)
                val start = Clock.System.now()
                for (gameNo in startIndex until endIndex) {
                    val seed = masterSeed ?: Random.nextLong()
                    try {
                        testFunc(gameNo, seed)
                    } catch (e: Exception) {
                        failureCount.incrementAndGet()
                        System.err.println("Game $gameNo (seed: $seed) crashed with exception:\n${e.stackTraceToString()}")
                    }
                }
                val end = Clock.System.now()
                val duration = end - start
                val gamesInBatch = endIndex - startIndex
                println("Batch #${startIndex / batchSize} finished in ${duration.inWholeMilliseconds}ms, avg game time: ${duration.inWholeMilliseconds / gamesInBatch.toFloat()}ms")
            }
        }
    }
}

fun runBB2020Standard(seed: Long, enableStatistics: Boolean) {
    val random = Random(seed)
    val rules = StandardBB2020Rules().update {
        undoActionBehavior = UndoActionBehavior.ALLOWED
    }
    val state = createDefaultGameStateBB2020(rules)
    val stats = when (enableStatistics) {
        true -> GameStatistics()
        false -> null
    }
    val controller = GameEngineController(state, validateActions = false, statistics = stats)
    controller.startManualMode(logAvailableActions = false)
    while (controller.stack.isNotEmpty()) {
        val userAction = getSetupAction(controller) ?: createRandomAction(controller, random, canUndo = true)
        controller.handleAction(userAction)
    }
}

fun runBB2025Standard(seed: Long, enableStatistics: Boolean) {
    val random = Random(seed)
    val rules = StandardBB2025Rules().update {
        undoActionBehavior = UndoActionBehavior.ALLOWED
    }
    val homeTeam = createRandomTeamBB2025(rules, random, "H")
    val awayTeam = createRandomTeamBB2025(rules, random, "A")
    val state = createDefaultGameStateBB2025(rules, homeTeam, awayTeam)
    val stats = when (enableStatistics) {
        true -> GameStatistics()
        false -> null
    }
    val controller = GameEngineController(state, validateActions = false, statistics = stats)
    controller.startManualMode(logAvailableActions = false)
    while (controller.stack.isNotEmpty()) {
        val userAction = getSetupAction(controller) ?: createRandomAction(controller, random, canUndo = true)
        controller.handleAction(userAction)
    }

    // If statistics are enabled. Check that we can calculate a scor
    if (stats != null) {
        val probabilities = stats.diceProbabilities
        val score = PhysicalActionPathScorer.score(probabilities.observations, state.homeTeam.id, state.rules.allowMultipleTeamRerollsPrTurn)
        if (score !is ProbabilityScoreResult.Scored) error("Could not calculate score for game")
    }

    if (!state.contexts.isEmpty()) {
        error("Some procedure contexts are still present after finishing a game")
    }
}

@Suppress("UNUSED_PARAMETER")
fun runBB2020BB7(gameNo: Int, seed: Long, enableStatistics: Boolean) {
    val random = Random(seed)
    val rules = BB72020Rules().toBuilder().run {
        undoActionBehavior = UndoActionBehavior.ALLOWED
        build()
    }
    val state = createDefaultGameStateBB2020(rules)
    val stats = when (enableStatistics) {
        true -> GameStatistics()
        false -> null
    }
    val controller = GameEngineController(state, validateActions = false, statistics = stats)
    controller.startManualMode(logAvailableActions = false)
    while (controller.stack.isNotEmpty()) {
        val userAction = getSetupAction(controller) ?: createRandomAction(controller, random, canUndo = true)
        controller.handleAction(userAction)
    }
}

private fun getSetupAction(controller: GameEngineController): GameAction? {
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
    val playerCount = random.nextInt(9, 17)
    val allSkills = SkillType.entries.toList()
    val allKeywords = PlayerKeyword.entries.toList()
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
        logo = RosterLogo.NONE,
    )
    return teamBuilder(rules, roster) {
        coach = Coach(CoachId("$prefix-coach"), "${prefix}Coach")
        name = "${prefix}Team"
        currentTeamValue = 1_000_000 + random.nextInt(500_000)
        repeat(playerCount) { index ->
            val playerNo = index + 1
            val mv = random.nextInt(rules.moveRange)
            val st = random.nextInt(rules.strengthRange)
            val ag = random.nextInt(rules.agilityRange)
            val pa: Int? = if (random.nextBoolean()) random.nextInt(rules.passingRange) else null
            val av = random.nextInt(rules.armorValueRange)
            val skillCount = random.nextInt(0, 7)
            val skills = allSkills.shuffled(random).take(skillCount).map { it.id() }
            val keywordCount = random.nextInt(0, 3)
            val keywords = allKeywords.shuffled(random).take(keywordCount)
            val playerSize = if (random.nextInt(10) > 7) PlayerSize.BIG_GUY else PlayerSize.STANDARD
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
                keywords = keywords,
                size = playerSize,
                icon = null,
                portrait = null,
            )
            addPlayer(PlayerId("$prefix$playerNo"), "Player-$playerNo-$prefix", PlayerNo(playerNo), position)
        }
    }
}

private fun handleManualHomeKickingSetup(
    controller: GameEngineController,
    compositeActions: MutableList<GameAction>,
) {
    val game: Game = controller.state
    val team = game.homeTeam

    val setup = when (game.rules.gameType) {
        GameType.STANDARD -> listOf(
            PitchCoordinate(12, 6),
            PitchCoordinate(12, 7),
            PitchCoordinate(12, 8),
            PitchCoordinate(10, 1),
            PitchCoordinate(10, 4),
            PitchCoordinate(10, 10),
            PitchCoordinate(10, 13),
            PitchCoordinate(8, 1),
            PitchCoordinate(8, 4),
            PitchCoordinate(8, 10),
            PitchCoordinate(8, 13),
        )
        GameType.BB7 -> listOf(
            PitchCoordinate(6, 2),
            PitchCoordinate(6, 5),
            PitchCoordinate(6, 8),
            PitchCoordinate(5, 1),
            PitchCoordinate(5, 4),
            PitchCoordinate(5, 6),
            PitchCoordinate(5, 9),
        )
        else -> TODO("Game type not supported yet: ${game.rules.gameType}")
    }
    setupTeam(team, compositeActions, setup)
}

private fun handleManualAwayKickingSetup(
    controller: GameEngineController,
    compositeActions: MutableList<GameAction>,
) {
    val game: Game = controller.state
    val team = game.awayTeam

    val setup = when (game.rules.gameType) {
        GameType.STANDARD -> listOf(
            PitchCoordinate(13, 6),
            PitchCoordinate(13, 7),
            PitchCoordinate(13, 8),
            PitchCoordinate(15, 1),
            PitchCoordinate(15, 4),
            PitchCoordinate(15, 10),
            PitchCoordinate(15, 13),
            PitchCoordinate(17, 1),
            PitchCoordinate(17, 4),
            PitchCoordinate(17, 10),
            PitchCoordinate(17, 13),
        )
        GameType.BB7 -> listOf(
            PitchCoordinate(13, 2),
            PitchCoordinate(13, 5),
            PitchCoordinate(13, 8),
            PitchCoordinate(14, 1),
            PitchCoordinate(14, 4),
            PitchCoordinate(14, 6),
            PitchCoordinate(14, 9),
        )
        else -> TODO("Game type not supported yet: ${game.rules.gameType}")
    }
    setupTeam(team, compositeActions, setup)
}

private fun setupTeam(team: Team, compositeActions: MutableList<GameAction>, setup: List<PitchCoordinate>) {
    val playersTaken = mutableSetOf<PlayerId>()
    setup.forEach { pitchCoordinate: PitchCoordinate ->
        team.firstOrNull {
            val inReserve = (it.location == Dogout && it.state == PlayerDogoutState.RESERVE)
            inReserve && !playersTaken.contains(it.id)
        }?.let { selectedPlayer ->
            playersTaken.add(selectedPlayer.id)
            compositeActions.add(PlayerSelected(team[selectedPlayer.number]))
            compositeActions.add(PitchSquareSelected(pitchCoordinate))
        }
    }
}
