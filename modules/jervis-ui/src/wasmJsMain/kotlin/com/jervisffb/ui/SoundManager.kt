@file:OptIn(ExperimentalResourceApi::class)

package com.jervisffb.ui

import com.jervisffb.jervis_ui.generated.resources.Res
import org.jetbrains.compose.resources.ExperimentalResourceApi

/**
 * We cannot create dynamic JS code in WASM, so we need to delegate playing sounds
 * back to a JS function defined in the `index.html`.
 */
@JsFun("playSound")
external fun playSound(filePath: String)

@Suppress("EXPECT_ACTUAL_CLASSIFIERS_ARE_IN_BETA_WARNING")
actual object SoundManager {

    private val sounds: MutableMap<SoundEffect, String> = mutableMapOf()

    actual suspend fun initialize() {
        SoundEffect.entries.forEach { soundEffect ->
            val uri = Res.getUri("files/sounds/${soundEffect.fileName}")
            sounds[soundEffect] = uri
        }
    }

    actual fun play(sound: SoundEffect) {
        playSound(sounds[sound]!!)
    }
}
