package com.jervisffb.engine.bb2025.reports

import com.jervisffb.engine.bb2025.modifiers.PlayerStatusEffectType2025
import com.jervisffb.engine.model.Player
import com.jervisffb.engine.reports.LogCategory
import com.jervisffb.engine.reports.LogEntry

class ReportPlayerStatusEffect(player: Player, effect: PlayerStatusEffectType2025) : LogEntry() {
    override val category: LogCategory = LogCategory.GAME_PROGRESS
    override val message: String = buildString {
        append(player.name)
        when (effect) {
            PlayerStatusEffectType2025.DISTRACTED -> append("is Distracted")
            PlayerStatusEffectType2025.CHOMPED -> append("got Chomped")
            PlayerStatusEffectType2025.EYE_GOUGE -> append("got Eye Gouged")
            PlayerStatusEffectType2025.DODGY_SNACK -> append("got a Dodgy Snack")
            PlayerStatusEffectType2025.HANGOVER -> append("got a Hangover")
            PlayerStatusEffectType2025.DOPED -> append("is Doped")
            PlayerStatusEffectType2025.GRUDGE_MATCH -> append("goes into a Grudge Match")
            PlayerStatusEffectType2025.SET_PIECE -> append("feels like a Set Piece")
        }
    }
}
