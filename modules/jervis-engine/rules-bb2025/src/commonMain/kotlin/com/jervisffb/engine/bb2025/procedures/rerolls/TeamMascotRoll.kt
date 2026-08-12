package com.jervisffb.engine.bb2025.procedures.rerolls

import com.jervisffb.engine.GameDelta
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
import com.jervisffb.engine.bb2025.context.MascotRollContext
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
import com.jervisffb.engine.common.procedures.dicerolls.ChanceObservationUpdate
import com.jervisffb.engine.common.procedures.dicerolls.D6WithRerollProcedure
import com.jervisffb.engine.common.procedures.dicerolls.RerollData
import com.jervisffb.engine.common.procedures.dicerolls.optionsWhenApplicable
import com.jervisffb.engine.common.reports.ReportDiceRoll
import com.jervisffb.engine.common.reports.ReportRerollUsed
import com.jervisffb.engine.fsm.ActionNode
import com.jervisffb.engine.fsm.Node
import com.jervisffb.engine.fsm.ParentNode
import com.jervisffb.engine.fsm.Procedure
import com.jervisffb.engine.fsm.castDiceRoll
import com.jervisffb.engine.model.DieId
import com.jervisffb.engine.model.Game
import com.jervisffb.engine.model.Team
import com.jervisffb.engine.model.context.UseRerollContext
import com.jervisffb.engine.model.context.assertContext
import com.jervisffb.engine.model.context.getContext
import com.jervisffb.engine.model.context.getContextOrNull
import com.jervisffb.engine.rules.DiceRollType
import com.jervisffb.engine.rules.Rules
import com.jervisffb.engine.rules.common.procedures.D6DieRoll
import com.jervisffb.engine.rules.common.procedures.DieRoll
import com.jervisffb.engine.rules.common.rerolls.DiceRerollOption
import com.jervisffb.engine.rules.common.skills.Duration
import com.jervisffb.engine.rules.common.skills.Duration.END_OF_ACTION
import com.jervisffb.engine.rules.common.skills.Duration.SPECIAL
import com.jervisffb.engine.rules.common.skills.RerollSource
import com.jervisffb.engine.statistics.probability.observation.ChanceDieResult
import com.jervisffb.engine.statistics.probability.observation.ChanceObservation
import com.jervisffb.engine.statistics.probability.observation.ChanceObservationHandler
import com.jervisffb.engine.statistics.probability.observation.ChanceObservationScope
import com.jervisffb.engine.statistics.probability.observation.ChanceRerollOption
import com.jervisffb.engine.statistics.probability.observation.ChanceRerollSelection
import com.jervisffb.engine.statistics.probability.observation.ChanceRerollSource
import com.jervisffb.engine.statistics.probability.observation.ChanceRerollSourceKind
import com.jervisffb.engine.statistics.probability.observation.ChanceRerollTest
import com.jervisffb.engine.statistics.probability.observation.ChanceRerollTestEffect
import com.jervisffb.engine.statistics.probability.observation.ChanceResultId
import com.jervisffb.engine.utils.INVALID_ACTION
import com.jervisffb.engine.utils.INVALID_GAME_STATE
import kotlinx.collections.immutable.toPersistentList
import kotlin.collections.map
import kotlin.collections.orEmpty
import kotlin.let

/**
 * Procedure controlling the Mascot roll, i.e., when a Team checks to see if
 * Team Mascot reroll works.
 *
 * While this class is conceptually similar to [D6WithRerollProcedure] it
 * is slightly different in the sense that the Mascot works at a team, not
 * player level.
 *
 * For this reason, this procedure is self-contained, but follows the same
 * "shape" as [D6WithRerollProcedure]. This means that any change to this class
 * should most likely be mirrored there (and vice versa).
 */
object TeamMascotRoll: Procedure(), ChanceObservationHandler {
    override val initialNode: Node = RollDie
    override fun onEnterProcedure(state: Game, rules: Rules): Command {
        val context = state.getContext<MascotRollContext>()
        val rerollContext = UseRerollContext(
            type = DiceRollType.TEAM_MASCOT,
            originalRoll = emptyList(), // Will be set later
            team = context.team,
            chanceEnclosingRollIndex = state.getContextOrNull<UseRerollContext>()?.chanceRollIndex,
        )
        return AddContext(rerollContext)
    }
    override fun onExitProcedure(state: Game, rules: Rules): Command {
        val mascotContext = state.getContext<MascotRollContext>()
        val rerollContext = state.getRerollContext()
        val chanceCommand = finalizeRerollableChanceObservations(
            state = state,
            data = TeamRerollData(
                team = mascotContext.team,
                roll = mascotContext.roll!!,
                isSuccess = mascotContext.isSuccess,
            ),
            rerollContext = rerollContext,
        )
        return compositeCommandOf(
            chanceCommand,
            RemoveContext(rerollContext),
        )
    }
    override fun isValid(state: Game, rules: Rules) = state.assertContext<MascotRollContext>()

