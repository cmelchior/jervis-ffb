package com.jervisffb.engine.rules

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
import com.jervisffb.engine.model.Player
import com.jervisffb.engine.model.SkillId
import com.jervisffb.engine.model.Team
import com.jervisffb.engine.model.inducements.Apothecary
import com.jervisffb.engine.model.locations.Location
import com.jervisffb.engine.model.locations.PitchCoordinate
import com.jervisffb.engine.model.modifiers.DiceModifier
import com.jervisffb.engine.model.modifiers.StatModifier
import com.jervisffb.engine.rules.common.InducementRule
import com.jervisffb.engine.rules.common.SetupRule
import com.jervisffb.engine.rules.common.actions.BlockType
import com.jervisffb.engine.rules.common.actions.PlayerAction
import com.jervisffb.engine.rules.common.procedures.DieRoll
import com.jervisffb.engine.rules.common.rerolls.TeamReroll
import com.jervisffb.engine.rules.common.skills.Duration
import com.jervisffb.engine.rules.common.skills.RerollSource
import com.jervisffb.engine.rules.common.skills.Skill

/**
 * This interface is responsible for tracking all the "static" rules related to
 * a game of Blood Bowl, as well as helper methods for often asked questions.
 * Cross-cutting concepts from the rulebook like "Is a player marked?" should
 * generally be found here (like [isMarked]), rather than in a specific
 * [Procedure] or helper function.
 *
 * This interface should only contain rules for running a game, not rules for
 * building rosters.
 *
 * When defining pitch sizes, the "board" is assumed to be laid out vertically.
 * I.e., from left to right with the home team always on the left and the away
 * team always on the right. Coordinates start from the upper-left corner with
 * (0,0). If the UI wants to represent things differently, it is responsible for
 * swapping coordinates.
 *
 * Developer's Commentary:
 * The idea is that this class should be able to represent all game types, but
 * that hasn't been fully tested yet, e.g., Dungeon Bowl has a very different
 * view of what the pitch looks and behaves, so most likely some aspects need to
 * be redesigned. It is also a bit unclear how well this interface transcends
 * ruleset, i.e., between BB2016 and BB2020
 *
 * To keep other parts of the engine rules-agnostic, the interface also has
 * a number of references to procedures. This means that [Rules] can control
 * the flow of the rules. While this approach keeps the procedures clean, it
 * does mean we introduce a lot of references here. Maybe there is a way to
 * isolate these in their own sub-interface.
 */
interface Rules : RulesParameters {

    /**
     * Checks if a given setup is valid. If not valid, a list of broken rules
     * will be returned. If the setup is valid, an empty list is returned.
     */
    fun isSetupValid(state: Game, team: Team): List<SetupRule>

    /**
     * Returns whether the given location is in the valid setup area for a given
     * team. While this is described as a bit different between Standard and
     * BB7, it generalizes to the area up to the team's Line of Scrimmage.
     */
    fun isInSetupArea(team: Team, location: PitchCoordinate): Boolean

    /**
     * Returns whether a given location is valid for placing the ball during
     * kick-off.
     */
    fun canPlaceBallForKickoff(kickingTeam: Team, location: PitchSquare): Boolean

    /**
     * Roll on the random direction template.
     *
     * See page 25 in the BB2025 rulebook.
     */
    fun direction(d8: D8Result): Direction

    /**
     * Returns the result of rolling a direction using the Throw-in
     * template when attempting to throw a ball in after it went out-of-bounds
     * (or Random Direction template in case of corners).
     *
     * See page 26 in the BB2025 rulebook.
     */
    fun throwIn(from: PitchCoordinate, d3: D3Result): Direction

    /**
     * Returns the result of rolling on the throw-in template when it is put
     * down anywhere on the field and pointed in a specific direction.
     *
     * See page 26 in the BB2025 rulebook.
     */
    fun throwIn(direction: Direction, d3: D3Result): Direction

    /**
     * Returns whether a player is eligible for catching a ball that landed in
     * their location.
     */
    fun canCatch(player: Player): Boolean

