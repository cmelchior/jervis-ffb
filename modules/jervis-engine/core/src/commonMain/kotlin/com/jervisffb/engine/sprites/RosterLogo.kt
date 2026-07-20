package com.jervisffb.engine.sprites

import kotlinx.serialization.Serializable

@Serializable
data class RosterLogo(
    val large: SingleSprite?, // Should be an image 600x600px
    val small: SingleSprite?, // Should be an image 200x200px
) {
    companion object {
        val NONE: RosterLogo = RosterLogo(null, null)
    }
}
