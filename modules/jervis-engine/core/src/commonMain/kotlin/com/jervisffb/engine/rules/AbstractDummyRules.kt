package com.jervisffb.engine.rules

import com.jervisffb.engine.InducementSettings
import com.jervisffb.engine.TimerSettings
import com.jervisffb.engine.actions.D3Result
import com.jervisffb.engine.actions.D8Result
import com.jervisffb.engine.actions.InducementSelection
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
import com.jervisffb.engine.rules.common.pathfinder.PathFinder
import com.jervisffb.engine.rules.common.procedures.DieRoll
import com.jervisffb.engine.rules.common.rerolls.TeamReroll
import com.jervisffb.engine.rules.common.skills.Duration
import com.jervisffb.engine.rules.common.skills.RerollSource
import com.jervisffb.engine.rules.common.skills.Skill
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

class AbstractDummyRules : Rules {
    /**
     * Checks if a given setup is valid. If not valid, a list of broken rules
     * will be returned. If the setup is valid, an empty list is returned.
     */
    override fun isSetupValid(state: Game, team: Team): List<SetupRule> {
        TODO("Not yet implemented")
    }

    /**
     * Returns whether the given location is in the valid setup area for a given
     * team. While this is described as a bit different between Standard and
     * BB7, it generalizes to the area up to the team's Line of Scrimmage.
     */
    override fun isInSetupArea(team: Team, location: PitchCoordinate): Boolean {
        TODO("Not yet implemented")
    }

    /**
     * Returns whether a given location is valid for placing the ball during
     * kick-off.
     *
     * For Standard and BB7, this generalizes to all locations _not_ in the area
     * between the End Zone and kicking teams Line of Scrimmage. In particular,
     * it allows you to place the ball in all of any configured "No Man's Land".
     *
     * This is in line with the Designer's Commentary, May 2024, page 10.
     */
    override fun canPlaceBallForKickoff(kickingTeam: Team, location: PitchSquare): Boolean {
        TODO("Not yet implemented")
    }

    /**
     * Roll on the random direction template.
     *
     * See page 25 in the BB2025 rulebook.
     */
    override fun direction(d8: D8Result): Direction {
        TODO("Not yet implemented")
    }

    /**
     * Returns the result of rolling a direction using the Throw-in
     * template when attempting to throw a ball in after it went out-of-bounds
     * (or Random Direction template in case of corners).
     *
     * See page 26 in the BB2025 rulebook.
     */
    override fun throwIn(from: PitchCoordinate, d3: D3Result): Direction {
        TODO("Not yet implemented")
    }

    /**
     * Returns the result of rolling on the throw-in template when it is put
     * down anywhere on the field and pointed in a specific direction.
     *
     * See page 26 in the BB2025 rulebook.
     */
    override fun throwIn(direction: Direction, d3: D3Result): Direction {
        TODO("Not yet implemented")
    }

    /**
     * Returns whether a player is eligible for catching a ball that landed in
     * their location.
     */
    override fun canCatch(player: Player): Boolean {
        TODO("Not yet implemented")
    }

    /**
     * Returns whether a player can deflect a ball if it is thrown over them.
     */
    override fun canDeflect(player: Player): Boolean {
        TODO("Not yet implemented")
    }

    /**
     * Return `true` if this player is able to mark other players. `false` if
     * not.
     */
    override fun canMarkPlayers(player: Player): Boolean {
        TODO("Not yet implemented")
    }

    /**
     * Returns `true` if the player is considered "Open" as described on
     * page 38 in the BB2025 rulebook, `false` if not.
     */
    override fun isOpen(player: Player): Boolean {
        TODO("Not yet implemented")
    }

    /**
     * Returns `true` if the player is considered "Standing" as described
     * on page 38 in the BB2025 rulebook, `false` if not.
     */
    override fun isStanding(player: Player): Boolean {
        TODO("Not yet implemented")
    }

