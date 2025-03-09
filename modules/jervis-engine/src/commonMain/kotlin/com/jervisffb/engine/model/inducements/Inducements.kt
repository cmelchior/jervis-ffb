package com.jervisffb.engine.model.inducements

import com.jervisffb.engine.fsm.Procedure
import com.jervisffb.engine.model.Game
import com.jervisffb.engine.rules.Rules
import com.jervisffb.engine.rules.bb2020.procedures.DummyProcedure
import com.jervisffb.engine.rules.bb2020.skills.Duration
import kotlinx.serialization.Serializable

/**
 * Interface describing inducement effects like spells and special play cards;
 * that are optional to use during a game, i.e., they must be selected by the player.
 */
@Serializable
sealed interface InducementEffect {
    val name: String // Name of the effect
    var used: Boolean // Whether it has been used or not
    val triggers: List<Timing> // What conditions trigger this effect
    val procedure: Procedure // The procedure that handles the effect being selected
}

/**
 * Interface describing spells owned by Wizards,
 */
interface Spell: InducementEffect {
    // This causes a circular reference crashing serialization
    // val wizard: Wizard
}

/**
 * Interface describing infamous coach abilities that are optional
 * to use
 */
interface InfamousCoachAbility: InducementEffect {
    // Some cards have special conditions on their triggers
    // Call this method to ensure that the card truly is available.
    // It assumes that the trigger Timing condition has been met
    fun isApplicable(state: Game, rules: Rules): Boolean {
        return true
    }
}

// See page 16 in Deathzone
class ByThePowerOfTheGoods: InfamousCoachAbility {
    override val name: String = "By The Power Of The Gods!"
    override var used: Boolean = false
    override val triggers: List<Timing> = listOf(Timing.START_OF_DRIVE)
    override val procedure: Procedure = DummyProcedure
}

/**
 * Interface describing a Special Play card.
 */
interface SpecialPlayCard: InducementEffect {
    // Type of Special Play Card. Should we just model this using sealed interfaces?
    val type: SpecialPlayCardType
    // When the card stops being "in play".
    // Some cards also adds temporary skills, dice modifiers and other powers
    // these are removed independently. This only tracks "the card"
    val duration: Duration
    // If the card is "played" or in use.
    var isActive: Boolean
    // Some cards have special conditions on their triggers
    // Call this method to ensure that the card truly is available.
    // It assumes that the trigger Timing condition has been met
    fun isApplicable(state: Game, rules: Rules): Boolean {
        return true
    }
}

abstract class DirtyTrick: SpecialPlayCard {
    override val type: SpecialPlayCardType = SpecialPlayCardType.DIRTY_TRICK
    override var used: Boolean = false
    override var isActive: Boolean = false
}
abstract class RandomEvent: SpecialPlayCard {
    override val type: SpecialPlayCardType = SpecialPlayCardType.RANDOM_EVENT
    override var used: Boolean = false
    override var isActive: Boolean = false
}
abstract class MagicalMemorabilia: SpecialPlayCard {
    override val type: SpecialPlayCardType = SpecialPlayCardType.MAGIC_MEMORABILIA
    override var used: Boolean = false
    override var isActive: Boolean = false
}
abstract class HeroicFeat: SpecialPlayCard {
    override val type: SpecialPlayCardType = SpecialPlayCardType.HEROIC_FEAT
    override var used: Boolean = false
    override var isActive: Boolean = false
}
abstract class BenefitOfTraining: SpecialPlayCard {
    override val type: SpecialPlayCardType = SpecialPlayCardType.HEROIC_FEAT
    override var used: Boolean = false
    override var isActive: Boolean = false
}
abstract class MiscellaneousMayhem: SpecialPlayCard {
    override val type: SpecialPlayCardType = SpecialPlayCardType.MISCELLANEOUS_MAYHEM
    override var used: Boolean = false
    override var isActive: Boolean = false
}
