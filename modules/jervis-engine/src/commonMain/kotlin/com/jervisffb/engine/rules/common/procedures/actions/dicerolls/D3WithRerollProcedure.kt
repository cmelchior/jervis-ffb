@file:Suppress("PropertyName")

package com.jervisffb.engine.rules.common.procedures.actions.dicerolls

import com.jervisffb.engine.actions.Continue
import com.jervisffb.engine.actions.ContinueWhenReady
import com.jervisffb.engine.actions.D3Result
import com.jervisffb.engine.actions.Dice
import com.jervisffb.engine.actions.GameAction
import com.jervisffb.engine.actions.GameActionDescriptor
import com.jervisffb.engine.actions.NoRerollSelected
import com.jervisffb.engine.actions.RerollOptionSelected
import com.jervisffb.engine.actions.RollDice
import com.jervisffb.engine.actions.SelectNoReroll
import com.jervisffb.engine.commands.Command
import com.jervisffb.engine.commands.compositeCommandOf
import com.jervisffb.engine.commands.context.AddContext
import com.jervisffb.engine.commands.context.RemoveContext
import com.jervisffb.engine.commands.context.UpdateContext
import com.jervisffb.engine.commands.fsm.ExitProcedure
import com.jervisffb.engine.commands.fsm.GotoNode
import com.jervisffb.engine.fsm.ActionNode
import com.jervisffb.engine.fsm.Node
import com.jervisffb.engine.fsm.ParentNode
import com.jervisffb.engine.fsm.Procedure
import com.jervisffb.engine.fsm.castDiceRoll
import com.jervisffb.engine.model.Game
import com.jervisffb.engine.model.Team
import com.jervisffb.engine.model.context.ProcedureContext
import com.jervisffb.engine.model.context.UseRerollContext
import com.jervisffb.engine.model.context.getContext
import com.jervisffb.engine.reports.ReportDiceRoll
import com.jervisffb.engine.reports.ReportRerollUsed
import com.jervisffb.engine.rules.DiceRollType
import com.jervisffb.engine.rules.Rules
import com.jervisffb.engine.utils.INVALID_ACTION
import com.jervisffb.engine.utils.INVALID_GAME_STATE
import com.jervisffb.engine.utils.calculateAvailableRerollsFor

/**
 * Shared logic for Procedures that are handling a single D3 roll with
 * re-roll options.
 *
 * TODO Consolidate this with [D6WithRerollProcedure]
 */
abstract class D3WithRerollProcedure: Procedure() {
    // Roll initial dice
    abstract val RollDie: ActionNode
    // Select re-roll source or no reroll to keep result
    abstract val ChooseReRollSource: ActionNode
    // Use and Choose any potential reroll type.
    // Implementation Note: Use `lazy` to work around initialization order issues.
    open val UseRerollSource: ParentNode by lazy { CommonUseRerollSource(ReRollDie) }
    // Use the reroll and set the dice that can be rerolled
    // Reroll the dice set by `UseRerollSource`
    abstract val ReRollDie: ActionNode

    abstract val rollType: DiceRollType
    abstract fun getActionOwner(state: Game): Team

    // Roll specific "Enter" method
    abstract fun onEnterRollProcedure(state: Game, rules: Rules): Command?
    // Roll specific "Exit" method
    abstract fun onExitRollProcedure(state: Game, rules: Rules): Command?

    final override fun onEnterProcedure(state: Game, rules: Rules): Command {
        val rollContextCommands = onEnterRollProcedure(state, rules)
        val rerollContextCommand = AddContext(UseRerollContext(type = rollType))
        return compositeCommandOf(
            rollContextCommands,
            rerollContextCommand
        )
    }

    final override fun onExitProcedure(state: Game, rules: Rules): Command {
        val rollContextCommands = onExitRollProcedure(state, rules)
        val rerollContext = state.getContext<UseRerollContext>()
        if (rerollContext.type != rollType) {
            INVALID_GAME_STATE("UseRerollContext's are in an inconsistent state. Received: $rerollContext")
        }
        return compositeCommandOf(
            RemoveContext(rerollContext),
            rollContextCommands
        )
    }

