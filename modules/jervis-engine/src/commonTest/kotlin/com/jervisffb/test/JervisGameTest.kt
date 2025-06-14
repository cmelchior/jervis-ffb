package com.jervisffb.test

import com.jervisffb.engine.GameEngineController
import com.jervisffb.engine.actions.CalculatedAction
import com.jervisffb.engine.actions.Cancel
import com.jervisffb.engine.actions.CoinSideSelected
import com.jervisffb.engine.actions.CoinTossResult
import com.jervisffb.engine.actions.CompositeGameAction
import com.jervisffb.engine.actions.D6Result
import com.jervisffb.engine.actions.D8Result
import com.jervisffb.engine.actions.DiceRollResults
import com.jervisffb.engine.actions.EndSetup
import com.jervisffb.engine.actions.EndTurn
import com.jervisffb.engine.actions.FieldSquareSelected
import com.jervisffb.engine.actions.GameAction
import com.jervisffb.engine.actions.MoveType
import com.jervisffb.engine.actions.MoveTypeSelected
import com.jervisffb.engine.actions.NoRerollSelected
import com.jervisffb.engine.actions.PlayerActionSelected
import com.jervisffb.engine.actions.PlayerSelected
import com.jervisffb.engine.actions.RerollOptionSelected
import com.jervisffb.engine.actions.SelectRerollOption
import com.jervisffb.engine.ext.d3
import com.jervisffb.engine.ext.d6
import com.jervisffb.engine.ext.d8
import com.jervisffb.engine.ext.playerId
import com.jervisffb.engine.model.Coin
import com.jervisffb.engine.model.Game
import com.jervisffb.engine.model.PlayerId
import com.jervisffb.engine.model.PlayerNo
import com.jervisffb.engine.model.Team
import com.jervisffb.engine.model.locations.FieldCoordinate
import com.jervisffb.engine.rules.PlayerStandardActionType
import com.jervisffb.engine.rules.Rules
import com.jervisffb.engine.rules.StandardBB2020Rules
import com.jervisffb.engine.rules.bb2020.procedures.FullGame
import com.jervisffb.engine.rules.bb2020.skills.SkillType
import com.jervisffb.engine.rules.builder.UndoActionBehavior
import com.jervisffb.test.ext.rollForward
import kotlin.collections.flatMap
import kotlin.test.BeforeTest

/**
 * Abstract class for tests that involving testing the flow of
 * events during a real game.
 *
 * This class makes it easier to setup and manipulate the
 */
abstract class JervisGameTest {

    open val rules: Rules = StandardBB2020Rules().toBuilder().run {
        undoActionBehavior = UndoActionBehavior.ALLOWED
        build()
    }
    protected lateinit var state: Game
    protected lateinit var controller: GameEngineController
    protected lateinit var homeTeam: Team
    protected lateinit var awayTeam: Team

    @BeforeTest
    open fun setUp() {
        homeTeam = createDefaultHomeTeam(rules)
        awayTeam = humanTeamAway(rules)
        state = createDefaultGameState(rules, homeTeam, awayTeam).apply {
            // Should be on LoS
            homeTeam[PlayerNo(1)].apply {
                addSkill(SkillType.BREAK_TACKLE.id())
                strength = 4
            }
            // Should be on LoS
            homeTeam[PlayerNo(2)].apply {
                addSkill(SkillType.BREAK_TACKLE.id())
                strength = 5
            }
        }
        homeTeam = state.homeTeam
        awayTeam = state.awayTeam
        controller = GameEngineController(state)
        controller.startTestMode(FullGame)
    }

    fun startDefaultGame() {
        controller.rollForward(
            *defaultPregame(),
            *defaultSetup(),
            *defaultKickOffHomeTeam(),
        )
    }

    protected fun useTeamReroll(controller: GameEngineController) =
        RerollOptionSelected(
            controller.getAvailableActions().actions.filterIsInstance<SelectRerollOption>().first().options.first()
        )
}

fun defaultFanFactor() = arrayOf(
    1.d3, // Home Fan Factor Roll
    2.d3, // Away Fan Factor Roll
)

fun defaultWeather() = DiceRollResults(3.d6, 4.d6)

fun defaultJourneyMen() = emptyArray<GameAction>()

fun defaultInducements() = emptyArray<GameAction>()

fun defaultPrayersToNuffle() = emptyArray<GameAction>()

fun defaultDetermineKickingTeam() = arrayOf(
    CoinSideSelected(Coin.HEAD), // Away: Select side
    CoinTossResult(Coin.HEAD), // Home flips coin
    Cancel // Away choices to receive
)

fun defaultPregame(
    fanFactor: Array<out GameAction> = defaultFanFactor(),
    weatherRoll: DiceRollResults = defaultWeather(),
    journeyMen: Array<out GameAction> = defaultJourneyMen(),
    inducements: Array<out GameAction> = defaultInducements(),
    prayersToNuffle: Array<out GameAction> = defaultPrayersToNuffle(),
    determineKickingTeam: Array<out GameAction> = defaultDetermineKickingTeam(),
) = arrayOf(
    *fanFactor,
    weatherRoll,
    *journeyMen,
    *inducements,
    *prayersToNuffle,
    *determineKickingTeam
)

fun defaultSetup(homeFirst: Boolean = true): Array<GameAction> {
    val homeTeam = defaultHomeSetup()
    val awayTeam = defaultAwaySetup()
    return if (homeFirst) {
        arrayOf(*homeTeam, *awayTeam)
    } else {
        arrayOf(*awayTeam, *homeTeam)
    }
}

