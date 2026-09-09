package com.jervisffb.ui.game.mappings

import com.jervisffb.engine.bb2020.inducements.effects.SpecialPlayCardCategory2020
import com.jervisffb.engine.bb2025.inducements.effects.SpecialPlayCardCategory2025
import com.jervisffb.engine.model.Team
import com.jervisffb.engine.model.inducements.InducementEffect
import com.jervisffb.engine.model.inducements.biasedreferee.BiasedRefereeAbility
import com.jervisffb.engine.model.inducements.card.SpecialPlayCard
import com.jervisffb.engine.model.inducements.infamouscoach.InfamousCoachAbility
import com.jervisffb.engine.model.inducements.wizard.Spell

enum class UiInducementEffect(val categoryLabel: String) {
    WIZARD("Wizard"),
    RANDOM_EVENT("Special Play Card"),
    DIRTY_TRICK("Special Play Card"),
    MAGIC_MEMORABILIA("Special Play Card"),
    HEROIC_FEAT("Special Play Card"),
    BENEFIT_OF_TRAINING("Special Play Card"),
    MISCELLANEOUS_MAYHEM("Special Play Card"),
    DESPERATE_MEASURES("Desperate Measures"),
    INFAMOUS_COACHING_STAFF("Infamous Coaching Staff"),
    BIASED_REFEREE("Biased Referee"),
    ;

    fun getSubLabel(
        effect: InducementEffect,
        team: Team,
    ): String? {
        return when (this) {
            WIZARD -> team.wizards
                .firstOrNull { wizard -> wizard.spells.any { it.id == effect.id } }
                ?.type
                ?.label
                ?: error("Could not find wizard for inducement effect: $effect")

            RANDOM_EVENT,
            DIRTY_TRICK,
            MAGIC_MEMORABILIA,
            HEROIC_FEAT,
            BENEFIT_OF_TRAINING,
            MISCELLANEOUS_MAYHEM -> (effect as SpecialPlayCard).type.label

            DESPERATE_MEASURES -> null

            INFAMOUS_COACHING_STAFF -> team.infamousCoachingStaff
                .firstOrNull { staff -> staff.specialAbilities.any { it.id == effect.id } }
                ?.name
                ?: error("Could not find infamous coaching staff for inducement effect: $effect")

            BIASED_REFEREE -> team.biasedReferees
                .firstOrNull { referee -> referee.specialAbilities.any { it.id == effect.id } }
                ?.name
                ?: error("Could not find biased referee for inducement effect: $effect")
        }
    }

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
