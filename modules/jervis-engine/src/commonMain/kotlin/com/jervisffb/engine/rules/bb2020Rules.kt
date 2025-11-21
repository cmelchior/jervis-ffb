package com.jervisffb.engine.rules

import com.jervisffb.engine.InducementSettings
import com.jervisffb.engine.model.Player
import com.jervisffb.engine.model.inducements.settings.InducementType
import com.jervisffb.engine.rules.bb2020.DEFAULT_INDUCEMENTS_BB2020
import com.jervisffb.engine.rules.bb2020.tables.BB7KickOffEventTable
import com.jervisffb.engine.rules.bb2020.tables.BB7PrayersToNuffleTable
import com.jervisffb.engine.rules.bb2020.tables.BB7StandardInjuryTable
import com.jervisffb.engine.rules.bb2020.tables.BB7StuntyInjuryTable
import com.jervisffb.engine.rules.builder.FoulActionBehavior
import com.jervisffb.engine.rules.builder.GameType
import com.jervisffb.engine.rules.builder.GameVersion
import com.jervisffb.engine.rules.builder.KickingPlayerBehavior
import com.jervisffb.engine.rules.builder.UseApothecaryBehavior
import io.ktor.http.parameters
import kotlinx.serialization.Serializable

/**
 * This file contains the standard rules for BB2020 games (and its variants).
 * - Standard (Strict)
 * - Standard (FUMBBL-Compatible)
 * - BB7
 */
@Serializable
abstract class BB2020Rules(
    private val bb2020RuleParameters: RulesParametersHolder
) : Rules(bb2020RuleParameters) {
    companion object {
        val DEFAULTS = RulesParametersHolder(
            name = "Blood Bowl 2020 Rules",
            baseVersion = GameVersion.BB2020,
            gameType = GameType.STANDARD,
            foulActionBehavior = FoulActionBehavior.BB2020,
            inducements = InducementSettings(DEFAULT_INDUCEMENTS_BB2020),
        )
    }

    // In BB2020, Open status does not consider if the player is standing or not.
    // This is probably and oversight that has been fixed in BB20205, but for
    // now we implement those semantics faithfully.
    override fun isOpen(player: Player): Boolean {
        return !isMarked(player)
    }
}

@Serializable
class StandardBB2020Rules(
    private val standardBB2020RuleParameters: RulesParametersHolder = DEFAULTS
) : BB2020Rules(standardBB2020RuleParameters) {

    companion object {
        val DEFAULTS = BB2020Rules.DEFAULTS.copy(
            name = "Blood Bowl 2020 Rules (Strict)",
        )
    }

    // Builder API infrastructure
    override fun toBuilder() = StandardBB2020RulesBuilder(standardBB2020RuleParameters)
    class StandardBB2020RulesBuilder(parameters: RulesParameters): RulesParameterBuilder(parameters) {
        override fun build(): StandardBB2020Rules = StandardBB2020Rules(buildParameters())
    }
}

/**
 * Ruleset that is compatible with the way FUMBBL organizes its rules.
 * While it generally follows the rules as written, there are minor differences.
 *
 * - KickOff: No need to select the kicking player. This is done automatically.
 *   Priority will be given to a legal player with "Kick".
 * - Foul: Player is not selected when starting the action.
 * - A more lenient timing system, so the opponents must time out each other.
 */
@Serializable
class FumbblBB2020Rules(
    private val fumbblBB2020RuleParameters: RulesParametersHolder = DEFAULTS
) : BB2020Rules(fumbblBB2020RuleParameters) {
    companion object {
        val DEFAULTS = BB2020Rules.DEFAULTS.copy(
            name = "Blood Bowl 2020 Rules (FUMBBL Compatible)",
            kickingPlayerBehavior = KickingPlayerBehavior.FUMBBL,
            foulActionBehavior = FoulActionBehavior.BB2025,
        )
    }

    // Builder API infrastructure
    override fun toBuilder() = FumbblBB2020RulesBuilder(fumbblBB2020RuleParameters)
    class FumbblBB2020RulesBuilder(parameters: RulesParameters): RulesParameterBuilder(parameters) {
        override fun build() = FumbblBB2020Rules(buildParameters())
    }
}

/**
 * Ruleset for the 2020 Blood Bowl Sevens game.
 * See Dungeon Bowl rulebook page 90 for more information.
 */
