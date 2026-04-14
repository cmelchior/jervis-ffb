package com.jervisffb.engine.rules

import com.jervisffb.engine.InducementSettings
import com.jervisffb.engine.TimerSettings
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

/**
 *  Rules builder making it easier to create variants of
 */
abstract class RulesParameterBuilder(parameters: RulesParameters) {
    var name: String = parameters.name
    var gameVersion: GameVersion = parameters.baseVersion
    var gameType: GameType = parameters.gameType
    var timers: TimerSettings.Builder = parameters.timers.toBuilder()
    var inducements: InducementSettings.Builder = parameters.inducements.toBuilder()
    var moveRange: IntRange = parameters.moveRange
    var strengthRange: IntRange = parameters.strengthRange
    var agilityRange: IntRange = parameters.agilityRange
    var passingRange: IntRange = parameters.passingRange
    var armorValueRange: IntRange = parameters.armorValueRange
    var halfsPrGame: Int = parameters.halfsPrGame
    var turnsPrHalf: Int = parameters.turnsPrHalf
    var hasExtraTime: Boolean = parameters.hasExtraTime
    var turnsInExtraTime: Int = parameters.turnsInExtraTime
    var hasShootoutInExtraTime: Boolean = parameters.hasShootoutInExtraTime
    var pitchWidth: Int = parameters.pitchWidth
    var pitchHeight: Int = parameters.pitchHeight
    var wideZone: Int = parameters.wideZone
    var endZone: Int = parameters.endZone
    var lineOfScrimmageHome: Int = parameters.lineOfScrimmageHome
    var lineOfScrimmageAway: Int = parameters.lineOfScrimmageAway
    var playersRequiredOnLineOfScrimmage: Int = parameters.playersRequiredOnLineOfScrimmage
    var maxPlayersInWideZone: Int = parameters.maxPlayersInWideZone
    var maxPlayersOnPitch: Int = parameters.maxPlayersOnPitch
    var stadium: StadiumRule = parameters.stadium
    var ballSelectorRule: BallSelectorRule = parameters.ballSelectorRule
    var pitchType: PitchType = parameters.pitchType
    var matchEventsEnabled: Boolean = parameters.matchEventsEnabled
    var kickOffEventTable: KickOffTable = parameters.kickOffEventTable
    var prayersToNufflePrice: Int = parameters.prayersToNufflePrice
    var prayersToNuffleEnabled: Boolean = parameters.prayersToNuffleEnabled
    var prayersToNuffleTable: PrayersToNuffleTable = parameters.prayersToNuffleTable
    var weatherTable: WeatherTable = parameters.weatherTable
    var injuryTable: InjuryTable = parameters.injuryTable
    var stuntyInjuryTable: InjuryTable = parameters.stuntyInjuryTable
    var casualtyTable: CasualtyTable = parameters.casualtyTable
    var lastingInjuryTable: LastingInjuryTable = parameters.lastingInjuryTable
    var argueTheCallTable: ArgueTheCallTable = parameters.argueTheCallTable
    var randomDirectionTemplate: RandomDirectionTemplate = parameters.randomDirectionTemplate
    var rangeRuler: RangeRuler = parameters.rangeRuler
    var teamActions: TeamActions = parameters.teamActions
    var rushesPrAction: Int = parameters.rushesPrAction
    var allowMultipleTeamRerollsPrTurn: Boolean = parameters.allowMultipleTeamRerollsPrTurn
    var standingUpTarget: Int = parameters.standingUpTarget
    var moveRequiredForStandingUp: Int = parameters.moveRequiredForStandingUp
    var secureTheBallTarget: Int = parameters.secureTheBallTarget
    var undoActionBehavior: UndoActionBehavior = parameters.undoActionBehavior
    var diceRollsOwner: DiceRollOwner = parameters.diceRollsOwner
    var foulActionBehavior: FoulActionBehavior = parameters.foulActionBehavior
    var kickingPlayerBehavior: KickingPlayerBehavior = parameters.kickingPlayerBehavior
    var useApothecaryBehavior: UseApothecaryBehavior = parameters.useApothecaryBehavior
    var skillSettings: SkillSettings = parameters.skillSettings
    var allowPlayerEditsDuringGame: Boolean = parameters.allowPlayerEditsDuringGame

    fun buildParameters(): RulesParametersHolder {
        return RulesParametersHolder(
            name = name,
            baseVersion = gameVersion,
            gameType = gameType,
            timers = timers.build(),
            inducements = inducements.build(),
            moveRange = moveRange,
            strengthRange = strengthRange,
            agilityRange = agilityRange,
            passingRange = passingRange,
            armorValueRange = armorValueRange,
            halfsPrGame = halfsPrGame,
            turnsPrHalf = turnsPrHalf,
            hasExtraTime = hasExtraTime,
            turnsInExtraTime = turnsInExtraTime,
            hasShootoutInExtraTime = hasShootoutInExtraTime,
            pitchWidth = pitchWidth,
            pitchHeight = pitchHeight,
            wideZone = wideZone,
            endZone = endZone,
            lineOfScrimmageHome = lineOfScrimmageHome,
            lineOfScrimmageAway = lineOfScrimmageAway,
            playersRequiredOnLineOfScrimmage = playersRequiredOnLineOfScrimmage,
            maxPlayersInWideZone = maxPlayersInWideZone,
            maxPlayersOnPitch = maxPlayersOnPitch,
            stadium = stadium,
            ballSelectorRule = ballSelectorRule,
            pitchType = pitchType,
            matchEventsEnabled = matchEventsEnabled,
            kickOffEventTable = kickOffEventTable,
            prayersToNufflePrice = prayersToNufflePrice,
            prayersToNuffleEnabled = prayersToNuffleEnabled,
            prayersToNuffleTable = prayersToNuffleTable,
            weatherTable = weatherTable,
            injuryTable = injuryTable,
            stuntyInjuryTable = stuntyInjuryTable,
            casualtyTable = casualtyTable,
            lastingInjuryTable = lastingInjuryTable,
            argueTheCallTable = argueTheCallTable,
            randomDirectionTemplate = randomDirectionTemplate,
            rangeRuler = rangeRuler,
            teamActions = teamActions,
            rushesPrAction = rushesPrAction,
            allowMultipleTeamRerollsPrTurn = allowMultipleTeamRerollsPrTurn,
            standingUpTarget = standingUpTarget,
            moveRequiredForStandingUp = moveRequiredForStandingUp,
            secureTheBallTarget = secureTheBallTarget,
            undoActionBehavior = undoActionBehavior,
            diceRollsOwner = diceRollsOwner,
            foulActionBehavior = foulActionBehavior,
            kickingPlayerBehavior = kickingPlayerBehavior,
            useApothecaryBehavior = useApothecaryBehavior,
            skillSettings = skillSettings,
            allowPlayerEditsDuringGame = allowPlayerEditsDuringGame
        )
    }

    abstract fun build(): Rules
}
