package com.jervisffb.engine.common.procedures.inducements.spells

import com.jervisffb.engine.actions.GameAction
import com.jervisffb.engine.actions.GameActionDescriptor
import com.jervisffb.engine.actions.PitchSquareSelected
import com.jervisffb.engine.actions.SelectPitchLocation
import com.jervisffb.engine.actions.TargetSquare
import com.jervisffb.engine.commands.Command
import com.jervisffb.engine.commands.compositeCommandOf
import com.jervisffb.engine.commands.context.AddContext
import com.jervisffb.engine.commands.context.RemoveContext
import com.jervisffb.engine.commands.fsm.ExitProcedure
import com.jervisffb.engine.commands.fsm.GotoNode
import com.jervisffb.engine.common.comm.SetInducementEffectUsed
import com.jervisffb.engine.common.commands.SetTurnOver
import com.jervisffb.engine.common.context.ResolveInducementEffectsContext
import com.jervisffb.engine.common.context.RiskingInjuryContext
import com.jervisffb.engine.common.procedures.tables.injury.RiskingInjuryMode
import com.jervisffb.engine.common.reports.ReportFireballHit
import com.jervisffb.engine.fsm.ActionNode
import com.jervisffb.engine.fsm.Node
import com.jervisffb.engine.fsm.ParentNode
import com.jervisffb.engine.fsm.Procedure
import com.jervisffb.engine.fsm.castAction
import com.jervisffb.engine.model.Game
import com.jervisffb.engine.model.Player
import com.jervisffb.engine.model.Team
import com.jervisffb.engine.model.TurnOver
import com.jervisffb.engine.model.context.ProcedureContext
import com.jervisffb.engine.model.context.getContext
import com.jervisffb.engine.model.locations.PitchCoordinate
import com.jervisffb.engine.rules.Rules
import com.jervisffb.engine.rules.common.procedures.D6DieRoll
import com.jervisffb.engine.utils.requireGameState
import kotlinx.collections.immutable.PersistentList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toPersistentList

data class FireBallContext(
    val target: PitchCoordinate,
    val potentialPlayers: PersistentList<Player> = persistentListOf(),
    val rolls: PersistentList<Hit> = persistentListOf(),
): ProcedureContext {
    data class Hit(
        val player: Player, val roll: D6DieRoll, val isSuccess: Boolean
    ) {
        val isStanding = player.team.game.rules.isStanding(player)
    }
}

/**
 * Procedure handling the effect of using the "Fireball" Spell available to
 * Sports-Wizards.
 *
 * See page 149 in the BB2025 Rulebook
 */
object FireBallProcedure: Procedure() {
    override val initialNode: Node = SelectLocation
    override fun onEnterProcedure(state: Game, rules: Rules): Command? = null
    override fun onExitProcedure(state: Game, rules: Rules): Command {
        val inducementContext = state.getContext<ResolveInducementEffectsContext>()
        return compositeCommandOf(
            SetInducementEffectUsed(inducementContext.inducement, used = true),
            RemoveContext<FireBallContext>()
        )
    }
    override fun isValid(state: Game, rules: Rules) {
        state.getContext<ResolveInducementEffectsContext>().let {
            requireGameState(!it.inducement.used) { "Inducement already used: ${it.inducement}" }
        }
    }

    object SelectLocation: ActionNode() {
        override fun actionOwner(state: Game, rules: Rules): Team = state.getContext<ResolveInducementEffectsContext>().team
        override fun getAvailableActions(state: Game, rules: Rules): List<GameActionDescriptor> {
            // All squares on the pitch are valid targets
            val targetSquares = state.pitch.map { square ->
                TargetSquare(square, type = TargetSquare.Type.FIREBALL)
            }
            return listOf(SelectPitchLocation(targetSquares))
        }
        override fun applyAction(action: GameAction, state: Game, rules: Rules): Command {
            return castAction<PitchSquareSelected>(action) { square ->
                val inducementContext = state.getContext<ResolveInducementEffectsContext>()
                val target = square.coordinate
                val potentialPlayers = (listOf(target) + target.getSurroundingCoordinates(rules, distance = 1))
                    .mapNotNull { state.pitch[it].player }
                val fireballContext = FireBallContext(
                    target = square.coordinate,
                    potentialPlayers = potentialPlayers.asReversed().toPersistentList()
                )
                compositeCommandOf(
                    ReportFireballHit(inducementContext.team, potentialPlayers.size),
                    AddContext(fireballContext),
                    when (potentialPlayers.isNotEmpty()) {
                        true -> GotoNode(RollToHit)
                        false -> ExitProcedure()
                    }
                )
            }
        }
    }

    object RollToHit: ParentNode() {
        override fun getChildProcedure(state: Game, rules: Rules): Procedure = FireballRoll
        override fun onExitNode(state: Game, rules: Rules): Command {
            val context = state.getContext<FireBallContext>()
            val playersMissing = context.potentialPlayers.isNotEmpty()
            val isHit = context.rolls.last().isSuccess
            return when {
                isHit -> GotoNode(PlayerHit)
                playersMissing -> GotoNode(RollToHit)
                else -> ExitProcedure()
            }
        }
    }

    object PlayerHit: ParentNode() {
        override fun onEnterNode(state: Game, rules: Rules): Command {
            val context = state.getContext<FireBallContext>()
            val player = context.rolls.last().player
            val injuryContext = RiskingInjuryContext(
                player = player,
                causedBy = null,
                mode = RiskingInjuryMode.FIREBALL
            )
            return AddContext(injuryContext)
        }
        override fun getChildProcedure(state: Game, rules: Rules): Procedure {
            val context = state.getContext<RiskingInjuryContext>()
            return when (rules.isStanding(context.player)) {
                true -> rules.knockedDownStep
                false -> rules.riskingInjuryRoll
            }
        }
        override fun onExitNode(state: Game, rules: Rules): Command {
            val context = state.getContext<RiskingInjuryContext>()
            val fireballContext = state.getContext<FireBallContext>()
            val playerHit = fireballContext.rolls.last()

            // If the player hit was on the active team and knocked down, it is a turnover.
            // Most likely it wil not matter as we are resolving end-of-turn, but
            // it might impact other inducement effects that could trigger rerolls.
            val wasStanding = playerHit.isStanding
            val isStandingAfterHit = rules.isStanding(playerHit.player)
            val moreTargets = fireballContext.potentialPlayers.isNotEmpty()

            return compositeCommandOf(
                RemoveContext(context),
                when (wasStanding && !isStandingAfterHit && !state.isTurnOver()) {
                    true -> SetTurnOver(TurnOver.STANDARD)
                    false -> null
                },
                when (moreTargets) {
                    true -> GotoNode(RollToHit)
                    false -> ExitProcedure()
                }
            )
        }
    }
}
