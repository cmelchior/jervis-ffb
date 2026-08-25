package com.jervisffb.engine.bb2020.inducements.effects

import com.jervisffb.engine.model.inducements.card.SpecialPlayCardCategory

enum class BB2020SpecialPlayCardCategory(override val label: String): SpecialPlayCardCategory {
    RANDOM_EVENT("Random Event"),
    DIRTY_TRICK("Dirty Trick"),
    MAGIC_MEMORABILIA("Magic Memorabilia"),
    HEROIC_FEAT("Heroic Feat"),
    BENEFIT_OF_TRAINING("Benefit of Training"),
    MISCELLANEOUS_MAYHEM("Miscellaneous Mayhem"),
}
