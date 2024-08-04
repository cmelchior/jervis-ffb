package dk.ilios.jervis.fumbbl

import dk.ilios.jervis.actions.ActionDescriptor
import dk.ilios.jervis.actions.Cancel
import dk.ilios.jervis.actions.CoinSideSelected
import dk.ilios.jervis.actions.CoinTossResult
import dk.ilios.jervis.actions.Confirm
import dk.ilios.jervis.actions.Continue
import dk.ilios.jervis.actions.D3Result
import dk.ilios.jervis.actions.D6Result
import dk.ilios.jervis.actions.DiceResults
import dk.ilios.jervis.actions.EndAction
import dk.ilios.jervis.actions.EndSetup
import dk.ilios.jervis.actions.EndTurn
import dk.ilios.jervis.actions.FieldSquareSelected
import dk.ilios.jervis.actions.GameAction
import dk.ilios.jervis.actions.PlayerActionSelected
import dk.ilios.jervis.actions.PlayerSelected
import dk.ilios.jervis.actions.RandomPlayersSelected
import dk.ilios.jervis.controller.GameController
import dk.ilios.jervis.ext.d3
import dk.ilios.jervis.ext.d6
import dk.ilios.jervis.fsm.Node
import dk.ilios.jervis.fumbbl.model.ModelChangeId
import dk.ilios.jervis.fumbbl.model.PlayerAction
import dk.ilios.jervis.fumbbl.model.ReportId
import dk.ilios.jervis.fumbbl.model.TurnMode
import dk.ilios.jervis.fumbbl.model.change.ActingPlayerSetPlayerId
import dk.ilios.jervis.fumbbl.model.change.FieldModelSetPlayerCoordinate
import dk.ilios.jervis.fumbbl.model.change.FieldModelSetPlayerState
import dk.ilios.jervis.fumbbl.model.change.GameSetSetupOffense
import dk.ilios.jervis.fumbbl.model.change.GameSetTurnMode
import dk.ilios.jervis.fumbbl.model.reports.CatchRollReport
import dk.ilios.jervis.fumbbl.model.reports.CoinThrowReport
import dk.ilios.jervis.fumbbl.model.reports.FanFactorReport
import dk.ilios.jervis.fumbbl.model.reports.KickoffPitchInvasionReport
import dk.ilios.jervis.fumbbl.model.reports.KickoffResultReport
import dk.ilios.jervis.fumbbl.model.reports.KickoffScatterReport
import dk.ilios.jervis.fumbbl.model.reports.PlayerActionReport
import dk.ilios.jervis.fumbbl.model.reports.ReceiveChoiceReport
import dk.ilios.jervis.fumbbl.model.reports.Report
import dk.ilios.jervis.fumbbl.model.reports.ScatterBallReport
import dk.ilios.jervis.fumbbl.model.reports.WeatherReport
import dk.ilios.jervis.fumbbl.net.commands.ServerCommandModelSync
import dk.ilios.jervis.fumbbl.net.commands.ServerCommandReplay
import dk.ilios.jervis.fumbbl.utils.FumbblCoordinate
import dk.ilios.jervis.fumbbl.utils.FumbblGame
import dk.ilios.jervis.fumbbl.utils.fromFumbblState
import dk.ilios.jervis.model.Coin
import dk.ilios.jervis.model.FieldCoordinate
import dk.ilios.jervis.model.Game
import dk.ilios.jervis.model.PlayerId
import dk.ilios.jervis.procedures.Bounce
import dk.ilios.jervis.procedures.CatchRoll
import dk.ilios.jervis.procedures.DetermineKickingTeam
import dk.ilios.jervis.procedures.MoveAction
import dk.ilios.jervis.procedures.RollForStartingFanFactor
import dk.ilios.jervis.procedures.RollForTheWeather
import dk.ilios.jervis.procedures.SetupTeam
import dk.ilios.jervis.procedures.TeamTurn
import dk.ilios.jervis.procedures.TheKickOff
import dk.ilios.jervis.procedures.TheKickOffEvent
import dk.ilios.jervis.procedures.bb2020.kickoff.PitchInvasion
import dk.ilios.jervis.rules.Rules
import dk.ilios.jervis.rules.tables.RandomDirectionTemplate
import kotlinx.coroutines.runBlocking
import okio.Path
import okio.Path.Companion.toPath

sealed interface JervisActionHolder {
    val expectedNode: Node
}

data class JervisAction(
    val action: GameAction,
    override val expectedNode: Node,
) : JervisActionHolder

data class CalculatedJervisAction(
    val actionFunc: (state: Game, rules: Rules) -> GameAction,
    override val expectedNode: Node,
) : JervisActionHolder

