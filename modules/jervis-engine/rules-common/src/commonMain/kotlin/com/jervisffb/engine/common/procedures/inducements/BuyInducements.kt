package com.jervisffb.engine.common.procedures.inducements

import com.jervisffb.engine.actions.Cancel
import com.jervisffb.engine.actions.CancelWhenReady
import com.jervisffb.engine.actions.GameAction
import com.jervisffb.engine.actions.GameActionDescriptor
import com.jervisffb.engine.actions.InducementsSelected
import com.jervisffb.engine.actions.SelectInducements
import com.jervisffb.engine.commands.Command
import com.jervisffb.engine.commands.compositeCommandOf
import com.jervisffb.engine.commands.context.AddContext
import com.jervisffb.engine.commands.context.RemoveContext
import com.jervisffb.engine.commands.context.UpdateContext
import com.jervisffb.engine.commands.fsm.ExitProcedure
import com.jervisffb.engine.commands.fsm.GotoNode
import com.jervisffb.engine.common.commands.SetPettyCash
import com.jervisffb.engine.common.commands.SetTreasury
import com.jervisffb.engine.common.context.BuyInducementsContext
import com.jervisffb.engine.fsm.ActionNode
import com.jervisffb.engine.fsm.ComputationNode
import com.jervisffb.engine.fsm.Node
import com.jervisffb.engine.fsm.ParentNode
import com.jervisffb.engine.fsm.Procedure
import com.jervisffb.engine.model.Game
import com.jervisffb.engine.model.Team
import com.jervisffb.engine.model.context.getContext
import com.jervisffb.engine.rules.Rules
import com.jervisffb.engine.utils.INVALID_ACTION
import kotlin.math.min

/**
 * Procedure controlling both teams purchasing inducements as described on
 * page 94 in the BB2025 rulebook.
 */
object BuyInducements : Procedure() {
    // How much gold can the lower ctv team transfer from their treasury to buy inducements.
    override val initialNode: Node = DetermineStartingTeam
    override fun onEnterProcedure(state: Game, rules: Rules): Command? = null
    override fun onExitProcedure(state: Game, rules: Rules): Command {
        return compositeCommandOf(
            RemoveContext<BuyInducementsContext>(),
            SetPettyCash(state.homeTeam, 0),
            SetPettyCash(state.awayTeam, 0)
        )
    }

    object DetermineStartingTeam : ComputationNode() {
        override fun apply(state: Game, rules: Rules): Command {
            val home = state.homeTeam
            val away = state.awayTeam
            val higher: Team
            val lower: Team
            when (home.currentTeamValue > away.currentTeamValue) {
                true -> { higher = home; lower = away }
                false -> { higher = away; lower = home }
            }
            val ctvDiff = higher.currentTeamValue - lower.currentTeamValue
            return compositeCommandOf(
                AddContext(BuyInducementsContext(higher, lower)),
                if (ctvDiff == 0) ExitProcedure() else GotoNode(HigherCtvBuyPurchaseInducements),
            )
        }
    }

    object HigherCtvBuyPurchaseInducements : ActionNode() {
        override fun actionOwner(state: Game, rules: Rules): Team = state.getContext<BuyInducementsContext>().higherCtvTeam
        override fun getAvailableActions(state: Game, rules: Rules): List<GameActionDescriptor> {
            val team = state.getContext<BuyInducementsContext>().higherCtvTeam
            return listOf(CancelWhenReady, SelectInducements(treasury = min(team.treasury, rules.inducements.topDogTopUpLimitFromTreasury), pettyCash = 0))
        }
        override fun applyAction(action: GameAction, state: Game, rules: Rules): Command {
            val context = state.getContext<BuyInducementsContext>()
            return when (action) {
                Cancel -> {
                    compositeCommandOf(
                        SetPettyCash(context.lowerCtvTeam, context.ctvDifference),
                        GotoNode(LowerCtvBuyPurchaseInducements),
                    )
                }
                is InducementsSelected -> {
                    val team = context.higherCtvTeam
                    val totalPrice = action.totalPrice(team)
                    val ctvDifference = context.ctvDifference
                    compositeCommandOf(
                        SetTreasury(team, team.treasury - totalPrice),
                        SetPettyCash(team.otherTeam(), totalPrice + ctvDifference),
                        UpdateContext(context.copy(higherCtvTeamInducements = action)),
                        AddContext(ApplyInducementsContext(team, action)),
                        GotoNode(ApplyInducementsForHigherCtv)
                    )
                }
                else -> INVALID_ACTION(action)
            }
        }
    }

    object ApplyInducementsForHigherCtv : ParentNode() {
        override fun getChildProcedure(state: Game, rules: Rules) = rules.applyInducementsStep
        override fun onExitNode(state: Game, rules: Rules): Command {
            return compositeCommandOf(
                RemoveContext<ApplyInducementsContext>(),
                GotoNode(LowerCtvBuyPurchaseInducements)
            )
        }
    }

    object LowerCtvBuyPurchaseInducements : ActionNode() {
        override fun actionOwner(state: Game, rules: Rules): Team = state.getContext<BuyInducementsContext>().lowerCtvTeam
        override fun getAvailableActions(state: Game, rules: Rules): List<GameActionDescriptor> {
            val context = state.getContext<BuyInducementsContext>()
            return listOf(
                CancelWhenReady,
                SelectInducements(treasury = min(context.lowerCtvTeam.treasury, rules.inducements.underdogTopUpLimitFromTreasury), pettyCash = context.lowerCtvTeam.pettyCash)
            )
        }
        override fun applyAction(action: GameAction, state: Game, rules: Rules): Command {
            return when (action) {
                is Cancel -> ExitProcedure()
                is InducementsSelected -> {
                    val context = state.getContext<BuyInducementsContext>()
                    val team = context.lowerCtvTeam
                    val totalPrice = action.totalPrice(team)
                    val usedFromOwnTreasury = totalPrice - context.ctvDifference
                    compositeCommandOf(
                        if (usedFromOwnTreasury > 0) SetTreasury(team, team.treasury - usedFromOwnTreasury) else null,
                        UpdateContext(context.copy(lowerCtvTeamInducements = action)),
                        AddContext(ApplyInducementsContext(team, action)),
                        GotoNode(ApplyInducementsForLowerCtv)
                    )
                }
                else -> INVALID_ACTION(action)
            }
        }
    }

    object ApplyInducementsForLowerCtv : ParentNode() {
        override fun getChildProcedure(state: Game, rules: Rules) = rules.applyInducementsStep
        override fun onExitNode(state: Game, rules: Rules): Command {
            return compositeCommandOf(
                RemoveContext<ApplyInducementsContext>(),
                ExitProcedure()
            )
        }
    }
}
