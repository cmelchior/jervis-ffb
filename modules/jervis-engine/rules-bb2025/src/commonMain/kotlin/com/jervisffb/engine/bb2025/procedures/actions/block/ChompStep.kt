package com.jervisffb.engine.bb2025.procedures.actions.block

import com.jervisffb.engine.actions.EndAction
import com.jervisffb.engine.actions.EndActionWhenReady
import com.jervisffb.engine.actions.GameAction
import com.jervisffb.engine.actions.GameActionDescriptor
import com.jervisffb.engine.actions.PlayerSelected
import com.jervisffb.engine.actions.SelectPlayer
import com.jervisffb.engine.bb2025.procedures.actions.blitz.BlitzAction
import com.jervisffb.engine.bb2025.reports.ReportChompResult
import com.jervisffb.engine.commands.AddPlayerStatusEffect
import com.jervisffb.engine.commands.Command
import com.jervisffb.engine.commands.buildCompositeCommand
import com.jervisffb.engine.commands.compositeCommandOf
import com.jervisffb.engine.commands.context.AddContext
import com.jervisffb.engine.commands.context.RemoveContext
import com.jervisffb.engine.commands.context.UpdateContext
import com.jervisffb.engine.commands.fsm.ExitProcedure
import com.jervisffb.engine.commands.fsm.GotoNode
import com.jervisffb.engine.fsm.ActionNode
import com.jervisffb.engine.fsm.ComputationNode
import com.jervisffb.engine.fsm.Node
import com.jervisffb.engine.fsm.ParentNode
import com.jervisffb.engine.fsm.Procedure
import com.jervisffb.engine.model.Game
import com.jervisffb.engine.model.Player
import com.jervisffb.engine.model.Team
import com.jervisffb.engine.model.context.ActivatePlayerContext
import com.jervisffb.engine.model.context.BlitzActionContext
import com.jervisffb.engine.model.context.ProcedureContext
import com.jervisffb.engine.model.context.assertContext
import com.jervisffb.engine.model.context.getContext
import com.jervisffb.engine.model.context.hasContext
import com.jervisffb.engine.model.isSkillAvailable
import com.jervisffb.engine.model.modifiers.PlayerStatusEffect
import com.jervisffb.engine.reports.ReportSkillUsed
import com.jervisffb.engine.rules.Rules
import com.jervisffb.engine.rules.common.procedures.D6DieRoll
import com.jervisffb.engine.rules.common.procedures.actions.block.FoulAppearanceContext
import com.jervisffb.engine.rules.common.procedures.actions.block.FoulAppearanceRoll
import com.jervisffb.engine.rules.common.skills.SkillType
import com.jervisffb.engine.utils.INVALID_ACTION
import com.jervisffb.engine.utils.INVALID_GAME_STATE

data class ChompContext(
    val attacker: Player,
    val defender: Player? = null,
    val chompRoll: D6DieRoll? = null,
    val isSuccess: Boolean = false,
): ProcedureContext

/**
 * Procedure for handling the "Chomp"-part of a Chomp Special Action.
 * It is in its own procedure, so we can more easily support Block, Blitz and
 * Frenzy.
 *
 * See [ChompAction], [BlitzAction], and each ruleset's `BlockAction`.
 */
object ChompStep: Procedure() {
    override val initialNode: Node = DecideOnFirstStep
    override fun onEnterProcedure(state: Game, rules: Rules): Command? {
        val context = state.getContext<ChompContext>()
        val isBlitz = state.hasContext<BlitzActionContext>()
        return when (isBlitz) {
            true -> ReportSkillUsed(context.attacker, SkillType.MONSTROUS_MOUTH)
            false -> null
        }
    }
    override fun onExitProcedure(state: Game, rules: Rules): Command? = null
    override fun isValid(state: Game, rules: Rules) {
        state.assertContext<ChompContext>()
    }

    // During a Blitz or Frenzy, the target is pre-defined, so we can skip this step
    object DecideOnFirstStep: ComputationNode() {
        override fun apply(state: Game, rules: Rules): Command {
            val context = state.getContext<ChompContext>()
            return when (context.defender != null) {
                true -> GotoNode(CheckForFoulAppearance)
                false -> GotoNode(SelectDefenderOrEndAction)
            }
        }
    }

    object SelectDefenderOrEndAction : ActionNode() {
        override fun actionOwner(state: Game, rules: Rules): Team = state.activePlayer!!.team
        override fun getAvailableActions(state: Game, rules: Rules): List<GameActionDescriptor> {
            val attacker = state.activePlayer!!
            val eligibleDefenders: GameActionDescriptor =
                attacker.coordinates.getSurroundingCoordinates(rules)
                    .filter { state.pitch[it].isOccupied() }
                    .filter { state.pitch[it].player!!.team != attacker.team }
                    .map { state.pitch[it].player!! }
                    .let { SelectPlayer.fromPlayers(it) }

            return listOf(eligibleDefenders, EndActionWhenReady)
        }
        override fun applyAction(action: GameAction, state: Game, rules: Rules): Command {
            return when (action) {
                EndAction -> ExitProcedure()
                is PlayerSelected -> {
                    val context = state.getContext<ChompContext>()
                    val player = action.getPlayer(state)
                    compositeCommandOf(
                        UpdateContext(context.copy(
                            defender = player,
                        )),
                        GotoNode(CheckForFoulAppearance),
                    )
                }
                else -> INVALID_ACTION(action)
            }
        }
    }

    object CheckForFoulAppearance: ParentNode() {
        override fun skipNodeFor(state: Game, rules: Rules): Node? {
            val context = state.getContext<ChompContext>()
            val hasFoulAppearance = context.defender?.isSkillAvailable(SkillType.FOUL_APPEARANCE) ?: error("Missing defender: $context")
            return when (hasFoulAppearance) {
                true -> null
                false -> RollForChomp
            }
        }
        override fun onEnterNode(state: Game, rules: Rules): Command {
            val chompContext = state.getContext<ChompContext>()
            val foulAppearanceContext = FoulAppearanceContext(chompContext.attacker, chompContext.defender!!)
            return AddContext(foulAppearanceContext)
        }
        override fun getChildProcedure(state: Game, rules: Rules): Procedure = FoulAppearanceRoll
        override fun onExitNode(state: Game, rules: Rules): Command {
            val activePlayerContext = state.getContext<ActivatePlayerContext>()
            val context = state.getContext<FoulAppearanceContext>()
            return buildCompositeCommand {
                add(RemoveContext<FoulAppearanceContext>())
                when (context.isSuccess) {
                    true -> add(GotoNode(RollForChomp))
                    // Failing Foul Appearance ends the activation immediately.
                    false -> addAll(
                        UpdateContext(activePlayerContext.copy(activationEndsImmediately = true)),
                        ExitProcedure()
                    )
                }
            }
        }
    }

    object RollForChomp: ParentNode() {
        override fun getChildProcedure(state: Game, rules: Rules): Procedure = ChompRoll
        override fun onExitNode(state: Game, rules: Rules): Command {
            val context = state.getContext<ChompContext>()
            val defender = context.defender ?: INVALID_GAME_STATE("Missing defender: $context")
            return compositeCommandOf(
                ReportChompResult(context),
                when (context.isSuccess) {
                    true -> AddPlayerStatusEffect(defender, PlayerStatusEffect.chomped(context.attacker))
                    false -> null
                },
                ExitProcedure()
            )
        }
    }
}
