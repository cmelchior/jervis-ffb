package com.jervisffb.test

import com.jervisffb.engine.InducementSettings
import com.jervisffb.engine.TimerSettings
import com.jervisffb.engine.actions.D3Result
import com.jervisffb.engine.actions.D8Result
import com.jervisffb.engine.actions.InducementSelection
import com.jervisffb.engine.actions.SelectMoveType
import com.jervisffb.engine.commands.Command
import com.jervisffb.engine.fsm.Node
import com.jervisffb.engine.fsm.Procedure
import com.jervisffb.engine.model.Ball
import com.jervisffb.engine.model.Direction
import com.jervisffb.engine.model.Game
import com.jervisffb.engine.model.PitchSquare
import com.jervisffb.engine.model.PitchType
import com.jervisffb.engine.model.Player
import com.jervisffb.engine.model.SkillId
import com.jervisffb.engine.model.Team
import com.jervisffb.engine.model.inducements.Apothecary
import com.jervisffb.engine.model.locations.Location
import com.jervisffb.engine.model.locations.OnPitchLocation
import com.jervisffb.engine.model.locations.PitchCoordinate
import com.jervisffb.engine.model.modifiers.DiceModifier
import com.jervisffb.engine.model.modifiers.StatModifier
import com.jervisffb.engine.rules.DiceRollType
import com.jervisffb.engine.rules.Rules
import com.jervisffb.engine.rules.RulesParameterBuilder
import com.jervisffb.engine.rules.builder.BallSelectorRule
import com.jervisffb.engine.rules.builder.DiceRollOwner
import com.jervisffb.engine.rules.builder.FoulActionBehavior
import com.jervisffb.engine.rules.builder.GameType
import com.jervisffb.engine.rules.builder.GameVersion
import com.jervisffb.engine.rules.builder.KickingPlayerBehavior
import com.jervisffb.engine.rules.builder.StadiumRule
import com.jervisffb.engine.rules.builder.UndoActionBehavior
import com.jervisffb.engine.rules.builder.UseApothecaryBehavior
import com.jervisffb.engine.rules.common.InducementRule
import com.jervisffb.engine.rules.common.SetupRule
import com.jervisffb.engine.rules.common.actions.BlockType
import com.jervisffb.engine.rules.common.actions.PlayerAction
import com.jervisffb.engine.rules.common.actions.TeamActions
import com.jervisffb.engine.rules.common.planner.ActionPlanner
import com.jervisffb.engine.rules.common.procedures.DieRoll
import com.jervisffb.engine.rules.common.procedures.DummyProcedure
import com.jervisffb.engine.rules.common.rerolls.TeamReroll
import com.jervisffb.engine.rules.common.skills.Duration
import com.jervisffb.engine.rules.common.skills.RerollSource
import com.jervisffb.engine.rules.common.skills.Skill
import com.jervisffb.engine.rules.common.skills.SkillSettings
import com.jervisffb.engine.rules.common.skills.SkillType
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

/**
 * Rules used for testing probability calculations.
 */
