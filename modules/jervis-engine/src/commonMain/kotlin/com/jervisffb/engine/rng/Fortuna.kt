package com.jervisffb.engine.rng

import com.jervisffb.engine.actions.D12Result
import com.jervisffb.engine.actions.D16Result
import com.jervisffb.engine.actions.D20Result
import com.jervisffb.engine.actions.D2Result
import com.jervisffb.engine.actions.D3Result
import com.jervisffb.engine.actions.D4Result
import com.jervisffb.engine.actions.D6Result
import com.jervisffb.engine.actions.D8Result
import com.jervisffb.engine.actions.DBlockResult
import com.jervisffb.engine.actions.Dice
import com.jervisffb.engine.actions.DieResult
import com.jervisffb.engine.utils.assert
import dev.whyoleg.cryptography.CryptographyProvider
import dev.whyoleg.cryptography.algorithms.symmetric.AES
import dev.whyoleg.cryptography.operations.cipher.Cipher
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlin.experimental.xor

/**
 * https://github.com/christerk/ffb/blob/48cbbc770a0b6d9dee6a43244949a8894f10d1bf/ffb-server/src/main/java/com/fumbbl/ffb/server/util/rng/Fortuna.java
 * Original author: Christer Kaivo-oja
 *
 * Converted to Kotlin and replaced crypto implementation with cryptography-kotlin which has multiplatform support and uses the same
 * JVM classes under the hood.
 *
 * This class is not thread safe (yet).
 *
 * See https://en.wikipedia.org/wiki/Fortuna_(PRNG) for details on this algorithm.
 */
class Fortuna: DiceRollGenerator {
    private val pools: Array<EntropyPool?>
    private var currentPool: Int
    private var poolSelector: Int
    private val algorithm = CryptographyProvider.Default.get(AES.CTR)
    private lateinit var cipher: Cipher
    private val nonce: ByteArray
    private var randomData: ByteArray = byteArrayOf()
    private var byteOffset = 0
    private var lastRekeying: Instant = Instant.DISTANT_PAST
    private var rekeyings: Long = 0
    private var numberOfBytes: Long = 0

    init {
        pools = arrayOfNulls(NUMBER_OF_POOLS)
        for (i in 0 until NUMBER_OF_POOLS) {
            pools[i] = EntropyPool()
        }
        currentPool = NUMBER_OF_POOLS - 1
        poolSelector = 1

        nonce = byteArrayOf(
            0x4E.toByte(),
            0xC1.toByte(),
            0x37.toByte(),
            0xA4.toByte(),
            0x26.toByte(),
            0xDA.toByte(),
            0xBF.toByte(),
            0x8A.toByte(),
            0xA0.toByte(),
            0xBE.toByte(),
            0xB8.toByte(),
            0xBC.toByte(),
            0x0C.toByte(),
            0x2B.toByte(),
            0x89.toByte(),
            0xD6.toByte()
        )
    }

    suspend fun init() {
        val key = byteArrayOf(
            0x95.toByte(),
            0xA8.toByte(),
            0xEE.toByte(),
            0x8E.toByte(),
            0x89.toByte(),
            0x97.toByte(),
            0x9B.toByte(),
            0x9E.toByte(),
            0xFD.toByte(),
            0xCB.toByte(),
            0xC6.toByte(),
            0xEB.toByte(),
            0x97.toByte(),
            0x97.toByte(),
            0x52.toByte(),
            0x8D.toByte(),
            0x43.toByte(),
            0x2D.toByte(),
            0xC2.toByte(),
            0x60.toByte(),
            0x61.toByte(),
            0x55.toByte(),
            0x38.toByte(),
            0x18.toByte(),
            0xEA.toByte(),
            0x63.toByte(),
            0x5E.toByte(),
            0xC5.toByte(),
            0xD5.toByte(),
            0xA7.toByte(),
            0x72.toByte(),
            0x7E.toByte()
        )

        for (i in key.indices) {
            val l: Long = Clock.System.now().nanosecondsOfSecond.toLong()
            val b = (l and 0xFF).toByte()
            val threadId = 0.toLong() // TODO Not sure this is relevant (java.lang.Thread.currentThread().getId()
            key[i] = (key[i] xor (b xor (threadId and 0xFFL).toByte()))
        }

        rekeyGenerator(key)
        generateRandomData()
        rekeyGenerator(randomData)
        generateRandomData()
    }

    fun displayStats() {
        println("Rekeyings:         " + rekeyings)
        println("Bytes fetched:     $numberOfBytes")
        println("Bits per rekeying: " + (8 * numberOfBytes) / rekeyings)
    }

    private val byte: Int
        get() {
            numberOfBytes++
            val result = randomData[byteOffset].toInt()
            byteOffset++
            if (byteOffset >= 16) generateRandomData()
            return result and 0xff
        }

    override fun rollDie(die: Dice): DieResult {
        return when (die) {
            Dice.D2 -> D2Result(getDieRoll(max = 2))
            Dice.D3 -> D3Result(getDieRoll(max = 3))
            Dice.D4 -> D4Result(getDieRoll(max = 4))
            Dice.D6 -> D6Result(getDieRoll(max = 6))
            Dice.D8 -> D8Result(getDieRoll(max = 8))
            Dice.D12 -> D12Result(getDieRoll(max = 12))
            Dice.D16 -> D16Result(getDieRoll(max = 16))
            Dice.D20 -> D20Result(getDieRoll(max = 20))
            Dice.BLOCK -> DBlockResult(getDieRoll(max = 6))
        }
    }

    private fun getDieRoll(max: Int): Int {
        var result: Int
        assert(max < 256 && max > 0)
        do {
            result = byte
        } while (result >= 256 - (256 % max))
        return 1 + (result % max)
    }

    // @Synchronized
    suspend fun rekeyGenerator(newKey: ByteArray) {
        lastRekeying = Clock.System.now()
        rekeyings++
        cipher = algorithm.keyDecoder().decodeFrom(format = AES.Key.Format.RAW, input = newKey).cipher()
    }

    private fun generateRandomData() {
        randomData = cipher.encryptBlocking(nonce)
        for (i in nonce.indices.reversed()) {
            nonce[i]++
            if (nonce[i].toInt() != 0) break
        }
        byteOffset = 0
    }

    // @Synchronized
    suspend fun addEntropy(data: Byte) {
        pools[currentPool]!!.addEntropy(data)
        currentPool--
        if (currentPool < 0) {
            currentPool += NUMBER_OF_POOLS
            val now = Clock.System.now().toEpochMilliseconds()
            if (pools[0]!!.hasEnoughEntropy() && (now - lastRekeying.toEpochMilliseconds()) > MAX_REKEY_DELAY_MS) {
                val newKey = pools[0]!!.getEntropy()
                for (i in 1 until NUMBER_OF_POOLS) {
                    if ((poolSelector and (1 shl i)) != 0) {
                        val entropy = pools[i]!!.getEntropy()
                        for (j in entropy.indices) newKey[j] = (newKey[j].toInt() xor entropy[j].toInt()).toByte()
                    }
                }
                rekeyGenerator(newKey)
                poolSelector++
            }
        }
    }

    companion object {
        private const val NUMBER_OF_POOLS = 32
        private const val MAX_REKEY_DELAY_MS: Long = 1000
    }

}
