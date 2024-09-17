package dk.ilios.jervis.fumbbl.adapter.impl.setup

import dk.ilios.jervis.actions.D3Result
import dk.ilios.jervis.actions.D6Result
import dk.ilios.jervis.actions.RandomPlayersSelected
import dk.ilios.jervis.fumbbl.adapter.CommandActionMapper
import dk.ilios.jervis.fumbbl.adapter.JervisActionHolder
import dk.ilios.jervis.fumbbl.adapter.add
import dk.ilios.jervis.fumbbl.model.ModelChangeId
import dk.ilios.jervis.fumbbl.model.reports.KickoffPitchInvasionReport
import dk.ilios.jervis.fumbbl.net.commands.ServerCommandModelSync
import dk.ilios.jervis.fumbbl.utils.FumbblGame
import dk.ilios.jervis.model.Game
import dk.ilios.jervis.model.PlayerId
import dk.ilios.jervis.procedures.tables.kickoff.PitchInvasion

object PitchInvasionMapper: CommandActionMapper {
    override fun isApplicable(
        game: FumbblGame,
        command: ServerCommandModelSync,
        processedCommands: MutableList<ServerCommandModelSync>
    ): Boolean {
        return (
            command.firstChangeId() == ModelChangeId.FIELD_MODEL_SET_PLAYER_STATE &&
            command.reportList.size == 1 &&
            command.reportList.first() is KickoffPitchInvasionReport
        )
    }

    override fun mapServerCommand(
        fumbblGame: dk.ilios.jervis.fumbbl.model.Game,
        jervisGame: Game,
        command: ServerCommandModelSync,
        processedCommands: MutableList<ServerCommandModelSync>,
        jervisCommands: List<JervisActionHolder>,
        newActions: MutableList<JervisActionHolder>
    ) {
        // Resolve a Pitch Invasion
        val isHomeKicking = jervisGame.kickingTeam == jervisGame.homeTeam
        val report = command.reportList.first() as KickoffPitchInvasionReport
        val homeRoll = D6Result(report.rollHome)
        val awayRoll = D6Result(report.rollAway)
        newActions.add(if (isHomeKicking) homeRoll else awayRoll, PitchInvasion.RollForKickingTeamFans)
        newActions.add(if (isHomeKicking) awayRoll else homeRoll, PitchInvasion.RollForReceivingTeamFans)
        // Split stuns into teams to figure out the result
        val (homeStuns, awayStuns) =
            report.playerIds.map {
                jervisGame.getPlayerById(PlayerId(it.id))
            }.partition { player ->
                player.team.isHomeTeam()
            }

        if (isHomeKicking) {
            if (awayStuns.isNotEmpty()) {
                newActions.add(D3Result(awayStuns.size), PitchInvasion.RollForReceivingTeamStuns)
                newActions.add(RandomPlayersSelected(awayStuns.map { it.id }), PitchInvasion.SelectReceivingTeamAffectedPlayers)
            }
            if (homeStuns.isNotEmpty()) {
                newActions.add(D3Result(homeStuns.size), PitchInvasion.RollForKickingTeamStuns)
                newActions.add(RandomPlayersSelected(homeStuns.map { it.id }), PitchInvasion.SelectKickingTeamAffectedPlayers)
            }
        } else {
            if (homeStuns.isNotEmpty()) {
                newActions.add(D3Result(homeStuns.size), PitchInvasion.RollForReceivingTeamStuns)
                newActions.add(RandomPlayersSelected(homeStuns.map { it.id }), PitchInvasion.SelectReceivingTeamAffectedPlayers)
            }
            if (awayStuns.isNotEmpty()) {
                newActions.add(D3Result(awayStuns.size), PitchInvasion.RollForKickingTeamStuns)
                newActions.add(RandomPlayersSelected(awayStuns.map { it.id }), PitchInvasion.SelectKickingTeamAffectedPlayers)
            }
        }
    }
}
