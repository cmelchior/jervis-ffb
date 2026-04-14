package com.jervisffb.engine.rules

import com.jervisffb.engine.actions.D3Result
import com.jervisffb.engine.actions.D8Result
import com.jervisffb.engine.model.Ball
import com.jervisffb.engine.model.Direction
import com.jervisffb.engine.model.FieldSquare
import com.jervisffb.engine.model.Game
import com.jervisffb.engine.model.Player
import com.jervisffb.engine.model.PlayerState
import com.jervisffb.engine.model.SkillId
import com.jervisffb.engine.model.Team
import com.jervisffb.engine.model.isSkillAvailable
import com.jervisffb.engine.model.locations.FieldCoordinate
import com.jervisffb.engine.model.locations.Location
import com.jervisffb.engine.model.locations.OnFieldLocation
import com.jervisffb.engine.model.modifiers.CatchModifier
import com.jervisffb.engine.model.modifiers.DiceModifier
import com.jervisffb.engine.model.modifiers.MarkedModifier
import com.jervisffb.engine.model.modifiers.PlayerStatusEffectType
import com.jervisffb.engine.model.modifiers.StatModifier
import com.jervisffb.engine.rules.common.MissingPlayersOnLoS
import com.jervisffb.engine.rules.common.SetupRule
import com.jervisffb.engine.rules.common.TooManyPlayersInWideZone
import com.jervisffb.engine.rules.common.WrongAmountOfPlayersOnField
import com.jervisffb.engine.rules.common.actions.PlayerAction
import com.jervisffb.engine.rules.common.procedures.DieRoll
import com.jervisffb.engine.rules.common.skills.Duration
import com.jervisffb.engine.rules.common.skills.RerollSource
import com.jervisffb.engine.rules.common.skills.Skill
import com.jervisffb.engine.rules.common.skills.SkillType
import com.jervisffb.engine.rules.common.tables.ThrowInPosition
import com.jervisffb.engine.rules.common.tables.ThrowInTemplate
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
 * Methods in this class have been implemented using the standard BB2025 behavior, so subclasses only need to
 * override them if the behavior differs from that.
 *
 * Developer's Commentary:
 * The idea is that this class should be able to represent all game types, but that hasn't been
 * fully tested yet, e.g., Dungeon Bowl has a very different view of what the field looks
 * and behaves, so most likely some aspects need to be redesigned.*
 * It is also a bit unclear how well this class transcends ruleset, i.e., between BB2016 and BB2020
 */
