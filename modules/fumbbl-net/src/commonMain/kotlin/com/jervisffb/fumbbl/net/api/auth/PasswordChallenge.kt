package com.jervisffb.fumbbl.net.api.auth

import io.ktor.utils.io.core.toByteArray
import okio.Buffer

/**
 * Copied and modified from the FUMBBL Java Client:
 * com.fumbbl.ffb.PasswordChallenge
 */
object PasswordChallenge {
    /**
     * Create the client response to the servers password challenge when joining a game.
     *
     * It looks something likes this:
     *
     * ```
     * {"netCommandId":"clientPasswordChallenge","coach":"cmelchior"}
     * {"netCommandId":"serverPasswordChallenge","commandNr":0,"challenge":"45b0f59c6da82c2dddf251b9c52bbf21"}
     * {"netCommandId":"clientJoin","clientMode":"player","coach":"cmelchior","password":"3ca38f9b443be2cd0c9dd267be4b4ac6","gameId":0,"gameName":"test:jervis-test","teamId":"1158751","teamName":"Team 1"}
     * ```
     *
     * If an auth token is provided at startup, this step is skipped and the auth token is used directly
     * in the "clientJoin" command.
     */
    fun createChallengeResponse(
        password: String,
        challenge: String,
    ): String {
        return createResponse(challenge, md5Encode(password.toByteArray()))
    }

    private fun createResponse(
        challenge: String,
        md5EncodedPassword: ByteArray,
    ): String {
        if (challenge.isNotEmpty()) {
            val challenge = fromHexString(challenge)
            val opad = xor(md5EncodedPassword, 92.toByte())
            val ipad = xor(md5EncodedPassword, 54.toByte())
            return toHexString(md5Encode(concat(opad, md5Encode(concat(ipad, challenge)))))
        }
        return toHexString(md5EncodedPassword)
    }

    private fun fromHexString(hexString: String): ByteArray {
        val len = hexString.length
        val data = ByteArray(len / 2)
        var i = 0
        while (i < len) {
            data[i / 2] =
                (
                    (hexString[i].digitToIntOrNull(16) ?: -1 shl 4) + hexString[i + 1].digitToIntOrNull(16)!!
                        ?: -1
                ).toByte()
            i += 2
        }
        return data
    }

    private fun toHexString(bytes: ByteArray): String {
        val hexString = StringBuilder()
        for (i in bytes.indices) {
            hexString.append(((bytes[i].toInt() and 0xFF) + 256).toString(16).substring(1))
        }
        return hexString.toString()
    }

    private fun concat(
        bytes1: ByteArray?,
        bytes2: ByteArray?,
    ): ByteArray {
        val size1 = if ((bytes1 != null)) bytes1.size else 0
        val size2 = if ((bytes2 != null)) bytes2.size else 0
        val result = ByteArray(size1 + size2)
        var i = 0
        while (i < size1) {
            result[i] = bytes1!![i]
            i++
        }
        i = 0
        while (i < size2) {
            result[i + size1] = bytes2!![i]
            i++
        }
        return result
    }

    private fun xor(
        bytes: ByteArray,
        mask: Byte,
    ): ByteArray {
        val result: ByteArray?
        if (bytes.isNotEmpty()) {
            result = ByteArray(bytes.size)
            for (i in result.indices) {
                result[i] = (bytes[i].toInt() xor mask.toInt()).toByte()
            }
        } else {
            result = ByteArray(0)
        }
        return result
    }

    private fun md5Encode(bytes: ByteArray): ByteArray {
        val buffer = Buffer()
        return buffer.write(bytes).md5().toByteArray()
    }
}
