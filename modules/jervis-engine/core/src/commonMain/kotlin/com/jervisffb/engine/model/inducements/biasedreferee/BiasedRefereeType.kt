package com.jervisffb.engine.model.inducements.biasedreferee

import com.jervisffb.engine.model.Team

/**
 * Interface describing the type of Biased Referee.
 * This is used to more easily identify the referee when configuring
 * inducements available for the game.
 */
interface BiasedRefereeType {
    val label: String
    fun create(team: Team): BiasedReferee
}
