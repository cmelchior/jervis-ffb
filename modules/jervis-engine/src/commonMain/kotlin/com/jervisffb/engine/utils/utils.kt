package com.jervisffb.engine.utils

import com.jervisffb.engine.GameController
import com.jervisffb.engine.actions.ActionDescriptor
import com.jervisffb.engine.actions.BlockTypeSelected
import com.jervisffb.engine.actions.Cancel
import com.jervisffb.engine.actions.CancelWhenReady
import com.jervisffb.engine.actions.CoinSideSelected
import com.jervisffb.engine.actions.CoinTossResult
import com.jervisffb.engine.actions.Confirm
import com.jervisffb.engine.actions.ConfirmWhenReady
import com.jervisffb.engine.actions.Continue
import com.jervisffb.engine.actions.ContinueWhenReady
import com.jervisffb.engine.actions.D12Result
import com.jervisffb.engine.actions.D16Result
import com.jervisffb.engine.actions.D20Result
import com.jervisffb.engine.actions.D2Result
import com.jervisffb.engine.actions.D3Result
import com.jervisffb.engine.actions.D4Result
import com.jervisffb.engine.actions.D6Result
import com.jervisffb.engine.actions.D8Result
import com.jervisffb.engine.actions.DBlockResult
import com.jervisffb.engine.actions.DeselectPlayer
import com.jervisffb.engine.actions.Dice
import com.jervisffb.engine.actions.DicePoolChoice
import com.jervisffb.engine.actions.DicePoolResultsSelected
import com.jervisffb.engine.actions.DiceRollResults
import com.jervisffb.engine.actions.DieResult
import com.jervisffb.engine.actions.DirectionSelected
import com.jervisffb.engine.actions.DogoutSelected
import com.jervisffb.engine.actions.EndAction
import com.jervisffb.engine.actions.EndActionWhenReady
import com.jervisffb.engine.actions.EndSetup
import com.jervisffb.engine.actions.EndSetupWhenReady
import com.jervisffb.engine.actions.EndTurn
import com.jervisffb.engine.actions.EndTurnWhenReady
import com.jervisffb.engine.actions.FieldSquareSelected
import com.jervisffb.engine.actions.GameAction
import com.jervisffb.engine.actions.InducementSelected
import com.jervisffb.engine.actions.MoveTypeSelected
import com.jervisffb.engine.actions.NoRerollSelected
import com.jervisffb.engine.actions.PlayerActionSelected
import com.jervisffb.engine.actions.PlayerDeselected
import com.jervisffb.engine.actions.PlayerSelected
import com.jervisffb.engine.actions.RandomPlayersSelected
import com.jervisffb.engine.actions.RerollOptionSelected
import com.jervisffb.engine.actions.RollDice
import com.jervisffb.engine.actions.SelectBlockType
import com.jervisffb.engine.actions.SelectCoinSide
import com.jervisffb.engine.actions.SelectDicePoolResult
import com.jervisffb.engine.actions.SelectDirection
import com.jervisffb.engine.actions.SelectDogout
import com.jervisffb.engine.actions.SelectFieldLocation
import com.jervisffb.engine.actions.SelectInducement
import com.jervisffb.engine.actions.SelectMoveType
import com.jervisffb.engine.actions.SelectNoReroll
import com.jervisffb.engine.actions.SelectPlayer
import com.jervisffb.engine.actions.SelectPlayerAction
import com.jervisffb.engine.actions.SelectRandomPlayers
import com.jervisffb.engine.actions.SelectRerollOption
import com.jervisffb.engine.actions.SelectSkill
import com.jervisffb.engine.actions.SkillSelected
import com.jervisffb.engine.actions.TossCoin
import com.jervisffb.engine.commands.ResetAvailableTeamRerolls
import com.jervisffb.engine.commands.SetPlayerLocation
import com.jervisffb.engine.commands.SetPlayerState
import com.jervisffb.engine.model.Coach
import com.jervisffb.engine.model.CoachId
import com.jervisffb.engine.model.Coin
import com.jervisffb.engine.model.Field
import com.jervisffb.engine.model.Game
import com.jervisffb.engine.model.Player
import com.jervisffb.engine.model.PlayerId
import com.jervisffb.engine.model.PlayerNo
import com.jervisffb.engine.model.PlayerState
import com.jervisffb.engine.model.Team
import com.jervisffb.engine.model.locations.FieldCoordinate
import com.jervisffb.engine.model.modifiers.DiceModifier
import com.jervisffb.engine.model.modifiers.StatModifier
import com.jervisffb.engine.rules.BB2020Rules
import com.jervisffb.engine.rules.Rules
import com.jervisffb.engine.rules.StandardBB2020Rules
import com.jervisffb.engine.rules.bb2020.procedures.D6DieRoll
import com.jervisffb.engine.rules.bb2020.roster.HumanTeam
import com.jervisffb.engine.rules.bb2020.roster.LizardmenTeam
import com.jervisffb.engine.rules.bb2020.skills.DiceRerollOption
import com.jervisffb.engine.rules.bb2020.skills.DiceRollType
import com.jervisffb.engine.rules.bb2020.skills.Frenzy
import com.jervisffb.engine.rules.bb2020.skills.RerollSource
import com.jervisffb.engine.rules.bb2020.skills.SideStep
import com.jervisffb.engine.rules.bb2020.skills.Skill
import com.jervisffb.engine.teamBuilder
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlin.jvm.JvmName
import kotlin.random.Random

