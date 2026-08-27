package com.jervisffb.engine.model.modifiers

import com.jervisffb.engine.model.Game
import com.jervisffb.engine.model.Player
import com.jervisffb.engine.model.PlayerId
import com.jervisffb.engine.rules.common.skills.Duration
import kotlinx.serialization.Serializable

@Serializable
sealed interface PlayerStatusEffect {
    val type: PlayerStatusEffectType
    val duration: Duration

}

@Serializable
data class SimplePlayerStatusEffect(
    override val type: PlayerStatusEffectType,
    override val duration: Duration,
) : PlayerStatusEffect

@Serializable
data class OwnedPlayerStatusEffect(
    override val type: PlayerStatusEffectType,
    override val duration: Duration,
    val causedBy: PlayerId,
) : PlayerStatusEffect {
    constructor(
        type: PlayerStatusEffectType,
        duration: Duration,
        causedBy: Player,
    ): this(type, duration, causedBy.id)

    fun getCausedBy(state: Game): Player {
        return state.getPlayerById(causedBy)
    }
}
