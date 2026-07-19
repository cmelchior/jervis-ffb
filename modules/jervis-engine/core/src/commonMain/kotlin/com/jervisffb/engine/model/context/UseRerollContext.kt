package com.jervisffb.engine.model.context

import com.jervisffb.engine.model.Player
import com.jervisffb.engine.model.Team
import com.jervisffb.engine.rules.DiceRollType
import com.jervisffb.engine.rules.common.procedures.DieRoll
import com.jervisffb.engine.rules.common.skills.RerollSource

/**
 * Wrap the choice of the reroll type used and whether it can be used to
 * reroll the current dice. This context should _not_ track the actual reroll,
 * just the intent to do so.
 *
 * Some reroll types, like Pro, count as being used but might fail, preventing
 * the actual reroll. It is up to the reroll procedure to handle this case,
 */
data class UseRerollContext(
    // Type of Dice Roll. Used to do an easy first-pass filter for reroll options.
    val type: DiceRollType,
    // Dice pool available to reroll. The dice being rerolled are tracked in
    // `selectedRerollOption.dice`
    val originalRoll: List<DieRoll<*>>,
    // All re-rolls are associated with a team. `source` could also provide this,
    // but in some cases, we do not know the exact source until later, so we also
    // need a reference here. Any team reference on `RerollSource`, should be the
    // same as this one.
    val team: Team,
    // If a roll is being made by a player, and the player skills, traits or
    // special rules might impact the roll.
    val player: Player? = null,
    // Reference to the selected Skill or Team Reroll variant (or other effect)
    // that can supply the reroll option.
    val source: RerollSource? = null,
    // Which dice can actually be rerolled. This might be a subset of `roll`.
    // Must be set after `UseRerollSource` has been run. If the re-roll failed
    // to work (e.g., a failed Pro), this must still be set to indicate which
    // dice was selected for the reroll as they still count as being rerolled.
    val rerollDice: List<DieRoll<*>>? = null,
    // Regardless of what happened to the reroll. Rerolling the selected dice is
    // allowed.
    val rerollAllowed: Boolean = false,
    // The reroll was aborted by the user, the original roll stands, and the
    // reroll is not marked as used after all.
    val rerollAborted: Boolean = false,
) : ProcedureContext {
    constructor(type: DiceRollType, roll: List<DieRoll<*>>, player: Player) : this(type, roll, player.team, player)
    constructor(type: DiceRollType, roll: List<DieRoll<*>>, player: Player, source: RerollSource) : this(type, roll, player.team, player)
}