    /**
     * Returns `true` if the player is considered "Distracted" as described on
     * page 38 in the BB2025 rulebook.
     */
    override fun isDistracted(player: Player): Boolean {
        TODO("Not yet implemented")
    }

    /**
     * Returns `true` if this player has a state that is considered an "Injury"
     * This is mostly used for UI purposes.
     */
    override fun isInjuried(player: Player): Boolean {
        TODO("Not yet implemented")
    }

    /**
     * Returns `true` if the player is considered `Marked` as described on
     * page 26 in the rulebook.
     *
     * @param player The player that is checked for marks.
     * @param location The location the player is in. Can be overridden to fake the player
     *     being in another location (used, e.g., when checking if dodging is needed).
     */
    override fun isMarked(player: Player, location: Location): Boolean {
        TODO("Not yet implemented")
    }

    /**
     * Returns `true` if [player] count as marking [target], `false` if not.
     */
    override fun isMarking(player: Player, target: Player): Boolean {
        TODO("Not yet implemented")
    }

    /**
     * Returns `true` if [player] count as marking the given location, `false` if not.
     */
    override fun isMarking(player: Player, target: Location): Boolean {
        TODO("Not yet implemented")
    }

    /**
     * Calculate how many offensive assists the [attacker] has if all assists
     * are provided.
     *
     * See page 57 in the BB2020 rulebook:
     * - Must be marking defender
     * - Cannot assist if being marked themselves (by someone other than the defender)
     *
     * @param attacker The attacking player
     * @param defender the defending player
     */
    override fun calculateOffensiveAssists(attacker: Player, defender: Player): Int {
        TODO("Not yet implemented")
    }

    /**
     * Calculate how many defensive assists the [defender] has if all assists
     * are provided.
     *
     * See page 57 in the rulebook:
     * - Must be marking attacker
     * - Cannot assist if being marked themselves (by someone other than the attacker)
     *
     * @param defender the defending player
     * @param attacker The attacking player
     */
    override fun calculateDefensiveAssists(defender: Player, attacker: Player): Int {
        TODO("Not yet implemented")
    }

    /**
     * Return `true` if the [assister] player can offer either an offensive or
     * defensive assist against [target], `false` if not.
     *
     * See page 57 in the BB2020 rulebook.
     * See page 61 in the BB2025 rulebook.
     */
    override fun canOfferAssist(assister: Player, target: Player): Boolean {
        TODO("Not yet implemented")
    }

    /**
     * Calculate how many marks are on [square] for a player on the [markedTeam].
     * Marks will be returned as modifiers in the [modifiers] list.
     */
    override fun addMarkedModifiers(
        game: Game,
        markedTeam: Team,
        square: PitchCoordinate,
        modifiers: MutableList<DiceModifier>,
        markedModifier: DiceModifier
    ) {
        TODO("Not yet implemented")
    }

    /**
     * Returns all players not from the [markedTeam] that can mark the [square].
     */
    override fun getMarkingPlayers(
        game: Game,
        markedTeam: Team,
        square: PitchCoordinate
    ): List<Player> {
        TODO("Not yet implemented")
    }

    /**
     * Calculates how many opponent players are marking a given pitch square.
     *
     * A player is marking a square if:
     * - The player has its tackle zones.
     * - The square is in the player's tackle zone.
     * - The player is standing.
     *
     * See page 26 in the BB2020 rulebook.
     */
    override fun calculateMarks(game: Game, markedTeam: Team, square: OnPitchLocation): Int {
        TODO("Not yet implemented")
    }

    /**
     * Returns `true` if the player can use a team re-roll at the current state
     * of the game, `false` if not.
     */
    override fun canUseTeamReroll(game: Game, player: Player?): Boolean {
        TODO("Not yet implemented")
    }

