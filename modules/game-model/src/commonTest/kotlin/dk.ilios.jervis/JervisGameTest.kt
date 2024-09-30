package dk.ilios.jervis

import dk.ilios.jervis.actions.Cancel
import dk.ilios.jervis.actions.CoinSideSelected
import dk.ilios.jervis.actions.CoinTossResult
import dk.ilios.jervis.actions.D8Result
import dk.ilios.jervis.actions.DiceRollResults
import dk.ilios.jervis.actions.EndSetup
import dk.ilios.jervis.actions.EndTurn
import dk.ilios.jervis.actions.FieldSquareSelected
import dk.ilios.jervis.actions.GameAction
import dk.ilios.jervis.actions.MoveType
import dk.ilios.jervis.actions.MoveTypeSelected
import dk.ilios.jervis.actions.PlayerActionSelected
import dk.ilios.jervis.actions.PlayerSelected
import dk.ilios.jervis.actions.RerollOptionSelected
import dk.ilios.jervis.actions.SelectRerollOption
import dk.ilios.jervis.controller.GameController
import dk.ilios.jervis.ext.d3
import dk.ilios.jervis.ext.d6
import dk.ilios.jervis.ext.d8
import dk.ilios.jervis.model.Coin
import dk.ilios.jervis.model.Game
import dk.ilios.jervis.model.PlayerId
import dk.ilios.jervis.model.PlayerNo
import dk.ilios.jervis.model.Team
import dk.ilios.jervis.model.locations.FieldCoordinate
import dk.ilios.jervis.rules.BB2020Rules
import dk.ilios.jervis.rules.PlayerStandardActionType
import dk.ilios.jervis.rules.StandardBB2020Rules
import dk.ilios.jervis.rules.skills.BreakTackle
import dk.ilios.jervis.utils.createDefaultGameState
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
        "H7" to FieldCoordinate(11, 2),
        "H8" to FieldCoordinate(11, 12),
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
        "A7" to FieldCoordinate(14, 2),
        "A8" to FieldCoordinate(14, 12),
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


