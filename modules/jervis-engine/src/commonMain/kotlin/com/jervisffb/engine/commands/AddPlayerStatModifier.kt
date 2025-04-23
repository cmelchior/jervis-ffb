package com.jervisffb.engine.commands

import com.jervisffb.engine.model.Game
import com.jervisffb.engine.model.Player
import com.jervisffb.engine.model.modifiers.StatModifier

class AddPlayerStatModifier(private val player: Player, val modifier: StatModifier) : Command {
    override fun execute(state: Game) {
        player.apply {
            addStatModifier(modifier)
            player.team.game.rules.updatePlayerStat(player, modifier)
        }
    }

    override fun undo(state: Game) {
        player.apply {
            removeStatModifier(modifier)
            player.team.game.rules.updatePlayerStat(player, modifier)
        }
    }
}
