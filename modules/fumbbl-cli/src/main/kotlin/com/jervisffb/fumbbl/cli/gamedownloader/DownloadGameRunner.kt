package com.jervisffb.fumbbl.cli.gamedownloader

import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.booleanOrNull
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import okhttp3.WebSocket
import okhttp3.WebSocketListener
import okio.ByteString
import okio.ByteString.Companion.encodeUtf8
import java.io.File
import java.util.concurrent.CountDownLatch

@JvmInline
value class JsonResponse(val json: String)

data class ServerResponse(val response: JsonResponse, val error: String? = null) {
    fun isSuccess(): Boolean = (error == null)
}

class DownloadGameRunner(private val prettyPrint: Boolean) {
    private lateinit var webSocket: WebSocket
    private var client: OkHttpClient? = null
    private var responseLatch: CountDownLatch = CountDownLatch(1)
    private var error: String? = null
    private val response: MutableList<String> = mutableListOf()
    val json = Json { prettyPrint = this@DownloadGameRunner.prettyPrint }

    fun run(
        gameId: String,
        outputDir: File,
    ) {
        val gameId = gameId.toLong()
        outputDir.let {
            if (!it.exists()) {
                it.mkdirs()
            }
        }
        client = OkHttpClient()
        start()
        val message =
            """
            {"netCommandId":"clientReplay","gameId":$gameId,"replayToCommandNr":0,"coach":null}
            """.trimIndent()
        val result = sendAndReceiveMessage(message)
        println("") // Account for progress dots not ending with a newline
        if (result.isSuccess()) {
            val output = File(outputDir, "game-$gameId.json")
            output.writeText(result.response.json, charset = Charsets.UTF_8)
            println("Saved game to ${output.absolutePath}")
        } else {
            println(result.error)
        }
    }

    fun start() {
        val request = Request.Builder().url("ws://fumbbl.com:22223/command").build()
        webSocket =
            client!!.newWebSocket(
                request,
                object : WebSocketListener() {
                    override fun onOpen(
                        webSocket: WebSocket,
                        response: Response,
                    ) {
                        println("WebSocket connected successfully")
                    }

                    override fun onMessage(
                        webSocket: WebSocket,
                        text: String,
                    ) {
                        println("Received unexpected text message: $text")
                    }

                    override fun onMessage(
                        webSocket: WebSocket,
                        bytes: ByteString,
                    ) {
                        print(".")
                        System.out.flush()
                        try {
                            val compressedResponse: String = bytes.utf8()
                            val jsonResponse: String? = LZString.decompressFromUTF16(compressedResponse)
                            if (jsonResponse != null) {
                                val responseObj: JsonElement = json.parseToJsonElement(jsonResponse)

                                // Check if server could not return a replay
                                if (
                                    responseObj.jsonObject["netCommandId"]?.jsonPrimitive?.contentOrNull == "serverStatus"
                                    && responseObj.jsonObject["serverStatus"]?.jsonPrimitive?.contentOrNull == "Replay Unavailable"
                                ) {
                                    error = "Replay unavailable"
                                    responseLatch.countDown()
                                } else {
                                    val done: Boolean = responseObj.jsonObject["lastCommand"]?.jsonPrimitive?.booleanOrNull == true
                                    val prettyJson: String = json.encodeToString(responseObj)
                                    response.add(prettyJson)
                                    if (done) {
                                        responseLatch.countDown()
                                    }
                                }
                            } else {
                                error = "Received `null` from the server"
                                responseLatch.countDown()
                            }
                        } catch (e: Throwable) {
                            error = "Unexpected error while downloading game:\n${e.stackTraceToString()}"
                            responseLatch.countDown()
                        }
                    }

                    override fun onFailure(
                        webSocket: WebSocket,
                        t: Throwable,
                        response: Response?,
                    ) {
                        error = "WebSocket connection failure: $response, ${t.message}"
                        responseLatch.countDown()
                    }

                    override fun onClosed(webSocket: WebSocket, code: Int, reason: String) {
                        client?.dispatcher?.let {
                            it.cancelAll()
                            it.executorService.shutdown()
                        }
                        client?.connectionPool?.evictAll()

                    }
                },
            )
    }

    fun sendAndReceiveMessage(message: String): ServerResponse {
        try {
            // FUMBBL Server only seem to accept compressed data.
            val msg: String = LZString.compressToUTF16(message)
            webSocket.send(msg.encodeUtf8())
            responseLatch.await()
        } catch (e: InterruptedException) {
            error = "Interrupted while waiting for response"
        } catch (e: Throwable) {
            error = e.stackTraceToString()
        } finally {
            close()
        }
        val jsonOutput = "[\n${response.joinToString(",\n")}\n]"
        return ServerResponse(JsonResponse((jsonOutput)), error)
    }

    private fun close() {
        webSocket.close(NORMAL_CLOSURE_STATUS, "Done")
    }

    companion object {
        // See https://www.rfc-editor.org/rfc/rfc6455#section-7.4
        private const val NORMAL_CLOSURE_STATUS = 1000
    }
}
