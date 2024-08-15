package dk.ilios.jervis.fumbbl.adapter

import dk.ilios.jervis.fumbbl.ModelChangeProcessor
import dk.ilios.jervis.fumbbl.adapter.impl.AbortActionMapper
import dk.ilios.jervis.fumbbl.adapter.impl.BounceBallMapper
import dk.ilios.jervis.fumbbl.adapter.impl.EndTeamTurnMapper
import dk.ilios.jervis.fumbbl.adapter.impl.KickOffAndScatterMapper
import dk.ilios.jervis.fumbbl.adapter.impl.move.EndMoveVariant1Mapper
import dk.ilios.jervis.fumbbl.adapter.impl.move.EndMoveVariant2Mapper
import dk.ilios.jervis.fumbbl.adapter.impl.move.MovePlayerMapper
import dk.ilios.jervis.fumbbl.adapter.impl.setup.EndDefenseSetupMapper
import dk.ilios.jervis.fumbbl.adapter.impl.setup.EndOffenseSetupMapper
import dk.ilios.jervis.fumbbl.adapter.impl.setup.KickoffRollMapper
import dk.ilios.jervis.fumbbl.adapter.impl.setup.PitchInvasionMapper
import dk.ilios.jervis.fumbbl.adapter.impl.setup.RollFanFactorMapper
import dk.ilios.jervis.fumbbl.adapter.impl.setup.SelectToKickoffOrReceiveMapper
import dk.ilios.jervis.fumbbl.adapter.impl.setup.SetupPlayerMapper
import dk.ilios.jervis.fumbbl.adapter.impl.setup.ThrowCoinMapper
import dk.ilios.jervis.fumbbl.adapter.impl.setup.WeatherRollMapper
import dk.ilios.jervis.fumbbl.net.commands.ServerCommandModelSync
import dk.ilios.jervis.fumbbl.utils.FumbblGame
import dk.ilios.jervis.model.Game

/**
 * Map all server commands into the equivalent [JervisAction] by using all configured
 * [CommandActionMapper]s.
 */
class MapperChain(private val jervisGame: Game, private val fumbblGame: FumbblGame) {

    private val mappers = listOf(
        AbortActionMapper, // This must be first for some reason
        BounceBallMapper,
        EndDefenseSetupMapper,
        EndMoveVariant1Mapper,
        EndMoveVariant2Mapper,
        EndOffenseSetupMapper,
        EndTeamTurnMapper,
        KickOffAndScatterMapper,
        KickoffRollMapper,
        MovePlayerMapper,
        PitchInvasionMapper,
        RollFanFactorMapper,
        SelectToKickoffOrReceiveMapper,
        SetupPlayerMapper,
        ThrowCoinMapper,
        WeatherRollMapper,
    )

    fun process(commands: List<ServerCommandModelSync>): List<JervisActionHolder> {
        val actions = mutableListOf<JervisActionHolder>()
        val processedCommands = mutableSetOf<ServerCommandModelSync>()

        var i = 0
        while (i < commands.size) {
            val serverCommand: ServerCommandModelSync = commands[i]

            // Map CommandModelSync changes to Jervis actions using all configured mappers
            val mapper = mappers.firstOrNull { it: CommandActionMapper ->
                it.isApplicable(fumbblGame, serverCommand, processedCommands)
            }

            if (mapper != null) {
                val newActions = mutableListOf<JervisActionHolder>()
                mapper.mapServerCommand(fumbblGame, jervisGame, serverCommand, processedCommands, actions, newActions)
                actions.addAll(newActions)
            } else {
                reportNotHandled(serverCommand)
            }

            // Then update the FUMBBL State model
            serverCommand.modelChangeList.forEach {
                if (!ModelChangeProcessor.apply(fumbblGame, it)) {
                    throw IllegalStateException("Failed at: $it")
                }
            }
            i += 1
        }

        return actions
    }

    private fun reportNotHandled(command: ServerCommandModelSync): List<JervisActionHolder> {
        println("Not handling: $command")
        return emptyList()
    }
}
