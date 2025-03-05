package com.jervisffb.fumbbl.net.adapter.impl.setup

import com.jervisffb.engine.actions.CoinSideSelected
import com.jervisffb.engine.actions.CoinTossResult
import com.jervisffb.engine.model.Coin
import com.jervisffb.engine.model.Game
import com.jervisffb.engine.rules.bb2020.procedures.DetermineKickingTeam
import com.jervisffb.fumbbl.net.adapter.CommandActionMapper
import com.jervisffb.fumbbl.net.adapter.JervisActionHolder
import com.jervisffb.fumbbl.net.adapter.add
import com.jervisffb.fumbbl.net.api.commands.ServerCommandModelSync
import com.jervisffb.fumbbl.net.model.ModelChangeId
import com.jervisffb.fumbbl.net.model.reports.CoinThrowReport
import com.jervisffb.fumbbl.net.utils.FumbblGame

object ThrowCoinMapper: CommandActionMapper {
    override fun isApplicable(
        game: FumbblGame,
        command: ServerCommandModelSync,
        processedCommands: MutableList<ServerCommandModelSync>
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
        processedCommands: MutableList<ServerCommandModelSync>,
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
