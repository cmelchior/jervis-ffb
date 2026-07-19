package com.jervisffb.engine.rules.bb2025.procedures.skills

import com.jervisffb.engine.commands.AddPlayerStatusEffect
import com.jervisffb.engine.commands.Command
import com.jervisffb.engine.commands.SetHasTackleZones
import com.jervisffb.engine.commands.buildCompositeCommand
import com.jervisffb.engine.commands.compositeCommandOf
import com.jervisffb.engine.commands.context.AddContext
import com.jervisffb.engine.commands.context.RemoveContext
import com.jervisffb.engine.commands.context.UpdateContext
import com.jervisffb.engine.commands.fsm.ExitProcedure
import com.jervisffb.engine.commands.fsm.GotoNode
import com.jervisffb.engine.fsm.Node
import com.jervisffb.engine.fsm.ParentNode
import com.jervisffb.engine.fsm.Procedure
import com.jervisffb.engine.model.Game
import com.jervisffb.engine.model.context.ActivatePlayerContext
import com.jervisffb.engine.model.context.HypnoticGazeContext
import com.jervisffb.engine.model.context.assertContext
import com.jervisffb.engine.model.context.getContext
import com.jervisffb.engine.model.isSkillAvailable
import com.jervisffb.engine.model.modifiers.PlayerStatusEffect
import com.jervisffb.engine.rules.Rules
import com.jervisffb.engine.rules.common.procedures.actions.block.FoulAppearanceContext
import com.jervisffb.engine.rules.common.procedures.actions.block.FoulAppearanceRoll
import com.jervisffb.engine.rules.common.skills.SkillType
import com.jervisffb.engine.utils.INVALID_GAME_STATE

/**
 * Procedure implementing the gaze attempt for a [HypnoticGazeAction].
 *
 * See page 129 in the BB2025 rulebook.
 */
object HypnoticGazeStep : Procedure() {
    override val initialNode: Node = CheckForFoulAppearance
    override fun onEnterProcedure(state: Game, rules: Rules): Command? = null
    override fun onExitProcedure(state: Game, rules: Rules): Command {
        val activateContext = state.getContext<ActivatePlayerContext>()
        return UpdateContext(activateContext.copy(activationEndsImmediately = true))
    }
    override fun isValid(state: Game, rules: Rules) = state.assertContext<HypnoticGazeContext>()

    object CheckForFoulAppearance : ParentNode() {
        override fun skipNodeFor(state: Game, rules: Rules): Node? {
            val context = state.getContext<HypnoticGazeContext>()
            val target = context.target ?: INVALID_GAME_STATE("Missing target: $context")
            return when (target.isSkillAvailable(SkillType.FOUL_APPEARANCE)) {
                true -> null
                false -> RollForHypnoticGaze
            }
        }
        override fun onEnterNode(state: Game, rules: Rules): Command {
            val context = state.getContext<HypnoticGazeContext>()
            return AddContext(FoulAppearanceContext(context.gazer, context.target!!))
        }
        override fun getChildProcedure(state: Game, rules: Rules): Procedure = FoulAppearanceRoll
        override fun onExitNode(state: Game, rules: Rules): Command {
            val foulAppearanceContext = state.getContext<FoulAppearanceContext>()
            return buildCompositeCommand {
                add(RemoveContext(foulAppearanceContext))
                when (foulAppearanceContext.isSuccess) {
                    true -> add(GotoNode(RollForHypnoticGaze))
                    // A failed Foul Appearance ends the action; FoulAppearanceRoll already
                    // set `activationEndsImmediately`/`markActionAsUsed`, so just exit.
                    false -> add(ExitProcedure())
                }
            }
        }
    }

    object RollForHypnoticGaze : ParentNode() {
        override fun getChildProcedure(state: Game, rules: Rules): Procedure = HypnoticGazeRoll
        override fun onExitNode(state: Game, rules: Rules): Command {
            val context = state.getContext<HypnoticGazeContext>()
            val target = context.target ?: INVALID_GAME_STATE("Missing target: $context")
            val updatedContext = context.copy(hasGazed = true)
            return if (context.isSuccess) {
                compositeCommandOf(
                    UpdateContext(updatedContext),
                    AddPlayerStatusEffect(target, PlayerStatusEffect.distracted()),
                    SetHasTackleZones(target, hasTackleZones = false),
                    ExitProcedure()
                )
            } else {
                compositeCommandOf(
                    UpdateContext(updatedContext),
                    ExitProcedure()
                )
            }
        }
    }
}
