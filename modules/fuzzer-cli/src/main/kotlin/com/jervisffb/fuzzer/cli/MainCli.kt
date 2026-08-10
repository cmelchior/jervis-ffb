package com.jervisffb.fuzzer.cli

import co.touchlab.kermit.Logger
import co.touchlab.kermit.Severity
import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.core.Context
import com.github.ajalt.clikt.core.main
import com.github.ajalt.clikt.parameters.arguments.argument
import com.github.ajalt.clikt.parameters.arguments.help
import com.github.ajalt.clikt.parameters.options.default
import com.github.ajalt.clikt.parameters.options.help
import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.clikt.parameters.types.boolean
import com.github.ajalt.clikt.parameters.types.choice
import com.github.ajalt.clikt.parameters.types.int
import com.github.ajalt.clikt.parameters.types.long
import com.jervisffb.utils.loggerInstance
import java.util.concurrent.atomic.AtomicInteger
import kotlin.system.exitProcess

private const val TEST_BB2020_STANDARD = "bb2020"
private const val TEST_BB2025_STANDARD = "bb2025"
private const val TEST_BB2020_BB7 = "bb2020-bb7"
private val ALL_TESTS = listOf(TEST_BB2020_STANDARD, TEST_BB2025_STANDARD, TEST_BB2020_BB7)

class FuzzerCLI : CliktCommand(name = "fuzzer-cli") {

    override fun help(context: Context): String =
        "Fuzz-test the Jervis rules engine by running batches of randomised games."

    private val fuzzerConfiguration: String by argument("fuzzer-configuration")
        .choice(*ALL_TESTS.toTypedArray())
        .help("Which fuzzer configuration to run. Should be one of: ${ALL_TESTS.joinToString(prefix = "[", postfix = "]")}")

    private val games: Int by option("--games").int()
        .default(100_000)
        .help("Number of games to run. Default: 100.000")

    private val batchSize: Int by option("--batch-size").int()
        .default(5_000)
        .help("Games per parallel batch. Default: 5.000")

    private val seed: Long? by option("--seed").long()
        .help("Seed for the RNG. Combine with --games 1 to reproduce a specific game.")

    private val threads: Int by option("--threads").int()
        .default(8)
        .help("Size of the parallel worker pool. Default: 8")

    private val statistics: Boolean by option("--statistics").boolean()
        .default(false)
        .help("Enable statistics collector when running games.")


    override fun run() {
        // Force the Kermit min severity down to Assert before any fuzz work starts. Touch
        // `loggerInstance` first so its `by lazy` block runs (setting the default severity)
        // and then override it.
        loggerInstance
        Logger.setMinSeverity(Severity.Assert)

        echo("Running fuzz test '$fuzzerConfiguration' (games=$games, batchSize=$batchSize, threads=$threads, seed=${seed ?: "random"}, statistics=$statistics)")

        val failureCount = AtomicInteger(0)
        runFuzzTest(games, batchSize, seed, threads, failureCount) { gameNo, gameSeed ->
            when (fuzzerConfiguration) {
                TEST_BB2020_STANDARD -> runBB2020Standard(gameSeed, statistics)
                TEST_BB2025_STANDARD -> runBB2025Standard(gameSeed, statistics)
                TEST_BB2020_BB7 -> runBB2020BB7(gameNo, gameSeed, statistics)
                else -> error("Unknown test: $fuzzerConfiguration")
            }
        }

        val failures = failureCount.get()
        if (failures > 0) {
            System.err.println("Fuzz test finished with $failures failure(s).")
            exitProcess(1)
        }
        echo("Fuzz test finished without failures.")
        // multiThreadDispatcher spins up non-daemon threads that keep the JVM
        // alive after the coroutines finish. Force exit so the CLI
        // terminates promptly.
        exitProcess(0)
    }
}

fun main(args: Array<String>) = FuzzerCLI().main(args)
