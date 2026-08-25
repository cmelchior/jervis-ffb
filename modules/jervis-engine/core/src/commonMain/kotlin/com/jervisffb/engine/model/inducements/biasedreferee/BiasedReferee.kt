package com.jervisffb.engine.model.inducements.biasedreferee

import com.jervisffb.engine.rules.common.roster.PlayerSpecialRule

/**
 * This class represents a single Biased Referee inducement.
 */
interface BiasedReferee {
    val name: String
    val type: BiasedRefereeType
    val specialRules: List<PlayerSpecialRule>
    val specialAbilities: List<BiasedRefereeAbility>
}
