package dk.ilios.jervis.utils

import dk.ilios.jervis.actions.ActionDescriptor
import dk.ilios.jervis.actions.BlockTypeSelected
import dk.ilios.jervis.actions.Cancel
import dk.ilios.jervis.actions.CancelWhenReady
import dk.ilios.jervis.actions.CoinSideSelected
import dk.ilios.jervis.actions.CoinTossResult
import dk.ilios.jervis.actions.Confirm
import dk.ilios.jervis.actions.ConfirmWhenReady
import dk.ilios.jervis.actions.Continue
import dk.ilios.jervis.actions.ContinueWhenReady
import dk.ilios.jervis.actions.D12Result
import dk.ilios.jervis.actions.D16Result
import dk.ilios.jervis.actions.D20Result
import dk.ilios.jervis.actions.D2Result
import dk.ilios.jervis.actions.D3Result
import dk.ilios.jervis.actions.D4Result
import dk.ilios.jervis.actions.D6Result
import dk.ilios.jervis.actions.D8Result
import dk.ilios.jervis.actions.DBlockResult
import dk.ilios.jervis.actions.DeselectPlayer
import dk.ilios.jervis.actions.Dice
import dk.ilios.jervis.actions.DicePoolChoice
import dk.ilios.jervis.actions.DicePoolResultsSelected
import dk.ilios.jervis.actions.DiceRollResults
import dk.ilios.jervis.actions.DieResult
import dk.ilios.jervis.actions.DogoutSelected
import dk.ilios.jervis.actions.EndAction
import dk.ilios.jervis.actions.EndActionWhenReady
import dk.ilios.jervis.actions.EndSetup
import dk.ilios.jervis.actions.EndSetupWhenReady
import dk.ilios.jervis.actions.EndTurn
import dk.ilios.jervis.actions.EndTurnWhenReady
import dk.ilios.jervis.actions.FieldSquareSelected
import dk.ilios.jervis.actions.GameAction
import dk.ilios.jervis.actions.InducementSelected
import dk.ilios.jervis.actions.MoveTypeSelected
import dk.ilios.jervis.actions.NoRerollSelected
import dk.ilios.jervis.actions.PlayerActionSelected
import dk.ilios.jervis.actions.PlayerDeselected
import dk.ilios.jervis.actions.PlayerSelected
import dk.ilios.jervis.actions.RandomPlayersSelected
import dk.ilios.jervis.actions.RerollOptionSelected
import dk.ilios.jervis.actions.RollDice
import dk.ilios.jervis.actions.SelectBlockType
import dk.ilios.jervis.actions.SelectCoinSide
import dk.ilios.jervis.actions.SelectDicePoolResult
import dk.ilios.jervis.actions.SelectDogout
import dk.ilios.jervis.actions.SelectFieldLocation
import dk.ilios.jervis.actions.SelectInducement
import dk.ilios.jervis.actions.SelectMoveType
import dk.ilios.jervis.actions.SelectNoReroll
import dk.ilios.jervis.actions.SelectPlayer
import dk.ilios.jervis.actions.SelectPlayerAction
import dk.ilios.jervis.actions.SelectRandomPlayers
import dk.ilios.jervis.actions.SelectRerollOption
import dk.ilios.jervis.actions.SelectSkill
import dk.ilios.jervis.actions.SkillSelected
import dk.ilios.jervis.actions.TossCoin
import dk.ilios.jervis.commands.ResetAvailableTeamRerolls
import dk.ilios.jervis.commands.SetPlayerLocation
import dk.ilios.jervis.commands.SetPlayerState
import dk.ilios.jervis.controller.GameController
import dk.ilios.jervis.model.Coach
import dk.ilios.jervis.model.CoachId
import dk.ilios.jervis.model.Coin
import dk.ilios.jervis.model.Field
import dk.ilios.jervis.model.Game
import dk.ilios.jervis.model.Player
import dk.ilios.jervis.model.PlayerId
import dk.ilios.jervis.model.PlayerNo
import dk.ilios.jervis.model.PlayerState
import dk.ilios.jervis.model.Team
import dk.ilios.jervis.model.locations.FieldCoordinate
import dk.ilios.jervis.model.modifiers.DiceModifier
import dk.ilios.jervis.model.modifiers.StatModifier
import dk.ilios.jervis.procedures.D6DieRoll
import dk.ilios.jervis.rules.BB2020Rules
import dk.ilios.jervis.rules.Rules
import dk.ilios.jervis.rules.roster.bb2020.HumanTeam
import dk.ilios.jervis.rules.roster.bb2020.LizardmenTeam
import dk.ilios.jervis.rules.skills.DiceRerollOption
import dk.ilios.jervis.rules.skills.DiceRollType
import dk.ilios.jervis.rules.skills.RerollSource
import dk.ilios.jervis.rules.skills.SideStep
import dk.ilios.jervis.rules.skills.Skill
import dk.ilios.jervis.teamBuilder
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlin.jvm.JvmName
import kotlin.random.Random

