package com.jervisffb.engine.bb2020.inducements.effects

import com.jervisffb.engine.model.inducements.card.SpecialPlayCard
import com.jervisffb.engine.model.inducements.card.SpecialPlayCardCategory
import kotlinx.serialization.Serializable

/**
 * This file contains the groupings used by BB2020 for Special Play Cards.
 */

@Serializable
abstract class DirtyTrick: SpecialPlayCard {
    override val type: SpecialPlayCardCategory = SpecialPlayCardCategory2020.DIRTY_TRICK
    override var used: Boolean = false
    override var isActive: Boolean = false
}

@Serializable
abstract class RandomEvent: SpecialPlayCard {
    override val type: SpecialPlayCardCategory = SpecialPlayCardCategory2020.RANDOM_EVENT
    override var used: Boolean = false
    override var isActive: Boolean = false
}
@Serializable
abstract class MagicalMemorabilia: SpecialPlayCard {
    override val type: SpecialPlayCardCategory = SpecialPlayCardCategory2020.MAGIC_MEMORABILIA
    override var used: Boolean = false
    override var isActive: Boolean = false
}
@Serializable
abstract class HeroicFeat: SpecialPlayCard {
    override val type: SpecialPlayCardCategory = SpecialPlayCardCategory2020.HEROIC_FEAT
    override var used: Boolean = false
    override var isActive: Boolean = false
}
@Serializable
abstract class BenefitOfTraining: SpecialPlayCard {
    override val type: SpecialPlayCardCategory = SpecialPlayCardCategory2020.HEROIC_FEAT
    override var used: Boolean = false
    override var isActive: Boolean = false
}
@Serializable
abstract class MiscellaneousMayhem: SpecialPlayCard {
    override val type: SpecialPlayCardCategory = SpecialPlayCardCategory2020.MISCELLANEOUS_MAYHEM
    override var used: Boolean = false
    override var isActive: Boolean = false
}
