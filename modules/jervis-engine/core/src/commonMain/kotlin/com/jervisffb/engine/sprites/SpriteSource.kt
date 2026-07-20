package com.jervisffb.engine.sprites

import kotlinx.serialization.Serializable

@Serializable
sealed interface SpriteSource {
    val type: SpriteLocation
    val resource: String
}
