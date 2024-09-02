package dk.ilios.jervis

import dk.ilios.jervis.actions.CoinSideSelected
import dk.ilios.jervis.actions.CoinTossResult
import dk.ilios.jervis.actions.Confirm
import dk.ilios.jervis.actions.D8Result
import dk.ilios.jervis.actions.DiceResults
import dk.ilios.jervis.actions.EndSetup
import dk.ilios.jervis.actions.FieldSquareSelected
import dk.ilios.jervis.actions.GameAction
import dk.ilios.jervis.actions.MoveType
import dk.ilios.jervis.actions.MoveTypeSelected
import dk.ilios.jervis.actions.PlayerActionSelected
import dk.ilios.jervis.actions.PlayerSelected
import dk.ilios.jervis.actions.RerollOptionSelected
import dk.ilios.jervis.actions.SelectRerollOption
import dk.ilios.jervis.commands.Command
import dk.ilios.jervis.controller.GameController
import dk.ilios.jervis.ext.d3
import dk.ilios.jervis.ext.d6
import dk.ilios.jervis.ext.d8
import dk.ilios.jervis.model.Coin
import dk.ilios.jervis.model.FieldCoordinate
import dk.ilios.jervis.model.Game
import dk.ilios.jervis.model.PlayerId
import dk.ilios.jervis.model.PlayerNo
import dk.ilios.jervis.model.Team
import dk.ilios.jervis.rules.BB2020Rules
import dk.ilios.jervis.rules.PlayerActionType
import dk.ilios.jervis.rules.skills.BreakTackle
import dk.ilios.jervis.utils.createDefaultGameState
import dk.ilios.jervis.utils.setupTeamsOnField
import kotlin.test.BeforeTest

abstract class GameFlowTests {

    val rules = BB2020Rules
    lateinit var state: Game
    lateinit var controller: GameController
    lateinit var homeTeam: Team
    lateinit var awayTeam: Team

    @BeforeTest
    open fun setUp() {
        state = createDefaultGameState(rules).apply {
            // Should be on LoS
            homeTeam[PlayerNo(1)]!!.apply {
                addSkill(BreakTackle.Factory.createSkill())
                baseStrenght = 4
            }
            // Should be on LoS
            homeTeam[PlayerNo(2)]!!.apply {
                addSkill(BreakTackle.Factory.createSkill())
                baseStrenght = 5
            }
        }
        homeTeam = state.homeTeam
        awayTeam = state.awayTeam
        controller = GameController(rules, state)
        setupTeamsOnField(controller)
    }

    protected fun useTeamReroll(controller: GameController) =
        RerollOptionSelected(
            controller.getAvailableActions().filterIsInstance<SelectRerollOption>().first().option
        )

    protected fun execute(vararg commands: Command) {
        commands.forEach {
            it.execute(state, controller)
        }
    }
}

fun defaultFanFactor() = arrayOf(
    1.d3, // Home Fan Factor Roll
    2.d3, // Away Fan Factor Roll
)

fun defaultWeather() = DiceResults(3.d6, 4.d6)

fun defaultDetermineKickingTeam() = arrayOf(
    CoinSideSelected(Coin.HEAD), // Home: Select side
    CoinTossResult(Coin.HEAD), // Flip coin
    Confirm // Home: Confirm to kick
)

fun defaultPregame(
    fanFactor: Array<out GameAction> = defaultFanFactor(),
    weatherRoll: DiceResults = defaultWeather(),
    journeyMen: Array<out GameAction> = emptyArray(),
    inducements: Array<out GameAction> = emptyArray(),
    prayersToNuffle: Array<out GameAction> = emptyArray(),
    determineKickingTeam: Array<out GameAction> = defaultDetermineKickingTeam(),
) = arrayOf(
    *fanFactor,
    weatherRoll,
    *journeyMen,
    *inducements,
    *prayersToNuffle,
    *determineKickingTeam
)

fun defaultSetup(): Array<GameAction> {
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
    return arrayOf(
        *homeTeam,
        EndSetup,
        *awayTeam,
        EndSetup,
    )
}

fun defaultKickOffHomeTeam(
    deviate: DiceResults = DiceResults(4.d8, 1.d6), // Land on [18,7]
    bounce: D8Result? = 4.d8 // Bounce to [17,7]
) = arrayOf(
    PlayerSelected(PlayerId("H8")), // Select Kicker
    FieldSquareSelected(19, 7), // Center of Away Half,
    deviate,
    DiceResults(3.d6, 4.d6), // Roll on kick-off table, does nothing for now
    bounce
)

fun activatePlayer(playerId: String, type: PlayerActionType) = arrayOf(
    PlayerSelected(PlayerId(playerId)),
    PlayerActionSelected(type),
)

fun moveTo(x: Int, y: Int) = arrayOf(
    MoveTypeSelected(MoveType.STANDARD),
    FieldSquareSelected(FieldCoordinate(x, y)),
)