    /**
     * Return all locations you can choose from when pushing a player.
     * This only returns the normal push options and doesn't take into
     * account skills or if the squares are occupied.
     *
     * If that matters or not, it is up to the caller of this method.
     *
     * If pushing a player OUT_OF_BOUNDS is possible, all options to do so will
     * be possible and should be deteted using [PitchCoordinate.isOutOfBounds].
     */
    override fun getPushOptions(pusher: Player, pushee: Player): Set<PitchCoordinate> {
        TODO("Not yet implemented")
    }

    /**
     * Returns `true` if the team has a hold of the ball.
     *
     * @param ball if set, only this ball is checked, if `false` any ball is accepted.
     */
    override fun teamHasBall(team: Team, ball: Ball?): Boolean {
        TODO("Not yet implemented")
    }

    /**
     * Return all available rerolls available for a Team.
     *
     * If multiple versions of the same reroll are available, only one of them
     * is returned.
     *
     * E.g., if a Team has 3 regular, 1 Brilliant Coaching and 1 Mascot reroll,
     * 3 rerolls will be returned: (1xregular, 1xbrilliant, 1xmascot)
     */
    override fun getAvailableTeamRerolls(team: Team): List<RerollSource> {
        TODO("Not yet implemented")
    }

    /**
     * Returns all actions available to this player when they are activated.
     * This method should filter out actions that require targets that do not
     * exist, like Blitz or Foul (in BB2020).
     */
    override fun getAvailableActions(
        state: Game,
        player: Player
    ): List<PlayerAction> {
        TODO("Not yet implemented")
    }

    /**
     * When either a `baseX` or `XModifiers` stat value has been updated, this
     * method should also be called so the total player stat can be calculated
     * correctly.
     *
     * Example:
     *  - [Player.baseStrength]
     *  - [Player.strengthModifiers]
     *  - [Player.strength]
     */
    override fun updatePlayerStat(player: Player, stat: StatModifier.Type) {
        TODO("Not yet implemented")
    }

    /**
     * Returns `true` if the current game is the start of a half, `false` if
     * not. Start of Extra Time will also return `false`.
     */
    override fun isStartOfHalf(state: Game): Boolean {
        TODO("Not yet implemented")
    }

    /**
     * Create a skill instance from it's [SkillId].
     *
     * Skills might change subtly between rule versions; for that reason, we
     * need a single place to look up skill definitions from their id (since we
     * might want to support teams across multiple rulesets).
     */
    override fun createSkill(
        player: Player,
        skill: SkillId,
        expiresAt: Duration
    ): Skill<*> {
        TODO("Not yet implemented")
    }

    /**
     * Returns `true` if rerolls of some dice in the dice pool are still allowed.
     * Note; this doesn't mean that a reroll is available, just that it is allowed
     * if possible.
     */
    override fun isRerollAllowed(dicePool: List<DieRoll<*>>): Boolean {
        TODO("Not yet implemented")
    }

    /**
     * Return `true` if a Team Reroll is allowed to re-roll this type of roll.
     *
     * We keep this method here as some skills offer a re-roll that is
     * functionaly the same as a Team Reroll. They should all go through this
     * method.
     */
    override fun canBeRerolledByTeamReroll(type: DiceRollType): Boolean {
        TODO("Not yet implemented")
    }

    /**
     * Maximum strength a player with the "Right Stuff" skill can have to be
     * throwable. Ruleset-specific: BB2020 caps at 3, BB2025 has no cap.
     */
    override val rightStuffMaxStrength: Int
        get() = TODO("Not yet implemented")

    /**
     * The target roll for a successful Pro re-roll. BB2025 uses 3+ on a D6.
     */
    override val proSuccessTarget: Int
        get() = TODO("Not yet implemented")

    /**
     * When a player with the Leader skill is removed from the pitch, return
     * the command that updates the team's Leader re-roll state (disable/remove),
     * or `null` if no change is needed. Each ruleset provides its own logic.
     */
    override fun calculateLeaderRerollStatusChange(team: Team): Command? {
        TODO("Not yet implemented")
    }