fun humanTeamAway(): Team {
    return teamBuilder(StandardBB2020Rules, HumanTeam) {
        coach = Coach(CoachId("away-coach"), "AwayCoach")
        name = "AwayTeam"
        addPlayer(PlayerId("A1"), "Lineman-1-A", PlayerNo(1), HumanTeam.LINEMAN)
        addPlayer(PlayerId("A2"), "Lineman-2-A", PlayerNo(2), HumanTeam.LINEMAN)
        addPlayer(PlayerId("A3"), "Lineman-3-A", PlayerNo(3), HumanTeam.LINEMAN)
        addPlayer(PlayerId("A4"), "Lineman-4-A", PlayerNo(4), HumanTeam.LINEMAN)
        addPlayer(PlayerId("A5"), "Thrower-5-A", PlayerNo(5), HumanTeam.THROWER)
        addPlayer(PlayerId("A6"), "Catcher-6-A", PlayerNo(6), HumanTeam.CATCHER)
        addPlayer(PlayerId("A7"), "Catcher-7-A", PlayerNo(7), HumanTeam.CATCHER)
        addPlayer(PlayerId("A8"), "Blitzer-8-A", PlayerNo(8), HumanTeam.BLITZER)
        addPlayer(PlayerId("A9"), "Blitzer-9-A", PlayerNo(9), HumanTeam.BLITZER)
        addPlayer(PlayerId("A10"), "Blitzer-10-A", PlayerNo(10), HumanTeam.BLITZER)
        addPlayer(PlayerId("A11"), "Blitzer-11-A", PlayerNo(11), HumanTeam.BLITZER)
        addPlayer(PlayerId("A12"), "Lineman-12-A", PlayerNo(12), HumanTeam.LINEMAN)
        reRolls = 4
        apothecaries = 1
        dedicatedFans = 2
        teamValue = 1_000_000
    }
}