    /**
     * Returns whether a player can deflect a ball if it is thrown over them.
     */
    fun canDeflect(player: Player): Boolean

    /**
     * Return `true` if this player is able to mark other players. `false` if
     * not.
     */
    fun canMarkPlayers(player: Player): Boolean

    /**
     * Returns `true` if the player is considered "Open" as described on
     * page 38 in the BB2025 rulebook, `false` if not.
     */
    fun isOpen(player: Player): Boolean

    /**
     * Returns `true` if the player is considered "Standing" as described
     * on page 38 in the BB2025 rulebook, `false` if not.
     */
    fun isStanding(player: Player): Boolean

    /**
     * Returns `true` if the player is considered "Distracted" as described on
     * page 38 in the BB2025 rulebook.
     */
    fun isDistracted(player: Player): Boolean

    /**
     * Returns `true` if this player has a state that is considered an "Injury"
     * This is mostly used for UI purposes.
     */
    fun isInjuried(player: Player): Boolean

    /**
     * Returns `true` if the player is considered `Marked` as described on
     * page 26 in the rulebook.
     *
     * @param player The player that is checked for marks.
     * @param location The location the player is in. Can be overridden to fake the player
     *     being in another location (used, e.g., when checking if dodging is needed).
     */
    fun isMarked(player: Player, location: Location = player.location): Boolean

    /**
     * Returns `true` if [player] count as marking [target], `false` if not.
     */
    fun isMarking(player: Player, target: Player): Boolean

    /**
     * Returns `true` if [player] count as marking the given location, `false` if not.
     */
    fun isMarking(player: Player, target: Location): Boolean

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
    fun calculateOffensiveAssists(attacker: Player, defender: Player): Int

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
    fun calculateDefensiveAssists(defender: Player, attacker: Player): Int

    /**
     * Return `true` if the [assister] player can offer either an offensive or
     * defensive assist against [target], `false` if not.
     *
     * See page 57 in the BB2020 rulebook.
     * See page 61 in the BB2025 rulebook.
     */
    fun canOfferAssist(assister: Player, target: Player): Boolean

    /**
     * Calculate how many marks are on [square] for a player on the [markedTeam].
     * Marks will be returned as modifiers in the [modifiers] list.
     */
    fun addMarkedModifiers(
        game: Game,
        markedTeam: Team,
        square: PitchCoordinate,
        modifiers: MutableList<DiceModifier>,
        markedModifier: DiceModifier,
    )

    /**
     * Returns all players not from the [markedTeam] that can mark the [square].
     */
    fun getMarkingPlayers(game: Game, markedTeam: Team, square: PitchCoordinate): List<Player>

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
    fun calculateMarks(game: Game, markedTeam: Team, square: com.jervisffb.engine.model.locations.OnPitchLocation): Int

    /**
     * Returns `true` if the player can use a team re-roll at the current state
     * of the game, `false` if not.
     */
    fun canUseTeamReroll(game: Game, player: Player?): Boolean

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
    fun getPushOptions(pusher: Player, pushee: Player): Set<PitchCoordinate>

    /**
     * Returns `true` if the team has a hold of the ball.
     *
     * @param ball if set, only this ball is checked, if `false` any ball is accepted.
     */
    fun teamHasBall(team: Team, ball: Ball? = null): Boolean

    /**
     * Return all available rerolls available for a Team.
     *
     * If multiple versions of the same reroll are available, only one of them
     * is returned.
     *
     * E.g., if a Team has 3 regular, 1 Brilliant Coaching and 1 Mascot reroll,
     * 3 rerolls will be returned: (1xregular, 1xbrilliant, 1xmascot)
     */
    fun getAvailableTeamRerolls(team: Team): List<RerollSource>

    /**
     * Returns all actions available to this player when they are activated.
     * This method should filter out actions that require targets that do not
     * exist, like Blitz or Foul (in BB2020).
     */
    fun getAvailableActions(state: Game, player: Player): List<PlayerAction>

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
    fun updatePlayerStat(player: Player, stat: StatModifier.Type)