fun defaultHomeSetup(endSetup: Boolean = true): Array<GameAction> {
    val setup = buildList {
        add("H1".playerId to FieldCoordinate(12, 5))
        add("H2".playerId to FieldCoordinate(12, 6))
        add("H3".playerId to FieldCoordinate(12, 7))
        add("H4".playerId to FieldCoordinate(12, 8))
        add("H5".playerId to FieldCoordinate(12, 9))
        add("H6".playerId to FieldCoordinate(11, 1))
        add("H7".playerId to FieldCoordinate(10, 1))
        add("H8".playerId to FieldCoordinate(10, 13))
        add("H9".playerId to FieldCoordinate(11, 13))
        add("H10".playerId to  FieldCoordinate(9, 7))
        add("H11".playerId to  FieldCoordinate(3, 7))
    }
    return teamSetup(setup, endSetup)
}

fun defaultAwaySetup(endSetup: Boolean = true): Array<GameAction> {
    val setup= listOf(
        "A1".playerId to FieldCoordinate(13, 5),
        "A2".playerId to FieldCoordinate(13, 6),
        "A3".playerId to FieldCoordinate(13, 7),
        "A4".playerId to FieldCoordinate(13, 8),
        "A5".playerId to FieldCoordinate(13, 9),
        "A6".playerId to FieldCoordinate(14, 1),
        "A7".playerId to FieldCoordinate(15, 1),
        "A8".playerId to FieldCoordinate(15, 13),
        "A9".playerId to FieldCoordinate(14, 13),
        "A10".playerId to FieldCoordinate(16, 7),
        "A11".playerId to FieldCoordinate(22, 7),
    )
    return teamSetup(setup, endSetup)
}

fun teamSetup(vararg setup: Pair<PlayerId, FieldCoordinate>): Array<GameAction> {
    return teamSetup(setup.toList())
}

fun teamSetup(setup: List<Pair<PlayerId, FieldCoordinate>>, endSetup: Boolean = true): Array<GameAction> {
    return setup.flatMap {
        val playerId = it.first
        listOf(PlayerSelected(playerId), FieldSquareSelected(it.second))
    }.let { list ->
        (list + if (endSetup) EndSetup else null).filterNotNull().toTypedArray()
    }
}

fun defaultKickOffEvent(): Array<GameAction> = arrayOf(
    DiceRollResults(3.d6, 4.d6), // Roll on kick-off table
    1.d6, // Brilliant coaching
    1.d6 // Brilliant coaching
)

fun defaultKickOffHomeTeam(
    selectKicker: PlayerSelected = PlayerSelected(PlayerId("H8")), // Select Kicker
    placeKick: FieldSquareSelected = FieldSquareSelected(19, 7), // Center of Away Half,
    deviate: DiceRollResults = DiceRollResults(4.d8, 1.d6), // Land on [18,7]
    kickoffEvent: Array<GameAction> = defaultKickOffEvent(),
    bounce: D8Result? = 4.d8 // Bounce to [17,7]
) = arrayOf(
    selectKicker,
    placeKick,
    deviate,
    *kickoffEvent,
    bounce
)

fun defaultKickOffAwayTeam(
    placeKick: FieldSquareSelected = FieldSquareSelected(6, 7), // Center of Away Half,
    deviate: DiceRollResults = DiceRollResults(4.d8, 1.d6), // Land on [5,7]
    kickoffEvent: Array<GameAction> = defaultKickOffEvent(),
    bounce: D8Result? = 4.d8 // Bounce to [4,7]
) = arrayOf(
    PlayerSelected(PlayerId("A8")), // Select Kicker
    placeKick,
    deviate,
    *kickoffEvent,
    bounce
)

fun activatePlayer(playerId: String, type: PlayerStandardActionType) = arrayOf(
    PlayerSelected(PlayerId(playerId)),
    PlayerActionSelected(type),
)

fun moveTo(x: Int, y: Int) = arrayOf(
    MoveTypeSelected(MoveType.STANDARD),
    FieldSquareSelected(FieldCoordinate(x, y)),
)

fun rushTo(x: Int, y: Int, rushRoll: D6Result = 6.d6) = arrayOf(
    MoveTypeSelected(MoveType.STANDARD),
    FieldSquareSelected(FieldCoordinate(x, y)),
    rushRoll,
    NoRerollSelected()
)

/**
 * Use the PathFinder to move the active player to the destined location. Only
 * moves that require no rolls are used.
 */
@Suppress("TestFunctionName")
fun SmartMoveTo(x: Int, y: Int): GameAction {
    return CalculatedAction { state, rules ->
        val activePlayer = state.activePlayer
        val pathfinder = rules.pathFinder
        val start = activePlayer!!.coordinates
        val end = FieldCoordinate(x, y)
        val path = pathfinder.calculateShortestPath(state, start, end, activePlayer.movesLeft)
        val lastSquare = path.path.last()
        if (lastSquare != FieldCoordinate(x, y)) {
            throw IllegalArgumentException(
                "Cannot reach destination (${x}, ${y}). Last step was ($lastSquare.x}, ${lastSquare.y})."
            )
        }
        val pathMoves = path.path.flatMap {
            listOf(*moveTo(it.x, it.y))
        }
        CompositeGameAction(pathMoves)
    }
}

fun setupPlayer(id: PlayerId, field: FieldCoordinate) = arrayOf(
    PlayerSelected(id),
    FieldSquareSelected(field),
)

fun skipTurns(count: Int) = Array(count) { EndTurn }