fun lizardMenAwayTeam(): Team {
    return teamBuilder(StandardBB2020Rules, LizardmenTeam) {
        coach = Coach(CoachId("away-coach"), "AwayCoach")
        name = "AwayTeam"
        addPlayer(PlayerId("A1"), "Kroxigor-1-A", PlayerNo(1), LizardmenTeam.KROXIGOR)
        addPlayer(PlayerId("A2"), "Saurus-2-A", PlayerNo(2), LizardmenTeam.SAURUS_BLOCKERS)
        addPlayer(PlayerId("A3"), "Saurus-3-A", PlayerNo(3), LizardmenTeam.SAURUS_BLOCKERS)
        addPlayer(PlayerId("A4"), "Saurus-4-A", PlayerNo(4), LizardmenTeam.SAURUS_BLOCKERS)
        addPlayer(PlayerId("A5"), "Saurus-5-A", PlayerNo(5), LizardmenTeam.SAURUS_BLOCKERS)
        addPlayer(PlayerId("A6"), "Saurus-6-A", PlayerNo(6), LizardmenTeam.SAURUS_BLOCKERS, listOf(Frenzy()))
        addPlayer(PlayerId("A7"), "Saurus-7-A", PlayerNo(7), LizardmenTeam.SAURUS_BLOCKERS, listOf(Frenzy()))
        addPlayer(PlayerId("A8"), "ChameleonSkink-8-A", PlayerNo(8), LizardmenTeam.CHAMELEON_SKINKS)
        addPlayer(PlayerId("A9"), "Skink-9-A", PlayerNo(9), LizardmenTeam.SKINK_RUNNER_LINEMEN)
        addPlayer(PlayerId("A10"), "Skink-10-A", PlayerNo(10), LizardmenTeam.SKINK_RUNNER_LINEMEN)
        addPlayer(PlayerId("A11"), "Skink-11-A", PlayerNo(11), LizardmenTeam.SKINK_RUNNER_LINEMEN)
        reRolls = 4
        apothecaries = 1
        teamValue = 1_000_000
    }
}

fun createRandomAction(
    state: Game,
    availableActions: List<ActionDescriptor>,
): GameAction {

    // Select a random action but disallow certain ones
    var actionDesc: ActionDescriptor? = null
    val filtered = availableActions.filter { it != EndActionWhenReady }
    if (filtered.isEmpty()) {
        actionDesc = availableActions.random()
    } else {
        actionDesc = filtered.random()
    }

    return when (val action = actionDesc) {
        ContinueWhenReady -> Continue
        EndTurnWhenReady -> EndTurn
        is RollDice -> {
            val results =
                action.dice.map {
                    when (it) {
                        Dice.D2 -> D2Result()
                        Dice.D3 -> D3Result()
                        Dice.D4 -> D4Result()
                        Dice.D6 -> D6Result()
                        Dice.D8 -> D8Result()
                        Dice.D12 -> D12Result()
                        Dice.D16 -> D16Result()
                        Dice.D20 -> D20Result()
                        Dice.BLOCK -> DBlockResult()
                    }
                }
            return DiceRollResults(results)
        }
        ConfirmWhenReady -> Confirm
        EndSetupWhenReady -> EndSetup
        SelectDogout -> DogoutSelected
        is SelectFieldLocation -> FieldSquareSelected(action.x, action.y)
        is SelectPlayer -> PlayerSelected(action.player)
        is DeselectPlayer -> PlayerDeselected(action.player)
        is SelectPlayerAction -> PlayerActionSelected(action.action.type)
        EndActionWhenReady -> EndAction
        CancelWhenReady -> Cancel
        SelectCoinSide -> {
            when (Random.nextInt(2)) {
                0 -> CoinSideSelected(Coin.HEAD)
                1 -> CoinSideSelected(Coin.TAIL)
                else -> throw IllegalStateException()
            }
        }
        TossCoin -> {
            when (Random.nextInt(2)) {
                0 -> CoinTossResult(Coin.HEAD)
                1 -> CoinTossResult(Coin.TAIL)
                else -> throw IllegalStateException()
            }
        }

        is SelectRandomPlayers ->
            RandomPlayersSelected(action.players.shuffled().subList(0, action.count))

        is SelectNoReroll -> NoRerollSelected(action.dicePoolId)
        is SelectRerollOption -> RerollOptionSelected(action.option)
        is SelectDicePoolResult -> {
            DicePoolResultsSelected(action.pools.map { pool ->
                DicePoolChoice(pool.id, pool.dice.shuffled().subList(0, pool.selectDice).map { it.result })
            })
        }
        is SelectMoveType -> MoveTypeSelected(action.type)
        is SelectSkill -> SkillSelected(action.skill)
        is SelectInducement -> InducementSelected(action.id)
        is SelectBlockType -> BlockTypeSelected(action.type)
        is SelectDirection -> DirectionSelected(action.directions.random())
        null -> TODO()
    }
}

