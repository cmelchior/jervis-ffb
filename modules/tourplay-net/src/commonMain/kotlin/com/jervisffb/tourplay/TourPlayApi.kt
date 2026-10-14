package com.jervisffb.tourplay

import com.jervisffb.engine.rules.Rules
import com.jervisffb.engine.rules.builder.GameVersion
import com.jervisffb.engine.serialization.FILE_FORMAT_VERSION
import com.jervisffb.engine.serialization.JervisMetaData
import com.jervisffb.engine.serialization.JervisTeamFile
import com.jervisffb.tourplay.api.TourPlayRoster
import com.jervisffb.utils.getHttpClient
import com.jervisffb.utils.jervisLogger
import io.ktor.client.HttpClient
import io.ktor.client.request.get
import io.ktor.client.statement.HttpResponse
import io.ktor.client.statement.bodyAsText
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.isSuccess
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import kotlinx.coroutines.yield
import kotlinx.serialization.json.Json
import okio.ByteString.Companion.decodeBase64
import okio.ByteString.Companion.encodeUtf8

/**
 * Wrapper around the TourPlay REST API: https://tourplay.net
 *
 * TourPlay protects roster requests with a proof-of-work cookie named `tp-pow`.
 * When a request has no accepted proof (e.g. the cookie is missing or expired),
 * the server responds with HTTP 403 and an `X-TP-PoW-Challenge` header. The
 * challenge contains a server-issued nonce, expiry timestamp, difficulty,
 * proof lifetime in minutes, and signature.
 *
 * [TourPlayProofOfWorkChallenge] decodes the challenge and searches for a
 * lowercase hexadecimal counter such that `SHA-256(nonce + counter)`, expressed
 * as hexadecimal, starts with `difficulty` zero characters. The original request
 * is then retried with `tp-pow=nonce.expires.difficulty.counter.signature` in its
 * Cookie header and an `X-TP-PoW-Retry` header indicating the retry count. At most
 * two retries are allowed; a 403 without a valid challenge remains a failure.
 *
 * The proof is cached per [TourPlayApi] instance and sent on the first request of
 * later roster loads, including loads of other rosters. A new instance must
 * obtain a challenge before it can produce a proof, since the nonce and signature
 * come from the server. Cached proofs are replaced when the server challenges
 * them again; this client does not calculate or persist their expiry.
 *
 * TourPlay's browser client sets its cookie expiry to
 * `challenge.expires + proofTtlMinutes * 60` (Unix seconds), using a 45-minute
 * fallback if no positive proof lifetime is supplied. The challenge inspected on
 * 2026-10-14 had an expiry five minutes ahead and a 15-minute proof lifetime,
 * giving the browser cookie roughly 20 minutes from issuance.
 *
 * [iconMapping] contains the mapping between TourPlay positions and FUMBBL
 * icons/portraits. The mapping is allowed to not be initialized, but will
 * result in all players using generated sprites and default portraits.
 */
class TourPlayApi(
    private val iconMapping: TourPlayIconMapping = TourPlayIconMapping(),
    private val client: HttpClient = getHttpClient(),
) {

    companion object {
        // Max number of retries when trying to answer the proof-of-work challenge
        const val MAX_RETRIES = 2
        val LOG = jervisLogger()
        val BASE_URL = "https://tourplay.net/api"
    }

    private val json = Json { ignoreUnknownKeys = true }
    private val proofMutex = Mutex()
    private var proofCookie: String? = null

    /**
     * Load a TourPlay Team and convert it to a Jervis, so it can be used
     * inside Jervis games.
     */
    suspend fun loadRoster(
        rosterId: Long,
        rules: Rules,
    ): Result<JervisTeamFile> {
        try {
            val rosterData = loadTeamFromTourPlay(rosterId)
            val mapper = when (rules.baseVersion) {
                GameVersion.BB2020 -> Mapper2020(iconMapping)
                GameVersion.BB2025 -> Mapper2025(iconMapping)
            }
            val jervisRoster = mapper.convertToJervisRoster(rules, rosterData)
            val jervisTeam = mapper.convertToJervisTeam(rules, jervisRoster, rosterData)
            return Result.success(JervisTeamFile(
                metadata = JervisMetaData(fileFormat = FILE_FORMAT_VERSION),
                team = jervisTeam,
                history = null,
            ))
        } catch (ex: CancellationException) {
            throw ex
        } catch (ex: Exception) {
            return Result.failure(ex)
        }
    }

    private suspend fun loadTeamFromTourPlay(teamId: Long): TourPlayRoster {
        val response = proofMutex.withLock {
            var response = requestTeam(teamId)
            var retries = 0
            // TourPlay's website answers a 403 challenge with a tp-pow cookie, then
            // retries at most twice. Keep the proof local to this API and reuse it
            // until the server asks for a fresh one.
            while (response.status == HttpStatusCode.Forbidden && retries < MAX_RETRIES) {
                val header = response.headers["X-TP-PoW-Challenge"] ?: break
                val challenge = TourPlayProofOfWorkChallenge.parse(header) ?: break
                proofCookie = challenge.solve()
                response = requestTeam(teamId, ++retries)
            }
            response
        }
        if (response.status.isSuccess()) {
            val rosterAsJson = response.bodyAsText()
            return json.decodeFromString<TourPlayRoster>(rosterAsJson)
        } else {
            throw IllegalStateException("Loading roster: $teamId failed with status: ${response.status}")
        }
    }

    private suspend fun requestTeam(teamId: Long, retry: Int = 0): HttpResponse {
        return client.get("$BASE_URL/rosters/$teamId") {
            with(headers) {
                append("Accept", "application/json, text/plain, */*")
                append("Referer", "https://tourplay.net/en/blood-bowl/roster/$teamId")
                append(
                    "User-Agent",
                    "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/135.0.0.0 Safari/537.36"
                )
                proofCookie?.let { append(HttpHeaders.Cookie, "tp-pow=$it") }
                if (retry > 0) append("X-TP-PoW-Retry", retry.toString())
            }
        }
    }
}

