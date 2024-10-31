package com.jervisffb.test

import com.jervisffb.engine.GameController
import com.jervisffb.engine.actions.Cancel
import com.jervisffb.engine.actions.CoinSideSelected
import com.jervisffb.engine.actions.CoinTossResult
import com.jervisffb.engine.actions.D8Result
import com.jervisffb.engine.actions.DiceRollResults
import com.jervisffb.engine.actions.EndSetup
import com.jervisffb.engine.actions.EndTurn
import com.jervisffb.engine.actions.FieldSquareSelected
import com.jervisffb.engine.actions.GameAction
import com.jervisffb.engine.actions.MoveType
import com.jervisffb.engine.actions.MoveTypeSelected
import com.jervisffb.engine.actions.PlayerActionSelected
import com.jervisffb.engine.actions.PlayerSelected
import com.jervisffb.engine.actions.RerollOptionSelected
import com.jervisffb.engine.actions.SelectRerollOption
import com.jervisffb.engine.ext.d3
import com.jervisffb.engine.ext.d6
import com.jervisffb.engine.ext.d8
import com.jervisffb.engine.model.Coin
import com.jervisffb.engine.model.Game
import com.jervisffb.engine.model.PlayerId
import com.jervisffb.engine.model.PlayerNo
import com.jervisffb.engine.model.Team
import com.jervisffb.engine.model.locations.FieldCoordinate
import com.jervisffb.engine.rules.BB2020Rules
import com.jervisffb.engine.rules.PlayerStandardActionType
import com.jervisffb.engine.rules.StandardBB2020Rules
import com.jervisffb.engine.rules.bb2020.procedures.FullGame
import com.jervisffb.engine.rules.bb2020.skills.BreakTackle
import com.jervisffb.engine.utils.createDefaultGameState
import kotlin.test.BeforeTest

/**
 * Abstract class for tests that involving testing the flow of
 * events during a real game.
 *
 * This class makes it easier to setup and manipulate the
 */
abstract class JervisGameTest {

    open val rules: BB2020Rules = StandardBB2020Rules
    protected lateinit var state: Game
    protected lateinit var controller: GameController
    protected lateinit var homeTeam: Team
    protected lateinit var awayTeam: Team

    @BeforeTest
    open fun setUp() {
        state = createDefaultGameState(rules).apply {
            // Should be on LoS
            homeTeam[PlayerNo(1)].apply {
                addSkill(BreakTackle())
                baseStrenght = 4
            }
            // Should be on LoS
            homeTeam[PlayerNo(2)].apply {
                addSkill(BreakTackle())
                baseStrenght = 5
            }
        }
        homeTeam = state.homeTeam
        awayTeam = state.awayTeam
        controller = GameController(rules, state)
        controller.startTestMode(FullGame)
    }

    protected fun useTeamReroll(controller: GameController) =
        RerollOptionSelected(
            controller.getAvailableActions().actions.filterIsInstance<SelectRerollOption>().first().option
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
    val homeTeam = listOf(
        "H1" to FieldCoordinate(12, 5),
        "H2" to FieldCoordinate(12, 6),
        "H3" to FieldCoordinate(12, 7),
        "H4" to FieldCoordinate(12, 8),
        "H5" to FieldCoordinate(12, 9),
        "H6" to FieldCoordinate(11, 1),
        "H7" to FieldCoordinate(10, 1),
        "H8" to FieldCoordinate(10, 13),
        "H9" to FieldCoordinate(11, 13),
        "H10" to FieldCoordinate(9, 7),
        "H11" to FieldCoordinate(3, 7),
    ).flatMap {
        val playerId = PlayerId(it.first)
        listOf(PlayerSelected(playerId), FieldSquareSelected(it.second))
    }.toTypedArray()

    val awayTeam = listOf(
        "A1" to FieldCoordinate(13, 5),
        "A2" to FieldCoordinate(13, 6),
        "A3" to FieldCoordinate(13, 7),
        "A4" to FieldCoordinate(13, 8),
        "A5" to FieldCoordinate(13, 9),
        "A6" to FieldCoordinate(14, 1),
        "A7" to FieldCoordinate(15, 1),
        "A8" to FieldCoordinate(15, 13),
        "A9" to FieldCoordinate(14, 13),
        "A10" to FieldCoordinate(16, 7),
        "A11" to FieldCoordinate(22, 7),
    ).flatMap {
        val playerId = PlayerId(it.first)
        listOf(PlayerSelected(playerId), FieldSquareSelected(it.second))
    }.toTypedArray()

    return if (homeFirst) {
        arrayOf(
            *homeTeam,
            EndSetup,
            *awayTeam,
            EndSetup,
        )
    } else {
        arrayOf(
            *awayTeam,
            EndSetup,
            *homeTeam,
            EndSetup,
        )
    }

}

fun defaultKickOffEvent(): Array<GameAction> = arrayOf(
    DiceRollResults(3.d6, 4.d6), // Roll on kick-off table, does nothing for now
    1.d6, // Brilliant coaching
    1.d6 // Brilliant coaching
)

fun defaultKickOffHomeTeam(
    placeKick: FieldSquareSelected = FieldSquareSelected(19, 7), // Center of Away Half,
    deviate: DiceRollResults = DiceRollResults(4.d8, 1.d6), // Land on [18,7]
    kickoffEvent: Array<GameAction> = defaultKickOffEvent(),
    bounce: D8Result? = 4.d8 // Bounce to [17,7]
) = arrayOf(
    PlayerSelected(PlayerId("H8")), // Select Kicker
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

fun skipTurns(count: Int) = Array(count) { EndTurn }
