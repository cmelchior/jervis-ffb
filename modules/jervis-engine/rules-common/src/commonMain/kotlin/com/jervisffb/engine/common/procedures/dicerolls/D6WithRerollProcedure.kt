@file:Suppress("PropertyName")

package com.jervisffb.engine.common.procedures.dicerolls

import com.jervisffb.engine.actions.Continue
import com.jervisffb.engine.actions.ContinueWhenReady
import com.jervisffb.engine.actions.D6Result
import com.jervisffb.engine.actions.Dice
import com.jervisffb.engine.actions.GameAction
import com.jervisffb.engine.actions.GameActionDescriptor
import com.jervisffb.engine.actions.NoRerollSelected
import com.jervisffb.engine.actions.RerollOptionSelected
import com.jervisffb.engine.actions.RollDice
import com.jervisffb.engine.actions.SelectNoReroll
import com.jervisffb.engine.actions.SelectRerollOption
import com.jervisffb.engine.commands.Command
import com.jervisffb.engine.commands.compositeCommandOf
import com.jervisffb.engine.commands.context.AddContext
import com.jervisffb.engine.commands.context.RemoveContext
import com.jervisffb.engine.commands.context.UpdateContext
import com.jervisffb.engine.commands.fsm.ExitProcedure
import com.jervisffb.engine.commands.fsm.GotoNode
import com.jervisffb.engine.commands.probabiliy.AddChanceObservation
import com.jervisffb.engine.common.reports.ReportDiceRoll
import com.jervisffb.engine.common.reports.ReportRerollUsed
import com.jervisffb.engine.common.utils.calculateAvailableRerollsForPlayer
import com.jervisffb.engine.fsm.ActionNode
import com.jervisffb.engine.fsm.Node
import com.jervisffb.engine.fsm.ParentNode
import com.jervisffb.engine.fsm.Procedure
import com.jervisffb.engine.fsm.castDiceRoll
import com.jervisffb.engine.model.Game
import com.jervisffb.engine.model.Player
import com.jervisffb.engine.model.Team
import com.jervisffb.engine.model.context.ProcedureContext
import com.jervisffb.engine.model.context.UseRerollContext
import com.jervisffb.engine.model.context.getContext
import com.jervisffb.engine.model.context.getContextOrNull
import com.jervisffb.engine.rules.DiceRollType
import com.jervisffb.engine.rules.Rules
import com.jervisffb.engine.rules.common.skills.RerollSource
import com.jervisffb.engine.statistics.probability.ChanceObservationHandler
import com.jervisffb.engine.utils.INVALID_ACTION
import com.jervisffb.engine.utils.INVALID_GAME_STATE
import kotlinx.collections.immutable.toPersistentList

/**
 * Shared logic for Procedures that are handling a single D6 roll with
 * re-roll options for players.
 *
 * TODO Consolidate this with [D3WithRerollProcedure]
 *
 * WARNING: Be careful with storing commands in abstract nodes. If these
 * commands have mutable state (like logs), they risk being inconsistent and
 * will crash the game engine.
 *
 * Right now, there is only a single procedure that does the same for Teams,
 * `com.jervisffb.engine.bb2025.procedures.rerolls.TeamMascotRoll`. This
 * procedure implements the same nodes as found here, so any changes should
 * be mirrored there.
 */
abstract class D6WithRerollProcedure: Procedure(), ChanceObservationHandler {
    // Roll initial dice
    abstract val RollDie: ActionNode
    // Select re-roll source or no reroll to keep result
    abstract val ChooseReRollSource: AbstractChooseRerollSource
    // Use and Choose any potential reroll type.
    // Implementation Note: Use `lazy` to work around initialization order issues.
    open val UseRerollSource: ParentNode by lazy { CommonUseRerollSource(ReRollDie) }
    // Use the reroll and set the dice that can be rerolled
    // Reroll the dice set by `UseRerollSource`
    abstract val ReRollDie: ActionNode

    abstract val rollType: DiceRollType
    // Returns Action Owner for all "roll" nodes. For things with rerolls, this
    // should always be associated with a Player
    abstract fun getActionOwner(state: Game): Player
    // Roll specific "Enter" method
    abstract fun onEnterRollProcedure(state: Game, rules: Rules): Command?
    // Roll specific "Exit" method
    abstract fun onExitRollProcedure(state: Game, rules: Rules): Command?

    final override fun onEnterProcedure(state: Game, rules: Rules): Command {
        val owner = getActionOwner(state)
        val rollContextCommands = onEnterRollProcedure(state, rules)
        val enclosingRollSequence = state.getContextOrNull<UseRerollContext>()?.chanceRollIndex
        val rerollContextCommand = AddContext(
            UseRerollContext(
                type = rollType,
                originalRoll = emptyList(), // Will be set later
                team = owner.team,
                player = owner,
                chanceRollIndex = enclosingRollSequence,
            )
        )
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
        val chanceCommand = finalizeD6ChanceObservations(
            state = state,
            data = ChooseReRollSource.getRerollData(state, rules),
            rerollContext = rerollContext,
        )
        return compositeCommandOf(
            RemoveContext(rerollContext),
            rollContextCommands,
            chanceCommand,
        )
    }