@Serializable
class BB72020Rules(
    private val bb72020RuleParameters: RulesParametersHolder = DEFAULTS
) : BB2020Rules(bb72020RuleParameters) {

    companion object {
        val DEFAULTS = BB2020Rules.DEFAULTS.copy(
            name = "Blood Bowl Sevens 2020 Rules",
            gameType = GameType.BB7,
            fieldWidth = 20,
            fieldHeight = 11,
            wideZone = 2,
            endZone = 1,
            lineOfScrimmageHome = 6,
            lineOfScrimmageAway = 13,
            playersRequiredOnLineOfScrimmage = 3,
            maxPlayersInWideZone = 1,
            maxPlayersOnField = 7,
            turnsPrHalf = 6,
            kickOffEventTable = BB7KickOffEventTable,
            injuryTable = BB7StandardInjuryTable,
            stuntyInjuryTable = BB7StuntyInjuryTable,
            prayersToNuffleTable = BB7PrayersToNuffleTable,
            useApothecaryBehavior = UseApothecaryBehavior.BB7,
            inducements = InducementSettings(DEFAULT_INDUCEMENTS_BB2020).toBuilder().run {
                InducementType.entries.forEach { type ->
                    when (type) {
                        InducementType.TEMP_AGENCY_CHEERLEADER -> {
                            get(type)!!.let {
                                it.price = 30_000
                                it.max = 2
                            }
                        }

                        InducementType.PART_TIME_ASSISTANT_COACH -> {
                            get(type)!!.let {
                                it.price = 30_000
                                it.max = 1
                            }
                        }

                        InducementType.WEATHER_MAGE -> get(type)!!.enabled = false
                        InducementType.BLOODWEISER_KEG -> { /* Do nothing */
                        }

                        InducementType.SPECIAL_PLAY -> { /* Do nothing */
                        }

                        InducementType.EXTRA_TEAM_TRAINING -> get(type)!!.price = 150_000
                        InducementType.BRIBE -> { /* Do nothing */
                        }

                        InducementType.WANDERING_APOTHECARY -> { /* Do nothing */
                        }

                        InducementType.MORTUARY_ASSISTANT -> { /* Do nothing */
                        }

                        InducementType.PLAGUE_DOCTOR -> { /* Do nothing */
                        }

                        InducementType.RIOTOUS_ROOKIE -> get(type)!!.enabled = false
                        InducementType.HALFLING_MASTER_CHEF -> { /* Do nothing */
                        }

                        InducementType.STANDARD_MERCENARY_PLAYERS -> { /* Do nothing */
                        }

                        InducementType.STAR_PLAYERS -> get(type)!!.enabled = false
                        InducementType.INFAMOUS_COACHING_STAFF -> get(type)!!.enabled = false
                        InducementType.WIZARD -> get(type)!!.enabled = false
                        InducementType.BIASED_REFEREE -> get(type)!!.enabled = false
                        InducementType.WAAAGH_DRUMMER -> get(type)!!.enabled = false
                        InducementType.CAVORTING_NURGLINGS -> get(type)!!.enabled = false
                        InducementType.DWARFEN_RUNESMITH -> get(type)!!.enabled = false
                        InducementType.HALFLING_HOTPOT -> get(type)!!.enabled = false
                        InducementType.MASTER_OF_BALLISTICS -> get(type)!!.enabled = false
                        InducementType.EXPANDED_MERCENARY_PLAYERS -> { /* Do nothing */
                        }

                        InducementType.GIANT -> get(type)!!.enabled = false
                        InducementType.DESPERATE_MEASURES -> {
                            get(type)!!.let {
                                it.enabled = true
                                it.price = 50_000
                                it.max = 5
                            }
                        }

                        InducementType.PRAYERS_TO_NUFFLE,
                        InducementType.TEAM_MASCOT,
                        InducementType.BLITZERS_BEST_KEGS,
                        InducementType.BRETONNIAN_PASTRIES,
                        InducementType.BRETONNIAN_DAMSEL,
                        InducementType.CANOPIC_JAR -> { /* Ignore */
                        }
                    }
                }
                build()
            }
        )


    }

    // Builder API infrastructure
    override fun toBuilder() = BB72020RulesBuilder(bb72020RuleParameters)
    class BB72020RulesBuilder(parameters: RulesParameters): RulesParameterBuilder(parameters) {
        override fun build() = BB72020Rules(buildParameters())
    }
}
