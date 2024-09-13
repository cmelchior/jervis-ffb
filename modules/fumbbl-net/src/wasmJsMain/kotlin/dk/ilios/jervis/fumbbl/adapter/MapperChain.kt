package dk.ilios.jervis.fumbbl.adapter

import dk.ilios.jervis.fumbbl.net.commands.ServerCommandModelSync
import dk.ilios.jervis.fumbbl.utils.FumbblGame
import dk.ilios.jervis.model.Game
import kotlin.reflect.KClass

/**
 * Map all server commands into the equivalent [JervisAction] by using all configured
 * [CommandActionMapper]s.
 */
actual class MapperChain actual constructor(jervisGame: Game, fumbblGame: FumbblGame, checkCommands: Boolean) {
    actual fun getMappersInPackage(): List<KClass<*>> { TODO() }
    actual fun process(commands: List<ServerCommandModelSync>): List<JervisActionHolder> { TODO() }
}
