package com.jervisffb.fumbbl.net.adapter.impl.setup

import com.jervisffb.engine.actions.D3Result
import com.jervisffb.engine.actions.D6Result
import com.jervisffb.engine.actions.RandomPlayersSelected
import com.jervisffb.engine.model.Game
import com.jervisffb.engine.model.PlayerId
import com.jervisffb.engine.rules.bb2020.procedures.tables.kickoff.PitchInvasion
import com.jervisffb.fumbbl.net.adapter.CommandActionMapper
import com.jervisffb.fumbbl.net.adapter.JervisActionHolder
import com.jervisffb.fumbbl.net.adapter.add
import com.jervisffb.fumbbl.net.api.commands.ServerCommandModelSync
import com.jervisffb.fumbbl.net.model.ModelChangeId
import com.jervisffb.fumbbl.net.model.reports.KickoffPitchInvasionReport
import com.jervisffb.fumbbl.net.utils.FumbblGame

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
        fumbblGame: com.jervisffb.fumbbl.net.model.Game,
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
