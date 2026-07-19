package com.jervisffb.engine.rules.bb2025.procedures.actions.throwteammate

import com.jervisffb.engine.actions.D6Result
import com.jervisffb.engine.commands.Command
import com.jervisffb.engine.fsm.Node
import com.jervisffb.engine.model.Game
import com.jervisffb.engine.model.Player
import com.jervisffb.engine.model.context.ActivatePlayerContext
import com.jervisffb.engine.model.context.ProcedureContext
import com.jervisffb.engine.model.context.assertContext
import com.jervisffb.engine.model.context.getContext
import com.jervisffb.engine.rules.DiceRollType
import com.jervisffb.engine.rules.Rules
import com.jervisffb.engine.rules.common.procedures.D6DieRoll
import com.jervisffb.engine.rules.common.procedures.actions.dicerolls.D6WithRerollProcedure
import com.jervisffb.engine.rules.common.procedures.actions.dicerolls.RerollData

object AlwaysHungryRoll: D6WithRerollProcedure() {
    override val rollType: DiceRollType = DiceRollType.ALWAYS_HUNGRY
    override val initialNode: Node get() = RollDie
    override fun onEnterRollProcedure(state: Game, rules: Rules): Command? = null
    override fun onExitRollProcedure(state: Game, rules: Rules): Command? = null
    override fun getActionOwner(state: Game): Player = state.getContext<AlwaysHungryContext>().thrower
    override fun isValid(state: Game, rules: Rules) = state.assertContext<AlwaysHungryContext>()

    override val RollDie = object : AbstractRollDie() {
        override fun updateContext(state: Game, rules: Rules, d6: D6Result): ProcedureContext {
            val context = state.getContext<AlwaysHungryContext>()
            return context.copy(
                isHungryRoll = D6DieRoll.create(state, d6),
                isHungry = calculateIsHungry(d6)
            )
        }
    }

    override val ChooseReRollSource = object : AbstractChooseRerollSource() {
        override fun getRerollData(state: Game, rules: Rules): RerollData {
            val context = state.getContext<AlwaysHungryContext>()
            return RerollData(context.thrower, context.isHungryRoll!!, context.isHungry)
        }
    }

    override val ReRollDie = object : AbstractReRollDie() {
        override fun updateContext(state: Game, rules: Rules, d6: D6Result): ProcedureContext {
            val activateContext = state.getContext<ActivatePlayerContext>()
            val rollResultContext = state.getContext<AlwaysHungryContext>()
            return rollResultContext.copy(
                isHungryRoll = rollResultContext.isHungryRoll!!.copyReroll(
                    rerollSource = state.getRerollContext().source,
                    rerolledResult = d6,
                ),
                isHungry = calculateIsHungry(d6)
            )
        }
    }

    private fun calculateIsHungry(d6: D6Result): Boolean {
        val target = 2
        val isNotHungry = (d6.value >= target)
        return !isNotHungry
    }
}
