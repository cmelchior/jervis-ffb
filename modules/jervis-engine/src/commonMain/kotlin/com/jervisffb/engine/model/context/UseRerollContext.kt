package com.jervisffb.engine.model.context

import com.jervisffb.engine.model.Player
import com.jervisffb.engine.model.Team
import com.jervisffb.engine.rules.DiceRollType
import com.jervisffb.engine.rules.common.rerolls.DiceRerollOption
import com.jervisffb.engine.rules.common.skills.RerollSource

/**
 * Wrap the choice of the reroll type used and whether it can be used to
 * reroll the current dice.
 *
 * Some reroll types, like Pro, count as being used but might fail, so they
 * mark the dice as being re-rolled, without actually doing it.
 */
data class UseRerollContext(
    // Type of Dice Roll. Used to do an easy first-pass filter for reroll options.
    val type: DiceRollType,
    // All re-rolls are associated with a team. `source` could also provide this,
    // but in some cases, we do not know the exact source until later, so we also
    // need a reference here. Any team reference on `RerollSource, should be the
    // same as this one.
    val team: Team,
    // If a roll is being made by a player, and the player skills, traits or
    // special rules might impact the roll.
    val player: Player? = null,
    // Reference to the selected Skill or Team Reroll variant (or other effect)
    // that can supply the reroll option.
    val source: RerollSource? = null,
    // If `null` after `UseRerollSource` has been run, it means that no rerolls
    // are allowed after all. E.g. failing a Mascot or Pro roll.
    val selectedRerollOption: DiceRerollOption? = null,
    // Regardless of what happened to the reroll. Rerolling the selected dice is
    // allowed.
    val rerollAllowed: Boolean = false,
) : ProcedureContext {
    constructor(type: DiceRollType, player: Player) : this(type, player.team, player)
    constructor(type: DiceRollType, player: Player, source: RerollSource) : this(type, player.team, player)
}
