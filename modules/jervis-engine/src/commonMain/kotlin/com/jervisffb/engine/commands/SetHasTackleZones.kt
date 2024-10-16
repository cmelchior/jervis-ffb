package com.jervisffb.engine.commands

import com.jervisffb.engine.model.Game
import com.jervisffb.engine.model.Player

class SetHasTackleZones(private val player: Player, private val hasTackleZones: Boolean) : Command {
    private var originalValue: Boolean = false

    override fun execute(state: Game) {
        originalValue = player.hasTackleZones
        player.apply {
            this.hasTackleZones = this@SetHasTackleZones.hasTackleZones
            notifyUpdate()
        }
    }

    override fun undo(state: Game) {
        player.apply {
            this.hasTackleZones = originalValue
            notifyUpdate()
        }
    }
}
