package com.jervisffb.engine.bb2025.inducements.biasedreferee

import com.jervisffb.engine.model.inducements.biasedreferee.BiasedReferee
import com.jervisffb.engine.model.inducements.biasedreferee.BiasedRefereeAbility
import com.jervisffb.engine.rules.common.roster.PlayerSpecialRule
import kotlinx.serialization.Serializable

/**
 * Standard Biased Referee in BB20205.
 * See page XXX in the BB2025 rulebook.
 */
@Serializable
class DodgyLeagueRep: BiasedReferee {
    override val type: BiasedRefereeType = BiasedRefereeType.DODGY_LEAGUE_REP
    override val name: String = BiasedRefereeType.DODGY_LEAGUE_REP.label
    override val specialRules = listOf(
        PlayerSpecialRule.I_DID_NOT_SEE_A_THING,
        PlayerSpecialRule.CLOSE_SCRUTINY,
    )
    override val specialAbilities: List<BiasedRefereeAbility> = emptyList()
}
