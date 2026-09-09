package com.jervisffb.engine.model

import com.jervisffb.engine.model.locations.Dogout
import com.jervisffb.engine.model.locations.GiantLocation
import com.jervisffb.engine.model.locations.Location
import com.jervisffb.engine.model.locations.PitchCoordinate
import com.jervisffb.engine.model.modifiers.PlayerStatusEffect
import com.jervisffb.engine.model.modifiers.PlayerStatusEffectType
import com.jervisffb.engine.model.modifiers.StatModifier
import com.jervisffb.engine.rules.Rules
import com.jervisffb.engine.rules.common.roster.PlayerSpecialRule
import com.jervisffb.engine.rules.common.roster.Position
import com.jervisffb.engine.rules.common.skills.Skill
import com.jervisffb.engine.rules.common.skills.SkillType
import com.jervisffb.engine.serialization.PlayerUiData
import com.jervisffb.engine.utils.INVALID_GAME_STATE
import com.jervisffb.engine.utils.LiveMergeList

interface Player {
    val id: PlayerId

    // The position the player is currently playing as. This is normally the position
    // they were hired into, but a temporary transformation, like the "Zap!" spell
    // turning the player into a Frog, swaps it out. See [transformation].
    var position: Position

    // Set for as long as the player is transformed into something else. It holds
    // everything needed to turn them back to normal again. See [PlayerTransformation].
    var transformation: PlayerTransformation?
    val isTransformed: Boolean
        get() = (transformation != null)

    var icon: PlayerUiData?
    val type: PlayerType
    var team: Team
    var location: Location

    // Shortcut for getting a players coordinates. Only works for players currently on the pitch
    // taking up a single square.
    val coordinates: PitchCoordinate

    // True from the player is selected as the thrown player until they finalize their landing
    // in an empty square or out of bounds.
    var isBeingThrown: Boolean
    var facing: PlayerFacing
    var state: PlayerState
    var intermediateState: PlayerIntermediateState?
    val isActive: Boolean
    var available: Availability
    var stunnedThisTurn: Boolean?
    var hasTackleZones: Boolean
    var isStalling: Boolean
    var name: String
    var number: PlayerNo

    // When updating `baseMove` and `moveModifiers`, `move` must also be updated.
    // This requires knowledge about the rules so cannot be done in this class.
    var baseMove: Int
    val moveModifiers: List<StatModifier>
    var move: Int

    // How many moves the player has left, before rushes are needed.
    // Rolling for a Rush will modify this number.
    var movesLeft: Int

    // How many rushes the player has left this turn
    var rushesLeft: Int

    // When updating `baseStrength` and `strengthModifiers`, `strength` must also be updated.
    // This requires knowledge about the rules so cannot be done in this class.
    var baseStrength: Int
    val strengthModifiers: List<StatModifier>
    var strength: Int

    // When updating `baseAgility` and `agilityModifiers`, `agility` must also be updated.
    // This requires knowledge about the rules so cannot be done in this class.
    var baseAgility: Int
    val agilityModifiers: List<StatModifier>
    var agility: Int

    // When updating `basePassing` and `passingModifiers`, `passing` must also be updated.
    // This requires knowledge about the rules so cannot be done in this class.
    var basePassing: Int?
    val passingModifiers: List<StatModifier>
    var passing: Int?

    // When updating `baseArmorValue` and `armourModifiers`, `armorValue` must also be updated.
    // This requires knowledge about the rules so cannot be done in this class.
    var baseArmorValue: Int
    val armourModifiers: List<StatModifier>
    var armorValue: Int

    // Return all stat modifiers across all stats
    val statModifiers: List<StatModifier>

    // Some effects are hard to put into other buckets, like a player that failed a Blood Lust roll
    // or a player that was added to the pitch through Spot The Sneak. In these cases, we might want
    // to mark the player somehow. This is done through a PlayerStatusEffect.
    val statusEffects: MutableList<PlayerStatusEffect>

    // Skills tracking
    val extraSkills: MutableList<Skill<*>>
    val positionSkills: MutableList<Skill<*>>
    val skills: List<Skill<*>>

    // Special Rules tracking
    val extraSpecialRules: MutableList<PlayerSpecialRule>
    val positionSpecialRules: MutableList<PlayerSpecialRule>
    val specialRules: List<PlayerSpecialRule>

