package dk.ilios.jervis.fumbbl.adapter.impl.block

import dk.ilios.jervis.actions.DBlockResult
import dk.ilios.jervis.actions.DiceRollResults
import dk.ilios.jervis.actions.RerollOptionSelected
import dk.ilios.jervis.actions.SelectRerollOption
import dk.ilios.jervis.fumbbl.adapter.CommandActionMapper
import dk.ilios.jervis.fumbbl.adapter.JervisActionHolder
import dk.ilios.jervis.fumbbl.adapter.add
import dk.ilios.jervis.fumbbl.model.PlayerAction
import dk.ilios.jervis.fumbbl.model.reports.BlockRollReport
import dk.ilios.jervis.fumbbl.model.reports.ReRollReport
import dk.ilios.jervis.fumbbl.net.commands.ServerCommandModelSync
import dk.ilios.jervis.fumbbl.utils.FumbblGame
import dk.ilios.jervis.model.Game
import dk.ilios.jervis.procedures.actions.block.standard.StandardBlockChooseReroll
import dk.ilios.jervis.procedures.actions.block.standard.StandardBlockRerollDice
import dk.ilios.jervis.rules.Rules
import dk.ilios.jervis.rules.skills.DiceRerollOption
import dk.ilios.jervis.rules.skills.RegularTeamReroll

/**
 * {
 *     "netCommandId" : "serverModelSync",
 *     "commandNr" : 64,
 *     "modelChangeList" : {
 *       "modelChangeArray" : [ {
 *         "modelChangeId" : "turnDataSetReRolls",
 *         "modelChangeKey" : "away",
 *         "modelChangeValue" : 1
 *       }, {
 *         "modelChangeId" : "gameSetDialogParameter",
 *         "modelChangeKey" : null,
 *         "modelChangeValue" : {
 *           "dialogId" : "blockRollPartialReRoll",
 *           "choosingTeamId" : "1128152",
 *           "nrOfDice" : 1,
 *           "blockRoll" : [ 5 ],
 *           "reRolledDice" : [ ],
 *           "teamReRollOption" : false,
 *           "proReRollOption" : false,
 *           "brawlerOption" : false,
 *           "reRollSourceSingleUse" : null,
 *           "consummateOption" : false,
 *           "addBlockDieSkill" : null
 *         }
 *       } ]
 *     },
 *     "reportList" : {
 *       "reports" : [ {
 *         "reportId" : "reRoll",
 *         "playerId" : "15602213",
 *         "reRollSource" : "Team ReRoll",
 *         "successful" : false,
 *         "roll" : 0
 *       }, {
 *         "reportId" : "block",
 *         "defenderId" : "15521055"
 *       }, {
 *         "reportId" : "blockRoll",
 *         "choosingTeamId" : "1128152",
 *         "blockRoll" : [ 5 ],
 *         "defenderId" : null
 *       } ]
 *     },
 *     "sound" : "block",
 *     "gameTime" : 272491,
 *     "turnTime" : 55809
 *   }
 */

object BlockChooseRerollMapper: CommandActionMapper {
    override fun isApplicable(
        game: FumbblGame,
        command: ServerCommandModelSync,
        processedCommands: MutableList<ServerCommandModelSync>
    ): Boolean {
        return (
            game.actingPlayer.playerAction == PlayerAction.BLOCK &&
                command.reportList.lastOrNull() is BlockRollReport &&
                command.reportList.firstOrNull() is ReRollReport &&
                (command.firstReport() as ReRollReport).reRollSource == "Team ReRoll" &&
                command.sound == "block"
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
        val rollReport = command.reportList.last() as BlockRollReport
        val rerollReport = command.reportList.first() as ReRollReport
//        if (rerollReport.reRollSource != "Team ReRoll") {
//            throw IllegalStateException("Unexpected re-roll source: " + rerollReport.reRollSource)
//        }
        newActions.add(
            action = { state: Game, rules: Rules ->
                StandardBlockChooseReroll.ReRollSourceOrAcceptRoll.getAvailableActions(state, rules).first {
                    (it is SelectRerollOption) && (it.option.source is RegularTeamReroll)
                }.let {
                    val option = it as SelectRerollOption
                    RerollOptionSelected(DiceRerollOption(option.option.source, option.option.dice))
                }
            },
            expectedNode = StandardBlockChooseReroll.ReRollSourceOrAcceptRoll
        )
        val diceRoll = rollReport.blockRoll.map { DBlockResult(it) }
//        newActions.add(BlockTypeSelected(BlockType.STANDARD), BlockAction.SelectBlockType)
        newActions.add(DiceRollResults(diceRoll), StandardBlockRerollDice.ReRollDie)
    }
}
