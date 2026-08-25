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
import com.jervisffb.engine.rules.common.planner.ActionPlanner
import com.jervisffb.engine.rules.common.skills.SkillSettings
import com.jervisffb.engine.rules.common.tables.ArgueTheCallTable
import com.jervisffb.engine.rules.common.tables.CasualtyTable
import com.jervisffb.engine.rules.common.tables.DesperateMeasuresTable
import com.jervisffb.engine.rules.common.tables.InjuryTable
import com.jervisffb.engine.rules.common.tables.KickOffTable
import com.jervisffb.engine.rules.common.tables.LastingInjuryTable
import com.jervisffb.engine.rules.common.tables.PrayersToNuffleTable
import com.jervisffb.engine.rules.common.tables.RandomDirectionTemplate
import com.jervisffb.engine.rules.common.tables.RangeRuler
import com.jervisffb.engine.rules.common.tables.WeatherTable
import kotlinx.serialization.Serializable

/**
 * This class holds all parameters used by [Rules]. We have this to better
 * support UI builders that can modify a ruleset.
 *
 * Developer's Commentary:
 * All properties in this class are required and must not have default values.
 * This makes it possible for this class to be in the `core` module without
 * having to potentially reference BB2020 or BB2025 classes in other modules.
 * Each ruleset supplies all values through its own `DEFAULTS` companion.
 */
@Serializable
data class RulesParametersHolder(
    override val name: String,
    override val baseVersion: GameVersion,
    override val gameType: GameType,
    override val timers: TimerSettings,
    override val inducements: InducementSettings,
    @Serializable(IntRangeSerializer::class)
    override val moveRange: IntRange,
    @Serializable(IntRangeSerializer::class)
    override val strengthRange: IntRange,
    @Serializable(IntRangeSerializer::class)
    override val agilityRange: IntRange,
    @Serializable(IntRangeSerializer::class)
    override val passingRange: IntRange,
    @Serializable(IntRangeSerializer::class)
    override val armorValueRange: IntRange,
    override val halfsPrGame: Int,
    override val turnsPrHalf: Int,
    override val hasExtraTime: Boolean,
    override val turnsInExtraTime: Int,
    override val hasShootoutInExtraTime: Boolean,
    override val pitchWidth: Int,
    override val pitchHeight: Int,
    override val wideZone: Int,
    override val endZone: Int,
    override val lineOfScrimmageHome: Int,
    override val lineOfScrimmageAway: Int,
    override val playersRequiredOnLineOfScrimmage: Int,
    override val maxPlayersInWideZone: Int,
    override val maxPlayersOnPitch: Int,
    override val stadium: StadiumRule,
    override val ballSelectorRule: BallSelectorRule,
    override val pitchType: PitchType,
    override val matchEventsEnabled: Boolean,
    override val kickOffEventTable: KickOffTable,
    override val prayersToNufflePriceForUnderdog: Int,
    override val prayersToNuffleEnabledForUnderdogDuringPregame: Boolean,
    override val prayersToNuffleTable: PrayersToNuffleTable,
    override val desperateMeasuresTable: DesperateMeasuresTable,
    override val weatherTable: WeatherTable,
    override val injuryTable: InjuryTable,
    override val stuntyInjuryTable: InjuryTable,
    override val casualtyTable: CasualtyTable,
    override val lastingInjuryTable: LastingInjuryTable,
    override val argueTheCallTable: ArgueTheCallTable,
    override val randomDirectionTemplate: RandomDirectionTemplate,
    override val rangeRuler: RangeRuler,
    override val teamActions: TeamActions,
    override val rushesPrAction: Int,
    override val allowMultipleTeamRerollsPrTurn: Boolean,
    override val standingUpTarget: Int,
    override val moveRequiredForStandingUp: Int,
    override val secureTheBallTarget: Int,
    override val actionPlanner: ActionPlanner,
    override val undoActionBehavior: UndoActionBehavior,
    override val diceRollsOwner: DiceRollOwner,
    override val foulActionBehavior: FoulActionBehavior,
    override val kickingPlayerBehavior: KickingPlayerBehavior,
    override val useApothecaryBehavior: UseApothecaryBehavior,
    override val skillSettings: SkillSettings,
    override val allowPlayerEditsDuringGame: Boolean,
    override val canUseMultipleRerollsOnDicePools: Boolean,
) : RulesParameters
