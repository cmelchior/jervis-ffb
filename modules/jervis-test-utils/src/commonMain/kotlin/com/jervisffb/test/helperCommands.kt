package com.jervisffb.test

import com.jervisffb.engine.actions.BlockTypeSelected
import com.jervisffb.engine.actions.CalculatedAction
import com.jervisffb.engine.actions.Cancel
import com.jervisffb.engine.actions.CoinSideSelected
import com.jervisffb.engine.actions.CoinTossResult
import com.jervisffb.engine.actions.CompositeGameAction
import com.jervisffb.engine.actions.Confirm
import com.jervisffb.engine.actions.D3Result
import com.jervisffb.engine.actions.D6Result
import com.jervisffb.engine.actions.D8Result
import com.jervisffb.engine.actions.DBlockResult
import com.jervisffb.engine.actions.DiceRollResults
import com.jervisffb.engine.actions.EndSetup
import com.jervisffb.engine.actions.EndTurn
import com.jervisffb.engine.actions.GameAction
import com.jervisffb.engine.actions.MoveType
import com.jervisffb.engine.actions.MoveTypeSelected
import com.jervisffb.engine.actions.NoRerollSelected
import com.jervisffb.engine.actions.PitchSquareSelected
import com.jervisffb.engine.actions.PlayerActionSelected
import com.jervisffb.engine.actions.PlayerSelected
import com.jervisffb.engine.commands.SetBallState
import com.jervisffb.engine.commands.SetPlayerLocation
import com.jervisffb.engine.ext.d3
import com.jervisffb.engine.ext.d6
import com.jervisffb.engine.ext.d8
import com.jervisffb.engine.ext.playerId
import com.jervisffb.engine.model.Coin
import com.jervisffb.engine.model.Player
import com.jervisffb.engine.model.PlayerId
import com.jervisffb.engine.model.Team
import com.jervisffb.engine.model.locations.PitchCoordinate
import com.jervisffb.engine.rules.common.actions.ActionType
import com.jervisffb.engine.rules.common.actions.BlockType
import com.jervisffb.test.utils.SelectSingleBlockDieResult
import kotlin.collections.plus

/**
 * This file contains helper command functions that group common patterns,
 * making it easier to write tests that are more readable
 *
 * For now, these needs to be compatible with both BB2020 and BB2025
 */

fun defaultFanFactor() = arrayOf(
    1.d3, // Home Fan Factor Roll
    2.d3, // Away Fan Factor Roll
)

fun defaultWeather() = DiceRollResults(3.d6, 4.d6)

fun defaultJourneyMen() = emptyArray<GameAction>()

fun defaultInducements() = emptyArray<GameAction>() // Assume that the two teams have equal CTV

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
        add("H1".playerId to PitchCoordinate(12, 5))
        add("H2".playerId to PitchCoordinate(12, 6))
        add("H3".playerId to PitchCoordinate(12, 7))
        add("H4".playerId to PitchCoordinate(12, 8))
        add("H5".playerId to PitchCoordinate(12, 9))
        add("H6".playerId to PitchCoordinate(11, 1))
        add("H7".playerId to PitchCoordinate(10, 1))
        add("H8".playerId to PitchCoordinate(10, 13))
        add("H9".playerId to PitchCoordinate(11, 13))
        add("H10".playerId to  PitchCoordinate(9, 7))
        add("H11".playerId to  PitchCoordinate(3, 7))
    }
    return teamSetup(setup, endSetup)
}

fun defaultAwaySetup(endSetup: Boolean = true): Array<GameAction> {
    val setup= listOf(
        "A1".playerId to PitchCoordinate(13, 5),
        "A2".playerId to PitchCoordinate(13, 6),
        "A3".playerId to PitchCoordinate(13, 7),
        "A4".playerId to PitchCoordinate(13, 8),
        "A5".playerId to PitchCoordinate(13, 9),
        "A6".playerId to PitchCoordinate(14, 1),
        "A7".playerId to PitchCoordinate(15, 1),
        "A8".playerId to PitchCoordinate(15, 13),
        "A9".playerId to PitchCoordinate(14, 13),
        "A10".playerId to PitchCoordinate(16, 7),
        "A11".playerId to PitchCoordinate(22, 7),
    )
    return teamSetup(setup, endSetup)
}

