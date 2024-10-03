//package dk.ilios.jervis.fumbbl.adapter.impl.block
//
//import dk.ilios.jervis.actions.DBlockResult
//import dk.ilios.jervis.actions.DiceRollResults
//import dk.ilios.jervis.fumbbl.adapter.CommandActionMapper
//import dk.ilios.jervis.fumbbl.adapter.JervisActionHolder
//import dk.ilios.jervis.fumbbl.adapter.add
//import dk.ilios.jervis.fumbbl.model.PlayerAction
//import dk.ilios.jervis.fumbbl.model.reports.BlockRollReport
//import dk.ilios.jervis.fumbbl.model.reports.ReRollReport
//import dk.ilios.jervis.fumbbl.net.commands.ServerCommandModelSync
//import dk.ilios.jervis.fumbbl.utils.FumbblGame
//import dk.ilios.jervis.model.Game
//import dk.ilios.jervis.procedures.actions.block.standard.StandardBlockRerollDice
//
//object BlockReRollMapper: CommandActionMapper {
//    override fun isApplicable(
//        game: FumbblGame,
//        command: ServerCommandModelSync,
//        processedCommands: MutableList<ServerCommandModelSync>
//    ): Boolean {
//        return (
//            game.actingPlayer.playerAction == PlayerAction.BLOCK &&
//            command.reportList.lastOrNull() is BlockRollReport &&
//            command.reportList.firstOrNull() is ReRollReport &&
//            command.sound == "block"
//        )
//    }
//
//    override fun mapServerCommand(
//        fumbblGame: dk.ilios.jervis.fumbbl.model.Game,
//        jervisGame: Game,
//        command: ServerCommandModelSync,
//        processedCommands: MutableList<ServerCommandModelSync>,
//        jervisCommands: List<JervisActionHolder>,
//        newActions: MutableList<JervisActionHolder>
//    ) {
//        val rollReport = command.reportList.last() as BlockRollReport
//        val diceRoll = rollReport.blockRoll.map { DBlockResult(it) }
//        newActions.add(DiceRollResults(diceRoll), StandardBlockRerollDice.ReRollDie)
//    }
//}
