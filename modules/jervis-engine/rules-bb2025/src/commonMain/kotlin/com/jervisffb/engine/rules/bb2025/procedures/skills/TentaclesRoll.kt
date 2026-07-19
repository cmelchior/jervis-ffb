package com.jervisffb.engine.rules.bb2025.procedures.skills

import com.jervisffb.engine.actions.D6Result
import com.jervisffb.engine.commands.Command
import com.jervisffb.engine.fsm.Node
import com.jervisffb.engine.model.Game
import com.jervisffb.engine.model.Player
import com.jervisffb.engine.model.context.ProcedureContext
import com.jervisffb.engine.model.context.TentaclesRollContext
import com.jervisffb.engine.model.context.assertContext
import com.jervisffb.engine.model.context.getContext
import com.jervisffb.engine.model.modifiers.DefensiveTentaclesModifier
import com.jervisffb.engine.model.modifiers.DiceModifier
import com.jervisffb.engine.model.modifiers.OffensiveTentaclesModifier
import com.jervisffb.engine.rules.DiceRollType
import com.jervisffb.engine.rules.Rules
import com.jervisffb.engine.rules.common.procedures.D6DieRoll
import com.jervisffb.engine.rules.common.procedures.actions.dicerolls.D6WithRerollProcedure
import com.jervisffb.engine.rules.common.procedures.actions.dicerolls.RerollData
import com.jervisffb.engine.utils.INVALID_GAME_STATE
import com.jervisffb.engine.utils.sum

/**
 * Procedure for handling a Tentacles roll.
 *
 * The result is stored in [TentaclesRollContext]] and it is up to the caller of the procedure to
 * choose the appropriate action depending on the outcome.
 */
object TentaclesRoll : D6WithRerollProcedure() {
    override val rollType: DiceRollType = DiceRollType.TENTACLES
    override val initialNode: Node get() = RollDie
    override fun onEnterRollProcedure(state: Game, rules: Rules): Command? = null
    override fun onExitRollProcedure(state: Game, rules: Rules): Command? = null
    override fun isValid(state: Game, rules: Rules) = state.assertContext<TentaclesRollContext>()
    override fun getActionOwner(state: Game): Player = state.getContext<TentaclesRollContext>().movingPlayer

    override val RollDie = object: AbstractRollDie() {
        override fun updateContext(state: Game, rules: Rules, d6: D6Result): ProcedureContext {
            val context = state.getContext<TentaclesRollContext>()
            val modifiers = listOf(
                OffensiveTentaclesModifier(context.tentaclePlayer!!),
                DefensiveTentaclesModifier(context.movingPlayer)
            )
            return context.copy(
                roll = D6DieRoll.create(state, d6),
                modifiers = modifiers,
                isSuccess = isTentaclesSuccessful(d6, modifiers),
            )
        }
    }

    override val ChooseReRollSource = object : AbstractChooseRerollSource() {
        override fun getRerollData(state: Game, rules: Rules): RerollData {
            val context = state.getContext<TentaclesRollContext>()
            val tentaclePlayer = context.tentaclePlayer ?: INVALID_GAME_STATE("Missing tentacle player: $context")
            return RerollData(
                player = tentaclePlayer,
                roll = context.roll!!,
                isSuccess = context.isSuccess
            )
        }
    }

    override val ReRollDie = object : AbstractReRollDie() {
        override fun updateContext(state: Game, rules: Rules, d6: D6Result): ProcedureContext {
            val context = state.getContext<TentaclesRollContext>()
            return context.copy(
                roll = context.roll!!.copyReroll(
                    rerollSource = state.getRerollContext().source,
                    rerolledResult = d6,
                ),
                isSuccess = isTentaclesSuccessful(d6, context.modifiers)
            )
        }
    }

    private fun isTentaclesSuccessful(roll: D6Result, modifiers: List<DiceModifier>): Boolean {
        val target = 6
        return when (roll.value) {
            1 -> false
            in 2..5 -> (target <= roll.value + modifiers.sum())
            6 -> true
            else -> error("Invalid value: ${roll.value}")
        }
    }
}
