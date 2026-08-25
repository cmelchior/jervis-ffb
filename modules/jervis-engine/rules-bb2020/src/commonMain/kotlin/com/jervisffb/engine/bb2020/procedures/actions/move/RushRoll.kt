package com.jervisffb.engine.bb2020.procedures.actions.move

import com.jervisffb.engine.actions.D6Result
import com.jervisffb.engine.bb2020.tables.PrayerToNuffleTableResult2020
import com.jervisffb.engine.commands.Command
import com.jervisffb.engine.commands.context.UpdateContext
import com.jervisffb.engine.common.modifiers.RushModifier
import com.jervisffb.engine.common.procedures.dicerolls.D6WithRerollProcedure
import com.jervisffb.engine.common.procedures.dicerolls.RerollData
import com.jervisffb.engine.fsm.Node
import com.jervisffb.engine.model.Game
import com.jervisffb.engine.model.Player
import com.jervisffb.engine.model.context.ProcedureContext
import com.jervisffb.engine.model.context.RushRollContext
import com.jervisffb.engine.model.context.assertContext
import com.jervisffb.engine.model.context.getContext
import com.jervisffb.engine.model.modifiers.DiceModifier
import com.jervisffb.engine.rules.DiceRollType
import com.jervisffb.engine.rules.Rules
import com.jervisffb.engine.rules.common.procedures.D6DieRoll
import com.jervisffb.engine.rules.common.tables.Weather
import com.jervisffb.engine.utils.sum

/**
 * Handle a player rushing a single square.
 *
 * See page 44 in the BB2020 rulebook.
 *
 * This procedure is only responsible for the actual dice roll. The parent
 * procedure must handle the result of the roll, it is not handled here.
 */
object RushRoll: D6WithRerollProcedure() {
    override val rollType: DiceRollType = DiceRollType.RUSH
    override val initialNode: Node get() = RollDie

    override fun onEnterRollProcedure(state: Game, rules: Rules): Command? {
        val modifiers = mutableListOf<DiceModifier>()
        if (state.weather == Weather.BLIZZARD) {
            modifiers.add(RushModifier.BLIZZARD)
        }
        if (state.homeTeam.activePrayersToNuffle.contains(PrayerToNuffleTableResult2020.MOLES_UNDER_THE_PITCH)) {
            modifiers.add(RushModifier.MOLES_UNDER_THE_PITCH_HOME)
        }
        if (state.awayTeam.activePrayersToNuffle.contains(PrayerToNuffleTableResult2020.MOLES_UNDER_THE_PITCH)) {
            modifiers.add(RushModifier.MOLES_UNDER_THE_PITCH_AWAY)
        }
        return if (modifiers.isNotEmpty()) {
            val rushContext = state.getContext<RushRollContext>()
            UpdateContext(rushContext.copyAndAddModifier(*modifiers.toTypedArray()))
        } else {
            null
        }
    }

    override fun onExitRollProcedure(state: Game, rules: Rules): Command? = null
    override fun isValid(state: Game, rules: Rules) = state.assertContext<RushRollContext>()
    override fun getActionOwner(state: Game): Player = state.getContext<RushRollContext>().player

    override val RollDie = object: AbstractRollDie() {
        override fun updateContext(state: Game, rules: Rules, d6: D6Result): ProcedureContext {
            val context = state.getContext<RushRollContext>()
            return context.copy(
                roll = D6DieRoll.create(d6),
                isSuccess = isRushSuccess(d6, context.modifiers),
            )
        }
    }

    override val ChooseReRollSource = object : AbstractChooseRerollSource() {
        override fun getRerollData(state: Game, rules: Rules): RerollData {
            val context = state.getContext<RushRollContext>()
            return RerollData(context.player, context.roll!!, context.isSuccess)
        }
    }

    override val ReRollDie = object : AbstractReRollDie() {
        override fun updateContext(state: Game, rules: Rules, d6: D6Result): ProcedureContext {
            val rushContext = state.getContext<RushRollContext>()
            return rushContext.copy(
                roll = rushContext.roll!!.copyReroll(
                    rerollSource = state.getRerollContext().source,
                    rerolledResult = d6,
                ),
                isSuccess = isRushSuccess(d6, rushContext.modifiers),
            )
        }
    }

    private fun isRushSuccess(d6: D6Result, modifiers: List<DiceModifier>): Boolean {
        return d6.value + modifiers.sum() >= 2
    }
}