const val enableAsserts = true

inline fun assert(condition: Boolean) {
    if (enableAsserts && !condition) {
        throw IllegalStateException("A invariant failed")
    }
}

@JvmName("sumOfDieResults")
fun List<DieResult>.sum(): Int = fold(0) { acc, el -> acc + el.value }

fun List<DiceModifier>.sum(): Int = this.sumOf { it.modifier }

@JvmName("sumOfStatModifiers")
fun List<StatModifier>.sum(): Int = this.sumOf { it.modifier }

class InvalidActionException(message: String) : RuntimeException(message)

class InvalidGameStateException(message: String) : IllegalStateException(message)

inline fun INVALID_GAME_STATE(message: String = "Unexpected game state"): Nothing {
    throw InvalidGameStateException(message)
}

inline fun INVALID_ACTION(action: GameAction, customMessage: String? = null): Nothing {
    throw InvalidActionException(customMessage?.let {
        customMessage
    } ?: "Invalid action selected: $action")
}


/**
 * Default setup of two test teams.
 *
 * They will be set up in a mirror way.
 *
 * - 1-5 Are setup in the midle of the LoS
 * - 6-7 are setup next to each other on the right line 1 step away from LoS
 * - 8-9 are setup next to each on the left line 1 step away from LoS
 * - 10-11 are setup in the backfield
 */
fun setupTeamsOnField(controller: GameController) {
    val homeCommands = with(controller.state.homeTeam) {
        listOf(
            SetPlayerLocation(get(PlayerNo(1))!!, FieldCoordinate(12, 5)),
            SetPlayerLocation(get(PlayerNo(2))!!, FieldCoordinate(12, 6)),
            SetPlayerLocation(get(PlayerNo(3))!!, FieldCoordinate(12, 7)),
            SetPlayerLocation(get(PlayerNo(4))!!, FieldCoordinate(12, 8)),
            SetPlayerLocation(get(PlayerNo(5))!!, FieldCoordinate(12, 9)),
            SetPlayerLocation(get(PlayerNo(6))!!, FieldCoordinate(11, 1)),
            SetPlayerLocation(get(PlayerNo(7))!!, FieldCoordinate(11, 2)),
            SetPlayerLocation(get(PlayerNo(8))!!, FieldCoordinate(11, 12)),
            SetPlayerLocation(get(PlayerNo(9))!!, FieldCoordinate(11, 13)),
            SetPlayerLocation(get(PlayerNo(10))!!, FieldCoordinate(9, 7)),
            SetPlayerLocation(get(PlayerNo(11))!!, FieldCoordinate(3, 7))
        )
    }
    val awayCommands = with(controller.state.awayTeam) {
        listOf(
            SetPlayerLocation(get(PlayerNo(1))!!, FieldCoordinate(13, 5)),
            SetPlayerLocation(get(PlayerNo(2))!!, FieldCoordinate(13, 6)),
            SetPlayerLocation(get(PlayerNo(3))!!, FieldCoordinate(13, 7)),
            SetPlayerLocation(get(PlayerNo(4))!!, FieldCoordinate(13, 8)),
            SetPlayerLocation(get(PlayerNo(5))!!, FieldCoordinate(13, 9)),
            SetPlayerLocation(get(PlayerNo(6))!!, FieldCoordinate(14, 1)),
            SetPlayerLocation(get(PlayerNo(7))!!, FieldCoordinate(14, 2)),
            SetPlayerLocation(get(PlayerNo(8))!!, FieldCoordinate(14, 12)),
            SetPlayerLocation(get(PlayerNo(9))!!, FieldCoordinate(14, 13)),
            SetPlayerLocation(get(PlayerNo(10))!!, FieldCoordinate(16, 7)),
            SetPlayerLocation(get(PlayerNo(11))!!, FieldCoordinate(22, 7))
        )
    }

    (homeCommands + awayCommands).forEach { command ->
        command.execute(controller.state)
    }

    // Also enable Team rerolls
    controller.state.activeTeam = controller.state.homeTeam
    ResetAvailableTeamRerolls(controller.state.homeTeam).execute(controller.state)
    ResetAvailableTeamRerolls(controller.state.awayTeam).execute(controller.state)
}

