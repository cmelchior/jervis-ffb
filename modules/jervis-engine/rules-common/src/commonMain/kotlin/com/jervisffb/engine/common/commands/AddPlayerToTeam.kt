package com.jervisffb.engine.common.commands

import com.jervisffb.engine.commands.Command
import com.jervisffb.engine.model.Game
import com.jervisffb.engine.model.Player
import com.jervisffb.engine.model.Team

class AddPlayerToTeam(private val team: Team, private val player: Player) : Command {
    var originalPlayerRemoved: Player? = null
    override fun execute(state: Game) {
        if (team.noToPlayer.contains(player.number)) {
            originalPlayerRemoved = team[player.number]
        }
        team.add(player)
    }

    override fun undo(state: Game) {
        team.noToPlayer.remove(player.number)
        if (originalPlayerRemoved != null) {
            team.add(originalPlayerRemoved!!)
        }
    }
}