    // Keywords tracking. These follow the position the player is currently in,
    // so they change if the player is transformed. See [PlayerTransformation].
    val keywords: MutableList<PlayerKeyword>
    var nigglingInjuries: Int
    var missNextGame: Boolean
    var starPlayerPoints: Int
    var level: PlayerLevel
    var cost: Int
    val ball: Ball?

    // Shortcut to avoid avoiding creating a SkillId.
    // Warning: This method should only be used for skills that do not have values
    // Shortcut to avoid avoiding creating a SkillId.
    // Warning: This method should only be used for skills that do not have values
    fun addSkill(skill: SkillType) {
        val skill = team.game.rules.createSkill(this, skill.id())
        extraSkills.add(skill)
    }

    fun addSkill(skill: SkillId) {
        val skill = team.game.rules.createSkill(this, skill)
        extraSkills.add(skill)
    }

    fun addSkill(skill: Skill<*>) {
        if (skill.player != this) {
            throw IllegalArgumentException("Skill $skill is not owned by ${this.id}: ${skill.player.id}")
        }
        extraSkills.add(skill)
    }

    fun removeSkill(skill: Skill<*>) {
        if (!extraSkills.remove(skill)) {
            if (!positionSkills.remove(skill)) {
                INVALID_GAME_STATE("Could not remove skill: ${skill.name}")
            }
        }
    }

    fun hasBall(): Boolean = (ball != null)

    fun getSkill(type: SkillType): Skill<*> {
        return skills.firstOrNull { it.type == type } ?: INVALID_GAME_STATE("Player does not have the skill $type")
    }

    fun getSkillOrNull(type: SkillType): Skill<*>? {
        return skills.firstOrNull { it.type == type }
    }

    fun addStatModifier(modifier: StatModifier)
    fun removeStatModifier(modifier: StatModifier)

    /**
     * Change the [Position] the player is playing as, e.g. when the "Zap!"
     * spell turns them into a Frog. Positional skills are created from scratch,
     * so they start out unused.
     *
     * Go through [com.jervisffb.engine.commands.TransformPlayer] rather than
     * calling this directly, so the change can be undone again.
     */
    fun transformInto(position: Position) {
        val rules = team.game.rules
        setForm(
            position = position,
            icon = PlayerUiData(sprite = position.icon, portrait = position.portrait),
            baseMove = position.move,
            baseStrength = position.strength,
            baseAgility = position.agility,
            basePassing = position.passing,
            baseArmorValue = position.armorValue,
            positionSkills = position.skills.mapNotNull {
                // TODO For now, just ignore skills that are not supported
                when (rules.skillSettings.isSkillSupported(it.type)) {
                    true -> rules.createSkill(this, it)
                    false -> null
                }
            },
            positionSpecialRules = position.specialRules,
            keywords = position.keywords,
        )
    }

    /**
     * Put the player back into the form captured by [transformation]. This
     * reuses the original [Skill] instances, so whether those skills had
     * already been used is restored as well.
     *
     * Go through [com.jervisffb.engine.commands.EndPlayerTransformation] rather
     * than calling this directly, so the change can be undone again.
     */
    fun restoreFrom(transformation: PlayerTransformation) {
        setForm(
            position = transformation.position,
            icon = transformation.icon,
            baseMove = transformation.baseMove,
            baseStrength = transformation.baseStrength,
            baseAgility = transformation.baseAgility,
            basePassing = transformation.basePassing,
            baseArmorValue = transformation.baseArmorValue,
            positionSkills = transformation.positionSkills,
            positionSpecialRules = transformation.positionSpecialRules,
            keywords = transformation.keywords,
        )
    }

