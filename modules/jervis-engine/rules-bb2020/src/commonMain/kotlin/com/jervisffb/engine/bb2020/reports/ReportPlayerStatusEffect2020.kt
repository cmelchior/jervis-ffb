package com.jervisffb.engine.bb2020.reports

import com.jervisffb.engine.bb2020.modifiers.PlayerStatusEffectType2020
import com.jervisffb.engine.model.Player
import com.jervisffb.engine.reports.LogCategory
import com.jervisffb.engine.reports.LogEntry

class ReportPlayerStatusEffect(player: Player, effect: PlayerStatusEffectType2020) : LogEntry() {
    override val category: LogCategory = LogCategory.GAME_PROGRESS
    override val message: String = buildString {
        append(player.name)
        append(" ")
        when (effect) {
            PlayerStatusEffectType2020.BONE_HEAD -> append("feels Bone Headed")
            PlayerStatusEffectType2020.REALLY_STUPID -> append("feels Really Stupid")
            PlayerStatusEffectType2020.UNCHANNELLED_FURY -> append("goes into an Unchannelled Fury")
        }
    }
}
