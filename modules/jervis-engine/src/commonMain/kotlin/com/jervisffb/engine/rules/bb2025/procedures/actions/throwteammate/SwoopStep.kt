package com.jervisffb.engine.rules.bb2025.procedures.actions.throwteammate

import com.jervisffb.engine.actions.CancelWhenReady
import com.jervisffb.engine.actions.Confirm
import com.jervisffb.engine.actions.ConfirmWhenReady
import com.jervisffb.engine.actions.ContinueWhenReady
import com.jervisffb.engine.actions.DirectionSelected
import com.jervisffb.engine.actions.GameAction
import com.jervisffb.engine.actions.GameActionDescriptor
import com.jervisffb.engine.actions.SelectDirection
import com.jervisffb.engine.commands.Command
import com.jervisffb.engine.commands.compositeCommandOf
import com.jervisffb.engine.commands.context.UpdateContext
import com.jervisffb.engine.commands.fsm.ExitProcedure
import com.jervisffb.engine.commands.fsm.GotoNode
import com.jervisffb.engine.ext.d6
import com.jervisffb.engine.fsm.ActionNode
import com.jervisffb.engine.fsm.ComputationNode
import com.jervisffb.engine.fsm.Node
import com.jervisffb.engine.fsm.ParentNode
import com.jervisffb.engine.fsm.Procedure
import com.jervisffb.engine.model.Direction
import com.jervisffb.engine.model.Game
import com.jervisffb.engine.model.Player
import com.jervisffb.engine.model.Team
import com.jervisffb.engine.model.context.ProcedureContext
import com.jervisffb.engine.model.context.assertContext
import com.jervisffb.engine.model.context.getContext
import com.jervisffb.engine.model.isSkillAvailable
import com.jervisffb.engine.model.locations.PitchCoordinate
import com.jervisffb.engine.reports.ReportSkillUsed
import com.jervisffb.engine.rules.Rules
import com.jervisffb.engine.rules.bb2025.skills.Swoop
import com.jervisffb.engine.rules.common.procedures.D3DieRoll
import com.jervisffb.engine.rules.common.procedures.D6DieRoll
import com.jervisffb.engine.rules.common.skills.SkillType
import com.jervisffb.engine.utils.INVALID_GAME_STATE

data class SwoopContext(
    val player: Player,
    val selectedDirection: Direction? = null,
    val directionRoll: D3DieRoll? = null,
    val rolledDirection: Direction? = null,
    val distanceRoll: D6DieRoll? = null,
    val landsAt: PitchCoordinate? = null,
    // If the player lands outside the pitch, this is the location they left the field.
    val outOfBoundsAt: PitchCoordinate? = null,
): ProcedureContext {
    val coordinate: PitchCoordinate = player.coordinates
}

/**
 * This player controls the usage of the [Swoop] skill, up until the point where
 * the player is about to land.
 *
 * All results are stored in [SwoopContext] and it is up to the caller to determine
 * what happens with the player based on them, including moving them.
 */
object SwoopStep: Procedure() {
    override val initialNode: Node = ChooseToUseSwoop
    override fun onEnterProcedure(state: Game, rules: Rules): Command? = null
    override fun onExitProcedure(state: Game, rules: Rules): Command? = null
    override fun isValid(state: Game, rules: Rules) {
        state.assertContext<SwoopContext>()
    }

    object ChooseToUseSwoop: ActionNode() {
        override fun actionOwner(state: Game, rules: Rules): Team {
            return state.getContext<SwoopContext>().player.team
        }
        override fun getAvailableActions(state: Game, rules: Rules): List<GameActionDescriptor> {
            val context = state.getContext<SwoopContext>()
            val hasSkill = context.player.isSkillAvailable(SkillType.SWOOP)
            return when (hasSkill) {
                true -> listOf(ConfirmWhenReady, CancelWhenReady)
                false -> listOf(ContinueWhenReady)
            }
        }
        override fun applyAction(action: GameAction, state: Game, rules: Rules): Command {
            val context = state.getContext<SwoopContext>()
            val usedSkill = (action == Confirm)
            return when (usedSkill) {
                true -> compositeCommandOf(
                    ReportSkillUsed(context.player, SkillType.SWOOP),
                    GotoNode(ChooseDirection)
                )
                false -> ExitProcedure()
            }
        }
    }

