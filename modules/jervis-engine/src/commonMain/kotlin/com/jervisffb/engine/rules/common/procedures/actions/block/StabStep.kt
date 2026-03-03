package com.jervisffb.engine.rules.common.procedures.actions.block

import com.jervisffb.engine.actions.EndAction
import com.jervisffb.engine.actions.EndActionWhenReady
import com.jervisffb.engine.actions.GameAction
import com.jervisffb.engine.actions.GameActionDescriptor
import com.jervisffb.engine.actions.PlayerSelected
import com.jervisffb.engine.actions.SelectPlayer
import com.jervisffb.engine.commands.Command
import com.jervisffb.engine.commands.compositeCommandOf
import com.jervisffb.engine.commands.context.RemoveContext
import com.jervisffb.engine.commands.context.SetContext
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
import com.jervisffb.engine.model.context.ProcedureContext
import com.jervisffb.engine.model.context.assertContext
import com.jervisffb.engine.model.context.getContext
import com.jervisffb.engine.rules.Rules
import com.jervisffb.engine.rules.bb2025.procedures.actions.block.BlockAction
import com.jervisffb.engine.rules.common.procedures.actions.blitz.BlitzAction
import com.jervisffb.engine.rules.common.procedures.tables.injury.RiskingInjuryContext
import com.jervisffb.engine.rules.common.procedures.tables.injury.RiskingInjuryMode
import com.jervisffb.engine.rules.common.procedures.tables.injury.RiskingInjuryRoll
import com.jervisffb.engine.utils.INVALID_ACTION

data class StabContext(
    val attacker: Player,
    val defender: Player? = null,
    val stabResult: RiskingInjuryContext? = null,
): ProcedureContext

/**
 * Procedure for handling the "Stab"-part of a Stab Special Action. It is in
 * its own procedure, so we can more easily support Block, Frenzy, and Multiple
 * Block.
 *
 * See [StabAction], [BlockAction] and [BlitzAction].
 */
object StabStep: Procedure() {
    override val initialNode: Node = DecideOnFirstStep
    override fun onEnterProcedure(state: Game, rules: Rules): Command? = null
    override fun onExitProcedure(state: Game, rules: Rules): Command? {
        val context = state.getContext<StabContext>()
        val activateContext = state.getContext<ActivatePlayerContext>()
        return if (context.stabResult != null) {
            // For a Block, this doesn't matter, but during a Blitz, the player is not
            // allowed to move further.
            SetContext(activateContext.copy(activationEndsImmediately = true))
        } else {
            null
        }
    }
    override fun isValid(state: Game, rules: Rules) {
        state.assertContext<StabContext>()
    }

    // During a Blitz, the target is pre-defined, so we can skip this step
    object DecideOnFirstStep: ComputationNode() {
        override fun apply(state: Game, rules: Rules): Command {
            val context = state.getContext<StabContext>()
            return when (context.defender != null) {
                true -> GotoNode(RollForArmourAnInjury)
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
                    .filter { state.field[it].isOccupied() }
                    .filter { state.field[it].player!!.team != attacker.team }
                    .map { state.field[it].player!! }
                    .let { SelectPlayer.fromPlayers(it) }

            return listOf(eligibleDefenders, EndActionWhenReady)
        }
        override fun applyAction(action: GameAction, state: Game, rules: Rules): Command {
            return when (action) {
                EndAction -> ExitProcedure()
                is PlayerSelected -> {
                    val context = state.getContext<StabContext>().copy(defender = action.getPlayer(state))
                    compositeCommandOf(
                        SetContext(context),
                        GotoNode(RollForArmourAnInjury),
                    )
                }
                else -> INVALID_ACTION(action)
            }
        }
    }

    object RollForArmourAnInjury : ParentNode() {
        override fun onEnterNode(state: Game, rules: Rules): Command {
            val context = state.getContext<StabContext>()
            val injuryContext = RiskingInjuryContext(
                player = context.defender!!,
                causedBy = context.attacker,
                mode = RiskingInjuryMode.STAB
            )
            return SetContext(injuryContext)
        }
        override fun getChildProcedure(state: Game, rules: Rules): Procedure = RiskingInjuryRoll
        override fun onExitNode(state: Game, rules: Rules): Command {
            val injuryContext = state.getContext<RiskingInjuryContext>()
            val stabContext = state.getContext<StabContext>()
            return compositeCommandOf(
                SetContext(stabContext.copy(
                    stabResult = injuryContext
                )),
                RemoveContext<RiskingInjuryContext>(),
                ExitProcedure()
            )
        }
    }
}
