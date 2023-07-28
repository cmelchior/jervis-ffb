package ilios.dk

import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.*
import okhttp3.*
import okio.ByteString
import okio.ByteString.Companion.encodeUtf8
import java.io.File
import java.util.concurrent.CountDownLatch

@JvmInline
value class JsonResponse(val json: String)

data class ServerResponse(val response: JsonResponse, val error: String? = null) {
    fun isSuccess(): Boolean = (error == null)
}

class WebSocketClient {

    private lateinit var webSocket: WebSocket
    private var responseLatch: CountDownLatch = CountDownLatch(1)
    private var error: String? = null
    private val response: MutableList<String> = mutableListOf()
    val json = Json { /*prettyPrint = true */ }

    fun start() {
        val client = OkHttpClient()
        val request = Request.Builder().url("ws://fumbbl.com:22223/command").build()

        webSocket = client.newWebSocket(request, object : WebSocketListener() {
            override fun onOpen(webSocket: WebSocket, response: Response) {
                println("WebSocket connection successful")
            }

            override fun onMessage(webSocket: WebSocket, text: String) {
                println("Received text message: $text")
            }

            override fun onMessage(webSocket: WebSocket, bytes: ByteString) {
                    val compressedResponse: String = bytes.utf8()
                    val jsonResponse: String? = LZString.decompressFromUTF16(compressedResponse)
                    if (jsonResponse != null) {
                        val responseObj: JsonElement = json.parseToJsonElement(jsonResponse)
                        val done: Boolean = responseObj.jsonObject["lastCommand"]?.jsonPrimitive?.booleanOrNull == true
                        val prettyJson: String = json.encodeToString(responseObj)
                        response.add(prettyJson)
                        if (done) {
                            responseLatch.countDown()
                        }
                    } else {
                        error = "Received `null` from the server"
                        responseLatch.countDown()
                    }
            }

            override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
                error = "WebSocket connection failure: $response, ${t.message}"
                responseLatch.countDown()
            }

            override fun onClosing(webSocket: WebSocket, code: Int, reason: String) {
                println("WebSocket connection closing")
            }
        })
    }

    fun sendAndReceiveMessage(message: String): ServerResponse {
        try {
            // FUMBBL Server only seem to accept compressed data.
            val msg: String = LZString.compressToUTF16(message)
            webSocket.send(msg.encodeUtf8())
            responseLatch.await()
        } catch (e: InterruptedException) {
            error = "Interrupted while waiting for response"
        } catch(e: Throwable) {
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


fun main(args: Array<String>) {
    if (args.isEmpty()) {
        System.out.println("Usage: java -jar game-download.jar <gameId> <optionalDirToSaveFile>")
        System.exit(0);
    }

    // val gameId = args[0].toInt() // e.g. 1602474
    val gameId = 1624379
    val saveDir = if (args.size == 2) File(args[1]) else File(System.getProperty("user.dir"))
    if (!saveDir.exists()) {
        saveDir.mkdirs()
    }

    val client = WebSocketClient()
    client.start()
    val message = """
        {"netCommandId":"clientReplay","gameId":$gameId,"replayToCommandNr":0,"coach":null}
    """.trimIndent()
    val result = client.sendAndReceiveMessage(message)
    if (result.isSuccess()) {
        val output = File(saveDir, "game-$gameId.json")
        output.writeText(result.response.json, charset = Charsets.UTF_8)
        println("Saved game to ${output.absolutePath}")
    } else {
        println("Error downloading $gameId:\n ${result.error}")
    }
}