    /**
     * Commands to run at end-of-turn to reset per-turn skill counters for the
     * given player. BB2020 returns an empty list; BB2025 returns commands like
     * `ResetShadowingSkill` when the player has the corresponding skill.
     */
    override fun getEndOfTurnResetCommands(player: Player): List<Command> {
        TODO("Not yet implemented")
    }

    /**
     * Whether the given player had a Secret Weapon on the pitch during the
     * current drive. BB2025-only concept; BB2020 always returns `false`.
     */
    override fun wasSecretWeaponOnPitchDuringDrive(player: Player): Boolean {
        TODO("Not yet implemented")
    }

    /**
     * Command to set whether the given player's Secret Weapon was on the
     * pitch during the current drive. BB2025-only concept; BB2020 returns
     * `null`.
     */
    override fun setSecretWeaponOnPitchDuringDrive(player: Player, onPitch: Boolean): Command? {
        TODO("Not yet implemented")
    }

    /**
     * Returns the [RerollSource] to use for a Lone Fouler re-roll for the
     * given player, or `null` if unavailable (or not supported by ruleset).
     */
    override fun getLoneFoulerRerollSource(player: Player): RerollSource? {
        TODO("Not yet implemented")
    }

    /**
     * Return all block types and special actions that can replace a block,
     * that is available to the given player.
     */
    override fun getAvailableBlockType(player: Player, isMultipleBlock: Boolean): List<BlockType> {
        TODO("Not yet implemented")
    }

    /** Creates a standard team apothecary */
    override fun createTeamApothecary(): Apothecary {
        TODO("Not yet implemented")
    }

    /**
     * Creates a standard Team reroll.
     * [index] is used to identify the reroll. Can normally just be the index
     * of the reroll in the team's reroll list.
     */
    override fun createTeamReroll(team: Team, index: Int): TeamReroll {
        TODO("Not yet implemented")
    }

    /**
     * Creates the reroll created by having a Leader on the pitch when a drive
     * starts.
     */
    override fun createLeaderTeamReroll(team: Team): TeamReroll {
        TODO("Not yet implemented")
    }

    /**
     * Create the reroll created by rolling "Brilliant Coaching" on the
     * kick-off table.
     */
    override fun createBrilliantCoachingReroll(team: Team): TeamReroll {
        TODO("Not yet implemented")
    }

    /**
     * Verify if the lists of inducements are valid for the given team.
     * If yes, an empty list is returned, otherwise a list of broken rules.
     */
    override fun isInducementsValid(
        team: Team,
        inducements: List<InducementSelection<*>>
    ): List<InducementRule> {
        TODO("Not yet implemented")
    }

    /**
     * Returns `true` if the given reroll is a Leader reroll, `false` if not.
     */
    override fun isLeaderReroll(reroll: TeamReroll): Boolean {
        TODO("Not yet implemented")
    }

    /**
     * The procedure resolving a standard block after a target has been
     * selected. In BB2020 this is a multi-step block sequence, in BB2025 it
     * is the single-block flow.
     */
    override val standardBlockStep: Procedure
        get() = TODO("Not yet implemented")

    /** The procedure resolving a player falling over (failed rush/dodge, etc.). */
    override val fallingOverStep: Procedure
        get() = TODO("Not yet implemented")

    /**
     * The procedure resolving a player being knocked down (e.g. from a chainsaw,
     * breathe fire, or animal savagery result).
     */
    override val knockedDownStep: Procedure
        get() = TODO("Not yet implemented")

    /**
     * The node inside the kick-off event flow that resolves a touchback.
     * See [com.jervisffb.engine.rules.common.procedures.TheKickOffEvent].
     */
    override val kickOffTouchBackNode: Node
        get() = TODO("Not yet implemented")

    /**
     * The procedure for a Jump move (present in both rulesets, but the
     * concrete implementation differs).
     */
    override val jumpStep: Procedure
        get() = TODO("Not yet implemented")

