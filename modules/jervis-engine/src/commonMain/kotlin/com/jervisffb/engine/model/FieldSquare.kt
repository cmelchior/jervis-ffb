package com.jervisffb.engine.model

import com.jervisffb.engine.model.field.Portal
import com.jervisffb.engine.model.field.SquareEdge
import com.jervisffb.engine.model.field.SquareEnvironmentalFeatureType
import com.jervisffb.engine.model.field.SquareSubstrateType
import com.jervisffb.engine.model.field.SquareType
import com.jervisffb.engine.model.locations.FieldCoordinate

/**
 * This class represents a square on the [Field].
 *
 * *Developer's Commentary:*
 * Squares can contain quite a lot of things, all from UI representation to special rules to
 * environmental features. Capturing all of these, while making the model flexible is quite
 * difficult. So the current approach is just a first step towards it. Most likely edge-cases
 * will show themselves once proper Dungeon Bowl support is added.
 *
 * To capture all possible permutations, a field is built up of multiple layers. They consist
 * of the following properties:
 *
 * 1. [type] contains the high-level description, like what room it is or if it
 *    is a normal pitch square.
 * 2. [substrate] describes the surface being played on, like grass or rock.
 * 3. [environmentalFeature] describe any special "thing" that is on the square. This
 *    is most commonly statues and the like
 * 4. [impassable] is its own property and controls if players are allowed to move through the
 *    square. Players can temporarily be on a square that is impassable, but it should only be
 *    a temporary state. Like when a player is pushed into a Fiery Chasm.
 * 5. [blockLineOfSight] whether a square blocks LoS. This currently has no effect on the rules,
 *    but we track it for completeness and just in case some extra rule is introduced in the future
 *    that might use it.
 * 6. [hasTrapdoor] Whether the square has a trapdoor. This is currently tracked separately
 *    because it is unclear if you could place trapdoors on things like a suspension bridge.
 * 7. [hasChest] Whether the square has a chest. It is currently just a boolean, but might need
 *    to be expanded to a proper class in the future.
 * 8. [portal] Whether the square has a portal or not.
 * 9. [edges] Describe the edges of the square. These are only relevant for Dungeon Bowl and
 *    Gutter Bowl where it is used to track walls and doors.
 *
 * Note, a lot of these properties are mutually exclusive, but it is up to the procedures
 * to ensure that these invariants are not broken. This should allow us to model a wider
 * variety of cases.
 */
class FieldSquare(
    val coordinates: FieldCoordinate
): FieldCoordinate by coordinates {
    constructor(x: Int, y: Int) : this(FieldCoordinate(x, y))
    var player: Player? = null
    // Having multiple balls in the same field should just be a temporary state
    // as the BB2020 Rules do not allow two balls in the same square. One will always bounce.
    var balls: MutableList<Ball> = mutableListOf()

    // Overall type of square.
    var type: SquareType = SquareType.STANDARD

    // How to interpret the edges of the square
    var edges: SquareEdge = SquareEdge()

    // The kind of surface this square has.
    var substrate: SquareSubstrateType = SquareSubstrateType.GRASS

    // Any special environmental feature.
    var environmentalFeature: SquareEnvironmentalFeatureType = SquareEnvironmentalFeatureType.NONE

    // If a square is impassable, it means that players are now allowed to walk through it.
    // Some effects might allow you to enter it, like being pushed into a Fiery Chasm, but
    // it should only be a temporary state while resolving the effect.
    var impassable: Boolean = false

    // *Developer's Commentary*
    // LoS is defined in Dungeon Ball, and some features like The Crypt, Teleports
    // and Chests are noted as not blocking line of sight. Blocking LoS doesn't have
    // any use in the rules though (for now). I assume the wording is there because
    // Line-of-Sight mattered for throwing at some point during early development but
    // was later removed. For now, we still track it, just in case some special play
    // card or Spike Magazine end up using it.
    var blockLineOfSight: Boolean = false

    // Whether the square has a trapdoor
    var hasTrapdoor: Boolean = false

    // Whether the square has a chest.
    var hasChest: Boolean = false

    // Whether the square has a portal or not
    var portal: Portal? = null

    // Is field unoccupied as per the definition on page 44 in the rulebook.
    fun isUnoccupied(): Boolean = (player == null)

    // Is field occupied as per the definition on page 44 in the rulebook.
    fun isOccupied(): Boolean = !isUnoccupied()
}
