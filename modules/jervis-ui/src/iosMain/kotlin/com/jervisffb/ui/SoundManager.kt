package com.jervisffb.ui

import com.jervisffb.jervis_ui.generated.resources.Res
import com.jervisffb.utils.jervisLogger
import kotlinx.cinterop.BetaInteropApi
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.memScoped
import kotlinx.cinterop.toCValues
import org.jetbrains.compose.resources.ExperimentalResourceApi
import platform.AVFAudio.AVAudioPlayer
import platform.Foundation.NSData
import platform.Foundation.create

@ExperimentalResourceApi
@Suppress("EXPECT_ACTUAL_CLASSIFIERS_ARE_IN_BETA_WARNING")
actual object SoundManager {
    private val LOG = jervisLogger()
    private val sounds: MutableMap<SoundEffect, NSData> = mutableMapOf()

    actual suspend fun initialize() {
        SoundEffect.entries.forEach { soundEffect ->
            val soundBytes = Res.readBytes("files/sounds/${soundEffect.fileName}")
            val data = soundBytes.toNSData()
            sounds[soundEffect] = data
        }
    }

    @ExperimentalForeignApi
    actual fun play(sound: SoundEffect) {
        try {
            val url = sounds[sound] ?: error("SoundEffect not found: $sound")
            val player = AVAudioPlayer(data = url, error = null)
            player.play()
        } catch (ex: Throwable) {
            LOG.e("Error playing sound (${sound.name}: ex.message: ${ex.message}")
        }
    }
}

@OptIn(ExperimentalForeignApi::class, BetaInteropApi::class)
private fun ByteArray.toNSData(): NSData = memScoped {
    return NSData.create(
        bytes = toCValues().getPointer(this),
        length = size.toULong()
    )
}