fun teamSetup(vararg setup: Pair<PlayerId, PitchCoordinate>): Array<GameAction> {
    return teamSetup(setup.toList())
}

fun teamSetup(setup: List<Pair<PlayerId, PitchCoordinate>>, endSetup: Boolean = true): Array<GameAction> {
    return setup.flatMap {
        val playerId = it.first
        listOf(PlayerSelected(playerId), PitchSquareSelected(it.second))
    }.let { list ->
        (list + if (endSetup) EndSetup else null).filterNotNull().toTypedArray()
    }
}

fun defaultKickOffEvent(): Array<GameAction?> = arrayOf(
    DiceRollResults(3.d6, 4.d6), // Roll on kick-off table
    1.d6, // Brilliant coaching
    1.d6 // Brilliant coaching
)

fun defaultKickOffHomeTeam(
    selectKicker: PlayerSelected? = PlayerSelected(PlayerId("H10")), // Select Kicker
    placeKick: PitchSquareSelected = PitchSquareSelected(19, 7), // Center of Away Half,
    deviate: DiceRollResults = DiceRollResults(4.d8, 1.d6), // Land on [18,7]
    kickoffEvent: Array<GameAction?> = defaultKickOffEvent(),
    bounce: D8Result? = 4.d8 // Bounce to [17,7]
) = arrayOf(
    selectKicker,
    placeKick,
    deviate,
    *kickoffEvent,
    bounce
)

fun defaultKickOffAwayTeam(
    selectKicker: PlayerSelected = PlayerSelected("A10".playerId),
    placeKick: PitchSquareSelected = PitchSquareSelected(6, 7), // Center of Away Half,
    deviate: DiceRollResults = DiceRollResults(4.d8, 1.d6), // Land on [5,7]
    kickoffEvent: Array<GameAction?> = defaultKickOffEvent(),
    bounce: D8Result? = 4.d8 // Bounce to [4,7]
) = arrayOf(
    selectKicker,
    placeKick,
    deviate,
    *kickoffEvent,
    bounce
)

fun activatePlayer(playerId: String, type: ActionType) = arrayOf(
    PlayerSelected(PlayerId(playerId)),
    PlayerActionSelected(type),
)

fun activatePlayer(player: Player, type: ActionType) = arrayOf(
    PlayerSelected(player.id),
    PlayerActionSelected(type),
)

fun moveTo(x: Int, y: Int) = arrayOf(
    MoveTypeSelected(MoveType.STANDARD),
    PitchSquareSelected(PitchCoordinate(x, y)),
)

fun jumpTo(x: Int, y: Int) = arrayOf(
    MoveTypeSelected(MoveType.JUMP),
    PitchSquareSelected(PitchCoordinate(x, y)),
)

fun leapTo(x: Int, y: Int) = arrayOf(
    MoveTypeSelected(MoveType.LEAP),
    PitchSquareSelected(PitchCoordinate(x, y)),
)

fun pogoTo(x: Int, y: Int) = arrayOf(
    MoveTypeSelected(MoveType.POGO),
    PitchSquareSelected(PitchCoordinate(x, y)),
)

fun rushTo(x: Int, y: Int, rushRoll: D6Result = 6.d6) = arrayOf(
    MoveTypeSelected(MoveType.STANDARD),
    PitchSquareSelected(PitchCoordinate(x, y)),
    rushRoll,
    NoRerollSelected()
)

fun shadowPlayer(playerWithShadowing: Player, roll: D6Result = 6.d6) = arrayOf(
    PlayerSelected(playerWithShadowing),
    roll
)

