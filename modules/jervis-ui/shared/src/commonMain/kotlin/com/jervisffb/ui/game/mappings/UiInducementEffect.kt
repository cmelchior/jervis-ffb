package com.jervisffb.ui.game.mappings

import com.jervisffb.engine.bb2020.inducements.effects.SpecialPlayCardCategory2020
import com.jervisffb.engine.bb2025.inducements.effects.SpecialPlayCardCategory2025
import com.jervisffb.engine.model.inducements.InducementEffect
import com.jervisffb.engine.model.inducements.biasedreferee.BiasedRefereeAbility
import com.jervisffb.engine.model.inducements.card.SpecialPlayCard
import com.jervisffb.engine.model.inducements.infamouscoach.InfamousCoachAbility
import com.jervisffb.engine.model.inducements.wizard.Spell

enum class UiInducementEffect(val categoryLabel: String) {
    WIZARD("Wizard"),
    RANDOM_EVENT("Random Event"),
    DIRTY_TRICK("Dirty Trick"),
    MAGIC_MEMORABILIA("Magic Memorabilia"),
    HEROIC_FEAT("Heroic Feat"),
    BENEFIT_OF_TRAINING("Benefit of Training"),
    MISCELLANEOUS_MAYHEM("Miscellaneous Mayhem"),
    DESPERATE_MEASURES("Desperate Measures"),
    INFAMOUS_COACHING_STAFF("Infamous Coaching Staff"),
    BIASED_REFEREE("Biased Referee"),
    ;

    companion object : UiMapping<InducementEffect, UiInducementEffect> {
        override fun mapFrom(el: InducementEffect): UiInducementEffect {
            return when (el) {
                is Spell -> WIZARD
                is SpecialPlayCard -> mapSpecialPlayCard(el)
                is InfamousCoachAbility -> INFAMOUS_COACHING_STAFF
                is BiasedRefereeAbility -> BIASED_REFEREE
                else -> error("Unsupported inducement effect: $el")
            }
        }

        private fun mapSpecialPlayCard(card: SpecialPlayCard): UiInducementEffect {
            return when (val type = card.type) {
                is SpecialPlayCardCategory2020 -> {
                    when (type) {
                        SpecialPlayCardCategory2020.RANDOM_EVENT -> RANDOM_EVENT
                        SpecialPlayCardCategory2020.DIRTY_TRICK -> DIRTY_TRICK
                        SpecialPlayCardCategory2020.MAGIC_MEMORABILIA -> MAGIC_MEMORABILIA
                        SpecialPlayCardCategory2020.HEROIC_FEAT -> HEROIC_FEAT
                        SpecialPlayCardCategory2020.BENEFIT_OF_TRAINING -> BENEFIT_OF_TRAINING
                        SpecialPlayCardCategory2020.MISCELLANEOUS_MAYHEM -> MISCELLANEOUS_MAYHEM
                    }
                }

                is SpecialPlayCardCategory2025 -> {
                    when (type) {
                        SpecialPlayCardCategory2025.DESPERATE_MEASURES -> DESPERATE_MEASURES
                    }
                }

                else -> error("Unsupported special play card category: $type")
            }
        }
    }
}