    // Shared logic for rolling the initial D6
    abstract inner class AbstractRollDie: ActionNode() {
        override fun name(): String = "RollDie"
        // Update specific Roll Context with the roll data
        abstract fun updateContext(state: Game, rules: Rules, d6: D6Result): ProcedureContext
        // Which node to go to after rolling the die
        open val nextNode: Node get() = ChooseReRollSource

        override fun actionOwner(state: Game, rules: Rules): Team = getActionOwner(state).team
        override fun getAvailableActions(state: Game, rules: Rules): List<GameActionDescriptor> {
            return listOf(RollDice(Dice.D6))
        }
        override fun applyAction(action: GameAction, state: Game, rules: Rules): Command {
            return castDiceRoll<D6Result>(action) { d6 ->
                val updatedContext = updateContext(state, rules, d6)
                val rerollContext = state.getContext<UseRerollContext>()
                val chanceObservation = createD6ChanceObservation(
                    state = state,
                    rollType = rollType,
                    player = getActionOwner(state),
                    result = d6,
                    rerollContext = rerollContext,
                )
                val chanceCommand = chanceObservation?.let { observation ->
                    UpdateContext(
                        rerollContext.copy(
                            chanceRollIndex = observation.index,
                            chanceObservations = rerollContext.chanceObservations.add(observation),
                        )
                    )
                }
                return compositeCommandOf(
                    UpdateContext(updatedContext),
                    chanceCommand,
                    chanceObservation?.let(::AddChanceObservation),
                    ReportDiceRoll(rollType, d6),
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
        override fun actionOwner(state: Game, rules: Rules): Team = getActionOwner(state).team
        override fun getAvailableActions(state: Game, rules: Rules): List<GameActionDescriptor> {
            val rerollInfo: RerollData = getRerollData(state, rules)
            val rerollOptions = calculateAvailableRerollsForPlayer(
                rerollInfo.player,
                rollType,
                rerollInfo.roll,
                rerollInfo.isSuccess

            )
            return when (rerollOptions.isEmpty()) {
                true -> listOf(ContinueWhenReady)
                false -> listOf(SelectNoReroll(rerollInfo.isSuccess), SelectRerollOption(rerollOptions))
            }
        }
        override fun applyAction(action: GameAction, state: Game, rules: Rules): Command {
            val rollData = getRerollData(state, rules)
            val rerollContext = state.getRerollContext()
            if (rerollContext.type != rollType) {
                INVALID_GAME_STATE("Reroll type mismatch: expected $rollType, got $rerollContext")
            }
            val selectedSource = (action as? RerollOptionSelected)?.getRerollSource(state)
            val observationUpdate = updateD6ChanceDecision(
                state = state,
                rules = rules,
                rollType = rollType,
                data = rollData,
                rerollContext = rerollContext,
                selectedSource = selectedSource,
            )
            val contextWithObservation = observationUpdate?.let { update ->
                rerollContext.copy(
                    chanceObservations = rerollContext.chanceObservations.map { observation ->
                        if (observation.index == update.previous.index) update.updated else observation
                    }.toPersistentList(),
                )
            } ?: rerollContext
            return when (action) {
                Continue -> compositeCommandOf(
                    observationUpdate?.command,
                    observationUpdate?.let { UpdateContext(contextWithObservation) },
                    rerollNotAvailableCommand(),
                )
                is NoRerollSelected -> compositeCommandOf(
                    observationUpdate?.command,
                    observationUpdate?.let { UpdateContext(contextWithObservation) },
                    noRerollSelectedCommand(),
                )
                is RerollOptionSelected -> {
                    val updatedContext = contextWithObservation.copy(
                        originalRoll = listOf(rollData.roll),
                        source = selectedSource,
                        rerollDice = action.getRerollDice(),
                    )
                    compositeCommandOf(
                        observationUpdate?.command,
                        UpdateContext(updatedContext),
                        ReportRerollUsed(selectedSource!!),
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
        abstract fun updateContext(state: Game, rules: Rules, d6: D6Result): ProcedureContext
        // Which node to go to after re-rolling the dice
        open fun nextNodeCommand(): Command = ExitProcedure()

        override fun actionOwner(state: Game, rules: Rules): Team = getActionOwner(state).team
        override fun getAvailableActions(state: Game, rules: Rules): List<GameActionDescriptor> {
            return listOf(RollDice(Dice.D6))
        }
        override fun applyAction(action: GameAction, state: Game, rules: Rules): Command {
            return castDiceRoll<D6Result>(action) { d6 ->
                val updatedContext = updateContext(state, rules, d6)
                val rerollContext = state.getContext<UseRerollContext>()
                val chanceObservation = rerollContext.chanceRollIndex?.let { rootSequence ->
                    createD6ChanceObservation(
                        state = state,
                        rollType = rollType,
                        player = getActionOwner(state),
                        result = d6,
                        rerollContext = rerollContext,
                        rerolledRollId = rootSequence,
                    )
                }
                return compositeCommandOf(
                    UpdateContext(updatedContext),
                    chanceObservation?.let { observation ->
                        UpdateContext(
                            rerollContext.copy(
                                chanceObservations = rerollContext.chanceObservations.add(observation),
                            ),
                        )
                    },
                    chanceObservation?.let(::AddChanceObservation),
                    ReportDiceRoll(rollType, d6),
                    nextNodeCommand()
                )
            }
        }
    }

    /**
     * Class representing a "common" re-roll [Node].
     *
     * Common in this context means:
     *
     * 1. Using the reroll can be delegated to [RerollSource.rerollProcedure].
     * 2. We can immediately jump to the next node without adding any other
     *    [Command] objects.
     */
    class CommonUseRerollSource(
        private val rerollDiceNode: ActionNode,
        private val noRerollCommand: () -> Command = { ExitProcedure() }
    ): ParentNode() {
        override fun getChildProcedure(state: Game, rules: Rules): Procedure {
            val context = state.getRerollContextOrNull()
            return context?.source?.rerollProcedure ?: INVALID_GAME_STATE("Missing reroll source: $context")
        }
        override fun onExitNode(state: Game, rules: Rules): Command {
            val context = state.getRerollContext()
            return when (context.rerollAllowed) {
                true -> GotoNode(rerollDiceNode)
                false -> noRerollCommand()
            }
        }
    }
}
