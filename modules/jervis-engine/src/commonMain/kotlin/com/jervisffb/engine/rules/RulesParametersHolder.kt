package com.jervisffb.engine.rules

import com.jervisffb.engine.InducementSettings
import com.jervisffb.engine.TimerSettings
import com.jervisffb.engine.model.IntRangeSerializer
import com.jervisffb.engine.model.PitchType
import com.jervisffb.engine.rules.bb2020.BB2020SkillSettings
import com.jervisffb.engine.rules.bb2020.BB2020TeamActions
import com.jervisffb.engine.rules.bb2020.tables.BB2020ArgueTheCallTable
import com.jervisffb.engine.rules.bb2020.tables.BB2020CasualtyTable
import com.jervisffb.engine.rules.bb2020.tables.BB2020LastingInjuryTable
import com.jervisffb.engine.rules.bb2020.tables.BB2020RangeRuler
import com.jervisffb.engine.rules.bb2020.tables.BB2020StandardInjuryTable
import com.jervisffb.engine.rules.bb2020.tables.BB2020StandardKickOffEventTable
import com.jervisffb.engine.rules.bb2020.tables.BB2020StandardPrayersToNuffleTable
import com.jervisffb.engine.rules.bb2020.tables.BB2020StandardWeatherTable
import com.jervisffb.engine.rules.bb2020.tables.BB2020StuntyInjuryTable
import com.jervisffb.engine.rules.bb2025.DEFAULT_INDUCEMENTS_BB2025
import com.jervisffb.engine.rules.builder.BallSelectorRule
import com.jervisffb.engine.rules.builder.DiceRollOwner
import com.jervisffb.engine.rules.builder.FoulActionBehavior
import com.jervisffb.engine.rules.builder.GameType
import com.jervisffb.engine.rules.builder.GameVersion
import com.jervisffb.engine.rules.builder.KickingPlayerBehavior
import com.jervisffb.engine.rules.builder.NoStadium
import com.jervisffb.engine.rules.builder.StadiumRule
import com.jervisffb.engine.rules.builder.StandardBall
import com.jervisffb.engine.rules.builder.UndoActionBehavior
import com.jervisffb.engine.rules.builder.UseApothecaryBehavior
import com.jervisffb.engine.rules.common.actions.TeamActions
import com.jervisffb.engine.rules.common.pathfinder.BB2020PathFinder
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
 * Rule parameters shared between all rulesets.
 *
 * We use a parameter object to make it easy to create builders in all rule
 * classes.
 */
@kotlinx.serialization.Serializable
data class RulesParametersHolder(
    override val name: String = "Default",
    override val baseVersion: GameVersion = GameVersion.BB2020,
    override val gameType: GameType = GameType.STANDARD,
    override val timers: TimerSettings = TimerSettings.Companion.BB_CLOCK,
    override val inducements: InducementSettings = InducementSettings(DEFAULT_INDUCEMENTS_BB2025),
    @kotlinx.serialization.Serializable(IntRangeSerializer::class)
    override val moveRange: IntRange = 1..9,
    @kotlinx.serialization.Serializable(IntRangeSerializer::class)
    override val strengthRange: IntRange = 1..8,
    @kotlinx.serialization.Serializable(IntRangeSerializer::class)
    override val agilityRange: IntRange = 1 .. 6,
    @kotlinx.serialization.Serializable(IntRangeSerializer::class)
    override val passingRange: IntRange = 1.. 6,
    @Serializable(IntRangeSerializer::class)
    override val armorValueRange: IntRange = 3 .. 11,
    override val halfsPrGame: Int = 2,
    override val turnsPrHalf: Int = 8,
    override val hasExtraTime: Boolean = false,
    override val turnsInExtraTime: Int = 8,
    override val hasShootoutInExtraTime: Boolean = true,
    override val fieldWidth: Int = 26,
    override val fieldHeight: Int = 15,
    override val wideZone: Int = 4,
    override val endZone: Int = 1,
    override val lineOfScrimmageHome: Int = 12,
    override val lineOfScrimmageAway: Int = 13,
    override val playersRequiredOnLineOfScrimmage: Int = 3,
    override val maxPlayersInWideZone: Int = 2,
    override val maxPlayersOnField: Int  = 11,
    override val stadium: StadiumRule = NoStadium,
    override val ballSelectorRule: BallSelectorRule = StandardBall,
    override val pitchType: PitchType = PitchType.STANDARD,
    override val matchEventsEnabled: Boolean = false,
    override val kickOffEventTable: KickOffTable = BB2020StandardKickOffEventTable,
    override val prayersToNufflePrice: Int = 50_000,
    override val prayersToNuffleEnabled: Boolean = true,
    override val prayersToNuffleTable: PrayersToNuffleTable = BB2020StandardPrayersToNuffleTable,
    override val weatherTable: WeatherTable = BB2020StandardWeatherTable,
    override val injuryTable: InjuryTable = BB2020StandardInjuryTable,
    override val stuntyInjuryTable: InjuryTable = BB2020StuntyInjuryTable,
    override val casualtyTable: CasualtyTable = BB2020CasualtyTable,
    override val lastingInjuryTable: LastingInjuryTable = BB2020LastingInjuryTable,
    override val argueTheCallTable: ArgueTheCallTable = BB2020ArgueTheCallTable,
    override val randomDirectionTemplate: RandomDirectionTemplate = RandomDirectionTemplate,
    override val rangeRuler: RangeRuler = BB2020RangeRuler,
    override val teamActions: TeamActions = BB2020TeamActions(),
    override val rushesPrAction: Int = 2,
    override val allowMultipleTeamRerollsPrTurn: Boolean = true,
    override val standingUpTarget: Int = 4, // See page 44 in the rule book
    override val moveRequiredForStandingUp: Int = 3,
    override val secureTheBallTarget: Int = 2, // Blood Bowl Season 3 announcement blogposts
    override val pathFinder: PathFinder = BB2020PathFinder(),
    override val undoActionBehavior: UndoActionBehavior = UndoActionBehavior.ONLY_NON_RANDOM_ACTIONS,
    override val diceRollsOwner: DiceRollOwner = DiceRollOwner.ROLL_ON_SERVER,
    override val foulActionBehavior: FoulActionBehavior = FoulActionBehavior.BB2025,
    override val kickingPlayerBehavior: KickingPlayerBehavior = KickingPlayerBehavior.STRICT,
    override val useApothecaryBehavior: UseApothecaryBehavior = UseApothecaryBehavior.STANDARD,
    override val skillSettings: SkillSettings = BB2020SkillSettings(),
    override val allowPlayerEditsDuringGame: Boolean = false,
): RulesParameters
