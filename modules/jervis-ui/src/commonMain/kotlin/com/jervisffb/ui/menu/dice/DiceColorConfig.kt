package com.jervisffb.ui.menu.dice

import com.jervisffb.engine.actions.DBlockResult
import com.jervisffb.engine.actions.DieResult
import com.jervisffb.engine.ext.d16
import com.jervisffb.engine.ext.d3
import com.jervisffb.engine.ext.d6
import com.jervisffb.engine.ext.d8
import com.jervisffb.engine.rules.DiceRollType
import com.jervisffb.ui.game.icons.DiceColor

/**
 * Configuration for a single dice roll type, including how to display it and what colors are available.
 */
data class DiceRollTypeConfig(
    val rollType: DiceRollType,
    val label: String,
    val representativeDie: DieResult,
    val availableColors: List<DiceColor>,
    val defaultColor: DiceColor,
) {
    val settingsKey: String = "jervis.diceColor.${rollType.name.lowercase()}"
}

/**
 * Abstract configuration class mapping [DiceRollType] values to their visual representation and
 * available colors. Designed to be extended per ruleset (BB2020, BB2025, etc.).
 */
abstract class DiceColorConfig {
    abstract val entries: List<DiceRollTypeConfig>

    fun configFor(rollType: DiceRollType): DiceRollTypeConfig =
        entries.find { it.rollType == rollType } ?: error("No config for: $rollType")
}

/**
 * BB2025 dice color configuration. Maps all [DiceRollType] values to their die type and available
 * colors.
 *
 * For now, all dice roll types must be configured, but it should probably be extended to allowing
 * disabling some of them for the rulesets where they don't make sense.
 */
object BB2025DiceColorConfig : DiceColorConfig() {

    private val d6Colors = listOf(
        DiceColor.BLACK,
        DiceColor.BLUE,
        DiceColor.BROWN,
        DiceColor.RED,
        DiceColor.WHITE,
        DiceColor.YELLOW,
    )
    private val singleColor = listOf(DiceColor.DEFAULT)

    // Dice face shown to the user
    private val d6 = 6.d6
    private val d3 = 3.d3
    private val d8 = 8.d8
    private val d16 = 16.d16
    private val block = DBlockResult.allOptions().first()

