package com.jervisffb.engine.bb2020.inducements.biasedreferee

import com.jervisffb.engine.model.Team
import com.jervisffb.engine.model.TeamId
import com.jervisffb.engine.model.inducements.biasedreferee.BiasedReferee
import com.jervisffb.engine.model.inducements.biasedreferee.BiasedRefereeAbility
import com.jervisffb.engine.rules.common.roster.PlayerSpecialRule
import kotlinx.serialization.Serializable

/**
 * Standard Biased Referee
 * See page 95 in the BB2020 rulebook.
 */
@Serializable
class StandardBiasedReferee(private val team: TeamId): BiasedReferee {
    constructor(team: Team): this(team.id)
    override val type: BiasedRefereeType2020 = BiasedRefereeType2020.STANDARD
    override val name: String = "Biased Referee"
    override val specialRules = listOf(
        PlayerSpecialRule.I_DID_NOT_SEE_A_THING,
        PlayerSpecialRule.CLOSE_SCRUTINY,
    )
    override val specialAbilities: List<BiasedRefereeAbility> = emptyList()
}


// Not implemented yet
@Serializable
class RanulfRedHokuli(private val team: TeamId): BiasedReferee {
    constructor(team: Team): this(team.id)
    override val name: String = BiasedRefereeType2020.RANULF_RED_HOKULI.label
    override val type: BiasedRefereeType2020 = BiasedRefereeType2020.RANULF_RED_HOKULI
    override val specialRules = emptyList<PlayerSpecialRule>()
    override val specialAbilities: List<BiasedRefereeAbility> = emptyList()
}

// Not implemented yet
@Serializable
class ThoronKorensson(private val team: TeamId): BiasedReferee {
    constructor(team: Team): this(team.id)
    override val name: String = BiasedRefereeType2020.THORON_KORENSSON.label
    override val type: BiasedRefereeType2020 = BiasedRefereeType2020.THORON_KORENSSON
    override val specialRules = emptyList<PlayerSpecialRule>()
    override val specialAbilities: List<BiasedRefereeAbility> = emptyList()
}

// Not implemented yet
@Serializable
class JormTheOgre(private val team: TeamId): BiasedReferee {
    constructor(team: Team): this(team.id)
    override val name: String = BiasedRefereeType2020.JORM_THE_OGRE.label
    override val type: BiasedRefereeType2020 = BiasedRefereeType2020.JORM_THE_OGRE
    override val specialRules = emptyList<PlayerSpecialRule>()
    override val specialAbilities: List<BiasedRefereeAbility> = emptyList()
}

// Not implemented yet
@Serializable
class TheThrundlefootTriplets(private val team: TeamId): BiasedReferee {
    constructor(team: Team): this(team.id)
    override val name: String = BiasedRefereeType2020.THE_THRUNDLEFOOT_TRIPLETS.label
    override val type: BiasedRefereeType2020 = BiasedRefereeType2020.THE_THRUNDLEFOOT_TRIPLETS
    override val specialRules = emptyList<PlayerSpecialRule>()
    override val specialAbilities: List<BiasedRefereeAbility> = emptyList()
}