    /** The procedure for a Leap move. Defaults to `DummyProcedure` (BB2020 has no Leap). */
    override val leapStep: Procedure
        get() = TODO("Not yet implemented")

    /** The procedure for a Pogo move. Defaults to `DummyProcedure` (BB2020 has no Pogo). */
    override val pogoStep: Procedure
        get() = TODO("Not yet implemented")

    /** The procedure for a Secure the Ball action. Defaults to `DummyProcedure` (BB2020 only). */
    override val secureTheBallStep: Procedure
        get() = TODO("Not yet implemented")

    /** The procedure for the Shadowing skill. Defaults to `DummyProcedure` (BB2020 has no Shadowing). */
    override val shadowingStep: Procedure
        get() = TODO("Not yet implemented")

    /** The procedure for the Tentacles skill. Defaults to `DummyProcedure` (BB2020 has no Tentacles). */
    override val tentaclesStep: Procedure
        get() = TODO("Not yet implemented")

    /** The procedure for the Hit and Run step. Defaults to `DummyProcedure` (BB2020 has no Hit and Run). */
    override val hitAndRunStep: Procedure
        get() = TODO("Not yet implemented")

    /** The procedure for the Hail Mary Pass step. Defaults to `DummyProcedure` (BB2020 currently uses TODO). */
    override val hailMaryPassStep: Procedure
        get() = TODO("Not yet implemented")

    /** The main team-turn procedure (bb2020.TeamTurn vs bb2025.TeamTurn). */
    override val teamTurn: Procedure
        get() = TODO("Not yet implemented")

    /** The procedure resolving a pass throw (bb2020.PassStep vs bb2025.PassStep). */
    override val passStep: Procedure
        get() = TODO("Not yet implemented")

    /** The procedure resolving a Throw Team-mate action (bb2020.ThrowPlayerStep vs bb2025.ThrowPlayerStep). */
    override val throwPlayerStep: Procedure
        get() = TODO("Not yet implemented")

    /** The procedure resolving Apply Inducements. BB2025-only; BB2020 uses `DummyProcedure`. */
    override val applyInducementsStep: Procedure
        get() = TODO("Not yet implemented")

    /** The procedure resolving the Cheering Fans kick-off event. Each ruleset supplies its own. */
    override val cheeringFansStep: Procedure
        get() = TODO("Not yet implemented")

    /** The root procedure for a full game. Shared across all rulesets; supplied by AbstractRules. */
    override val fullGameStep: Procedure
        get() = TODO("Not yet implemented")

    /** How to select the player kicking the ball during Kick Off */
    override val kickOffStep: Procedure
        get() = TODO("Not yet implemented")

    /** Procedure controlling how to select and use an Apothecary */
    override val useApothecaryStep: Procedure
        get() = TODO("Not yet implemented")

    /** Procedure for using a Chainsaw during a Foul */
    override val chainsawFoulStep: Procedure
        get() = TODO("Not yet implemented")

    override fun toBuilder(): RulesParameterBuilder {
        TODO("Not yet implemented")
    }