@Serializable
abstract class Rules(
    private val parameters: RulesParametersHolder
): RulesParameters by parameters {

    /**
     * Checks if a given setup is valid. If not valid, a list of broken rules
     * will be returned. If the setup is valid, an empty list is returned.
     */
    open fun isSetupValid(state: Game, team: Team): List<SetupRule> {
        val isHomeTeam = team.isHomeTeam()
        val inReserve: List<Player> = team.filter { it.state == PlayerState.RESERVE && !it.location.isOnField(this) }
        val onField: List<Player> = team.filter { it.state == PlayerState.STANDING && it.location.isOnField(this) }
        val totalAvailablePlayers: Int = inReserve.size + onField.size

        val brokenRules = mutableListOf<SetupRule>()

        // If below 11 players, all players must be fielded
        if (totalAvailablePlayers < maxPlayersOnField && inReserve.isNotEmpty()) {
            brokenRules.add(
                WrongAmountOfPlayersOnField(
                    availablePlayers = totalAvailablePlayers,
                    playersOnField = onField.size
                )
            )
        }

        // Otherwise 11 players must be on the field
        // TODO Swarming might change this
        if (totalAvailablePlayers >= maxPlayersOnField && onField.size != maxPlayersOnField) {
            brokenRules.add(
                WrongAmountOfPlayersOnField(
                    availablePlayers = totalAvailablePlayers,
                    playersOnField = onField.size
                )
            )
        }

        // Check LoS requirements
        val field = state.field
        val losIndex: Int = if (isHomeTeam) lineOfScrimmageHome else lineOfScrimmageAway
        val playersOnLos =
            (wideZone .. fieldHeight - wideZone).filter { y: Int ->
                field[losIndex, y].isOccupied()
            }.size

        // If available, 3 players must be on the Center Field LoS
        if (totalAvailablePlayers >= playersRequiredOnLineOfScrimmage && playersOnLos < playersRequiredOnLineOfScrimmage) {
            brokenRules.add(
                MissingPlayersOnLoS(
                    players = playersOnLos,
                    requiredPlayers = playersRequiredOnLineOfScrimmage
                )
            )
        }

        // If less than 3 players, all must be on the Centre Field LoS
        if (totalAvailablePlayers < playersRequiredOnLineOfScrimmage && onField.size != playersOnLos) {
            brokenRules.add(
                MissingPlayersOnLoS(
                    players = onField.size,
                    requiredPlayers = totalAvailablePlayers
                )
            )
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
            brokenRules.add(
                TooManyPlayersInWideZone(
                    top = true,
                    players = topWideZoneCount,
                    maxPlayers = maxPlayersInWideZone
                )
            )
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
            brokenRules.add(
                TooManyPlayersInWideZone(
                    top = false,
                    players = bottomWideZoneCount,
                    maxPlayers = maxPlayersInWideZone
                )
            )
        }

        return brokenRules
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
     * template when attempting to throw a ball in after it went out-of-bounds
     * (or Random Direction template in case of corners).
     */
    fun throwIn(from: FieldCoordinate, d3: D3Result): Direction {
        val corner = from.getCornerLocation(this)
        return if (corner != null) {
            randomDirectionTemplate.roll(corner, d3)
        } else {
            when {
                (from.x == 0) -> ThrowInTemplate.roll(ThrowInPosition.LEFT, d3)
                (from.x == fieldWidth - 1) -> ThrowInTemplate.roll(ThrowInPosition.RIGHT, d3)
                (from.y == 0) -> ThrowInTemplate.roll(ThrowInPosition.TOP, d3)
                (from.y == fieldHeight - 1) -> ThrowInTemplate.roll(ThrowInPosition.BOTTOM, d3)
                else -> throw IllegalArgumentException("Cannot determine position of: $from")
            }
        }
    }

    /**
     * Returns the result of rolling on the throw-in template when it is put down
     * anywhere on the field and pointed in a specific direction.
     */
    fun throwIn(direction: Direction, d3: D3Result): Direction {
        return ThrowInTemplate.roll(direction, d3)
    }

    /**
     * Returns whether a player is eligible for catching a ball that landed in their field.
     */
    fun canCatch(player: Player): Boolean {
        // TODO Probably need to account for difference between Bomb and Ball here
        return player.hasTackleZones
            && player.statusEffects.none { it.type == PlayerStatusEffectType.DISTRACTED }
            && player.state == PlayerState.STANDING
            && player.location.isOnField(this)
            && !player.hasBall()
    }

    /**
     * Returns whether a player can deflect a ball if it is thrown over them.
     */
    fun canDeflect(player: Player): Boolean {
        // See rules-faq.md, but we allow a player already holding a ball
        // to deflect.
        // TODO Players with "No Hands" cannot deflect
        return player.hasTackleZones
            && player.state == PlayerState.STANDING
            && player.location.isOnField(this)
    }

    /**
     * Return `true` if this player is able to mark other players.
     */
    fun canMarkPlayers(player: Player): Boolean {
        return player.hasTackleZones && player.state == PlayerState.STANDING
    }

    /**
     * Returns `true` if the player is considered `Open` as described on
     * page 38 in the BB2025 rulebook.
     */
    open fun isOpen(player: Player): Boolean {
        return isStanding(player) && !isMarked(player)
    }

    /**
     * Returns `true` if the player is considered "Standing" as described
     * on page 38 in the BB2025 rulebook.
     */
    fun isStanding(player: Player): Boolean {
        return player.state == PlayerState.STANDING && player.location.isOnField(this)
    }

    /**
     * Returns `true` if the player is considered "Distracted" as described on
     * page 38 in the BB2025 rulebook.
     */
    fun isDistracted(player: Player): Boolean {
        return isStanding(player) && !player.hasTackleZones
    }

    /**
     * Returns `true` if this player has a state that is considered an "Injury"
     * This is mostly used for UI purposes.
     */
    fun isInjuried(player: Player): Boolean {
        return when (player.state) {
            PlayerState.BANNED,
            PlayerState.DODGY_SNACK,
            PlayerState.FAINTED,
            PlayerState.PRONE,
            PlayerState.RESERVE,
            PlayerState.STANDING,
            PlayerState.STUNNED,
            PlayerState.STUNNED_OWN_TURN -> false
            PlayerState.BADLY_HURT,
            PlayerState.DEAD,
            PlayerState.KNOCKED_OUT,
            PlayerState.LASTING_INJURY,
            PlayerState.SERIOUSLY_HURT,
            PlayerState.SERIOUS_INJURY -> true
        }
    }


    /**
     * Returns `true` if the player is considered `Marked as described on
     * page 26 in the rulebook.
     *
     * @param player The player that is checked for marks.
     * @param location The location the player is in. Can be overridden to fake the player
     *     being in another location (used, e.g., when checking if dodging is needed).
     */
    fun isMarked(player: Player, location: Location = player.location): Boolean {
        if (!location.isOnField(this)) return false
        if (location !is FieldCoordinate) return false
        val field = player.team.game.field
        return location.getSurroundingCoordinates(this, 1)
            .asSequence()
            .filter {
                val otherPlayer = field[it].player
                otherPlayer != null && otherPlayer.team != player.team
            }
            .firstOrNull { canMarkPlayers(field[it].player!!) } != null
    }

    /**
     * Returns `true` if [player] count as marking [target], `false` if not.*
     */
    fun isMarking(player: Player, target: Player): Boolean {
        if (!player.location.isOnField(this)) return false
        if (!target.location.isOnField(this)) return false
        if (!player.hasTackleZones) return false
        if (player.state != PlayerState.STANDING) return false
        val state = player.team.game
        return player.coordinates.getSurroundingCoordinates(this, 1)
            .any { state.field[it].player == target }
    }

    /**
     * Calculate how many offensive assists the [attacker] has if all assists
     * are provided.
     *
     * See page 57 in the rulebook:
     * - Must be marking defender
     * - Cannot assist if being marked themselves (by someone other than the defender)
     *
     * @param attacker The attacking player
     * @param defender the defending player
     */
    fun calculateOffensiveAssists(attacker: Player, defender: Player): Int {
        val field = defender.team.game.field
        return defender.coordinates.getSurroundingCoordinates(this)
            .mapNotNull { field[it].player }
            .filter { it != attacker && it.team == attacker.team }
            .count { player ->
                canOfferAssist(player, defender)
            }
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
    fun calculateDefensiveAssists(defender: Player, attacker: Player): Int {
        val field = defender.team.game.field
        return attacker.coordinates.getSurroundingCoordinates(this)
            .mapNotNull { field[it].player }
            .filter { it != defender && it.team == defender.team }
            .count { player ->
                canOfferAssist(player, attacker)
            }
    }

    /**
     * Return `true` if the [assister] player can offer either an offensive or
     * defensive assist against [target], `false` if not.
     *
     * See page 57 in the BB2020 rulebook.
     * See page 61 in the BB2025 rulebook.
     */
    fun canOfferAssist(
        assister: Player,
        target: Player,
    ): Boolean {
        if (assister.team == target.team) return false
        if (!assister.location.isAdjacent(this, target.location)) return false
        if (!canMarkPlayers(assister)) return false
        // Eye Gouge is only present in BB2025, but should be safe to check for in common code
        if (assister.statusEffects.any { it.type == PlayerStatusEffectType.EYE_GOUGE }) return false

        // We always apply Guard and Defensive.
        // They are technically optional skills, but there should be no reason
        // (not even a bad one) to not apply them.
        val hasGuard = assister.isSkillAvailable(SkillType.GUARD)
        if (hasGuard) {
            val state = assister.team.game
            val ignoreGuard = getMarkingPlayers(
                game = state,
                markedTeam = assister.team,
                square = assister.coordinates
            ).any {
                it.isSkillAvailable(SkillType.DEFENSIVE)
            }
            if (!ignoreGuard) return true
        }

        // A player can only assist if they themselves are not being marked.
        // This logic does not take into account any skills.
        val field = assister.team.game.field
        return assister.coordinates
            .getSurroundingCoordinates(this, 1, false)
            .none { coordinate ->
                // Check that no opponents prevent `assister` from actually assisting
                // This does not take into account any skills.
                val adjacentPlayer = field[coordinate].player
                val isOpponent = (adjacentPlayer?.team != assister.team)
                if (adjacentPlayer != null && isOpponent && adjacentPlayer != target) {
                    canMarkPlayers(adjacentPlayer)
                } else {
                    false
                }
            }
    }

    /**
     * Calculate how many marks are on [square] for a player on the [markedTeam].
     * Marks will be returned as modifiers in the [modifiers] list.
     */
    fun addMarkedModifiers(
        game: Game,
        markedTeam: Team,
        square: FieldCoordinate,
        modifiers: MutableList<DiceModifier>,
        markedModifier: DiceModifier = CatchModifier.MARKED
    ) {
        val marks = calculateMarks(game, markedTeam, square)
        if (marks > 0) {
            modifiers.add(MarkedModifier(marks, markedModifier))
        }
    }

    /**
     * Returns all players not from the [markedTeam] that can mark the [square].
     */
    fun getMarkingPlayers(
        game: Game,
        markedTeam: Team,
        square: FieldCoordinate,
    ): List<Player> {
        if (!square.isOnField(this)) throw IllegalArgumentException("${square.toLogString()} is not on the field")
        return square.getSurroundingCoordinates(this).mapNotNull { coordinate ->
            val markingPlayer: Player? = game.field[coordinate].player
            val otherTeam = markingPlayer?.team
            val canMark = markingPlayer?.let { canMarkPlayers(it) } ?: false
            if (markingPlayer != null && otherTeam != markedTeam && canMark) {
                markingPlayer
            } else {
                null
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
            val canMark = markingPlayer?.let { canMarkPlayers(it) } ?: false
            if (markingPlayer != null && otherTeam != markedTeam && canMark) {
                acc + 1
            } else {
                acc
            }
        }
    }

    fun canUseTeamReroll(game: Game, player: Player?): Boolean {
        if (!game.canUseTeamRerolls) return false
        if (player != null && game.activeTeam != player.team) return false
        return when (game.activeTeam?.usedRerollThisTurn) {
            true -> allowMultipleTeamRerollsPrTurn
            false -> true
            null -> false // If there is no active team, rerolls are not allowed
        }
    }

    /**
     * Return all locations you can choose from when pushing a player.
     * This only returns the normal push options and doesn't take into
     * account skills or if the squares are occupied.
     *
     * If that matters or not, it is up to the caller of this method.
     *
     * If pushing a player OUT_OF_BOUNDS is possible, all options to do so will
     * be possible and should be deteted using [FieldCoordinate.isOutOfBounds].
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
            .map { it.first }
            .toSet()
        return result
    }

    /**
     * Returns `true` if the team has a hold of the ball.
     *
     * @param ball if set, only this ball is checked, if `false` any ball is accepted.
     */
    fun teamHasBall(team: Team, ball: Ball? = null): Boolean {
        return team.firstOrNull {
            if (ball != null) {
                it.ball == ball
            } else {
                it.hasBall()
            }
        } != null
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
    fun getAvailableTeamRerolls(team: Team): List<RerollSource> {
        return team.availableRerolls
            .filter { it.enabled }
            .distinctBy { it::class }
    }

    /**
     * Returns all actions available to this player when they are activated.
     * This method should filter out actions that require targets that do not
     * exist, like Blitz or Foul (in BB2020).
     */
    abstract fun getAvailableActions(state: Game, player: Player): List<PlayerAction>

    /**
     * When either a `baseX` or `XModifiers` stat value has been updated, this method should also
     * be called so the total player stat can be calculated correctly.
     */
    fun updatePlayerStat(player: Player, stat: StatModifier.Type) {
        with(player) {
            when (stat) {
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
                    passing = newPassing?.coerceIn(passingRange)
                }
                StatModifier.Type.AG -> agility = (baseAgility + agilityModifiers.sum()).coerceIn(agilityRange)
                StatModifier.Type.ST -> strength = (baseStrength + strengthModifiers.sum()).coerceIn(strengthRange)
            }
        }
    }

    /**
     * Returns true if the current game is the start of a half (and not start of overtime)
     */
    fun isStartOfHalf(state: Game): Boolean {
        val rules = state.rules
        return (state.halfNo >= 1&& state.halfNo <= rules.halfsPrGame)
            && state.homeTeam.turnMarker == 0
            && state.homeTeam.turnMarker == 0
    }

    /**
     * Skills might change subtly between rule versions, for that reason, we need a single place to lookup
     * skill definitions from their id (since we might want to support teams across multiple rulesets).
     */
    fun createSkill(player: Player, skill: SkillId, expiresAt: Duration = Duration.PERMANENT): Skill<*> {
        return skillSettings.createSkill(player, skill, expiresAt)
    }

    /**
     * Returns `true` if rerolls of some dice in the dice pool are still allowed.
     * Note; this doesn't mean that a reroll is available, just that it is allowed
     * if possible.
     */
    fun isRerollAllowed(dicePool: List<DieRoll<*>>): Boolean {
        // It is only allowed to reroll a die a single time. So if a rerollSource
        // exists, it cannot be rerolled again
        return dicePool.none { it.rerollSource != null }
    }

    /**
     * Return `true` if a Team Reroll is allowed to re-roll this type of roll.
     *
     * We keep this method here as some skills offer a re-roll that is
     * functionaly the same as a Team Reroll. They should all go through this
     * method.
     */
    fun canBeRerolledByTeamReroll(type: DiceRollType): Boolean {
        return when (type) {
            // Explicitly mentioned in the rulebook (page 33 BB2025)
            DiceRollType.ARGUE_THE_CALL,
            DiceRollType.ARMOUR,
            DiceRollType.BOUNCE, // Is a Scatter(1)
            DiceRollType.BRIBE,
            DiceRollType.CASUALTY,
            DiceRollType.CROWD_TAKES_ACTION,
            DiceRollType.INJURY,
            DiceRollType.LASTING_INJURY, // Assume it is covered by CASUALTY
            DiceRollType.SCATTER,
            DiceRollType.THROWIN_DIRECTION,
            DiceRollType.THROWIN_DISTANCE,

            // No team is active: Pre-game / Kick-off events / Post-game
            DiceRollType.BAD_HABITS,
            DiceRollType.BLITZ, // Kick-off Event
            DiceRollType.BRILLIANT_COACHING,
            DiceRollType.CHARGE,
            DiceRollType.DEVIATE,
            DiceRollType.DODGY_SNACK_EFFECT,
            DiceRollType.DODGY_SNACK_ROLL_OFF,
            DiceRollType.FAN_FACTOR,
            DiceRollType.KICK_OFF_TABLE,
            DiceRollType.OFFICIOUS_REF_FAN_FACTOR,
            DiceRollType.OFFICIOUS_REF_REFEREE,
            DiceRollType.PITCH_INVASION_FAN_FACTOR,
            DiceRollType.PITCH_INVASION_PLAYERS_AFFECTED,
            DiceRollType.PRAYERS_TO_NUFFLE, // Only in BB2020
            DiceRollType.QUICK_SNAP,
            DiceRollType.SOLID_DEFENSE,
            DiceRollType.SUDDEN_DEATH,
            DiceRollType.SWELTERING_HEAT,
            DiceRollType.THROW_A_ROCK,

            // Covered by "can not be used if team is not active"
            DiceRollType.INTERCEPTION,
            DiceRollType.PASSING_INTERFERENCE,
            DiceRollType.WEATHER,

            // Probably cannot be used, find reference
            DiceRollType.BB7_APOTHECARY -> false

            // All of these should be allowed
            DiceRollType.ACCURACY,
            DiceRollType.BLOCK,
            DiceRollType.BLOODLUST,
            DiceRollType.BONE_HEAD,
            DiceRollType.BREATHE_FIRE,
            DiceRollType.CATCH,
            DiceRollType.CHAINSAW,
            DiceRollType.CHEERING_FANS,
            DiceRollType.DAUNTLESS,
            DiceRollType.DODGE,
            DiceRollType.FOUL_APPEARANCE,
            DiceRollType.HYPNOTIC_GAZE,
            DiceRollType.JUMP,
            DiceRollType.JUMP_UP,
            DiceRollType.LANDING,
            DiceRollType.LEAP,
            DiceRollType.LONER,
            DiceRollType.PASS,
            DiceRollType.PICKUP,
            DiceRollType.POGO,
            DiceRollType.PRO,
            DiceRollType.PROJECTILE_VOMIT,
            DiceRollType.QUALITY,
            DiceRollType.REALLY_STUPID,
            DiceRollType.REGENERATION,
            DiceRollType.RUSH,
            DiceRollType.SECURE_THE_BALL,
            DiceRollType.SHADOWING,
            DiceRollType.STANDING_UP,
            DiceRollType.STEADY_FOOTING,
            DiceRollType.SWOOP_DIRECTION,
            DiceRollType.SWOOP_DISTANCE,
            DiceRollType.TAKE_ROOT,
            DiceRollType.TEAM_CAPTAIN,
            DiceRollType.TEAM_MASCOT,
            DiceRollType.TREACHEROUS_TRAPDOOR,
            DiceRollType.UNCHANNELLED_FURY -> true
        }
    }



    abstract fun toBuilder(): RulesParameterBuilder

}
