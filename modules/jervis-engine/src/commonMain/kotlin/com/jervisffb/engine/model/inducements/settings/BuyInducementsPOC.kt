package com.jervisffb.engine.model.inducements.settings

import com.jervisffb.engine.model.inducements.BiasedReferee
import com.jervisffb.engine.model.inducements.InfamousCoachingStaff
import com.jervisffb.engine.model.inducements.JosefBugman
import com.jervisffb.engine.model.inducements.StandardBiasedReferee
import com.jervisffb.engine.model.inducements.wizards.HirelingSportsWizard
import com.jervisffb.engine.model.inducements.wizards.Wizard
import com.jervisffb.engine.rules.bb2020.roster.SpecialRules
import com.jervisffb.engine.rules.bb2020.roster.StarPlayerPosition
import com.jervisffb.engine.rules.bb2020.roster.TeamSpecialRule
import com.jervisffb.teams.THE_BLACK_GOBBO
import kotlinx.serialization.Serializable

// This file currently just contain snippets of code trying while I am trying
// to figure out how to model buying inducements
// They should probably be split into "Setup/Rules Inducements" and "Inducements added to the team"

/**
 * See page 89 in the rulebook
 */
enum class InducementType {

    // Standard game, se page 89 in the rulebook
    TEMP_AGENCY_CHEERLEADER,
    PART_TIME_ASSISTANT_COACH,
    WEATHER_MAGE,
    BLOODWEISER_KEG,
    SPECIAL_PLAY,
    EXTRA_TEAM_TRAINING,
    BRIBE,
    WANDERING_APOTHECARY,
    MORTUARY_ASSISTANT,
    PLAGUE_DOCTOR,
    RIOTOUS_ROOKIE,
    HALFLING_MASTER_CHEF,
    STANDARD_MERCENARY_PLAYERS,
    STAR_PLAYERS,
    INFAMOUS_COACHING_STAFF,
    WIZARD,
    BIASED_REFEREE,

    // DeathZone
    // ...
    WAAAGH_DRUMMER,
    CAVORTING_NURGLINGS,
    DWARFEN_RUNESMITH,
    HALFLING_HOTPOT,
    MASTER_OF_BALLISTICS,
    EXPANDED_MERCENARY_PLAYERS, // Contains a lot of sub options
    GIANT

    // Other extensions
    // ...
}

// Top-level interface for all inducements available to buy during the pregame sequence.
@Serializable
sealed interface Inducement<T: InducementBuilder> {
    val type: InducementType
    val name: String
    val max: Int
    val price: Int?
    val enabled: Boolean
    fun toBuilder(): T
}

sealed interface InducementBuilder {
    val type: InducementType
    val name: String
    var max: Int
    var price: Int?
    var enabled: Boolean
}

/**
 * This class describes simple inducements, i.e., inducements that only have one price
 * and one "effect".
 */
@Serializable
data class SimpleInducement(
    override val type: InducementType,
    override val name: String,
    override val max: Int,
    override val price: Int?,
    override val enabled: Boolean,
    val requirements: List<SpecialRules> = emptyList(),
    val modifier: List<Pair<SpecialRules, Float>> = emptyList()
): Inducement<SimpleInducement.Builder> {

    override fun toBuilder() = Builder(this)

    class Builder(private val inducement: SimpleInducement): InducementBuilder {
        override val type: InducementType = inducement.type
        override val name: String = inducement.name
        override var max: Int = inducement.max
        override var price: Int? = inducement.price
        override var enabled: Boolean = inducement.enabled
        var requirements: List<SpecialRules> = inducement.requirements.toList()
        var modifier: List<Pair<SpecialRules, Float>> = inducement.modifier.toList()
        fun build() = SimpleInducement(
            type, inducement.name, max, price, enabled, requirements, modifier
        )
    }
}

@Serializable
data class WizardsInducement(
    override val max: Int = 1,
    override val enabled: Boolean,
    val wizards: List<WizardInducement> = listOf(
        WizardInducement(HirelingSportsWizard(), 1, 150_000, named = false, enabled = true)
    )
): Inducement<WizardsInducement.Builder> {
    override val name: String = "Wizard"
    override val type: InducementType = InducementType.WIZARD
    override val price: Int? = null

    override fun toBuilder() = Builder(this)

    class Builder(inducement: WizardsInducement): InducementBuilder{
        override val type: InducementType = inducement.type
        override val name: String = inducement.name
        override var max: Int = inducement.max
        override var price: Int? = inducement.price
        override var enabled: Boolean = inducement.enabled
        var wizards: List<WizardInducement.Builder> = inducement.wizards.toList().map { it.toBuilder() }
        fun build() = WizardsInducement(max, enabled, wizards.map { it.build() })
    }
}

