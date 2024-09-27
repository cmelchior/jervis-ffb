package dk.ilios.jervis.rules

import dk.ilios.jervis.actions.D3Result
import dk.ilios.jervis.actions.D8Result
import dk.ilios.jervis.model.FieldSquare
import dk.ilios.jervis.model.Game
import dk.ilios.jervis.model.Player
import dk.ilios.jervis.model.PlayerState
import dk.ilios.jervis.model.Team
import dk.ilios.jervis.model.locations.FieldCoordinate
import dk.ilios.jervis.model.locations.FieldCoordinate.Companion.OUT_OF_BOUNDS
import dk.ilios.jervis.model.locations.Location
import dk.ilios.jervis.model.modifiers.CatchModifier
import dk.ilios.jervis.model.modifiers.DiceModifier
import dk.ilios.jervis.rules.pathfinder.BB2020PathFinder
import dk.ilios.jervis.rules.pathfinder.PathFinder
import dk.ilios.jervis.rules.skills.RerollSource
import dk.ilios.jervis.rules.skills.SpecialActionProvider
import dk.ilios.jervis.rules.tables.ArgueTheCallTable
import dk.ilios.jervis.rules.tables.CasualtyTable
import dk.ilios.jervis.rules.tables.Direction
import dk.ilios.jervis.rules.tables.InjuryTable
import dk.ilios.jervis.rules.tables.KickOffEventTable
import dk.ilios.jervis.rules.tables.LastingInjuryTable
import dk.ilios.jervis.rules.tables.PrayersToNuffleTable
import dk.ilios.jervis.rules.tables.RandomDirectionTemplate
import dk.ilios.jervis.rules.tables.RangeRuler
import dk.ilios.jervis.rules.tables.StuntyInjuryTable
import dk.ilios.jervis.rules.tables.ThrowInPosition
import dk.ilios.jervis.rules.tables.ThrowInTemplate
import dk.ilios.jervis.rules.tables.WeatherTable
import dk.ilios.jervis.utils.INVALID_GAME_STATE

interface Rules {
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
     * Returns whether a not a player is eligible for catching a ball that landed in his field.
     */
    fun canCatch(
        state: Game,
        player: Player,
    ): Boolean {
        return player.hasTackleZones && player.state == PlayerState.STANDING && player.location.isOnField(this)
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
        movingTeam: Team,
        square: FieldCoordinate,
    ): Int {
        if (!square.isOnField(this)) throw IllegalArgumentException("${square.toLogString()} is not on the field")
        return square.getSurroundingCoordinates(this).fold(initial = 0) { acc, coordinate ->
            val markingPlayer: Player? = game.field[coordinate].player
            val otherTeam = markingPlayer?.team
            val canMark = markingPlayer?.let { canMark(it) } ?: false
            if (markingPlayer != null && otherTeam != movingTeam && canMark) {
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
     * If that matters or not is up to the call of this method.
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



//        return (pushee.location as FieldCoordinate).getSurroundingCoordinates(this, includeOutOfBounds = true)
//            .toSet() // Remove multiple instances of OUT_OF_BOUNDS
//            .map {
//                Pair(it, it.realDistanceTo(start))
//            }
//            .sortedBy { it.second }
//            .subList(0, 3)
//            .map { it.first }
//            .toSet()
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
     */
    fun getAvailableTeamReroll(team: Team): RerollSource {
        return team.availableRerolls.last()
    }

    /**
     * Returns all actions available to this player when they are activated.
     */
    fun getAvailableActions(state: Game, player: Player): List<PlayerAction> {
        if (state.activePlayer != player) INVALID_GAME_STATE("$player is not the active player")

        return buildList {
            // Add any team actions that are available
            state.activeTeam.turnData.let {
                if (it.moveActions > 0) add(teamActions.move)
                if (it.passActions > 0) add(teamActions.pass)
                if (it.handOffActions > 0) add(teamActions.handOff)
                if (it.blockActions > 0) add(teamActions.block)
                if (it.blitzActions > 0) add(teamActions.blitz)
                if (it.foulActions > 0) add(teamActions.foul)
            }

            // Add any special actions that are provided by skills
            player.skills.filterIsInstance<SpecialActionProvider>().forEach {
                val type = it.specialAction
                val isSkillActionUsed = it.isSpecialActionUsed
                val isActionAvailable = state.activeTeam.turnData.availableSpecialActions[type]!! > 0
                if (!isSkillActionUsed && isActionAvailable) {
                    add(teamActions[type])
                }
            }
        }
    }

    val name: String

    // Characteristic limits
    // See page 28 in the rulebook
    val moveRange: IntRange
        get() = 1..9

    val strengthRange: IntRange
        get() = 1..8

    val agilityRange: IntRange
        get() = 1 .. 6

    val passingRange: IntRange
        get() = 1.. 6

    val armorValueRange: IntRange
        get() = 3 .. 11

    val rushesPrAction: Int
        get() = 2

    // Game length setup

    val halfsPrGame: Int
        get() = 2

    val turnsPrHalf: Int
        get() = 8

    // Field description

    // Total width of the field
    val fieldWidth: Int
        get() = 26

    // Total height of the field
    val fieldHeight: Int
        get() = 15

    // Height of the Wide Zone at the top and bottom of the field
    val wideZone: Int
        get() = 4

    // Width of the End Zone at each end of the field
    val endZone: Int
        get() = 1

    // X-coordinates for the line of scrimmage for the home team
    val lineOfScrimmageHome: Int
        get() = 12

    // X-coordinate for the line of scrimmage for the away team
    val lineOfScrimmageAway: Int
        get() = 13

    val playersRequiredOnLineOfScrimmage: Int
        get() = 3

    val maxPlayersInWideZone: Int
        get() = 2

    val maxPlayersOnField: Int
        get() = 11

    val randomDirectionTemplate
        get() = RandomDirectionTemplate

    val kickOffEventTable
        get() = KickOffEventTable

    val prayersToNuffleTable
        get() = PrayersToNuffleTable

    val weatherTable
        get() = WeatherTable

    val injuryTable
        get() = InjuryTable

    val stuntyInjuryTable
        get() = StuntyInjuryTable

    val casualtyTable
        get() = CasualtyTable

    val lastingInjuryTable
        get() = LastingInjuryTable

    val argueTheCallTable
        get() = ArgueTheCallTable

    val rangeRuler
        get() = RangeRuler

    val teamActions: TeamActions
        get() = BB2020TeamActions()

    val allowMultipleTeamRerollsPrTurn: Boolean
        get() = true

    val pathFinder: PathFinder
        get() = BB2020PathFinder(this)

    // Blood Bowl 7
    // Total width of the field
//    val fieldWidth = 2
//    val fieldHeight = 11
//    val wideZone = 2
//    val endZone = 1
//    val lineOfScrimmage = 7
}
