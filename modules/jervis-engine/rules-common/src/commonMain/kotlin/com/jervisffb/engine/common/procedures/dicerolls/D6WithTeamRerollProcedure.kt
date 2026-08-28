@file:Suppress("PropertyName")

package com.jervisffb.engine.common.procedures.dicerolls

import com.jervisffb.engine.actions.Continue
import com.jervisffb.engine.actions.ContinueWhenReady
import com.jervisffb.engine.actions.D6Result
import com.jervisffb.engine.actions.Dice
import com.jervisffb.engine.actions.DieResult
import com.jervisffb.engine.actions.GameAction
import com.jervisffb.engine.actions.GameActionDescriptor
import com.jervisffb.engine.actions.NoRerollSelected
import com.jervisffb.engine.actions.RerollOptionSelected
import com.jervisffb.engine.actions.RollDice
import com.jervisffb.engine.actions.SelectNoReroll
import com.jervisffb.engine.actions.SelectRerollOption
import com.jervisffb.engine.commands.Command
import com.jervisffb.engine.commands.buildCompositeCommand
import com.jervisffb.engine.commands.compositeCommandOf
import com.jervisffb.engine.commands.context.AddContext
import com.jervisffb.engine.commands.context.RemoveContext
import com.jervisffb.engine.commands.context.UpdateContext
import com.jervisffb.engine.commands.fsm.ExitProcedure
import com.jervisffb.engine.commands.fsm.GotoNode
import com.jervisffb.engine.commands.probabiliy.AddChanceObservation
import com.jervisffb.engine.commands.probabiliy.UpdateChanceObservation
import com.jervisffb.engine.common.reports.ReportDiceRoll
import com.jervisffb.engine.common.reports.ReportRerollUsed
import com.jervisffb.engine.common.utils.calculateAvailableRerollsForTeam
import com.jervisffb.engine.fsm.ActionNode
import com.jervisffb.engine.fsm.Node
import com.jervisffb.engine.fsm.ParentNode
import com.jervisffb.engine.fsm.Procedure
import com.jervisffb.engine.fsm.castDiceRoll
import com.jervisffb.engine.model.DieId
import com.jervisffb.engine.model.Game
import com.jervisffb.engine.model.Team
import com.jervisffb.engine.model.context.ProcedureContext
import com.jervisffb.engine.model.context.UseRerollContext
import com.jervisffb.engine.model.context.getContextOrNull
import com.jervisffb.engine.rules.DiceRollType
import com.jervisffb.engine.rules.Rules
import com.jervisffb.engine.rules.common.procedures.DieRoll
import com.jervisffb.engine.rules.common.rerolls.DiceRerollOption
import com.jervisffb.engine.rules.common.rerolls.TeamReroll
import com.jervisffb.engine.rules.common.skills.RerollSource
import com.jervisffb.engine.statistics.probability.observation.ChanceDieResult
import com.jervisffb.engine.statistics.probability.observation.ChanceObservation
import com.jervisffb.engine.statistics.probability.observation.ChanceObservationHandler
import com.jervisffb.engine.statistics.probability.observation.ChanceObservationScope
import com.jervisffb.engine.statistics.probability.observation.ChanceRerollOption
import com.jervisffb.engine.statistics.probability.observation.ChanceRerollSelection
import com.jervisffb.engine.statistics.probability.observation.ChanceRerollSource
import com.jervisffb.engine.statistics.probability.observation.ChanceRerollSourceKind
import com.jervisffb.engine.statistics.probability.observation.ChanceResultId
import com.jervisffb.engine.utils.INVALID_ACTION
import com.jervisffb.engine.utils.INVALID_GAME_STATE
import kotlinx.collections.immutable.toPersistentList

/**
 * Shared logic for procedures that handle a single D6 roll with reroll options
 * for teams rather than players.
 *
 * WARNING: Be careful with storing commands in abstract nodes. If these
 * commands have mutable state (like logs), they risk being inconsistent and
 * will crash the game engine.
 */
abstract class D6WithTeamRerollProcedure : Procedure(), ChanceObservationHandler {
    abstract val RollDie: ActionNode
    abstract val ChooseReRollSource: AbstractChooseRerollSource
    open val UseRerollSource: ParentNode by lazy { UseRerollSourceCommon(ReRollDie) }
    abstract val ReRollDie: ActionNode