@Serializable
data class WizardInducement(
    val wizard: Wizard,
    val max: Int,
    val price: Int?,
    val named: Boolean, // Is "named" in the context of the rules, i.e. has special restrictions in League Play
    val enabled: Boolean,
    val requirements: List<SpecialRules> = emptyList(),
    val modifier: List<Pair<SpecialRules, Float>> = emptyList()
) {
    fun toBuilder() = Builder(this)

    class Builder(wizardInducement: WizardInducement) {
        val wizard: Wizard = wizardInducement.wizard
        var max: Int = wizardInducement.max
        var price: Int? = wizardInducement.price
        var named: Boolean = wizardInducement.named
        var enabled: Boolean = wizardInducement.enabled
        var requirements: MutableList<SpecialRules> = wizardInducement.requirements.toMutableList()
        var modifier: MutableList<Pair<SpecialRules, Float>> = wizardInducement.modifier.toMutableList()
        fun build() = WizardInducement(wizard, max, price, named, enabled, requirements, modifier)
    }
}

@Serializable
data class InfamousCoachingStaffsInducement(
    override val name: String,
    override val max: Int,
    override val enabled: Boolean,
    val coachingStaff: List<InfamousCoachingStaffInducement> = listOf(
        InfamousCoachingStaffInducement(
            JosefBugman(),
            1,
            100_000,
            named = true,
            enabled = true
        ),
    )
): Inducement<InfamousCoachingStaffsInducement.Builder> {
    override val type: InducementType = InducementType.INFAMOUS_COACHING_STAFF
    override val price: Int? = null

    override fun toBuilder() = Builder(this)

    class Builder(private val inducement: InfamousCoachingStaffsInducement): InducementBuilder {
        override val type: InducementType = inducement.type
        override val name: String = inducement.name
        override var max: Int = inducement.max
        override var price: Int? = inducement.price
        override var enabled: Boolean = inducement.enabled
        var coachingStaff: MutableList<InfamousCoachingStaffInducement.Builder> = inducement.coachingStaff.toList().map { it.toBuilder() }.toMutableList()

        fun build() = InfamousCoachingStaffsInducement(inducement.name, max, enabled, coachingStaff.map { it.build() })
    }
}

@Serializable
data class InfamousCoachingStaffInducement(
    val staff: InfamousCoachingStaff,
    val max: Int,
    val price: Int?,
    val named: Boolean, // Is "named" in the context of the rules, i.e. has special restrictions in League Play
    val enabled: Boolean,
    val requirements: List<SpecialRules> = emptyList(),
    val modifier: List<Pair<SpecialRules, Float>> = emptyList()
) {
    fun toBuilder() = Builder(this)

    class Builder(inducement: InfamousCoachingStaffInducement) {
        val staff: InfamousCoachingStaff = inducement.staff
        var max: Int = inducement.max
        var price: Int? = inducement.price
        var named: Boolean = inducement.named
        var enabled: Boolean = inducement.enabled
        var requirements: MutableList<SpecialRules> = inducement.requirements.toMutableList()
        var modifier: MutableList<Pair<SpecialRules, Float>> = inducement.modifier.toMutableList()

        fun build() = InfamousCoachingStaffInducement(staff, max, price, named, enabled, requirements, modifier)
    }
}

// See page 92 in the rulebook.
// This inducement should not be allowed at the same type as ExpandedMercenaryInducements
@Serializable
data class StandardMercenaryInducements(
    override val max: Int = Int.MAX_VALUE,
    override val enabled: Boolean = true,
    val extraCost: Int = 30_000,
    val skillCost: Int = 50_000,
): Inducement<StandardMercenaryInducements.Builder> {
    override val type: InducementType = InducementType.STANDARD_MERCENARY_PLAYERS
    override val name: String = "Mercenary Players"
    override val price: Int? = null

    override fun toBuilder() = Builder(this)

    class Builder(inducement: StandardMercenaryInducements): InducementBuilder {
        override val type: InducementType = inducement.type
        override val name: String = inducement.name
        override var max: Int = inducement.max
        override var price: Int? = inducement.price
        override var enabled: Boolean = inducement.enabled
        var extraCost: Int = inducement.extraCost
        var skillCost: Int = inducement.skillCost

        fun build() = StandardMercenaryInducements(max, enabled, extraCost, skillCost)
    }
}

// See page 41 in DeathZone.
// This inducement should not be allowed at the same type as StandardMercenaryInducements.
// Not sure how much customization we want in the UI for these as it turns pretty complex
// quickly. So for now, just expose the minimum. Support for this has a pretty low priority
// regardless, so just postpone adding it to the pre-game sequence UI.
@Serializable
data class ExpandedMercenaryInducements(
    override val max: Int = 3,
    override val enabled: Boolean = true,
): Inducement<ExpandedMercenaryInducements.Builder> {
    override val type: InducementType = InducementType.EXPANDED_MERCENARY_PLAYERS
    override val name: String = "Expanded Mercenary Players"
    override val price: Int? = null

    override fun toBuilder() = Builder(this)

    class Builder(inducement: ExpandedMercenaryInducements): InducementBuilder {
        override val type: InducementType = inducement.type
        override val name: String = inducement.name
        override var max: Int = inducement.max
        override var price: Int? = inducement.price
        override var enabled: Boolean = inducement.enabled

        fun build() = ExpandedMercenaryInducements(max, enabled)
    }
}