fun tentaclePlayer(playerWithTentacles: Player, roll: D6Result = 6.d6) = arrayOf(
    PlayerSelected(playerWithTentacles),
    roll
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
        val end = PitchCoordinate(x, y)
        val path = pathfinder.calculateShortestPath(state, start, end, activePlayer.movesLeft)
        val lastSquare = path.path.last()
        if (lastSquare != PitchCoordinate(x, y)) {
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

fun setupPlayer(id: PlayerId, square: PitchCoordinate) = arrayOf(
    PlayerSelected(id),
    PitchSquareSelected(square),
)

fun skipTurns(count: Int) = Array(count) { EndTurn }


fun recoverPlayer(id: PlayerId) = arrayOf(
    PlayerSelected(id),
    4.d6,
)

fun recoverPlayers(players: List<Player>, startWith: Team): Array<GameAction> {
    return players
        .filter { it.team == startWith }
        .plus(players.filter { it.team != startWith })
        .flatMap {
            listOf(PlayerSelected(it), 4.d6)
        }
        .toTypedArray()
}

// Do a standard 1 die block with no reroll
fun standardBlock(target: Player, die: DBlockResult) = standardBlock(target.id.value, die)
fun standardBlock(target: String, die: DBlockResult) = arrayOf(
    PlayerSelected(target.playerId),
    die,
    NoRerollSelected(),
    SelectSingleBlockDieResult(),
)

fun blitzBlock(player: Player, die: DBlockResult) = blitzBlock(player.id.value, die)
fun blitzBlock(target: String, die: DBlockResult) = arrayOf(
    PlayerSelected(target.playerId),
    BlockTypeSelected(BlockType.STANDARD),
    die,
    NoRerollSelected(),
    SelectSingleBlockDieResult(),
)

fun accuracy(roll: D6Result = 6.d6, reroll: GameAction? = NoRerollSelected()) = arrayOf(
    roll,
    reroll,
)

fun alwaysHungry(roll: D6Result = 6.d6, reroll: GameAction? = NoRerollSelected()) = arrayOf(
    roll,
    reroll,
)

fun squirmFree(roll: D6Result = 6.d6, reroll: GameAction? = NoRerollSelected()) = arrayOf(
    roll,
    reroll,
)

fun animalSavagery(roll: D6Result = 6.d6, reroll: GameAction? = NoRerollSelected()) = arrayOf(
    roll,
    reroll,
)

fun boneHead(roll: D6Result = 6.d6, reroll: GameAction? = NoRerollSelected()) = arrayOf(
    roll,
    reroll,
)

fun breatheFireRoll(roll: D6Result = 6.d6, reroll: GameAction? = NoRerollSelected()) = arrayOf(
    roll,
    reroll,
)

fun chompRoll(roll: D6Result = 6.d6, reroll: GameAction? = NoRerollSelected()) = arrayOf(
    roll,
    reroll,
)

fun useApothecary(value: Boolean) = when (value) {
    true -> Confirm
    false -> Cancel
}

fun usePlagueDoctor(value: Boolean) = when (value) {
    true -> Confirm
    false -> Cancel
}

fun useMortuaryAssistant(value: Boolean) = when (value) {
    true -> Confirm
    false -> Cancel
}

fun followUp(value: Boolean) = when (value) {
    true -> Confirm
    false -> Cancel
}

fun argueTheCall(value: Boolean) = when (value) {
    true -> Confirm
    false -> Cancel
}

fun useBribe(value: Boolean) = when (value) {
    true -> Confirm
    false -> Cancel
}

fun foulAppearanceRoll(roll: D6Result = 6.d6, reroll: GameAction? = NoRerollSelected()) = arrayOf(
    roll,
    reroll,
)

fun reallyStupid(roll: D6Result = 6.d6, reroll: GameAction? = NoRerollSelected()) = arrayOf(
    roll,
    reroll,
)

fun unchannelledFury(roll: D6Result = 6.d6, reroll: GameAction? = NoRerollSelected()) = arrayOf(
    roll,
    reroll,
)

fun dodge(roll: D6Result = 6.d6, reroll: GameAction? = NoRerollSelected()) = arrayOf(
    roll,
    reroll,
)

fun catch(roll: D6Result = 6.d6, reroll: GameAction? = NoRerollSelected()) = arrayOf(
    roll,
    reroll,
)

fun chainsawRoll(roll: D6Result = 6.d6, reroll: GameAction? = NoRerollSelected()) = arrayOf(
    roll,
    reroll,
)

fun hypnoticGazeRoll(roll: D6Result = 6.d6, reroll: GameAction? = NoRerollSelected()) = arrayOf(
    roll,
    reroll,
)

fun pickup(roll: D6Result = 6.d6, reroll: GameAction? = NoRerollSelected()) = arrayOf(
    roll,
    reroll,
)

fun jump(roll: D6Result = 6.d6, reroll: GameAction? = NoRerollSelected()) = arrayOf(
    roll,
    reroll,
)

fun leap(roll: D6Result = 6.d6, reroll: GameAction? = NoRerollSelected()) = arrayOf(
    roll,
    reroll,
)

fun loner(roll: D6Result = 6.d6, reroll: GameAction? = NoRerollSelected()) = arrayOf(
    roll, reroll
)

fun dauntlessRoll(roll: D6Result = 6.d6, reroll: GameAction? = NoRerollSelected()) = arrayOf(
    roll,
    reroll,
)

fun proRoll(roll: D6Result = 6.d6, reroll: GameAction? = NoRerollSelected()) = arrayOf(
    roll,
    reroll
)

fun qualityRoll(roll: D6Result = 6.d6, reroll: GameAction? = NoRerollSelected()) = arrayOf(
    roll,
    reroll,
)

fun landingRoll(roll: D6Result = 6.d6, reroll: GameAction? = NoRerollSelected()) = arrayOf(
    roll,
    reroll,
)

fun rushRoll(roll: D6Result = 6.d6, reroll: GameAction? = NoRerollSelected()) = arrayOf(
    roll,
    reroll,
)

fun pogoRoll(roll: D6Result = 6.d6, reroll: GameAction? = NoRerollSelected()) = arrayOf(
    roll,
    reroll
)

fun projectileVomitRoll(roll: D6Result = 6.d6, reroll: GameAction? = NoRerollSelected()) = arrayOf(
    roll,
    reroll,
)

fun puntDirection(roll: D3Result, reroll: GameAction? = NoRerollSelected()) = arrayOf(
    roll,
    reroll,
)

fun puntDistance(roll: D6Result, reroll: GameAction? = NoRerollSelected()) = arrayOf(
    roll,
    reroll,
)

fun standingUpRoll(roll: D6Result = 6.d6, reroll: GameAction? = NoRerollSelected()) = arrayOf(
    roll,
    reroll,
)

fun steadyFootingRoll(roll: D6Result = 6.d6, reroll: GameAction? = NoRerollSelected()) = arrayOf(
    roll,
    reroll,
)

fun swoopDirectionRoll(roll: D3Result, reroll: GameAction? = NoRerollSelected()) = arrayOf(
    roll,
    reroll,
)

fun swoopDistanceRoll(roll: D6Result, reroll: GameAction? = NoRerollSelected()) = arrayOf(
    roll,
    reroll,
)

fun takeRoot(roll: D6Result = 6.d6, reroll: GameAction? = NoRerollSelected()) = arrayOf(
    roll,
    reroll,
)

fun teamCaptainRoll(roll: D6Result = 6.d6, reroll: GameAction? = NoRerollSelected()) = arrayOf(
    roll, reroll
)

fun teamMascotRoll(roll: D6Result = 6.d6) = arrayOf(
    roll
)

fun throwBall(roll: D6Result = 6.d6, reroll: GameAction? = NoRerollSelected()) = arrayOf(
    roll,
    reroll
)

fun throwIn(roll: D3Result) = roll

fun bounce(roll: D8Result) = roll

fun movePlayerTo(player: Player, coordinate: PitchCoordinate) {
    SetPlayerLocation(player, coordinate).execute(player.team.game)
}

fun giveBallToPlayer(player: Player) {
    val state = player.team.game
    val ball = state.singleBall()
    SetBallState.carried(ball, player).execute(state)
}

fun regenerationRoll(roll: D6Result = 6.d6) = roll


