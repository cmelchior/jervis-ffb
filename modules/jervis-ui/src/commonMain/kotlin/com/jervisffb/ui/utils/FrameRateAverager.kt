package com.jervisffb.ui.utils

import kotlin.time.Clock
import kotlin.time.Duration
import kotlin.time.DurationUnit
import kotlin.time.ExperimentalTime

/**
 * POC class for helping tracking average "frame"-rates during the game-loop.
 * It isn't actually the frame-rate, but more "time to calculate" new state.
 */
@OptIn(ExperimentalTime::class)
class FrameRateAverager(private val windowSize: Int = 60) {
    private val frameTimes = mutableListOf<Duration>() // stores frame durations in ns
    var start = Clock.System.now()
    var timeUsed = Duration.ZERO

    fun start() {
        timeUsed = Duration.ZERO
        start = Clock.System.now()
    }

    fun pause() {
        timeUsed = (Clock.System.now() - start)
    }

    fun resume() {
        start = Clock.System.now()
    }

    fun end() {
        timeUsed = (Clock.System.now() - start)
        frameTimes.add(timeUsed)
        val avgFrameTime = frameTimes.sumOf { it.toDouble(DurationUnit.MILLISECONDS) } / frameTimes.size
        println("Time used: ${timeUsed.toDouble(DurationUnit.MILLISECONDS)} ms, avg: $avgFrameTime ms")
    }
}