    object RollDie: ActionNode() {
        override fun actionOwner(state: Game, rules: Rules): Team = state.getContext<MascotRollContext>().team
        override fun getAvailableActions(state: Game, rules: Rules): List<GameActionDescriptor> {
            return listOf(RollDice(Dice.D6, type = DiceRollType.TEAM_MASCOT))
        }
        override fun applyAction(action: GameAction, state: Game, rules: Rules): Command {
            return castDiceRoll<D6Result>(action) { d6 ->
                val context = state.getContext<MascotRollContext>()
                val rerollContext = state.getRerollContext()
                val success = isSuccess(d6)
                val roll = D6DieRoll.create(d6)
                val chanceObservation = createRerollableChanceObservation(
                    state = state,
                    rollType = DiceRollType.TEAM_MASCOT,
                    team = context.team,
                    result = d6,
                    rerollContext = rerollContext,
                )
                compositeCommandOf(
                    ReportDiceRoll(DiceRollType.TEAM_MASCOT, d6),
                    chanceObservation?.let(::AddChanceObservation),
                    UpdateContext(context.copy(
                        roll = roll,
                        isSuccess = success,
                    )),
                    UpdateContext(rerollContext.copy(
                        originalRoll = listOf(roll),
                        chanceRollIndex = chanceObservation?.index,
                        chanceObservations = when (chanceObservation != null) {
                            true -> rerollContext.chanceObservations.add(chanceObservation)
                            false -> rerollContext.chanceObservations
                        }
                    )),
                    GotoNode(ChooseReRollSource)
                )
            }
        }
    }

    object ChooseReRollSource : ActionNode() {
        override fun actionOwner(state: Game, rules: Rules): Team = state.getContext<MascotRollContext>().team
        override fun getAvailableActions(state: Game, rules: Rules): List<GameActionDescriptor> {
            val context = state.getContext<MascotRollContext>()
            // Team re-rolls are not supported on Mascot rolls, and it is uncler how
            // player skills could affect it, so for now, no rerolls are allowed.
            val availableRerolls = emptyList<DiceRerollOption>()
            return when (availableRerolls.isEmpty()) {
                true -> listOf(ContinueWhenReady)
                false -> listOf(SelectNoReroll(context.isSuccess), SelectRerollOption(availableRerolls))
            }
        }

