package com.jervisffb.engine.common.procedures.inducements.spells

import com.jervisffb.engine.actions.D6Result
import com.jervisffb.engine.commands.Command
import com.jervisffb.engine.common.context.ResolveInducementEffectsContext
import com.jervisffb.engine.common.procedures.dicerolls.D6WithTeamRerollProcedure
import com.jervisffb.engine.common.procedures.dicerolls.TeamRerollData
import com.jervisffb.engine.fsm.Node
import com.jervisffb.engine.model.Game
import com.jervisffb.engine.model.Team
import com.jervisffb.engine.model.context.ProcedureContext
import com.jervisffb.engine.model.context.assertContext
import com.jervisffb.engine.model.context.getContext
import com.jervisffb.engine.rules.DiceRollType
import com.jervisffb.engine.rules.Rules
import com.jervisffb.engine.rules.common.procedures.D6DieRoll
import kotlinx.collections.immutable.toPersistentList

/**
 * Control rolling the die for the Fireball spell.
 *
 * See page 149 in the BB2025 rulebook.
 */
object FireballRoll: D6WithTeamRerollProcedure() {
    val FIREBALL_HIT = 4
    override val rollType: DiceRollType = DiceRollType.FIREBALL
    override val initialNode: Node get() = RollDie
    override fun onEnterRollProcedure(state: Game, rules: Rules): Command? = null
    override fun onExitRollProcedure(state: Game, rules: Rules): Command? = null
    override fun isValid(state: Game, rules: Rules) = state.assertContext<FireBallContext>()
    override fun getActionOwner(state: Game): Team = state.getContext<ResolveInducementEffectsContext>().team

    override val RollDie = object : AbstractRollDie() {
        override fun updateContext(state: Game, rules: Rules, d6: D6Result): ProcedureContext {
            val context = state.getContext<FireBallContext>()
            val player = context.potentialPlayers.last()
            val isHit = isSuccess(d6)
            val missingPlayers = context.potentialPlayers.dropLast(1).toPersistentList()
            return context.copy(
                potentialPlayers = missingPlayers,
                rolls = context.rolls.add(FireBallContext.Hit(player, D6DieRoll.create(d6), isHit))
            )
        }
    }

    override val ChooseReRollSource = object : AbstractChooseRerollSource() {
        override fun getRerollData(state: Game, rules: Rules): TeamRerollData {
            val inducementsContext = state.getContext<ResolveInducementEffectsContext>()
            val context = state.getContext<FireBallContext>()
            val roll = context.rolls.last()
            return TeamRerollData(inducementsContext.team, roll.roll, roll.isSuccess)
        }
    }

    override val ReRollDie = object : AbstractReRollDie() {
        override fun updateContext(state: Game, rules: Rules, d6: D6Result): ProcedureContext {
            val context = state.getContext<FireBallContext>()
            val updatedRoll = context.rolls.last().let {
                it.copy(
                    roll = it.roll.copyReroll(
                        rerollSource = state.getRerollContext().source,
                        rerolledResult = d6,
                    ),
                    isSuccess = isSuccess(d6)
                )
            }
            return context.copy(
                rolls = context.rolls.dropLast(1).toPersistentList().add(updatedRoll)
            )
        }
    }

    private fun isSuccess(roll: D6Result): Boolean {
        val target = FIREBALL_HIT
        return roll.value >= target
    }
}