fun createDefaultGameState(rules: BB2020Rules, awayTeam: Team = humanTeamAway()): Game {
    val team1: Team =
        teamBuilder(rules, HumanTeam) {
            coach = Coach(CoachId("home-coach"), "HomeCoach")
            name = "HomeTeam"
            addPlayer(PlayerId("H1"), "Lineman-1-H", PlayerNo(1), HumanTeam.LINEMAN)
            addPlayer(PlayerId("H2"), "Lineman-2-H", PlayerNo(2), HumanTeam.LINEMAN)
            addPlayer(PlayerId("H3"), "Lineman-3-H", PlayerNo(3), HumanTeam.LINEMAN)
            addPlayer(PlayerId("H4"), "Lineman-4-H", PlayerNo(4), HumanTeam.LINEMAN)
            addPlayer(PlayerId("H5"), "Thrower-5-H", PlayerNo(5), HumanTeam.THROWER)
            addPlayer(PlayerId("H6"), "Catcher-6-H", PlayerNo(6), HumanTeam.CATCHER, listOf(SideStep()))
            addPlayer(PlayerId("H7"), "Catcher-7-H", PlayerNo(7), HumanTeam.CATCHER)
            addPlayer(PlayerId("H8"), "Blitzer-8-H", PlayerNo(8), HumanTeam.BLITZER)
            addPlayer(PlayerId("H9"), "Blitzer-9-H", PlayerNo(9), HumanTeam.BLITZER)
            addPlayer(PlayerId("H10"), "Blitzer-10-H", PlayerNo(10), HumanTeam.BLITZER)
            addPlayer(PlayerId("H11"), "Blitzer-11-H", PlayerNo(11), HumanTeam.BLITZER)
            addPlayer(PlayerId("H12"), "Lineman-12-H", PlayerNo(12), HumanTeam.LINEMAN)
            reRolls = 4
            apothecaries = 1
            dedicatedFans = 1
            teamValue = 1_000_000
        }
    val field = Field.createForRuleset(rules)
    return Game(rules, team1, awayTeam, field)
}

/**
 * Move all players onto the field as if starting a game.
 * Only works on the setup defined above
 */