    object ChooseDirection: ActionNode() {
        override fun actionOwner(state: Game, rules: Rules): Team {
            val context = state.getContext<SwoopContext>()
            return context.player.team
        }
        override fun getAvailableActions(state: Game, rules: Rules): List<GameActionDescriptor> {
            val context = state.getContext<SwoopContext>()
            val player = context.player
            val coordinate = player.coordinates

            val directions = buildList {
                if (coordinate.x > 0) add(Direction(-1, 0))
                if (coordinate.x < rules.pitchWidth - 1) add(Direction(1, 0))
                if (coordinate.y > 0) add(Direction(0, -1))
                if (coordinate.y < rules.pitchHeight - 1) add(Direction(0, 1))
            }
            return listOf(SelectDirection(coordinate, directions))
        }
        override fun applyAction(action: GameAction, state: Game, rules: Rules): Command {
            val context = state.getContext<SwoopContext>()
            val selectedDirection = (action as DirectionSelected)
            return compositeCommandOf(
                UpdateContext(context.copy(selectedDirection = selectedDirection.direction)),
                GotoNode(RollDirection)
            )
        }
    }

    object RollDirection: ParentNode() {
        override fun getChildProcedure(state: Game, rules: Rules): Procedure = SwoopDirectionRoll
        override fun onExitNode(state: Game, rules: Rules): Command {
            // If the direction causes out-of-bounds even when rolling 1, we skip rolling distance.
            val context = state.getContext<SwoopContext>()
            val isOutOfBounds = context.coordinate.move(context.rolledDirection!!, steps = 1).isOutOfBounds(rules)
            return when (isOutOfBounds) {
                true -> compositeCommandOf(
                    UpdateContext(context.copy(distanceRoll = D6DieRoll.create(state, 1.d6))),
                    GotoNode(DetermineSwoopResult)
                )
                false -> GotoNode(RollDistance)
            }
        }
    }

    object RollDistance: ParentNode() {
        override fun getChildProcedure(state: Game, rules: Rules): Procedure = SwoopDistanceRoll
        override fun onExitNode(state: Game, rules: Rules): Command {
            return GotoNode(DetermineSwoopResult)
        }
    }

    object DetermineSwoopResult: ComputationNode() {
        override fun apply(state: Game, rules: Rules): Command {
            val context = state.getContext<SwoopContext>()
            val direction = context.rolledDirection ?: INVALID_GAME_STATE("Missing rolled direction: $context")
            val distance = context.distanceRoll?.result?.value ?: INVALID_GAME_STATE("Missing distance roll result: $context")
            var landsAt = context.coordinate.move(direction, steps = distance)
            var outOfBoundsAt: PitchCoordinate? = null
            if (landsAt.isOutOfBounds(rules)) {
                // Create a direct line between player and location. This is the line the player follows when thrown
                // So, we just need to find where they go out-of-bounds using this.
                // This logic might need to be revisited for Dungeon Bowl.
                val exit = rules.pathFinder.getStraightLine(state, context.coordinate, landsAt)
                    .zipWithNext()
                    .firstOrNull { (from, to) ->
                        from.isOnPitch(rules) && !to.isOnPitch(rules)
                    }
                outOfBoundsAt = exit?.first ?: INVALID_GAME_STATE("Could not find out-of-bounds location for: $context")
                landsAt = exit.second
            }
            return compositeCommandOf(
                UpdateContext(context.copy(
                    landsAt = landsAt,
                    outOfBoundsAt = outOfBoundsAt
                )),
                ExitProcedure()
            )
        }
    }
}
