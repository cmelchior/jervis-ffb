package dk.ilios.jervis.rng

import okio.Buffer
import okio.HashingSink
import okio.blackholeSink

/**
 * Credit: https://github.com/christerk/ffb/blob/48cbbc770a0b6d9dee6a43244949a8894f10d1bf/ffb-server/src/main/java/com/fumbbl/ffb/server/util/rng/EntropyPool.java#L10
 * Original author: Christer Kaivo-oja
 *
 * Converted to Kotlin and replaced `MessageDigest` with okio which has multiplatform support (and uses MessageDigest) for
 * JVM platforms.
 *
 * This class is not thread safe.
 */
class EntropyPool {
    private val inputSource = Buffer()
    private val digest = HashingSink.sha256(blackholeSink())
    private var byteCount = 0

    fun addEntropy(data: Byte) {
        inputSource.writeByte(data.toInt())
        digest.write(inputSource, 1)
        byteCount++
    }

    /**
     * Returns the current entropy and reset the pool.
     * This method should not be called unless [hasEnoughEntropy] returns `true`.
     */
    fun getEntropy(): ByteArray {
        if (byteCount < ENTROPY_SIZE) {
            throw IllegalStateException("Not enough entropy: $byteCount")
        }
        byteCount = 0
        return digest.hash.toByteArray()
    }

    fun hasEnoughEntropy(): Boolean {
        return byteCount >= 32
    }

    companion object {
        private const val ENTROPY_SIZE = 32
    }
}
