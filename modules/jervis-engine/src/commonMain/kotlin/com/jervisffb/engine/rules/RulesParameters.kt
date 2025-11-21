package com.jervisffb.engine.rules

import com.jervisffb.engine.InducementSettings
import com.jervisffb.engine.TimerSettings
import com.jervisffb.engine.model.IntRangeSerializer
import com.jervisffb.engine.model.PitchType
import com.jervisffb.engine.rules.builder.BallSelectorRule
import com.jervisffb.engine.rules.builder.DiceRollOwner
import com.jervisffb.engine.rules.builder.FoulActionBehavior
import com.jervisffb.engine.rules.builder.GameType
import com.jervisffb.engine.rules.builder.GameVersion
import com.jervisffb.engine.rules.builder.KickingPlayerBehavior
import com.jervisffb.engine.rules.builder.StadiumRule
import com.jervisffb.engine.rules.builder.UndoActionBehavior
import com.jervisffb.engine.rules.builder.UseApothecaryBehavior
import com.jervisffb.engine.rules.common.actions.TeamActions
import com.jervisffb.engine.rules.common.pathfinder.PathFinder
import com.jervisffb.engine.rules.common.skills.SkillSettings
import com.jervisffb.engine.rules.common.tables.ArgueTheCallTable
import com.jervisffb.engine.rules.common.tables.CasualtyTable
import com.jervisffb.engine.rules.common.tables.InjuryTable
import com.jervisffb.engine.rules.common.tables.KickOffTable
import com.jervisffb.engine.rules.common.tables.LastingInjuryTable
import com.jervisffb.engine.rules.common.tables.PrayersToNuffleTable
import com.jervisffb.engine.rules.common.tables.RandomDirectionTemplate
import com.jervisffb.engine.rules.common.tables.RangeRuler
import com.jervisffb.engine.rules.common.tables.WeatherTable
import kotlinx.serialization.Serializable

/**
 * Interface describing all parameters used by the [Rules].
 *
 * This is an interface, so we can share the contract between [Rules],
 * subclasses of these and [RulesParameterBuilder].
 */
sealed interface RulesParameters {
    // Name of the rule set
    val name: String
    // Which base version of Blood Bowl is this ruleset based on
    val baseVersion: GameVersion
    // What type of game is this ruleset intended for.
    val gameType: GameType
    // Which timer settings are in place for this game
    val timers: TimerSettings
    // Which inducements are available in this game.
    val inducements: InducementSettings

    // Characteristic limits
    // See page 28 in the BB2020 rulebook and pxage 37 in the BB2025 rulebook
    @Serializable(IntRangeSerializer::class)
    val moveRange: IntRange
    @Serializable(IntRangeSerializer::class)
    val strengthRange: IntRange
    @Serializable(IntRangeSerializer::class)
    val agilityRange: IntRange
    @Serializable(IntRangeSerializer::class)
    val passingRange: IntRange
    @Serializable(IntRangeSerializer::class)
    val armorValueRange: IntRange

    // Game length settings
    val halfsPrGame: Int
    val turnsPrHalf: Int
    val hasExtraTime: Boolean
    val turnsInExtraTime: Int
    val hasShootoutInExtraTime: Boolean

    // Field (Defined as being horizontal with a home team on the right, away team on the left)

    // Total width of the field
    val fieldWidth: Int
    // Total height of the field
    val fieldHeight: Int
    // Height of the Wide Zone at the top and bottom of the field
    val wideZone: Int
    // Width of the End Zone at each end of the field where the ball is scored.
    val endZone: Int
    // X-coordinates for the line of scrimmage for the home team. It is zero-indexed.
    val lineOfScrimmageHome: Int
    // X-coordinate for the line of scrimmage for the away team. It is zero-indexed.
    val lineOfScrimmageAway: Int
    // During the setup, how many players must be placed on the Line of Scrimmage inside
    // the Center Field.
    val playersRequiredOnLineOfScrimmage: Int
    // How many players are allowed in each wide zone during setup
    val maxPlayersInWideZone: Int
    // Default max number of players on the field. Skills and effects might change this
    val maxPlayersOnField: Int

    // Stadium / Pitch / Ball rules / Match Events
    val stadium: StadiumRule
    // How is the ball being used for the game selected
    val ballSelectorRule: BallSelectorRule
    // Which pitch is used for this game
    val pitchType: PitchType
    // Match Events (See page XX)
    val matchEventsEnabled: Boolean

    // Tables
    val kickOffEventTable: KickOffTable
    // This defines the BB2020 behavior where Prayers To Nuffle are rolled during the Pre-game Sequence.
    // BB2025 Prayers are defined under inducements.
    val prayersToNufflePrice: Int
    val prayersToNuffleEnabled: Boolean
    val prayersToNuffleTable: PrayersToNuffleTable
    val weatherTable: WeatherTable
    val injuryTable: InjuryTable
    val stuntyInjuryTable: InjuryTable
    val casualtyTable: CasualtyTable
    val lastingInjuryTable: LastingInjuryTable
    val argueTheCallTable: ArgueTheCallTable

    // Templates
    val randomDirectionTemplate: RandomDirectionTemplate
    val rangeRuler: RangeRuler

    // Team Actions
    val teamActions: TeamActions
    val rushesPrAction: Int
    val allowMultipleTeamRerollsPrTurn: Boolean
    // Dice roll targets defined in the rulebook
    val standingUpTarget: Int
    val moveRequiredForStandingUp: Int
    val secureTheBallTarget: Int

    // Defines how the paths between locations on the field are calculated. This can be rules-specific,
    // since it might involve the use of skills.
    val pathFinder: PathFinder
    // Behavior customization, .e.g. allow the rules to specify which Procedure should
    // be used for certain aspects of the game
    // Whether coaches are allowed to undo actions, and to what degree.
    val undoActionBehavior: UndoActionBehavior
    // Who is responsible for rolling dice or taking random actions.
    val diceRollsOwner: DiceRollOwner
    // Probably need to replace this with a reference to the FoulProcedure
    val foulActionBehavior: FoulActionBehavior
    // Probably need to replace this with a reference to the KickProcedure
    val kickingPlayerBehavior: KickingPlayerBehavior
    // Which procedure to use when deciding and using an apothecary.
    // The rules differ between BB7 and Standard, but it is unclear if we want two different
    // procedures for this, but as there are multiple differences (who they apply to + "Patching-up" section).
    // Keep them separate for now.
    val useApothecaryBehavior: UseApothecaryBehavior
    // Configure skills available, their behaviour and which category they belong to.
    val skillSettings: SkillSettings
    // If `true`, coaches are allowed to edit their own team while the game is in progress.
    // (For now, this is just a local setting that doesn't work in networked games)
    val allowPlayerEditsDuringGame: Boolean
}

