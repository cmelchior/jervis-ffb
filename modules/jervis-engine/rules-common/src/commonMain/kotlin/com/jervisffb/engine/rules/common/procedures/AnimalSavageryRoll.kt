package com.jervisffb.engine.rules.common.procedures

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
import com.jervisffb.engine.rules.common.actions.PlayerStandardActionType
import com.jervisffb.engine.rules.common.procedures.actions.dicerolls.D6WithRerollProcedure
import com.jervisffb.engine.rules.common.procedures.actions.dicerolls.RerollData

object AnimalSavageryRoll: D6WithRerollProcedure() {
    override val rollType: DiceRollType = DiceRollType.ANIMAL_SAVAGERY
    override val initialNode: Node get() = RollDie
    override fun onEnterRollProcedure(state: Game, rules: Rules): Command? = null
    override fun onExitRollProcedure(state: Game, rules: Rules): Command? = null
    override fun getActionOwner(state: Game): Player = state.getContext<AnimalSavageryContext>().player
    override fun isValid(state: Game, rules: Rules) = state.assertContext<AnimalSavageryContext>()

    override val RollDie = object : AbstractRollDie() {
        override fun updateContext(state: Game, rules: Rules, d6: D6Result): ProcedureContext {
            val context = state.getContext<AnimalSavageryContext>()
            val activateContext = state.getContext<ActivatePlayerContext>()
            val isSuccess = calculateSuccess(activateContext, d6)
            return context.copy(
                roll = D6DieRoll.create(state, d6),
                isSuccess = isSuccess
            )
        }
    }

    override val ChooseReRollSource = object : AbstractChooseRerollSource() {
        override fun getRerollData(state: Game, rules: Rules): RerollData {
            val context = state.getContext<AnimalSavageryContext>()
            return RerollData(context.player, context.roll!!, context.isSuccess)
        }
    }

    override val ReRollDie = object : AbstractReRollDie() {
        override fun updateContext(state: Game, rules: Rules, d6: D6Result): ProcedureContext {
            val activateContext = state.getContext<ActivatePlayerContext>()
            val rollResultContext = state.getContext<AnimalSavageryContext>()
            return rollResultContext.copy(
                roll = rollResultContext.roll!!.copyReroll(
                    rerollSource = state.getRerollContext().source,
                    rerolledResult = d6,
                ),
                isSuccess = calculateSuccess(activateContext, d6)
            )
        }
    }

    private fun calculateSuccess(context: ActivatePlayerContext, d6: D6Result): Boolean {
        val modifier = when (context.declaredAction!!.type) {
            PlayerStandardActionType.BLOCK,
            PlayerStandardActionType.BLITZ -> 2
            else -> 0
        }
        val isSuccess = (d6.value + modifier >= 4)
        return isSuccess
    }
}
