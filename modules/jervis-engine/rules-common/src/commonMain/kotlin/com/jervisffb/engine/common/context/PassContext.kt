package com.jervisffb.engine.common.context

import com.jervisffb.engine.common.procedures.actions.pass.PassingType
import com.jervisffb.engine.model.Player
import com.jervisffb.engine.model.context.PassingInterferenceContext
import com.jervisffb.engine.model.context.ProcedureContext
import com.jervisffb.engine.model.locations.PitchCoordinate
import com.jervisffb.engine.model.modifiers.DiceModifier
import com.jervisffb.engine.rules.common.actions.PassType
import com.jervisffb.engine.rules.common.procedures.D6DieRoll
import com.jervisffb.engine.rules.common.tables.Range
import kotlinx.collections.immutable.PersistentList
import kotlinx.collections.immutable.persistentListOf

data class PassContext(
    val thrower: Player,
    val type: PassType = PassType.STANDARD,
    val hasMoved: Boolean = false,
    // Target of the Pass in the current step. This means it will be updated when the ball scatters or deviates, but
    // not bounces after it lands.
    val target: PitchCoordinate? = null,
    val range: Range? = null,
    val useNervesOfSteel: Boolean = false,
    val passingRoll: D6DieRoll? = null,
    val passingModifiers: PersistentList<DiceModifier> = persistentListOf(),
    val passingResult: PassingType? = null,
    val useSafePass: Boolean = false,
    val runInterference: Player? = null,
    // Used in BB2020
    val passingInterference: PassingInterferenceContext? = null,
    // Used in BB2025
    val intercept: InterceptionContext? = null,
) : ProcedureContext {
    fun copyAndAdd(passingModifier: DiceModifier): PassContext = this.copy(
        passingModifiers = passingModifiers.add(passingModifier)
    )
    val hasThrown: Boolean
        get() = (range != null)
}
