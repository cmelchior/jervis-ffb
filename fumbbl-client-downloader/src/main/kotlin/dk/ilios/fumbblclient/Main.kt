import io.ktor.client.HttpClient
import io.ktor.client.engine.okhttp.OkHttp
import io.ktor.client.request.get
import io.ktor.client.statement.bodyAsChannel
import io.ktor.http.isSuccess
import io.ktor.util.cio.writeChannel
import io.ktor.utils.io.copyAndClose
import kotlinx.coroutines.runBlocking
import java.io.File

fun main(args: Array<String>) {
    val baseUrl = "https://fumbbl.com"
    val jnlpFileName = "ffblive.jnlp"
    val client = HttpClient(OkHttp)
    val root = File("./FantasyFootballClient-Debug")
    root.mkdirs()
    println("Downloading into: ${root.absolutePath}")
    runBlocking {
        val jnlpFile = File(root, jnlpFileName)
        val file = client.downloadFile(jnlpFile, "$baseUrl/$jnlpFileName")
        processJNLPFile(client, root, jnlpFile)
    }
    println("Done!")
}

suspend fun processJNLPFile(client: HttpClient, root: File, file: File) {
    val content = file.readText()
    val findBaseUrl = Regex("codebase=\"(.*?)\"")
    val baseUrl = findBaseUrl.find(content)!!.groups[1]!!.value
    val findJars: Regex = Regex("<jar href=\"(.*?)\"/>", RegexOption.MULTILINE)
    val matches: Sequence<MatchResult> = findJars.findAll(content)
    matches.forEach {
        val resourceFile = it.groups[1]!!.value
        println("Downloading: $resourceFile")
        client.downloadFile(File(root, resourceFile),"$baseUrl/$resourceFile")
    }
    val findMainClass = Regex("main-class=\"(.*?)\"")
    val mainClass = findMainClass.find(content)!!.groups[1]!!.value
    println("Usage: java -cp FantasyFootballClient.jar:* $mainClass -replay -gameId <gameId>")
    println("Debug usage: java -agentlib:jdwp=transport=dt_socket,server=y,suspend=y,address=127.0.0.1:8000 -cp FantasyFootballClient.jar:* $mainClass -replay -gameId <gameId>")
}

suspend fun HttpClient.downloadFile(targetFile: File, url: String) {
    if (targetFile.exists()) {
        targetFile.delete()
    }
    val call = this.get(url)
    if (!call.status.isSuccess()) {
        throw IllegalStateException("Cannot download: $url: ${call.status}")
    }
    call.bodyAsChannel().copyAndClose(targetFile.writeChannel())
}
