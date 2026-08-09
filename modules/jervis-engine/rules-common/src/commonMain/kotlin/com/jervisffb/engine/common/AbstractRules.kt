package com.jervisffb.engine.common

import com.jervisffb.engine.actions.D3Result
import com.jervisffb.engine.actions.D8Result
import com.jervisffb.engine.actions.InducementSelection
import com.jervisffb.engine.commands.Command
import com.jervisffb.engine.common.inducements.StandardApothecary
import com.jervisffb.engine.common.modifiers.MarkedModifier
import com.jervisffb.engine.common.procedures.FullGame
import com.jervisffb.engine.common.procedures.TheFumbblKickOff
import com.jervisffb.engine.common.procedures.TheKickOff
import com.jervisffb.engine.common.procedures.inducements.BuyInducements
import com.jervisffb.engine.common.procedures.tables.injury.UseBB11Apothecary
import com.jervisffb.engine.common.procedures.tables.injury.UseBB7Apothecary
import com.jervisffb.engine.fsm.Procedure
import com.jervisffb.engine.model.Ball
import com.jervisffb.engine.model.Direction
import com.jervisffb.engine.model.Game
import com.jervisffb.engine.model.PitchSquare
import com.jervisffb.engine.model.Player
import com.jervisffb.engine.model.PlayerDogoutState
import com.jervisffb.engine.model.PlayerPitchState
import com.jervisffb.engine.model.SkillId
import com.jervisffb.engine.model.Team
import com.jervisffb.engine.model.inducements.Apothecary
import com.jervisffb.engine.model.inducements.settings.InducementType
import com.jervisffb.engine.model.isSkillAvailable
import com.jervisffb.engine.model.locations.Location
import com.jervisffb.engine.model.locations.OnPitchLocation
import com.jervisffb.engine.model.locations.PitchCoordinate
import com.jervisffb.engine.model.modifiers.DiceModifier
import com.jervisffb.engine.model.modifiers.PlayerStatusEffectType
import com.jervisffb.engine.model.modifiers.StatModifier
import com.jervisffb.engine.rules.DiceRollType
import com.jervisffb.engine.rules.Rules
import com.jervisffb.engine.rules.RulesParameters
import com.jervisffb.engine.rules.builder.KickingPlayerBehavior
import com.jervisffb.engine.rules.builder.UseApothecaryBehavior
import com.jervisffb.engine.rules.common.InducementLimitExceeded
import com.jervisffb.engine.rules.common.InducementNotAvailableToTeam
import com.jervisffb.engine.rules.common.InducementNotEnabled
import com.jervisffb.engine.rules.common.InducementNotFound
import com.jervisffb.engine.rules.common.InducementRule
import com.jervisffb.engine.rules.common.MissingPlayersOnLoS
import com.jervisffb.engine.rules.common.SetupRule
import com.jervisffb.engine.rules.common.TooManyPlayersInWideZone
import com.jervisffb.engine.rules.common.TooMuchGoldUsed
import com.jervisffb.engine.rules.common.WrongAmountOfPlayersOnPitch
import com.jervisffb.engine.rules.common.actions.BlockType
import com.jervisffb.engine.rules.common.skills.Duration
import com.jervisffb.engine.rules.common.skills.RerollSource
import com.jervisffb.engine.rules.common.skills.Skill
import com.jervisffb.engine.rules.common.skills.SkillType
import com.jervisffb.engine.rules.common.tables.ThrowInPosition
import com.jervisffb.engine.rules.common.tables.ThrowInTemplate
import com.jervisffb.engine.utils.sum
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import kotlin.math.min

/**
 * Abstract-class base for all [com.jervisffb.engine.rules.Rules] implementations.
 *
 * This abstract class encodes standard BB2025 rules, so subclasses must
 * override members with this in mind.
 */
