package com.jervisffb.engine.bb2025.procedures.actions.ballandchain

import com.jervisffb.engine.actions.DirectionSelected
import com.jervisffb.engine.actions.EndAction
import com.jervisffb.engine.actions.EndActionWhenReady
import com.jervisffb.engine.actions.GameAction
import com.jervisffb.engine.actions.GameActionDescriptor
import com.jervisffb.engine.actions.SelectDirection
import com.jervisffb.engine.bb2025.context.BallAndChainContext
import com.jervisffb.engine.commands.Command
import com.jervisffb.engine.commands.SetSkillUsed
import com.jervisffb.engine.commands.buildCompositeCommand
import com.jervisffb.engine.commands.compositeCommandOf
import com.jervisffb.engine.commands.context.AddContext
import com.jervisffb.engine.commands.context.RemoveContext
import com.jervisffb.engine.commands.context.UpdateContext
import com.jervisffb.engine.commands.fsm.ExitProcedure
import com.jervisffb.engine.commands.fsm.GotoNode
import com.jervisffb.engine.common.commands.SetPlayerRushesLeft
import com.jervisffb.engine.common.context.ActivatePlayerContext
import com.jervisffb.engine.common.procedures.getResetPlayerTemporaryModifiersCommands
import com.jervisffb.engine.common.procedures.getSetPlayerRushesCommand
import com.jervisffb.engine.common.reports.ReportSkillUsed
import com.jervisffb.engine.common.utils.endActionImmediately
import com.jervisffb.engine.fsm.ActionNode
import com.jervisffb.engine.fsm.Node
import com.jervisffb.engine.fsm.ParentNode
import com.jervisffb.engine.fsm.Procedure
import com.jervisffb.engine.model.Direction
import com.jervisffb.engine.model.Game
import com.jervisffb.engine.model.Team
import com.jervisffb.engine.model.context.getContext
import com.jervisffb.engine.model.isSkillAvailable
import com.jervisffb.engine.rules.Rules
import com.jervisffb.engine.rules.SPRINT_EXTRA_RUSHES
import com.jervisffb.engine.rules.common.skills.Duration
import com.jervisffb.engine.rules.common.skills.SkillType
import com.jervisffb.engine.utils.INVALID_ACTION

/**
 * Procedure controlling the entire Ball & Chain special action.
 *
 * See page 124 in the BB2025 rulebook.
 */
object BallAndChainAction : Procedure() {
    override val initialNode: Node = SelectTemplateOrientation
    override fun onEnterProcedure(state: Game, rules: Rules): Command {
        return getSetPlayerRushesCommand(rules, state.activePlayerOrThrow())
    }
    override fun onExitProcedure(state: Game, rules: Rules): Command {
        return compositeCommandOf(
            *getResetPlayerTemporaryModifiersCommands(state, rules, state.activePlayerOrThrow(), Duration.END_OF_ACTION),
        )
    }
    object SelectTemplateOrientation : ActionNode() {
        override fun actionOwner(state: Game, rules: Rules): Team = state.activePlayerOrThrow().team
        override fun getAvailableActions(state: Game, rules: Rules): List<GameActionDescriptor> = buildList {
            val player = state.activePlayerOrThrow()
            val hasMovesLeft = (player.movesLeft > 0 || player.rushesLeft > 0 || player.isSkillAvailable(SkillType.SPRINT))
            if (rules.isStanding(player) && hasMovesLeft) {
                add(SelectDirection(player.coordinates, listOf(Direction.UP, Direction.DOWN, Direction.LEFT, Direction.RIGHT)))
            }
            add(EndActionWhenReady)
        }

        override fun applyAction(action: GameAction, state: Game, rules: Rules): Command {
            return when (action) {
                EndAction -> ExitProcedure()
                is DirectionSelected -> buildCompositeCommand {
                    val player = state.activePlayerOrThrow()
                    if (player.movesLeft == 0 && player.rushesLeft == 0 && player.isSkillAvailable(SkillType.SPRINT)) {
                        addAll(
                            ReportSkillUsed(player, SkillType.SPRINT),
                            SetSkillUsed(player, player.getSkill(SkillType.SPRINT), true),
                            SetPlayerRushesLeft(player, player.rushesLeft + SPRINT_EXTRA_RUSHES),
                        )
                    }
                    addAll(
                        UpdateContext(state.getContext<ActivatePlayerContext>().copyWithMarkedAction(true)),
                        AddContext(BallAndChainContext(player, action.direction)),
                        GotoNode(ResolveStep),
                    )
                }
                else -> INVALID_ACTION(action)
            }
        }
    }

    object ResolveStep : ParentNode() {
        override fun getChildProcedure(state: Game, rules: Rules): Procedure = BallAndChainStep
        override fun onExitNode(state: Game, rules: Rules): Command {
            val player = state.activePlayerOrThrow()
            return compositeCommandOf(
                RemoveContext<BallAndChainContext>(),
                when (state.endActionImmediately() || !rules.isStanding(player) || !player.location.isOnPitch(rules)) {
                    true -> ExitProcedure()
                    false -> GotoNode(SelectTemplateOrientation)
                },
            )
        }
    }
}
