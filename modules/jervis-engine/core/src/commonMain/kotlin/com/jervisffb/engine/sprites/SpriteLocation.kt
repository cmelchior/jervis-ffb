package com.jervisffb.engine.sprites

/**
 * Enum describing where a sprite is coming from.
 */
enum class SpriteLocation {
    // The sprite is included in the application bundle and is under `composeResources`.
    EMBEDDED,
    // The sprite is hosted on a remote server.
    URL,
    // The sprite is provided by FUMBBL and its remote location is defined by its ini file.
    FUMBBL_INI,
    // The Sprite is generated at runtime
    GENERATED,
}