fun humanTeamAway(): Team {
    return teamBuilder(BB2020Rules, HumanTeam) {
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
    return teamBuilder(BB2020Rules, LizardmenTeam) {
        coach = Coach(CoachId("away-coach"), "AwayCoach")
        name = "AwayTeam"
        addPlayer(PlayerId("A1"), "Kroxigor-1-A", PlayerNo(1), LizardmenTeam.KROXIGOR)
        addPlayer(PlayerId("A2"), "Saurus-2-A", PlayerNo(2), LizardmenTeam.SAURUS_BLOCKERS)
        addPlayer(PlayerId("A3"), "Saurus-3-A", PlayerNo(3), LizardmenTeam.SAURUS_BLOCKERS)
        addPlayer(PlayerId("A4"), "Saurus-4-A", PlayerNo(4), LizardmenTeam.SAURUS_BLOCKERS)
        addPlayer(PlayerId("A5"), "Saurus-5-A", PlayerNo(5), LizardmenTeam.SAURUS_BLOCKERS)
        addPlayer(PlayerId("A6"), "Saurus-6-A", PlayerNo(6), LizardmenTeam.SAURUS_BLOCKERS)
        addPlayer(PlayerId("A7"), "Saurus-7-A", PlayerNo(7), LizardmenTeam.SAURUS_BLOCKERS)
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
    return when (val action = availableActions.random()) {
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

class InvalidAction(message: String) : RuntimeException(message)

class InvalidGameStateException(message: String) : IllegalStateException(message)

inline fun INVALID_GAME_STATE(message: String = "Unexpected game state"): Nothing {
    throw InvalidGameStateException(message)
}

inline fun INVALID_ACTION(action: GameAction, customMessage: String? = null): Nothing {
    throw InvalidAction(customMessage?.let {
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
        command.execute(controller.state, controller)
    }

    // Also enable Team rerolls
    controller.state.activeTeam = controller.state.homeTeam
    ResetAvailableTeamRerolls(controller.state.homeTeam).execute(controller.state, controller)
    ResetAvailableTeamRerolls(controller.state.awayTeam).execute(controller.state, controller)
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
    return Game(team1, awayTeam, field)
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
            SetPlayerLocation(it, fieldCoordinate).execute(state, GameController(BB2020Rules, state))
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

fun List<Skill>.getRerollActionDescriptors(type: DiceRollType, roll: D6DieRoll, successOnFirstRoll: Boolean): List<SelectRerollOption> {
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
    firstRollWasSuccess: Boolean // Whether the first roll was a success.
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
                listOf(SelectRerollOption(DiceRerollOption(rules.getAvailableTeamReroll(team), listOf(roll))))
            } else {
                emptyList()
            }
        skillRerolls + teamReroll
    }
}

