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
import com.jervisffb.engine.sprites.SpriteSheet
import com.jervisffb.tourplay.TourPlayApi.Companion.LOG
import com.jervisffb.tourplay.api.LineUpMaster
import com.jervisffb.tourplay.api.TourPlayRoster
import com.jervisffb.tourplay.api.positionShortHand

/**
 * Subclasses of [JervisMapper] are responsible for mapping a team from TourPlay
 * to Jervis.
 *
 * For now, we have one for each ruleset, but it is unclear if that is actually
 * needed.
 *
 * [icons] contains the configuration needed for mapping TourPlay positions to
 * the FUMBBL artwork used for them.
 */
abstract class JervisMapper(private val icons: TourPlayIconMapping) {

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

    protected fun convertLeagueSpecialRules(leagueFlags: Int): List<RegionalSpecialRule> {
        if (leagueFlags == 0) return emptyList()
        // TourPlay describes their name mapping in the `en.json` file (use Chrome Dev View)
        // This was extracted on 10/09/2026:
        //    "LEAGUE": {
        //        "1": "Badlands Brawl",
        //        "2": "Elven Kingdoms League",
        //        "4": "Halfling Thimble Cup",
        //        "8": "Lustrian Superleague",
        //        "16": "Old World Classic",
        //        "32": "Sylvanian Spotlight",
        //        "64": "Underworld Challenge",
        //        "128": "Worlds Edge Superleague",
        //        "256": "Woodland League",
        //        "512": "Chaos Clash"
        //    },
        return leagueFlags.splitFlags().mapNotNull { flag ->
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
                256 -> RegionalSpecialRule.WOODLAND_LEAGUE
                512 -> RegionalSpecialRule.CHAOS_CLASH
                else -> {
                    LOG.d { "Could not map special rule flag: $flag" }
                    null
                }
            }
        }
    }

    protected fun convertRosterSpecialRules(specialRulesFlag: Int): List<SpecialRules> {
        if (specialRulesFlag == 0) return emptyList()
        // TourPlay describes their name mapping in the `en.json` file (use Chrome Dev View)
        // This was extracted on 10/09/2026:
        //    "TEAM_SPECIAL_RULE": {
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
        //        "131072": "Favoured of Hashut",
        //        "262144": "Brawlin' Brutes",
        //        "524288": "Team Captain",
        //        "1048576": "Swarming"
        //    },
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
                //        "146944": "Favoured of...",
                131072 -> TeamSpecialRule.FAVOURED_OF_HASHUT
                262144 -> TeamSpecialRule.BRAWLIN_BRUTES
                524288 -> TeamSpecialRule.TEAM_CAPTAIN
                1048576 -> TeamSpecialRule.SWARMING
                else -> {
                    LOG.d { "Could not map special rule flag: $flag" }
                    null
                }
            }
        }
    }

    /**
     * Find the icon set to use for a TourPlay position. TourPlay does not have
     * artwork we can use, so we borrow FUMBBL's. Positions with no FUMBBL
     * counterpart fall back to a sprite generated from the position's
     * shorthand.
     *
     * See [extractPositionPortrait]
     */
    protected fun extractPositionIcon(roster: TourPlayRoster, position: LineUpMaster): SpriteSheet {
        return icons.getSprite(roster.rosterMaster.name, position.position)
            ?: SpriteSheet.generated(position.positionShortHand)
    }

    /**
     * Find the portrait to use for a TourPlay position. Similar to player
     * sprites, TourPlay does not have any portraits, so we use FUMBBL's
     * instead or fallback to a default portrait if no FUMBBL counterpart is
     * found.
     *
     * See [extractPositionIcon]
     */
    protected fun extractPositionPortrait(roster: TourPlayRoster, position: LineUpMaster): SingleSprite {
        return icons.getPortrait(roster.rosterMaster.name, position.position)
            ?: SingleSprite.embedded(DEFAULT_PORTRAIT)
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
        return when (roster.imageFile != null) {
            true -> {
                RosterLogo(
                    large = SingleSprite.url("$emblemBaseUrl/${roster.imageFile}"),
                    small = SingleSprite.url("$emblemBaseUrl/${roster.imageFile}"),
                )
            }
            false -> RosterLogo.NONE
        }
    }

    private fun Int.splitFlags(): List<Int> =
        (0 until Int.SIZE_BITS)
            .map { 1 shl it }
            .filter { this and it != 0 }

    private companion object {
        const val DEFAULT_PORTRAIT = "jervis/portraits/default_portrait.png"
    }
}