    abstract val rollType: DiceRollType
    abstract fun getActionOwner(state: Game): Team
    abstract fun onEnterRollProcedure(state: Game, rules: Rules): Command?
    abstract fun onExitRollProcedure(state: Game, rules: Rules): Command?

    final override fun onEnterProcedure(state: Game, rules: Rules): Command {
        val owner = getActionOwner(state)
        val rollContextCommands = onEnterRollProcedure(state, rules)
        val enclosingRollIndex = state.getContextOrNull<UseRerollContext>()?.chanceRollIndex
        val rerollContextCommand = AddContext(
            UseRerollContext(
                type = rollType,
                originalRoll = emptyList(),
                team = owner,
                chanceEnclosingRollIndex = enclosingRollIndex,
            ),
        )
        return compositeCommandOf(
            rollContextCommands,
            rerollContextCommand,
        )
    }

    final override fun onExitProcedure(state: Game, rules: Rules): Command {
        val rollContextCommands = onExitRollProcedure(state, rules)
        val rerollContext = state.getRerollContext()
        if (rerollContext.type != rollType) {
            INVALID_GAME_STATE("UseRerollContext's are in an inconsistent state. Received: $rerollContext")
        }
        val chanceCommand = finalizeRerollableChanceObservations(
            state = state,
            data = ChooseReRollSource.getRerollData(state, rules),
            rerollContext = rerollContext,
        )
        return compositeCommandOf(
            chanceCommand,
            RemoveContext(rerollContext),
            rollContextCommands,
        )
    }

    abstract inner class AbstractRollDie : ActionNode() {
        override fun name(): String = "RollDie"

        abstract fun updateContext(state: Game, rules: Rules, d6: D6Result): ProcedureContext

        open val nextNode: Node get() = ChooseReRollSource

        override fun actionOwner(state: Game, rules: Rules): Team = getActionOwner(state)

        override fun getAvailableActions(state: Game, rules: Rules): List<GameActionDescriptor> {
            return listOf(RollDice(Dice.D6, type = rollType))
        }

        override fun applyAction(action: GameAction, state: Game, rules: Rules): Command {
            return castDiceRoll<D6Result>(action) { d6 ->
                val updatedContext = updateContext(state, rules, d6)
                val rerollContext = state.getRerollContext()
                val chanceObservation = createRerollableChanceObservation(
                    state = state,
                    rollType = rollType,
                    team = getActionOwner(state),
                    result = d6,
                    rerollContext = rerollContext,
                )
                compositeCommandOf(
                    UpdateContext(updatedContext),
                    chanceObservation?.let { observation ->
                        UpdateContext(
                            rerollContext.copy(
                                chanceRollIndex = observation.index,
                                chanceObservations = rerollContext.chanceObservations.add(observation),
                            ),
                        )
                    },
                    chanceObservation?.let(::AddChanceObservation),
                    ReportDiceRoll(rollType, d6),
                    GotoNode(nextNode),
                )
            }
        }
    }

