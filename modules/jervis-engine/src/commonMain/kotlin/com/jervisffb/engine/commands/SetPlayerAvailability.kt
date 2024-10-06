package com.jervisffb.engine.commands

import com.jervisffb.engine.controller.GameController
import com.jervisffb.engine.model.Availability
import com.jervisffb.engine.model.Game
import com.jervisffb.engine.model.Player

class SetPlayerAvailability(private val player: Player, val availability: Availability) : Command {
    private lateinit var originalAvailability: Availability

    override fun execute(
        state: Game,
        controller: GameController,
    ) {
        this.originalAvailability = player.available
        player.apply {
            available = availability
            notifyUpdate()
        }
    }

    override fun undo(
        state: Game,
        controller: GameController,
    ) {
        player.apply {
            available = originalAvailability
            notifyUpdate()
        }
    }
}
