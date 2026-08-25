package com.jervisffb.engine.bb2025.inducements.biasedreferee

import kotlinx.serialization.Serializable

@Serializable
enum class BiasedRefereeType(override val label: String): com.jervisffb.engine.model.inducements.biasedreferee.BiasedRefereeType {
    DODGY_LEAGUE_REP("Dodgy League Rep")
}
