package com.jervisffb.engine.bb2020.inducements.biasedreferee

import com.jervisffb.engine.model.inducements.biasedreferee.BiasedReferee
import com.jervisffb.engine.model.inducements.biasedreferee.BiasedRefereeAbility
import com.jervisffb.engine.rules.common.roster.PlayerSpecialRule
import kotlinx.serialization.Serializable

/**
 * Standard Biased Referee
 * See page 95 in the BB2020 rulebook.
 */
@Serializable
class StandardBiasedReferee: BiasedReferee {
    override val type: BiasedRefereeType = BiasedRefereeType.STANDARD
    override val name: String = "Biased Referee"
    override val specialRules = listOf(
        PlayerSpecialRule.I_DID_NOT_SEE_A_THING,
        PlayerSpecialRule.CLOSE_SCRUTINY,
    )
    override val specialAbilities: List<BiasedRefereeAbility> = emptyList()
}


// Not implemented yet
@Serializable
class RanulfRedHokuli: BiasedReferee {
    override val name: String = BiasedRefereeType.RANULF_RED_HOKULI.label
    override val type: BiasedRefereeType = BiasedRefereeType.RANULF_RED_HOKULI
    override val specialRules = emptyList<PlayerSpecialRule>()
    override val specialAbilities: List<BiasedRefereeAbility> = emptyList()
}

// Not implemented yet
@Serializable
class ThoronKorensson: BiasedReferee {
    override val name: String = BiasedRefereeType.THORON_KORENSSON.label
    override val type: BiasedRefereeType = BiasedRefereeType.THORON_KORENSSON
    override val specialRules = emptyList<PlayerSpecialRule>()
    override val specialAbilities: List<BiasedRefereeAbility> = emptyList()
}

// Not implemented yet
@Serializable
class JormTheOgre: BiasedReferee {
    override val name: String = BiasedRefereeType.JORM_THE_OGRE.label
    override val type: BiasedRefereeType = BiasedRefereeType.JORM_THE_OGRE
    override val specialRules = emptyList<PlayerSpecialRule>()
    override val specialAbilities: List<BiasedRefereeAbility> = emptyList()
}

// Not implemented yet
@Serializable
class TheThrundlefootTriplets: BiasedReferee {
    override val name: String = BiasedRefereeType.THE_THRUNDLEFOOT_TRIPLETS.label
    override val type: BiasedRefereeType = BiasedRefereeType.THE_THRUNDLEFOOT_TRIPLETS
    override val specialRules = emptyList<PlayerSpecialRule>()
    override val specialAbilities: List<BiasedRefereeAbility> = emptyList()
}
