package com.jervisffb.fumbbl.net.adapter

//import org.reflections.Reflections
import com.jervisffb.fumbbl.net.api.commands.ServerCommandModelSync
import com.jervisffb.fumbbl.net.utils.FumbblGame
import com.jervisffb.engine.model.Game
import kotlin.reflect.KClass

/**
 * Map all server commands into the equivalent [JervisAction] by using all configured
 * [CommandActionMapper]s.
 */
expect class MapperChain(jervisGame: Game, fumbblGame: FumbblGame, checkCommands: Boolean = false) {
    fun getMappersInPackage(): List<KClass<*>>
    fun process(commands: List<ServerCommandModelSync>): List<JervisActionHolder>
}