    /**
     * Returns `true` if the current game is the start of a half, `false` if
     * not. Start of Extra Time will also return `false`.
     */
    fun isStartOfHalf(state: Game): Boolean

    /**
     * Create a skill instance from it's [SkillId].
     *
     * Skills might change subtly between rule versions; for that reason, we
     * need a single place to look up skill definitions from their id (since we
     * might want to support teams across multiple rulesets).
     */
    fun createSkill(
        player: Player,
        skill: SkillId,
        expiresAt: Duration = Duration.PERMANENT
    ): Skill<*>

    /**
     * Returns `true` if rerolls of some dice in the dice pool are still allowed.
     * Note; this doesn't mean that a reroll is available, just that it is allowed
     * if possible.
     */
    fun isRerollAllowed(dicePool: List<DieRoll<*>>): Boolean

    /**
     * Return `true` if a Team Reroll is allowed to re-roll this type of roll.
     *
     * We keep this method here as some skills offer a re-roll that is
     * functionaly the same as a Team Reroll. They should all go through this
     * method.
     */
    fun canBeRerolledByTeamReroll(type: DiceRollType): Boolean

    /**
     * Maximum strength a player with the "Right Stuff" skill can have to be
     * throwable. Ruleset-specific: BB2020 caps at 3, BB2025 has no cap.
     */
    val rightStuffMaxStrength: Int

    /**
     * The target roll for a successful Pro re-roll. BB2025 uses 3+ on a D6.
     */
    val proSuccessTarget: Int

    /**
     * When a player with the Leader skill is removed from the pitch, return
     * the command that updates the team's Leader re-roll state (disable/remove),
     * or `null` if no change is needed. Each ruleset provides its own logic.
     */
    fun calculateLeaderRerollStatusChange(team: Team): Command?

    /**
     * Commands to run at end-of-turn to reset per-turn skill counters for the
     * given player. BB2020 returns an empty list; BB2025 returns commands like
     * `ResetShadowingSkill` when the player has the corresponding skill.
     */
    fun getEndOfTurnResetCommands(player: Player): List<Command>

    /**
     * Whether the given player had a Secret Weapon on the pitch during the
     * current drive. BB2025-only concept; BB2020 always returns `false`.
     */
    fun wasSecretWeaponOnPitchDuringDrive(player: Player): Boolean

    /**
     * Command to set whether the given player's Secret Weapon was on the
     * pitch during the current drive. BB2025-only concept; BB2020 returns
     * `null`.
     */
    fun setSecretWeaponOnPitchDuringDrive(player: Player, onPitch: Boolean): Command?

    /**
     * Returns the [RerollSource] to use for a Lone Fouler re-roll for the
     * given player, or `null` if unavailable (or not supported by ruleset).
     */
    fun getLoneFoulerRerollSource(player: Player): RerollSource?

//    /**
//     * When an injury is rolled during a Multiple Block, delegate to the
//     * ruleset's `MultipleBlockContext` so it can track the injury reference
//     * for the given player.
//     */
//    fun addMultipleBlockInjuryReference(
//        state: Game,
//        player: Player,
//        injuryContext: com.jervisffb.engine.rules.common.procedures.tables.injury.RiskingInjuryContext,
//    ): Command

    /**
     * Return all block types and special actions that can replace a block,
     * that is available to the given player.
     */
    fun getAvailableBlockType(player: Player, isMultipleBlock: Boolean): List<BlockType>

    /** Creates a standard team apothecary */
    fun createTeamApothecary(): Apothecary

    /**
     * Creates a standard Team reroll.
     * [index] is used to identify the reroll. Can normally just be the index
     * of the reroll in the team's reroll list.
     */
    fun createTeamReroll(team: Team, index: Int): TeamReroll

    /**
     * Creates the reroll created by having a Leader on the pitch when a drive
     * starts.
     */
    fun createLeaderTeamReroll(team: Team): TeamReroll

    /**
     * Create the reroll created by rolling "Brilliant Coaching" on the
     * kick-off table.
     */
    fun createBrilliantCoachingReroll(team: Team): TeamReroll