fun MutableList<JervisActionHolder>.add(
    action: GameAction,
    expectedNode: Node,
) {
    this.add(JervisAction(action, expectedNode))
}

fun MutableList<JervisActionHolder>.add(
    action: (state: Game, rules: Rules) -> GameAction,
    expectedNode: Node,
) {
    this.add(CalculatedJervisAction(action, expectedNode))
}

// TODO This approach is ultimately broken. It isn't possible to extract actions
//  from incomplete data. It looks like FUMBBL is sometimes sending the same
//  data multiple times, so figuring out which to convert to actions is impossible
//  without the context of the game.
//
// What else can we do
// Whenever there is a request for action send current Node + actions to a method that will roll forward until it
// finds what it needs... Counter: Will be tricky, some Fumbbl commands will create multiple actions, then they need to
// stored somewhere and depleted (might be possible)
//
// Only other approach would be to replicate the state machine that FUMBBL use and update the FUMBBL metadata.
// This will give us a full state to query. It might be more helpful if others want to convert a FUMBBL state into
// something else as well...but will probably be more work. Hmm, maybe it is just porting the ModelChangeProcessor
// which is just a lot of trivial code.
class FumbblReplayAdapter(private var replayFile: Path) {
    private lateinit var gameCommands: MutableList<JervisActionHolder>
    private lateinit var game: FumbblGame
    private lateinit var jervisGame: Game
    private var modelChangeIndex: Int = 0
    private lateinit var modelChangeCommands: List<ServerCommandModelSync>
    private lateinit var adapter: FumbblFileReplayAdapter

    suspend fun loadCommands() {
        val file = platformFileSystem.canonicalize("".toPath()) / (replayFile)
        adapter = FumbblFileReplayAdapter(file)
        val commands: MutableList<ServerCommandReplay> = mutableListOf()
        runBlocking {
            adapter.start()
            game = adapter.getGame()
            jervisGame = Game.fromFumbblState(game)
            var isDone = false
            while (!isDone) {
                val cmd = adapter.receive()
                isDone =
                    when (cmd) {
                        is ServerCommandReplay -> cmd.lastCommand
                        else -> false
                    }
                commands.add(cmd)
            }
        }
        adapter.close()

        // Normalize replay to a list of model changes
        modelChangeCommands =
            commands.flatMap {
                it.commandArray
            }
        processCommands(modelChangeCommands)
    }

    data class IndexWrapper(var value: Int = 0) {
        fun increment() = value++
    }

    private fun processCommands(commands: List<ServerCommandModelSync>) {
        val jervisCommands: MutableList<JervisActionHolder> = mutableListOf()
        val i = IndexWrapper()
        while (i.value < commands.size) {
            val cmd: ServerCommandModelSync = commands[i.value]
            handleCommand(cmd, i, commands, jervisCommands)
            cmd.modelChangeList.forEach {
                if (!ModelChangeProcessor.apply(game, it)) {
                    throw IllegalStateException("Failed at: $it")
                }
            }
            i.increment()
        }
        gameCommands = jervisCommands
    }

