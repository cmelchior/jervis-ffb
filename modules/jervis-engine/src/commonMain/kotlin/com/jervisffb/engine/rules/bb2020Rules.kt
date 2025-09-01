package com.jervisffb.engine.rules

import com.jervisffb.engine.DEFAULT_INDUCEMENTS
import com.jervisffb.engine.InducementSettings
import com.jervisffb.engine.model.inducements.settings.InducementType
import com.jervisffb.engine.rules.bb2020.tables.BB7KickOffEventTable
import com.jervisffb.engine.rules.bb2020.tables.BB7PrayersToNuffleTable
import com.jervisffb.engine.rules.bb2020.tables.BB7StandardInjuryTable
import com.jervisffb.engine.rules.bb2020.tables.BB7StuntyInjuryTable
import com.jervisffb.engine.rules.bb2020.tables.InjuryTable
import com.jervisffb.engine.rules.bb2020.tables.KickOffTable
import com.jervisffb.engine.rules.bb2020.tables.PrayersToNuffleTable
import com.jervisffb.engine.rules.builder.FoulActionBehavior
import com.jervisffb.engine.rules.builder.GameType
import com.jervisffb.engine.rules.builder.KickingPlayerBehavior
import com.jervisffb.engine.rules.builder.UseApothecaryBehavior
import kotlinx.serialization.Serializable

/**
 * This file contains the standard rules for BB2020 games (and its variants).
 * - Standard (Strict)
 * - Standard (FUMBBL-Compatible)
 * - BB7
 */
abstract class BB2020Rules : Rules(
    name = "Blood Bowl 2020 Rules",
    gameType = GameType.STANDARD
) {
}

@Serializable
class StandardBB2020Rules : BB2020Rules() {
    override val name: String = "Blood Bowl 2020 Rules (Strict)"
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
class FumbblBB2020Rules : BB2020Rules() {
    override val name: String
        get() = "Blood Bowl 2020 Rules (FUMBBL Compatible)"
    override val kickingPlayerBehavior: KickingPlayerBehavior = KickingPlayerBehavior.FUMBBL
    override val foulActionBehavior: FoulActionBehavior = FoulActionBehavior.FUMBBL
}

/**
 * Ruleset for the 2020 Blood Bowl Sevens game.
 * See Dungeon Bowl rulebook page 90 for more information.
 */
@Serializable
class BB72020Rules : BB2020Rules() {
    override val name: String = "Blood Bowl Sevens 2020 Rules"
    override val gameType: GameType = GameType.BB7
    override val fieldWidth: Int = 20
    override val fieldHeight: Int = 11
    override val wideZone: Int = 2
    override val endZone: Int = 1
    override val lineOfScrimmageHome: Int = 6
    override val lineOfScrimmageAway: Int = 13
    override val playersRequiredOnLineOfScrimmage: Int = 3
    override val maxPlayersInWideZone: Int = 1
    override val maxPlayersOnField: Int  = 7
    override val turnsPrHalf: Int = 6
    override val kickOffEventTable: KickOffTable = BB7KickOffEventTable
    override val injuryTable: InjuryTable = BB7StandardInjuryTable
    override val stuntyInjuryTable: InjuryTable = BB7StuntyInjuryTable
    override val prayersToNuffleTable: PrayersToNuffleTable = BB7PrayersToNuffleTable
    override val useApothecaryBehavior = UseApothecaryBehavior.BB7
    override val inducements = InducementSettings(DEFAULT_INDUCEMENTS).toBuilder().run {
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
                InducementType.BLOODWEISER_KEG -> { /* Do nothing */ }
                InducementType.SPECIAL_PLAY -> { /* Do nothing */ }
                InducementType.EXTRA_TEAM_TRAINING -> get(type)!!.price = 150_000
                InducementType.BRIBE -> { /* Do nothing */ }
                InducementType.WANDERING_APOTHECARY -> { /* Do nothing */ }
                InducementType.MORTUARY_ASSISTANT -> { /* Do nothing */ }
                InducementType.PLAGUE_DOCTOR -> { /* Do nothing */ }
                InducementType.RIOTOUS_ROOKIE -> get(type)!!.enabled = false
                InducementType.HALFLING_MASTER_CHEF -> { /* Do nothing */ }
                InducementType.STANDARD_MERCENARY_PLAYERS -> { /* Do nothing */ }
                InducementType.STAR_PLAYERS -> get(type)!!.enabled = false
                InducementType.INFAMOUS_COACHING_STAFF -> get(type)!!.enabled = false
                InducementType.WIZARD -> get(type)!!.enabled = false
                InducementType.BIASED_REFEREE -> get(type)!!.enabled = false
                InducementType.WAAAGH_DRUMMER -> get(type)!!.enabled = false
                InducementType.CAVORTING_NURGLINGS -> get(type)!!.enabled = false
                InducementType.DWARFEN_RUNESMITH -> get(type)!!.enabled = false
                InducementType.HALFLING_HOTPOT -> get(type)!!.enabled = false
                InducementType.MASTER_OF_BALLISTICS -> get(type)!!.enabled = false
                InducementType.EXPANDED_MERCENARY_PLAYERS -> { /* Do nothing */ }
                InducementType.GIANT -> get(type)!!.enabled = false
                InducementType.DESPERATE_MEASURES -> {
                    get(type)!!.let {
                        it.enabled = true
                        it.price = 50_000
                        it.max = 5
                    }
                }
            }
        }
        build()
    }
}
