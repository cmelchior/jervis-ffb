package com.jervisffb.engine.rules

import com.jervisffb.engine.DEFAULT_INDUCEMENTS
import com.jervisffb.engine.InducementSettings
import com.jervisffb.engine.TimerSettings
import com.jervisffb.engine.actions.D3Result
import com.jervisffb.engine.actions.D8Result
import com.jervisffb.engine.model.Direction
import com.jervisffb.engine.model.FieldSquare
import com.jervisffb.engine.model.Game
import com.jervisffb.engine.model.IntRangeSerializer
import com.jervisffb.engine.model.PitchType
import com.jervisffb.engine.model.Player
import com.jervisffb.engine.model.PlayerState
import com.jervisffb.engine.model.SkillId
import com.jervisffb.engine.model.Team
import com.jervisffb.engine.model.locations.FieldCoordinate
import com.jervisffb.engine.model.locations.FieldCoordinate.Companion.OUT_OF_BOUNDS
import com.jervisffb.engine.model.locations.Location
import com.jervisffb.engine.model.locations.OnFieldLocation
import com.jervisffb.engine.model.modifiers.CatchModifier
import com.jervisffb.engine.model.modifiers.DiceModifier
import com.jervisffb.engine.model.modifiers.StatModifier
import com.jervisffb.engine.rules.bb2020.BB2020SkillSettings
import com.jervisffb.engine.rules.bb2020.SkillSettings
import com.jervisffb.engine.rules.bb2020.skills.Duration
import com.jervisffb.engine.rules.bb2020.skills.RerollSource
import com.jervisffb.engine.rules.bb2020.skills.Skill
import com.jervisffb.engine.rules.bb2020.skills.SpecialActionProvider
import com.jervisffb.engine.rules.bb2020.tables.ArgueTheCallTable
import com.jervisffb.engine.rules.bb2020.tables.CasualtyTable
import com.jervisffb.engine.rules.bb2020.tables.InjuryTable
import com.jervisffb.engine.rules.bb2020.tables.KickOffTable
import com.jervisffb.engine.rules.bb2020.tables.LastingInjuryTable
import com.jervisffb.engine.rules.bb2020.tables.PrayersToNuffleTable
import com.jervisffb.engine.rules.bb2020.tables.RandomDirectionTemplate
import com.jervisffb.engine.rules.bb2020.tables.RangeRuler
import com.jervisffb.engine.rules.bb2020.tables.StandardInjuryTable
import com.jervisffb.engine.rules.bb2020.tables.StandardKickOffEventTable
import com.jervisffb.engine.rules.bb2020.tables.StandardPrayersToNuffleTable
import com.jervisffb.engine.rules.bb2020.tables.StandardWeatherTable
import com.jervisffb.engine.rules.bb2020.tables.StuntyInjuryTable
import com.jervisffb.engine.rules.bb2020.tables.ThrowInPosition
import com.jervisffb.engine.rules.bb2020.tables.ThrowInTemplate
import com.jervisffb.engine.rules.bb2020.tables.WeatherTable
import com.jervisffb.engine.rules.builder.BallSelectorRule
import com.jervisffb.engine.rules.builder.DiceRollOwner
import com.jervisffb.engine.rules.builder.FoulActionBehavior
import com.jervisffb.engine.rules.builder.GameType
import com.jervisffb.engine.rules.builder.KickingPlayerBehavior
import com.jervisffb.engine.rules.builder.NoStadium
import com.jervisffb.engine.rules.builder.StadiumRule
import com.jervisffb.engine.rules.builder.StandardBall
import com.jervisffb.engine.rules.builder.UndoActionBehavior
import com.jervisffb.engine.rules.builder.UseApothecaryBehavior
import com.jervisffb.engine.rules.common.pathfinder.BB2020PathFinder
import com.jervisffb.engine.rules.common.pathfinder.PathFinder
import com.jervisffb.engine.utils.INVALID_GAME_STATE
import com.jervisffb.engine.utils.sum
import kotlinx.serialization.Serializable