    /**
     * Replace everything the players [Position] contributes to them and
     * recalculate their stats.
     *
     * Note, this deliberately leaves temporary stat modifiers, temporary skills
     * and status effects alone; those belong to the player rather than to the
     * position they are currently playing as.
     */
    private fun setForm(
        position: Position,
        icon: PlayerUiData?,
        baseMove: Int,
        baseStrength: Int,
        baseAgility: Int,
        basePassing: Int?,
        baseArmorValue: Int,
        positionSkills: List<Skill<*>>,
        positionSpecialRules: List<PlayerSpecialRule>,
        keywords: List<PlayerKeyword>,
    ) {
        this.position = position
        this.icon = icon
        this.baseMove = baseMove
        this.baseStrength = baseStrength
        this.baseAgility = baseAgility
        this.basePassing = basePassing
        this.baseArmorValue = baseArmorValue
        this.positionSkills.clear()
        this.positionSkills.addAll(positionSkills)
        this.positionSpecialRules.clear()
        this.positionSpecialRules.addAll(positionSpecialRules)
        this.keywords.clear()
        this.keywords.addAll(keywords)
        val rules = team.game.rules
        StatModifier.Type.entries.forEach { rules.updatePlayerStat(this, it) }
    }

    fun addStatusEffect(effect: PlayerStatusEffect) {
        if (!statusEffects.add(effect)) {
            INVALID_GAME_STATE("Could not add status effect: ${effect.type}")
        }
    }

    fun removeStatusEffect(effect: PlayerStatusEffect) {
        if (!statusEffects.remove(effect)) {
            INVALID_GAME_STATE("Could not remove status effect: ${effect.type}")
        }
    }

    fun hasStatusEffect(effect: PlayerStatusEffectType): Boolean {
        return statusEffects.any { it.type == effect }
    }
}