/**
 * Challenge decoder and SHA-256 solver adapted from
 * [TourPlay's browser JavaScript](https://tourplay.net/main.82584aff1aa7a3db.js),
 * inspected on 2026-10-13. The sources are the HTTP interceptor's
 * `readPowChallenge` method and the `solve` method of its `powSolverService`.
 * Search the bundle for `X-TP-PoW-Challenge` and `tp-pow` to locate them. The
 * alphabet, XOR key, cookie format, and maximum difficulty of six come from
 * that implementation. New deployments may use a different bundle filename.
 *
 * The current `v1` challenge reverses a string encoded with a rotated Base64
 * alphabet. Decoding restores the alphabet and byte order, separates interleaved
 * payload halves, and removes an XOR mask based on [XOR_KEY] and the byte index.
 * The resulting payload is
 * `v1.nonce.expires.difficulty.proofTtlMinutes.signature`. The website also accepts
 * a legacy format containing the same five fields in ordinary Base64, without
 * the `v1` prefix. The proof lifetime field affects browser cookie retention;
 * it is not included in the proof cookie sent back to the server.
 */
private data class TourPlayProofOfWorkChallenge(
    val nonce: String,
    val expires: Long,
    val difficulty: Int,
    val signature: String,
) {
    companion object {
        private const val BASE64_ALPHABET = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789+/"
        private const val CHALLENGE_ALPHABET = "defghijklmnopqrstuvwxyz0123456789+/ABCDEFGHIJKLMNOPQRSTUVWXYZabc"
        private val XOR_KEY = intArrayOf(
            58, 113, 196, 31, 139, 226, 71, 13, 153, 92, 166, 48, 219, 110, 241, 37,
            123, 24, 206, 67, 176, 90, 132, 47, 247, 12, 105, 211, 61, 161, 86, 232,
        )
        private val COOKIE_VALUE = Regex("[A-Za-z0-9_+/=-]+")

        fun parse(header: String): TourPlayProofOfWorkChallenge? {
            val decoded = decodeObfuscated(header) ?: header.decodeBase64()?.utf8() ?: return null
            val parts = decoded.split('.')
            if (parts.size != 5) return null
            val expires = parts[1].toLongOrNull() ?: return null
            val difficulty = parts[2].toIntOrNull() ?: return null
            // The website also caps difficulty at six leading hexadecimal zeros.
            if (expires <= 0 || difficulty !in 1..6) return null
            if (!COOKIE_VALUE.matches(parts[0]) || !COOKIE_VALUE.matches(parts[4])) return null
            return TourPlayProofOfWorkChallenge(parts[0], expires, difficulty, parts[4])
        }

        private fun decodeObfuscated(header: String): String? {
            val base64 = buildString {
                for (char in header.reversed()) {
                    if (char == '=') {
                        append(char)
                        continue
                    }
                    val index = CHALLENGE_ALPHABET.indexOf(char)
                    if (index < 0) return null
                    append(BASE64_ALPHABET[index])
                }
            }
            val shuffled = base64.decodeBase64()?.toByteArray() ?: return null
            val decoded = ByteArray(shuffled.size)
            val half = (shuffled.size + 1) / 2
            for (index in shuffled.indices) {
                val target = when (index % 2) {
                    0 -> index / 2
                    else -> half + index / 2
                }
                decoded[target] = (shuffled[index].toInt() xor XOR_KEY[target % XOR_KEY.size] xor ((93 * target) and 255)).toByte()
            }
            val payload = decoded.decodeToString()
            return payload.takeIf { it.startsWith("v1.") }?.removePrefix("v1.")
        }
    }

    suspend fun solve(): String = withContext(Dispatchers.Default) {
        val prefix = "0".repeat(difficulty)
        var counter = 0L
        var solution = counter.toString(16)
        yield()
        while (!(nonce + solution).encodeUtf8().sha256().hex().startsWith(prefix)) {
            counter++
            solution = counter.toString(16)
            // Keep cancellation and the browser event loop responsive while hashing.
            if (counter % 4096 == 0L) yield()
        }
        "$nonce.$expires.$difficulty.$solution.$signature"
    }
}
