package dk.ilios.jervis.fumbbl.adapter.impl.setup

import dk.ilios.jervis.actions.CoinSideSelected
import dk.ilios.jervis.actions.CoinTossResult
import dk.ilios.jervis.fumbbl.adapter.CommandActionMapper
import dk.ilios.jervis.fumbbl.adapter.JervisActionHolder
import dk.ilios.jervis.fumbbl.adapter.add
import dk.ilios.jervis.fumbbl.model.ModelChangeId
import dk.ilios.jervis.fumbbl.model.reports.CoinThrowReport
import dk.ilios.jervis.fumbbl.net.commands.ServerCommandModelSync
import dk.ilios.jervis.fumbbl.utils.FumbblGame
import dk.ilios.jervis.model.Coin
import dk.ilios.jervis.model.Game
import dk.ilios.jervis.procedures.DetermineKickingTeam

object ThrowCoinMapper: CommandActionMapper {
    override fun isApplicable(
        game: FumbblGame,
        command: ServerCommandModelSync,
        processedCommands: MutableSet<ServerCommandModelSync>
    ): Boolean {
        return (
            command.firstChangeId() == ModelChangeId.GAME_SET_DIALOG_PARAMETER &&
            command.reportList.firstOrNull() is CoinThrowReport
        )
    }

    override fun mapServerCommand(
        fumbblGame: FumbblGame,
        jervisGame: Game,
        command: ServerCommandModelSync,
        processedCommands: MutableSet<ServerCommandModelSync>,
        jervisCommands: List<JervisActionHolder>,
        newActions: MutableList<JervisActionHolder>
    ) {
        // Handle Coin throw for starting player
        val report = command.reportList.firstOrNull() as CoinThrowReport
        val throwHeads = report.coinThrowHeads
        val choseHeads = report.coinChoiceHeads
        newActions.add(
            CoinSideSelected(if (choseHeads) Coin.HEAD else Coin.TAIL),
            DetermineKickingTeam.SelectCoinSide,
        )
        newActions.add(
            CoinTossResult(if (throwHeads) Coin.HEAD else Coin.TAIL),
            DetermineKickingTeam.CoinToss,
        )
        // jervisCommands.add(CoinSideSelected(if (choseHeads) Coin.HEAD else Coin.TAIL), DetermineKickingTeam.C)
    }
}