    // Order of list will also be used by the game interface, so keep these in alphabetical order.
    override val entries: List<DiceRollTypeConfig> = DiceRollType.entries.map { rollType ->
        when (rollType) {
            DiceRollType.ACCURACY -> DiceRollTypeConfig(rollType, "Accuracy", d6, d6Colors, DiceColor.DEFAULT)
            DiceRollType.ARMOUR -> DiceRollTypeConfig(rollType, "Armour", d6, d6Colors, DiceColor.RED)
            DiceRollType.BAD_HABITS -> DiceRollTypeConfig(rollType, "Bad Habits", d3, d6Colors, DiceColor.WHITE)
            DiceRollType.BB7_APOTHECARY -> DiceRollTypeConfig(rollType, "Apothecary (Sevens)", d6, d6Colors, DiceColor.DEFAULT)
            DiceRollType.BLITZ -> DiceRollTypeConfig(rollType, "Blitz", d6, d6Colors, DiceColor.WHITE)
            DiceRollType.BLOCK -> DiceRollTypeConfig(rollType, "Block", block, singleColor, DiceColor.DEFAULT)
            DiceRollType.BLOODLUST -> DiceRollTypeConfig(rollType, "Bloodlust", d6, d6Colors, DiceColor.DEFAULT)
            DiceRollType.BONE_HEAD -> DiceRollTypeConfig(rollType, "Bone Head", d6, d6Colors, DiceColor.DEFAULT)
            DiceRollType.BOUNCE -> DiceRollTypeConfig(rollType, "Bounce", d8, singleColor, DiceColor.DEFAULT)
            DiceRollType.BREATHE_FIRE -> DiceRollTypeConfig(rollType, "Breathe Fire", d6, d6Colors, DiceColor.DEFAULT)
            DiceRollType.BRILLIANT_COACHING -> DiceRollTypeConfig(rollType, "Brilliant Coaching", d6, d6Colors, DiceColor.WHITE)
            DiceRollType.CASUALTY -> DiceRollTypeConfig(rollType, "Casualty", d16, singleColor, DiceColor.DEFAULT)
            DiceRollType.CATCH -> DiceRollTypeConfig(rollType, "Catch", d6, d6Colors, DiceColor.DEFAULT)
            DiceRollType.CHAINSAW -> DiceRollTypeConfig(rollType, "Chainsaw", d6, d6Colors, DiceColor.DEFAULT)
            DiceRollType.CHARGE -> DiceRollTypeConfig(rollType, "Charge", d6, d6Colors, DiceColor.WHITE)
            DiceRollType.CHEERING_FANS -> DiceRollTypeConfig(rollType, "Cheering Fans", d6, d6Colors, DiceColor.WHITE)
            DiceRollType.DAUNTLESS -> DiceRollTypeConfig(rollType, "Dauntless", d6, d6Colors, DiceColor.DEFAULT)
            DiceRollType.DEVIATE -> DiceRollTypeConfig(rollType, "Deviate", d8, singleColor, DiceColor.DEFAULT)
            DiceRollType.DODGE -> DiceRollTypeConfig(rollType, "Dodge", d6, d6Colors, DiceColor.YELLOW)
            DiceRollType.DODGY_SNACK_EFFECT -> DiceRollTypeConfig(rollType, "Dodgy Snack Effect", d6, d6Colors, DiceColor.WHITE)
            DiceRollType.DODGY_SNACK_ROLL_OFF -> DiceRollTypeConfig(rollType, "Dodgy Snack Roll-Off", d6, d6Colors, DiceColor.WHITE)
            DiceRollType.FAN_FACTOR -> DiceRollTypeConfig(rollType, "Fan Factor", d3, d6Colors, DiceColor.WHITE)
            DiceRollType.FOUL_APPEARANCE -> DiceRollTypeConfig(rollType, "Foul Appearance", d6, d6Colors, DiceColor.DEFAULT)
            DiceRollType.HYPNOTIC_GAZE -> DiceRollTypeConfig(rollType, "Hypnotic Gaze", d6, d6Colors, DiceColor.DEFAULT)
            DiceRollType.INJURY -> DiceRollTypeConfig(rollType, "Injury", d6, d6Colors, DiceColor.BLUE)
            DiceRollType.INTERCEPTION -> DiceRollTypeConfig(rollType, "Interception", d6, d6Colors, DiceColor.DEFAULT)
            DiceRollType.JUMP -> DiceRollTypeConfig(rollType, "Jump", d6, d6Colors, DiceColor.YELLOW)
            DiceRollType.JUMP_UP -> DiceRollTypeConfig(rollType, "Jump Up", d6, d6Colors, DiceColor.YELLOW)
            DiceRollType.KICK_OFF_TABLE -> DiceRollTypeConfig(rollType, "Kick-Off Table", d6, d6Colors, DiceColor.WHITE)
            DiceRollType.LANDING -> DiceRollTypeConfig(rollType, "Landing", d6, d6Colors, DiceColor.DEFAULT)
            DiceRollType.LASTING_INJURY -> DiceRollTypeConfig(rollType, "Lasting Injury", d6, d6Colors, DiceColor.YELLOW)
            DiceRollType.LEAP -> DiceRollTypeConfig(rollType, "Leap", d6, d6Colors, DiceColor.YELLOW)
            DiceRollType.LONER -> DiceRollTypeConfig(rollType, "Loner", d6, d6Colors, DiceColor.DEFAULT)
            DiceRollType.OFFICIOUS_REF_FAN_FACTOR -> DiceRollTypeConfig(rollType, "Officious Ref (Fan Factor)", d6, d6Colors, DiceColor.WHITE)
            DiceRollType.OFFICIOUS_REF_REFEREE -> DiceRollTypeConfig(rollType, "Officious Ref (Referee)", d6, d6Colors, DiceColor.DEFAULT)
            DiceRollType.PASS -> DiceRollTypeConfig(rollType, "Pass", d6, d6Colors, DiceColor.DEFAULT)
            DiceRollType.PASSING_INTERFERENCE -> DiceRollTypeConfig(rollType, "Passing Interference", d6, d6Colors, DiceColor.DEFAULT)
            DiceRollType.PICKUP -> DiceRollTypeConfig(rollType, "Pickup", d6, d6Colors, DiceColor.DEFAULT)
            DiceRollType.PITCH_INVASION_FAN_FACTOR -> DiceRollTypeConfig(rollType, "Pitch Invasion (Fan Factor)", d6, d6Colors, DiceColor.WHITE)
            DiceRollType.PITCH_INVASION_PLAYERS_AFFECTED -> DiceRollTypeConfig(rollType, "Pitch Invasion (Players Affected)", d3, d6Colors, DiceColor.WHITE)
            DiceRollType.POGO -> DiceRollTypeConfig(rollType, "Pogo Stick", d6, d6Colors, DiceColor.YELLOW)
            DiceRollType.PRAYERS_TO_NUFFLE -> DiceRollTypeConfig(rollType, "Prayers to Nuffle", d16, singleColor, DiceColor.DEFAULT)
            DiceRollType.PRO -> DiceRollTypeConfig(rollType, "Pro", d6, d6Colors, DiceColor.DEFAULT)
            DiceRollType.PROJECTILE_VOMIT -> DiceRollTypeConfig(rollType, "Projectile Vomit", d6, d6Colors, DiceColor.DEFAULT)
            DiceRollType.QUALITY -> DiceRollTypeConfig(rollType, "Quality", d6, d6Colors, DiceColor.DEFAULT)
            DiceRollType.QUICK_SNAP -> DiceRollTypeConfig(rollType, "Quick Snap", d3, d6Colors, DiceColor.WHITE)
            DiceRollType.REALLY_STUPID -> DiceRollTypeConfig(rollType, "Really Stupid", d6, d6Colors, DiceColor.DEFAULT)
            DiceRollType.REGENERATION -> DiceRollTypeConfig(rollType, "Regeneration", d6, d6Colors, DiceColor.DEFAULT)
            DiceRollType.RUSH -> DiceRollTypeConfig(rollType, "Rush", d6, d6Colors, DiceColor.YELLOW)
            DiceRollType.SCATTER -> DiceRollTypeConfig(rollType, "Scatter", d8, singleColor, DiceColor.DEFAULT)
            DiceRollType.SECURE_THE_BALL -> DiceRollTypeConfig(rollType, "Secure the Ball", d6, d6Colors, DiceColor.DEFAULT)
            DiceRollType.SHADOWING -> DiceRollTypeConfig(rollType, "Shadowing", d6, d6Colors, DiceColor.DEFAULT)
            DiceRollType.SOLID_DEFENSE -> DiceRollTypeConfig(rollType, "Solid Defense", d3, d6Colors, DiceColor.WHITE)
            DiceRollType.STANDING_UP -> DiceRollTypeConfig(rollType, "Standing Up", d6, d6Colors, DiceColor.DEFAULT)
            DiceRollType.STEADY_FOOTING -> DiceRollTypeConfig(rollType, "Steady Footing", d6, d6Colors, DiceColor.DEFAULT)
            DiceRollType.SUDDEN_DEATH -> DiceRollTypeConfig(rollType, "Sudden Death", d6, d6Colors, DiceColor.DEFAULT)
            DiceRollType.SWELTERING_HEAT -> DiceRollTypeConfig(rollType, "Sweltering Heat", d3, d6Colors, DiceColor.WHITE)
            DiceRollType.SWOOP_DIRECTION -> DiceRollTypeConfig(rollType, "Swoop Direction", d3, d6Colors, DiceColor.DEFAULT)
            DiceRollType.SWOOP_DISTANCE -> DiceRollTypeConfig(rollType, "Swoop Distance", d6, d6Colors, DiceColor.DEFAULT)
            DiceRollType.TAKE_ROOT -> DiceRollTypeConfig(rollType, "Take Root", d6, d6Colors, DiceColor.DEFAULT)
            DiceRollType.THROWIN_DIRECTION -> DiceRollTypeConfig(rollType, "Throw-In Direction", d8, singleColor, DiceColor.DEFAULT)
            DiceRollType.THROWIN_DISTANCE -> DiceRollTypeConfig(rollType, "Throw-In Distance", d16, singleColor, DiceColor.DEFAULT)
            DiceRollType.THROW_A_ROCK -> DiceRollTypeConfig(rollType, "Throw a Rock", d6, d6Colors, DiceColor.WHITE)
            DiceRollType.TREACHEROUS_TRAPDOOR -> DiceRollTypeConfig(rollType, "Treacherous Trapdoor", d6, d6Colors, DiceColor.DEFAULT)
            DiceRollType.UNCHANNELLED_FURY -> DiceRollTypeConfig(rollType, "Unchannelled Fury", d6, d6Colors, DiceColor.DEFAULT)
            DiceRollType.WEATHER -> DiceRollTypeConfig(rollType, "Weather", d6, d6Colors, DiceColor.WHITE)
        }
    }
}
