package com.jervisffb.engine.commands

import com.jervisffb.engine.model.Game
import com.jervisffb.engine.model.Player
import com.jervisffb.engine.model.Team

class AddPlayerToTeam(private val team: Team, private val player: Player) : Command {
    override fun execute(state: Game) {
        team.add(player)
    }

    override fun undo(state: Game) {
        team.noToPlayer.remove(player.number)
    }
}