    // Shared logic for rolling the initial D3
    abstract inner class AbstractRollDie: ActionNode() {
        override fun name(): String = "RollDie"
        // Update specific Roll Context with the roll data
        abstract fun updateContext(state: Game, rules: Rules, d3: D3Result): ProcedureContext
        // Which node to go to after rolling the die
        open val nextNode: Node get()= ChooseReRollSource

        override fun actionOwner(state: Game, rules: Rules): Team = getActionOwner(state)
        override fun getAvailableActions(state: Game, rules: Rules): List<GameActionDescriptor> {
            return listOf(RollDice(Dice.D3))
        }
        override fun applyAction(action: GameAction, state: Game, rules: Rules): Command {
            return castDiceRoll<D3Result>(action) { d3 ->
                val updatedContext = updateContext(state, rules, d3)
                return compositeCommandOf(
                    UpdateContext(updatedContext),
                    ReportDiceRoll(rollType, d3),
                    GotoNode(nextNode),
                )
            }
        }
    }

    // Shared logic for choosing the reroll source
    abstract inner class AbstractChooseRerollSource(
        // Where to go,if no rerolls are available
        val rerollNotAvailableCommand: () -> Command,
        // Where to go, if Coach accepts first roll
        val noRerollSelectedCommand: () -> Command
    ): ActionNode() {
        // Shortcut when exit nodes are the same for both cases
        constructor(exitWithoutRerollCommand: () -> Command = { ExitProcedure() }) : this(
            rerollNotAvailableCommand = exitWithoutRerollCommand,
            noRerollSelectedCommand = exitWithoutRerollCommand,
        )
        override fun name(): String = "ChooseRerollSource"
        abstract fun getRerollData(state: Game, rules: Rules): RerollData
        override fun actionOwner(state: Game, rules: Rules): Team = getActionOwner(state)
        override fun getAvailableActions(state: Game, rules: Rules): List<GameActionDescriptor> {
            val rerollInfo: RerollData = getRerollData(state, rules)
            val availableRerolls = calculateAvailableRerollsFor(
                rules,
                rerollInfo.player,
                rollType,
                rerollInfo.roll,
                rerollInfo.isSuccess

            )
            return when (availableRerolls == null) {
                true -> listOf(ContinueWhenReady)
                false -> listOf(SelectNoReroll(rerollInfo.isSuccess)) + availableRerolls
            }
        }
        override fun applyAction(action: GameAction, state: Game, rules: Rules): Command {
            return when (action) {
                Continue -> rerollNotAvailableCommand()
                is NoRerollSelected -> noRerollSelectedCommand()
                is RerollOptionSelected -> {
                    val rerollContext = state.rerollContext ?: INVALID_GAME_STATE("Missing reroll context")
                    if (rerollContext.type != rollType) {
                        INVALID_GAME_STATE("Reroll type mismatch: expected $rollType, got $rerollContext")
                    }
                    val updatedContext = rerollContext.copy(
                        source = action.getRerollSource(state),
                        selectedRerollOption = action.option
                    )
                    compositeCommandOf(
                        UpdateContext(updatedContext),
                        ReportRerollUsed(action.getRerollSource(state)),
                        GotoNode(UseRerollSource),
                    )
                }
                else -> INVALID_ACTION(action)
            }
        }
    }

    // Shared logic for re-rolling the dice after a reroll is selected
    // TODO Since we know there is only one dice, we do not need to figure out
    //   which dice can be rerolled. There is only one. Should we modify the
    //   API somehow to make it and Block the same?
    abstract inner class AbstractReRollDie: ActionNode() {
        override fun name(): String = "ReRollDie"
        // Update specific Roll Context with the roll data
        abstract fun updateContext(state: Game, rules: Rules, d3: D3Result): ProcedureContext
        // Which node to go to after re-rolling the dice
        open fun nextNodeCommand(): Command = ExitProcedure()

        override fun actionOwner(state: Game, rules: Rules): Team = getActionOwner(state)
        override fun getAvailableActions(state: Game, rules: Rules): List<GameActionDescriptor> {
            return listOf(RollDice(Dice.D3))
        }
        override fun applyAction(action: GameAction, state: Game, rules: Rules): Command {
            return castDiceRoll<D3Result>(action) { d3 ->
                val updatedContext = updateContext(state, rules, d3)
                return compositeCommandOf(
                    UpdateContext(updatedContext),
                    ReportDiceRoll(rollType, d3),
                    nextNodeCommand()
                )
            }
        }
    }

    class CommonUseRerollSource(private val rerollDiceNode: ActionNode): ParentNode() {
        override fun getChildProcedure(state: Game, rules: Rules): Procedure {
            return state.rerollContext?.source?.rerollProcedure ?: error("Missing reroll procedure for D3WithReroll: ${state.rerollContext}")
        }
        override fun onExitNode(state: Game, rules: Rules): Command {
            val context = state.rerollContext!!
            return when (context.rerollAllowed) {
                true -> GotoNode(rerollDiceNode)
                false -> ExitProcedure()
            }
        }
    }
}
