package com.jervisffb.fumbbl.net.adapter

import com.jervisffb.fumbbl.net.api.commands.ServerCommandModelSync
import com.jervisffb.fumbbl.net.utils.FumbblGame
import com.jervisffb.engine.model.Game
import kotlin.reflect.KClass

/**
 * Map all server commands into the equivalent [JervisAction] by using all configured
 * [CommandActionMapper]s.
 */
@Suppress("EXPECT_ACTUAL_CLASSIFIERS_ARE_IN_BETA_WARNING")
actual class MapperChain actual constructor(jervisGame: Game, fumbblGame: FumbblGame, checkCommands: Boolean) {
    actual fun getMappersInPackage(): List<KClass<*>> { TODO() }
    actual fun process(commands: List<ServerCommandModelSync>): List<JervisActionHolder> { TODO() }
}