    /**
     * Verify if the lists of inducements are valid for the given team.
     * If yes, an empty list is returned, otherwise a list of broken rules.
     */
    fun isInducementsValid(
        team: Team,
        inducements: List<InducementSelection<*>>
    ): List<InducementRule>

    /**
     * Returns `true` if the given reroll is a Leader reroll, `false` if not.
     */
    fun isLeaderReroll(reroll: TeamReroll): Boolean


    // -- PROCEDURE-DISPATCH BEGIN
    // Procedure dispatch properties. We use these as choosing between
    // "next" step in cases where a "common" procedure needs to delegate to
    // a sub-procedure that differs based on the ruleset. We don't want this
    // kind of dispatch-logic in Procedure classes, so instead they are placed
    // here.
    //
    // TODO This list needs to be reviewed, I suspect we can trim it down or
    //  somehow move it out of the Rules class.

    /**
     * The procedure resolving a standard block after a target has been
     * selected. In BB2020 this is a multi-step block sequence, in BB2025 it
     * is the single-block flow.
     */
    val standardBlockStep: Procedure
    /** The procedure resolving a player falling over (failed rush/dodge, etc.). */
    val fallingOverStep: Procedure
    /**
     * The procedure resolving a player being knocked down (e.g. from a chainsaw,
     * breathe fire, or animal savagery result).
     */
    val knockedDownStep: Procedure
    /**
     * The node inside the kick-off event flow that resolves a touchback.
     * See [com.jervisffb.engine.rules.common.procedures.TheKickOffEvent].
     */
    val kickOffTouchBackNode: Node
    /**
     * The procedure for a Jump move (present in both rulesets, but the
     * concrete implementation differs).
     */
    val jumpStep: Procedure
    /** The procedure for a Leap move. Defaults to `DummyProcedure` (BB2020 has no Leap). */
    val leapStep: Procedure
    /** The procedure for a Pogo move. Defaults to `DummyProcedure` (BB2020 has no Pogo). */
    val pogoStep: Procedure
    /** The procedure for a Secure the Ball action. Defaults to `DummyProcedure` (BB2020 only). */
    val secureTheBallStep: Procedure
    /** The procedure for the Shadowing skill. Defaults to `DummyProcedure` (BB2020 has no Shadowing). */
    val shadowingStep: Procedure
    /** The procedure for the Tentacles skill. Defaults to `DummyProcedure` (BB2020 has no Tentacles). */
    val tentaclesStep: Procedure
    /** The procedure for the Hit and Run step. Defaults to `DummyProcedure` (BB2020 has no Hit and Run). */
    val hitAndRunStep: Procedure
    /** The procedure for the Hail Mary Pass step. Defaults to `DummyProcedure` (BB2020 currently uses TODO). */
    val hailMaryPassStep: Procedure
    /** The main team-turn procedure (bb2020.TeamTurn vs bb2025.TeamTurn). */
    val teamTurn: Procedure
    /** The procedure resolving a pass throw (bb2020.PassStep vs bb2025.PassStep). */
    val passStep: Procedure
    /** The procedure resolving a Throw Team-mate action (bb2020.ThrowPlayerStep vs bb2025.ThrowPlayerStep). */
    val throwPlayerStep: Procedure
    /** The procedure resolving Apply Inducements. BB2025-only; BB2020 uses `DummyProcedure`. */
    val applyInducementsStep: Procedure
    /** The procedure resolving the Cheering Fans kick-off event. Each ruleset supplies its own. */
    val cheeringFansStep: Procedure
    /** The root procedure for a full game. Shared across all rulesets; supplied by AbstractRules. */
    val fullGameStep: Procedure
    /** How to select the player kicking the ball during Kick Off */
    val kickOffStep: Procedure
    /** Procedure controlling how to select and use an Apothecary */
    val useApothecaryStep: Procedure
    /** Procedure for using a Chainsaw during a Foul */
    val chainsawFoulStep: Procedure
    /** Procedure for handling deviate during Kick Off */
    val kickOffDeviateRollStep: Procedure

    // -- PROCEDURE-DISPATCH END

    fun toBuilder(): RulesParameterBuilder
}
