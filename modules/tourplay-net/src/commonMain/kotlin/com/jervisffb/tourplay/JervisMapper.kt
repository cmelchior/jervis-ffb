package com.jervisffb.tourplay

import com.jervisffb.engine.model.SkillId
import com.jervisffb.engine.rules.Rules
import com.jervisffb.engine.rules.common.roster.RegionalSpecialRule
import com.jervisffb.engine.rules.common.roster.Roster
import com.jervisffb.engine.rules.common.roster.SpecialRules
import com.jervisffb.engine.rules.common.roster.TeamSpecialRule
import com.jervisffb.engine.rules.common.skills.SkillCategory
import com.jervisffb.engine.serialization.SerializedTeam
import com.jervisffb.engine.sprites.RosterLogo
import com.jervisffb.engine.sprites.SingleSprite
import com.jervisffb.tourplay.TourPlayApi.Companion.LOG
import com.jervisffb.tourplay.api.TourPlayRoster

/**
 * Subclasses of [JervisMapper] are responsible for mapping a team from TourPlay
 * to Jervis.
 *
 * For now, we have one for each ruleset, but it is unclear if that is actually
 * needed.
 */
abstract class JervisMapper {

    abstract fun convertToJervisRoster(rules: Rules, roster: TourPlayRoster): Roster
    abstract fun convertToJervisTeam(rules: Rules, jervisRoster: Roster, team: TourPlayRoster): SerializedTeam

    /**
     * Map TourPlay Skill Names to Jervis [SkillId]s.
     * Return `null` if the name could not be mapped or if the skill isn't supported
     * by the ruletset.
     */
    protected fun convertTourPlaySkillToSkillId(rules: Rules, skillName: String): SkillId? {
        // We should probably hard code all the TourPlay titles instead of hoping the names are the same.
        // But for now, we just do it in the few places with known problems and hope for the best.
        val normalizedSkillName = when (skillName) {
            // "Side Step" -> SkillType.SIDESTEP.description
            else -> skillName
        }
        return rules.skillSettings.getSkillIdFromNiceDescription(normalizedSkillName)
    }

    protected fun mapToSkillCategory(skillFlags: Int): List<SkillCategory> {
        return skillFlags.splitFlags().map {
            // These values are gathered from manually inspecting the API
            // Agility: 1
            // General: 2
            // Mutation: 4
            // Passing: 8
            // Strength: 16
            // Trait: 64
            // Devious: 128
            when (it) {
                1 -> SkillCategory.AGILITY
                2 -> SkillCategory.GENERAL
                4 -> SkillCategory.MUTATIONS
                8 -> SkillCategory.PASSING
                16 -> SkillCategory.STRENGTH
                32 -> error("Unknown skill flag: $it")
                64 -> SkillCategory.TRAITS
                128 -> SkillCategory.DEVIOUS
                else -> error("Unknown skill flag: $it")
            }
        }
    }

    protected fun convertRosterSpecialRules(specialRulesFlag: Int): List<SpecialRules> {
        // TourPlay describes their name mapping in the `en.json` file (use Chrome Dev View)
        // This was extracted on 19/08/2025
        //        "1": "Badlands Brawl",
        //        "2": "Elven Kingdoms League",
        //        "4": "Halfling Thimble Cup",
        //        "8": "Lustrian Superleague",
        //        "16": "Old World Classic",
        //        "32": "Sylvanian Spotlight",
        //        "64": "Underworld Challenge",
        //        "128": "Worlds Edge Superleague",
        //        "256": "Bribery and Corruption",
        //        "512": "Favoured of Chaos Undivided",
        //        "1024": "Favoured of Khorne",
        //        "2048": "Favoured of Nurgle",
        //        "4096": "Favoured of Tzeentch",
        //        "8192": "Favoured of Slaanesh",
        //        "16384": "Low Cost Linemen",
        //        "32768": "Master of Undeath",
        //        "65536": "Vampire Lord",
        //        "15872": "Favoured of...",
        //        "146944": "Favoured of...",
        //        "131072": "Favoured of Hashut"
        return specialRulesFlag.splitFlags().mapNotNull { flag ->
            when (flag) {
                // Regional special rules
                1 -> RegionalSpecialRule.BADLANDS_BRAWL
                2 -> RegionalSpecialRule.ELVEN_KINGDOMS_LEAGUE
                4 -> RegionalSpecialRule.HAFLING_THIMBLE_CUP // Note: enum uses HAFLING (as defined)
                8 -> RegionalSpecialRule.LUSTRIAN_SUPERLEAGUE
                16 -> RegionalSpecialRule.OLD_WORLD_CLASSIC
                32 -> RegionalSpecialRule.SYLVANIAN_SPOTLIGHT
                64 -> RegionalSpecialRule.UNDERWORLD_CHALLENGE
                128 -> RegionalSpecialRule.WORLDS_EDGE_SUPERLEAGUE

                // Team special rules
                256 -> TeamSpecialRule.BRIBERY_AND_CORRUPTION
                512 -> TeamSpecialRule.FAVOURED_OF_CHAOS_UNDIVIDED
                1024 -> TeamSpecialRule.FAVOURED_OF_KHORNE
                2048 -> TeamSpecialRule.FAVOURED_OF_NURGLE
                4096 -> TeamSpecialRule.FAVOURED_OF_TZEENTCH
                8192 -> TeamSpecialRule.FAVOURED_OF_SLAANESH
                16384 -> TeamSpecialRule.LOW_COST_LINEMEN
                32768 -> TeamSpecialRule.MASTERS_OF_UNDEATH
                //        "65536": "Vampire Lord",
                //        "15872": "Favoured of...",
                131072 -> TeamSpecialRule.FAVOURED_OF_HASHUT
                //        "146944": "Favoured of...",
                else -> {
                    LOG.d { "Could not map special rule flag: $flag" }
                    null
                }
            }
        }
    }

    /**
     * Convert the TourPlay team emblem to a Jervis Roster Logo.
     */
    protected fun extractRosterLogo(roster: TourPlayRoster): RosterLogo {
        // In previous versions of TourPlay, the TourPlay API had an option for resizing emblems. This seems to have been
        // removed in the newest version. Jervis just reuses that size across all use cases for now.
        // Example URL: https://tourplay.b-cdn.net/emblems/214739_x3_k1uigi5y0hfr.avif
        // Note, it looks like the x3 part of the name cannot be modified, and is probably a legacy modifier
        val emblemBaseUrl = "https://tourplay.b-cdn.net/emblems"
        return RosterLogo(
            large = SingleSprite.url("$emblemBaseUrl/${roster.imageFile}"),
            small = SingleSprite.url("$emblemBaseUrl/${roster.imageFile}"),
        )
    }

    private fun Int.splitFlags(): List<Int> =
        (0 until Int.SIZE_BITS)
            .map { 1 shl it }
            .filter { this and it != 0 }
}