@Serializable
data class StarPlayersInducement(
    override val max: Int = 2,
    override val enabled: Boolean = true,
    val starPlayers: List<StarPlayerInducement> = listOf(
        StarPlayerInducement(
            THE_BLACK_GOBBO,
            1,
            THE_BLACK_GOBBO.cost,
            enabled = true,
            requirements = THE_BLACK_GOBBO.playsFor,

        ),
    )
): Inducement<StarPlayersInducement.Builder> {
    override val type: InducementType = InducementType.STAR_PLAYERS
    override val name: String = "Star Players"
    override val price: Int? = null

    override fun toBuilder() = Builder(this)

    class Builder(inducement: StarPlayersInducement): InducementBuilder {
        override val type: InducementType = inducement.type
        override val name: String = inducement.name
        override var max: Int = inducement.max
        override var price: Int? = null
        override var enabled: Boolean = inducement.enabled
        var starPlayers: List<StarPlayerInducement.Builder> = inducement.starPlayers.map { it.toBuilder() }.toMutableList()

        fun build() = StarPlayersInducement(max, enabled, starPlayers.map { it.build()})
    }
}

/**
 * Class wrapping the details for a single Star Player inducement.
 */
@Serializable
data class StarPlayerInducement(
    val starPlayer: StarPlayerPosition,
    val max: Int,
    val price: Int?,
    val enabled: Boolean,
    // Team must have _one_ of the special rules listed here.
    val requirements: List<SpecialRules> = emptyList(),
    // If the hiring team has the special rule listed, they get a discount on the price
    val reducedPrice: List<Pair<SpecialRules, Int>> = emptyList()
) {
    fun toBuilder() = Builder(this)

    class Builder(inducement: StarPlayerInducement) {
        val starPlayer: StarPlayerPosition = inducement.starPlayer
        var max: Int = inducement.max
        var price: Int? = inducement.price
        var enabled: Boolean = inducement.enabled
        var requirements: MutableList<SpecialRules> = inducement.requirements.toMutableList()
        var reducedPrice: MutableList<Pair<SpecialRules, Int>> = inducement.reducedPrice.toMutableList()

        fun build() = StarPlayerInducement(starPlayer, max, price, enabled, requirements, reducedPrice)
    }
}

@Serializable
data class BiasedRefereesInducement(
    override val max: Int = 1,
    override val enabled: Boolean,
    val referees: List<BiasedRefereeInducement> = listOf(
        BiasedRefereeInducement(
            referee = StandardBiasedReferee(),
            max = 1,
            price = 120_000,
            named = false,
            enabled = true,
            reducedPrice = listOf(Pair(TeamSpecialRule.BRIBERY_AND_CORRUPTION, 80_0000))
        ),
    )
): Inducement<BiasedRefereesInducement.Builder> {
    override val name: String = "Biased Referee"
    override val type: InducementType = InducementType.BIASED_REFEREE
    override val price: Int? = null

    override fun toBuilder() = Builder(this)

    class Builder(inducement: BiasedRefereesInducement): InducementBuilder {
        override val type: InducementType = inducement.type
        override val name: String = inducement.name
        override var max: Int = inducement.max
        override var price: Int? = inducement.price
        override var enabled: Boolean = inducement.enabled
        var referees: List<BiasedRefereeInducement.Builder> = inducement.referees.map { it.toBuilder() }.toMutableList()

        fun build() = BiasedRefereesInducement(max, enabled, referees.map { it.build() })
    }
}

@Serializable
data class BiasedRefereeInducement(
    val referee: BiasedReferee,
    val max: Int,
    val price: Int?,
    val named: Boolean, // Is "named" in the context of the rules, i.e. has special restrictions in League Play
    val enabled: Boolean,
    val requirements: List<SpecialRules> = emptyList(),
    val reducedPrice: List<Pair<SpecialRules, Int>> = emptyList()
) {
    fun toBuilder() = Builder(this)

    class Builder(inducement: BiasedRefereeInducement) {
        val referee: BiasedReferee = inducement.referee
        var max: Int = inducement.max
        var price: Int? = inducement.price
        var named: Boolean = inducement.named
        var enabled: Boolean = inducement.enabled
        var requirements: MutableList<SpecialRules> = inducement.requirements.toMutableList()
        var reducedPrice: MutableList<Pair<SpecialRules, Int>> = inducement.reducedPrice.toMutableList()

        fun build() = BiasedRefereeInducement(referee, max, price, named, enabled, requirements, reducedPrice)
    }
}

/**
 * Track all inducements allocated to this team.
 * Note, this just tracks whether or not it has been bought.
 * Any single inducement might add modifiers, players and skills.
 * These are added during the Buy Inducement step and any procedure
 * that is affected by them will need to check for it.
 */
//class TeamInducements {
//    fun hasInducement(type: InducementType): Boolean {
//        TODO()
//    }
////    fun addInducement()
////    fun removeInducement(inducement: Inducement)
//}

