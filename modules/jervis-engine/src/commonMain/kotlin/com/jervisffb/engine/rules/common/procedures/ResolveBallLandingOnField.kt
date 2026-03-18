package com.jervisffb.engine.rules.common.procedures

import com.jervisffb.engine.actions.CancelWhenReady
import com.jervisffb.engine.actions.Continue
import com.jervisffb.engine.actions.ContinueWhenReady
import com.jervisffb.engine.actions.GameAction
import com.jervisffb.engine.actions.GameActionDescriptor
import com.jervisffb.engine.actions.PlayerSelected
import com.jervisffb.engine.actions.PlayersSelected
import com.jervisffb.engine.actions.SelectPlayer
import com.jervisffb.engine.actions.SelectPlayers
import com.jervisffb.engine.commands.Command
import com.jervisffb.engine.commands.SetBallState
import com.jervisffb.engine.commands.SetSkillUsed
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
import com.jervisffb.engine.model.Ball
import com.jervisffb.engine.model.BallState
import com.jervisffb.engine.model.Game
import com.jervisffb.engine.model.Player
import com.jervisffb.engine.model.Team
import com.jervisffb.engine.model.context.CatchContext
import com.jervisffb.engine.model.context.ProcedureContext
import com.jervisffb.engine.model.context.getContext
import com.jervisffb.engine.model.context.getContextOrNull
import com.jervisffb.engine.model.isSkillAvailable
import com.jervisffb.engine.reports.ReportSkillUsed
import com.jervisffb.engine.rules.Rules
import com.jervisffb.engine.rules.common.skills.SkillType
import com.jervisffb.engine.utils.INVALID_ACTION
import com.jervisffb.engine.utils.INVALID_GAME_STATE
import kotlinx.collections.immutable.PersistentList
import kotlinx.collections.immutable.persistentListOf

data class DivingCatchContext(
    val ball: Ball,
    // Which players have been selected for being able to use Diving Catch
    val selectedPlayers: PersistentList<Player> = persistentListOf(),
    // When resolving Diving Catch for players in order, this is the current player being resolved.
    val currentPlayer: Player? = null
): ProcedureContext

/**
 * Procedure handling a ball landing on a square on the field, either occupied
 * or not.
 *
 * This unifies handling the ball landing from the Pass Action, Throw-In, or
 * Kick-off.
 *
 * See [com.jervisffb.engine.rules.bb2025.procedures.actions.pass.PassStep.ResolveBounceOrCatch]
 * See [ThrowIn.ResolveLandOnField]
 * See [TheKickOffEvent.ResolveBallLanding]
 */
object ResolveBallLandingOnField: Procedure() {
    override val initialNode: Node = DetermineIfCatchIsPossible
    override fun onEnterProcedure(state: Game, rules: Rules): Command? = null
    override fun onExitProcedure(state: Game, rules: Rules): Command? {
        // If any player has used Diving Catch, it can now be used again
        val context = state.getContextOrNull<DivingCatchContext>()
        val commands = listOf(state.homeTeam, state.awayTeam).flatMap { team ->
            team.mapNotNull { player ->
                val divingCatchSkill = player.getSkillOrNull(SkillType.DIVING_CATCH)
                when (divingCatchSkill?.used == true) {
                    true -> SetSkillUsed(player, divingCatchSkill, false)
                    false -> null
                }
            }
        }
        return compositeCommandOf(
            if (context != null) RemoveContext(context) else null,
            when (commands.isNotEmpty()) {
                true -> compositeCommandOf(commands)
                false -> null
            }
        )
    }
    override fun isValid(state: Game, rules: Rules) {
        val ball = state.currentBallOrNull() ?: INVALID_GAME_STATE("Missing current ball")
        if (!ball.coordinates.isOnField(rules)) INVALID_GAME_STATE("Ball is not on the field: $ball")
    }

    object DetermineIfCatchIsPossible: ComputationNode() {
        override fun apply(state: Game, rules: Rules): Command {
            val ball = state.currentBall()
            val playerInSquare = state.field[ball.coordinates].player

            // If there is a player in the landing square, they prevent the use of Diving Catch
            if (playerInSquare?.let { rules.canCatch(it) } == true) {
                return GotoNode(CatchBallInLandingSquare)
            }

            // If a player with Diving Catch is adjacent, they get a chance to use Diving Catch before the ball lands.
            val divingCatchAvailable = ball.coordinates.getSurroundingCoordinates(rules, distance = 1, includeOutOfBounds = false)
                .mapNotNull { state.field[it].player }
                .any { it.isSkillAvailable(SkillType.DIVING_CATCH) }

            if (divingCatchAvailable) {
                return compositeCommandOf(
                    AddContext(DivingCatchContext(ball)),
                    GotoNode(InactiveTeamChoosesDivingCatchPlayers),
                )
            }

            // No player is available to catch it, so ball will bounce
            return GotoNode(BounceBall)
        }
    }

    object CatchBallInLandingSquare: ParentNode() {
        override fun onEnterNode(state: Game, rules: Rules): Command {
            val ball = state.currentBall()
            val player = state.field[ball.coordinates].player ?: INVALID_GAME_STATE("Missing player on: ${ball.coordinates}")
            return AddContext(CatchContext(player, ball))
        }
        override fun getChildProcedure(state: Game, rules: Rules): Procedure = Catch
        override fun onExitNode(state: Game, rules: Rules): Command {
            // Catch will handle all follow-up steps until Ball is at rest, so just exit here
            return compositeCommandOf(
                RemoveContext<CatchContext>(),
                ExitProcedure()
            )
        }
    }

