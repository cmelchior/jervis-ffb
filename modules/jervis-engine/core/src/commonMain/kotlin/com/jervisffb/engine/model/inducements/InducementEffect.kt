package com.jervisffb.engine.model.inducements

import com.jervisffb.engine.fsm.Procedure
import com.jervisffb.engine.model.InducementEffectId

/**
 * Interface describing inducement effects like spells and special play cards;
 * that are optional to use during a game, i.e., they must be selected by the player.
 */
interface InducementEffect {
    val id: InducementEffectId
    val name: String // Name of the effect
    var used: Boolean // Whether it has been used or not
    val triggers: List<Timing> // What conditions trigger this effect
    val procedure: Procedure // The procedure that handles the effect being selected
}
