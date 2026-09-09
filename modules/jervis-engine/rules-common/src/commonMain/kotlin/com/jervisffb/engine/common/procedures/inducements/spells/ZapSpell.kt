package com.jervisffb.engine.common.procedures.inducements.spells

import com.jervisffb.engine.model.Player
import com.jervisffb.engine.model.inducements.wizard.Spell
import com.jervisffb.engine.rules.common.roster.Position

abstract class ZapSpell: Spell {
    /**
     * The position [player] is turned into if this spell hits them.
     */
    abstract fun getFrogPositionData(player: Player): Position
}