/**
 * This class is responsible for tracking all the "static" rules related to a game of Blood Bowl, as well
 * as helper methods for often asked questions. Concepts from the rulebook liked "Is a marked player" should
 * generally be found in this class, rather than a specific [com.jervisffb.engine.fsm.Procedure].
 *
 * This class should only contain rules for running a game, not rules for building rosters.
 *
 * When defining field sizes, the "board" is assumed to be laid out vertically. I.e., from left to right
 * with the home team always on the left and the away team always on the right. Coordinates start from
 * the upper-left corner with (0,0). If the UI wants to represent things differently, it is responsible
 * for swapping coordinates.
 *
 * Developer's Commentary:
 * The idea is that this class should be able to represent all game types, but that hasn't been
 * fully tested yet, e.g., Dungeon Bowl has a very different view of what the field looks
 * and behaves, so most likely some aspects need to be redesigned.
 *
 * It is also a bit unclear how well this class transcends ruleset, i.e., between BB2016 and BB2020
 */
@Serializable
open class Rules(
    // Name of the rule set
    open val name: String,
    // What type of game is this ruleset intended for
    open val gameType: GameType,

    // Which timer settings are in place for this game
    open val timers: TimerSettings = TimerSettings.Companion.BB_CLOCK,
    // Which inducements are available in this game
    open val inducements: InducementSettings = InducementSettings(DEFAULT_INDUCEMENTS),

    // Characteristic limits
    // See page 28 in the rulebook
    @Serializable(IntRangeSerializer::class)
    open val moveRange: IntRange = 1..9,
    @Serializable(IntRangeSerializer::class)
    open val strengthRange: IntRange = 1..8,
    @Serializable(IntRangeSerializer::class)
    open val agilityRange: IntRange = 1 .. 6,
    @Serializable(IntRangeSerializer::class)
    open val passingRange: IntRange = 1.. 6,
    @Serializable(IntRangeSerializer::class)
    open val armorValueRange: IntRange = 3 .. 11,

    // Game length settings
    open val halfsPrGame: Int = 2,
    open val turnsPrHalf: Int = 8,
    open val hasExtraTime: Boolean = false,
    open val turnsInExtraTime: Int = 8,
    open val hasShootoutInExtraTime: Boolean = true,

    // Field (Defined as being horizontal with a home team on the right, away team on the left)

    // Total width of the field
    open val fieldWidth: Int = 26,
    // Total height of the field
    open val fieldHeight: Int = 15,
    // Height of the Wide Zone at the top and bottom of the field
    open val wideZone: Int = 4,
    // Width of the End Zone at each end of the field where the ball is scored.
    open val endZone: Int = 1,
    // X-coordinates for the line of scrimmage for the home team. It is zero-indexed.
    open val lineOfScrimmageHome: Int = 12,
    // X-coordinate for the line of scrimmage for the away team. It is zero-indexed.
    open val lineOfScrimmageAway: Int = 13,
    // During the setup, how many players must be placed on the Line of Scrimmage inside
    // the Center Field.
    open val playersRequiredOnLineOfScrimmage: Int = 3,
    // How many players are allowed in each wide zone during setup
    open val maxPlayersInWideZone: Int = 2,
    // Default max number of players on the field. Skills and effects might change this
    open val maxPlayersOnField: Int  = 11,

    // Stadium / Pitch / Ball rules / Match Events
    open val stadium: StadiumRule = NoStadium,
    // How is the ball being used for the game selected
    open val ballSelectorRule: BallSelectorRule = StandardBall,
    // Which pitch is used for this game
    open val pitchType: PitchType = PitchType.STANDARD,
    // Match Events (See page XX)
    open val matchEventsEnabled: Boolean = false,

    // Tables
    open val kickOffEventTable: KickOffTable = StandardKickOffEventTable,
    open val prayersToNuffleEnabled: Boolean = true,
    open val prayersToNuffleTable: PrayersToNuffleTable = StandardPrayersToNuffleTable,
    open val weatherTable: WeatherTable = StandardWeatherTable,
    open val injuryTable: InjuryTable = StandardInjuryTable,
    open val stuntyInjuryTable: InjuryTable = StuntyInjuryTable,
    open val casualtyTable: CasualtyTable = CasualtyTable,
    open val lastingInjuryTable: LastingInjuryTable = LastingInjuryTable,
    open val argueTheCallTable: ArgueTheCallTable = ArgueTheCallTable,

    // Templates
    open val randomDirectionTemplate: RandomDirectionTemplate = RandomDirectionTemplate,
    open val rangeRuler: RangeRuler = RangeRuler,

    // Team Actions
    open val teamActions: TeamActions = BB2020TeamActions(),
    open val rushesPrAction: Int = 2,
    open val allowMultipleTeamRerollsPrTurn: Boolean = true,
    // Dice roll targets defined in the rulebook
    open val standingUpTarget: Int = 4, // See page 44 in the rule book
    open val moveRequiredForStandingUp: Int = 3,

    // Behavior customization, .e.g. allow the rules to specify which Procedure should
    // be used for certain aspects of the game
    // Whether coaches are allowed to undo actions, and to what degree.
    open val undoActionBehavior: UndoActionBehavior = UndoActionBehavior.ONLY_NON_RANDOM_ACTIONS,
    // Who is responsible for rolling dice or taking random actions.
    open val diceRollsOwner: DiceRollOwner = DiceRollOwner.ROLL_ON_SERVER,
    // Probably need to replace this with a reference to the FoulProcedure
    open val foulActionBehavior: FoulActionBehavior = FoulActionBehavior.STRICT,
    // Probably need to replace this with a reference to the KickProcedure
    open val kickingPlayerBehavior: KickingPlayerBehavior = KickingPlayerBehavior.STRICT,
    // Which procedure to use when deciding and using an apothecary.
    // The rules differ between BB7 and Standard, but it is unclear if we want two different
    // procedures for this, but as there are multiple differences (who they apply to + "Patching-up" section).
    // Keep them separate for now.
    open val useApothecaryBehavior: UseApothecaryBehavior = UseApothecaryBehavior.STANDARD,
    // Configure skills available, their behaviour and which category they belong to.
    open val skillSettings: SkillSettings = BB2020SkillSettings(),
) {
    // Defines how the paths between locations on the field are calculated. This can be rules-specific,
    // since it might involve the use of skills.
    open val pathFinder: PathFinder = BB2020PathFinder()

    fun isValidSetup(state: Game, team: Team): Boolean {
        val isHomeTeam = team.isHomeTeam()
        val inReserve: List<Player> = team.filter { it.state == PlayerState.RESERVE && !it.location.isOnField(this) }
        val onField: List<Player> = team.filter { it.state == PlayerState.STANDING && it.location.isOnField(this) }
        val totalAvailablePlayers: Int = inReserve.size + onField.size

        // If below 11 players, all players must be fielded
        if (totalAvailablePlayers < maxPlayersOnField && inReserve.isNotEmpty()) {
            return false
        }

        // Otherwise 11 players must be on the field
        // TODO Swarming might change this
        if (totalAvailablePlayers >= maxPlayersOnField && onField.size != maxPlayersOnField) {
            return false
        }

        // Check LoS requirements
        val field = state.field
        val losIndex: Int = if (isHomeTeam) lineOfScrimmageHome else lineOfScrimmageAway
        val playersOnLos =
            (wideZone .. fieldHeight - wideZone).filter { y: Int ->
                field[losIndex, y].isOccupied()
            }.size

        // If available, 3 players must be on the Centre Field LoS
        if (onField.size >= playersRequiredOnLineOfScrimmage && playersOnLos < playersRequiredOnLineOfScrimmage.toInt()) {
            return false
        }

        // If less than 3 players, all must be on the Centre Field LoS
        if (onField.size < playersRequiredOnLineOfScrimmage && onField.size != playersOnLos) {
            return false
        }

        // Max two players on the Top Wide Zone.
        var topWideZoneCount = 0
        if (isHomeTeam) {
            (0..lineOfScrimmageHome).forEach { x ->
                (0 until wideZone).forEach { y ->
                    if (field[x, y].isOccupied()) {
                        topWideZoneCount++
                    }
                }
            }
        } else {
            (fieldWidth - 1 downTo lineOfScrimmageAway).forEach { x ->
                (0 until wideZone).forEach { y ->
                    if (field[x, y].isOccupied()) {
                        topWideZoneCount++
                    }
                }
            }
        }
        if (topWideZoneCount > maxPlayersInWideZone) {
            return false
        }

        // Max two players on the Bottom Wide Zone
        var bottomWideZoneCount = 0
        if (isHomeTeam) {
            (0..lineOfScrimmageHome).forEach { x ->
                (fieldHeight - wideZone  until fieldHeight).forEach { y ->
                    if (field[x, y].isOccupied()) {
                        bottomWideZoneCount++
                    }
                }
            }
        } else {
            (fieldWidth - 1 downTo lineOfScrimmageAway).forEach { x ->
                (fieldHeight - wideZone  until fieldHeight).forEach { y ->
                    if (field[x, y].isOccupied()) {
                        bottomWideZoneCount++
                    }
                }
            }
        }
        if (bottomWideZoneCount > maxPlayersInWideZone) {
            return false
        }

        return true
    }

    /**
     * Returns whether the given location is in the valid setup area for a given team.
     * While this is described as a bit different between Standard and BB7, it generalizes
     * to the area up to the team's Line of Scrimmage.
     */
    fun isInSetupArea(team: Team, location: FieldCoordinate): Boolean {
        return if (team.isHomeTeam()) {
            location.x <= lineOfScrimmageHome
        } else {
            location.x >= lineOfScrimmageAway
        }
    }

    /**
     * Returns whether a given location is valid for placing the ball during kick-off.
     *
     * For Standard and BB7, this generalizes to all locations _not_ in the area between
     * the End Zone and kicking teams Line of Scrimmage. In particular, it allows you to
     * place the ball in all of any configured "No Man's Land".
     *
     * This is in line with the Designer's Commentary, May 2024, page 10.
     */
    fun canPlaceBallForKickoff(kickingTeam: Team, location: FieldSquare): Boolean {
        return when (kickingTeam.isHomeTeam()) {
            true -> location.x > lineOfScrimmageHome
            false -> location.x < lineOfScrimmageAway
        }
    }

    // Roll on the random direction template
    fun direction(d8: D8Result): Direction = randomDirectionTemplate.roll(d8)

    /**
     * Returns the result of rolling a direction using the Throw-in
     * template (or Random Direction template in case of corners)
     */
    fun throwIn(from: FieldCoordinate, d3: D3Result): Direction {
        val corner = from.getCornerLocation(this)
        return if (corner != null) {
            randomDirectionTemplate.roll(corner, d3)
        } else {
            if (from.x == 0) {
                ThrowInTemplate.roll(ThrowInPosition.LEFT, d3)
            } else if (from.x == fieldWidth - 1) {
                ThrowInTemplate.roll(ThrowInPosition.RIGHT, d3)
            } else if (from.y == 0) {
                ThrowInTemplate.roll(ThrowInPosition.TOP, d3)
            } else if (from.y == fieldHeight - 1) {
                ThrowInTemplate.roll(ThrowInPosition.BOTTOM, d3)
            } else {
                throw IllegalArgumentException("Cannot determine position of: $from")
            }
        }
    }

    /**
     * Returns whether a player is eligible for catching a ball that landed in their field.
     */
    fun canCatch(
        state: Game,
        player: Player,
    ): Boolean {
        // TODO Probably need to account for difference between Bomb and Ball here
        return player.hasTackleZones
            && player.state == PlayerState.STANDING
            && player.location.isOnField(this)
            && !player.hasBall()
    }

    /**
     * Return `true` if this player is able to mark other players.
     */
    fun canMark(player: Player): Boolean {
        return player.hasTackleZones && player.state == PlayerState.STANDING
    }

    /**
     * Returns `true` if the player is considered `Open` as described on
     * page 26 in the rulebook.
     */
    fun isOpen(player: Player): Boolean {
        return player.state == PlayerState.STANDING && !isMarked(player)
    }

    /**
     * Returns `true` if the player is considered "Standing" as described
     * on page 26 in the rulebook.
     */
    fun isStanding(player: Player): Boolean {
        return player.state == PlayerState.STANDING && player.location.isOnField(this)
    }

    /**
     * Returns `true` if the player is considered `Marked as described on
     * page 26 in the rulebook.
     */
    fun isMarked(player: Player, overrideLocation: Location): Boolean {
        if (!overrideLocation.isOnField(this)) return false
        if (overrideLocation !is FieldCoordinate) return false
        val field = player.team.game.field
        return overrideLocation.getSurroundingCoordinates(this, 1)
            .asSequence()
            .filter {
                val otherPlayer = field[it].player
                otherPlayer != null && otherPlayer.team != player.team
            }
            .firstOrNull { canMark(field[it].player!!) } != null
    }

    fun isMarked(player: Player): Boolean {
        if (!player.location.isOnField(this)) return false
        val field = player.team.game.field
        return player.coordinates.getSurroundingCoordinates(this, 1)
            .asSequence()
            .filter {
                val otherPlayer = field[it].player
                otherPlayer != null && otherPlayer.team != player.team
            }
            .firstOrNull { canMark(field[it].player!!) } != null
    }

    fun isMarking(state: Game, player: Player, target: Player): Boolean {
        if (!player.location.isOnField(this)) return false
        if (!target.location.isOnField(this)) return false
        if (!player.hasTackleZones) return false

        return player.coordinates.getSurroundingCoordinates(this, 1)
            .any { state.field[it].player == target }
    }

    /**
     * Return `true` if the [assisting] player can assist another player against
     * [target], `false` if not.
     */
    fun canOfferAssistAgainst(
        assisting: Player,
        target: Player,
    ): Boolean {
        if (assisting.team == target.team) return false
        if (!assisting.location.isAdjacent(this, target.location)) return false
        if (!canMark(assisting)) return false
        // TODO If player has Guard, player can always assist
        return assisting.coordinates.getSurroundingCoordinates(this).firstOrNull {
            assisting.team.game.field[it].player?.let { adjacentPlayer ->
                adjacentPlayer != target &&
                    adjacentPlayer.team != assisting.team &&
                    canMark(adjacentPlayer)
            } ?: false
        } == null
    }

    // Only call this method for the active team
    fun addMarkedModifiers(
        game: Game,
        activeTeam: Team,
        square: FieldSquare,
        modifiers: MutableList<DiceModifier>,
        markedModifier: DiceModifier = CatchModifier.MARKED
    ) {
        square.coordinates.getSurroundingCoordinates(this).forEach {
            val markingPlayer: Player? = game.field[it].player
            if (markingPlayer != null) {
                if (markingPlayer.team != activeTeam && canMark(markingPlayer)) {
                    modifiers.add(markedModifier)
                }
            }
        }
    }

    /**
     * Calculates how many opponent players are marking a given field square.
     * See page 26 in the rulebook.
     *
     * A player is marking a square if:
     * - The player has its tackle zones.
     * - The square is in the player's tackle zone.
     * - The player is standing.
     */
    fun calculateMarks(
        game: Game,
        markedTeam: Team,
        square: OnFieldLocation,
    ): Int {
        if (!square.isOnField(this)) throw IllegalArgumentException("${square.toLogString()} is not on the field")
        return square.getSurroundingCoordinates(this).fold(initial = 0) { acc, coordinate ->
            val markingPlayer: Player? = game.field[coordinate].player
            val otherTeam = markingPlayer?.team
            val canMark = markingPlayer?.let { canMark(it) } ?: false
            if (markingPlayer != null && otherTeam != markedTeam && canMark) {
                acc + 1
            } else {
                acc
            }
        }
    }

    fun canUseTeamReroll(game: Game, player: Player): Boolean {
        if (!game.canUseTeamRerolls) return false
        if (game.activeTeam != player.team) return false
        return when (player.team.usedRerollThisTurn) {
            true -> allowMultipleTeamRerollsPrTurn
            false -> true
        }
    }

    /**
     * Return all locations you can choose from when pushing a player.
     * This only returns the normal push options and doesn't take into
     * account skills or if the squares are occupied.
     *
     * If that matters or not is up to the call of this method.
     *
     * If pushing a player OUT_OF_BOUNDS is possible, it will only be reported once
     * as [FieldCoordinate.OUT_OF_BOUNDS].
     */
    fun getPushOptions(pusher: Player, pushee: Player): Set<FieldCoordinate> {
        val start: FieldCoordinate = pusher.location as? FieldCoordinate ?: throw IllegalStateException("Pusher must be on field.")
        val direction: FieldCoordinate = pushee.location as? FieldCoordinate ?: throw IllegalStateException("Pushee must be on field.")
        if (!start.isAdjacent(this, direction)) {
            throw IllegalArgumentException("Pusher and Pushee must be adjacent to each other")
        }

        val all =  (pushee.location as FieldCoordinate).getSurroundingCoordinates(this, includeOutOfBounds = true).toSet()
        val map = all.map { Pair(it, it.realDistanceTo(start)) }
        val result = map
            .sortedByDescending { it.second }
            .subList(0, 3)
            .map {
                val coords = it.first
                if (coords.isOutOfBounds(this)) {
                    OUT_OF_BOUNDS
                } else {
                    coords
                }
            }
            .toSet()
        return result
    }

    /**
     * Returns `true` if the team has a hold of the ball.
     */
    fun teamHasBall(team: Team): Boolean {
        return team.firstOrNull { it.hasBall() } != null
    }

    /**
     * Returns the best team reroll available.
     * This means using temporary rerolls before using permanent ones
     * TODO Should we instead return a list here, so players can manually select
     *  between all the temporary rerolls?
     */
    fun getAvailableTeamReroll(team: Team): RerollSource {
        return team.availableRerolls.last()
    }

    /**
     * Returns all actions available to this player when they are activated.
     */
    fun getAvailableActions(state: Game, player: Player): List<PlayerAction> {
        if (state.activePlayer != player) INVALID_GAME_STATE("$player is not the active player")
        if (player.location !is OnFieldLocation) return emptyList()

        return buildList {
            // Add any team actions that are available
            state.activeTeamOrThrow().turnData.let {
                if (it.moveActions > 0) add(teamActions.move)
                if (it.passActions > 0) add(teamActions.pass)
                if (it.handOffActions > 0) add(teamActions.handOff)
                if (it.blockActions > 0) {
                    val isStanding = (player.state == PlayerState.STANDING)
                    val hasEligibleTargets = (player.location as OnFieldLocation)
                        .getSurroundingCoordinates(this@Rules, 1)
                        .mapNotNull { state.field[it].player }
                        .filter { otherPlayer -> otherPlayer.team != player.team }
                        .filter { otherPlayer -> isStanding(otherPlayer)}
                        .any { otherPlayer -> isMarking(state, player, otherPlayer)}

                    if (isStanding && hasEligibleTargets) {
                        add(teamActions.block)
                    }
                }
                if (it.blitzActions > 0) {
                    val hasEligibleTargets = player.team.otherTeam()
                        .filter { targetPlayer ->  targetPlayer.location.isOnField(this@Rules) }
                        .any {  targetPlayer -> isStanding(targetPlayer) }

                    if (hasEligibleTargets) {
                        add(teamActions.blitz)
                    }
                }
                if (it.foulActions > 0) {
                    // TODO Check if any players are currently prone/stunned
                    add(teamActions.foul)
                }
            }

            // Add any special actions that are provided by skills
            player.skills.filterIsInstance<SpecialActionProvider>().forEach {
                val type = it.specialAction
                val isSkillActionUsed = it.isSpecialActionUsed
                val isActionAvailable = state.activeTeamOrThrow().turnData.availableSpecialActions[type]!! > 0
                if (!isSkillActionUsed && isActionAvailable) {
                    add(teamActions[type])
                }
            }
        }
    }

    /**
     * When either a `baseX` or `XModifiers` stat value has been updated, this method should also
     * be called so the total player stat can be calculated correctly.
     */
    fun updatePlayerStat(player: Player, modifier: StatModifier) {
        with(player) {
            when (modifier.type) {
                StatModifier.Type.AV -> armorValue = (baseArmorValue + armourModifiers.sum()).coerceIn(armorValueRange)
                StatModifier.Type.MA -> move = (baseMove + moveModifiers.sum()).coerceIn(moveRange)
                StatModifier.Type.PA -> {
                    // How to handle modifiers to `null`. I believe the stat is then treated as 7+, but find reference
                    val newPassing = if (basePassing == null && passingModifiers.isNotEmpty()) {
                        (7 + passingModifiers.sum())
                    } else if (basePassing != null && passingModifiers.isNotEmpty()) {
                        (basePassing!! + passingModifiers.sum())
                    } else {
                        basePassing
                    }
                    passing = newPassing?.coerceIn(passingRange) ?: passing
                }
                StatModifier.Type.AG -> agility = (baseAgility + agilityModifiers.sum()).coerceIn(agilityRange)
                StatModifier.Type.ST -> strength = (baseStrength + strengthModifiers.sum()).coerceIn(strengthRange)
            }
        }
    }

    /**
     * Skills might change subtly between rule versions, for that reason, we need a single place to lookup
     * skill definitions from their id (since we might want to support teams across multiple rulesets).
     */
    fun createSkill(player: Player, skill: SkillId, expiresAt: Duration = Duration.PERMANENT): Skill {
        return skillSettings.createSkill(player, skill, expiresAt)
    }

    fun toBuilder(): Builder {
        return Builder(this)
    }

    /**
     *  Rules builder making it easier to create variants of
     */
    class Builder(rules: Rules) {
        var name: String = rules.name
        var gameType: GameType = rules.gameType
        var timers: TimerSettings.Builder = rules.timers.toBuilder()
        var inducements: InducementSettings.Builder = rules.inducements.toBuilder()
        var moveRange: IntRange = rules.moveRange
        var strengthRange: IntRange = rules.strengthRange
        var agilityRange: IntRange = rules.agilityRange
        var passingRange: IntRange = rules.passingRange
        var armorValueRange: IntRange = rules.armorValueRange
        var halfsPrGame: Int = rules.halfsPrGame
        var turnsPrHalf: Int = rules.turnsPrHalf
        var hasExtraTime: Boolean = rules.hasExtraTime
        var turnsInExtraTime: Int = rules.turnsInExtraTime
        var hasShootoutInExtraTime: Boolean = rules.hasShootoutInExtraTime
        var fieldWidth: Int = rules.fieldWidth
        var fieldHeight: Int = rules.fieldHeight
        var wideZone: Int = rules.wideZone
        var endZone: Int = rules.endZone
        var lineOfScrimmageHome: Int = rules.lineOfScrimmageHome
        var lineOfScrimmageAway: Int = rules.lineOfScrimmageAway
        var playersRequiredOnLineOfScrimmage: Int = rules.playersRequiredOnLineOfScrimmage
        var maxPlayersInWideZone: Int = rules.maxPlayersInWideZone
        var maxPlayersOnField: Int = rules.maxPlayersOnField
        var stadium: StadiumRule = rules.stadium
        var ballSelectorRule: BallSelectorRule = rules.ballSelectorRule
        var pitchType: PitchType = rules.pitchType
        var matchEventsEnabled: Boolean = rules.matchEventsEnabled
        var kickOffEventTable: KickOffTable = rules.kickOffEventTable
        var prayersToNuffleEnabled: Boolean = rules.prayersToNuffleEnabled
        var prayersToNuffleTable: PrayersToNuffleTable = rules.prayersToNuffleTable
        var weatherTable: WeatherTable = rules.weatherTable
        var injuryTable: InjuryTable = rules.injuryTable
        var stuntyInjuryTable: InjuryTable = rules.stuntyInjuryTable
        var casualtyTable: CasualtyTable = rules.casualtyTable
        var lastingInjuryTable: LastingInjuryTable = rules.lastingInjuryTable
        var argueTheCallTable: ArgueTheCallTable = rules.argueTheCallTable
        var randomDirectionTemplate: RandomDirectionTemplate = rules.randomDirectionTemplate
        var rangeRuler: RangeRuler = rules.rangeRuler
        var teamActions: TeamActions = rules.teamActions
        var rushesPrAction: Int = rules.rushesPrAction
        var allowMultipleTeamRerollsPrTurn: Boolean = rules.allowMultipleTeamRerollsPrTurn
        var standingUpTarget: Int = rules.standingUpTarget
        var moveRequiredForStandingUp: Int = rules.moveRequiredForStandingUp
        var undoActionBehavior: UndoActionBehavior = rules.undoActionBehavior
        var diceRollsOwner: DiceRollOwner = rules.diceRollsOwner
        var foulActionBehavior: FoulActionBehavior = rules.foulActionBehavior
        var kickingPlayerBehavior: KickingPlayerBehavior = rules.kickingPlayerBehavior
        var useApothecaryBehavior: UseApothecaryBehavior = rules.useApothecaryBehavior
        var skillSettings: SkillSettings = rules.skillSettings

        fun build() = Rules(
            name,
            gameType,
            timers.build(),
            inducements.build(),
            moveRange,
            strengthRange,
            agilityRange,
            passingRange,
            armorValueRange,
            halfsPrGame,
            turnsPrHalf,
            hasExtraTime,
            turnsInExtraTime,
            hasShootoutInExtraTime,
            fieldWidth,
            fieldHeight,
            wideZone,
            endZone,
            lineOfScrimmageHome,
            lineOfScrimmageAway,
            playersRequiredOnLineOfScrimmage,
            maxPlayersInWideZone,
            maxPlayersOnField,
            stadium,
            ballSelectorRule,
            pitchType,
            matchEventsEnabled,
            kickOffEventTable,
            prayersToNuffleEnabled,
            prayersToNuffleTable,
            weatherTable,
            injuryTable,
            stuntyInjuryTable,
            casualtyTable,
            lastingInjuryTable,
            argueTheCallTable,
            randomDirectionTemplate,
            rangeRuler,
            teamActions,
            rushesPrAction,
            allowMultipleTeamRerollsPrTurn,
            standingUpTarget,
            moveRequiredForStandingUp,
            undoActionBehavior,
            diceRollsOwner,
            foulActionBehavior,
            kickingPlayerBehavior,
            useApothecaryBehavior,
            skillSettings
        )
    }
}