fun createStartingTestSetup(state: Game) {
    fun setupPlayer(
        state: Game,
        player: Player?,
        fieldCoordinate: FieldCoordinate,
    ) {
        player?.let {
            SetPlayerLocation(it, fieldCoordinate).execute(state)
            SetPlayerState(it, PlayerState.STANDING)
        } ?: error("")
    }

    // Home
    with(state.homeTeam) {
        setupPlayer(state, this[PlayerNo(1)], FieldCoordinate(12, 6))
        setupPlayer(state, this[PlayerNo(2)], FieldCoordinate(12, 7))
        setupPlayer(state, this[PlayerNo(3)], FieldCoordinate(12, 8))
        setupPlayer(state, this[PlayerNo(4)], FieldCoordinate(10, 1))
        setupPlayer(state, this[PlayerNo(5)], FieldCoordinate(10, 4))
        setupPlayer(state, this[PlayerNo(6)], FieldCoordinate(10, 10))
        setupPlayer(state, this[PlayerNo(7)], FieldCoordinate(10, 13))
        setupPlayer(state, this[PlayerNo(8)], FieldCoordinate(8, 1))
        setupPlayer(state, this[PlayerNo(9)], FieldCoordinate(8, 4))
        setupPlayer(state, this[PlayerNo(10)], FieldCoordinate(8, 10))
        setupPlayer(state, this[PlayerNo(11)], FieldCoordinate(8, 13))
    }

    // Away
    with(state.awayTeam) {
        setupPlayer(state, this[PlayerNo(1)], FieldCoordinate(13, 6))
        setupPlayer(state, this[PlayerNo(2)], FieldCoordinate(13, 7))
        setupPlayer(state, this[PlayerNo(3)], FieldCoordinate(13, 8))
        setupPlayer(state, this[PlayerNo(4)], FieldCoordinate(15, 1))
        setupPlayer(state, this[PlayerNo(5)], FieldCoordinate(15, 4))
        setupPlayer(state, this[PlayerNo(6)], FieldCoordinate(15, 10))
        setupPlayer(state, this[PlayerNo(7)], FieldCoordinate(15, 13))
        setupPlayer(state, this[PlayerNo(8)], FieldCoordinate(17, 1))
        setupPlayer(state, this[PlayerNo(9)], FieldCoordinate(17, 4))
        setupPlayer(state, this[PlayerNo(10)], FieldCoordinate(17, 10))
        setupPlayer(state, this[PlayerNo(11)], FieldCoordinate(17, 13))
    }
}

fun <T : Any?> MutableStateFlow<T>.safeTryEmit(value: T) {
    if (!this.tryEmit(value)) {
        throw IllegalStateException("Failed to emit value: $value")
    }
}

fun <T : Any?> MutableSharedFlow<T>.safeTryEmit(value: T) {
    if (!this.tryEmit(value)) {
        throw IllegalStateException("Failed to emit value: $value")
    }
}

fun List<Skill>.getRerollActionDescriptors(type: DiceRollType, roll: D6DieRoll, successOnFirstRoll: Boolean?): List<SelectRerollOption> {
    return this.asSequence().filter { it is RerollSource }
        .map { it as RerollSource }
        .filter { it.canReroll(type, listOf(roll), successOnFirstRoll) }
        .flatMap { it: RerollSource -> it.calculateRerollOptions(type, roll, successOnFirstRoll) }
        .map { SelectRerollOption(it) }.toList()
}

/**
 * Calculate all available re-rolls options for a given roll type.
 * If no re-rolls are available, an empty list is returned.
 *
 * This method doesn't work for BLOCK rolls.
 */
fun calculateAvailableRerollsFor(
    rules: Rules, // Ruleset used
    player: Player, // Player rolling the dice
    type: DiceRollType, // Which type of dice roll
    roll: D6DieRoll, // The result of the first dice
    firstRollWasSuccess: Boolean? // Whether the first roll was a success.
): List<SelectRerollOption> {
    if (type == DiceRollType.BLOCK) throw IllegalArgumentException("Use XX instead")

    // Check any skills available to the player
    val skillRerolls: List<SelectRerollOption> = player.skills.getRerollActionDescriptors(
        type,
        roll,
        firstRollWasSuccess
    )

    // Check if there is any team re-rolls available
    val team = player.team
    val hasTeamRerolls = team.availableRerollCount > 0
    val allowedToUseTeamReroll = rules.canUseTeamReroll(player.team.game, player)

    // Calculate the full list of re-roll options
    return if (skillRerolls.isEmpty() && (!hasTeamRerolls || !allowedToUseTeamReroll)) {
        emptyList()
    } else {
        val teamReroll = if (hasTeamRerolls && allowedToUseTeamReroll) {
                listOf(
                    SelectRerollOption(
                    DiceRerollOption(
                        rules.getAvailableTeamReroll(
                            team
                        ), listOf(roll)
                    )
                )
                )
            } else {
                emptyList()
            }
        skillRerolls + teamReroll
    }
}