class PlayerImpl(
    rules: Rules,
    override val id: PlayerId,
    override var position: Position,
    override var icon: PlayerUiData? = null,
    override val type: PlayerType
) : Player {
    override var transformation: PlayerTransformation? = null
    override lateinit var team: Team
    override var location: Location = Dogout

    // Shortcut for getting a players coordinates. Only works for players currently on the pitch
    // taking up a single square.
    override val coordinates: PitchCoordinate
        get() {
            return when (val playerLocation = location) {
                Dogout -> INVALID_GAME_STATE("Cannot ask for coordinates when player is in the Dogout")
                is PitchCoordinate -> playerLocation
                is GiantLocation -> INVALID_GAME_STATE("Cannot ask for coordinates for a giant player")
            }
        }

    // True from the player is selected as the thrown player until they finalize their landing
    // in an empty square or out of bounds.
    override var isBeingThrown: Boolean = false
    override var facing: PlayerFacing = PlayerFacing.UNKNOWN
    override var state: PlayerState = PlayerDogoutState.RESERVE
    override var intermediateState: PlayerIntermediateState? = null
    override val isActive: Boolean get() = (team.game.activePlayer == this)
    override var available: Availability = Availability.AVAILABLE
    override var stunnedThisTurn: Boolean? = null
    override var hasTackleZones: Boolean = true
    override var isStalling: Boolean = false
    override var name: String = ""
    override var number: PlayerNo = PlayerNo(0)
    // When updating `baseMove` and `moveModifiers`, `move` must also be updated.
    // This requires knowledge about the rules so cannot be done in this class.
    override var baseMove: Int = position.move
    override val moveModifiers = mutableListOf<StatModifier>()
    override var move: Int = position.move
    // How many moves the player has left, before rushes are needed.
    // Rolling for a Rush will modify this number.
    override var movesLeft: Int = 0
    // How many rushes the player has left this turn
    override var rushesLeft: Int = 0
    // When updating `baseStrength` and `strengthModifiers`, `strength` must also be updated.
    // This requires knowledge about the rules so cannot be done in this class.
    override var baseStrength: Int = position.strength
    override val strengthModifiers: List<StatModifier>
        field = mutableListOf()
    override var strength: Int = position.strength
    // When updating `baseAgility` and `agilityModifiers`, `agility` must also be updated.
    // This requires knowledge about the rules so cannot be done in this class.
    override var baseAgility: Int = position.agility
    override val agilityModifiers: List<StatModifier>
        field = mutableListOf()
    override var agility: Int = position.agility
    // When updating `basePassing` and `passingModifiers`, `passing` must also be updated.
    // This requires knowledge about the rules so cannot be done in this class.
    override var basePassing: Int? = position.passing
    override val passingModifiers: List<StatModifier>
        field = mutableListOf()
    override var passing: Int? = position.passing
    // When updating `baseArmorValue` and `armourModifiers`, `armorValue` must also be updated.
    // This requires knowledge about the rules so cannot be done in this class.
    override var baseArmorValue: Int = position.armorValue
    override val armourModifiers: List<StatModifier>
        field = mutableListOf()
    override var armorValue: Int = position.armorValue

    override val statModifiers: List<StatModifier>
        get() {
            return buildList {
                addAll(this@PlayerImpl.moveModifiers)
                addAll(this@PlayerImpl.strengthModifiers)
                addAll(this@PlayerImpl.agilityModifiers)
                addAll(this@PlayerImpl.passingModifiers)
                addAll(this@PlayerImpl.armourModifiers)
            }
        }

    // Some effects are hard to put into other buckets, like a player that failed a Blood Lust roll
    // or a player that was added to the pitch through Spot The Sneak. In these cases, we might want
    // to mark the player somehow. This is done through a PlayerStatusEffect.
    override val statusEffects: MutableList<PlayerStatusEffect> = mutableListOf()

    // Skills tracking
    override val extraSkills = mutableListOf<Skill<*>>()
    override val positionSkills = position.skills.mapNotNull {
        // TODO For now, just ignore skills that are not supported
        if (rules.skillSettings.isSkillSupported(it.type)) {
            rules.createSkill(this, it)
        } else {
            null
        }
    }.toMutableList()
    override val skills: List<Skill<*>> = LiveMergeList(extraSkills, positionSkills)

    // Special Rules tracking
    override val extraSpecialRules = mutableListOf<PlayerSpecialRule>()
    override val positionSpecialRules: MutableList<PlayerSpecialRule> = position.specialRules.toMutableList()
    override val specialRules: List<PlayerSpecialRule> = LiveMergeList(extraSpecialRules, positionSpecialRules)

    // Keywords tracking
    override val keywords: MutableList<PlayerKeyword> = position.keywords.toMutableList()

    override var nigglingInjuries: Int = 0
    override var missNextGame: Boolean = false
    override var starPlayerPoints: Int = 0
    override var level: PlayerLevel = PlayerLevel.ROOKIE
    override var cost: Int = 0

    override val ball: Ball?
        get() = team.game.balls.firstOrNull { it.carriedBy == this }

    override fun addStatModifier(modifier: StatModifier) {
        when (modifier.type) {
            StatModifier.Type.AV -> armourModifiers.add(modifier)
            StatModifier.Type.MA -> moveModifiers.add(modifier)
            StatModifier.Type.PA -> passingModifiers.add(modifier)
            StatModifier.Type.AG -> agilityModifiers.add(modifier)
            StatModifier.Type.ST -> strengthModifiers.add(modifier)
        }
    }

    override fun removeStatModifier(modifier: StatModifier) {
        // TODO We should start search from the end of array
        // It doesn't matter much, but will ensure that the list
        // stays more consistent across Do/Undo
        val success = when (modifier.type) {
            StatModifier.Type.AV -> armourModifiers.remove(modifier)
            StatModifier.Type.MA -> moveModifiers.remove(modifier)
            StatModifier.Type.PA -> passingModifiers.remove(modifier)
            StatModifier.Type.AG -> agilityModifiers.remove(modifier)
            StatModifier.Type.ST -> strengthModifiers.remove(modifier)
        }
        if (!success) {
            INVALID_GAME_STATE("Could not remove $modifier from $name")
        }
    }
}

// Functional constructor for players
fun Player(
    rules: Rules,
    id: PlayerId,
    position: Position,
    icon: PlayerUiData? = null,
    type: PlayerType
) = PlayerImpl(rules, id, position, icon, type)

fun Player.isOnHomeTeam(): Boolean {
    return this.team.isHomeTeam()
}

fun Player.isOnAwayTeam(): Boolean {
    return this.team.isAwayTeam()
}

fun Player.hasSkill(type: SkillType): Boolean = this.skills.any { it.type == type }

fun Player.isSkillAvailable(type: SkillType): Boolean = this.team.game.rules.isSkillAvailable(this, type)

/**
 * Only used this when you are 100% sure about the skill class, i.e. when it used by a single
 * ruleset.
 */
inline fun <reified T: Skill<*>> Player.getSkill(): T {
    return this.skills.first { it is T } as T
}

inline fun <reified T: Skill<*>> Player.getSkillOrNull(): T? {
    return this.skills.firstOrNull { it is T } as T?
}