    private fun handleCommand(
        cmd: ServerCommandModelSync,
        i: IndexWrapper,
        commands: List<ServerCommandModelSync>,
        jervisCommands: MutableList<JervisActionHolder>,
    ) {
        if (cmd.reportList.size == 1 && cmd.reportList.first() is PlayerActionReport) {
            // Abort a previous started action if possible (only move right now?).
            // Jervis doesn't support undoing actions right now, so just remove the first action from the action list.
            if (jervisCommands.last().expectedNode == TeamTurn.DeselectPlayerOrSelectAction) {
                jervisCommands.removeLast() // Select Move Action
                jervisCommands.removeLast() // Select Player
            }
            when ((cmd.reportList.first() as PlayerActionReport).playerAction) {
                PlayerAction.MOVE -> {
                    val movingPlayerId = cmd.modelChangeList.filterIsInstance<ActingPlayerSetPlayerId>().first().value!!
                    val movingPlayer = jervisGame.getPlayerById(PlayerId(movingPlayerId.id))!!
                    jervisCommands.add(PlayerSelected(movingPlayer), TeamTurn.SelectPlayerOrEndTurn)
                    jervisCommands.add(
                        { state, rules -> PlayerActionSelected(rules.teamActions.move.action) },
                        TeamTurn.DeselectPlayerOrSelectAction,
                    )
                    return
                }
                else -> { /* Fall through */ }
            }
        }

        // Figure out which event is being executed by looking at the first ModelChange.
        // This is often not enough, so each entry might contain addtional that either
        // checks reports or other model changes in the same ModelSync batch in order
        // to figure out exactly what is happening.
        when (cmd.modelChangeList.firstOrNull()?.id) {
            ModelChangeId.ACTING_PLAYER_MARK_SKILL_USED -> reportNotHandled(cmd)
            ModelChangeId.ACTING_PLAYER_MARK_SKILL_UNUSED -> reportNotHandled(cmd)
            ModelChangeId.ACTING_PLAYER_SET_CURRENT_MOVE -> {
                when (game.actingPlayer.playerAction) {
                    PlayerAction.MOVE -> {
                        val moves: List<FieldModelSetPlayerCoordinate> = cmd.modelChangeList.filterIsInstance<FieldModelSetPlayerCoordinate>()
                        moves.forEach {
                            val coord = FieldCoordinate(it.value!!.x, it.value!!.y)
                            jervisCommands.add(FieldSquareSelected(coord), MoveAction.SelectSquareOrEndAction)
                        }
                    }
                    else -> reportNotHandled(cmd)
                }
            }
            ModelChangeId.ACTING_PLAYER_SET_DODGING -> reportNotHandled(cmd)
            ModelChangeId.ACTING_PLAYER_SET_GOING_FOR_IT -> reportNotHandled(cmd)
            ModelChangeId.ACTING_PLAYER_SET_HAS_BLOCKED -> reportNotHandled(cmd)
            ModelChangeId.ACTING_PLAYER_SET_HAS_FED -> reportNotHandled(cmd)
            ModelChangeId.ACTING_PLAYER_SET_HAS_FOULED -> reportNotHandled(cmd)
            ModelChangeId.ACTING_PLAYER_SET_HAS_JUMPED -> reportNotHandled(cmd)
            ModelChangeId.ACTING_PLAYER_SET_HAS_MOVED -> {
//                    jervisCommands.add(EndAction, MoveAction.SelectSquareOrEndAction)
            }
            ModelChangeId.ACTING_PLAYER_SET_HAS_PASSED -> reportNotHandled(cmd)
            ModelChangeId.ACTING_PLAYER_SET_JUMPING -> reportNotHandled(cmd)
            ModelChangeId.ACTING_PLAYER_SET_OLD_PLAYER_STATE -> reportNotHandled(cmd)
            ModelChangeId.ACTING_PLAYER_SET_PLAYER_ACTION -> reportNotHandled(cmd)
            ModelChangeId.ACTING_PLAYER_SET_PLAYER_ID -> reportNotHandled(cmd)
            ModelChangeId.ACTING_PLAYER_SET_STANDING_UP -> reportNotHandled(cmd)
            ModelChangeId.ACTING_PLAYER_SET_STRENGTH -> {
//                    if (cmd.modelChangeList.size >= 4 && cmd.modelChangeList[4] is ActingPlayerSetPlayerAction) {
//                        when((cmd.modelChangeList[4] as ActingPlayerSetPlayerAction).value) {
//                            PlayerAction.MOVE -> TODO()
//                            else -> TODO()
//                        }
//
//                    } else if (cmd.modelChangeList.size >= 5 && cmd.modelChangeList[5] is ActingPlayerSetPlayerAction) {
//                        // Standup
//                    } else {
                reportNotHandled(cmd)
//                    }
            }
            ModelChangeId.ACTING_PLAYER_SET_SUFFERING_ANIMOSITY -> reportNotHandled(cmd)
            ModelChangeId.ACTING_PLAYER_SET_SUFFERING_BLOOD_LUST -> reportNotHandled(cmd)
            ModelChangeId.FIELD_MODEL_ADD_BLOOD_SPOT -> reportNotHandled(cmd)
            ModelChangeId.FIELD_MODEL_ADD_CARD -> reportNotHandled(cmd)
            ModelChangeId.FIELD_MODEL_ADD_CARD_EFFECT -> reportNotHandled(cmd)
            ModelChangeId.FIELD_MODEL_ADD_DICE_DECORATION -> reportNotHandled(cmd)
            ModelChangeId.FIELD_MODEL_ADD_INTENSIVE_TRAINING -> reportNotHandled(cmd)
            ModelChangeId.FIELD_MODEL_ADD_FIELD_MARKER -> reportNotHandled(cmd)
            ModelChangeId.FIELD_MODEL_ADD_MOVE_SQUARE -> reportNotHandled(cmd)
            ModelChangeId.FIELD_MODEL_ADD_PLAYER_MARKER -> reportNotHandled(cmd)
            ModelChangeId.FIELD_MODEL_ADD_PRAYER -> reportNotHandled(cmd)
            ModelChangeId.FIELD_MODEL_ADD_SKILL_ENHANCEMENTS -> reportNotHandled(cmd)
            ModelChangeId.FIELD_MODEL_ADD_PUSHBACK_SQUARE -> reportNotHandled(cmd)
            ModelChangeId.FIELD_MODEL_ADD_TRACK_NUMBER -> reportNotHandled(cmd)
            ModelChangeId.FIELD_MODEL_ADD_TRAP_DOOR -> reportNotHandled(cmd)
            ModelChangeId.FIELD_MODEL_ADD_WISDOM -> reportNotHandled(cmd)
            ModelChangeId.FIELD_MODEL_KEEP_DEACTIVATED_CARD -> reportNotHandled(cmd)
            ModelChangeId.FIELD_MODEL_REMOVE_CARD -> reportNotHandled(cmd)
            ModelChangeId.FIELD_MODEL_REMOVE_CARD_EFFECT -> reportNotHandled(cmd)
            ModelChangeId.FIELD_MODEL_REMOVE_DICE_DECORATION -> reportNotHandled(cmd)
            ModelChangeId.FIELD_MODEL_REMOVE_FIELD_MARKER -> reportNotHandled(cmd)
            ModelChangeId.FIELD_MODEL_REMOVE_MOVE_SQUARE -> reportNotHandled(cmd)
            ModelChangeId.FIELD_MODEL_REMOVE_PLAYER -> reportNotHandled(cmd)
            ModelChangeId.FIELD_MODEL_REMOVE_PLAYER_MARKER -> reportNotHandled(cmd)
            ModelChangeId.FIELD_MODEL_REMOVE_PRAYER -> reportNotHandled(cmd)
            ModelChangeId.FIELD_MODEL_REMOVE_SKILL_ENHANCEMENTS -> reportNotHandled(cmd)
            ModelChangeId.FIELD_MODEL_REMOVE_PUSHBACK_SQUARE -> reportNotHandled(cmd)
            ModelChangeId.FIELD_MODEL_REMOVE_TRACK_NUMBER -> reportNotHandled(cmd)
            ModelChangeId.FIELD_MODEL_REMOVE_TRAP_DOOR -> reportNotHandled(cmd)
            ModelChangeId.FIELD_MODEL_SET_BALL_COORDINATE -> {
                // End setup and scatter ball
                if (cmd.reportList.size == 1 && cmd.reportList.first() is KickoffScatterReport) {
                    // FUMBBL does not seem to pick a kicking player (probably because it doesn't really
                    // matter), but instead just asks you if you want to use Kick if an eligible player
                    // is present. To mirror this behavior, attempt to find a valid player with Kick
                    // and if not found, just pick a random one
//                        jervisCommands.add(EndSetup, SetupTeam.SelectPlayerOrEndSetup)
                    jervisCommands.add({ state: Game, rules: Rules ->
                        // TODO This might return 0 players if all are on the LoS
                        val eligiblePlayers =
                            state.kickingTeam.filter {
                                it.location.isInCenterField(rules) && !it.location.isOnLineOfScrimmage(rules)
                            }
                        // TODO: Find a player with Kick (not implemented yet)
                        PlayerSelected(eligiblePlayers.random())
                    }, TheKickOff.NominateKickingPlayer)

                    val report = cmd.reportList.first() as KickoffScatterReport

                    // FUMBBL use a different Random Direction Template than the official rules. Theirs start
                    // with 1 = North and the go clockwise.
                    val endLocation: FumbblCoordinate = report.ballCoordinateEnd
                    val startingPoint: FumbblCoordinate =
                        endLocation.move(
                            report.scatterDirection.reverse(),
                            report.rollScatterDistance,
                        )

                    // TODO Kick not supported yet
                    jervisCommands.add(FieldSquareSelected(startingPoint.x, startingPoint.y), TheKickOff.PlaceTheKick)
                    jervisCommands.add(
                        DiceResults(
                            RandomDirectionTemplate.getRollForDirection(
                                report.scatterDirection.transformToJervisDirection(),
                            ),
                            D6Result(report.rollScatterDistance),
                        ),
                        TheKickOff.TheKickDeviates,
                    )
                } else if (
                    // Ball bounce
                    cmd.reportList.size == 1 &&
                    cmd.reportList.first() is ScatterBallReport &&
                    cmd.sound == "bounce"
                ) {
                    val direction = (cmd.reportList[0] as ScatterBallReport).directionArray.first().transformToJervisDirection()
                    val roll = RandomDirectionTemplate.getRollForDirection(direction)
                    jervisCommands.add(DiceResults(roll), Bounce.RollDirection)
                }
            }
            ModelChangeId.FIELD_MODEL_SET_BALL_IN_PLAY -> reportNotHandled(cmd)
            ModelChangeId.FIELD_MODEL_SET_BALL_MOVING -> reportNotHandled(cmd)
            ModelChangeId.FIELD_MODEL_SET_BLITZ_STATE -> reportNotHandled(cmd)
            ModelChangeId.FIELD_MODEL_SET_BOMB_COORDINATE -> reportNotHandled(cmd)
            ModelChangeId.FIELD_MODEL_SET_BOMB_MOVING -> reportNotHandled(cmd)
            ModelChangeId.FIELD_MODEL_SET_PLAYER_COORDINATE -> reportNotHandled(cmd)
            ModelChangeId.FIELD_MODEL_SET_PLAYER_STATE -> {
                // Moving a player for setting up a drive
                // This is also being called when starting the half. Not sure why FUMBBL does this
                // but we just need to discard these events
                if (cmd.modelChangeList.size == 2 &&
                    cmd.reportList.isEmpty() &&
                    cmd.modelChangeList[1].id == ModelChangeId.FIELD_MODEL_SET_PLAYER_COORDINATE &&
                    game.turnMode == TurnMode.SETUP
                ) {
                    val playerId = (cmd.modelChangeList.first() as FieldModelSetPlayerState).key!!
                    var coordinates = (cmd.modelChangeList[1] as FieldModelSetPlayerCoordinate).value!!
                    val selectedPlayer = jervisGame.getPlayerById(PlayerId(playerId))!!
                    jervisCommands.add(PlayerSelected(selectedPlayer), SetupTeam.SelectPlayerOrEndSetup)
                    jervisCommands.add(FieldSquareSelected(coordinates.x, coordinates.y), SetupTeam.PlacePlayer)
                } else if (cmd.reportList.size == 1 && cmd.reportList.first() is KickoffPitchInvasionReport) {
                    // Resolve a Pitch Invasion
                    val report = cmd.reportList.first() as KickoffPitchInvasionReport
                    val homeRoll = D6Result(report.rollHome)
                    val awayRoll = D6Result(report.rollAway)
                    jervisCommands.add(homeRoll, PitchInvasion.RollForHomeTeam)
                    jervisCommands.add(awayRoll, PitchInvasion.RollForAwayTeam)
                    // Split stuns into teams to figure out the result
                    val (homeStuns, awayStuns) =
                        report.playerIds.map {
                            jervisGame.getPlayerById(PlayerId(it.id))!!
                        }.partition { player ->
                            player.team.isHomeTeam()
                        }
                    if (homeStuns.isNotEmpty()) {
                        jervisCommands.add(D3Result(homeStuns.size), PitchInvasion.RollForHomeTeamStuns)
                        jervisCommands.add(RandomPlayersSelected(homeStuns), PitchInvasion.ResolveHomeTeamStuns)
                    }
                    if (awayStuns.isNotEmpty()) {
                        jervisCommands.add(D3Result(awayStuns.size), PitchInvasion.RollForAwayTeamStuns)
                        jervisCommands.add(RandomPlayersSelected(awayStuns), PitchInvasion.RollForHomeTeamStuns)
                    }
                } else if (cmd.modelChangeList.size >= 3 && cmd.modelChangeList[2] is ActingPlayerSetPlayerId) {
                    if ((cmd.modelChangeList[2] as ActingPlayerSetPlayerId).value == null) {
                        when (game.actingPlayer.playerAction) {
                            PlayerAction.MOVE -> {
//                                if (jervisCommands.last().expectedNode == TeamTurn.DeselectPlayerOrSelectAction) {
//                                    jer
//                                } else {
                                jervisCommands.add(EndAction, MoveAction.SelectSquareOrEndAction)
//                                }
                            }
                            else -> reportNotHandled(cmd)
                        }
                    }
                } else {
                    reportNotHandled(cmd)
                }
            }
            ModelChangeId.FIELD_MODEL_SET_RANGE_RULER -> reportNotHandled(cmd)
            ModelChangeId.FIELD_MODEL_SET_TARGET_SELECTION_STATE -> reportNotHandled(cmd)
            ModelChangeId.FIELD_MODEL_SET_WEATHER -> reportNotHandled(cmd)
            ModelChangeId.GAME_SET_ADMIN_MODE -> reportNotHandled(cmd)
            ModelChangeId.GAME_SET_CONCEDED_LEGALLY -> reportNotHandled(cmd)
            ModelChangeId.GAME_SET_CONCESSION_POSSIBLE -> reportNotHandled(cmd)
            ModelChangeId.GAME_SET_DEFENDER_ACTION -> reportNotHandled(cmd)
            ModelChangeId.GAME_SET_DEFENDER_ID -> reportNotHandled(cmd)
            ModelChangeId.GAME_SET_DIALOG_PARAMETER -> {
                // Handle a bunch of Dialogs
                val report: Report? = cmd.reportList.firstOrNull()
                if (report is CoinThrowReport) {
                    // Handle Coin throw for starting player
                    val throwHeads = report.coinThrowHeads
                    val choseHeads = report.coinChoiceHeads
                    jervisCommands.add(
                        CoinSideSelected(if (choseHeads) Coin.HEAD else Coin.TAIL),
                        DetermineKickingTeam.SelectCoinSide,
                    )
                    jervisCommands.add(
                        CoinTossResult(if (throwHeads) Coin.HEAD else Coin.TAIL),
                        DetermineKickingTeam.CoinToss,
                    )
//                        jervisCommands.add(CoinSideSelected(if (choseHeads) Coin.HEAD else Coin.TAIL), DetermineKickingTeam.C)
                } else if (report is ReceiveChoiceReport) {
                    // Handle selecting to receive or kick
                    val kicking = !report.receiveChoice
                    jervisCommands.add(if (kicking) Cancel else Confirm, DetermineKickingTeam.ChooseKickingTeam)
                } else {
                    reportNotHandled(cmd)
                }
            }
            ModelChangeId.GAME_SET_FINISHED -> reportNotHandled(cmd)
            ModelChangeId.GAME_SET_HALF -> reportNotHandled(cmd)
            ModelChangeId.GAME_SET_HOME_FIRST_OFFENSE -> reportNotHandled(cmd)
            ModelChangeId.GAME_SET_HOME_PLAYING -> {
                if (game.turnMode == TurnMode.SETUP && cmd.modelChangeList.size == 2 && cmd.modelChangeList.last() is GameSetSetupOffense) {
                    // Ending first team setup
                    jervisCommands.add(EndSetup, SetupTeam.SelectPlayerOrEndSetup)
                } else if (game.turnMode == TurnMode.SETUP && cmd.modelChangeList.size == 3 && cmd.modelChangeList.last() is GameSetTurnMode) {
                    // Ending second team setup
                    jervisCommands.add(EndSetup, SetupTeam.SelectPlayerOrEndSetup)
                } else {
                    reportNotHandled(cmd)
                }
            }
            ModelChangeId.GAME_SET_ID -> reportNotHandled(cmd)
            ModelChangeId.GAME_SET_LAST_DEFENDER_ID -> reportNotHandled(cmd)
            ModelChangeId.GAME_SET_LAST_TURN_MODE -> reportNotHandled(cmd)
            ModelChangeId.GAME_SET_PASS_COORDINATE -> reportNotHandled(cmd)
            ModelChangeId.GAME_SET_SCHEDULED -> reportNotHandled(cmd)
            ModelChangeId.GAME_SET_SETUP_OFFENSE -> reportNotHandled(cmd)
            ModelChangeId.GAME_SET_STARTED -> {
                // Start the game and roll for fan factor
                verifyReportSize(2, cmd)
                val homeTeamRoll = (cmd.reportList.reports[0] as FanFactorReport).dedicatedFansRoll
                val awayTeamRoll = (cmd.reportList.reports[1] as FanFactorReport).dedicatedFansRoll
                jervisCommands.add(homeTeamRoll.d3, RollForStartingFanFactor.SetFanFactorForHomeTeam)
                jervisCommands.add(homeTeamRoll.d3, RollForStartingFanFactor.SetFanFactorForAwayTeam)
            }
            ModelChangeId.GAME_SET_TESTING -> reportNotHandled(cmd)
            ModelChangeId.GAME_SET_THROWER_ID -> reportNotHandled(cmd)
            ModelChangeId.GAME_SET_THROWER_ACTION -> reportNotHandled(cmd)
            ModelChangeId.GAME_SET_TIMEOUT_ENFORCED -> reportNotHandled(cmd)
            ModelChangeId.GAME_SET_TIMEOUT_POSSIBLE -> reportNotHandled(cmd)
            ModelChangeId.GAME_SET_TURN_MODE -> reportNotHandled(cmd)
            ModelChangeId.GAME_SET_WAITING_FOR_OPPONENT -> reportNotHandled(cmd)
            ModelChangeId.GAME_OPTIONS_ADD_OPTION -> reportNotHandled(cmd)
            ModelChangeId.INDUCEMENT_SET_ACTIVATE_CARD -> reportNotHandled(cmd)
            ModelChangeId.INDUCEMENT_SET_ADD_AVAILABLE_CARD -> reportNotHandled(cmd)
            ModelChangeId.INDUCEMENT_SET_ADD_INDUCEMENT -> reportNotHandled(cmd)
            ModelChangeId.INDUCEMENT_SET_CARD_CHOICES -> reportNotHandled(cmd)
            ModelChangeId.INDUCEMENT_SET_DEACTIVATE_CARD -> reportNotHandled(cmd)
            ModelChangeId.INDUCEMENT_SET_ADD_PRAYER -> reportNotHandled(cmd)
            ModelChangeId.INDUCEMENT_SET_REMOVE_AVAILABLE_CARD -> reportNotHandled(cmd)
            ModelChangeId.INDUCEMENT_SET_REMOVE_INDUCEMENT -> reportNotHandled(cmd)
            ModelChangeId.INDUCEMENT_SET_REMOVE_PRAYER -> reportNotHandled(cmd)
            ModelChangeId.PLAYER_MARK_SKILL_USED -> reportNotHandled(cmd)
            ModelChangeId.PLAYER_MARK_SKILL_UNUSED -> reportNotHandled(cmd)
            ModelChangeId.PLAYER_RESULT_SET_BLOCKS -> reportNotHandled(cmd)
            ModelChangeId.PLAYER_RESULT_SET_CASUALTIES -> reportNotHandled(cmd)
            ModelChangeId.PLAYER_RESULT_SET_CASUALTIES_WITH_ADDITIONAL_SPP -> reportNotHandled(cmd)
            ModelChangeId.PLAYER_RESULT_SET_COMPLETIONS -> reportNotHandled(cmd)
            ModelChangeId.PLAYER_RESULT_SET_COMPLETIONS_WITH_ADDITIONAL_SPP -> reportNotHandled(cmd)
            ModelChangeId.PLAYER_RESULT_SET_CURRENT_SPPS -> reportNotHandled(cmd)
            ModelChangeId.PLAYER_RESULT_SET_DEFECTING -> reportNotHandled(cmd)
            ModelChangeId.PLAYER_RESULT_SET_FOULS -> reportNotHandled(cmd)
            ModelChangeId.PLAYER_RESULT_SET_HAS_USED_SECRET_WEAPON -> reportNotHandled(cmd)
            ModelChangeId.PLAYER_RESULT_SET_INTERCEPTIONS -> reportNotHandled(cmd)
            ModelChangeId.PLAYER_RESULT_SET_DEFLECTIONS -> reportNotHandled(cmd)
            ModelChangeId.PLAYER_RESULT_SET_PASSING -> reportNotHandled(cmd)
            ModelChangeId.PLAYER_RESULT_SET_PLAYER_AWARDS -> reportNotHandled(cmd)
            ModelChangeId.PLAYER_RESULT_SET_RUSHING -> reportNotHandled(cmd)
            ModelChangeId.PLAYER_RESULT_SET_SEND_TO_BOX_BY_PLAYER_ID -> reportNotHandled(cmd)
            ModelChangeId.PLAYER_RESULT_SET_SEND_TO_BOX_HALF -> reportNotHandled(cmd)
            ModelChangeId.PLAYER_RESULT_SET_SEND_TO_BOX_REASON -> reportNotHandled(cmd)
            ModelChangeId.PLAYER_RESULT_SET_SEND_TO_BOX_TURN -> reportNotHandled(cmd)
            ModelChangeId.PLAYER_RESULT_SET_SERIOUS_INJURY -> reportNotHandled(cmd)
            ModelChangeId.PLAYER_RESULT_SET_SERIOUS_INJURY_DECAY -> reportNotHandled(cmd)
            ModelChangeId.PLAYER_RESULT_SET_TOUCHDOWNS -> reportNotHandled(cmd)
            ModelChangeId.PLAYER_RESULT_SET_TURNS_PLAYED -> {
                if (cmd.reportList.singleOrNull()?.reportId == ReportId.TURN_END) {
                    jervisCommands.add(EndTurn, TeamTurn.SelectPlayerOrEndTurn)
                } else {
                    reportNotHandled(cmd)
                }
            }
            ModelChangeId.TEAM_RESULT_SET_CONCEDED -> reportNotHandled(cmd)
            ModelChangeId.TEAM_RESULT_SET_DEDICATED_FANS_MODIFIER -> reportNotHandled(cmd)
            ModelChangeId.TEAM_RESULT_SET_FAME -> reportNotHandled(cmd)
            ModelChangeId.TEAM_RESULT_SET_FAN_FACTOR -> reportNotHandled(cmd)
            ModelChangeId.TEAM_RESULT_SET_BADLY_HURT_SUFFERED -> reportNotHandled(cmd)
            ModelChangeId.TEAM_RESULT_SET_FAN_FACTOR_MODIFIER -> reportNotHandled(cmd)
            ModelChangeId.TEAM_RESULT_SET_PENALTY_SCORE -> reportNotHandled(cmd)
            ModelChangeId.TEAM_RESULT_SET_PETTY_CASH_TRANSFERRED -> reportNotHandled(cmd)
            ModelChangeId.TEAM_RESULT_SET_PETTY_CASH_USED -> reportNotHandled(cmd)
            ModelChangeId.TEAM_RESULT_SET_RAISED_DEAD -> reportNotHandled(cmd)
            ModelChangeId.TEAM_RESULT_SET_RIP_SUFFERED -> reportNotHandled(cmd)
            ModelChangeId.TEAM_RESULT_SET_SCORE -> reportNotHandled(cmd)
            ModelChangeId.TEAM_RESULT_SET_SERIOUS_INJURY_SUFFERED -> reportNotHandled(cmd)
            ModelChangeId.TEAM_RESULT_SET_SPECTATORS -> reportNotHandled(cmd)
            ModelChangeId.TEAM_RESULT_SET_SPIRALLING_EXPENSES -> reportNotHandled(cmd)
            ModelChangeId.TEAM_RESULT_SET_TEAM_VALUE -> reportNotHandled(cmd)
            ModelChangeId.TEAM_RESULT_SET_WINNINGS -> reportNotHandled(cmd)
            ModelChangeId.TURN_DATA_SET_APOTHECARIES -> reportNotHandled(cmd)
            ModelChangeId.TURN_DATA_SET_BLITZ_USED -> reportNotHandled(cmd)
            ModelChangeId.TURN_DATA_SET_BOMB_USED -> reportNotHandled(cmd)
            ModelChangeId.TURN_DATA_SET_FIRST_TURN_AFTER_KICKOFF -> reportNotHandled(cmd)
            ModelChangeId.TURN_DATA_SET_FOUL_USED -> reportNotHandled(cmd)
            ModelChangeId.TURN_DATA_SET_HAND_OVER_USED -> reportNotHandled(cmd)
            ModelChangeId.TURN_DATA_SET_LEADER_STATE -> reportNotHandled(cmd)
            ModelChangeId.TURN_DATA_SET_PASS_USED -> reportNotHandled(cmd)
            ModelChangeId.TURN_DATA_SET_PLAGUE_DOCTORS -> reportNotHandled(cmd)
            ModelChangeId.TURN_DATA_SET_KTM_USED -> reportNotHandled(cmd)
            ModelChangeId.TURN_DATA_SET_RE_ROLLS -> reportNotHandled(cmd)
            ModelChangeId.TURN_DATA_SET_RE_ROLLS_BRILLIANT_COACHING_ONE_DRIVE -> reportNotHandled(cmd)
            ModelChangeId.TURN_DATA_SET_RE_ROLLS_PUMP_UP_THE_CROWD_ONE_DRIVE -> reportNotHandled(cmd)
            ModelChangeId.TURN_DATA_SET_RE_ROLLS_SINGLE_USE -> reportNotHandled(cmd)
            ModelChangeId.TURN_DATA_SET_RE_ROLL_USED -> reportNotHandled(cmd)
            ModelChangeId.TURN_DATA_SET_TURN_NR -> reportNotHandled(cmd)
            ModelChangeId.TURN_DATA_SET_TURN_STARTED -> reportNotHandled(cmd)
            ModelChangeId.TURN_DATA_SET_WANDERING_APOTHECARIES -> reportNotHandled(cmd)
            ModelChangeId.TURN_DATA_SET_COACH_BANNED -> reportNotHandled(cmd)
            null -> {
                val report: Report? = cmd.reportList.reports.firstOrNull()
                when (report) {
                    is KickoffResultReport -> {
                        val roll = report.kickoffRoll
                        jervisCommands.add(
                            DiceResults(roll.first().d6, roll.last().d6),
                            TheKickOffEvent.RollForKickOffEvent,
                        )
                    }
                    is WeatherReport -> {
                        val weatherRoll = report.weatherRoll.map { D6Result(it) }
                        jervisCommands.add(DiceResults(weatherRoll), RollForTheWeather.RollWeatherDice)
                    }
                    is CatchRollReport -> {
                        // TODO The report gives you the final result. We need to deconstruct it
                        //
                        val diceRoll = D6Result(report.roll)
                        if (report.reRolled) {
                            jervisCommands.add(DiceResults(diceRoll), CatchRoll.ReRollDie)
                        } else {
                            jervisCommands.add(DiceResults(diceRoll), CatchRoll.RollDie)
                            jervisCommands.add(Continue, CatchRoll.ChooseReRollSource)
                        }
                    }
                    else -> reportNotHandled(cmd)
                }
            }
        }
    }

    private fun reportNotHandled(cmd: ServerCommandModelSync) {
        println("Not handling: $cmd")
    }

    private fun verifyReportSize(
        expectedSize: Int,
        command: ServerCommandModelSync,
    ) {
        if (command.reportList.reports.size != expectedSize) {
            throw IllegalStateException(
                "Expected reports of size $expectedSize, was ${command.reportList.reports.size}",
            )
        }
    }

    fun getGame(): Game = jervisGame

    fun getActionProvider(): (controller: GameController, availableActions: List<ActionDescriptor>) -> GameAction {
        TODO()
    }

    fun getCommands(): List<JervisActionHolder> = gameCommands
}
