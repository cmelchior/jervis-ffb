package com.jervisffb.engine.rules.bb2020.roster

import com.jervisffb.engine.model.RosterId
import com.jervisffb.engine.rules.common.roster.Roster
import com.jervisffb.engine.serialize.SpriteSource
import kotlinx.serialization.Serializable

@Serializable
data class BB2020Roster(
    override val id: RosterId,
    override val name: String,
    val tier: Int,
    override val numberOfRerolls: Int,
    override val rerollCost: Int,
    override val allowApothecary: Boolean,
    val specialRules: List<SpecialRules>,
    override val positions: List<BB2020Position>,
    override val rosterLogo: SpriteSource?
) : Roster