@Serializable
abstract class AbstractRules(
    private val parameters: RulesParameters,
) : Rules, RulesParameters by parameters {

    override fun isSetupValid(state: Game, team: Team): List<SetupRule> {
        val isHomeTeam = team.isHomeTeam()
        val inReserve: List<Player> = team.filter { it.state == PlayerDogoutState.RESERVE && !it.location.isOnPitch(this) }
        val onField: List<Player> = team.filter { it.state == PlayerPitchState.STANDING && it.location.isOnPitch(this) }
        val totalAvailablePlayers: Int = inReserve.size + onField.size

        val brokenRules = mutableListOf<SetupRule>()

        // If below 11 players, all players must be fielded on the pitch
        if (totalAvailablePlayers < maxPlayersOnPitch && inReserve.isNotEmpty()) {
            brokenRules.add(
                WrongAmountOfPlayersOnPitch(
                    availablePlayers = totalAvailablePlayers,
                    playersOnPitch = onField.size,
                ),
            )
        }

        // Otherwise 11 players must be on the pitch
        // TODO Swarming might change this
        if (totalAvailablePlayers >= maxPlayersOnPitch && onField.size != maxPlayersOnPitch) {
            brokenRules.add(
                WrongAmountOfPlayersOnPitch(
                    availablePlayers = totalAvailablePlayers,
                    playersOnPitch = onField.size,
                ),
            )
        }

        // Check LoS requirements
        val field = state.pitch
        val losIndex: Int = if (isHomeTeam) lineOfScrimmageHome else lineOfScrimmageAway
        val playersOnLos =
            (wideZone..pitchHeight - wideZone).filter { y: Int ->
                field[losIndex, y].isOccupied()
            }.size

        // If available, 3 players must be on the Center Field LoS
        if (totalAvailablePlayers >= playersRequiredOnLineOfScrimmage && playersOnLos < playersRequiredOnLineOfScrimmage) {
            brokenRules.add(
                MissingPlayersOnLoS(
                    players = playersOnLos,
                    requiredPlayers = playersRequiredOnLineOfScrimmage,
                ),
            )
        }

        // If less than 3 players, all must be on the Centre Field LoS
        if (totalAvailablePlayers < playersRequiredOnLineOfScrimmage && onField.size != playersOnLos) {
            brokenRules.add(
                MissingPlayersOnLoS(
                    players = onField.size,
                    requiredPlayers = totalAvailablePlayers,
                ),
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
            (pitchWidth - 1 downTo lineOfScrimmageAway).forEach { x ->
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
                    maxPlayers = maxPlayersInWideZone,
                ),
            )
        }

        // Max two players on the Bottom Wide Zone
        var bottomWideZoneCount = 0
        if (isHomeTeam) {
            (0..lineOfScrimmageHome).forEach { x ->
                (pitchHeight - wideZone until pitchHeight).forEach { y ->
                    if (field[x, y].isOccupied()) {
                        bottomWideZoneCount++
                    }
                }
            }
        } else {
            (pitchWidth - 1 downTo lineOfScrimmageAway).forEach { x ->
                (pitchHeight - wideZone until pitchHeight).forEach { y ->
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
                    maxPlayers = maxPlayersInWideZone,
                ),
            )
        }

        return brokenRules
    }

    override fun isInSetupArea(team: Team, location: PitchCoordinate): Boolean {
        return if (team.isHomeTeam()) {
            location.x <= lineOfScrimmageHome
        } else {
            location.x >= lineOfScrimmageAway
        }
    }

    override fun canPlaceBallForKickoff(kickingTeam: Team, location: PitchSquare): Boolean {
        return when (kickingTeam.isHomeTeam()) {
            true -> location.x > lineOfScrimmageHome
            false -> location.x < lineOfScrimmageAway
        }
    }

    override fun direction(d8: D8Result): Direction = randomDirectionTemplate.roll(d8)

    override fun throwIn(from: PitchCoordinate, d3: D3Result): Direction {
        val corner = from.getCornerLocation(this)
        return if (corner != null) {
            randomDirectionTemplate.roll(corner, d3)
        } else {
            when {
                (from.x == 0) -> ThrowInTemplate.roll(ThrowInPosition.LEFT, d3)
                (from.x == pitchWidth - 1) -> ThrowInTemplate.roll(ThrowInPosition.RIGHT, d3)
                (from.y == 0) -> ThrowInTemplate.roll(ThrowInPosition.TOP, d3)
                (from.y == pitchHeight - 1) -> ThrowInTemplate.roll(ThrowInPosition.BOTTOM, d3)
                else -> throw IllegalArgumentException("Cannot determine position of: $from")
            }
        }
    }

    override fun throwIn(direction: Direction, d3: D3Result): Direction {
        return ThrowInTemplate.roll(direction, d3)
    }

    override fun canCatch(player: Player): Boolean {
        // TODO Probably need to account for difference between Bomb and Ball here
        return player.hasTackleZones &&
            player.statusEffects.none { it.type == PlayerStatusEffectType.DISTRACTED } &&
            player.state == PlayerPitchState.STANDING &&
            player.location.isOnPitch(this) &&
            !player.hasBall()
    }

    override fun canDeflect(player: Player): Boolean {
        // See rules-faq.md, but we allow a player already holding a ball
        // to deflect.
        // TODO Players with "No Hands" cannot deflect
        return player.hasTackleZones &&
            player.state == PlayerPitchState.STANDING &&
            player.location.isOnPitch(this)
    }

    override fun canMarkPlayers(player: Player): Boolean {
        return player.hasTackleZones && player.state == PlayerPitchState.STANDING
    }

    override fun isOpen(player: Player): Boolean {
        return isStanding(player) && !isMarked(player)
    }

    override fun isStanding(player: Player): Boolean {
        return player.state == PlayerPitchState.STANDING && player.location.isOnPitch(this)
    }

    override fun isInjuried(player: Player): Boolean {
        return when (player.state) {
            PlayerDogoutState.BANNED,
            PlayerDogoutState.DODGY_SNACK,
            PlayerDogoutState.FAINTED,
            PlayerPitchState.PRONE,
            PlayerDogoutState.RESERVE,
            PlayerPitchState.STANDING,
            PlayerPitchState.STUNNED,
            PlayerPitchState.STUNNED_OWN_TURN,
            -> false
            PlayerDogoutState.BADLY_HURT,
            PlayerDogoutState.DEAD,
            PlayerDogoutState.KNOCKED_OUT,
            PlayerDogoutState.LASTING_INJURY,
            PlayerDogoutState.SERIOUSLY_HURT,
            PlayerDogoutState.SERIOUS_INJURY,
            -> true
        }
    }

    override fun isMarked(player: Player, location: Location): Boolean {
        if (!location.isOnPitch(this)) return false
        if (location !is PitchCoordinate) return false
        val field = player.team.game.pitch
        return location.getSurroundingCoordinates(this, 1)
            .asSequence()
            .filter {
                val otherPlayer = field[it].player
                otherPlayer != null && otherPlayer.team != player.team
            }
            .firstOrNull { canMarkPlayers(field[it].player!!) } != null
    }

    override fun isMarking(player: Player, target: Player): Boolean {
        if (!player.location.isOnPitch(this)) return false
        if (!target.location.isOnPitch(this)) return false
        if (!player.hasTackleZones) return false
        if (player.state != PlayerPitchState.STANDING) return false
        val state = player.team.game
        return player.coordinates.getSurroundingCoordinates(this, 1)
            .any { state.pitch[it].player == target }
    }

    override fun isMarking(player: Player, target: Location): Boolean {
        if (!player.location.isOnPitch(this)) return false
        if (target !is OnPitchLocation) return false
        if (!player.hasTackleZones) return false
        if (player.state != PlayerPitchState.STANDING) return false
        val state = player.team.game
        return target.getSurroundingCoordinates(this, 1)
            .any { state.pitch[it] == player.location }
    }

    override fun calculateOffensiveAssists(attacker: Player, defender: Player): Int {
        val field = defender.team.game.pitch
        return defender.coordinates.getSurroundingCoordinates(this)
            .mapNotNull { field[it].player }
            .filter { it != attacker && it.team == attacker.team }
            .count { player ->
                canOfferAssist(player, defender)
            }
    }

    override fun calculateDefensiveAssists(defender: Player, attacker: Player): Int {
        val field = defender.team.game.pitch
        return attacker.coordinates.getSurroundingCoordinates(this)
            .mapNotNull { field[it].player }
            .filter { it != defender && it.team == defender.team }
            .count { player ->
                canOfferAssist(player, attacker)
            }
    }

    override fun canOfferAssist(assister: Player, target: Player): Boolean {
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
                square = assister.coordinates,
            ).any {
                it.isSkillAvailable(SkillType.DEFENSIVE)
            }
            if (!ignoreGuard) return true
        }

        // A player can only assist if they themselves are not being marked.
        // This logic does not take into account any skills.
        val field = assister.team.game.pitch
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

    override fun addMarkedModifiers(
        game: Game,
        markedTeam: Team,
        square: PitchCoordinate,
        modifiers: MutableList<DiceModifier>,
        markedModifier: DiceModifier,
    ) {
        val marks = calculateMarks(game, markedTeam, square)
        if (marks > 0) {
            modifiers.add(MarkedModifier(marks, markedModifier))
        }
    }

    override fun getMarkingPlayers(game: Game, markedTeam: Team, square: PitchCoordinate): List<Player> {
        if (!square.isOnPitch(this)) throw IllegalArgumentException("${square.toLogString()} is not on the Pitch")
        return square.getSurroundingCoordinates(this).mapNotNull { coordinate ->
            val markingPlayer: Player? = game.pitch[coordinate].player
            val otherTeam = markingPlayer?.team
            val canMark = markingPlayer?.let { canMarkPlayers(it) } ?: false
            if (markingPlayer != null && otherTeam != markedTeam && canMark) {
                markingPlayer
            } else {
                null
            }
        }
    }

    override fun calculateMarks(game: Game, markedTeam: Team, square: OnPitchLocation): Int {
        if (!square.isOnPitch(this)) throw IllegalArgumentException("${square.toLogString()} is not on the Pitch")
        return square.getSurroundingCoordinates(this).fold(initial = 0) { acc, coordinate ->
            val markingPlayer: Player? = game.pitch[coordinate].player
            val otherTeam = markingPlayer?.team
            val canMark = markingPlayer?.let { canMarkPlayers(it) } ?: false
            if (markingPlayer != null && otherTeam != markedTeam && canMark) {
                acc + 1
            } else {
                acc
            }
        }
    }

    override fun canUseTeamReroll(game: Game, player: Player?): Boolean {
        if (!game.canUseTeamRerolls) return false
        if (player != null && game.activeTeam != player.team) return false
        return when (game.activeTeam?.usedRerollThisTurn) {
            true -> allowMultipleTeamRerollsPrTurn
            false -> true
            null -> false // If there is no active team, rerolls are not allowed
        }
    }

    override fun getPushOptions(pusher: Player, pushee: Player): Set<PitchCoordinate> {
        val start: PitchCoordinate = pusher.location as? PitchCoordinate ?: throw IllegalStateException("Pusher must be on Pitch.")
        val direction: PitchCoordinate = pushee.location as? PitchCoordinate ?: throw IllegalStateException("Pushee must be on Pitch.")
        if (!start.isAdjacent(this, direction)) {
            throw IllegalArgumentException("Pusher and Pushee must be adjacent to each other")
        }

        val all = (pushee.location as PitchCoordinate).getSurroundingCoordinates(this, includeOutOfBounds = true).toSet()
        val map = all.map { Pair(it, it.realDistanceTo(start)) }
        val result = map
            .sortedByDescending { it.second }
            .subList(0, 3)
            .map { it.first }
            .toSet()
        return result
    }

    override fun teamHasBall(team: Team, ball: Ball?): Boolean {
        return team.firstOrNull {
            if (ball != null) {
                it.ball == ball
            } else {
                it.hasBall()
            }
        } != null
    }

    override fun getAvailableTeamRerolls(team: Team): List<RerollSource> {
        return team.availableRerolls
            .filter { it.enabled }
            .distinctBy { it::class }
    }

    override fun updatePlayerStat(player: Player, stat: StatModifier.Type) {
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

    override fun isStartOfHalf(state: Game): Boolean {
        val rules = state.rules
        return (state.halfNo >= 1 && state.halfNo <= rules.halfsPrGame) &&
            state.homeTeam.turnMarker == 0 &&
            state.homeTeam.turnMarker == 0
    }

    override fun createSkill(player: Player, skill: SkillId, expiresAt: Duration): Skill<*> {
        return skillSettings.createSkill(player, skill, expiresAt)
    }

    override fun canBeRerolledByTeamReroll(type: DiceRollType): Boolean {
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
            DiceRollType.COIN_TOSS,
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
            DiceRollType.RECOVER_PLAYER,
            DiceRollType.SOLID_DEFENSE,
            DiceRollType.SUDDEN_DEATH,
            DiceRollType.SWELTERING_HEAT,
            DiceRollType.THROW_A_ROCK,

            // Covered by "can not be used if team is not active"
            DiceRollType.INTERCEPTION,
            DiceRollType.PASSING_INTERFERENCE,
            DiceRollType.WEATHER,

            // Probably cannot be used, find reference
            DiceRollType.BB7_APOTHECARY,
            -> false

            // All of these should be allowed
            DiceRollType.ACCURACY,
            DiceRollType.ALWAYS_HUNGRY,
            DiceRollType.ALWAYS_HUNGRY_EAT_ATTEMPT,
            DiceRollType.ANIMAL_SAVAGERY,
            DiceRollType.BLOCK,
            DiceRollType.BLOODLUST,
            DiceRollType.BONE_HEAD,
            DiceRollType.BREATHE_FIRE,
            DiceRollType.CATCH,
            DiceRollType.CHAINSAW,
            DiceRollType.CHEERING_FANS,
            DiceRollType.CHOMP,
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
            DiceRollType.PUNT_DIRECTION,
            DiceRollType.PUNT_DISTANCE,
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
            DiceRollType.TENTACLES,
            DiceRollType.TREACHEROUS_TRAPDOOR,
            DiceRollType.UNCHANNELLED_FURY,
            -> true
        }
    }

    /** The root procedure for a full game; shared across all rulesets. */
    @Transient
    override val fullGameStep: Procedure = FullGame

    /** Which apothecary procedure to use; resolved from [useApothecaryBehavior]. */
    override val useApothecaryStep: Procedure
        get() = when (useApothecaryBehavior) {
            UseApothecaryBehavior.STANDARD -> UseBB11Apothecary
            UseApothecaryBehavior.BB7 -> UseBB7Apothecary
        }

    override val kickOffStep: Procedure
        get() = when (kickingPlayerBehavior) {
            KickingPlayerBehavior.STRICT -> TheKickOff
            KickingPlayerBehavior.FUMBBL -> TheFumbblKickOff
        }

    override val proSuccessTarget: Int = 3

    override fun getEndOfTurnResetCommands(player: Player): List<Command> = emptyList()

    override fun wasSecretWeaponOnPitchDuringDrive(player: Player): Boolean = false

    override fun setSecretWeaponOnPitchDuringDrive(player: Player, onPitch: Boolean): Command? = null

    override fun getLoneFoulerRerollSource(player: Player): RerollSource? = null

    // This logic is identical between BB2020 and BB2025.
    override fun getAvailableBlockType(player: Player, isMultipleBlock: Boolean): List<BlockType> {
        return buildList {
            BlockType.entries.forEach { type ->
                when (type) {
                    BlockType.BREATHE_FIRE -> if (player.isSkillAvailable(SkillType.BREATHE_FIRE)) add(type)
                    BlockType.CHAINSAW -> if (player.isSkillAvailable(SkillType.CHAINSAW)) add(type)
                    BlockType.CHOMP -> if (player.isSkillAvailable(SkillType.MONSTROUS_MOUTH)) add(type)
                    BlockType.MULTIPLE_BLOCK -> if (!isMultipleBlock) add(type)
                    BlockType.PROJECTILE_VOMIT -> if (player.isSkillAvailable(SkillType.PROJECTILE_VOMIT)) add(type)
                    BlockType.STAB -> if (player.isSkillAvailable(SkillType.STAB)) add(type)
                    BlockType.STANDARD -> add(type)
                }
            }
        }
    }

    override fun createTeamApothecary(): Apothecary {
        return StandardApothecary(used = false)
    }

    override fun isInducementsValid(team: Team, inducements: List<InducementSelection<*>>): List<InducementRule> {

        fun isValid(team: Team, maxLimit: Int): List<InducementRule> {
            val errors = mutableListOf<InducementRule>()
            val rules = team.game.rules
            val settings = rules.inducements
            var usedGold = 0
            val typeCount = mutableMapOf<InducementType, Int>()
            for (inducement in inducements) {
                val inducementSettings = settings[inducement.type]
                if (inducementSettings == null) {
                    errors.add(InducementNotFound(inducement.type))
                    continue
                }
                if (!inducementSettings.enabled) {
                    errors.add(InducementNotEnabled(inducement.type))
                }
                // Ideally, all inducements of the same type should be in a single InducementSelection,
                // but right now this is not enforced, so here we track it across multiple selections.
                val updatedCount = (typeCount.getOrElse(inducement.type) { 0 } + inducement.count)
                typeCount[inducement.type] = updatedCount
                if (inducementSettings.max < updatedCount) {
                    errors.add(InducementLimitExceeded(inducement.type, updatedCount, inducementSettings.max))
                }
                usedGold += inducement.getPrice(team)
                if (!inducement.isAvailableToTeam(team)) {
                    errors.add(
                        InducementNotAvailableToTeam(inducement.type, inducementSettings, team)
                    )
                }
            }
            if (usedGold > maxLimit) {
                errors.add(TooMuchGoldUsed(usedGold, maxLimit))
            }
            return errors
        }

        val state = team.game
        val currentNode = state.stack.currentNode()
        return when (currentNode) {
            is BuyInducements.HigherCtvBuyPurchaseInducements -> {
                isValid(team, min(team.treasury, state.rules.inducements.topDogTopUpLimitFromTreasury))
            }
            is BuyInducements.LowerCtvBuyPurchaseInducements -> {
                val maxGold = team.pettyCash + min(team.treasury, state.rules.inducements.underdogTopUpLimitFromTreasury)
                isValid(team, maxGold)
            }
            else -> emptyList()
        }
    }

}
