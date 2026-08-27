package com.jervisffb.engine.common.reports

import com.jervisffb.engine.common.modifiers.PlayerStatusEffectTypeCommon
import com.jervisffb.engine.model.Player
import com.jervisffb.engine.reports.LogCategory
import com.jervisffb.engine.reports.LogEntry

class ReportPlayerStatusEffect(player: Player, effect: PlayerStatusEffectTypeCommon) : LogEntry() {
    override val category: LogCategory = LogCategory.GAME_PROGRESS
    override val message: String = buildString {
        append(player.name)
        when (effect) {
            PlayerStatusEffectTypeCommon.BANNED -> append("is Banned")
            PlayerStatusEffectTypeCommon.BLOOD_LUST -> append("goes into a Blood Lust")
            PlayerStatusEffectTypeCommon.FAINTED -> append("is Fainted due to the Heat")
            PlayerStatusEffectTypeCommon.HYPNOTIC_GAZE -> append("is affected by Hypnotic Gaze")
            PlayerStatusEffectTypeCommon.ROOTED -> append("is Rooted")
        }
    }
}
