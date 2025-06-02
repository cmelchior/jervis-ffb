package com.jervisffb.net.test

import com.jervisffb.engine.GameEngineController
import com.jervisffb.engine.actions.CompositeGameAction
import com.jervisffb.engine.actions.EndSetup
import com.jervisffb.engine.actions.FieldSquareSelected
import com.jervisffb.engine.actions.GameAction
import com.jervisffb.engine.actions.PlayerSelected
import com.jervisffb.engine.model.CoachId
import com.jervisffb.engine.model.CoachType
import com.jervisffb.engine.model.Field
import com.jervisffb.engine.model.Game
import com.jervisffb.engine.model.PlayerNo
import com.jervisffb.engine.model.PlayerState
import com.jervisffb.engine.model.Team
import com.jervisffb.engine.model.context.getContext
import com.jervisffb.engine.model.locations.FieldCoordinate
import com.jervisffb.engine.rules.Rules
import com.jervisffb.engine.rules.StandardBB2020Rules
import com.jervisffb.engine.rules.bb2020.procedures.SetupTeam
import com.jervisffb.engine.rules.bb2020.procedures.SetupTeamContext
import com.jervisffb.engine.rules.builder.DiceRollOwner
import com.jervisffb.engine.rules.builder.GameType
import com.jervisffb.engine.rules.builder.UndoActionBehavior
import com.jervisffb.engine.serialize.SerializedTeam
import com.jervisffb.engine.utils.containsActionWithRandomBehavior
import com.jervisffb.engine.utils.createRandomAction
import com.jervisffb.net.GameId
import com.jervisffb.net.JervisClientWebSocketConnection
import com.jervisffb.net.LightServer
import com.jervisffb.net.gameId
import com.jervisffb.net.messages.AcceptGameMessage
import com.jervisffb.net.messages.CoachJoinedMessage
import com.jervisffb.net.messages.ConfirmGameStartMessage
import com.jervisffb.net.messages.GameActionMessage
import com.jervisffb.net.messages.GameActionSyncMessage
import com.jervisffb.net.messages.GameReadyMessage
import com.jervisffb.net.messages.GameStartedMessage
import com.jervisffb.net.messages.GameStateSyncMessage
import com.jervisffb.net.messages.JoinGameAsCoachMessage
import com.jervisffb.net.messages.P2PTeamInfo
import com.jervisffb.net.messages.TeamJoinedMessage
import com.jervisffb.net.messages.TeamSelectedMessage
import com.jervisffb.net.messages.UpdateClientStateMessage
import com.jervisffb.net.messages.UpdateHostStateMessage
import com.jervisffb.test.createDefaultHomeTeam
import com.jervisffb.test.lizardMenAwayTeam
import com.jervisffb.utils.runBlocking
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.withTimeoutOrNull
import kotlin.random.Random
import kotlin.test.Ignore
import kotlin.test.Test
import kotlin.test.fail
import kotlin.time.Duration.Companion.seconds

/**
 * This class can be used to fuzz-test a P2P game. It is mostly relevant for
 * finding race conditions and errors in the serialization.
 *
 * For now, this class has to be run manually.
 *
 * Note: If running many tests, there will be a huge performance increase by
 * raising the log level to `Assert` in [com.jervisffb.utils.jervisLogger]
 **/
@Ignore // Comment out to run
class NetworkFuzzTester {

    @Test
    fun runRandomNetworkGames() {
        val games = 100
        repeat(games) { gameNo ->
            val seed = Random.nextLong()
            try {
                runRandomGame(seed, StandardBB2020Rules())
            } catch (ex: Throwable) {
                fail("Game $gameNo (seed: $seed) crashed with exception:\n${ex.stackTraceToString()}")
            }
        }
    }

    @Test
    fun runRandomBB7NetworkGames() {
        val games = 100
        repeat(games) { gameNo ->
            val seed = Random.nextLong()
            try {
                runRandomGame(seed, com.jervisffb.engine.rules.BB72020Rules())
            } catch (ex: Throwable) {
                fail("Game $gameNo (seed: $seed) crashed with exception:\n${ex.stackTraceToString()}")
            }
        }
    }