    abstract inner class AbstractChooseRerollSource(
        val rerollNotAvailableCommand: () -> Command,
        val noRerollSelectedCommand: () -> Command,
    ) : ActionNode() {
        constructor(exitWithoutRerollCommand: () -> Command = { ExitProcedure() }) : this(
            rerollNotAvailableCommand = exitWithoutRerollCommand,
            noRerollSelectedCommand = exitWithoutRerollCommand,
        )

        override fun name(): String = "ChooseRerollSource"
        abstract fun getRerollData(state: Game, rules: Rules): TeamRerollData
        open fun getAvailableRerolls(
            state: Game,
            rules: Rules,
            rerollData: TeamRerollData,
        ): List<DiceRerollOption> = calculateAvailableRerollsForTeam(
            team = rerollData.team,
            type = rollType,
            roll = listOf(rerollData.roll),
            firstRollWasSuccess = rerollData.isSuccess,
        )
        override fun actionOwner(state: Game, rules: Rules): Team = getActionOwner(state)
        override fun getAvailableActions(state: Game, rules: Rules): List<GameActionDescriptor> {
            val rerollData = getRerollData(state, rules)
            val rerollOptions = getAvailableRerolls(state, rules, rerollData)
            return when (rerollOptions.isEmpty()) {
                true -> listOf(ContinueWhenReady)
                false -> listOf(SelectNoReroll(rerollData.isSuccess), SelectRerollOption(rerollOptions))
            }
        }
        override fun applyAction(action: GameAction, state: Game, rules: Rules): Command {
            val rollData = getRerollData(state, rules)
            val rerollContext = state.getRerollContext()
            if (rerollContext.type != rollType) {
                INVALID_GAME_STATE("Reroll type mismatch: expected $rollType, got $rerollContext")
            }
            val selectedSource = (action as? RerollOptionSelected)?.getRerollSource(state)
            val observationUpdate = updateRerollableChanceDecision(
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
                Continue ->
                    compositeCommandOf(
                        observationUpdate?.command,
                        observationUpdate?.let { UpdateContext(contextWithObservation) },
                        rerollNotAvailableCommand(),
                    )
                is NoRerollSelected ->
                    compositeCommandOf(
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

    abstract inner class AbstractReRollDie : ActionNode() {
        override fun name(): String = "ReRollDie"

        abstract fun updateContext(state: Game, rules: Rules, d6: D6Result): ProcedureContext

        open fun nextNodeCommand(): Command = ExitProcedure()

        override fun actionOwner(state: Game, rules: Rules): Team = getActionOwner(state)

        override fun getAvailableActions(state: Game, rules: Rules): List<GameActionDescriptor> {
            return listOf(RollDice(Dice.D6, type = rollType))
        }

        override fun applyAction(action: GameAction, state: Game, rules: Rules): Command {
            return castDiceRoll<D6Result>(action) { d6 ->
                val updatedContext = updateContext(state, rules, d6)
                val rerollContext = state.getRerollContext()
                val chanceObservation = rerollContext.chanceRollIndex?.let { rootIndex ->
                    createRerollableChanceObservation(
                        state = state,
                        rollType = rollType,
                        team = getActionOwner(state),
                        result = d6,
                        rerollContext = rerollContext,
                        rerolledRollIndex = rootIndex,
                    )
                }
                compositeCommandOf(
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
                    nextNodeCommand(),
                )
            }
        }
    }

    class UseRerollSourceCommon(
        private val rerollDiceNode: ActionNode,
        private val noRerollCommand: () -> Command = { ExitProcedure() },
    ) : ParentNode() {
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

    private fun createRerollableChanceObservation(
        state: Game,
        rollType: DiceRollType,
        team: Team,
        result: DieResult,
        rerollContext: UseRerollContext,
        rerolledRollIndex: Int? = null,
    ): ChanceObservation.DiceRoll? {
        if (!state.collectChanceData) return null
        val sequence = state.nextAvailableChanceObservationIndex
        return ChanceObservation.DiceRoll(
            index = sequence,
            rollType = rollType,
            team = team.id,
            player = null,
            dice = listOf(
                ChanceDieResult(
                    id = ChanceResultId(sequence, 0),
                    result = result,
                ),
            ),
            scope = chanceScope(state, team),
            enclosingRollIndex = rerollContext.chanceEnclosingRollIndex,
            rerolledRollIndex = rerolledRollIndex,
        )
    }

    private fun updateRerollableChanceDecision(
        state: Game,
        rules: Rules,
        rollType: DiceRollType,
        data: TeamRerollData,
        rerollContext: UseRerollContext,
        selectedSource: RerollSource? = null,
    ): ChanceObservationUpdate? {
        if (!state.collectChanceData) return null
        val rootSequence = rerollContext.chanceRollIndex ?: return null
        val root = rerollContext.chanceObservations.firstOrNull { it.index == rootSequence } ?: return null
        val resultId = root.dice.single().id
        val options = captureChanceRerollOptions(
            state = state,
            rules = rules,
            rollType = rollType,
            team = data.team,
            dicePool = listOf(data.roll),
            resultIdsByDieId = mapOf(data.roll.id to resultId),
            observedSuccess = data.isSuccess,
        )
        val updated = root.copy(
            success = data.isSuccess,
            rerollOptions = options,
            selectedReroll = selectedSource?.let { source ->
                ChanceRerollSelection(
                    sourceId = source.id,
                    resultIds = listOf(resultId),
                )
            },
        )
        return ChanceObservationUpdate(root, updated)
    }

    private fun finalizeRerollableChanceObservations(
        state: Game,
        data: TeamRerollData,
        rerollContext: UseRerollContext,
    ): Command? {
        if (!state.collectChanceData) return null
        val rootSequence = rerollContext.chanceRollIndex ?: return null
        val localObservations = rerollContext.chanceObservations
        if (localObservations.isEmpty()) return null
        val finalRollSequence = localObservations.maxOf { it.index }
        return buildCompositeCommand {
            localObservations.forEach { observation ->
                val selectedReroll = if (observation.index == rootSequence) {
                    observation.selectedReroll?.copy(
                        allowed = rerollContext.rerollAllowed,
                        aborted = rerollContext.rerollAborted,
                    )
                } else {
                    observation.selectedReroll
                }
                val updated = observation.copy(
                    success = if (observation.index == finalRollSequence) data.isSuccess ?: true else observation.success,
                    selectedReroll = selectedReroll,
                    finalized = true,
                )
                if (updated != observation) {
                    add(UpdateChanceObservation(observation.index, observation, updated))
                }
            }
        }
    }

    private fun captureChanceRerollOptions(
        state: Game,
        rules: Rules,
        rollType: DiceRollType,
        team: Team,
        dicePool: List<DieRoll<*>>,
        resultIdsByDieId: Map<DieId, ChanceResultId>,
        observedSuccess: Boolean?,
    ): List<ChanceRerollOption> {
        val sources = team.rerolls.distinctBy { it.id }

        return sources.flatMap { source ->
            val successOptions = source.optionsWhenApplicable(state, rollType, dicePool, wasSuccess = true)
            val failureOptions = source.optionsWhenApplicable(state, rollType, dicePool, wasSuccess = false)
            if (successOptions.isEmpty() && failureOptions.isEmpty()) return@flatMap emptyList()
            val choices = (successOptions + failureOptions)
                .map { option -> option.dice.orEmpty().map { it.id }.toSet() }
                .distinct()
            val successChoices = successOptions.map { option -> option.dice.orEmpty().map { it.id }.toSet() }
            val failureChoices = failureOptions.map { option -> option.dice.orEmpty().map { it.id }.toSet() }
            val sourceSnapshot = source.toChanceSnapshot(rules, team)
            choices.mapNotNull { choice ->
                val resultIds = choice.mapNotNull(resultIdsByDieId::get)
                if (resultIds.isEmpty()) return@mapNotNull null
                ChanceRerollOption(
                    source = sourceSnapshot,
                    resultIds = resultIds,
                    appliesOnSuccess = choice in successChoices,
                    appliesOnFailure = choice in failureChoices,
                    currentlyAvailable =
                        rules.isRerollAllowed(dicePool) &&
                            source.canReroll(state, rollType, dicePool, observedSuccess),
                )
            }
        }
    }

    private fun RerollSource.toChanceSnapshot(
        rules: Rules,
        team: Team,
    ): ChanceRerollSource {
        val reroll = this
        val tests = buildList {
            when (reroll) {
                is TeamReroll -> reroll.getChanceRerollTest()?.let { add(it) }
                else -> { /* Do nothing. Should this throw as only team rerolls should be allowed here? */ }
            }
        }
        return ChanceRerollSource(
            id = id,
            owner = team.id,
            kind = ChanceRerollSourceKind.TEAM_REROLL,
            description = rerollDescription,
            resetAt = rerollResetAt,
            skillId = null,
            tests = tests,
        )
    }

    private fun chanceScope(state: Game, team: Team): ChanceObservationScope {
        return ChanceObservationScope(
            half = state.halfNo,
            drive = state.driveNo,
            team = team.id,
            turn = team.turnMarker,
            player = null,
        )
    }
}

/** Data needed to calculate reroll options and finalize a team-owned roll. */
data class TeamRerollData(
    val team: Team,
    val roll: DieRoll<*>,
    val isSuccess: Boolean?,
)
