package com.jervisffb.engine.commands

import com.jervisffb.engine.model.Game
import com.jervisffb.engine.model.Player
import com.jervisffb.engine.model.PlayerKeyword

class AddPlayerKeyword(private val player: Player, val keyword: PlayerKeyword) : Command {
    override fun execute(state: Game) {
        player.keywords.add(keyword)
    }

    override fun undo(state: Game) {
        player.keywords.remove(keyword)
    }
}
