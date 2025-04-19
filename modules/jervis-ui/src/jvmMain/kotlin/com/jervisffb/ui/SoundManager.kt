@file:OptIn(ExperimentalResourceApi::class)

package com.jervisffb.ui

import com.jervisffb.jervis_ui.generated.resources.Res
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kuusisto.tinysound.Sound
import kuusisto.tinysound.TinySound
import org.jetbrains.compose.resources.ExperimentalResourceApi
import java.net.URI

@Suppress("EXPECT_ACTUAL_CLASSIFIERS_ARE_IN_BETA_WARNING")
actual object SoundManager {

    private val sounds: MutableMap<SoundEffect, Sound> = mutableMapOf()

    actual suspend fun initialize() {
        TinySound.init()
        TinySound.setGlobalVolume(0.5)
        SoundEffect.entries.forEach {soundEffect ->
            val path = Res.getUri("files/fumbbl/sounds/${soundEffect.fileName}")
            val url = URI(path).toURL()
            val sound = TinySound.loadSound(url)
            sounds[soundEffect] = sound
        }
    }

    @OptIn(DelicateCoroutinesApi::class)
    actual fun play(sound: SoundEffect) {
        GlobalScope.launch(Dispatchers.Unconfined) {
            sounds[sound]?.play() ?: error("SoundEffect not found: $sound")
            delay(sound.lengthMs.toLong())
        }
    }
}