open class AbstractTestRules: Rules {
    override fun isSetupValid(state: Game, team: Team): List<SetupRule> = emptyList()
    override fun isInSetupArea(team: Team, location: PitchCoordinate): Boolean = false
    override fun canPlaceBallForKickoff(kickingTeam: Team, location: PitchSquare): Boolean = false
    override fun direction(d8: D8Result): Direction = Direction.UP
    override fun throwIn(from: PitchCoordinate, d3: D3Result): Direction = Direction.UP
    override fun throwIn(direction: Direction, d3: D3Result): Direction = Direction.UP
    override fun canCatch(player: Player): Boolean = false
    override fun canDeflect(player: Player): Boolean = false
    override fun canMarkPlayers(player: Player): Boolean = false
    override fun isOpen(player: Player): Boolean = false
    override fun isStanding(player: Player): Boolean = false
    override fun isSkillAvailable(player: Player, type: SkillType): Boolean = false
    override fun isDistracted(player: Player): Boolean = false
    override fun calculateMoveTypesAvailable(state: Game, player: Player): SelectMoveType? = null
    override fun isInjuried(player: Player): Boolean = false
    override fun isMarked(player: Player, location: Location): Boolean = false
    override fun isMarking(player: Player, target: Player): Boolean = false
    override fun isMarking(player: Player, target: Location): Boolean = false
    override fun calculateOffensiveAssists(attacker: Player, defender: Player): Int = 0
    override fun calculateDefensiveAssists(defender: Player, attacker: Player): Int = 0
    override fun canOfferAssist(assister: Player, target: Player): Boolean = false
    override fun addMarkedModifiers(
        game: Game,
        markedTeam: Team,
        square: PitchCoordinate,
        modifiers: MutableList<DiceModifier>,
        markedModifier: DiceModifier
    ) {
        // Do nothing
    }
    override fun getMarkingPlayers(game: Game, markedTeam: Team, square: PitchCoordinate): List<Player> = emptyList()
    override fun calculateMarks(game: Game, markedTeam: Team, square: OnPitchLocation): Int = 0
    override fun canUseTeamReroll(game: Game, player: Player?): Boolean = false
    override fun getPushOptions(pusher: Player, pushee: Player): Set<PitchCoordinate> = emptySet()
    override fun teamHasBall(team: Team, ball: Ball?): Boolean = false
    override fun getAvailableTeamRerolls(team: Team): List<RerollSource> = emptyList()
    override fun getAvailableActions(state: Game, player: Player): List<PlayerAction> = emptyList()
    override fun updatePlayerStat(player: Player, stat: StatModifier.Type) { /* Do nothing */ }
    override fun isStartOfHalf(state: Game): Boolean = false
    override fun createSkill(player: Player, skill: SkillId, expiresAt: Duration): Skill<*> = TODO()
    override fun isRerollAllowed(dicePool: List<DieRoll<*>>): Boolean = false
    override fun canBeRerolledByTeamReroll(type: DiceRollType): Boolean = false
    override val rightStuffMaxStrength: Int = 3
    override val proSuccessTarget: Int = 3
    override fun calculateLeaderRerollStatusChange(team: Team): Command? = null
    override fun getEndOfTurnResetCommands(player: Player): List<Command> = emptyList()
    override fun wasSecretWeaponOnPitchDuringDrive(player: Player): Boolean = false
    override fun setSecretWeaponOnPitchDuringDrive(player: Player, onPitch: Boolean): Command? = null
    override fun getLoneFoulerRerollSource(player: Player): RerollSource? = null
    override fun getAvailableBlockType(player: Player, isMultipleBlock: Boolean): List<BlockType> = emptyList()
    override fun createTeamApothecary(): Apothecary = TODO()
    override fun createTeamReroll(team: Team, index: Int): TeamReroll = TODO()
    override fun createLeaderTeamReroll(team: Team): TeamReroll = TODO()
    override fun createBrilliantCoachingReroll(team: Team): TeamReroll = TODO()
    override fun isInducementsValid(team: Team, inducements: List<InducementSelection<*>>): List<InducementRule> = emptyList()
    override fun isLeaderReroll(reroll: TeamReroll): Boolean = false
    override val standardBlockStep: Procedure = DummyProcedure
    override val fallingOverStep: Procedure = DummyProcedure
    override val knockedDownStep: Procedure = DummyProcedure
    override val kickOffTouchBackNode: Node = DummyProcedure.Dummy
    override val jumpStep: Procedure = DummyProcedure
    override val leapStep: Procedure = DummyProcedure
    override val pogoStep: Procedure = DummyProcedure
    override val secureTheBallStep: Procedure = DummyProcedure
    override val shadowingStep: Procedure = DummyProcedure
    override val tentaclesStep: Procedure = DummyProcedure
    override val hitAndRunStep: Procedure = DummyProcedure
    override val hailMaryPassStep: Procedure = DummyProcedure
    override val teamTurn: Procedure = DummyProcedure
    override val passStep: Procedure = DummyProcedure
    override val throwPlayerStep: Procedure = DummyProcedure
    override val applyInducementsStep: Procedure = DummyProcedure
    override val setupTeam: Procedure = DummyProcedure
    override val fullGameStep: Procedure = DummyProcedure
    override val kickOffStep: Procedure = DummyProcedure
    override val useApothecaryStep: Procedure = DummyProcedure
    override val chainsawFoulStep: Procedure = DummyProcedure
    override val kickOffDeviateRollStep: Procedure = DummyProcedure
    override val rushRoll: Procedure = DummyProcedure
    override val gameDrive: Procedure = DummyProcedure
    override val beingSentOff: Procedure = DummyProcedure
    override val movePlayerIntoSquare: Procedure = DummyProcedure
    override val patchUpPlayer: Procedure = DummyProcedure
    override val riskingInjuryRoll: Procedure = DummyProcedure
    override val dodgeRoll: Procedure = DummyProcedure
    override val takeRootRoll: Procedure = DummyProcedure
    override val boneHeadRoll: Procedure = DummyProcedure
    override val reallyStupidRoll: Procedure = DummyProcedure
    override val unchannelledFuryRoll: Procedure = DummyProcedure
    override val animalSavageryStep: Procedure = DummyProcedure
    override val argueTheCallRoll: Procedure = DummyProcedure
    override fun toBuilder(): RulesParameterBuilder = TODO()
    override val name: String = "Test Rules"
    override val baseVersion: GameVersion = GameVersion.BB2025
    override val gameType: GameType = GameType.STANDARD
    override val timers: TimerSettings get() = TODO()
    override val inducements: InducementSettings get() = TODO()
    override val moveRange: IntRange = 1..9
    override val strengthRange: IntRange = 1..8
    override val agilityRange: IntRange = 1..6
    override val passingRange: IntRange = 1..6
    override val armorValueRange: IntRange = 1..11
    override val halfsPrGame: Int = 2
    override val turnsPrHalf: Int = 8
    override val hasExtraTime: Boolean = false
    override val turnsInExtraTime: Int = 8
    override val hasShootoutInExtraTime: Boolean = false
    override val pitchWidth: Int = 26
    override val pitchHeight: Int = 15
    override val wideZone: Int = 4
    override val endZone: Int = 1
    override val lineOfScrimmageHome: Int = 12
    override val lineOfScrimmageAway: Int = 13
    override val playersRequiredOnLineOfScrimmage: Int = 3
    override val maxPlayersInWideZone: Int = 2
    override val maxPlayersOnPitch: Int = 11
    override val stadium: StadiumRule get() = TODO()
    override val ballSelectorRule: BallSelectorRule get() = TODO()
    override val pitchType: PitchType = PitchType.STANDARD
    override val matchEventsEnabled: Boolean = false
    override val kickOffEventTable: KickOffTable get() = TODO()
    override val prayersToNufflePriceForUnderdog: Int = 0
    override val prayersToNuffleEnabledForUnderdogDuringPregame: Boolean = false
    override val prayersToNuffleTable: PrayersToNuffleTable get() = TODO()
    override val desperateMeasuresTable: DesperateMeasuresTable get() = TODO()
    override val weatherTable: WeatherTable get() = TODO()
    override val injuryTable: InjuryTable get() = TODO()
    override val stuntyInjuryTable: InjuryTable get() = TODO()
    override val casualtyTable: CasualtyTable get() = TODO()
    override val lastingInjuryTable: LastingInjuryTable get() = TODO()
    override val argueTheCallTable: ArgueTheCallTable get() = TODO()
    override val randomDirectionTemplate: RandomDirectionTemplate get() = TODO()
    override val rangeRuler: RangeRuler get() = TODO()
    override val teamActions: TeamActions get() = TODO()
    override val rushesPrAction: Int = 2
    override val allowMultipleTeamRerollsPrTurn: Boolean = false
    override val standingUpTarget: Int = 2
    override val moveRequiredForStandingUp: Int = 3
    override val secureTheBallTarget: Int = 0
    override val actionPlanner: ActionPlanner get() = TODO()
    override val undoActionBehavior: UndoActionBehavior = UndoActionBehavior.ALLOWED
    override val diceRollsOwner: DiceRollOwner = DiceRollOwner.ROLL_ON_CLIENT
    override val foulActionBehavior: FoulActionBehavior = FoulActionBehavior.BB2025
    override val kickingPlayerBehavior: KickingPlayerBehavior = KickingPlayerBehavior.STRICT
    override val useApothecaryBehavior: UseApothecaryBehavior = UseApothecaryBehavior.STANDARD
    override val skillSettings: SkillSettings get() = TODO()
    override val allowPlayerEditsDuringGame: Boolean = false
    override val canUseMultipleRerollsOnDicePools: Boolean = false
}