    // The opposing team coach, first select which players should be eligible to
    // use Diving Catch.
    // If we are during a Kick-off, technically, players on the kicking team can use Diving Catch,
    // but it will result in a touchback.
    object InactiveTeamChoosesDivingCatchPlayers: ActionNode() {
        override fun actionOwner(state: Game, rules: Rules): Team {
            // If there is no active team, we are during Kick-off
            return state.inactiveTeam ?: state.kickingTeam
        }
        override fun getAvailableActions(state: Game, rules: Rules): List<GameActionDescriptor> {
            val ball = state.currentBall()
            val players = ball.coordinates.getSurroundingCoordinates(rules, distance = 1, includeOutOfBounds = false)
                .mapNotNull { state.field[it].player }
                .filter { it.team == (state.inactiveTeam ?: state.kickingTeam)}
                .filter { it.isSkillAvailable(SkillType.DIVING_CATCH) }

            return when (players.isEmpty()) {
                true -> listOf(ContinueWhenReady)
                false -> listOf(SelectPlayers(players), CancelWhenReady)
            }
        }
        override fun applyAction(action: GameAction, state: Game, rules: Rules): Command {
            return buildCompositeCommand {
                if (action is PlayersSelected) {
                    val divingCatchContext = state.getContext<DivingCatchContext>()
                    val players = action.getPlayers(state)
                    add(UpdateContext(divingCatchContext.copy(
                        selectedPlayers = divingCatchContext.selectedPlayers.addAll(players)
                    )),
                    )
                }
                add(GotoNode(ActiveTeamChoosesDivingCatchPlayers))
            }
        }
    }

    // The active team coach, then select which players should be eligible to
    // use Diving Catch
    object ActiveTeamChoosesDivingCatchPlayers: ActionNode() {
        override fun actionOwner(state: Game, rules: Rules): Team {
            // If there is no active team, we are during Kick-off
            return state.activeTeam ?: state.receivingTeam
        }
        override fun getAvailableActions(state: Game, rules: Rules): List<GameActionDescriptor> {
            val ball = state.currentBall()
            val players = ball.coordinates.getSurroundingCoordinates(rules, distance = 1, includeOutOfBounds = false)
                .mapNotNull { state.field[it].player }
                .filter { it.team == (state.activeTeam ?: state.receivingTeam) }
                .filter { it.isSkillAvailable(SkillType.DIVING_CATCH) }

            return when (players.isNotEmpty()) {
                true -> listOf(SelectPlayers(players), CancelWhenReady)
                false -> listOf(ContinueWhenReady)
            }
        }
        override fun applyAction(action: GameAction, state: Game, rules: Rules): Command {
            val context = state.getContext<DivingCatchContext>()
            return when (action) {
                is PlayersSelected -> {
                    val players = action.getPlayers(state)
                    compositeCommandOf(
                        UpdateContext(context.copy(
                            selectedPlayers = context.selectedPlayers.addAll(players)
                        )),
                        GotoNode(ChooseDivingCatchPlayer)
                    )
                }
                else -> {
                    when ((context).selectedPlayers.isEmpty()) {
                        true -> GotoNode(BounceBall)
                        false -> GotoNode(ChooseDivingCatchPlayer)
                    }
                }
            }
        }
    }

    // Once the pool of al available players have been established. The active team coach
    // decides the order in which it is resolved (including the opposing team).
    object ChooseDivingCatchPlayer: ActionNode() {
        override fun actionOwner(state: Game, rules: Rules): Team {
            // If there is no active team, we are during Kick-off
            return state.activeTeam ?: state.receivingTeam
        }
        override fun getAvailableActions(state: Game, rules: Rules): List<GameActionDescriptor> {
            val context = state.getContext<DivingCatchContext>()
            val players = context.selectedPlayers
            return when (players.isNotEmpty()) {
                true -> listOf(SelectPlayer.fromPlayers(players))
                false -> listOf(ContinueWhenReady)
            }
        }
        override fun applyAction(action: GameAction, state: Game, rules: Rules): Command {
            return when (action) {
                is PlayerSelected -> {
                    val player = action.getPlayer(state)
                    val divingContext = state.getContext<DivingCatchContext>()
                    compositeCommandOf(
                        ReportSkillUsed(player, SkillType.DIVING_CATCH),
                        SetSkillUsed(player, player.getSkill(SkillType.DIVING_CATCH), used = true),
                        UpdateContext(divingContext.copy(
                            currentPlayer = player
                        )),
                        AddContext(CatchContext(player, state.currentBall())),
                        GotoNode(CatchBallUsingDivingCatch)
                    )
                }
                Continue -> GotoNode(BounceBall)
                else -> INVALID_ACTION(action)
            }
        }
    }

    object CatchBallUsingDivingCatch: ParentNode() {
        override fun getChildProcedure(state: Game, rules: Rules): Procedure = Catch
        override fun onExitNode(state: Game, rules: Rules): Command {
            val ball = state.currentBall()
            val divingContext = state.getContext<DivingCatchContext>()
            return compositeCommandOf(
                RemoveContext<CatchContext>(),
                UpdateContext(divingContext.copy(
                    selectedPlayers = divingContext.selectedPlayers.remove(divingContext.currentPlayer!!),
                    currentPlayer = null
                )),
                when (ball.state == BallState.CARRIED) {
                    true -> ExitProcedure()
                    false -> GotoNode(ChooseDivingCatchPlayer)
                }
            )
        }
    }

    object BounceBall: ParentNode() {
        override fun onEnterNode(state: Game, rules: Rules): Command {
            val ball = state.currentBall()
            return SetBallState.bouncing(ball)
        }
        override fun getChildProcedure(state: Game, rules: Rules): Procedure = Bounce
        override fun onExitNode(state: Game, rules: Rules): Command {
            // Bounce will handle all follow-up steps until Ball is at rest, so just exit here
            return ExitProcedure()
        }
    }
}
