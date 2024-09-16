package dk.ilios.jervis.fumbbl.adapter.impl

import dk.ilios.jervis.actions.D6Result
import dk.ilios.jervis.actions.DiceResults
import dk.ilios.jervis.actions.FieldSquareSelected
import dk.ilios.jervis.actions.PlayerSelected
import dk.ilios.jervis.fumbbl.adapter.CommandActionMapper
import dk.ilios.jervis.fumbbl.adapter.JervisActionHolder
import dk.ilios.jervis.fumbbl.adapter.add
import dk.ilios.jervis.fumbbl.model.ModelChangeId
import dk.ilios.jervis.fumbbl.model.TurnMode
import dk.ilios.jervis.fumbbl.model.reports.KickoffScatterReport
import dk.ilios.jervis.fumbbl.net.commands.ServerCommandModelSync
import dk.ilios.jervis.fumbbl.utils.FumbblCoordinate
import dk.ilios.jervis.fumbbl.utils.FumbblGame
import dk.ilios.jervis.model.Game
import dk.ilios.jervis.procedures.DeviateRoll
import dk.ilios.jervis.procedures.TheKickOff
import dk.ilios.jervis.rules.Rules
import dk.ilios.jervis.rules.tables.RandomDirectionTemplate

/**
 * End setup and scatter ball
 */
object KickOffAndScatterMapper: CommandActionMapper {

    override fun isApplicable(
        game: FumbblGame,
        command: ServerCommandModelSync,
        processedCommands: MutableSet<ServerCommandModelSync>
    ): Boolean {
        return (
            game.turnMode == TurnMode.KICKOFF &&
            command.firstChangeId() == ModelChangeId.FIELD_MODEL_SET_BALL_COORDINATE &&
            command.reportList.size == 1 && command.firstReport() is KickoffScatterReport
        )
    }

    override fun mapServerCommand(
        fumbblGame: dk.ilios.jervis.fumbbl.model.Game,
        jervisGame: Game,
        command: ServerCommandModelSync,
        processedCommands: MutableSet<ServerCommandModelSync>,
        jervisCommands: List<JervisActionHolder>,
        newActions: MutableList<JervisActionHolder>
    ) {
        // FUMBBL does not seem to pick a kicking player (probably because it doesn't really
        // matter), but instead just asks you if you want to use Kick if an eligible player
        // is present. To mirror this behavior, attempt to find a valid player with Kick
        // and if not found, just pick a random one
//                        jervisCommands.add(EndSetup, SetupTeam.SelectPlayerOrEndSetup)
        newActions.add({ state: Game, rules: Rules ->
            // TODO This might return 0 players if all are on the LoS
            val eligiblePlayers =
                state.kickingTeam.filter {
                    it.location.isInCenterField(rules) && !it.location.isOnLineOfScrimmage(rules)
                }
            // TODO: Find a player with Kick (not implemented yet)
            PlayerSelected(eligiblePlayers.random().id)
        }, TheKickOff.NominateKickingPlayer)

        val report = command.reportList.first() as KickoffScatterReport

        // FUMBBL use a different Random Direction Template than the official rules. Theirs start
        // with 1 = North and the go clockwise.
        val endLocation: FumbblCoordinate = report.ballCoordinateEnd
        val startingPoint: FumbblCoordinate =
            endLocation.move(
                report.scatterDirection.reverse(),
                report.rollScatterDistance,
            )

        // TODO Kick not supported yet
        newActions.add(FieldSquareSelected(startingPoint.x, startingPoint.y), TheKickOff.PlaceTheKick)
        newActions.add(
            DiceResults(
                RandomDirectionTemplate.getRollForDirection(
                    report.scatterDirection.transformToJervisDirection(),
                ),
                D6Result(report.rollScatterDistance),
            ),
            DeviateRoll.RollDice,
        )
    }
}
