package com.jervisffb.fumbbl.netcli.debugclient

import io.ktor.client.HttpClient
import io.ktor.client.engine.okhttp.OkHttp
import io.ktor.client.request.get
import io.ktor.client.statement.bodyAsChannel
import io.ktor.http.isSuccess
import io.ktor.util.cio.writeChannel
import io.ktor.utils.io.copyAndClose
import javassist.ClassPool
import javassist.expr.ExprEditor
import javassist.expr.MethodCall
import kotlinx.coroutines.runBlocking
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import java.nio.file.StandardCopyOption
import java.util.jar.JarEntry
import java.util.jar.JarFile
import java.util.jar.JarInputStream
import java.util.jar.JarOutputStream
import java.util.jar.Manifest
import java.util.stream.Collectors
import kotlin.io.path.name

/**
 * Create
 */
class CreateDebugClientRunner(private val cliJarFile: File) {
    fun run(jarFolder: File) {
        val baseUrl = "https://fumbbl.com"
        val jnlpFileName = "ffblive.jnlp"
        val client = HttpClient(OkHttp)
        val root = jarFolder
        logInfo("Downloading FUMBBL Client data into: ${root.absolutePath}")
        root.mkdirs()
        runBlocking {
            val jnlpFile = File(root, jnlpFileName)
            client.downloadFile(jnlpFile, "$baseUrl/$jnlpFileName")
            processJNLPFile(client, root, jnlpFile)
        }
        injectDebugCode(File(root, "FantasyFootballClient.jar"))
        client.close()
        logInfo("Done!")
    }

    /**
     * Go through the JNLP file and download all resources it mentions, so we can run the code locally.
     */
    suspend fun processJNLPFile(
        client: HttpClient,
        root: File,
        file: File,
    ) {
        val content: String = file.readText()
        val findBaseUrl = Regex("codebase=\"(.*?)\"")
        val baseUrl: String = findBaseUrl.find(content)!!.groups[1]!!.value
        val findJars = Regex("<jar href=\"(.*?)\"/>", RegexOption.MULTILINE)
        val matches: Sequence<MatchResult> = findJars.findAll(content)
        matches.forEach {
            val resourceFile = it.groups[1]!!.value.trim().removeSuffix("&#10;")
            logInfo("Downloading resource: $resourceFile")
            client.downloadFile(File(root, resourceFile), "$baseUrl/$resourceFile")
        }
        val findMainClass = Regex("main-class=\"(.*?)\"")
        val mainClass: String = findMainClass.find(content)!!.groups[1]!!.value
        logInfo("Usage: java -cp FantasyFootballClient.jar:* $mainClass -replay -gameId <gameId>")
        logInfo(
            "Debug usage: java -agentlib:jdwp=transport=dt_socket,server=y,suspend=y,address=127.0.0.1:8000 -cp FantasyFootballClient.jar:* $mainClass -replay -gameId <gameId>",
        )
    }

    /**
     * Helper function for easily downloading a file using HTTP GET.
     */
    suspend fun HttpClient.downloadFile(
        targetFile: File,
        url: String,
    ) {
        if (targetFile.exists()) {
            targetFile.delete()
        }
        val call = this.get(url)
        if (!call.status.isSuccess()) {
            throw IllegalStateException("Cannot download: $url: ${call.status}")
        }
        call.bodyAsChannel().copyAndClose(targetFile.writeChannel())
    }