    private fun runRandomGame(seed: Long, rules: Rules) = runBlocking {
        val random = Random(seed)
        val rules = rules.toBuilder().run {
            diceRollsOwner = DiceRollOwner.ROLL_ON_SERVER
            undoActionBehavior = UndoActionBehavior.ONLY_NON_RANDOM_ACTIONS
            timers.timersEnabled = false
            build()
        }
        val server = LightServer(
            gameName = "test",
            rules = rules,
            hostCoach = CoachId("HomeCoachID"),
            hostTeam = createDefaultHomeTeam(rules),
            clientCoach = null,
            clientTeam = null,
            testMode = true,
            random = random
        )

        // Start server and connections
        val gameId = "test".gameId
        server.start()

        val conn1 = JervisClientWebSocketConnection(GameId("test"), "ws://localhost:8080/joinGame?id=test", "host")
        conn1.start()
        val conn2 = JervisClientWebSocketConnection(GameId("test"), "ws://localhost:8080/joinGame?id=test", "client")
        conn2.start()

        // Run Join sequence, right up until starting the game
        var hostRules: Rules? = null
        var hostHomeTeam: Team? = null
        var hostAwayTeam: Team? = null

        val join1 = JoinGameAsCoachMessage(
            gameId,
            "host",
            null,
            "host",
            CoachType.HUMAN,
            true,
            P2PTeamInfo(createDefaultHomeTeam(rules))
        )
        conn1.send(join1)
        checkServerMessage<GameStateSyncMessage>(conn1) {
            hostRules = it.rules
        }
        consumeServerMessage<CoachJoinedMessage>(conn1)
        checkServerMessage<TeamJoinedMessage>(conn1) {
            hostHomeTeam = it.getTeam(rules)
        }
        consumeServerMessage<UpdateHostStateMessage>(conn1)

        // Client Joins
        var clientRules: Rules? = null
        var clientHomeTeam: Team? = null
        var clientAwayTeam: Team? = null
        val join2 = JoinGameAsCoachMessage(
            gameId,
            "client",
            null,
            "client",
            CoachType.HUMAN,
            false
        )
        conn2.send(join2)
        consumeServerMessage<CoachJoinedMessage>(conn1)
        checkServerMessage<GameStateSyncMessage>(conn2) {
            val homeTeamCoach = it.coaches.first()
            clientRules = it.rules
            clientHomeTeam = it.homeTeam?.let { teamData -> SerializedTeam.deserialize(clientRules, teamData, homeTeamCoach) }
        }
        consumeServerMessage<CoachJoinedMessage>(conn2)
        consumeServerMessage<UpdateClientStateMessage>(conn2)

        // Client selects team
        conn2.send(TeamSelectedMessage(P2PTeamInfo(lizardMenAwayTeam(rules))))
        checkServerMessage<TeamJoinedMessage>(conn1) {
            hostAwayTeam = it.getTeam(hostRules!!)
        }
        checkServerMessage<TeamJoinedMessage>(conn2) {
            clientAwayTeam = it.getTeam(clientRules!!)
        }

        // Receive request to start game
        consumeServerMessage<ConfirmGameStartMessage>(conn1)
        consumeServerMessage<ConfirmGameStartMessage>(conn2)
        consumeServerMessage<UpdateHostStateMessage>(conn1)
        consumeServerMessage<UpdateClientStateMessage>(conn2)

        // Confirm starting game
        conn1.send(AcceptGameMessage(true))
        conn2.send(AcceptGameMessage(true))

        // Game is starting
        consumeServerMessage<GameReadyMessage>(conn1)
        consumeServerMessage<GameReadyMessage>(conn2)
        consumeServerMessage<UpdateHostStateMessage>(conn1)
        consumeServerMessage<UpdateClientStateMessage>(conn2)

        // Run Game
        val success = withTimeoutOrNull(3000.seconds) {
            val host = async {
                runHost(conn1, gameId, hostRules!!, hostHomeTeam!!, hostAwayTeam!!, random)
            }
            val client = async {
                runClient(conn2, gameId, clientRules!!, clientHomeTeam!!, clientAwayTeam!!, random)
            }
            awaitAll(host, client)
            true
        }
        if (success != true) {
            fail("Game did not finish in time")
        }

        conn1.close()
        conn2.close()
        server.stop(immediately = true)
    }