    override val name: String
        get() = TODO("Not yet implemented")
    override val baseVersion: GameVersion
        get() = TODO("Not yet implemented")
    override val gameType: GameType
        get() = TODO("Not yet implemented")
    override val timers: TimerSettings
        get() = TODO("Not yet implemented")
    override val inducements: InducementSettings
        get() = TODO("Not yet implemented")
    override val moveRange: IntRange
        get() = TODO("Not yet implemented")
    override val strengthRange: IntRange
        get() = TODO("Not yet implemented")
    override val agilityRange: IntRange
        get() = TODO("Not yet implemented")
    override val passingRange: IntRange
        get() = TODO("Not yet implemented")
    override val armorValueRange: IntRange
        get() = TODO("Not yet implemented")
    override val halfsPrGame: Int
        get() = TODO("Not yet implemented")
    override val turnsPrHalf: Int
        get() = TODO("Not yet implemented")
    override val hasExtraTime: Boolean
        get() = TODO("Not yet implemented")
    override val turnsInExtraTime: Int
        get() = TODO("Not yet implemented")
    override val hasShootoutInExtraTime: Boolean
        get() = TODO("Not yet implemented")
    override val pitchWidth: Int
        get() = TODO("Not yet implemented")
    override val pitchHeight: Int
        get() = TODO("Not yet implemented")
    override val wideZone: Int
        get() = TODO("Not yet implemented")
    override val endZone: Int
        get() = TODO("Not yet implemented")
    override val lineOfScrimmageHome: Int
        get() = TODO("Not yet implemented")
    override val lineOfScrimmageAway: Int
        get() = TODO("Not yet implemented")
    override val playersRequiredOnLineOfScrimmage: Int
        get() = TODO("Not yet implemented")
    override val maxPlayersInWideZone: Int
        get() = TODO("Not yet implemented")
    override val maxPlayersOnPitch: Int
        get() = TODO("Not yet implemented")
    override val stadium: StadiumRule
        get() = TODO("Not yet implemented")
    override val ballSelectorRule: BallSelectorRule
        get() = TODO("Not yet implemented")
    override val pitchType: PitchType
        get() = TODO("Not yet implemented")
    override val matchEventsEnabled: Boolean
        get() = TODO("Not yet implemented")
    override val kickOffEventTable: KickOffTable
        get() = TODO("Not yet implemented")
    override val prayersToNufflePriceForUnderdog: Int
        get() = TODO("Not yet implemented")
    override val prayersToNuffleEnabledForUnderdogDuringPregame: Boolean
        get() = TODO("Not yet implemented")
    override val prayersToNuffleTable: PrayersToNuffleTable
        get() = TODO("Not yet implemented")
    override val weatherTable: WeatherTable
        get() = TODO("Not yet implemented")
    override val injuryTable: InjuryTable
        get() = TODO("Not yet implemented")
    override val stuntyInjuryTable: InjuryTable
        get() = TODO("Not yet implemented")
    override val casualtyTable: CasualtyTable
        get() = TODO("Not yet implemented")
    override val lastingInjuryTable: LastingInjuryTable
        get() = TODO("Not yet implemented")
    override val argueTheCallTable: ArgueTheCallTable
        get() = TODO("Not yet implemented")
    override val randomDirectionTemplate: RandomDirectionTemplate
        get() = TODO("Not yet implemented")
    override val rangeRuler: RangeRuler
        get() = TODO("Not yet implemented")
    override val teamActions: TeamActions
        get() = TODO("Not yet implemented")
    override val rushesPrAction: Int
        get() = TODO("Not yet implemented")
    override val allowMultipleTeamRerollsPrTurn: Boolean
        get() = TODO("Not yet implemented")
    override val standingUpTarget: Int
        get() = TODO("Not yet implemented")
    override val moveRequiredForStandingUp: Int
        get() = TODO("Not yet implemented")
    override val secureTheBallTarget: Int
        get() = TODO("Not yet implemented")
    override val pathFinder: PathFinder
        get() = TODO("Not yet implemented")
    override val undoActionBehavior: UndoActionBehavior
        get() = TODO("Not yet implemented")
    override val diceRollsOwner: DiceRollOwner
        get() = TODO("Not yet implemented")
    override val foulActionBehavior: FoulActionBehavior
        get() = TODO("Not yet implemented")
    override val kickingPlayerBehavior: KickingPlayerBehavior
        get() = TODO("Not yet implemented")
    override val useApothecaryBehavior: UseApothecaryBehavior
        get() = TODO("Not yet implemented")
    override val skillSettings: SkillSettings
        get() = TODO("Not yet implemented")
    override val allowPlayerEditsDuringGame: Boolean
        get() = TODO("Not yet implemented")
    override val canUseMultipleRerollsOnDicePools: Boolean
        get() = TODO("Not yet implemented")
}
