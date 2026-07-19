package com.jervisffb.engine.commands

import com.jervisffb.engine.model.Game
import com.jervisffb.engine.model.Team
import com.jervisffb.engine.model.inducements.MortuaryAssistant

class SetMortuaryAssistantUsed(
    private val team: Team,
    private val assistant: MortuaryAssistant,
    private val used: Boolean
) : Command {
    private var originalValue: Boolean = false

    override fun execute(state: Game) {
        originalValue = assistant.used
        assistant.used = used
    }

    override fun undo(state: Game) {
        assistant.used = originalValue
    }
}
