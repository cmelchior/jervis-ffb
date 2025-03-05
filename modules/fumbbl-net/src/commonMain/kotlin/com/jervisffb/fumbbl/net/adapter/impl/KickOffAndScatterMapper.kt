package com.jervisffb.fumbbl.net.adapter.impl

import com.jervisffb.engine.actions.D6Result
import com.jervisffb.engine.actions.DiceRollResults
import com.jervisffb.engine.actions.FieldSquareSelected
import com.jervisffb.engine.actions.PlayerSelected
import com.jervisffb.engine.model.Game
import com.jervisffb.engine.rules.Rules
import com.jervisffb.engine.rules.bb2020.procedures.DeviateRoll
import com.jervisffb.engine.rules.bb2020.procedures.TheKickOff
import com.jervisffb.engine.rules.bb2020.tables.RandomDirectionTemplate
import com.jervisffb.fumbbl.net.adapter.CommandActionMapper
import com.jervisffb.fumbbl.net.adapter.JervisActionHolder
import com.jervisffb.fumbbl.net.adapter.add
import com.jervisffb.fumbbl.net.api.commands.ServerCommandModelSync
import com.jervisffb.fumbbl.net.model.ModelChangeId
import com.jervisffb.fumbbl.net.model.TurnMode
import com.jervisffb.fumbbl.net.model.reports.KickoffScatterReport
import com.jervisffb.fumbbl.net.utils.FumbblCoordinate
import com.jervisffb.fumbbl.net.utils.FumbblGame

/**
 * End setup and scatter ball
 */
object KickOffAndScatterMapper: CommandActionMapper {

    override fun isApplicable(
        game: FumbblGame,
        command: ServerCommandModelSync,
        processedCommands: MutableList<ServerCommandModelSync>
    ): Boolean {
        return (
            game.turnMode == TurnMode.KICKOFF &&
                command.firstChangeId() == ModelChangeId.FIELD_MODEL_SET_BALL_COORDINATE &&
                command.reportList.size == 1 && command.firstReport() is KickoffScatterReport
        )
    }

    override fun mapServerCommand(
        fumbblGame: com.jervisffb.fumbbl.net.model.Game,
        jervisGame: Game,
        command: ServerCommandModelSync,
        processedCommands: MutableList<ServerCommandModelSync>,
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
            DiceRollResults(
                RandomDirectionTemplate.getRollForDirection(
                    report.scatterDirection.transformToJervisDirection(),
                ),
                D6Result(report.rollScatterDistance),
            ),
            DeviateRoll.RollDice,
        )
    }
}
