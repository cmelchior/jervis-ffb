package com.jervisffb.engine.rules.bb2020.procedures.actions.block

import com.jervisffb.engine.fsm.Procedure

/**
 * Interface describing the phases for doing a Block action (or a special action
 * that replaces a block).
 *
 * For normal blocks these steps are just run in sequence, but for players using
 * Multiple Block, these events will run in parallel, which means we need to switch
 * context between each phase.
 */
interface BlockActionProcedure {
    fun calculateModifiers(): Procedure
    fun rollDice(): Procedure
    fun rerollDice(): Procedure
    fun selectResult(): Procedure
    fun resolveResult(): Procedure
}
