package com.jervisffb.fumbbl.netcli

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.core.subcommands
import com.github.ajalt.clikt.parameters.options.convert
import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.clikt.parameters.options.required
import com.jervisffb.fumbbl.netcli.debugclient.CreateDebugClientRunner
import com.jervisffb.fumbbl.netcli.gamedownloader.DownloadGameClient
import java.io.File

class FumbblCLI : CliktCommand() {
    override fun run() = Unit // Base command does not perform any actions
}

class PrepareDebugClient : CliktCommand(help = "Downloads and inject debug code into the FUMBBL client.") {
    private val output: String? by option(help = "Optional path where JAR files should be saved").convert { it }

    override fun run() {
        echo("Preparing debug client...")
        val output: File =
            output?.let {
                File(it)
            } ?: getDefaultOutputLocation()
        val runner = CreateDebugClientRunner(getJarFileLocation())
        runner.run(output)
    }

    /**
     * Return a reference to the JAR file running this code.
     * We need to copy this to the classpath of FantasyFootballClient.jar
     * as it contains the debug code we are calling.
     */
    private fun getJarFileLocation(): File {
        return File(
            this::class.java.getProtectionDomain()
                .codeSource
                .location
                .toURI(),
        )
    }

    private fun getDefaultOutputLocation(): File {
        // Assume this is <root>/tools
        return File("${getJarFileLocation().parentFile.absolutePath}/../Debug-FantasyFootballClient/libs")
    }
}

class DownloadGame : CliktCommand(
    help =
        "Download all the websocket data associated with a given game on FUMBBL. " +
            "DO NOT use this to download bulk games as it stresses the game server,",
) {
    private val gameId by option(help = "ID of the game to download").required()
    private val output: String? by option(help = "Optional path where game files should be saved").convert { it }
    private val pretty: Boolean? by option(help = "Pretty print JSON output (warning, this will make the file quite a bit larger)").convert { it.toBoolean() }

    override fun run() {
        echo("Downloading game: $gameId")
        val output: File =
            output?.let {
                File(it)
            } ?: getDefaultOutputLocation()
        val runner = DownloadGameClient(pretty == true)
        runner.run(gameId, output)
    }

    private fun getDefaultOutputLocation(): File {
        // Assume this is <root>/tools
        val jarFileLocation =
            File(
                this::class.java.getProtectionDomain()
                    .codeSource
                    .location
                    .toURI(),
            )
        return File("${jarFileLocation.parentFile.absolutePath}/../replays-fumbbl")
    }
}

fun main(args: Array<String>) =
    FumbblCLI()
        .subcommands(PrepareDebugClient(), DownloadGame())
        .main(args)
