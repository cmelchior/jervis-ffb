package com.jervisffb.engine.bb2025.procedures.actions.block.singleblock

import com.jervisffb.engine.actions.DBlockResult
import com.jervisffb.engine.actions.Dice
import com.jervisffb.engine.actions.GameAction
import com.jervisffb.engine.actions.GameActionDescriptor
import com.jervisffb.engine.actions.RollDice
import com.jervisffb.engine.commands.Command
import com.jervisffb.engine.commands.compositeCommandOf
import com.jervisffb.engine.commands.context.UpdateContext
import com.jervisffb.engine.commands.fsm.ExitProcedure
import com.jervisffb.engine.commands.probabiliy.AddChanceObservation
import com.jervisffb.engine.common.context.BlockContext
import com.jervisffb.engine.common.procedures.dicerolls.chanceScope
import com.jervisffb.engine.common.procedures.dicerolls.createChanceDiceResults
import com.jervisffb.engine.common.reports.ReportDiceRoll
import com.jervisffb.engine.fsm.ActionNode
import com.jervisffb.engine.fsm.Node
import com.jervisffb.engine.fsm.Procedure
import com.jervisffb.engine.fsm.castDiceRollList
import com.jervisffb.engine.model.Game
import com.jervisffb.engine.model.Team
import com.jervisffb.engine.model.context.UseRerollContext
import com.jervisffb.engine.model.context.assertContext
import com.jervisffb.engine.model.context.getContext
import com.jervisffb.engine.rules.DiceRollType
import com.jervisffb.engine.rules.Rules
import com.jervisffb.engine.rules.common.procedures.BlockDieRoll
import com.jervisffb.engine.statistics.probability.ChanceObservation
import com.jervisffb.engine.statistics.probability.ChanceObservationHandler
import kotlinx.collections.immutable.toPersistentList
import kotlin.math.absoluteValue

/**
 * Roll block dice for the first time.
 *
 * @see [com.jervisffb.rules.bb2020.procedures.actions.block.MultipleBlockAction]
 * @see [com.jervisffb.rules.bb2020.procedures.actions.block.StandardBlockStep]
 */
object SingleStandardBlockRollDice: Procedure(), ChanceObservationHandler {
    override val initialNode: Node = RollDice
    override fun onEnterProcedure(state: Game, rules: Rules): Command? = null
    override fun onExitProcedure(state: Game, rules: Rules): Command? = null
    override fun isValid(state: Game, rules: Rules) {
        state.assertContext<BlockContext>()
        state.assertContext<UseRerollContext>()
    }

    object RollDice : ActionNode() {
        override fun actionOwner(state: Game, rules: Rules): Team = state.getContext<BlockContext>().attacker.team
        override fun getAvailableActions(state: Game, rules: Rules): List<GameActionDescriptor> {
            val noOfDice = state.getContext<BlockContext>().calculateNoOfBlockDice().absoluteValue
            return listOf(RollDice(List(noOfDice) { Dice.BLOCK }))
        }

        override fun applyAction(action: GameAction, state: Game, rules: Rules): Command {
            return castDiceRollList<DBlockResult>(action) { it: List<DBlockResult> ->
                val roll =
                    it.mapIndexed { index, dieRoll: DBlockResult ->
                        BlockDieRoll.create(dieRoll, index)
                    }
                val rerollContext = state.getRerollContext()
                val observation = if (state.collectChanceData) {
                    val sequence = state.chanceObservationSequence
                    ChanceObservation.DiceRoll(
                        index = sequence,
                        rollType = DiceRollType.BLOCK,
                        teamId = state.getContext<BlockContext>().attacker.team.id,
                        playerId = state.getContext<BlockContext>().attacker.id,
                        dice = createChanceDiceResults(sequence, roll.map { die -> die.id to die.result }),
                        scope = chanceScope(state, state.getContext<BlockContext>().attacker),
                    )
                } else {
                    null
                }
                return compositeCommandOf(
                    ReportDiceRoll(roll),
                    UpdateContext(
                        rerollContext.copy(
                            originalRoll = roll,
                            chanceRollIndex = observation?.index,
                            chanceObservations = observation?.let { listOf(it) }.orEmpty().toPersistentList(),
                        ),
                    ),
                    UpdateContext(state.getContext<BlockContext>().copy(roll = roll)),
                    observation?.let(::AddChanceObservation),
                    ExitProcedure(),
                )
            }
        }
    }

}
