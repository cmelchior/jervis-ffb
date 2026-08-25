package com.jervisffb.engine.common.commands

import com.jervisffb.engine.commands.Command
import com.jervisffb.engine.model.Game
import com.jervisffb.engine.model.Team
import com.jervisffb.engine.model.inducements.card.SpecialPlayCard

class AddSpecialPlayCard(private val team: Team, val card: SpecialPlayCard) : Command {
    override fun execute(state: Game) {
        team.specialPlayCards.add(card)
    }

    override fun undo(state: Game) {
        team.specialPlayCards.remove(card)
    }
}
