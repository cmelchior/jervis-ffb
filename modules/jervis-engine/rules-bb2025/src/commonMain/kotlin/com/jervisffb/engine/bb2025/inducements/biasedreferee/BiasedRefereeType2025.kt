package com.jervisffb.engine.bb2025.inducements.biasedreferee

import com.jervisffb.engine.model.Team
import com.jervisffb.engine.model.inducements.biasedreferee.BiasedReferee
import com.jervisffb.engine.model.inducements.biasedreferee.BiasedRefereeType
import kotlinx.serialization.Serializable

@Serializable
enum class BiasedRefereeType2025(override val label: String): BiasedRefereeType {
    DODGY_LEAGUE_REP("Dodgy League Rep") {
        override fun create(team: Team): BiasedReferee = DodgyLeagueRep(team)
    }
}
