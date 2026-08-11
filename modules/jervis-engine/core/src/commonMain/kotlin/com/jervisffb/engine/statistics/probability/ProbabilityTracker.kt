package com.jervisffb.engine.statistics.probability

import com.jervisffb.engine.GameDelta
import com.jervisffb.engine.statistics.GameStatistics
import com.jervisffb.engine.statistics.probability.observation.ChanceObservation
import com.jervisffb.engine.statistics.probability.observation.ChanceObservationChange

/**
 * Tracks probability data for dice rolls performed during a game.
 *
 * [GameStatistics] is generally responsible for making sure that this class
 * is used correctly, and accessing it from [GameStatistics.diceProbabilities]
 * ensure that it tracks the curren game state.
 */
class ProbabilityTracker {

    /** Return all probability observations seend during the entire game */
    val observations: List<ChanceObservation>
        field = mutableListOf()

    /** Record observations from a game delta */
    fun handleAction(delta: GameDelta) {
        delta.chanceObservations.forEach { apply(it, observations) }
    }

    /**
     * Reset all probability observations.
     * 
     * Can be used to e.g, reset observations between turns so it is possible to calculate
     * the chance of success for each turn during a game.
     */
    fun resetObservations() {
        observations.clear()
    }

    private fun apply(change: ChanceObservationChange, collector: MutableList<ChanceObservation>) {
        when (change) {
            is ChanceObservationChange.Recorded -> {
                require(change.observation.index == collector.size) {
                    "Chance observation sequence diverged: " + "expected ${collector.size}, found ${change.observation.index}"
                }
                collector.add(change.observation)
            }
            is ChanceObservationChange.Updated,
            is ChanceObservationChange.Restored -> {
                val event = change.observation
                require(event.index in collector.indices) {
                    "Chance observation update references unknown sequence ${event.index}"
                }
                collector[event.index] = event
            }
            is ChanceObservationChange.Removed -> {
                val event = change.observation
                require(collector.lastOrNull()?.index == event.index) {
                    "Chance observation removal was out of order: ${event.index}"
                }
                collector.removeLast()
            }
        }
    }
}