    private suspend fun runHost(
        conn: JervisClientWebSocketConnection,
        gameId: GameId,
        hostRules: Rules,
        hostHomeTeam: Team,
        hostAwayTeam: Team,
        random: Random,
    ) {
        // Setup Game state and start local game loop
        val game = Game(hostRules, hostHomeTeam, hostAwayTeam, Field.createForRuleset(hostRules))
        val controller = GameEngineController(game)
        controller.startManualMode(logAvailableActions = false)
        conn.send(GameStartedMessage(gameId))

        // Run game
        while (controller.stack.isNotEmpty()) {
            val availableActions = controller.getAvailableActions()
            val isRandom = availableActions.containsActionWithRandomBehavior()
            val hostAction = (availableActions.team == null) || (availableActions.team?.id == hostHomeTeam.id)
            if (hostAction && !isRandom) {
                val userAction = getSetupAction(controller) ?: createRandomAction(game, availableActions.actions, random)
                controller.handleAction(userAction)
                conn.send(GameActionMessage(controller.currentActionIndex(), userAction))
            } else {
                checkServerMessage<GameActionSyncMessage>(conn) {
                    if (it.serverIndex != controller.currentActionIndex() + 1) {
                        fail("[Host] Received server message out of order. Expected ${controller.currentActionIndex() + 1}, got ${it.serverIndex}")
                    }
                    val remoteAction = it.action
                    controller.handleAction(remoteAction)
                }
            }
        }
    }

    private suspend fun runClient(
        conn: JervisClientWebSocketConnection,
        gameId: GameId,
        clientRules: Rules,
        clientHomeTeam: Team,
        clientAwayTeam: Team,
        random: Random,
    ) {
        // Setup Game state and start local game loop
        val game = Game(clientRules, clientHomeTeam, clientAwayTeam, Field.createForRuleset(clientRules))
        val controller = GameEngineController(game)
        controller.startManualMode(logAvailableActions = false)
        conn.send(GameStartedMessage(gameId))

        // Run game loop
        while (controller.stack.isNotEmpty()) {
            val availableActions = controller.getAvailableActions()
            val isRandom = availableActions.containsActionWithRandomBehavior()
            val clientAction = (availableActions.team?.id == clientAwayTeam.id)
            if (clientAction && !isRandom) {
                val userAction = getSetupAction(controller) ?: createRandomAction(game, availableActions.actions, random)
                controller.handleAction(userAction)
                conn.send(GameActionMessage(controller.currentActionIndex(), userAction))
            } else {
                checkServerMessage<GameActionSyncMessage>(conn) {
                    if (it.serverIndex != controller.currentActionIndex() + 1) {
                        fail("[Host] Received server message out of order. Expected ${controller.currentActionIndex() + 1}, got ${it.serverIndex}")
                    }
                    val remoteAction = it.action
                    controller.handleAction(remoteAction)
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
            return CompositeGameAction(compositeActions)
        } else {
            null
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
        val playersTaken = mutableSetOf<PlayerNo>()

        setup.forEach { fieldCoordinate: FieldCoordinate ->
            team.firstOrNull {
                it.state == PlayerState.RESERVE && !playersTaken.contains(it.number)
            }?.let { replacementPlayer ->
                playersTaken.add(replacementPlayer.number)
                compositeActions.add(PlayerSelected(team[replacementPlayer.number]))
                compositeActions.add(FieldSquareSelected(fieldCoordinate))
            }
        }
    }
}