        override fun applyAction(action: GameAction, state: Game, rules: Rules): Command {
            val context = state.getContext<MascotRollContext>()
            val rerollContext = state.getRerollContext()
            val selectedSource = (action as? RerollOptionSelected)?.getRerollSource(state)
            val observationUpdate = updateRerollableChanceDecision(
                state = state,
                rules = rules,
                rollType = DiceRollType.TEAM_MASCOT,
                data = TeamRerollData(
                    team = context.team,
                    roll = context.roll!!,
                    isSuccess = context.isSuccess

                ),
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
                Continue,
                is NoRerollSelected -> compositeCommandOf(
                    observationUpdate?.command,
                    observationUpdate?.let { UpdateContext(contextWithObservation) },
                    ExitProcedure()
                )
                is RerollOptionSelected -> {
                    val updatedContext = contextWithObservation.copy(
                        originalRoll = listOf(context.roll),
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

    object UseRerollSource : ParentNode() {
        override fun getChildProcedure(state: Game, rules: Rules): Procedure {
            val context = state.getRerollContext()
            return context.source?.rerollProcedure ?: INVALID_GAME_STATE("Missing reroll source: $context")
        }
        override fun onExitNode(state: Game, rules: Rules): Command {
            val context = state.getRerollContext()
            return if (context.rerollAllowed) {
                GotoNode(ReRollDie)
            } else {
                ExitProcedure()
            }
        }
    }

    object ReRollDie : ActionNode() {
        override fun actionOwner(state: Game, rules: Rules): Team = state.getContext<MascotRollContext>().team
        override fun getAvailableActions(state: Game, rules: Rules): List<GameActionDescriptor> = listOf(RollDice(Dice.D6, type = DiceRollType.TEAM_MASCOT))
        override fun applyAction(action: GameAction, state: Game, rules: Rules): Command {
            return castDiceRoll<D6Result>(action) { d6 ->
                val rollContext = state.getContext<MascotRollContext>()
                val rerollContext = state.getRerollContext()
                val rerollResult = rollContext.copy(
                    roll = rollContext.roll!!.copyReroll(
                        rerollSource = rerollContext.source,
                        rerolledResult = d6,
                    ),
                    isSuccess = isSuccess(d6),
                )
                val chanceObservation = rerollContext.chanceRollIndex?.let { rootIndex ->
                    createRerollableChanceObservation(
                        state = state,
                        rollType = DiceRollType.TEAM_MASCOT,
                        team = rollContext.team,
                        result = d6,
                        rerollContext = rerollContext,
                        rerolledRollIndex = rootIndex,
                    )
                }
                return compositeCommandOf(
                    chanceObservation?.let { observation ->
                        UpdateContext(
                            rerollContext.copy(
                                chanceObservations = rerollContext.chanceObservations.add(observation),
                            ),
                        )
                    },
                    chanceObservation?.let(::AddChanceObservation),
                    UpdateContext(rerollResult),
                    ReportDiceRoll(DiceRollType.TEAM_MASCOT, d6),
                    ExitProcedure()
                )
            }
        }
    }

    // --- HELPER FUNCTIONS ---

    private fun isSuccess(roll: D6Result): Boolean {
        val target = TeamMascotReroll.TARGET
        return roll.value >= target
    }

    // For now, Team Mascot is the only team reroll with its own gate.
    // Since D6WithRerollProcedure is only meant for player rolls, we cannot
    // re-use those methods for this. As we are currently mirroring channges
    // from D6WithRerollProcedure to here, the below methods are exactly that.
    // Copies that are slightly modified.
    // TODO These APIs probably needs to be refactored to be more generic

    fun createRerollableChanceObservation(
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

    /**
     * Records the result of the reroll decision for the initial die observation in
     * [rerollContext].
     *
     * The update captures whether the roll succeeded, every reroll option that
     * applies to the observed result, and the selected source, if any. The returned
     * value contains both the updated observation and the command needed to apply
     * [GameDelta.chanceObservations].
     *
     * Returns the pending update, or `null` when chance-data collection is
     * disabled or the context does not contain its initial observation.
     */
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

    /**
     * Builds a command that finalizes every die observation collected by
     * [rerollContext].
     *
     * The final physical roll receives the resolved success value. The initial
     * roll's selected reroll is also annotated with whether its use was allowed or
     * aborted. All observations are then marked as finalized.
     *
     * Returns the command to finalize the dice, or `null` when chance-data
     * collection is disabled or the context contains no observations to finalize.
     */
    internal fun finalizeRerollableChanceObservations(
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

    /**
     * Captures the reroll choices offered by a player's skills and team rerolls for
     * a physical dice roll.
     *
     * Applicability is evaluated for both successful and failed outcomes, so each
     * [ChanceRerollOption] records the branches where it can appear. Logical dice
     * in those choices are translated to their observation result IDs using
     * [resultIdsByDieId], while [observedSuccess] determines whether the option is
     * currently usable.
     */
    private fun captureChanceRerollOptions(
        state: Game,
        rules: Rules,
        rollType: DiceRollType,
        team: Team,
        dicePool: List<DieRoll<*>>,
        resultIdsByDieId: Map<DieId, ChanceResultId>,
        observedSuccess: Boolean?,
    ): List<ChanceRerollOption> {
        val sources = buildList {
            addAll(team.rerolls)
        }.distinctBy { it.id }

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

    /**
     * Creates a self-contained chance-data snapshot of this reroll source.
     *
     * The snapshot includes conditional tests associated with using the source,
     * such as Team Mascot.
     */
    private fun RerollSource.toChanceSnapshot(
        rules: Rules,
        team: Team,
    ): ChanceRerollSource {
        val reroll = this
        val tests = buildList {
            when (reroll) {
                is BB2025TeamReroll -> {
                    when (reroll) {
                        is BrilliantCoachingReroll,
                        is ExtraTeamTrainingReroll,
                        is LeaderTeamReroll,
                        is StandardTeamReroll -> { /* Do nothing */ }
                        is TeamMascotReroll -> {
                            add(
                                ChanceRerollTest(
                                    rollType = DiceRollType.TEAM_MASCOT,
                                    dieSides = D6Result.SIDES,
                                    successTarget = TeamMascotReroll.TARGET,
                                    effect = ChanceRerollTestEffect.ALLOWS_REROLL,
                                ),
                            )
                        }
                    }

                }
                else -> { /* Do nothing */}
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

    /**
     * Captures enough metadata to ensure we can detect if a reroll boundary has
     * been reached, i.e., those lifetimes define in [Duration].
     *
     * TOOD This is not enough to capture everything, e.g. [Duration.END_OF_ACTION]
     * (as a player might have multiple actions) or [Duration.SPECIAL]
     */
    private fun chanceScope(state: Game, team: Team): ChanceObservationScope {
        return ChanceObservationScope(
            half = state.halfNo,
            drive = state.driveNo,
            team = team.id,
            turn = team.turnMarker,
            player = null,
        )
    }

    /**
     * Data wrapper for moving data between Roll Contexts and calculating
     * the final re-roll type (if any).
     *
     * This is a copy of [RerollData] but tailored for team rerolls, like Mascot.
     */
    data class TeamRerollData(
        val team: Team,
        val roll: DieRoll<*>,
        val isSuccess: Boolean?
    )
}