    /**
     * Modify the network code in the FUMBBL Client so it redirect content
     * to our own FumbblDebug class which allow us to capture it while a real
     * client is running.
     *
     * @return Map of <path/to/class/file/in/jar, path/to/new/file/on/disk>
     */
    fun createClassesWithDebugCode(fumbblClientJar: File): Map<String, File> {
        val cp = ClassPool.getDefault()
        if (!fumbblClientJar.exists()) {
            throw IllegalStateException("Fumbbl client jar is not found: $fumbblClientJar.a")
        }
        cp.insertClassPath(fumbblClientJar.absolutePath)
        val cc = cp["com.fumbbl.ffb.client.net.CommandEndpoint"]

        /**
         * This is the method template we want to modify:
         *
         *    public boolean send(NetCommand pCommand) throws IOException {
         *      if (pCommand == null || !isOpen()) {
         *        return false;
         *      }
         *      JsonValue jsonValue = pCommand.toJsonValue();
         *      <InjectionPointHere>
         *      if (jsonValue == null) {
         *        return false;
         *      }
         *      String textMessage = jsonValue.toString();
         *      if (this.fCommandCompression) {
         *        textMessage = LZString.compressToUTF16(textMessage);
         *      }
         *      if (!StringTool.isProvided(textMessage)) {
         *        return false;
         *      }
         *      this.fSession.getAsyncRemote().sendBinary(ByteBuffer.wrap(textMessage.getBytes(StandardCharsets.UTF_8)));
         *      return true;
         *    }
         */
        val sendMethod = cc.getDeclaredMethod("send")
        sendMethod.instrument(
            object : ExprEditor() {
                override fun edit(m: MethodCall) {
                    if (m.methodName.equals("toJsonValue")) {
                        m.replace(
                            "\$_ = \$proceed($$); com.jervisffb.fumbbl.net.FumbblDebugger.handleClientMessage(\$_);",
                        )
                    }
                }
            },
        )

        /**
         * This is the method template we want to modify:
         *
         *    @OnMessage
         *    public void onMessage(String pTextMessage) {
         *      if (!StringTool.isProvided(pTextMessage) || !isOpen()) {
         *        return;
         *      }
         *      JsonValue jsonValue = JsonValue.readFrom(this.fCommandCompression ? LZString.decompressFromUTF16(pTextMessage) : pTextMessage);
         *      <InjectionPointHere>
         *      handleNetCommand(this.fNetCommandFactory.forJsonValue((IFactorySource)this.fClient.getGame().getRules(), jsonValue));
         *    }
         */
        val onMessageMethod = cc.getDeclaredMethod("onMessage")
        onMessageMethod.instrument(
            object : ExprEditor() {
                override fun edit(m: MethodCall) {
                    if (m.methodName.equals("readFrom")) {
                        m.replace(
                            "\$_ = \$proceed($$); com.jervisffb.fumbbl.net.FumbblDebugger.handleServerMessage(\$_);",
                        )
                    }
                }
            },
        )

        val jarParentDir = fumbblClientJar.parentFile.absolutePath
        cc.writeFile(jarParentDir + okio.Path.DIRECTORY_SEPARATOR)
        val outputFile =
            File(
                jarParentDir + "/com/fumbbl/ffb/client/net/CommandEndpoint.class".replace("/", okio.Path.DIRECTORY_SEPARATOR),
            )
        return mapOf("com/fumbbl/ffb/client/net/CommandEndpoint.class" to outputFile)
    }

    /**
     * Inject debug code into the FantasyFootballClient.jar and repackage it, so it is ready for use.
     */
    fun injectDebugCode(fumbblClientJar: File) {
        val modifiedFiles = createClassesWithDebugCode(fumbblClientJar)
        rewriteJarFile(fumbblClientJar, modifiedFiles)
        copyCLIJarFile(cliJarFile.toPath(), fumbblClientJar.parentFile.toPath())
    }

    private fun copyCLIJarFile(
        cliJarFile: Path,
        targetDirectory: Path,
    ) {
        val targetFile = targetDirectory.resolve(cliJarFile.name)
        logInfo("Copy CLI Jar file to $targetFile")
        Files.copy(cliJarFile, targetFile, StandardCopyOption.REPLACE_EXISTING)
    }

    /**
     * Replace the modified classes in the FUMBBL Client jar file.
     */
    fun rewriteJarFile(
        fumbblClientJar: File,
        modifiedFiles: Map<String, File>,
    ) {
        val pathToFumbblClientJar = fumbblClientJar.absolutePath
        val tempJarPath = Files.createTempFile("tempJar", ".jar")

        // Get the paths of all entries in the original JAR file, excluding the entry to be replaced
        var entriesToKeep: List<String?> = ArrayList()
        JarFile(pathToFumbblClientJar).use { jar ->
            entriesToKeep =
                jar.stream()
                    .map(JarEntry::getName)
                    // Don't copy modified files
                    .filter { name -> !modifiedFiles.keys.contains(name) }
                    // Remove signatures as they are now broken and will prevent loading the JAR
                    .filter { name -> !name.endsWith(".SF") && !name.endsWith(".RSA") }
                    .collect(Collectors.toList())
        }

        // Create the new JAR file
        JarInputStream(FileInputStream(pathToFumbblClientJar)).use { inJar ->
            JarOutputStream(FileOutputStream(tempJarPath.toFile()), Manifest()).use { ourJar ->
                // Copy the entries from the original JAR file, excluding the entry to be replaced
                var entry: JarEntry? = null
                while (inJar.nextJarEntry?.let { entry = it } != null) {
                    if (entriesToKeep.contains(entry!!.name)) {
                        ourJar.putNextEntry(JarEntry(entry!!.name))
                        inJar.transferTo(ourJar)
                    }
                }
                modifiedFiles.forEach { (pathInJar: String, modifiedFile: File) ->
                    FileInputStream(modifiedFile.absolutePath).use { classIn ->
                        ourJar.putNextEntry(JarEntry(pathInJar))
                        classIn.transferTo(ourJar)
                    }
                }
            }
        }
        // Replace the original JAR file with the new JAR file
        Files.move(tempJarPath, Paths.get(pathToFumbblClientJar), StandardCopyOption.REPLACE_EXISTING)
    }

    /**
     * Print messages to the console
     */
    fun logInfo(message: String) {
        println(message)
    }
}
